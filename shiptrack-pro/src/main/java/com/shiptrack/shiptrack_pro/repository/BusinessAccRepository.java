package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.BusinessAcc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessAccRepository
        extends JpaRepository<BusinessAcc, Long> {

    Optional<BusinessAcc> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}