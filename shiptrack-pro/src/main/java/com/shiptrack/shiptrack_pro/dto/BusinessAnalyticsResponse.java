package com.shiptrack.shiptrack_pro.dto;

import java.util.List;
import java.util.Map;

public class BusinessAnalyticsResponse {

    private long totalShipments;
    private long activeShipments;
    private long deliveredShipments;
    private long delayedShipments;
    private long failedDeliveries;

    private double deliverySuccessRate;
    private double deliveryFailureRate;

    private Map<String, Long> shipmentStatusBreakdown;

    private List<RouteAnalytics> routePerformance;

    private List<CustomerActivity> customerActivity;

    private DelayAnalysis delayAnalysis;

    private LogisticsOverview logisticsOverview;


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

    public long getFailedDeliveries() {
        return failedDeliveries;
    }

    public void setFailedDeliveries(long failedDeliveries) {
        this.failedDeliveries = failedDeliveries;
    }

    public double getDeliverySuccessRate() {
        return deliverySuccessRate;
    }

    public void setDeliverySuccessRate(double deliverySuccessRate) {
        this.deliverySuccessRate = deliverySuccessRate;
    }

    public double getDeliveryFailureRate() {
        return deliveryFailureRate;
    }

    public void setDeliveryFailureRate(double deliveryFailureRate) {
        this.deliveryFailureRate = deliveryFailureRate;
    }

    public Map<String, Long> getShipmentStatusBreakdown() {
        return shipmentStatusBreakdown;
    }

    public void setShipmentStatusBreakdown(
            Map<String, Long> shipmentStatusBreakdown) {
        this.shipmentStatusBreakdown = shipmentStatusBreakdown;
    }

    public List<RouteAnalytics> getRoutePerformance() {
        return routePerformance;
    }

    public void setRoutePerformance(List<RouteAnalytics> routePerformance) {
        this.routePerformance = routePerformance;
    }

    public List<CustomerActivity> getCustomerActivity() {
        return customerActivity;
    }

    public void setCustomerActivity(
            List<CustomerActivity> customerActivity) {
        this.customerActivity = customerActivity;
    }

    public DelayAnalysis getDelayAnalysis() {
        return delayAnalysis;
    }

    public void setDelayAnalysis(DelayAnalysis delayAnalysis) {
        this.delayAnalysis = delayAnalysis;
    }

    public LogisticsOverview getLogisticsOverview() {
        return logisticsOverview;
    }

    public void setLogisticsOverview(
            LogisticsOverview logisticsOverview) {
        this.logisticsOverview = logisticsOverview;
    }


    public static class RouteAnalytics {

        private String route;
        private long shipmentCount;
        private long deliveredCount;

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public long getShipmentCount() {
            return shipmentCount;
        }

        public void setShipmentCount(long shipmentCount) {
            this.shipmentCount = shipmentCount;
        }

        public long getDeliveredCount() {
            return deliveredCount;
        }

        public void setDeliveredCount(long deliveredCount) {
            this.deliveredCount = deliveredCount;
        }
    }


    public static class CustomerActivity {

        private String customer;
        private long shipmentCount;

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public long getShipmentCount() {
            return shipmentCount;
        }

        public void setShipmentCount(long shipmentCount) {
            this.shipmentCount = shipmentCount;
        }
    }


    public static class DelayAnalysis {

        private long delayedShipments;
        private long onTimeShipments;
        private long shipmentsWithoutEta;

        public long getDelayedShipments() {
            return delayedShipments;
        }

        public void setDelayedShipments(long delayedShipments) {
            this.delayedShipments = delayedShipments;
        }

        public long getOnTimeShipments() {
            return onTimeShipments;
        }

        public void setOnTimeShipments(long onTimeShipments) {
            this.onTimeShipments = onTimeShipments;
        }

        public long getShipmentsWithoutEta() {
            return shipmentsWithoutEta;
        }

        public void setShipmentsWithoutEta(long shipmentsWithoutEta) {
            this.shipmentsWithoutEta = shipmentsWithoutEta;
        }
    }


    public static class LogisticsOverview {

        private long created;
        private long pickedUp;
        private long inTransit;
        private long outForDelivery;
        private long delivered;
        private long failedDelivery;
        private long cancelled;

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getPickedUp() {
            return pickedUp;
        }

        public void setPickedUp(long pickedUp) {
            this.pickedUp = pickedUp;
        }

        public long getInTransit() {
            return inTransit;
        }

        public void setInTransit(long inTransit) {
            this.inTransit = inTransit;
        }

        public long getOutForDelivery() {
            return outForDelivery;
        }

        public void setOutForDelivery(long outForDelivery) {
            this.outForDelivery = outForDelivery;
        }

        public long getDelivered() {
            return delivered;
        }

        public void setDelivered(long delivered) {
            this.delivered = delivered;
        }

        public long getFailedDelivery() {
            return failedDelivery;
        }

        public void setFailedDelivery(long failedDelivery) {
            this.failedDelivery = failedDelivery;
        }

        public long getCancelled() {
            return cancelled;
        }

        public void setCancelled(long cancelled) {
            this.cancelled = cancelled;
        }
    }
}