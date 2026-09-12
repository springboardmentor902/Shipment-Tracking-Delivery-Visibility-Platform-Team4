
import { useState } from "react";
import {
    getOptimizedRoutes,
    getBestRoute,
    getShortestRoute,
    getFastestRoute,
} from "../services/routeOptimizationService";
import "./RouteOptimization.css";

function RouteOptimization() {
    const [origin, setOrigin] = useState("");
    const [destination, setDestination] = useState("");
    const [routes, setRoutes] = useState([]);
    const [selectedRoute, setSelectedRoute] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const findRoutes = async () => {
        if (!origin.trim() || !destination.trim()) {
            setError("Please enter both origin and destination.");
            return;
        }

        try {
            setLoading(true);
            setError("");
            setSelectedRoute(null);

            const data = await getOptimizedRoutes(origin, destination);
            setRoutes(data || []);
        } catch (err) {
            setError(err.message || "Unable to calculate routes.");
        } finally {
            setLoading(false);
        }
    };

    const findBest = async () => {
        try {
            setLoading(true);
            setError("");

            const route = await getBestRoute(origin, destination);
            setSelectedRoute(route);
        } catch (err) {
            setError(err.message || "Unable to find best route.");
        } finally {
            setLoading(false);
        }
    };

    const findShortest = async () => {
        try {
            setLoading(true);
            setError("");

            const route = await getShortestRoute(origin, destination);
            setSelectedRoute(route);
        } catch (err) {
            setError(err.message || "Unable to find shortest route.");
        } finally {
            setLoading(false);
        }
    };

    const findFastest = async () => {
        try {
            setLoading(true);
            setError("");

            const route = await getFastestRoute(origin, destination);
            setSelectedRoute(route);
        } catch (err) {
            setError(err.message || "Unable to find fastest route.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="route-page">
            <div className="route-container">

                <h1 className="route-title">
                    🛣️ Route Management & Optimization
                </h1>

                <p className="route-subtitle">
                    Find optimized routes and compare distance, duration,
                    and traffic conditions.
                </p>

                <div className="route-search-card">

                    <input
                        className="route-input"
                        type="text"
                        placeholder="📍 Origin"
                        value={origin}
                        onChange={(e) => setOrigin(e.target.value)}
                    />

                    <input
                        className="route-input"
                        type="text"
                        placeholder="🏁 Destination"
                        value={destination}
                        onChange={(e) => setDestination(e.target.value)}
                    />

                    <button
                        className="find-button"
                        onClick={findRoutes}
                    >
                        🔍 Find Routes
                    </button>

                    <div className="route-actions">

                        <button
                            className="route-action-button"
                            onClick={findBest}
                        >
                            ⭐ Best Route
                        </button>

                        <button
                            className="route-action-button"
                            onClick={findShortest}
                        >
                            📏 Shortest Route
                        </button>

                        <button
                            className="route-action-button"
                            onClick={findFastest}
                        >
                            ⚡ Fastest Route
                        </button>

                    </div>
                </div>

                {loading && (
                    <p className="loading">
                        🔄 Calculating route...
                    </p>
                )}

                {error && (
                    <div className="error-message">
                        ⚠️ {error}
                    </div>
                )}

                {selectedRoute && (
                    <div className="selected-route">

                        <h2>⭐ Selected Route</h2>

                        <div className="route-stats">

                            <div className="route-stat">
                                <strong>Route Type</strong>
                                {selectedRoute.routeType}
                            </div>

                            <div className="route-stat">
                                <strong>Distance</strong>
                                {selectedRoute.distanceKm?.toFixed(2)} km
                            </div>

                            <div className="route-stat">
                                <strong>Duration</strong>
                                {selectedRoute.durationMinutes} min
                            </div>

                            <div className="route-stat">
                                <strong>Traffic Duration</strong>
                                {selectedRoute.trafficDurationMinutes} min
                            </div>

                        </div>
                    </div>
                )}

                {routes.length > 0 && (
                    <div>

                        <h2 className="available-title">
                            Available Routes
                        </h2>

                        {routes.map((route, index) => (
                            <div
                                className="route-card"
                                key={index}
                            >

                                <h3>
                                    {route.summary ||
                                        `${route.routeType} Route`}
                                </h3>

                                <span className="route-type">
                                    {route.routeType}
                                </span>

                                <p>
                                    <strong>Distance:</strong>{" "}
                                    {route.distanceKm?.toFixed(2)} km
                                </p>

                                <p>
                                    <strong>Duration:</strong>{" "}
                                    {route.durationMinutes} minutes
                                </p>

                                <p>
                                    <strong>Traffic-adjusted:</strong>{" "}
                                    {route.trafficDurationMinutes} minutes
                                </p>

                            </div>
                        ))}

                    </div>
                )}

            </div>
        </div>
    );
}

export default RouteOptimization;

