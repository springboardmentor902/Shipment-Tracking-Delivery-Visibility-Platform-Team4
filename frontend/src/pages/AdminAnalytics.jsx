import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AnalyticsDashboard.css";

import {
    getCurrentUser,
    getAdminAnalytics,
} from "../services/authService";

function AdminAnalytics() {
    const navigate = useNavigate();

    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        loadAnalytics();
    }, []);

    const loadAnalytics = async () => {
        try {
            setLoading(true);
            setError("");

            const user = getCurrentUser();

            if (!user) {
                navigate("/login");
                return;
            }

            if (user.role !== "ADMINISTRATOR") {
                setError(
                    "You are not authorized to view Admin Analytics."
                );
                return;
            }

            const data = await getAdminAnalytics();

            console.log("Admin analytics:", data);

            setAnalytics(data);
        } catch (err) {
            console.error("Admin analytics error:", err);
            setError(
                err.message || "Failed to fetch admin analytics."
            );
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="analytics-page">
                Loading analytics...
            </div>
        );
    }

    if (error) {
        return (
            <div className="analytics-page">
                <button
                    className="back-button"
                    onClick={() => navigate("/dashboard")}
                >
                    ← Dashboard
                </button>

                <div className="analytics-error">
                    {error}
                </div>
            </div>
        );
    }

    if (!analytics) {
        return null;
    }

    return (
        <div className="analytics-page">

            {/* HEADER */}
            <div className="analytics-header">
                <div>
                    <button
                        className="back-button"
                        onClick={() => navigate("/dashboard")}
                    >
                        ← Dashboard
                    </button>

                    <h1>Admin Analytics</h1>
                    <p>
                        Platform-wide shipment and system insights
                    </p>
                </div>

                <button
                    className="refresh-button"
                    onClick={loadAnalytics}
                >
                    Refresh
                </button>
            </div>


            {/* SUMMARY CARDS */}
            <div className="analytics-cards">

                <AnalyticsCard
                    icon="👥"
                    title="Total Users"
                    value={analytics.totalUsers}
                    text="Registered platform users"
                />

                <AnalyticsCard
                    icon="📦"
                    title="Total Shipments"
                    value={analytics.totalShipments}
                    text="Platform-wide shipments"
                />

                <AnalyticsCard
                    icon="✅"
                    title="Delivered"
                    value={analytics.deliveredShipments}
                    text="Delivered shipments"
                />

                <AnalyticsCard
                    icon="⚠️"
                    title="Delayed"
                    value={analytics.delayedShipments}
                    text="Delayed shipments"
                />

            </div>


            {/* USER SUMMARY */}
            <AnalyticsPanel title="User Summary">

                <DataRow
                    label="Total Users"
                    value={analytics.totalUsers}
                />

                <DataRow
                    label="Active Users"
                    value={analytics.activeUsers}
                />

                <DataRow
                    label="Customers"
                    value={analytics.totalCustomers}
                />

                <DataRow
                    label="Business Clients"
                    value={analytics.totalBusinessClients}
                />

                <DataRow
                    label="Logistics Operators"
                    value={analytics.totalOperators}
                />

                <DataRow
                    label="Support Agents"
                    value={analytics.totalSupportAgents}
                />

                <DataRow
                    label="Administrators"
                    value={analytics.totalAdministrators}
                />

            </AnalyticsPanel>


            {/* USER ROLE BREAKDOWN */}
            <AnalyticsPanel title="User Role Breakdown">

                {analytics.userRoleBreakdown &&
                Object.keys(analytics.userRoleBreakdown).length > 0 ? (

                    Object.entries(
                        analytics.userRoleBreakdown
                    ).map(([role, count]) => (
                        <DataRow
                            key={role}
                            label={formatLabel(role)}
                            value={count}
                        />
                    ))

                ) : (
                    <p>No user role data available.</p>
                )}

            </AnalyticsPanel>


            {/* USER STATUS BREAKDOWN */}
            <AnalyticsPanel title="User Status Breakdown">

                {analytics.userStatusBreakdown &&
                Object.keys(analytics.userStatusBreakdown).length > 0 ? (

                    Object.entries(
                        analytics.userStatusBreakdown
                    ).map(([status, count]) => (
                        <DataRow
                            key={status}
                            label={formatLabel(status)}
                            value={count}
                        />
                    ))

                ) : (
                    <p>No user status data available.</p>
                )}

            </AnalyticsPanel>


            {/* PLATFORM SHIPMENT MONITORING */}
            <AnalyticsPanel title="Platform-wide Shipment Monitoring">

                <DataRow
                    label="Total Shipments"
                    value={analytics.totalShipments}
                />

                <DataRow
                    label="Active Shipments"
                    value={analytics.activeShipments}
                />

                <DataRow
                    label="Delivered Shipments"
                    value={analytics.deliveredShipments}
                />

                <DataRow
                    label="Failed Deliveries"
                    value={analytics.failedDeliveries}
                />

                <DataRow
                    label="Cancelled Shipments"
                    value={analytics.cancelledShipments}
                />

                <DataRow
                    label="Delayed Shipments"
                    value={analytics.delayedShipments}
                />

                <DataRow
                    label="Delivery Success Rate"
                    value={`${analytics.deliverySuccessRate}%`}
                />

            </AnalyticsPanel>


            {/* SHIPMENT STATUS BREAKDOWN */}
            <AnalyticsPanel title="Shipment Status Breakdown">

                {analytics.shipmentStatusBreakdown &&
                Object.keys(
                    analytics.shipmentStatusBreakdown
                ).length > 0 ? (

                    Object.entries(
                        analytics.shipmentStatusBreakdown
                    ).map(([status, count]) => (
                        <DataRow
                            key={status}
                            label={formatLabel(status)}
                            value={count}
                        />
                    ))

                ) : (
                    <p>No shipment status data available.</p>
                )}

            </AnalyticsPanel>


            {/* DELIVERY ANALYTICS */}
            <AnalyticsPanel title="Delivery Analytics">

                <DataRow
                    label="Delivery Success Rate"
                    value={`${analytics.deliverySuccessRate}%`}
                />

                <DataRow
                    label="Delivered Shipments"
                    value={analytics.deliveredShipments}
                />

                <DataRow
                    label="Failed Deliveries"
                    value={analytics.failedDeliveries}
                />

                <DataRow
                    label="Delayed Shipments"
                    value={analytics.delayedShipments}
                />

                <DataRow
                    label="Cancelled Shipments"
                    value={analytics.cancelledShipments}
                />

            </AnalyticsPanel>


            {/* ROUTE PERFORMANCE */}
            <AnalyticsPanel title="Route Performance">

                {Array.isArray(analytics.routePerformance) &&
                analytics.routePerformance.length > 0 ? (

                    <div className="analytics-table-wrapper">
                        <table className="analytics-table">

                            <thead>
                            <tr>
                                <th>Route</th>
                                <th>Shipments</th>
                                <th>Delivered</th>
                                <th>Delayed</th>
                            </tr>
                            </thead>

                            <tbody>
                            {analytics.routePerformance.map(
                                (route, index) => (
                                    <tr key={index}>

                                        <td>
                                            {route.route}
                                        </td>

                                        <td>
                                            {route.shipmentCount}
                                        </td>

                                        <td>
                                            {route.deliveredCount}
                                        </td>

                                        <td>
                                            {route.delayedCount}
                                        </td>

                                    </tr>
                                )
                            )}
                            </tbody>

                        </table>
                    </div>

                ) : (
                    <p>No route performance data available.</p>
                )}

            </AnalyticsPanel>


            {/* SYSTEM MONITORING */}
            <AnalyticsPanel title="System Monitoring">

                {analytics.systemMonitoring ? (
                    <>
                        <DataRow
                            label="Total Shipments"
                            value={
                                analytics.systemMonitoring
                                    .totalShipments
                            }
                        />

                        <DataRow
                            label="Active Shipments"
                            value={
                                analytics.systemMonitoring
                                    .activeShipments
                            }
                        />

                        <DataRow
                            label="Shipments Without Location"
                            value={
                                analytics.systemMonitoring
                                    .shipmentsWithoutLocation
                            }
                        />

                        <DataRow
                            label="Shipments Without ETA"
                            value={
                                analytics.systemMonitoring
                                    .shipmentsWithoutEta
                            }
                        />
                    </>
                ) : (
                    <p>No system monitoring data available.</p>
                )}

            </AnalyticsPanel>


            {/* REPORT MANAGEMENT */}
            <AnalyticsPanel title="Reports Management">

                {analytics.reportsManagement ? (
                    <>
                        <DataRow
                            label="Total Reports"
                            value={
                                analytics.reportsManagement
                                    .totalReports
                            }
                        />

                        <DataRow
                            label="Generated Reports"
                            value={
                                analytics.reportsManagement
                                    .generatedReports
                            }
                        />
                    </>
                ) : (
                    <p>No reports management data available.</p>
                )}

            </AnalyticsPanel>

        </div>
    );
}


/* =====================================================
   ANALYTICS CARD
   ===================================================== */

function AnalyticsCard({
                           icon,
                           title,
                           value,
                           text,
                       }) {
    return (
        <div className="analytics-card">

            <div className="analytics-icon">
                {icon}
            </div>

            <h3>{title}</h3>

            <strong>
                {value ?? 0}
            </strong>

            <p>{text}</p>

        </div>
    );
}


/* =====================================================
   ANALYTICS PANEL
   ===================================================== */

function AnalyticsPanel({
                            title,
                            children,
                        }) {
    return (
        <div className="analytics-panel">

            <h2>{title}</h2>

            {children}

        </div>
    );
}


/* =====================================================
   DATA ROW
   ===================================================== */

function DataRow({
                     label,
                     value,
                 }) {
    return (
        <div className="simple-row">

            <span>
                {label}
            </span>

            <strong>
                {value ?? 0}
            </strong>

        </div>
    );
}


/* =====================================================
   FORMAT LABEL
   ===================================================== */

function formatLabel(value) {
    return String(value)
        .replaceAll("_", " ")
        .replace(/\b\w/g, char =>
            char.toUpperCase()
        );
}


export default AdminAnalytics;