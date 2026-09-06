package com.shiptrack.shiptrack_pro.dto;

import java.util.List;
import java.util.Map;

public class CustomerAnalyticsResponse {

    private long totalShipments;
    private long activeShipments;
    private long deliveredShipments;
    private long delayedShipments;

    private Map<String, Long> statusBreakdown;

    private List<ShipmentHistoryItem> shipmentHistory;

    private TrackingInsights trackingInsights;


    public long getTotalShipments() {
        return totalShipments;
    }

    public void setTotalShipments(long totalShipments) {
        this.totalShipments = totalShipments;
    }

    public long getActiveShipments() {
        return activeShipments;
    }

    public void setActiveShipments(long activeShipments) {
        this.activeShipments = activeShipments;
    }

    public long getDeliveredShipments() {
        return deliveredShipments;
    }

    public void setDeliveredShipments(long deliveredShipments) {
        this.deliveredShipments = deliveredShipments;
    }

    public long getDelayedShipments() {
        return delayedShipments;
    }

    public void setDelayedShipments(long delayedShipments) {
        this.delayedShipments = delayedShipments;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public List<ShipmentHistoryItem> getShipmentHistory() {
        return shipmentHistory;
    }

    public void setShipmentHistory(List<ShipmentHistoryItem> shipmentHistory) {
        this.shipmentHistory = shipmentHistory;
    }

    public TrackingInsights getTrackingInsights() {
        return trackingInsights;
    }

    public void setTrackingInsights(TrackingInsights trackingInsights) {
        this.trackingInsights = trackingInsights;
    }


    public static class ShipmentHistoryItem {

        private Long id;
        private String trackingNumber;
        private String origin;
        private String destination;
        private String status;
        private String currentLocation;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(String currentLocation) {
            this.currentLocation = currentLocation;
        }
    }


    public static class TrackingInsights {

        private long shipmentsWithLocation;
        private long shipmentsWithoutLocation;
        private long shipmentsOutForDelivery;

        public long getShipmentsWithLocation() {
            return shipmentsWithLocation;
        }

        public void setShipmentsWithLocation(long shipmentsWithLocation) {
            this.shipmentsWithLocation = shipmentsWithLocation;
        }

        public long getShipmentsWithoutLocation() {
            return shipmentsWithoutLocation;
        }

        public void setShipmentsWithoutLocation(long shipmentsWithoutLocation) {
            this.shipmentsWithoutLocation = shipmentsWithoutLocation;
        }

        public long getShipmentsOutForDelivery() {
            return shipmentsOutForDelivery;
        }

        public void setShipmentsOutForDelivery(long shipmentsOutForDelivery) {
            this.shipmentsOutForDelivery = shipmentsOutForDelivery;
        }
    }
}