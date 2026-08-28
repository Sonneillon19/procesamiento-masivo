package com.example.procesamiento_masivo.dto;

import java.time.LocalDateTime;

public record RespuestaErrorApi(
        LocalDateTime timestamp,
        int estado,
        String error,
        String mensaje,
        String ruta
) {
}