import { useEffect, useState } from "react";
import "./AnalyticsDashboard.css";

import {
    getCustomerAnalytics,
    getBusinessAnalytics,
    getAdminAnalytics
} from "../services/authService";


function AnalyticsDashboard({ user }) {

    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");


    useEffect(() => {

        if (!user || !user.role) {
            return;
        }

        loadAnalytics();

    }, [user]);


    async function loadAnalytics() {

        setLoading(true);
        setError("");

        try {

            let data;

            if (user.role === "CUSTOMER") {

                data = await getCustomerAnalytics();

            } else if (user.role === "BUSINESS_CLIENT") {

                data = await getBusinessAnalytics();

            } else if (user.role === "ADMINISTRATOR") {

                data = await getAdminAnalytics();

            } else {

                setAnalytics(null);
                setLoading(false);
                return;

            }

            console.log("Analytics response:", data);

            setAnalytics(data);

        } catch (err) {

            console.error("Analytics error:", err);

            setAnalytics(null);

            setError(
                err.message || "Failed to load analytics."
            );

        } finally {

            setLoading(false);

        }
    }


    if (!user) {
        return null;
    }


    return (
        <section className="analytics-section">

            {/* HEADER */}
            <div className="analytics-header">

                <div>

                    <h2>
                        Analytics Dashboard
                    </h2>

                    <p>

                        {user?.role === "CUSTOMER" &&
                            "Your shipment and tracking insights"}

                        {user?.role === "BUSINESS_CLIENT" &&
                            "Business shipment and delivery performance"}

                        {user?.role === "ADMINISTRATOR" &&
                            "Platform-wide system and shipment analytics"}

                    </p>

                </div>


                <button
                    className="analytics-refresh-button"
                    onClick={loadAnalytics}
                    disabled={loading}
                >
                    {loading ? "Loading..." : "Refresh"}
                </button>

            </div>


            {/* LOADING */}
            {loading && (

                <div className="analytics-loading">

                    <div className="analytics-spinner"></div>

                    <p>
                        Loading analytics...
                    </p>

                </div>

            )}


            {/* ERROR */}
            {!loading && error && (

                <div className="analytics-error">

                    <div>

                        <strong>
                            Analytics unavailable
                        </strong>

                        <p>
                            {error}
                        </p>

                    </div>


                    <button
                        onClick={loadAnalytics}
                    >
                        Retry
                    </button>

                </div>

            )}


            {/* NO DATA */}
            {!loading &&
                !error &&
                !analytics && (

                    <div className="analytics-empty">

                        <h3>
                            No analytics data available
                        </h3>

                        <p>
                            Analytics information could not be loaded.
                        </p>

                    </div>

                )}


            {/* CUSTOMER ANALYTICS */}
            {!loading &&
                !error &&
                analytics &&
                user?.role === "CUSTOMER" && (

                    <CustomerAnalytics
                        analytics={analytics}
                    />

                )}


            {/* BUSINESS ANALYTICS */}
            {!loading &&
                !error &&
                analytics &&
                user?.role === "BUSINESS_CLIENT" && (

                    <BusinessAnalytics
                        analytics={analytics}
                    />

                )}


            {/* ADMIN ANALYTICS */}
            {!loading &&
                !error &&
                analytics &&
                user?.role === "ADMINISTRATOR" && (

                    <AdminAnalytics
                        analytics={analytics}
                    />

                )}

        </section>
    );
}


/* ============================================================
   REUSABLE ANALYTICS CARD
============================================================ */

function AnalyticsCard({
                           title,
                           value,
                           description,
                           icon
                       }) {

    return (

        <div className="analytics-card">

            <div className="analytics-card-icon">
                {icon}
            </div>

            <div className="analytics-card-content">

                <h4>
                    {title}
                </h4>

                <div className="analytics-card-value">
                    {value ?? 0}
                </div>

                {description && (

                    <p>
                        {description}
                    </p>

                )}

            </div>

        </div>

    );
}


/* ============================================================
   ANALYTICS PANEL
============================================================ */

