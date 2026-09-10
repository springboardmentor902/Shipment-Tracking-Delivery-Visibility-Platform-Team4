import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./ReportsExport.css";

import { getCurrentUser } from "../services/authService";

import {
    downloadShipmentReport,
    downloadDeliveryReport,
    downloadRoutePerformanceReport,
    downloadDelayAnalysisReport,
} from "../services/reportService";

// ========================================
// REPORT TYPE OPTIONS
// ========================================

const REPORT_TYPES = [
    {
        value: "shipment",
        label: "Shipment Report",
        downloadFn: downloadShipmentReport,
    },
    {
        value: "delivery",
        label: "Delivery Report",
        downloadFn: downloadDeliveryReport,
    },
    {
        value: "route",
        label: "Route Performance Report",
        downloadFn: downloadRoutePerformanceReport,
    },
    {
        value: "delay",
        label: "Delay Analysis Report",
        downloadFn: downloadDelayAnalysisReport,
    },
];

function ReportsExport() {

    const navigate = useNavigate();

    // ========================================
    // USER
    // ========================================

    const [user, setUser] = useState(null);

    // ========================================
    // FORM STATE
    // ========================================

    const [reportType, setReportType] = useState(
        REPORT_TYPES[0].value
    );

    const [format, setFormat] = useState("pdf");

    // ========================================
    // DOWNLOAD STATE
    // ========================================

    const [downloading, setDownloading] = useState(false);
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    // ========================================
    // INITIAL LOAD
    // ========================================

    useEffect(() => {

        const currentUser = getCurrentUser();

        if (!currentUser) {
            navigate("/login");
            return;
        }

        setUser(currentUser);

    }, [navigate]);

    // ========================================
    // DOWNLOAD HANDLER
    // ========================================

    const handleDownload = async (event) => {

        event.preventDefault();

        try {

            setDownloading(true);
            setError("");
            setSuccessMessage("");

            const selectedReport =
                REPORT_TYPES.find(
                    report =>
                        report.value === reportType
                );

            await selectedReport.downloadFn(format);

            setSuccessMessage(
                "Report downloaded successfully."
            );

        } catch (error) {

            console.error(
                "Report download error:",
                error
            );

            setError(
                error.message ||
                "Failed to download report"
            );

        } finally {

            setDownloading(false);

        }

    };

    // ========================================
    // RENDER
    // ========================================

    return (

        <div className="reports-export">

            {/* =================================
                HEADER
            ================================= */}

            <div className="reports-export-header">

                <div>

                    <h1>
                        Reports & Export
                    </h1>

                    <p>
                        Download shipment and performance reports
                    </p>

                </div>

                <button
                    onClick={() =>
                        navigate("/dashboard")
                    }
                >
                    Back to Dashboard
                </button>

            </div>

            {/* =================================
                MESSAGES
            ================================= */}

            {error && (

                <div className="error-message">
                    {error}
                </div>

            )}

            {successMessage && (

                <div className="success-message">
                    {successMessage}
                </div>

            )}

            {/* =================================
                REPORT FORM
            ================================= */}

            <div className="report-form-card">

                <form onSubmit={handleDownload}>

                    {/* REPORT TYPE */}

                    <div>

                        <label>
                            Report Type
                        </label>

                        <select
                            value={reportType}
                            onChange={
                                (event) =>
                                    setReportType(
                                        event.target.value
                                    )
                            }
                        >

                            {REPORT_TYPES.map(
                                (report) => (

                                    <option
                                        key={report.value}
                                        value={report.value}
                                    >
                                        {report.label}
                                    </option>

                                )
                            )}

                        </select>

                    </div>

                    {/* FORMAT TOGGLE */}

                    <div>

                        <label>
                            Format
                        </label>

                        <div className="format-toggle">

                            <label>

                                <input
                                    type="radio"
                                    name="format"
                                    value="pdf"
                                    checked={format === "pdf"}
                                    onChange={
                                        (event) =>
                                            setFormat(
                                                event.target.value
                                            )
                                    }
                                />

                                {" "}
                                PDF

                            </label>

                            <label>

                                <input
                                    type="radio"
                                    name="format"
                                    value="excel"
                                    checked={format === "excel"}
                                    onChange={
                                        (event) =>
                                            setFormat(
                                                event.target.value
                                            )
                                    }
                                />

                                {" "}
                                Excel

                            </label>

                        </div>

                    </div>

                    {/* DOWNLOAD BUTTON */}

                    <div>

                        <button
                            type="submit"
                            disabled={downloading}
                        >

                            {
                                downloading
                                    ? "Downloading..."
                                    : "Download Report"
                            }

                        </button>

                    </div>

                </form>

            </div>

        </div>

    );
}

export default ReportsExport;