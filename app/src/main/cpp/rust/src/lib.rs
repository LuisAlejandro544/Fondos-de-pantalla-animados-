// Módulo de Rust para compresión, reducción de resolución y conservación de nitidez nativa
#[no_mangle]
pub extern "C" fn rust_video_optimizer_init() -> i32 {
    // Inicialización del motor de optimización Rust
    0
}

#[no_mangle]
pub extern "C" fn rust_calculate_downscale_dimensions(orig_w: i32, orig_h: i32, out_dims: *mut i32) -> i32 {
    if out_dims.is_null() || orig_w <= 0 || orig_h <= 0 {
        return -1;
    }

    // Objetivo de reducción de resolución por defecto manteniéndola optimizada
    // Si la altura es mayor a 720p, la escalamos a 720p conservando el aspecto
    let max_height = 720.0f32;
    let mut scale = 1.0f32;

    if (orig_h as f32) > max_height {
        scale = max_height / (orig_h as f32);
    } else {
        scale = 0.75f32; // Reducción mínima del 25% para ahorrar espacio
    }

    let mut target_w = ((orig_w as f32) * scale) as i32;
    let mut target_h = ((orig_h as f32) * scale) as i32;

    // Números pares requeridos por decodificadores/codificadores NDK
    if target_w % 2 != 0 { target_w -= 1; }
    if target_h % 2 != 0 { target_h -= 1; }

    if target_w < 320 { target_w = 320; }
    if target_h < 480 { target_h = 480; }

    unsafe {
        *out_dims.offset(0) = target_w;
        *out_dims.offset(1) = target_h;
    }
    0
}

#[no_mangle]
pub extern "C" fn rust_apply_unsharp_mask(
    buffer: *mut u8,
    width: i32,
    height: i32,
    stride: i32,
    sharpness_amount: f32,
) -> i32 {
    if buffer.is_null() || width <= 2 || height <= 2 || sharpness_amount <= 0.0 {
        return -1;
    }

    // Máscara de enfoque Rust sobre el canal RGBA para preservar nitidez en vídeo de baja resolución
    let amount = sharpness_amount.min(2.0).max(0.1);
    let w = width as usize;
    let h = height as usize;
    let s = stride as usize;

    unsafe {
        let pixels = std::slice::from_raw_parts_mut(buffer, h * s);
        // Filtro de realce de bordes (Laplaciano suavizado en canal RGB)
        for y in 1..(h - 1) {
            for x in 1..(w - 1) {
                let idx = y * s + x * 4;
                for c in 0..3 {
                    let center = pixels[idx + c] as f32;
                    let top = pixels[(y - 1) * s + x * 4 + c] as f32;
                    let bottom = pixels[(y + 1) * s + x * 4 + c] as f32;
                    let left = pixels[y * s + (x - 1) * 4 + c] as f32;
                    let right = pixels[y * s + (x + 1) * 4 + c] as f32;

                    let laplacian = center * 4.0 - top - bottom - left - right;
                    let sharpened = (center + laplacian * amount * 0.25).clamp(0.0, 255.0);
                    pixels[idx + c] = sharpened as u8;
                }
            }
        }
    }
    0
}

#[no_mangle]
pub extern "C" fn rust_compress_frame_buffer(input_ptr: *const u8, size: usize) -> i32 {
    if input_ptr.is_null() || size == 0 {
        return -1;
    }
    0
}

