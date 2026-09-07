package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipmentIdOrderByEventTimestampDesc(Long shipmentId);

    List<TrackingEvent> findByRouteIdOrderByEventTimestampAsc(Long routeId);

    List<TrackingEvent> findByEventType(TrackingEvent.EventType eventType);

    List<TrackingEvent> findByStatus(TrackingEvent.EventStatus status);

    @Query("SELECT te FROM TrackingEvent te WHERE te.shipmentId = :shipmentId ORDER BY te.eventTimestamp ASC")
    List<TrackingEvent> findTrackingHistoryByShipment(@Param("shipmentId") Long shipmentId);

    @Query("SELECT te FROM TrackingEvent te WHERE te.shipmentId = :shipmentId AND te.eventType = 'LOCATION_UPDATE' ORDER BY te.eventTimestamp DESC")
    List<TrackingEvent> findLatestLocations(@Param("shipmentId") Long shipmentId);

    @Query("SELECT te FROM TrackingEvent te WHERE te.eventTimestamp BETWEEN :start AND :end")
    List<TrackingEvent> findEventsByDateRange(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}