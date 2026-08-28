package com.example.procesamiento_masivo.controller;

import com.example.procesamiento_masivo.dto.RespuestaErrorApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespuestaErrorApi> manejarSolicitudIncorrecta(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        RespuestaErrorApi respuesta = new RespuestaErrorApi(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RespuestaErrorApi> manejarErrorInterno(
            IllegalStateException ex,
            HttpServletRequest request) {

        RespuestaErrorApi respuesta = new RespuestaErrorApi(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta);
    }
}