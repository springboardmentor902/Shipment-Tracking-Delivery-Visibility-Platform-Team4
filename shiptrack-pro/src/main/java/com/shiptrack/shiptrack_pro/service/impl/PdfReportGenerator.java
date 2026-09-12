package com.shiptrack.shiptrack_pro.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shiptrack.shiptrack_pro.dto.report.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfReportGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    // =====================================================
    // 1. SHIPMENT REPORT
    // =====================================================

    public byte[] generateShipmentReportPdf(List<ShipmentReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Status", "Origin",
                "Destination", "Created At", "Estimated Delivery"
        };

        return buildPdf("Shipment Report", headers, rows, row -> new String[]{
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

    public byte[] generateDeliveryReportPdf(List<DeliveryReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Actual Delivery Date",
                "Verification Status", "Delivered To"
        };

        return buildPdf("Delivery Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                formatDate(row.getActualDeliveryDate()),
                safe(row.getVerificationStatus()),
                safe(row.getDeliveredTo())
        });
    }


    // =====================================================
    // 3. ROUTE PERFORMANCE REPORT
    // =====================================================

    public byte[] generateRoutePerformanceReportPdf(List<RoutePerformanceReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Origin", "Destination",
                "Distance (km)", "Estimated (min)", "Actual (min)"
        };

        return buildPdf("Route Performance Report", headers, rows, row -> new String[]{
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

    public byte[] generateDelayAnalysisReportPdf(List<DelayAnalysisReportRow> rows) {

        String[] headers = {
                "Tracking Number", "Delay Risk Score",
                "Predicted Delivery Time", "Factors"
        };

        return buildPdf("Delay Analysis Report", headers, rows, row -> new String[]{
                safe(row.getTrackingNumber()),
                row.getDelayRiskScore() == null ? "-" : String.valueOf(row.getDelayRiskScore()),
                formatDate(row.getPredictedDeliveryTime()),
                safe(row.getFactors())
        });
    }


    // =====================================================
    // SHARED TABLE-BUILDING LOGIC
    // =====================================================

    // A small functional interface so buildPdf() can ask "give me
    // this row's values as text" without needing 4 separate copies
    // of the same table-drawing code.
    private interface RowFormatter<T> {
        String[] toRowValues(T row);
    }

    private <T> byte[] buildPdf(
            String title,
            String[] headers,
            List<T> rows,
            RowFormatter<T> formatter) {

        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setSpacingAfter(15);
            document.add(titleParagraph);

            // Table
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (T row : rows) {
                for (String value : formatter.toRowValues(row)) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, bodyFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            if (rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No data available", bodyFont));
                emptyCell.setColspan(headers.length);
                emptyCell.setPadding(8);
                table.addCell(emptyCell);
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return outputStream.toByteArray();
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