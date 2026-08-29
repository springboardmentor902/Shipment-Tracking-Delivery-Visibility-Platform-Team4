import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    getShipmentById,
    cancelShipment,
    getTrackingEvents,
    createTrackingEvent,
    getCurrentUser,
    updateShipmentStatus,
    getETAPrediction,
    predictETA,
} from "../services/authService";

import "./ShipmentDetail.css";


function ShipmentDetail() {

    const navigate = useNavigate();
    const { id } = useParams();

    // ========================================
    // USER
    // ========================================

    const currentUser = getCurrentUser();
    console.log("CURRENT USER:", currentUser);
    console.log("CURRENT ROLE:", currentUser?.role);

    const currentRole =
        currentUser?.role?.replace(
            "ROLE_",
            ""
        );


    // ========================================
    // SHIPMENT
    // ========================================

    const [shipment, setShipment] = useState(null);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");

    // ========================================
// ETA PREDICTION
// ========================================

    const [etaPrediction, setEtaPrediction] =
        useState(null);

    const [etaLoading, setEtaLoading] =
        useState(true);

    const [etaError, setEtaError] =
        useState("");

    const [etaRefreshing, setEtaRefreshing] =
        useState(false);


    // ========================================
    // CANCEL SHIPMENT
    // ========================================

    const [cancelling, setCancelling] = useState(false);

    const [cancelMessage, setCancelMessage] =
        useState("");


    // ========================================
    // STATUS UPDATE
    // ========================================

    const [updatingStatus, setUpdatingStatus] =
        useState(false);

    const [statusError, setStatusError] =
        useState("");


    // ========================================
    // TRACKING
    // ========================================

    const [trackingEvents, setTrackingEvents] =
        useState([]);

    const [trackingLoading, setTrackingLoading] =
        useState(true);

    const [trackingError, setTrackingError] =
        useState("");

    const [showTrackingForm, setShowTrackingForm] =
        useState(false);

    const [creatingEvent, setCreatingEvent] =
        useState(false);


    const [trackingForm, setTrackingForm] =
        useState({
            status: "CREATED",
            locationText: "",
            notes: "",
            latitude: "",
            longitude: "",
        });
    // ========================================
// LOAD ETA PREDICTION
// ========================================

    const loadETAPrediction = async () => {

        try {

            setEtaLoading(true);
            setEtaError("");

            const data =
                await getETAPrediction(id);

            console.log(
                "ETA prediction:",
                data
            );

            setEtaPrediction(data);

        } catch (err) {

            console.error(
                "ETA prediction error:",
                err
            );

            setEtaError(
                err.message ||
                "ETA prediction not available"
            );

            setEtaPrediction(null);

        } finally {

            setEtaLoading(false);

        }
    };


    // ========================================
    // INITIAL LOAD
    // ========================================
    useEffect(() => {

        const user = getCurrentUser();

        if (!user) {
            navigate("/login");
            return;
        }

        loadShipment();
        loadTrackingEvents();
        loadETAPrediction();

    }, [id, navigate]);
    // ========================================
// RECALCULATE ETA
// ========================================

    const handleRefreshETA = async () => {

        try {

            setEtaRefreshing(true);
            setEtaError("");

            const data =
                await predictETA(id);

            console.log(
                "ETA recalculated:",
                data
            );

            setEtaPrediction(data);

        } catch (err) {

            console.error(
                "ETA recalculation error:",
                err
            );

            setEtaError(
                err.message ||
                "Failed to recalculate ETA"
            );

        } finally {

            setEtaRefreshing(false);

        }
    };


    // ========================================
    // LOAD SHIPMENT
    // ========================================

    const loadShipment = async () => {

        try {

            setLoading(true);
            setError("");

            const data =
                await getShipmentById(id);

            console.log(
                "Shipment detail:",
                data
            );

            setShipment(data);
            console.log("SHIPMENT STATUS:", data?.status);

        } catch (err) {

            console.error(
                "Shipment detail error:",
                err
            );

            setError(
                err.message ||
                "Failed to load shipment"
            );

        } finally {

            setLoading(false);

        }
    };


    // ========================================
    // LOAD TRACKING EVENTS
    // ========================================

    const loadTrackingEvents = async () => {

        try {

            setTrackingLoading(true);
            setTrackingError("");

            const data =
                await getTrackingEvents(id);

            console.log(
                "Tracking events:",
                data
            );

            const sortedEvents =
                [...data].sort(
                    (a, b) =>
                        new Date(
                            b.eventTimestamp
                        ) -
                        new Date(
                            a.eventTimestamp
                        )
                );

            setTrackingEvents(
                sortedEvents
            );

        } catch (err) {

            console.error(
                "Tracking history error:",
                err
            );

            setTrackingError(
                err.message ||
                "Failed to load tracking history"
            );

        } finally {

            setTrackingLoading(false);

        }
    };


    // ========================================
    // GET NEXT STATUS
    // ========================================

    const getNextStatus = (status) => {

        switch (status) {

            case "CREATED":
                return "PICKED_UP";

            case "PICKED_UP":
                return "IN_TRANSIT";

            case "IN_TRANSIT":
                return "OUT_FOR_DELIVERY";

            case "OUT_FOR_DELIVERY":
                return "DELIVERED";

            default:
                return null;
        }
    };


    // ========================================
    // UPDATE NEXT STATUS
    // ========================================

    const handleNextStatus = async () => {

        if (!shipment) {
            return;
        }

        const nextStatus =
            getNextStatus(
                shipment.status
            );

        if (!nextStatus) {
            return;
        }

        try {

            setUpdatingStatus(true);
            setStatusError("");
            setError("");

            console.log(
                "Updating shipment status:",
                shipment.status,
                "→",
                nextStatus
            );

            const updatedShipment =
                await updateShipmentStatus(
                    shipment.id,
                    nextStatus
                );

            console.log(
                "Shipment status updated:",
                updatedShipment
            );

            setShipment(
                updatedShipment
            );

            setCancelMessage("");

            // Refresh timeline
            await loadTrackingEvents();

        } catch (err) {

            console.error(
                "Status update error:",
                err
            );

            setStatusError(
                err.message ||
                "Failed to update shipment status"
            );

        } finally {

            setUpdatingStatus(false);

        }
    };


    // ========================================
    // CANCEL SHIPMENT
    // ========================================

    const handleCancelShipment = async () => {

        const confirmed =
            window.confirm(
                "Are you sure you want to cancel this shipment?"
            );

        if (!confirmed) {
            return;
        }

        try {

            setCancelling(true);

            setError("");
            setStatusError("");
            setCancelMessage("");

            const result =
                await cancelShipment(
                    id
                );

            if (
                result &&
                result.status
            ) {

                setShipment(result);

            } else {

                setShipment(
                    previous => ({
                        ...previous,
                        status: "CANCELLED",
                    })
                );

            }

            setCancelMessage(
                "Shipment cancelled successfully."
            );

            await loadTrackingEvents();

        } catch (err) {

            console.error(
                "Cancel shipment error:",
                err
            );

            setError(
                err.message ||
                "Failed to cancel shipment"
            );

        } finally {

            setCancelling(false);

        }
    };


    // ========================================
    // TRACKING FORM
    // ========================================

    const handleTrackingChange = (event) => {

        const {
            name,
            value
        } = event.target;

        setTrackingForm(
            previous => ({
                ...previous,
                [name]: value,
            })
        );

    };


    // ========================================
    // CREATE TRACKING EVENT
    // ========================================

    const handleCreateTrackingEvent =
        async (event) => {

            event.preventDefault();

            try {

                setCreatingEvent(true);

                setTrackingError("");
                setError("");

                const eventData = {

                    status:
                    trackingForm.status,

                    locationText:
                        trackingForm.locationText ||
                        null,

                    notes:
                        trackingForm.notes ||
                        null,

                    latitude:
                        trackingForm.latitude !== ""
                            ? Number(
                                trackingForm.latitude
                            )
                            : null,

                    longitude:
                        trackingForm.longitude !== ""
                            ? Number(
                                trackingForm.longitude
                            )
                            : null,
                };

                const createdEvent =
                    await createTrackingEvent(
                        id,
                        eventData
                    );

                console.log(
                    "Tracking event created:",
                    createdEvent
                );

                setTrackingEvents(
                    previous =>
                        [
                            createdEvent,
                            ...previous,
                        ].sort(
                            (a, b) =>
                                new Date(
                                    b.eventTimestamp
                                ) -
                                new Date(
                                    a.eventTimestamp
                                )
                        )
                );

                // Refresh shipment so status is updated
                await loadShipment();
                await loadETAPrediction();

                setTrackingForm({
                    status:
                        "CREATED",
                    locationText: "",
                    notes: "",
                    latitude: "",
                    longitude: "",
                });

                setShowTrackingForm(false);

            } catch (err) {

                console.error(
                    "Create tracking event error:",
                    err
                );

                setTrackingError(
                    err.message ||
                    "Failed to create tracking event"
                );

            } finally {

                setCreatingEvent(false);

            }
        };


    // ========================================
    // LOADING
    // ========================================

    if (loading) {

        return (
            <div className="shipment-detail-page">

                <div className="shipment-detail-card">

                    <p>
                        Loading shipment...
                    </p>

                </div>

            </div>
        );
    }


    // ========================================
    // ERROR
    // ========================================

    if (error && !shipment) {

        return (
            <div className="shipment-detail-page">

                <div className="shipment-detail-card">

                    <div className="detail-error">
                        {error}
                    </div>

                    <button
                        className="back-button"
                        onClick={() =>
                            navigate("/dashboard")
                        }
                    >
                        ← Back to Dashboard
                    </button>

                </div>

            </div>
        );
    }


    // ========================================
    // RENDER
    // ========================================

    return (

        <div className="shipment-detail-page">

            <div className="shipment-detail-container">


                {/* =================================
                    HEADER
                ================================= */}

                <div className="detail-top">

                    <button
                        className="back-button"
                        onClick={() =>
                            navigate("/dashboard")
                        }
                    >
                        ← Dashboard
                    </button>

                    <div>

                        <h1>
                            Shipment Details
                        </h1>

                        <p>
                            View shipment information,
                            status and tracking history
                        </p>

                    </div>

                </div>


                {/* =================================
                    GENERAL ERROR
                ================================= */}

                {error && (

                    <div className="detail-error">
                        {error}
                    </div>

                )}


                {/* =================================
                    SUCCESS
                ================================= */}

                {cancelMessage && (

                    <div className="detail-success">
                        {cancelMessage}
                    </div>

                )}


                {shipment && (

                    <>


                        {/* =================================
                            TRACKING HEADER
                        ================================= */}

                        <div className="shipment-detail-card tracking-header-card">

                            <div>

                                <span className="detail-label">
                                    Tracking Number
                                </span>

                                <h2>
                                    {
                                        shipment.trackingNumber
                                    }
                                </h2>

                            </div>


                            <span
                                className={
                                    `shipment-status status-${String(
                                        shipment.status || ""
                                    ).toLowerCase()}`
                                }
                            >
                                {
                                    shipment.status
                                }
                            </span>

                        </div>


                        {/* =================================
                            ROUTE
                        ================================= */}

                        <div className="shipment-detail-card">

                            <h2>
                                Shipment Route
                            </h2>

                            <div className="route-details">

                                <div className="route-point">

                                    <span className="route-label">
                                        Origin
                                    </span>

                                    <strong>
                                        {
                                            shipment.origin
                                        }
                                    </strong>

                                </div>


                                <div className="route-arrow">
                                    →
                                </div>


                                <div className="route-point">

                                    <span className="route-label">
                                        Destination
                                    </span>

                                    <strong>
                                        {
                                            shipment.destination
                                        }
                                    </strong>

                                </div>

                            </div>

                        </div>


                        {/* =================================
                            SHIPMENT INFORMATION
                        ================================= */}

                        <div className="shipment-detail-card">

                            <h2>
                                Shipment Information
                            </h2>

                            <div className="detail-grid">


                                <div className="detail-item">

                                    <span>
                                        Sender
                                    </span>

                                    <strong>
                                        {
                                            shipment.sender
                                        }
                                    </strong>

                                </div>


                                <div className="detail-item">

                                    <span>
                                        Receiver
                                    </span>

                                    <strong>
                                        {
                                            shipment.receiver
                                        }
                                    </strong>

                                </div>


                                <div className="detail-item">

                                    <span>
                                        Current Location
                                    </span>

                                    <strong>
                                        {
                                            shipment.currentLocation ||
                                            "Not available"
                                        }
                                    </strong>

                                </div>


                                <div className="detail-item">

                                    <span>
                                        Status
                                    </span>

                                    <strong>
                                        {
                                            shipment.status
                                        }
                                    </strong>

                                </div>


                                <div className="detail-item">

                                    <span>
                                        Estimated Delivery
                                    </span>

                                    <strong>
                                        {
                                            shipment.estimatedDelivery
                                                ? new Date(
                                                    shipment.estimatedDelivery
                                                ).toLocaleString()
                                                : "Not available"
                                        }
                                    </strong>

                                </div>


                                <div className="detail-item">

                                    <span>
                                        Created
                                    </span>

                                    <strong>
                                        {
                                            shipment.createdAt
                                                ? new Date(
                                                    shipment.createdAt
                                                ).toLocaleString()
                                                : "Not available"
                                        }
                                    </strong>

                                </div>

                            </div>

                        </div>
                        {/* =================================
    ETA & DELAY RISK
================================= */}

                        <div className="shipment-detail-card eta-card">

                            <div className="eta-header">

                                <div>
                                    <h2>
                                        Estimated Delivery & Delay Risk
                                    </h2>

                                    <p>
                                        AI-style rule-based ETA prediction
                                    </p>
                                </div>

                                <button
                                    className="eta-refresh-button"
                                    onClick={handleRefreshETA}
                                    disabled={etaRefreshing}
                                >
                                    {etaRefreshing
                                        ? "Calculating..."
                                        : "Refresh ETA"}
                                </button>

                            </div>


                            {etaLoading ? (

                                <div className="eta-loading">
                                    Loading ETA prediction...
                                </div>

                            ) : etaError ? (

                                <div className="detail-error">
                                    {etaError}
                                </div>

                            ) : etaPrediction ? (

                                <div className="eta-content">

                                    <div className="eta-main">

                <span className="eta-label">
                    Predicted Delivery
                </span>

                                        <strong className="eta-time">

                                            {etaPrediction.predictedDeliveryTime
                                                ? new Date(
                                                    etaPrediction.predictedDeliveryTime
                                                ).toLocaleString()
                                                : "Not available"}

                                        </strong>

                                    </div>


                                    <div className="eta-risk">

                <span className="eta-label">
                    Delay Risk
                </span>

                                        <strong
                                            className={
                                                `eta-risk-score ${
                                                    etaPrediction.delayRiskScore >= 7
                                                        ? "risk-high"
                                                        : etaPrediction.delayRiskScore >= 4
                                                            ? "risk-medium"
                                                            : "risk-low"
                                                }`
                                            }
                                        >
                                            {etaPrediction.delayRiskScore}
                                            /10
                                        </strong>

                                    </div>


                                    <div className="eta-confidence">

                <span className="eta-label">
                    Confidence
                </span>

                                        <strong>
                                            {etaPrediction.confidenceScore}%
                                        </strong>

                                    </div>


                                    <div className="eta-factors">

                <span className="eta-label">
                    Factors
                </span>

                                        <p>
                                            {etaPrediction.factors ||
                                                "No factors available"}
                                        </p>

                                    </div>

                                </div>

                            ) : (

                                <div className="eta-loading">
                                    No ETA prediction available.
                                </div>

                            )}

                        </div>


                        {/* =================================
                            ACTIONS
                        ================================= */}

                        <div className="shipment-detail-card">

                            <h2>
                                Actions
                            </h2>


                            {/* STATUS ERROR */}

                            {statusError && (

                                <div className="detail-error">

                                    {statusError}

                                </div>

                            )}


                            <div className="detail-actions">


                                {/* NEXT STATUS */}

                                {(
                                        currentRole ===
                                        "ADMINISTRATOR" ||

                                        currentRole ===
                                        "LOGISTICS_OPERATOR"
                                    ) &&
                                    getNextStatus(
                                        shipment.status
                                    ) && (

                                        <button
                                            className="next-status-button"
                                            onClick={
                                                handleNextStatus
                                            }
                                            disabled={
                                                updatingStatus
                                            }
                                        >

                                            {
                                                updatingStatus
                                                    ? "Updating..."
                                                    : `Move to ${getNextStatus(
                                                        shipment.status
                                                    )}`
                                            }

                                        </button>

                                    )}


                                {/* CANCEL */}

                                {shipment.status !==
                                    "DELIVERED" &&
                                    shipment.status !==
                                    "CANCELLED" && (

                                        <button
                                            className="cancel-shipment-button"
                                            onClick={
                                                handleCancelShipment
                                            }
                                            disabled={
                                                cancelling
                                            }
                                        >

                                            {
                                                cancelling
                                                    ? "Cancelling..."
                                                    : "Cancel Shipment"
                                            }

                                        </button>

                                    )}


                                {/* DELIVERED MESSAGE */}

                                {shipment.status ===
                                    "DELIVERED" && (

                                        <p className="action-note">

                                            This shipment has
                                            already been delivered.

                                        </p>

                                    )}


                                {/* CANCELLED MESSAGE */}

                                {shipment.status ===
                                    "CANCELLED" && (

                                        <p className="action-note">

                                            This shipment has
                                            been cancelled.

                                        </p>

                                    )}

                            </div>

                        </div>


                        {/* =================================
                            TRACKING HISTORY
                        ================================= */}

                        <div className="shipment-detail-card tracking-history-card">

                            <div className="tracking-history-header">

                                <div>

                                    <h2>
                                        Tracking History
                                    </h2>

                                    <p>
                                        Shipment movement
                                        and status updates
                                    </p>

                                </div>


                                {/* ONLY OPERATOR / ADMIN */}

                                {(
                                    currentRole ===
                                    "ADMINISTRATOR" ||

                                    currentRole ===
                                    "LOGISTICS_OPERATOR"
                                ) && (

                                    <button
                                        className="add-event-button"
                                        onClick={() =>
                                            setShowTrackingForm(
                                                previous =>
                                                    !previous
                                            )
                                        }
                                    >

                                        {
                                            showTrackingForm
                                                ? "Close"
                                                : "+ Add Event"
                                        }

                                    </button>

                                )}

                            </div>


                            {trackingError && (

                                <div className="detail-error">

                                    {trackingError}

                                </div>

                            )}


                            {/* =================================
                                CREATE TRACKING EVENT FORM
                            ================================= */}

                            {showTrackingForm && (

                                <form
                                    className="tracking-event-form"
                                    onSubmit={
                                        handleCreateTrackingEvent
                                    }
                                >

                                    <h3>
                                        Add Tracking Event
                                    </h3>


                                    <div className="tracking-form-grid">


                                        <div className="tracking-form-group">

                                            <label>
                                                Status
                                            </label>

                                            <select
                                                name="status"
                                                value={
                                                    trackingForm.status
                                                }
                                                onChange={
                                                    handleTrackingChange
                                                }
                                                required
                                            >

                                                <option value="CREATED">
                                                    CREATED
                                                </option>

                                                <option value="PICKED_UP">
                                                    PICKED_UP
                                                </option>

                                                <option value="IN_TRANSIT">
                                                    IN_TRANSIT
                                                </option>

                                                <option value="OUT_FOR_DELIVERY">
                                                    OUT_FOR_DELIVERY
                                                </option>

                                                <option value="FAILED_DELIVERY">
                                                    FAILED_DELIVERY
                                                </option>

                                                <option value="DELIVERED">
                                                    DELIVERED
                                                </option>

                                                <option value="CANCELLED">
                                                    CANCELLED
                                                </option>

                                            </select>

                                        </div>


                                        <div className="tracking-form-group">

                                            <label>
                                                Location
                                            </label>

                                            <input
                                                type="text"
                                                name="locationText"
                                                value={
                                                    trackingForm.locationText
                                                }
                                                onChange={
                                                    handleTrackingChange
                                                }
                                                placeholder="Hyderabad Hub"
                                            />

                                        </div>


                                        <div className="tracking-form-group">

                                            <label>
                                                Latitude
                                            </label>

                                            <input
                                                type="number"
                                                step="any"
                                                name="latitude"
                                                value={
                                                    trackingForm.latitude
                                                }
                                                onChange={
                                                    handleTrackingChange
                                                }
                                                placeholder="17.3850"
                                            />

                                        </div>


                                        <div className="tracking-form-group">

                                            <label>
                                                Longitude
                                            </label>

                                            <input
                                                type="number"
                                                step="any"
                                                name="longitude"
                                                value={
                                                    trackingForm.longitude
                                                }
                                                onChange={
                                                    handleTrackingChange
                                                }
                                                placeholder="78.4867"
                                            />

                                        </div>

                                    </div>


                                    <div className="tracking-form-group">

                                        <label>
                                            Notes
                                        </label>

                                        <textarea
                                            name="notes"
                                            value={
                                                trackingForm.notes
                                            }
                                            onChange={
                                                handleTrackingChange
                                            }
                                            placeholder="Package arrived at Hyderabad hub"
                                            rows="3"
                                        />

                                    </div>


                                    <div className="tracking-form-actions">

                                        <button
                                            type="submit"
                                            className="save-event-button"
                                            disabled={
                                                creatingEvent
                                            }
                                        >

                                            {
                                                creatingEvent
                                                    ? "Adding..."
                                                    : "Add Tracking Event"
                                            }

                                        </button>

                                    </div>

                                </form>

                            )}


                            {/* =================================
                                TRACKING TIMELINE
                            ================================= */}

                            {trackingLoading ? (

                                <div className="tracking-loading">
                                    Loading tracking history...
                                </div>

                            ) : trackingEvents.length === 0 ? (

                                <div className="tracking-empty">
                                    No tracking events available yet.
                                </div>

                            ) : (

                                <div className="tracking-timeline">

                                    {trackingEvents.map(
                                        (event, index) => (

                                            <div
                                                className="timeline-item"
                                                key={
                                                    event.id
                                                }
                                            >

                                                <div className="timeline-line">

                                                    {index !==
                                                        trackingEvents.length - 1 && (
                                                            <span />
                                                        )}

                                                </div>


                                                <div
                                                    className={
                                                        `timeline-dot timeline-${String(
                                                            event.status || ""
                                                        ).toLowerCase()}`
                                                    }
                                                />


                                                <div className="timeline-content">

                                                    <div className="timeline-header">

                                                        <div>

                                                            <h3>
                                                                {
                                                                    event.status
                                                                }
                                                            </h3>

                                                            <span>
                                                                {
                                                                    event.eventTimestamp
                                                                        ? new Date(
                                                                            event.eventTimestamp
                                                                        ).toLocaleString()
                                                                        : "Unknown time"
                                                                }
                                                            </span>

                                                        </div>

                                                    </div>


                                                    {event.locationText && (

                                                        <p>

                                                            <strong>
                                                                Location:
                                                            </strong>{" "}

                                                            {
                                                                event.locationText
                                                            }

                                                        </p>

                                                    )}


                                                    {event.notes && (

                                                        <p>

                                                            <strong>
                                                                Notes:
                                                            </strong>{" "}

                                                            {
                                                                event.notes
                                                            }

                                                        </p>

                                                    )}


                                                    {event.updatedBy && (

                                                        <p>

                                                            <strong>
                                                                Updated by:
                                                            </strong>{" "}

                                                            {
                                                                event.updatedBy
                                                            }

                                                        </p>

                                                    )}


                                                    {(
                                                        event.latitude !==
                                                        null &&
                                                        event.latitude !==
                                                        undefined &&
                                                        event.longitude !==
                                                        null &&
                                                        event.longitude !==
                                                        undefined
                                                    ) && (

                                                        <p>

                                                            <strong>
                                                                Coordinates:
                                                            </strong>{" "}

                                                            {
                                                                event.latitude
                                                            }

                                                            {" , "}

                                                            {
                                                                event.longitude
                                                            }

                                                        </p>

                                                    )}

                                                </div>

                                            </div>

                                        )
                                    )}

                                </div>

                            )}

                        </div>

                    </>

                )}

            </div>

        </div>
    );
}


export default ShipmentDetail;