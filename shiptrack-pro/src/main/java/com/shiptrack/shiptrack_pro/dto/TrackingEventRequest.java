package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public class TrackingEventRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    private String locationText;

    private String notes;

    private Double latitude;

    private Double longitude;

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}