package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
=======
public interface RouteRepository extends JpaRepository<Route, Long> {
>>>>>>> origin/intern_shreya
}