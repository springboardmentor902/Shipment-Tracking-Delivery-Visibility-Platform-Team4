package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(Shipment.ShipmentStatus status);

    // ✅ Regular method without pagination
    List<Shipment> findByCreatedByUserId(Long userId);

    // ✅ Paginated version
    Page<Shipment> findByCreatedByUserId(Long userId, Pageable pageable);

    List<Shipment> findByBusinessId(Long businessId);

    List<Shipment> findByAssignedOperatorId(Long operatorId);

    @Query("SELECT s FROM Shipment s WHERE " +
            "LOWER(s.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.senderName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.recipientName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Shipment> searchShipments(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Shipment s WHERE s.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    List<Shipment> findActiveShipments();

    @Query("SELECT s FROM Shipment s WHERE s.createdByUserId = :userId AND s.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Shipment> findActiveShipmentsByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Shipment s WHERE s.expectedDeliveryDate < CURRENT_TIMESTAMP AND s.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Shipment> findDelayedShipments();
}