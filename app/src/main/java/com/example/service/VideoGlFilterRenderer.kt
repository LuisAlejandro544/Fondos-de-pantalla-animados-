package com.example.service

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.example.data.WallpaperConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoGlFilterRenderer(private val surfaceHolder: SurfaceHolder) : SurfaceTexture.OnFrameAvailableListener {

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    private var textureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var videoSurface: Surface? = null

    private var program: Int = 0
    private var aPositionHandle: Int = 0
    private var aTextureHandle: Int = 0
    private var uMVPMatrixHandle: Int = 0
    private var uSTMatrixHandle: Int = 0

    private var uBlurRadiusHandle: Int = 0
    private var uBrightnessHandle: Int = 0
    private var uContrastHandle: Int = 0
    private var uSaturationHandle: Int = 0
    private var uColorTintHandle: Int = 0

    private val mvpMatrix = FloatArray(16)
    private val stMatrix = FloatArray(16)

    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
         1.0f,  1.0f, 0.0f, 1.0f, 1.0f
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    @Volatile
    private var frameAvailable = false
    private var isInitialized = false

    var currentConfig: WallpaperConfig? = null

    fun initialize(): Surface? {
        try {
            initEgl()
            initGl()
            isInitialized = true
            return videoSurface
        } catch (e: Exception) {
            Log.e("VideoGlFilterRenderer", "Error inicializando OpenGL ES 2.0 Renderer: ${e.message}")
            release()
            return null
        }
    }

    private fun initEgl() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) throw RuntimeException("eglGetDisplay failed")

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) throw RuntimeException("eglInitialize failed")

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)
        val config = configs[0] ?: throw RuntimeException("eglChooseConfig failed")

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(eglDisplay, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (eglContext == EGL14.EGL_NO_CONTEXT) throw RuntimeException("eglCreateContext failed")

        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, config, surfaceHolder.surface, intArrayOf(EGL14.EGL_NONE), 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) throw RuntimeException("eglCreateWindowSurface failed")

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    private fun initGl() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener(this@VideoGlFilterRenderer)
        }
        videoSurface = Surface(surfaceTexture)

        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) throw RuntimeException("Failed creating GLSL program")

        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix")

        uBlurRadiusHandle = GLES20.glGetUniformLocation(program, "uBlurRadius")
        uBrightnessHandle = GLES20.glGetUniformLocation(program, "uBrightness")
        uContrastHandle = GLES20.glGetUniformLocation(program, "uContrast")
        uSaturationHandle = GLES20.glGetUniformLocation(program, "uSaturation")
        uColorTintHandle = GLES20.glGetUniformLocation(program, "uColorTint")

        Matrix.setIdentityM(mvpMatrix, 0)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        frameAvailable = true
        drawFrame()
    }

    fun drawFrame() {
        if (!isInitialized) return
        val st = surfaceTexture ?: return

        try {
            if (frameAvailable) {
                st.updateTexImage()
                st.getTransformMatrix(stMatrix)
                frameAvailable = false
            }

            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
            GLES20.glUseProgram(program)

            GLES20.glViewport(0, 0, surfaceHolder.surfaceFrame.width(), surfaceHolder.surfaceFrame.height())
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            vertexBuffer.position(0)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 20, vertexBuffer)
            GLES20.glEnableVertexAttribArray(aPositionHandle)

            vertexBuffer.position(3)
            GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false, 20, vertexBuffer)
            GLES20.glEnableVertexAttribArray(aTextureHandle)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, stMatrix, 0)

            val cfg = currentConfig
            val blur = cfg?.blurRadius ?: 0.0f
            val bright = cfg?.brightness ?: 0.0f
            val contrast = cfg?.contrast ?: 1.0f
            val sat = cfg?.saturation ?: 1.0f

            GLES20.glUniform1f(uBlurRadiusHandle, blur)
            GLES20.glUniform1f(uBrightnessHandle, bright)
            GLES20.glUniform1f(uContrastHandle, contrast)
            GLES20.glUniform1f(uSaturationHandle, sat)

            val tint = when (cfg?.colorFilterMode) {
                "LAUNCHER_DARK" -> floatArrayOf(0.0f, 0.0f, 0.0f, 0.35f)
                "SEPIA" -> floatArrayOf(0.4f, 0.25f, 0.1f, 0.30f)
                "CYBERPUNK" -> floatArrayOf(0.1f, 0.0f, 0.3f, 0.25f)
                "NIGHT_WARM" -> floatArrayOf(0.3f, 0.15f, 0.0f, 0.25f)
                else -> floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
            }
            GLES20.glUniform4fv(uColorTintHandle, 1, tint, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            EGL14.eglSwapBuffers(eglDisplay, eglSurface)
        } catch (e: Exception) {
            Log.e("VideoGlFilterRenderer", "Error dibujando cuadro con filtros: ${e.message}")
        }
    }

    fun release() {
        isInitialized = false
        videoSurface?.release()
        videoSurface = null
        surfaceTexture?.release()
        surfaceTexture = null

        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(eglDisplay, eglSurface)
            if (eglContext != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglTerminate(eglDisplay)
        }
        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (vertexShader == 0 || fragmentShader == 0) return 0

        var prog = GLES20.glCreateProgram()
        if (prog != 0) {
            GLES20.glAttachShader(prog, vertexShader)
            GLES20.glAttachShader(prog, fragmentShader)
            GLES20.glLinkProgram(prog)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                GLES20.glDeleteProgram(prog)
                prog = 0
            }
        }
        return prog
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            uniform mat4 uMVPMatrix;
            uniform mat4 uSTMatrix;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTextureCoord = (uSTMatrix * vec4(aTextureCoord, 0.0, 1.0)).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;
            uniform float uBlurRadius;
            uniform float uBrightness;
            uniform float uContrast;
            uniform float uSaturation;
            uniform vec4 uColorTint;

            void main() {
                vec4 color = texture2D(sTexture, vTextureCoord);

                if (uBlurRadius > 0.01) {
                    float step = uBlurRadius * 0.0035;
                    vec4 sum = color * 0.2270270270;
                    sum += texture2D(sTexture, vTextureCoord + vec2(step, 0.0)) * 0.1945945946;
                    sum += texture2D(sTexture, vTextureCoord - vec2(step, 0.0)) * 0.1945945946;
                    sum += texture2D(sTexture, vTextureCoord + vec2(0.0, step)) * 0.1945945946;
                    sum += texture2D(sTexture, vTextureCoord - vec2(0.0, step)) * 0.1945945946;
                    color = sum;
                }

                color.rgb += uBrightness;
                color.rgb = (color.rgb - 0.5) * uContrast + 0.5;

                float gray = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
                color.rgb = mix(vec3(gray), color.rgb, uSaturation);

                if (uColorTint.a > 0.0) {
                    color.rgb = mix(color.rgb, uColorTint.rgb, uColorTint.a);
                }

                gl_FragColor = color;
            }
        """
    }
}
