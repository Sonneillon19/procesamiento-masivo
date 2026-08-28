package com.example.procesamiento_masivo.controller;

import com.example.procesamiento_masivo.dto.RespuestaError;
import com.example.procesamiento_masivo.dto.RespuestaLote;
import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.repository.ErrorProcesamientoRepository;
import com.example.procesamiento_masivo.service.ProcesamientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/procesamientos")
public class ProcesamientoController {

    private final ProcesamientoService procesamientoService;
    private final ErrorProcesamientoRepository errorRepository;

    public ProcesamientoController(
            ProcesamientoService procesamientoService,
            ErrorProcesamientoRepository errorRepository) {

        this.procesamientoService = procesamientoService;
        this.errorRepository = errorRepository;
    }

    /**
     * Recibe un archivo CSV y lo procesa.
     */
    @PostMapping
    public ResponseEntity<RespuestaLote> procesarArchivo(
            @RequestParam("archivo") MultipartFile archivo) {

        LoteProcesamiento lote =
                procesamientoService.procesarArchivo(archivo);

        return ResponseEntity.ok(
                RespuestaLote.desde(lote)
        );
    }

    /**
     * Permite consultar el estado y los contadores de un lote.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaLote> obtenerEstado(
            @PathVariable Long id) {

        LoteProcesamiento lote =
                procesamientoService.obtenerLote(id);

        return ResponseEntity.ok(
                RespuestaLote.desde(lote)
        );
    }

    /**
     * Devuelve los registros invalidos de una ejecucion determinada.
     */
    @GetMapping("/{id}/errores")
    public ResponseEntity<List<RespuestaError>> obtenerErrores(
            @PathVariable Long id) {

        // Primero validamos que el lote realmente exista.
        procesamientoService.obtenerLote(id);

        List<RespuestaError> errores =
                errorRepository.findByLoteId(id)
                        .stream()
                        .map(RespuestaError::desde)
                        .toList();

        return ResponseEntity.ok(errores);
    }
}