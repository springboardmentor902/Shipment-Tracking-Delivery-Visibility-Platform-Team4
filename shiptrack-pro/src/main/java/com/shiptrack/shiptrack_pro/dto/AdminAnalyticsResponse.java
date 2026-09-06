package com.shiptrack.shiptrack_pro.dto;

import java.util.List;
import java.util.Map;

public class AdminAnalyticsResponse {

    private long totalUsers;
    private long activeUsers;
    private long totalCustomers;
    private long totalBusinessClients;
    private long totalOperators;
    private long totalSupportAgents;
    private long totalAdministrators;

    private long totalShipments;
    private long activeShipments;
    private long deliveredShipments;
    private long failedDeliveries;
    private long cancelledShipments;
    private long delayedShipments;

    private double deliverySuccessRate;

    private Map<String, Long> userRoleBreakdown;
    private Map<String, Long> userStatusBreakdown;
    private Map<String, Long> shipmentStatusBreakdown;

    private List<RoutePerformance> routePerformance;

    private SystemMonitoring systemMonitoring;

    private ReportsManagement reportsManagement;


    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalBusinessClients() {
        return totalBusinessClients;
    }

    public void setTotalBusinessClients(long totalBusinessClients) {
        this.totalBusinessClients = totalBusinessClients;
    }

    public long getTotalOperators() {
        return totalOperators;
    }

    public void setTotalOperators(long totalOperators) {
        this.totalOperators = totalOperators;
    }

    public long getTotalSupportAgents() {
        return totalSupportAgents;
    }

    public void setTotalSupportAgents(long totalSupportAgents) {
        this.totalSupportAgents = totalSupportAgents;
    }

    public long getTotalAdministrators() {
        return totalAdministrators;
    }

    public void setTotalAdministrators(long totalAdministrators) {
        this.totalAdministrators = totalAdministrators;
    }

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

    public long getFailedDeliveries() {
        return failedDeliveries;
    }

    public void setFailedDeliveries(long failedDeliveries) {
        this.failedDeliveries = failedDeliveries;
    }

    public long getCancelledShipments() {
        return cancelledShipments;
    }

    public void setCancelledShipments(long cancelledShipments) {
        this.cancelledShipments = cancelledShipments;
    }

    public long getDelayedShipments() {
        return delayedShipments;
    }

    public void setDelayedShipments(long delayedShipments) {
        this.delayedShipments = delayedShipments;
    }

    public double getDeliverySuccessRate() {
        return deliverySuccessRate;
    }

    public void setDeliverySuccessRate(double deliverySuccessRate) {
        this.deliverySuccessRate = deliverySuccessRate;
    }

    public Map<String, Long> getUserRoleBreakdown() {
        return userRoleBreakdown;
    }

    public void setUserRoleBreakdown(
            Map<String, Long> userRoleBreakdown) {
        this.userRoleBreakdown = userRoleBreakdown;
    }

    public Map<String, Long> getUserStatusBreakdown() {
        return userStatusBreakdown;
    }

    public void setUserStatusBreakdown(
            Map<String, Long> userStatusBreakdown) {
        this.userStatusBreakdown = userStatusBreakdown;
    }

    public Map<String, Long> getShipmentStatusBreakdown() {
        return shipmentStatusBreakdown;
    }

    public void setShipmentStatusBreakdown(
            Map<String, Long> shipmentStatusBreakdown) {
        this.shipmentStatusBreakdown = shipmentStatusBreakdown;
    }

    public List<RoutePerformance> getRoutePerformance() {
        return routePerformance;
    }

    public void setRoutePerformance(
            List<RoutePerformance> routePerformance) {
        this.routePerformance = routePerformance;
    }

    public SystemMonitoring getSystemMonitoring() {
        return systemMonitoring;
    }

    public void setSystemMonitoring(
            SystemMonitoring systemMonitoring) {
        this.systemMonitoring = systemMonitoring;
    }

    public ReportsManagement getReportsManagement() {
        return reportsManagement;
    }

    public void setReportsManagement(
            ReportsManagement reportsManagement) {
        this.reportsManagement = reportsManagement;
    }


    public static class RoutePerformance {

        private String route;
        private long shipmentCount;
        private long deliveredCount;
        private long delayedCount;

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

        public long getDelayedCount() {
            return delayedCount;
        }

        public void setDelayedCount(long delayedCount) {
            this.delayedCount = delayedCount;
        }
    }


    public static class SystemMonitoring {

        private long totalShipments;
        private long activeShipments;
        private long shipmentsWithoutLocation;
        private long shipmentsWithoutEta;

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

        public long getShipmentsWithoutLocation() {
            return shipmentsWithoutLocation;
        }

        public void setShipmentsWithoutLocation(
                long shipmentsWithoutLocation) {
            this.shipmentsWithoutLocation = shipmentsWithoutLocation;
        }

        public long getShipmentsWithoutEta() {
            return shipmentsWithoutEta;
        }

        public void setShipmentsWithoutEta(
                long shipmentsWithoutEta) {
            this.shipmentsWithoutEta = shipmentsWithoutEta;
        }
    }


    public static class ReportsManagement {

        private long totalReports;
        private long generatedReports;

        public long getTotalReports() {
            return totalReports;
        }

        public void setTotalReports(long totalReports) {
            this.totalReports = totalReports;
        }

        public long getGeneratedReports() {
            return generatedReports;
        }

        public void setGeneratedReports(long generatedReports) {
            this.generatedReports = generatedReports;
        }
    }
}