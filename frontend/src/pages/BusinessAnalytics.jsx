import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AnalyticsDashboard.css";

import {
    getCurrentUser,
    getBusinessAnalytics,
} from "../services/authService";

function BusinessAnalytics() {

    const navigate = useNavigate();

    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        loadAnalytics();
    }, []);

    const loadAnalytics = async () => {
        try {
            const user = getCurrentUser();

            if (!user) {
                navigate("/login");
                return;
            }

            if (user.role !== "BUSINESS_CLIENT") {
                setError(
                    "You are not authorized to view Business Analytics."
                );
                return;
            }

            const data = await getBusinessAnalytics();

            console.log("Business analytics:", data);

            setAnalytics(data);

        } catch (err) {
            console.error("Business analytics error:", err);
            setError(err.message || "Failed to fetch business analytics.");
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="analytics-page">Loading analytics...</div>;
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

    if (!analytics) return null;

    const statusBreakdown =
        analytics.logisticsOverview?.shipmentCountByStatus ||
        analytics.statusBreakdown ||
        {};

    const customerActivity =
        analytics.customerActivity || [];

    const routePerformance =
        analytics.routePerformance || [];

    return (
        <div className="analytics-page">

            <div className="analytics-header">
                <div>
                    <button
                        className="back-button"
                        onClick={() => navigate("/dashboard")}
                    >
                        ← Dashboard
                    </button>

                    <h1>Business Client Analytics</h1>
                    <p>Business shipment and delivery insights</p>
                </div>

                <button
                    className="refresh-button"
                    onClick={loadAnalytics}
                >
                    Refresh
                </button>
            </div>

            <div className="analytics-cards">

                <AnalyticsCard
                    icon="📦"
                    title="Total Shipments"
                    value={analytics.totalShipments ?? 0}
                    text="Your business shipments"
                />

                <AnalyticsCard
                    icon="🚚"
                    title="Active Shipments"
                    value={analytics.activeShipments ?? 0}
                    text="Currently active"
                />

                <AnalyticsCard
                    icon="✅"
                    title="Delivered"
                    value={analytics.deliveredShipments ?? 0}
                    text="Successfully delivered"
                />

                <AnalyticsCard
                    icon="⚠️"
                    title="Delayed"
                    value={analytics.delayedShipments ?? 0}
                    text="Delayed shipments"
                />

            </div>

            <AnalyticsSection
                title="Shipment Analytics"
                data={analytics.shipmentAnalytics}
            />

            <AnalyticsSection
                title="Delivery Performance"
                data={analytics.deliveryPerformance}
            />

            <AnalyticsSection
                title="Delay Analysis"
                data={analytics.delayAnalysis}
            />

            <div className="analytics-panel">
                <h2>Logistics Overview</h2>

                {Object.entries(statusBreakdown).map(
                    ([status, count]) => (
                        <div className="simple-row" key={status}>
                            <span>{formatStatus(status)}</span>
                            <strong>{count}</strong>
                        </div>
                    )
                )}
            </div>

            <div className="analytics-panel">
                <h2>Customer Activity</h2>

                {Array.isArray(customerActivity) &&
                customerActivity.length > 0 ? (
                    <div className="analytics-table-container">
                        <table className="analytics-table">
                            <thead>
                            <tr>
                                <th>Customer</th>
                                <th>Shipment Count</th>
                            </tr>
                            </thead>

                            <tbody>
                            {customerActivity.map((item, index) => (
                                <tr key={index}>
                                    <td>
                                        {item.customer ||
                                            item.receiver ||
                                            item.name ||
                                            "N/A"}
                                    </td>
                                    <td>
                                        {item.shipmentCount ??
                                            item.count ??
                                            0}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p>No customer activity available.</p>
                )}
            </div>

            <div className="analytics-panel">
                <h2>Route Performance</h2>

                {Array.isArray(routePerformance) &&
                routePerformance.length > 0 ? (
                    <div className="analytics-table-container">
                        <table className="analytics-table">
                            <thead>
                            <tr>
                                <th>Route</th>
                                <th>Shipment Count</th>
                            </tr>
                            </thead>

                            <tbody>
                            {routePerformance.map((route, index) => (
                                <tr key={index}>
                                    <td>
                                        {route.route ||
                                            route.name ||
                                            "N/A"}
                                    </td>
                                    <td>
                                        {route.shipmentCount ??
                                            route.count ??
                                            0}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p>No route performance data available.</p>
                )}
            </div>

        </div>
    );
}

function AnalyticsCard({ icon, title, value, text }) {
    return (
        <div className="analytics-card">
            <div className="analytics-icon">{icon}</div>
            <h3>{title}</h3>
            <strong>{value}</strong>
            <p>{text}</p>
        </div>
    );
}

function AnalyticsSection({ title, data }) {

    return (
        <div className="analytics-panel">
            <h2>{title}</h2>

            {data && typeof data === "object" ? (
                Object.entries(data).map(([key, value]) => (
                    <div className="simple-row" key={key}>
                        <span>{formatStatus(key)}</span>
                        <strong>
                            {typeof value === "number"
                                ? value
                                : String(value)}
                        </strong>
                    </div>
                ))
            ) : (
                <p>No data available.</p>
            )}
        </div>
    );
}

function formatStatus(status) {
    if (!status) return "N/A";

    return String(status)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

export default BusinessAnalytics;