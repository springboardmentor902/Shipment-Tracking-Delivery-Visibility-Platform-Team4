package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ShipmentRequest {

    @NotBlank
    private String sender;

    @NotBlank
    private String receiver;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    private String currentLocation;

    private LocalDateTime estimatedDelivery;


    public ShipmentRequest() {
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }


    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }


    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(
            LocalDateTime estimatedDelivery
    ) {
        this.estimatedDelivery = estimatedDelivery;
    }
}