function AnalyticsPanel({
                            title,
                            children
                        }) {

    return (

        <div className="analytics-panel">

            <div className="analytics-panel-header">

                <h3>
                    {title}
                </h3>

            </div>

            <div className="analytics-panel-body">

                {children}

            </div>

        </div>

    );
}


/* ============================================================
   STATUS ROW
============================================================ */

function StatusRow({
                       status,
                       count,
                       total
                   }) {

    const safeTotal = total || 0;

    const percentage =
        safeTotal > 0
            ? Math.round((count / safeTotal) * 100)
            : 0;


    return (

        <div className="analytics-status-row">

            <div className="analytics-status-info">

                <span>
                    {formatStatus(status)}
                </span>

                <strong>
                    {count ?? 0}
                </strong>

            </div>


            <div className="analytics-progress">

                <div
                    className="analytics-progress-bar"
                    style={{
                        width: `${percentage}%`
                    }}
                ></div>

            </div>


            <span className="analytics-percentage">

                {percentage}%

            </span>

        </div>

    );
}


/* ============================================================
   INSIGHT ROW
============================================================ */

function InsightRow({
                        label,
                        value
                    }) {

    return (

        <div className="analytics-insight-row">

            <span>
                {label}
            </span>

            <strong>
                {value ?? 0}
            </strong>

        </div>

    );

}


/* ============================================================
   CUSTOMER ANALYTICS
============================================================ */

