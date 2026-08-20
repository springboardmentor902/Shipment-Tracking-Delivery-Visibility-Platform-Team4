package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.BusinessAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {

    Optional<BusinessAccount> findByUserId(Long userId);

    List<BusinessAccount> findByIsVerified(Boolean isVerified);

    boolean existsByUserId(Long userId);

    Optional<BusinessAccount> findByGstNumber(String gstNumber);

    Optional<BusinessAccount> findByPanNumber(String panNumber);
}