package com.example.procesamiento_masivo.dto;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;

public record RespuestaError(
        Long numeroLinea,
        String registroOriginal,
        String motivoError
) {

    public static RespuestaError desde(ErrorProcesamiento error) {

        return new RespuestaError(
                error.getNumeroLinea(),
                error.getRegistroOriginal(),
                error.getMotivoError()
        );
    }
}