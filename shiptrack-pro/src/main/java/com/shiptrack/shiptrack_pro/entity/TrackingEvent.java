package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private EventStatus status;

    @Column(name = "recorded_by_user_id")
    private Long recordedByUserId;

    @Column(name = "recorded_by_driver_id")
    private Long recordedByDriverId;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum EventType {
        CREATED,
        PICKED_UP,
        IN_TRANSIT,
        LOCATION_UPDATE,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED_DELIVERY,
        DELAYED,
        CANCELLED,
        EXCEPTION
    }

    public enum EventStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}