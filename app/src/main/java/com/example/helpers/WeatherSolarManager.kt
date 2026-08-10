package com.example.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.example.data.WallpaperPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object WeatherSolarManager {

    private const val TAG = "WeatherSolarManager"

    suspend fun updateWeatherAndSolar(context: Context): Boolean = withContext(Dispatchers.IO) {
        val prefs = WallpaperPreferences(context)
        val config = prefs.loadConfig()

        var lat = config.lastLocationLat
        var lng = config.lastLocationLng
        var locationFound = false

        // 1. Try standard Android LocationManager without Google Play Services
        val loc = getAndroidLocation(context)
        if (loc != null) {
            lat = loc.latitude
            lng = loc.longitude
            locationFound = true
            Log.i(TAG, "Ubicación obtenida por LocationManager nativo: $lat, $lng")
        }

        // 2. IP Geolocation Fallback if no GPS/Network location
        if (!locationFound) {
            val ipLoc = getIpLocation()
            if (ipLoc != null) {
                lat = ipLoc.first
                lng = ipLoc.second
                locationFound = true
                Log.i(TAG, "Ubicación obtenida por IP Geolocation fallback: $lat, $lng")
            }
        }

        // Default fallback: Madrid if location unavailable
        if (!locationFound && (lat == 0.0 && lng == 0.0)) {
            lat = 40.4168
            lng = -3.7038
        }

        // 3. Compute Astronomical Offline Sunrise and Sunset
        val offlineSolar = calculateSolarTimes(lat, lng)
        var sunriseStr = offlineSolar.first
        var sunsetStr = offlineSolar.second

        var cityName = if (locationFound) "Ubicación Detectada (Lat: %.2f, Lng: %.2f)".format(lat, lng) else "Ubicación por Defecto"
        var weatherCondition = "CLEAR"
        var tempStr = "--°C"

        // 4. Query Open-Meteo API (Free, open-source, no Google Play / no API key)
        try {
            val apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current_weather=true&daily=sunrise,sunset&timezone=auto"
            val conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)

                if (root.has("timezone")) {
                    val tz = root.optString("timezone", "")
                    if (tz.isNotBlank()) {
                        cityName = tz.replace("_", " ").substringAfter("/")
                    }
                }

                if (root.has("current_weather")) {
                    val currentWeather = root.getJSONObject("current_weather")
                    val temp = currentWeather.optDouble("temperature", 0.0)
                    tempStr = "%.1f°C".format(temp)

                    val code = currentWeather.optInt("weathercode", 0)
                    weatherCondition = mapWmoCodeToCondition(code)
                }

                if (root.has("daily")) {
                    val daily = root.getJSONObject("daily")
                    val sunrises = daily.optJSONArray("sunrise")
                    val sunsets = daily.optJSONArray("sunset")
                    if (sunrises != null && sunrises.length() > 0 && sunsets != null && sunsets.length() > 0) {
                        val rawSunrise = sunrises.getString(0) // e.g. "2026-08-10T06:45"
                        val rawSunset = sunsets.getString(0)   // e.g. "2026-08-10T20:45"
                        if (rawSunrise.contains("T")) {
                            sunriseStr = rawSunrise.substringAfter("T")
                        }
                        if (rawSunset.contains("T")) {
                            sunsetStr = rawSunset.substringAfter("T")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo consultar Open-Meteo online, usando cálculo astronómico offline: ${e.message}")
        }

        prefs.updateWeatherData(
            cityName = cityName,
            condition = weatherCondition,
            temp = tempStr,
            sunrise = sunriseStr,
            sunset = sunsetStr,
            lat = lat,
            lng = lng
        )

        Log.i(TAG, "Clima y Sol actualizados: Ciudad=$cityName, Condición=$weatherCondition, Temp=$tempStr, Sol=$sunriseStr - $sunsetStr")
        true
    }

    @SuppressLint("MissingPermission")
    private fun getAndroidLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var bestLoc: Location? = null
            for (provider in providers) {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLoc == null || loc.time > bestLoc.time) {
                            bestLoc = loc
                        }
                    }
                }
            }
            bestLoc
        } catch (e: SecurityException) {
            Log.w(TAG, "Permiso de ubicación no otorgado aún.")
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getIpLocation(): Pair<Double, Double>? {
        return try {
            val conn = URL("https://ip-api.com/json/").openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                if (json.optString("status") == "success") {
                    val lat = json.optDouble("lat", 0.0)
                    val lon = json.optDouble("lon", 0.0)
                    if (lat != 0.0 || lon != 0.0) Pair(lat, lon) else null
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun mapWmoCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "CLEAR" // Despejado / Soleado
            1, 2, 3, 45, 48 -> "CLOUDS" // Nublado / Niebla
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99 -> "RAIN" // Lluvia / Tormenta
            71, 73, 75, 77, 85, 86 -> "SNOW" // Nieve
            else -> "CLEAR"
        }
    }

    /**
     * Algoritmo de cálculo astronómico de Amanecer y Atardecer en Kotlin.
     * Funciona 100% Offline para cualquier latitud y longitud sin llamadas a servidor.
     */
    fun calculateSolarTimes(lat: Double, lng: Double, date: Date = Date()): Pair<String, String> {
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val zenith = 90.8333 // Standard zenith for official sunrise/sunset

        // Convert lat/lng to radians
        val latRad = Math.toRadians(lat)

        // Calculate approximate time in days
        val lngHour = lng / 15.0

        // Sunrise
        val tRise = dayOfYear + ((6.0 - lngHour) / 24.0)
        val mRise = (0.9856 * tRise) - 3.289
        var lRise = mRise + (1.916 * sin(Math.toRadians(mRise))) + (0.020 * sin(Math.toRadians(2 * mRise))) + 282.634
        lRise = fix360(lRise)

        var raRise = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(lRise))))
        raRise = fix360(raRise)

        val lQuadrantRise = floor(lRise / 90.0) * 90.0
        val raQuadrantRise = floor(raRise / 90.0) * 90.0
        raRise += (lQuadrantRise - raQuadrantRise)
        raRise /= 15.0

        val sinDecRise = 0.39782 * sin(Math.toRadians(lRise))
        val cosDecRise = cos(Math.asin(sinDecRise))

        val cosHRise = (cos(Math.toRadians(zenith)) - (sinDecRise * sin(latRad))) / (cosDecRise * cos(latRad))

        var sunriseHour = 6.0
        if (cosHRise in -1.0..1.0) {
            val hRise = (360.0 - Math.toDegrees(acos(cosHRise))) / 15.0
            val tLocalRise = hRise + raRise - (0.06571 * tRise) - 6.622
            var utRise = tLocalRise - lngHour
            utRise = fix24(utRise)

            val tzOffset = TimeZone.getDefault().getOffset(date.time) / 3600000.0
            sunriseHour = fix24(utRise + tzOffset)
        }

        // Sunset
        val tSet = dayOfYear + ((18.0 - lngHour) / 24.0)
        val mSet = (0.9856 * tSet) - 3.289
        var lSet = mSet + (1.916 * sin(Math.toRadians(mSet))) + (0.020 * sin(Math.toRadians(2 * mSet))) + 282.634
        lSet = fix360(lSet)

        var raSet = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(lSet))))
        raSet = fix360(raSet)

        val lQuadrantSet = floor(lSet / 90.0) * 90.0
        val raQuadrantSet = floor(raSet / 90.0) * 90.0
        raSet += (lQuadrantSet - raQuadrantSet)
        raSet /= 15.0

        val sinDecSet = 0.39782 * sin(Math.toRadians(lSet))
        val cosDecSet = cos(Math.asin(sinDecSet))

        val cosHSet = (cos(Math.toRadians(zenith)) - (sinDecSet * sin(latRad))) / (cosDecSet * cos(latRad))

        var sunsetHour = 20.0
        if (cosHSet in -1.0..1.0) {
            val hSet = Math.toDegrees(acos(cosHSet)) / 15.0
            val tLocalSet = hSet + raSet - (0.06571 * tSet) - 6.622
            var utSet = tLocalSet - lngHour
            utSet = fix24(utSet)

            val tzOffset = TimeZone.getDefault().getOffset(date.time) / 3600000.0
            sunsetHour = fix24(utSet + tzOffset)
        }

        val srH = floor(sunriseHour).toInt()
        val srM = floor((sunriseHour - srH) * 60).toInt()

        val ssH = floor(sunsetHour).toInt()
        val ssM = floor((sunsetHour - ssH) * 60).toInt()

        val sunriseFormatted = "%02d:%02d".format(srH.coerceIn(0, 23), srM.coerceIn(0, 59))
        val sunsetFormatted = "%02d:%02d".format(ssH.coerceIn(0, 23), ssM.coerceIn(0, 59))

        return Pair(sunriseFormatted, sunsetFormatted)
    }

    private fun fix360(valIn: Double): Double {
        var v = valIn % 360.0
        if (v < 0) v += 360.0
        return v
    }

    private fun fix24(valIn: Double): Double {
        var v = valIn % 24.0
        if (v < 0) v += 24.0
        return v
    }
}
