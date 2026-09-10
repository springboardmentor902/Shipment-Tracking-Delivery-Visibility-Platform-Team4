package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.report.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelReportGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    // =====================================================
    // 1. SHIPMENT REPORT
    // =====================================================

    public byte[] generateShipmentReportExcel(List<ShipmentReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Status", "Origin",
                "Destination", "Created At", "Estimated Delivery"
        };

        return buildExcel("Shipment Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                safe(row.getStatus()),
                safe(row.getOrigin()),
                safe(row.getDestination()),
                formatDate(row.getCreatedAt()),
                formatDate(row.getEstimatedDelivery())
        });
    }


    // =====================================================
    // 2. DELIVERY REPORT
    // =====================================================

    public byte[] generateDeliveryReportExcel(List<DeliveryReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Actual Delivery Date",
                "Verification Status", "Delivered To"
        };

        return buildExcel("Delivery Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                formatDate(row.getActualDeliveryDate()),
                safe(row.getVerificationStatus()),
                safe(row.getDeliveredTo())
        });
    }


    // =====================================================
    // 3. ROUTE PERFORMANCE REPORT
    // =====================================================

    public byte[] generateRoutePerformanceReportExcel(List<RoutePerformanceReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Origin", "Destination",
                "Distance (km)", "Estimated (min)", "Actual (min)"
        };

        return buildExcel("Route Performance Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                safe(row.getOrigin()),
                safe(row.getDestination()),
                row.getDistanceKm() == null ? "-" : String.valueOf(row.getDistanceKm()),
                row.getEstimatedDurationMinutes() == null ? "-" : String.valueOf(row.getEstimatedDurationMinutes()),
                row.getActualDurationMinutes() == null ? "In progress" : String.valueOf(row.getActualDurationMinutes())
        });
    }


    // =====================================================
    // 4. DELAY ANALYSIS REPORT
    // =====================================================

    public byte[] generateDelayAnalysisReportExcel(List<DelayAnalysisReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Delay Risk Score",
                "Predicted Delivery Time", "Factors"
        };

        return buildExcel("Delay Analysis Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                row.getDelayRiskScore() == null ? "-" : String.valueOf(row.getDelayRiskScore()),
                formatDate(row.getPredictedDeliveryTime()),
                safe(row.getFactors())
        });
    }


    // =====================================================
    // SHARED SHEET-BUILDING LOGIC
    // =====================================================

    private interface RowFormatter<T> {
        String[] toRowValues(T row);
    }

    private <T> byte[] buildExcel(
            String sheetTitle,
            String[] headers,
            List<T> rows,
            RowFormatter<T> formatter) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetTitle);

            // Header style — bold text
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIndex = 1;
            for (T row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                String[] values = formatter.toRowValues(row);
                for (int col = 0; col < values.length; col++) {
                    excelRow.createCell(col).setCellValue(values[col]);
                }
            }

            if (rows.isEmpty()) {
                Row emptyRow = sheet.createRow(1);
                emptyRow.createCell(0).setCellValue("No data available");
            }

            // Auto-size columns so content isn't cut off
            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }


    // =====================================================
    // SMALL FORMATTING HELPERS
    // =====================================================

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_FORMAT);
    }
}