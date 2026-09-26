package com.clinic.aiassistant.repository;

import com.clinic.aiassistant.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(p.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(p.symptoms) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "ORDER BY p.createdAt DESC")
    List<Patient> search(@Param("q") String q);

    List<Patient> findAllByOrderByCreatedAtDesc();
}