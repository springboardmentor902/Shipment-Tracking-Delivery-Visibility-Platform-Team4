package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.AdminAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.BusinessAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.CustomerAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;


    // =====================================================
    // CUSTOMER ANALYTICS
    // =====================================================

    @Override
    @Cacheable(value = "customerAnalytics", key = "#userId")
    public CustomerAnalyticsResponse getCustomerAnalytics(Long userId) {
        // your existing code


        List<Shipment> shipments =
                shipmentRepository.findByUserId(userId);

        CustomerAnalyticsResponse response =
                new CustomerAnalyticsResponse();

        response.setTotalShipments(shipments.size());

        response.setActiveShipments(
                shipments.stream()
                        .filter(this::isActive)
                        .count()
        );

        response.setDeliveredShipments(
                shipments.stream()
                        .filter(s ->
                                s.getStatus() == ShipmentStatus.DELIVERED)
                        .count()
        );

        response.setDelayedShipments(
                shipments.stream()
                        .filter(this::isDelayed)
                        .count()
        );

        response.setStatusBreakdown(
                createStatusBreakdown(shipments)
        );

        List<CustomerAnalyticsResponse.ShipmentHistoryItem>
                history = shipments.stream()
                .sorted(
                        Comparator.comparing(
                                Shipment::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .map(this::createCustomerHistoryItem)
                .collect(Collectors.toList());

        response.setShipmentHistory(history);

        CustomerAnalyticsResponse.TrackingInsights insights =
                new CustomerAnalyticsResponse.TrackingInsights();

        insights.setShipmentsWithLocation(
                shipments.stream()
                        .filter(s ->
                                s.getCurrentLocation() != null &&
                                        !s.getCurrentLocation().isBlank())
                        .count()
        );

        insights.setShipmentsWithoutLocation(
                shipments.stream()
                        .filter(s ->
                                s.getCurrentLocation() == null ||
                                        s.getCurrentLocation().isBlank())
                        .count()
        );

        insights.setShipmentsOutForDelivery(
                shipments.stream()
                        .filter(s ->
                                s.getStatus() ==
                                        ShipmentStatus.OUT_FOR_DELIVERY)
                        .count()
        );

        response.setTrackingInsights(insights);

        return response;
    }


    // =====================================================
    // BUSINESS ANALYTICS
    // =====================================================

    @Override
    @Cacheable(value = "businessAnalytics", key = "#userId")
    public BusinessAnalyticsResponse getBusinessAnalytics(Long userId) {


        List<Shipment> shipments =
                shipmentRepository.findByUserId(userId);

        BusinessAnalyticsResponse response =
                new BusinessAnalyticsResponse();

        long total = shipments.size();

        long delivered = shipments.stream()
                .filter(s ->
                        s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        long failed = shipments.stream()
                .filter(s ->
                        s.getStatus() ==
                                ShipmentStatus.FAILED_DELIVERY)
                .count();

        response.setTotalShipments(total);

        response.setDeliveredShipments(delivered);

        response.setFailedDeliveries(failed);

        response.setActiveShipments(
                shipments.stream()
                        .filter(this::isActive)
                        .count()
        );

        response.setDelayedShipments(
                shipments.stream()
                        .filter(this::isDelayed)
                        .count()
        );

        double successRate =
                total == 0
                        ? 0
                        : ((double) delivered / total) * 100;

        double failureRate =
                total == 0
                        ? 0
                        : ((double) failed / total) * 100;

        response.setDeliverySuccessRate(
                Math.round(successRate * 100.0) / 100.0
        );

        response.setDeliveryFailureRate(
                Math.round(failureRate * 100.0) / 100.0
        );

        response.setShipmentStatusBreakdown(
                createStatusBreakdown(shipments)
        );


        // Delay analysis

        BusinessAnalyticsResponse.DelayAnalysis delayAnalysis =
                new BusinessAnalyticsResponse.DelayAnalysis();

        long delayed = shipments.stream()
                .filter(this::isDelayed)
                .count();

        long withoutEta = shipments.stream()
                .filter(s -> s.getEstimatedDelivery() == null)
                .count();

        long onTime = shipments.stream()
                .filter(s ->
                        s.getEstimatedDelivery() != null &&
                                !isDelayed(s))
                .count();

        delayAnalysis.setDelayedShipments(delayed);
        delayAnalysis.setOnTimeShipments(onTime);
        delayAnalysis.setShipmentsWithoutEta(withoutEta);

        response.setDelayAnalysis(delayAnalysis);


        // Logistics overview

        BusinessAnalyticsResponse.LogisticsOverview logistics =
                new BusinessAnalyticsResponse.LogisticsOverview();

        logistics.setCreated(countStatus(
                shipments, ShipmentStatus.CREATED));

        logistics.setPickedUp(countStatus(
                shipments, ShipmentStatus.PICKED_UP));

        logistics.setInTransit(countStatus(
                shipments, ShipmentStatus.IN_TRANSIT));

        logistics.setOutForDelivery(countStatus(
                shipments, ShipmentStatus.OUT_FOR_DELIVERY));

        logistics.setDelivered(countStatus(
                shipments, ShipmentStatus.DELIVERED));

        logistics.setFailedDelivery(countStatus(
                shipments, ShipmentStatus.FAILED_DELIVERY));

        logistics.setCancelled(countStatus(
                shipments, ShipmentStatus.CANCELLED));

        response.setLogisticsOverview(logistics);


        // Route performance

        Map<String, List<Shipment>> routes =
                shipments.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getOrigin()
                                        + " → "
                                        + s.getDestination()
                        ));

        List<BusinessAnalyticsResponse.RouteAnalytics>
                routeAnalytics = new ArrayList<>();

        routes.forEach((route, routeShipments) -> {

            BusinessAnalyticsResponse.RouteAnalytics item =
                    new BusinessAnalyticsResponse.RouteAnalytics();

            item.setRoute(route);
            item.setShipmentCount(routeShipments.size());

            item.setDeliveredCount(
                    routeShipments.stream()
                            .filter(s ->
                                    s.getStatus() ==
                                            ShipmentStatus.DELIVERED)
                            .count()
            );

            routeAnalytics.add(item);
        });

        response.setRoutePerformance(routeAnalytics);


        // Customer activity
        //
        // For business-owned shipments, the receiver is used
        // as the customer activity identifier.

        Map<String, Long> customerCounts =
                shipments.stream()
                        .collect(Collectors.groupingBy(
                                Shipment::getReceiver,
                                Collectors.counting()
                        ));

        List<BusinessAnalyticsResponse.CustomerActivity>
                customerActivity = new ArrayList<>();

        customerCounts.forEach((customer, count) -> {

            BusinessAnalyticsResponse.CustomerActivity item =
                    new BusinessAnalyticsResponse.CustomerActivity();

            item.setCustomer(customer);
            item.setShipmentCount(count);

            customerActivity.add(item);
        });

        response.setCustomerActivity(customerActivity);

        return response;
    }


    // =====================================================
    // ADMIN ANALYTICS
    // =====================================================

    @Override
    @Cacheable(value = "adminAnalytics", key = "'platform'")
    public AdminAnalyticsResponse getAdminAnalytics() {


        List<User> users = userRepository.findAll();

        List<Shipment> shipments =
                shipmentRepository.findAll();

        AdminAnalyticsResponse response =
                new AdminAnalyticsResponse();


        // -------------------------------------------------
        // USER SUMMARY
        // -------------------------------------------------

        response.setTotalUsers(users.size());

        response.setActiveUsers(
                users.stream()
                        .filter(user ->
                                "ACTIVE".equalsIgnoreCase(
                                        user.getStatus()))
                        .count()
        );

        response.setTotalCustomers(
                countUsersByRole(users, "CUSTOMER")
        );

        response.setTotalBusinessClients(
                countUsersByRole(users, "BUSINESS_CLIENT")
        );

        response.setTotalOperators(
                countUsersByRole(users, "LOGISTICS_OPERATOR")
        );

        response.setTotalSupportAgents(
                countUsersByRole(users, "SUPPORT_AGENT")
        );

        response.setTotalAdministrators(
                countUsersByRole(users, "ADMINISTRATOR")
        );


        // -------------------------------------------------
        // SHIPMENT MONITORING
        // -------------------------------------------------

        response.setTotalShipments(shipments.size());

        response.setActiveShipments(
                shipments.stream()
                        .filter(this::isActive)
                        .count()
        );

        response.setDeliveredShipments(
                countStatus(
                        shipments,
                        ShipmentStatus.DELIVERED)
        );

        response.setFailedDeliveries(
                countStatus(
                        shipments,
                        ShipmentStatus.FAILED_DELIVERY)
        );

        response.setCancelledShipments(
                countStatus(
                        shipments,
                        ShipmentStatus.CANCELLED)
        );

        response.setDelayedShipments(
                shipments.stream()
                        .filter(this::isDelayed)
                        .count()
        );


        double deliveryRate =
                shipments.isEmpty()
                        ? 0
                        : ((double) response.getDeliveredShipments()
                        / shipments.size()) * 100;

        response.setDeliverySuccessRate(
                Math.round(deliveryRate * 100.0) / 100.0
        );


        response.setShipmentStatusBreakdown(
                createStatusBreakdown(shipments)
        );


        // -------------------------------------------------
        // USER ROLE BREAKDOWN
        // -------------------------------------------------

        Map<String, Long> roleBreakdown =
                users.stream()
                        .collect(Collectors.groupingBy(
                                user -> user.getRole() == null
                                        ? "UNKNOWN"
                                        : user.getRole(),
                                Collectors.counting()
                        ));

        response.setUserRoleBreakdown(roleBreakdown);


        // -------------------------------------------------
        // USER STATUS BREAKDOWN
        // -------------------------------------------------

        Map<String, Long> userStatusBreakdown =
                users.stream()
                        .collect(Collectors.groupingBy(
                                user -> user.getStatus() == null
                                        ? "UNKNOWN"
                                        : user.getStatus(),
                                Collectors.counting()
                        ));

        response.setUserStatusBreakdown(
                userStatusBreakdown
        );


        // -------------------------------------------------
        // ROUTE PERFORMANCE
        // -------------------------------------------------

        Map<String, List<Shipment>> routes =
                shipments.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getOrigin()
                                        + " → "
                                        + s.getDestination()
                        ));

        List<AdminAnalyticsResponse.RoutePerformance>
                routePerformance = new ArrayList<>();

        routes.forEach((route, routeShipments) -> {

            AdminAnalyticsResponse.RoutePerformance item =
                    new AdminAnalyticsResponse.RoutePerformance();

            item.setRoute(route);
            item.setShipmentCount(routeShipments.size());

            item.setDeliveredCount(
                    routeShipments.stream()
                            .filter(s ->
                                    s.getStatus() ==
                                            ShipmentStatus.DELIVERED)
                            .count()
            );

            item.setDelayedCount(
                    routeShipments.stream()
                            .filter(this::isDelayed)
                            .count()
            );

            routePerformance.add(item);
        });

        response.setRoutePerformance(routePerformance);


        // -------------------------------------------------
        // SYSTEM MONITORING
        // -------------------------------------------------

        AdminAnalyticsResponse.SystemMonitoring monitoring =
                new AdminAnalyticsResponse.SystemMonitoring();

        monitoring.setTotalShipments(shipments.size());

        monitoring.setActiveShipments(
                shipments.stream()
                        .filter(this::isActive)
                        .count()
        );

        monitoring.setShipmentsWithoutLocation(
                shipments.stream()
                        .filter(s ->
                                s.getCurrentLocation() == null ||
                                        s.getCurrentLocation().isBlank())
                        .count()
        );

        monitoring.setShipmentsWithoutEta(
                shipments.stream()
                        .filter(s ->
                                s.getEstimatedDelivery() == null)
                        .count()
        );

        response.setSystemMonitoring(monitoring);


        // -------------------------------------------------
        // REPORT MANAGEMENT
        // -------------------------------------------------

        AdminAnalyticsResponse.ReportsManagement reports =
                new AdminAnalyticsResponse.ReportsManagement();

        /*
         * Your current project does not have a Report entity.
         * Therefore these values are initialized safely.
         * They can later be connected to a ReportRepository.
         */
        reports.setTotalReports(0);
        reports.setGeneratedReports(0);

        response.setReportsManagement(reports);

        return response;
    }


    // =====================================================
    // HELPER METHODS
    // =====================================================

    private boolean isActive(Shipment shipment) {

        ShipmentStatus status = shipment.getStatus();

        return status != ShipmentStatus.DELIVERED
                && status != ShipmentStatus.CANCELLED
                && status != ShipmentStatus.FAILED_DELIVERY;
    }


    private boolean isDelayed(Shipment shipment) {

        if (shipment.getEstimatedDelivery() == null) {
            return false;
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED
                || shipment.getStatus() ==
                ShipmentStatus.FAILED_DELIVERY) {

            return false;
        }

        return shipment.getEstimatedDelivery()
                .isBefore(LocalDateTime.now());
    }


    private long countStatus(
            List<Shipment> shipments,
            ShipmentStatus status) {

        return shipments.stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }


    private Map<String, Long> createStatusBreakdown(
            List<Shipment> shipments) {

        Map<String, Long> breakdown =
                new LinkedHashMap<>();

        for (ShipmentStatus status :
                ShipmentStatus.values()) {

            breakdown.put(
                    status.name(),
                    shipments.stream()
                            .filter(s ->
                                    s.getStatus() == status)
                            .count()
            );
        }

        return breakdown;
    }


    private long countUsersByRole(
            List<User> users,
            String role) {

        return users.stream()
                .filter(user ->
                        role.equalsIgnoreCase(user.getRole()))
                .count();
    }


    private CustomerAnalyticsResponse.ShipmentHistoryItem
    createCustomerHistoryItem(Shipment shipment) {

        CustomerAnalyticsResponse.ShipmentHistoryItem item =
                new CustomerAnalyticsResponse.ShipmentHistoryItem();

        item.setId(shipment.getId());
        item.setTrackingNumber(
                shipment.getTrackingNumber());
        item.setOrigin(shipment.getOrigin());
        item.setDestination(shipment.getDestination());

        item.setStatus(
                shipment.getStatus() == null
                        ? null
                        : shipment.getStatus().name()
        );

        item.setCurrentLocation(
                shipment.getCurrentLocation());

        return item;
    }
}