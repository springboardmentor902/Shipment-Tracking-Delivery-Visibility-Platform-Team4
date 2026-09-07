package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    List<Package> findByShipmentId(Long shipmentId);

    void deleteByShipmentId(Long shipmentId);
}