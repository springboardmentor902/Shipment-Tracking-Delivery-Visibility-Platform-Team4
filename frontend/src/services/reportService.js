const API_URL = "http://localhost:8080/api";


// ========================================
// HELPER — GET TOKEN (same as authService)
// ========================================

function getToken() {

    return localStorage.getItem(
        "token"
    );

}


// ========================================
// HELPER — TRIGGER BROWSER FILE DOWNLOAD
// ========================================

function triggerFileDownload(
    blob,
    fileName
) {

    const url =
        window.URL.createObjectURL(blob);

    const link =
        document.createElement("a");

    link.href = url;
    link.download = fileName;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    window.URL.revokeObjectURL(url);

}


// ========================================
// HELPER — DOWNLOAD A REPORT (shared logic)
// ========================================

async function downloadReport(
    endpoint,
    format,
    fileNamePrefix
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/reports/${endpoint}?format=${format}`,
        {
            method: "GET",

            headers: {
                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    if (!response.ok) {

        // Error responses are still JSON, not a file,
        // so we can safely read them as text here.
        const text = await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
            // response wasn't JSON, keep raw text
        }

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to download report (${response.status})`
        );

    }

    const blob = await response.blob();

    const extension =
        format === "pdf" ? "pdf" : "xlsx";

    triggerFileDownload(
        blob,
        `${fileNamePrefix}.${extension}`
    );

}


// ========================================
// 1. SHIPMENT REPORT
// ========================================

export async function downloadShipmentReport(format) {

    return downloadReport(
        "shipments",
        format,
        "shipment_report"
    );

}


// ========================================
// 2. DELIVERY REPORT
// ========================================

export async function downloadDeliveryReport(format) {

    return downloadReport(
        "delivery",
        format,
        "delivery_report"
    );

}


// ========================================
// 3. ROUTE PERFORMANCE REPORT
// ========================================

export async function downloadRoutePerformanceReport(format) {

    return downloadReport(
        "routes",
        format,
        "route_performance_report"
    );

}


// ========================================
// 4. DELAY ANALYSIS REPORT
// ========================================

export async function downloadDelayAnalysisReport(format) {

    return downloadReport(
        "delay-analysis",
        format,
        "delay_analysis_report"
    );

}