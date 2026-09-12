
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Shipments.css";

import { getETAPrediction } from "../services/etaService";

import {
    getCurrentUser,
    getShipments,
} from "../services/authService";

function Shipments() {
    const navigate = useNavigate();

    const [user, setUser] = useState(null);
    const [shipments, setShipments] = useState([]);
    const [etaPredictions, setEtaPredictions] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [searchTerm, setSearchTerm] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");

    useEffect(() => {
        const currentUser = getCurrentUser();

        if (!currentUser) {
            navigate("/login");
            return;
        }

        setUser(currentUser);
        loadShipments();
    }, [navigate]);

    const loadShipments = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getShipments();

            console.log("Shipments received:", data);

            const shipmentList = Array.isArray(data)
                ? data
                : [];

            setShipments(shipmentList);

            const etaResults = {};

            await Promise.all(
                shipmentList.map(async (shipment) => {
                    try {
                        const eta =
                            await getETAPrediction(
                                shipment.id
                            );

                        etaResults[shipment.id] = eta;
                    } catch (etaError) {
                        console.error(
    "ETA fetch failed for shipment " +
        shipment.id +
        ":",
    etaError
);
                    }
                })
            );

            setEtaPredictions(etaResults);
        } catch (shipmentError) {
            console.error(
                "Shipment fetch error:",
                shipmentError
            );

            setError(
                shipmentError?.message ||
                "Failed to load shipments"
            );
        } finally {
            setLoading(false);
        }
    };

    // =====================================================
    // COUNTS
    // =====================================================

    const totalCount = shipments.length;

    const deliveredCount = shipments.filter(
        (shipment) =>
            shipment.status === "DELIVERED"
    ).length;

    const transitCount = shipments.filter(
        (shipment) =>
            shipment.status === "IN_TRANSIT" ||
            shipment.status === "OUT_FOR_DELIVERY"
    ).length;

    const createdCount = shipments.filter(
        (shipment) =>
            shipment.status === "CREATED" ||
            shipment.status === "PICKED_UP"
    ).length;

    // =====================================================
    // FILTERED SHIPMENTS
    // =====================================================

    const filteredShipments = useMemo(() => {
        return shipments.filter((shipment) => {
            const trackingNumber =
                shipment.trackingNumber ||
                "";

            const origin =
                shipment.origin ||
                "";

            const destination =
                shipment.destination ||
                "";

            const sender =
                shipment.sender ||
                "";

            const receiver =
                shipment.receiver ||
                "";

            const search =
                searchTerm
                    .trim()
                    .toLowerCase();

            const matchesSearch =
                !search ||
                trackingNumber
                    .toLowerCase()
                    .includes(search) ||
                origin
                    .toLowerCase()
                    .includes(search) ||
                destination
                    .toLowerCase()
                    .includes(search) ||
                sender
                    .toLowerCase()
                    .includes(search) ||
                receiver
                    .toLowerCase()
                    .includes(search);

            let matchesStatus = true;

            if (statusFilter === "DELIVERED") {
                matchesStatus =
                    shipment.status ===
                    "DELIVERED";
            }

            if (statusFilter === "IN_TRANSIT") {
                matchesStatus =
                    shipment.status ===
                        "IN_TRANSIT" ||
                    shipment.status ===
                        "OUT_FOR_DELIVERY";
            }

            if (statusFilter === "CREATED") {
                matchesStatus =
                    shipment.status ===
                        "CREATED" ||
                    shipment.status ===
                        "PICKED_UP";
            }

            return (
                matchesSearch &&
                matchesStatus
            );
        });
    }, [
        shipments,
        searchTerm,
        statusFilter,
    ]);

    // =====================================================
    // HELPERS
    // =====================================================

    const getTrackingNumber = (shipment) => {
        return (
            shipment.trackingNumber ||
            `ST-${shipment.id}`
        );
    };

    const getStatusClass = (status) => {
        switch (status) {
            case "DELIVERED":
                return "shipment-status delivered";

            case "IN_TRANSIT":
                return "shipment-status in-transit";

            case "OUT_FOR_DELIVERY":
                return "shipment-status out-for-delivery";

            case "PICKED_UP":
                return "shipment-status picked-up";

            case "CREATED":
                return "shipment-status created";

            case "FAILED_DELIVERY":
                return "shipment-status failed";

            case "CANCELLED":
                return "shipment-status cancelled";

            default:
                return "shipment-status default";
        }
    };

    const formatStatus = (status) => {
        if (!status) {
            return "Unknown";
        }

        return status
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(
                /\b\w/g,
                (letter) =>
                    letter.toUpperCase()
            );
    };

    const formatDate = (dateValue) => {
        if (!dateValue) {
            return "Not available";
        }

        try {
            return new Date(
                dateValue
            ).toLocaleString();
        } catch {
            return "Not available";
        }
    };

    const getEtaText = (shipment) => {
        const eta =
            etaPredictions[shipment.id];

        if (!eta) {
            return shipment.estimatedDelivery
                ? formatDate(
                      shipment.estimatedDelivery
                  )
                : "Not available";
        }

        return (
            eta.predictedDeliveryTime ||
            eta.predictedDelivery ||
            eta.estimatedDelivery ||
            (shipment.estimatedDelivery
                ? formatDate(
                      shipment.estimatedDelivery
                  )
                : "Not available")
        );
    };

    const getCurrentLocation = (shipment) => {
        return (
            shipment.currentLocation ||
            "Not available"
        );
    };

    const getInitial = () => {
        const name =
            user?.fullName ||
            user?.name ||
            user?.email ||
            "U";

        return name
            .charAt(0)
            .toUpperCase();
    };

    const getUserName = () => {
        return (
            user?.fullName ||
            user?.name ||
            user?.email ||
            "User"
        );
    };

    // =====================================================
    // RENDER
    // =====================================================

    return (
        <div className="shipments-page">

            {/* =================================================
                TOP HEADER
            ================================================= */}

            <header className="shipments-topbar">

                <div className="shipments-brand">

                    <div className="shipments-brand-logo">
                        S
                    </div>

                    <div>
                        <strong>
                            ShipTrack Pro
                        </strong>

                        <span>
                            Shipment Management
                        </span>
                    </div>

                </div>


                <div className="shipments-topbar-right">

                    <button
                        className="back-dashboard-button"
                        onClick={() =>
                            navigate(
                                "/dashboard"
                            )
                        }
                    >
                        ← Dashboard
                    </button>


                    <div className="shipments-user">

                        <div className="shipments-avatar">
                            {getInitial()}
                        </div>

                        <div>
                            <strong>
                                {getUserName()}
                            </strong>

                            <span>
                                {user?.role
                                    ?.replaceAll(
                                        "_",
                                        " "
                                    )
                                    ?.toLowerCase()
                                    ?.replace(
                                        /\b\w/g,
                                        (letter) =>
                                            letter.toUpperCase()
                                    )}
                            </span>
                        </div>

                    </div>

                </div>

            </header>


            {/* =================================================
                MAIN CONTENT
            ================================================= */}

            <main className="shipments-content">


                {/* PAGE HEADER */}

                <section className="shipments-page-header">

                    <div>

                        <span className="page-kicker">
                            SHIPMENTS
                        </span>

                        <h1>
                            My Shipments
                        </h1>

                        <p>
                            View, monitor and track
                            all your shipments in one
                            place.
                        </p>

                    </div>

                </section>


                {/* =================================================
                    SUMMARY CARDS
                ================================================= */}

                <section className="shipment-summary-grid">

                    <button
                        className={
                            statusFilter === "ALL"
                                ? "summary-card active"
                                : "summary-card"
                        }
                        onClick={() =>
                            setStatusFilter("ALL")
                        }
                    >
                        <div className="summary-icon blue">
                            📦
                        </div>

                        <div>
                            <span>
                                ALL SHIPMENTS
                            </span>

                            <strong>
                                {totalCount}
                            </strong>
                        </div>
                    </button>


                    <button
                        className={
                            statusFilter ===
                            "IN_TRANSIT"
                                ? "summary-card active"
                                : "summary-card"
                        }
                        onClick={() =>
                            setStatusFilter(
                                "IN_TRANSIT"
                            )
                        }
                    >
                        <div className="summary-icon orange">
                            🚚
                        </div>

                        <div>
                            <span>
                                IN TRANSIT
                            </span>

                            <strong>
                                {transitCount}
                            </strong>
                        </div>
                    </button>


                    <button
                        className={
                            statusFilter ===
                            "DELIVERED"
                                ? "summary-card active"
                                : "summary-card"
                        }
                        onClick={() =>
                            setStatusFilter(
                                "DELIVERED"
                            )
                        }
                    >
                        <div className="summary-icon green">
                            ✓
                        </div>

                        <div>
                            <span>
                                DELIVERED
                            </span>

                            <strong>
                                {deliveredCount}
                            </strong>
                        </div>
                    </button>


                    <button
                        className={
                            statusFilter ===
                            "CREATED"
                                ? "summary-card active"
                                : "summary-card"
                        }
                        onClick={() =>
                            setStatusFilter(
                                "CREATED"
                            )
                        }
                    >
                        <div className="summary-icon purple">
                            ◷
                        </div>

                        <div>
                            <span>
                                PENDING
                            </span>

                            <strong>
                                {createdCount}
                            </strong>
                        </div>
                    </button>

                </section>


                {/* =================================================
                    SEARCH + FILTER
                ================================================= */}

                <section className="shipment-toolbar">

                    <div className="shipment-search">

                        <span>
                            ⌕
                        </span>

                        <input
                            type="text"
                            placeholder="Search tracking number, origin, destination..."
                            value={searchTerm}
                            onChange={(event) =>
                                setSearchTerm(
                                    event.target.value
                                )
                            }
                        />

                    </div>


                    <div className="status-filter">

                        <button
                            className={
                                statusFilter === "ALL"
                                    ? "filter-button selected"
                                    : "filter-button"
                            }
                            onClick={() =>
                                setStatusFilter(
                                    "ALL"
                                )
                            }
                        >
                            All
                        </button>

                        <button
                            className={
                                statusFilter ===
                                "IN_TRANSIT"
                                    ? "filter-button selected"
                                    : "filter-button"
                            }
                            onClick={() =>
                                setStatusFilter(
                                    "IN_TRANSIT"
                                )
                            }
                        >
                            In Transit
                        </button>

                        <button
                            className={
                                statusFilter ===
                                "DELIVERED"
                                    ? "filter-button selected"
                                    : "filter-button"
                            }
                            onClick={() =>
                                setStatusFilter(
                                    "DELIVERED"
                                )
                            }
                        >
                            Delivered
                        </button>

                        <button
                            className={
                                statusFilter ===
                                "CREATED"
                                    ? "filter-button selected"
                                    : "filter-button"
                            }
                            onClick={() =>
                                setStatusFilter(
                                    "CREATED"
                                )
                            }
                        >
                            Pending
                        </button>

                    </div>

                </section>


                {/* =================================================
                    ERROR
                ================================================= */}

                {error && (
                    <div className="shipments-error">
                        <span>!</span>
                        {error}
                    </div>
                )}


                {/* =================================================
                    SHIPMENTS
                ================================================= */}

                <section className="shipments-list-section">

                    <div className="list-header">

                        <div>
                            <h2>
                                Shipment List
                            </h2>

                            <p>
                                Showing{" "}
                                <strong>
                                    {
                                        filteredShipments.length
                                    }
                                </strong>{" "}
                                of{" "}
                                <strong>
                                    {totalCount}
                                </strong>{" "}
                                shipments
                            </p>
                        </div>

                        <button
                            className="refresh-button"
                            onClick={loadShipments}
                        >
                            ↻ Refresh
                        </button>

                    </div>


                    {loading ? (

                        <div className="shipments-loading">

                            <div className="loading-spinner"></div>

                            <p>
                                Loading shipments...
                            </p>

                        </div>

                    ) : filteredShipments.length ===
                      0 ? (

                        <div className="no-results">

                            <div className="no-results-icon">
                                📦
                            </div>

                            <h3>
                                No shipments found
                            </h3>

                            <p>
                                Try changing your search
                                or status filter.
                            </p>

                            <button
                                onClick={() => {
                                    setSearchTerm("");
                                    setStatusFilter(
                                        "ALL"
                                    );
                                }}
                            >
                                Clear Filters
                            </button>

                        </div>

                    ) : (

                        <div className="shipment-list">

                            {filteredShipments.map(
                                (shipment) => (

                                    <article
                                        className="shipment-card"
                                        key={
                                            shipment.id
                                        }
                                        onClick={() =>
                                            navigate(
                                                `/shipments/${shipment.id}`
                                            )
                                        }
                                    >

                                        {/* CARD TOP */}

                                        <div className="shipment-card-header">

                                            <div className="tracking-section">

                                                <div className="package-icon">
                                                    📦
                                                </div>

                                                <div>

                                                    <span className="tracking-label">
                                                        TRACKING NUMBER
                                                    </span>

                                                    <h3>
                                                        {
                                                            getTrackingNumber(
                                                                shipment
                                                            )
                                                        }
                                                    </h3>

                                                </div>

                                            </div>


                                            <span
                                                className={getStatusClass(
                                                    shipment.status
                                                )}
                                            >
                                                <span className="status-dot"></span>

                                                {
                                                    formatStatus(
                                                        shipment.status
                                                    )
                                                }
                                            </span>

                                        </div>


                                        {/* ROUTE */}

                                        <div className="route-section">

                                            <div className="location">

                                                <span>
                                                    FROM
                                                </span>

                                                <strong>
                                                    {
                                                        shipment.origin ||
                                                        "Not available"
                                                    }
                                                </strong>

                                            </div>


                                            <div className="route-line">

                                                <div className="route-line-left"></div>

                                                <div className="route-truck">
                                                    🚚
                                                </div>

                                                <div className="route-line-right"></div>

                                            </div>


                                            <div className="location destination">

                                                <span>
                                                    TO
                                                </span>

                                                <strong>
                                                    {
                                                        shipment.destination ||
                                                        "Not available"
                                                    }
                                                </strong>

                                            </div>

                                        </div>


                                        {/* DETAILS */}

                                        <div className="shipment-details">

                                            <div className="detail-item">

                                                <span>
                                                    CURRENT LOCATION
                                                </span>

                                                <strong>
                                                    {getCurrentLocation(
                                                        shipment
                                                    )}
                                                </strong>

                                            </div>


                                            <div className="detail-item">

                                                <span>
                                                    SENDER
                                                </span>

                                                <strong>
                                                    {
                                                        shipment.sender ||
                                                        "Not available"
                                                    }
                                                </strong>

                                            </div>


                                            <div className="detail-item">

                                                <span>
                                                    RECEIVER
                                                </span>

                                                <strong>
                                                    {
                                                        shipment.receiver ||
                                                        "Not available"
                                                    }
                                                </strong>

                                            </div>


                                            <div className="detail-item">

                                                <span>
                                                    ESTIMATED DELIVERY
                                                </span>

                                                <strong>
                                                    {getEtaText(
                                                        shipment
                                                    )}
                                                </strong>

                                            </div>

                                        </div>


                                        {/* CARD FOOTER */}

                                        <div className="shipment-card-footer">

                                            <span>
                                                View shipment
                                                details
                                            </span>

                                            <span className="details-arrow">
                                                →
                                            </span>

                                        </div>

                                    </article>

                                )
                            )}

                        </div>

                    )}

                </section>

            </main>

        </div>
    );
}

export default Shipments;

