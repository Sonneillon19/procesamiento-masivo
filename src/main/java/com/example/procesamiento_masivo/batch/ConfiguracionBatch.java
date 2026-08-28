package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.repository.ErrorProcesamientoRepository;
import com.example.procesamiento_masivo.repository.LoteProcesamientoRepository;
import com.example.procesamiento_masivo.repository.TransaccionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ConfiguracionBatch {

    private static final int TAMANO_CHUNK = 100;

    @Bean
    @StepScope
    public ItemStreamReader<RegistroCsvTransaccion> lectorCsvTransacciones(
            @Value("#{jobParameters['rutaArchivo']}") String rutaArchivo) {

        return new LectorCsvTransacciones(rutaArchivo);
    }

    @Bean
    @StepScope
    public ItemProcessor<RegistroCsvTransaccion, ResultadoProcesamiento>
    procesadorTransaccion(
            @Value("#{jobParameters['loteId']}") Long loteId,
            LoteProcesamientoRepository loteRepository) {

        LoteProcesamiento lote = loteRepository.findById(loteId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el lote de procesamiento con id: " + loteId
                        )
                );

        return new ProcesadorTransaccion(lote);
    }

    @Bean
    public ItemWriter<ResultadoProcesamiento> escritorResultadoProcesamiento(
            JdbcTemplate jdbcTemplate) {

        return new EscritorResultadoProcesamiento(
                jdbcTemplate
        );
    }

    @Bean
    @JobScope
    public ListenerLoteProcesamiento listenerLoteProcesamiento(
            @Value("#{jobParameters['loteId']}") Long loteId,
            LoteProcesamientoRepository loteRepository,
            TransaccionRepository transaccionRepository,
            ErrorProcesamientoRepository errorRepository) {

        return new ListenerLoteProcesamiento(
                loteId,
                loteRepository,
                transaccionRepository,
                errorRepository
        );
    }

    @Bean
    public Step pasoProcesarTransacciones(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<RegistroCsvTransaccion> lectorCsvTransacciones,
            ItemProcessor<RegistroCsvTransaccion, ResultadoProcesamiento> procesadorTransaccion,
            ItemWriter<ResultadoProcesamiento> escritorResultadoProcesamiento) {

        return new StepBuilder(
                "pasoProcesarTransacciones",
                jobRepository
        )
                .<RegistroCsvTransaccion, ResultadoProcesamiento>chunk(
                        TAMANO_CHUNK,
                        transactionManager
                )
                .reader(lectorCsvTransacciones)
                .processor(procesadorTransaccion)
                .writer(escritorResultadoProcesamiento)
                .build();
    }

    @Bean
    public Job procesarTransaccionesJob(
            JobRepository jobRepository,
            Step pasoProcesarTransacciones,
            ListenerLoteProcesamiento listenerLoteProcesamiento) {

        return new JobBuilder(
                "procesarTransaccionesJob",
                jobRepository
        )
                .listener(listenerLoteProcesamiento)
                .start(pasoProcesarTransacciones)
                .build();
    }
}