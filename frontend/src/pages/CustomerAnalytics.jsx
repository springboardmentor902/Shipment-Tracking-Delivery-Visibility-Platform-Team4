import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AnalyticsDashboard.css";

import {
    getCurrentUser,
    getCustomerAnalytics,
} from "../services/authService";

function CustomerAnalytics() {

    const navigate = useNavigate();

    const [user, setUser] = useState(null);
    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        loadAnalytics();
    }, []);

    const loadAnalytics = async () => {
        try {
            const currentUser = getCurrentUser();

            if (!currentUser) {
                navigate("/login");
                return;
            }

            setUser(currentUser);

            if (currentUser.role !== "CUSTOMER") {
                setError("You are not authorized to view Customer Analytics.");
                return;
            }

            const data = await getCustomerAnalytics();
            setAnalytics(data);

        } catch (err) {
            console.error("Customer analytics error:", err);
            setError(err.message || "Failed to fetch customer analytics.");
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
                <button onClick={() => navigate("/dashboard")}>
                    ← Back to Dashboard
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

    const statusBreakdown = analytics.statusBreakdown || {};
    const trackingInsights = analytics.trackingInsights || {};
    const shipmentHistory = analytics.shipmentHistory || [];

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

                    <h1>Customer Analytics</h1>
                    <p>Your shipment and tracking insights</p>
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
                    text="Your total shipments"
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
                    text="Past estimated delivery"
                />

            </div>

            <div className="analytics-panel">
                <h2>Shipment Status Breakdown</h2>

                {Object.entries(statusBreakdown).map(
                    ([status, count]) => {

                        const total = analytics.totalShipments || 1;
                        const percentage =
                            Math.round((count / total) * 100);

                        return (
                            <div className="status-row" key={status}>
                                <div>
                                    <strong>
                                        {formatStatus(status)}
                                    </strong>
                                    <span>{count}</span>
                                </div>

                                <div className="progress">
                                    <div
                                        className="progress-bar"
                                        style={{
                                            width: `${percentage}%`
                                        }}
                                    />
                                </div>

                                <small>{percentage}%</small>
                            </div>
                        );
                    }
                )}
            </div>

            <div className="analytics-panel">
                <h2>Tracking Insights</h2>

                <div className="insight-grid">

                    <Insight
                        title="Currently In Transit"
                        value={trackingInsights.inTransit ?? 0}
                    />

                    <Insight
                        title="Out for Delivery"
                        value={trackingInsights.outForDelivery ?? 0}
                    />

                    <Insight
                        title="Pending Pickup"
                        value={trackingInsights.pendingPickup ?? 0}
                    />

                    <Insight
                        title="Delayed"
                        value={trackingInsights.delayed ?? 0}
                    />

                </div>
            </div>

            <div className="analytics-panel">
                <h2>Shipment History</h2>

                {shipmentHistory.length === 0 ? (
                    <p>No shipment history available.</p>
                ) : (
                    <div className="analytics-table-container">
                        <table className="analytics-table">
                            <thead>
                            <tr>
                                <th>Tracking Number</th>
                                <th>Origin</th>
                                <th>Destination</th>
                                <th>Status</th>
                                <th>Estimated Delivery</th>
                            </tr>
                            </thead>

                            <tbody>
                            {shipmentHistory.map((shipment, index) => (
                                <tr key={shipment.id || index}>
                                    <td>
                                        {shipment.trackingNumber || "N/A"}
                                    </td>
                                    <td>
                                        {shipment.origin || "N/A"}
                                    </td>
                                    <td>
                                        {shipment.destination || "N/A"}
                                    </td>
                                    <td>
                                        {formatStatus(shipment.status)}
                                    </td>
                                    <td>
                                        {shipment.estimatedDelivery
                                            ? new Date(
                                                shipment.estimatedDelivery
                                            ).toLocaleDateString()
                                            : "N/A"}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
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

function Insight({ title, value }) {
    return (
        <div className="insight-card">
            <span>{title}</span>
            <strong>{value}</strong>
        </div>
    );
}

function formatStatus(status) {
    if (!status) return "N/A";

    return status
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

export default CustomerAnalytics;