package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.report.*;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ReportService;
import com.shiptrack.shiptrack_pro.service.impl.ExcelReportGenerator;
import com.shiptrack.shiptrack_pro.service.impl.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;
    private final UserRepository userRepository;


    // =====================================================
    // 1. SHIPMENT REPORT
    // =====================================================

    @GetMapping("/shipments")
    public ResponseEntity<byte[]> getShipmentReport(
            @RequestParam String format,
            Authentication authentication) {

        User currentUser = resolveUser(authentication);

        List<ShipmentReportRow> rows =
                reportService.getShipmentReport(currentUser);

        return buildFileResponse(
                format,
                "shipment_report",
                () -> pdfReportGenerator.generateShipmentReportPdf(rows),
                () -> excelReportGenerator.generateShipmentReportExcel(rows)
        );
    }


    // =====================================================
    // 2. DELIVERY REPORT
    // =====================================================

    @GetMapping("/delivery")
    public ResponseEntity<byte[]> getDeliveryReport(
            @RequestParam String format,
            Authentication authentication) {

        User currentUser = resolveUser(authentication);

        List<DeliveryReportRow> rows =
                reportService.getDeliveryReport(currentUser);

        return buildFileResponse(
                format,
                "delivery_report",
                () -> pdfReportGenerator.generateDeliveryReportPdf(rows),
                () -> excelReportGenerator.generateDeliveryReportExcel(rows)
        );
    }


    // =====================================================
    // 3. ROUTE PERFORMANCE REPORT
    // =====================================================

    @GetMapping("/routes")
    public ResponseEntity<byte[]> getRoutePerformanceReport(
            @RequestParam String format,
            Authentication authentication) {

        User currentUser = resolveUser(authentication);

        List<RoutePerformanceReportRow> rows =
                reportService.getRoutePerformanceReport(currentUser);

        return buildFileResponse(
                format,
                "route_performance_report",
                () -> pdfReportGenerator.generateRoutePerformanceReportPdf(rows),
                () -> excelReportGenerator.generateRoutePerformanceReportExcel(rows)
        );
    }


    // =====================================================
    // 4. DELAY ANALYSIS REPORT
    // =====================================================

    @GetMapping("/delay-analysis")
    public ResponseEntity<byte[]> getDelayAnalysisReport(
            @RequestParam String format,
            Authentication authentication) {

        User currentUser = resolveUser(authentication);

        List<DelayAnalysisReportRow> rows =
                reportService.getDelayAnalysisReport(currentUser);

        return buildFileResponse(
                format,
                "delay_analysis_report",
                () -> pdfReportGenerator.generateDelayAnalysisReportPdf(rows),
                () -> excelReportGenerator.generateDelayAnalysisReportExcel(rows)
        );
    }


    // =====================================================
    // HELPER — resolve Authentication -> full User entity
    // =====================================================

    private User resolveUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found for email: " + email
                ));
    }


    // =====================================================
    // HELPER — build the file response (pdf or excel)
    // =====================================================

    private interface ByteSupplier {
        byte[] get();
    }

    private ResponseEntity<byte[]> buildFileResponse(
            String format,
            String fileNamePrefix,
            ByteSupplier pdfSupplier,
            ByteSupplier excelSupplier) {

        byte[] fileBytes;
        MediaType contentType;
        String extension;

        if ("pdf".equalsIgnoreCase(format)) {
            fileBytes = pdfSupplier.get();
            contentType = MediaType.APPLICATION_PDF;
            extension = "pdf";

        } else if ("excel".equalsIgnoreCase(format)) {
            fileBytes = excelSupplier.get();
            contentType = MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            extension = "xlsx";

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid format '" + format + "' — must be 'pdf' or 'excel'"
            );
        }

        String fileName = fileNamePrefix + "." + extension;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition
                        .attachment()
                        .filename(fileName)
                        .build()
        );

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}