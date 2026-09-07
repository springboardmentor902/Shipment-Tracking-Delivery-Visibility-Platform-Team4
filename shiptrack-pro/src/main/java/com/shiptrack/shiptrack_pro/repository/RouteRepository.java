package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // ===== BASIC QUERIES =====
    Optional<Route> findByShipmentId(Long shipmentId);

    List<Route> findByAssignedDriverId(Long driverId);

    List<Route> findByStatus(String status);

    // ===== ✅ NEW: ROUTE HISTORY & CURRENT =====

    @Query("SELECT r FROM Route r WHERE r.shipmentId = :shipmentId ORDER BY r.createdAt DESC")
    List<Route> findByShipmentIdOrderByCreatedAtDesc(@Param("shipmentId") Long shipmentId);

    @Query("SELECT r FROM Route r WHERE r.shipmentId = :shipmentId AND r.isCurrentRoute = true")
    Optional<Route> findByShipmentIdAndIsCurrentRoute(@Param("shipmentId") Long shipmentId);

    @Query("SELECT r FROM Route r WHERE r.isCurrentRoute = true")
    List<Route> findAllCurrentRoutes();

    // ===== ACTIVE & DELAYED =====

    @Query("SELECT r FROM Route r WHERE r.status IN ('IN_TRANSIT', 'DELAYED')")
    List<Route> findActiveRoutes();

    @Query("SELECT r FROM Route r WHERE r.eta < CURRENT_TIMESTAMP AND r.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Route> findDelayedRoutes();
}