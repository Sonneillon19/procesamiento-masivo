package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.repository.ErrorProcesamientoRepository;
import com.example.procesamiento_masivo.repository.LoteProcesamientoRepository;
import com.example.procesamiento_masivo.repository.TransaccionRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.LocalDateTime;

public class ListenerLoteProcesamiento implements JobExecutionListener {

    private final Long loteId;
    private final LoteProcesamientoRepository loteRepository;
    private final TransaccionRepository transaccionRepository;
    private final ErrorProcesamientoRepository errorRepository;

    public ListenerLoteProcesamiento(
            Long loteId,
            LoteProcesamientoRepository loteRepository,
            TransaccionRepository transaccionRepository,
            ErrorProcesamientoRepository errorRepository) {

        this.loteId = loteId;
        this.loteRepository = loteRepository;
        this.transaccionRepository = transaccionRepository;
        this.errorRepository = errorRepository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

        LoteProcesamiento lote = obtenerLote();

        lote.setEstado("PROCESANDO");

        /*
         * Conservamos la fecha de inicio registrada al crear el lote
         * Si por alguna razón no existe se establece al iniciar el Job
         */
        if (lote.getFechaInicio() == null) {
            lote.setFechaInicio(LocalDateTime.now());
        }

        loteRepository.save(lote);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        LoteProcesamiento lote = obtenerLote();

        long exitosos = transaccionRepository.countByLoteId(loteId);
        long fallidos = errorRepository.countByLoteId(loteId);

        lote.setRegistrosExitosos(exitosos);
        lote.setRegistrosFallidos(fallidos);
        lote.setTotalRegistros(exitosos + fallidos);
        lote.setFechaFin(LocalDateTime.now());

        if (jobExecution.getStatus().isUnsuccessful()) {
            lote.setEstado("FALLIDO");
        } else {
            lote.setEstado("COMPLETADO");
        }

        loteRepository.save(lote);
    }

    private LoteProcesamiento obtenerLote() {

        return loteRepository.findById(loteId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el lote de procesamiento con id: " + loteId
                        )
                );
    }
}