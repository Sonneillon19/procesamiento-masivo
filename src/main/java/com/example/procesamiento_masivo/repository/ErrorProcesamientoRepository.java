package com.example.procesamiento_masivo.repository;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErrorProcesamientoRepository extends JpaRepository<ErrorProcesamiento, Long> {

    List<ErrorProcesamiento> findByLoteId(Long loteId);
}