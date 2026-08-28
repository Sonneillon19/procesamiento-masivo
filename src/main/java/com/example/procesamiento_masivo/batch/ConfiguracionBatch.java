package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.repository.ErrorProcesamientoRepository;
import com.example.procesamiento_masivo.repository.LoteProcesamientoRepository;
import com.example.procesamiento_masivo.repository.TransaccionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ConfiguracionBatch {

    private static final int TAMANO_CHUNK = 100;

    @Bean
    public ItemReader<RegistroCsvTransaccion> lectorCsvTransacciones() {
        return new LectorCsvTransacciones("archivo.csv");
    }

    @Bean
    public ItemProcessor<RegistroCsvTransaccion, ResultadoProcesamiento>
    procesadorTransaccion() {

        LoteProcesamiento lote = new LoteProcesamiento();

        return new ProcesadorTransaccion(lote);
    }

    @Bean
    public ItemWriter<ResultadoProcesamiento> escritorResultadoProcesamiento(
            TransaccionRepository transaccionRepository,
            ErrorProcesamientoRepository errorProcesamientoRepository) {

        return new EscritorResultadoProcesamiento(
                transaccionRepository,
                errorProcesamientoRepository
        );
    }

    @Bean
    public Step pasoProcesarTransacciones(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<RegistroCsvTransaccion> lectorCsvTransacciones,
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
            Step pasoProcesarTransacciones) {

        return new JobBuilder(
                "procesarTransaccionesJob",
                jobRepository
        )
                .start(pasoProcesarTransacciones)
                .build();
    }
}