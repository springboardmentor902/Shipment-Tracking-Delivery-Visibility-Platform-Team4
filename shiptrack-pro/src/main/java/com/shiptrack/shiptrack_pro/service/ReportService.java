package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.report.*;
import com.shiptrack.shiptrack_pro.entity.User;

import java.util.List;

public interface ReportService {

    List<ShipmentReportRow> getShipmentReport(User currentUser);

    List<DeliveryReportRow> getDeliveryReport(User currentUser);

    List<RoutePerformanceReportRow> getRoutePerformanceReport(User currentUser);

    List<DelayAnalysisReportRow> getDelayAnalysisReport(User currentUser);
}