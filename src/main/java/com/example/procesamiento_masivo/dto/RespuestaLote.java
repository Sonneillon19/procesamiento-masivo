package com.example.procesamiento_masivo.dto;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;

import java.time.LocalDateTime;

public record RespuestaLote(
        Long id,
        String nombreArchivo,
        String estado,
        Long totalRegistros,
        Long registrosExitosos,
        Long registrosFallidos,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {

    public static RespuestaLote desde(LoteProcesamiento lote) {

        return new RespuestaLote(
                lote.getId(),
                lote.getNombreArchivo(),
                lote.getEstado(),
                lote.getTotalRegistros(),
                lote.getRegistrosExitosos(),
                lote.getRegistrosFallidos(),
                lote.getFechaInicio(),
                lote.getFechaFin()
        );
    }
}