function CustomerAnalytics({
                               analytics
                           }) {

    if (!analytics) {
        return null;
    }


    const statusBreakdown =
        analytics.statusBreakdown || {};

    const trackingInsights =
        analytics.trackingInsights || {};


    const history =
        analytics.shipmentHistory || [];


    const totalShipments =
        analytics.totalShipments ??
        analytics.activeShipmentCount ??
        0;


    const activeShipments =
        analytics.activeShipmentCount ??
        analytics.activeShipments ??
        0;


    const delivered =
        analytics.deliveredShipments ??
        analytics.delivered ??
        0;


    const delayed =
        analytics.delayedShipments ??
        analytics.delayed ??
        0;


    return (

        <div className="analytics-content">


            {/* SUMMARY CARDS */}

            <div className="analytics-cards-grid">

                <AnalyticsCard
                    title="Total Shipments"
                    value={totalShipments}
                    description="Your total shipments"
                    icon="📦"
                />

                <AnalyticsCard
                    title="Active Shipments"
                    value={activeShipments}
                    description="Currently active"
                    icon="🚚"
                />

                <AnalyticsCard
                    title="Delivered"
                    value={delivered}
                    description="Successfully delivered"
                    icon="✅"
                />

                <AnalyticsCard
                    title="Delayed"
                    value={delayed}
                    description="Past estimated delivery"
                    icon="⚠️"
                />

            </div>


            {/* STATUS BREAKDOWN */}

            <AnalyticsPanel
                title="Shipment Status Breakdown"
            >

                {Object.keys(statusBreakdown).length > 0 ? (

                    <div className="analytics-status-list">

                        {Object.entries(statusBreakdown).map(
                            ([status, count]) => (

                                <StatusRow
                                    key={status}
                                    status={status}
                                    count={count}
                                    total={totalShipments}
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No status information available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* TRACKING INSIGHTS */}

            <AnalyticsPanel
                title="Tracking Insights"
            >

                <div className="analytics-insights-grid">

                    <InsightRow
                        label="Currently In Transit"
                        value={
                            trackingInsights.inTransit ??
                            trackingInsights.inTransitCount ??
                            0
                        }
                    />

                    <InsightRow
                        label="Out for Delivery"
                        value={
                            trackingInsights.outForDelivery ??
                            trackingInsights.outForDeliveryCount ??
                            0
                        }
                    />

                    <InsightRow
                        label="Pending Pickup"
                        value={
                            trackingInsights.pending ??
                            trackingInsights.pendingCount ??
                            0
                        }
                    />

                    <InsightRow
                        label="Delayed"
                        value={
                            trackingInsights.delayed ??
                            trackingInsights.delayedCount ??
                            delayed
                        }
                    />

                </div>

            </AnalyticsPanel>


            {/* SHIPMENT HISTORY */}

            <AnalyticsPanel
                title="Shipment History"
            >

                {history.length > 0 ? (

                    <div className="analytics-table-container">

                        <table className="analytics-table">

                            <thead>

                            <tr>

                                <th>
                                    Tracking Number
                                </th>

                                <th>
                                    Origin
                                </th>

                                <th>
                                    Destination
                                </th>

                                <th>
                                    Status
                                </th>

                                <th>
                                    Estimated Delivery
                                </th>

                            </tr>

                            </thead>


                            <tbody>

                            {history.map(
                                (shipment, index) => (

                                    <tr
                                        key={
                                            shipment.id ||
                                            shipment.trackingNumber ||
                                            index
                                        }
                                    >

                                        <td>
                                            {shipment.trackingNumber ||
                                                "N/A"}
                                        </td>

                                        <td>
                                            {shipment.origin ||
                                                "N/A"}
                                        </td>

                                        <td>
                                            {shipment.destination ||
                                                "N/A"}
                                        </td>

                                        <td>

                                                <span className="analytics-status-badge">

                                                    {formatStatus(
                                                        shipment.status
                                                    )}

                                                </span>

                                        </td>

                                        <td>
                                            {formatDate(
                                                shipment.estimatedDelivery
                                            )}
                                        </td>

                                    </tr>

                                )
                            )}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No shipment history available.
                    </div>

                )}

            </AnalyticsPanel>

        </div>

    );
}


/* ============================================================
   BUSINESS ANALYTICS
============================================================ */

function BusinessAnalytics({
                               analytics
                           }) {

    if (!analytics) {
        return null;
    }


    const statusBreakdown =
        analytics.statusBreakdown || {};

    const deliveryPerformance =
        analytics.deliveryPerformance || {};

    const delayAnalysis =
        analytics.delayAnalysis || {};

    const logisticsOverview =
        analytics.logisticsOverview || {};

    const customerActivity =
        analytics.customerActivity || {};

    const routePerformance =
        analytics.routePerformance || {};


    const totalShipments =
        analytics.totalShipments ??
        analytics.shipmentCount ??
        0;


    const delivered =
        analytics.deliveredShipments ??
        deliveryPerformance.delivered ??
        0;


    const delayed =
        analytics.delayedShipments ??
        delayAnalysis.delayed ??
        0;


    const failed =
        analytics.failedShipments ??
        deliveryPerformance.failed ??
        0;


    return (

        <div className="analytics-content">


            {/* SUMMARY */}

            <div className="analytics-cards-grid">

                <AnalyticsCard
                    title="Total Shipments"
                    value={totalShipments}
                    description="Business shipments"
                    icon="📦"
                />

                <AnalyticsCard
                    title="Delivered"
                    value={delivered}
                    description="Successfully delivered"
                    icon="✅"
                />

                <AnalyticsCard
                    title="Delayed"
                    value={delayed}
                    description="Delayed shipments"
                    icon="⚠️"
                />

                <AnalyticsCard
                    title="Failed"
                    value={failed}
                    description="Failed deliveries"
                    icon="❌"
                />

            </div>


            {/* STATUS BREAKDOWN */}

            <AnalyticsPanel
                title="Shipment Analytics"
            >

                {Object.keys(statusBreakdown).length > 0 ? (

                    <div className="analytics-status-list">

                        {Object.entries(statusBreakdown).map(
                            ([status, count]) => (

                                <StatusRow
                                    key={status}
                                    status={status}
                                    count={count}
                                    total={totalShipments}
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No shipment status data available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* DELIVERY PERFORMANCE */}

            <AnalyticsPanel
                title="Delivery Performance"
            >

                <div className="analytics-insights-grid">

                    <InsightRow
                        label="Total Deliveries"
                        value={
                            deliveryPerformance.totalDeliveries ??
                            totalShipments
                        }
                    />

                    <InsightRow
                        label="Delivered"
                        value={
                            deliveryPerformance.delivered ??
                            delivered
                        }
                    />

                    <InsightRow
                        label="Failed"
                        value={
                            deliveryPerformance.failed ??
                            failed
                        }
                    />

                    <InsightRow
                        label="Success Rate"
                        value={
                            formatPercentage(
                                deliveryPerformance.successRate
                            )
                        }
                    />

                </div>

            </AnalyticsPanel>


            {/* DELAY ANALYSIS */}

            <AnalyticsPanel
                title="Delay Analysis"
            >

                <div className="analytics-insights-grid">

                    <InsightRow
                        label="Delayed Shipments"
                        value={
                            delayAnalysis.delayed ??
                            delayed
                        }
                    />

                    <InsightRow
                        label="On-Time Shipments"
                        value={
                            delayAnalysis.onTime ??
                            delayAnalysis.onTimeShipments ??
                            Math.max(
                                totalShipments - delayed,
                                0
                            )
                        }
                    />

                    <InsightRow
                        label="Delay Rate"
                        value={
                            formatPercentage(
                                delayAnalysis.delayRate
                            )
                        }
                    />

                    <InsightRow
                        label="Average Delay"
                        value={
                            delayAnalysis.averageDelay ??
                            delayAnalysis.averageDelayDays ??
                            "0"
                        }
                    />

                </div>

            </AnalyticsPanel>


            {/* LOGISTICS OVERVIEW */}

            <AnalyticsPanel
                title="Logistics Overview"
            >

                {Object.keys(logisticsOverview).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            logisticsOverview
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No logistics information available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* CUSTOMER ACTIVITY */}

            <AnalyticsPanel
                title="Customer Activity"
            >

                {Object.keys(customerActivity).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            customerActivity
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No customer activity available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* ROUTE PERFORMANCE */}

            <AnalyticsPanel
                title="Route Performance"
            >

                {Object.keys(routePerformance).length > 0 ? (

                    <div className="analytics-table-container">

                        <table className="analytics-table">

                            <thead>

                            <tr>

                                <th>
                                    Route
                                </th>

                                <th>
                                    Shipments
                                </th>

                                <th>
                                    Delivered
                                </th>

                                <th>
                                    Delayed
                                </th>

                            </tr>

                            </thead>


                            <tbody>

                            {Object.entries(
                                routePerformance
                            ).map(
                                ([route, data], index) => {

                                    const routeData =
                                        typeof data === "object"
                                            ? data
                                            : {
                                                shipments: data
                                            };

                                    return (

                                        <tr key={index}>

                                            <td>
                                                {route}
                                            </td>

                                            <td>
                                                {routeData.shipments ??
                                                    routeData.total ??
                                                    0}
                                            </td>

                                            <td>
                                                {routeData.delivered ??
                                                    0}
                                            </td>

                                            <td>
                                                {routeData.delayed ??
                                                    0}
                                            </td>

                                        </tr>

                                    );

                                }
                            )}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No route performance data available.
                    </div>

                )}

            </AnalyticsPanel>

        </div>

    );
}


/* ============================================================
   ADMIN ANALYTICS
============================================================ */

function AdminAnalytics({
                            analytics
                        }) {

    if (!analytics) {
        return null;
    }


    const userSummary =
        analytics.userSummary || {};

    const platformShipmentMonitoring =
        analytics.platformShipmentMonitoring || {};

    const deliveryAnalytics =
        analytics.deliveryAnalytics || {};

    const routePerformance =
        analytics.routePerformance || {};

    const systemMonitoring =
        analytics.systemMonitoring || {};

    const reportsManagement =
        analytics.reportsManagement || {};


    const totalUsers =
        userSummary.totalUsers ??
        analytics.totalUsers ??
        0;


    const totalShipments =
        platformShipmentMonitoring.totalShipments ??
        analytics.totalShipments ??
        0;


    const delivered =
        deliveryAnalytics.delivered ??
        deliveryAnalytics.deliveredShipments ??
        0;


    const delayed =
        deliveryAnalytics.delayed ??
        deliveryAnalytics.delayedShipments ??
        0;


    return (

        <div className="analytics-content">


            {/* ADMIN SUMMARY */}

            <div className="analytics-cards-grid">

                <AnalyticsCard
                    title="Total Users"
                    value={totalUsers}
                    description="Platform users"
                    icon="👥"
                />

                <AnalyticsCard
                    title="Total Shipments"
                    value={totalShipments}
                    description="Platform shipments"
                    icon="📦"
                />

                <AnalyticsCard
                    title="Delivered"
                    value={delivered}
                    description="Successfully delivered"
                    icon="✅"
                />

                <AnalyticsCard
                    title="Delayed"
                    value={delayed}
                    description="Delayed shipments"
                    icon="⚠️"
                />

            </div>


            {/* USER SUMMARY */}

            <AnalyticsPanel
                title="User Summary"
            >

                {Object.keys(userSummary).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            userSummary
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No user summary available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* PLATFORM SHIPMENT MONITORING */}

            <AnalyticsPanel
                title="Platform Shipment Monitoring"
            >

                {Object.keys(
                    platformShipmentMonitoring
                ).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            platformShipmentMonitoring
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No platform shipment data available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* DELIVERY ANALYTICS */}

            <AnalyticsPanel
                title="Delivery Analytics"
            >

                {Object.keys(
                    deliveryAnalytics
                ).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            deliveryAnalytics
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No delivery analytics available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* ROUTE PERFORMANCE */}

            <AnalyticsPanel
                title="Route Performance"
            >

                {Object.keys(routePerformance).length > 0 ? (

                    <div className="analytics-table-container">

                        <table className="analytics-table">

                            <thead>

                            <tr>

                                <th>
                                    Route
                                </th>

                                <th>
                                    Shipments
                                </th>

                                <th>
                                    Delivered
                                </th>

                                <th>
                                    Delayed
                                </th>

                            </tr>

                            </thead>


                            <tbody>

                            {Object.entries(
                                routePerformance
                            ).map(
                                ([route, data], index) => {

                                    const routeData =
                                        typeof data === "object"
                                            ? data
                                            : {
                                                shipments: data
                                            };

                                    return (

                                        <tr key={index}>

                                            <td>
                                                {route}
                                            </td>

                                            <td>
                                                {routeData.shipments ??
                                                    routeData.total ??
                                                    0}
                                            </td>

                                            <td>
                                                {routeData.delivered ??
                                                    0}
                                            </td>

                                            <td>
                                                {routeData.delayed ??
                                                    0}
                                            </td>

                                        </tr>

                                    );

                                }
                            )}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No route performance data available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* SYSTEM MONITORING */}

            <AnalyticsPanel
                title="System Monitoring"
            >

                {Object.keys(
                    systemMonitoring
                ).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            systemMonitoring
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No system monitoring data available.
                    </div>

                )}

            </AnalyticsPanel>


            {/* REPORTS MANAGEMENT */}

            <AnalyticsPanel
                title="Reports Management"
            >

                {Object.keys(
                    reportsManagement
                ).length > 0 ? (

                    <div className="analytics-insights-grid">

                        {Object.entries(
                            reportsManagement
                        ).map(
                            ([key, value]) => (

                                <InsightRow
                                    key={key}
                                    label={formatLabel(key)}
                                    value={
                                        typeof value === "object"
                                            ? JSON.stringify(value)
                                            : value
                                    }
                                />

                            )
                        )}

                    </div>

                ) : (

                    <div className="analytics-no-data">
                        No report information available.
                    </div>

                )}

            </AnalyticsPanel>

        </div>

    );
}


/* ============================================================
   HELPER FUNCTIONS
============================================================ */

function formatStatus(status) {

    if (!status) {
        return "Unknown";
    }

    return status
        .toString()
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, letter =>
            letter.toUpperCase()
        );

}


function formatLabel(label) {

    if (!label) {
        return "";
    }

    return label
        .toString()
        .replaceAll("_", " ")
        .replace(/([A-Z])/g, " $1")
        .replace(/\s+/g, " ")
        .trim()
        .replace(/\b\w/g, letter =>
            letter.toUpperCase()
        );

}


function formatPercentage(value) {

    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "0%";
    }

    const numericValue =
        Number(value);

    if (Number.isNaN(numericValue)) {
        return value;
    }

    return `${numericValue.toFixed(1)}%`;

}


function formatDate(value) {

    if (!value) {
        return "N/A";
    }

    try {

        return new Date(value).toLocaleString();

    } catch {

        return value;

    }

}


export default AnalyticsDashboard;