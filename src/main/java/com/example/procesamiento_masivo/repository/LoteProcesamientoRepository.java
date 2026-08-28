package com.example.procesamiento_masivo.repository;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteProcesamientoRepository extends JpaRepository<LoteProcesamiento, Long> {
}