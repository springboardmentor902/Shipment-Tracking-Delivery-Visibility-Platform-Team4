import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

import {
    getCurrentUser,
    getShipments,
    createShipment,
    createPackage,
    logoutUser,
} from "../services/authService";

import {
    getNotifications,
    markNotificationAsRead,
    registerPushSubscription,
} from "../services/notificationService";

function Dashboard() {
    const navigate = useNavigate();

    // =====================================================
    // USER
    // =====================================================

    const [user, setUser] = useState(null);

    // =====================================================
    // SHIPMENTS
    // =====================================================

    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    const [notifications, setNotifications] = useState([]);
    const [showNotifications, setShowNotifications] = useState(false);

    // =====================================================
    // CREATE SHIPMENT
    // =====================================================

    const [showShipmentForm, setShowShipmentForm] = useState(false);
    const [creatingShipment, setCreatingShipment] = useState(false);
    const [shipmentError, setShipmentError] = useState("");

    const [shipmentForm, setShipmentForm] = useState({
        sender: "",
        receiver: "",
        origin: "",
        destination: "",
        currentLocation: "",
        estimatedDelivery: "",
    });

    // =====================================================
    // PACKAGE
    // =====================================================

    const [packageForm, setPackageForm] = useState({
        description: "",
        weightKg: "",
        lengthCm: "",
        widthCm: "",
        heightCm: "",
        quantity: 1,
        declaredValue: "",
        fragile: false,
    });

    // =====================================================
    // INITIAL LOAD
    // =====================================================

    useEffect(() => {
        const currentUser = getCurrentUser();

        if (!currentUser) {
            navigate("/login");
            return;
        }

        setUser(currentUser);

        loadNotifications();

        if (
            currentUser.role === "CUSTOMER" ||
            currentUser.role === "BUSINESS_CLIENT" ||
            currentUser.role === "ADMINISTRATOR" ||
            currentUser.role === "LOGISTICS_OPERATOR"
        ) {
            loadShipments();
        } else {
            setLoading(false);
        }

        registerPushSubscription()
            .then(() => {
                console.log(
                    "Push notifications enabled successfully."
                );
            })
            .catch((pushError) => {
                console.error(
                    "Push notification setup failed:",
                    pushError
                );
            });
    }, [navigate]);

    // =====================================================
    // LOAD NOTIFICATIONS
    // =====================================================

    const loadNotifications = async () => {
        try {
            const data = await getNotifications();

            setNotifications(
                Array.isArray(data) ? data : []
            );
        } catch (notificationError) {
            console.error(
                "Failed to load notifications:",
                notificationError
            );
        }
    };

    // =====================================================
    // LOAD SHIPMENTS
    // =====================================================

    const loadShipments = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getShipments();

            setShipments(
                Array.isArray(data) ? data : []
            );
        } catch (shipmentFetchError) {
            console.error(
                "Shipment fetch error:",
                shipmentFetchError
            );

            setError(
                shipmentFetchError.message ||
                "Failed to load shipments."
            );
        } finally {
            setLoading(false);
        }
    };

    // =====================================================
    // NOTIFICATION CLICK
    // =====================================================

    const handleNotificationClick = async (
        notification
    ) => {
        try {
            if (!notification.read) {
                await markNotificationAsRead(
                    notification.id
                );

                setNotifications((previous) =>
                    previous.map((item) =>
                        item.id === notification.id
                            ? {
                                  ...item,
                                  read: true,
                              }
                            : item
                    )
                );
            }
        } catch (notificationError) {
            console.error(
                "Failed to mark notification as read:",
                notificationError
            );
        }
    };

    // =====================================================
    // SHIPMENT FORM CHANGE
    // =====================================================

    const handleShipmentChange = (event) => {
        const {
            name,
            value,
        } = event.target;

        setShipmentForm((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    // =====================================================
    // PACKAGE FORM CHANGE
    // =====================================================

    const handlePackageChange = (event) => {
        const {
            name,
            value,
            type,
            checked,
        } = event.target;

        setPackageForm((previous) => ({
            ...previous,
            [name]:
                type === "checkbox"
                    ? checked
                    : value,
        }));
    };

    // =====================================================
    // CREATE SHIPMENT + PACKAGE
    // =====================================================

    const handleCreateShipment = async (
        event
    ) => {
        event.preventDefault();

        try {
            setCreatingShipment(true);
            setShipmentError("");

            const shipmentData = {
                sender: shipmentForm.sender,
                receiver: shipmentForm.receiver,
                origin: shipmentForm.origin,
                destination:
                    shipmentForm.destination,
                currentLocation:
                    shipmentForm.currentLocation ||
                    null,
                estimatedDelivery:
                    shipmentForm.estimatedDelivery
                        ? new Date(
                              shipmentForm.estimatedDelivery
                          ).toISOString()
                        : null,
            };

            console.log(
                "Creating shipment:",
                shipmentData
            );

            const createdShipment =
                await createShipment(
                    shipmentData
                );

            console.log(
                "Created shipment:",
                createdShipment
            );

            if (
                !createdShipment ||
                !createdShipment.id
            ) {
                throw new Error(
                    "Shipment was created but no shipment ID was returned."
                );
            }

            const packageData = {
                shipmentId:
                    createdShipment.id,
                description:
                    packageForm.description,
                weightKg: Number(
                    packageForm.weightKg
                ),
                lengthCm: Number(
                    packageForm.lengthCm
                ),
                widthCm: Number(
                    packageForm.widthCm
                ),
                heightCm: Number(
                    packageForm.heightCm
                ),
                quantity: Number(
                    packageForm.quantity
                ),
                declaredValue: Number(
                    packageForm.declaredValue
                ),
                fragile:
                    packageForm.fragile,
            };

            console.log(
                "Creating package:",
                packageData
            );

            await createPackage(
                packageData
            );

            setShipments((previous) => [
                createdShipment,
                ...previous,
            ]);

            setShipmentForm({
                sender: "",
                receiver: "",
                origin: "",
                destination: "",
                currentLocation: "",
                estimatedDelivery: "",
            });

            setPackageForm({
                description: "",
                weightKg: "",
                lengthCm: "",
                widthCm: "",
                heightCm: "",
                quantity: 1,
                declaredValue: "",
                fragile: false,
            });

            setShowShipmentForm(false);
        } catch (createError) {
            console.error(
                "Create shipment/package error:",
                createError
            );

            setShipmentError(
                createError.message ||
                "Failed to create shipment."
            );
        } finally {
            setCreatingShipment(false);
        }
    };

    // =====================================================
    // LOGOUT
    // =====================================================

    const handleLogout = () => {
        logoutUser();
        navigate("/login");
    };

    // =====================================================
    // NAVIGATION
    // =====================================================

    const getAnalyticsRoute = () => {
        if (
            user?.role ===
            "ADMINISTRATOR"
        ) {
            return "/admin/analytics";
        }

        if (
            user?.role ===
            "BUSINESS_CLIENT"
        ) {
            return "/business/analytics";
        }

        return "/customer/analytics";
    };

    // =====================================================
    // USER DISPLAY
    // =====================================================

    const getUserName = () => {
        return (
            user?.fullName ||
            user?.name ||
            user?.email ||
            "User"
        );
    };

    const getUserInitial = () => {
        return getUserName()
            .charAt(0)
            .toUpperCase();
    };

    const getRoleName = () => {
        if (!user?.role) {
            return "User";
        }

        return user.role
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(
                /\b\w/g,
                (letter) =>
                    letter.toUpperCase()
            );
    };

    // =====================================================
    // STATISTICS
    // =====================================================

    const totalShipments =
        shipments.length;

    const inTransit =
        shipments.filter(
            (shipment) =>
                shipment.status ===
                    "IN_TRANSIT" ||
                shipment.status ===
                    "OUT_FOR_DELIVERY"
        ).length;

    const delivered =
        shipments.filter(
            (shipment) =>
                shipment.status ===
                "DELIVERED"
        ).length;

    const pending =
        shipments.filter(
            (shipment) =>
                shipment.status ===
                    "CREATED" ||
                shipment.status ===
                    "PICKED_UP"
        ).length;

    const unreadCount =
        notifications.filter(
            (notification) =>
                !notification.read
        ).length;

    // =====================================================
    // RECENT SHIPMENTS
    // =====================================================

    const recentShipments =
        shipments.slice(0, 5);

    // =====================================================
    // SHIPMENT HELPERS
    // =====================================================

    const getTrackingId = (
        shipment
    ) => {
        return (
            shipment.trackingNumber ||
            shipment.trackingId ||
            shipment.trackingCode ||
            shipment.trackingNumber ||
            `ST-${shipment.id}`
        );
    };

    const getOrigin = (
        shipment
    ) => {
        return (
            shipment.origin ||
            shipment.source ||
            "—"
        );
    };

    const getDestination = (
        shipment
    ) => {
        return (
            shipment.destination ||
            shipment.receiverAddress ||
            "—"
        );
    };

    const formatStatus = (
        status
    ) => {
        if (!status) {
            return "Unknown";
        }

        return status
            .replaceAll(
                "_",
                " "
            )
            .toLowerCase()
            .replace(
                /\b\w/g,
                (letter) =>
                    letter.toUpperCase()
            );
    };

    const getStatusClass = (
        status
    ) => {
        switch (status) {
            case "DELIVERED":
                return "delivered";

            case "IN_TRANSIT":
                return "in-transit";

            case "OUT_FOR_DELIVERY":
                return "out-for-delivery";

            case "PICKED_UP":
                return "picked-up";

            case "CREATED":
                return "created";

            case "FAILED_DELIVERY":
                return "failed";

            case "CANCELLED":
                return "cancelled";

            default:
                return "default";
        }
    };

    const getPercentage = (
        value
    ) => {
        if (
            totalShipments === 0
        ) {
            return 0;
        }

        return Math.round(
            (value /
                totalShipments) *
                100
        );
    };

    // =====================================================
    // LOADING SCREEN
    // =====================================================

    if (loading && !user) {
        return (
            <div className="dashboard-loading">
                <div className="loading-spinner"></div>

                <p>
                    Loading dashboard...
                </p>
            </div>
        );
    }

    // =====================================================
    // RENDER
    // =====================================================

    return (
        <div className="dashboard">

            {/* =================================================
                SIDEBAR
            ================================================= */}

            <aside className="sidebar">

                <div className="sidebar-brand">

                    <div className="brand-logo">
                        S
                    </div>

                    <div className="brand-text">

                        <strong>
                            ShipTrack
                        </strong>

                        <span>
                            PRO
                        </span>

                    </div>

                </div>

                <div className="brand-description">
                    Shipment & Delivery
                    Visibility Platform
                </div>

                <nav className="sidebar-navigation">

                    <div className="navigation-title">
                        OVERVIEW
                    </div>

                    <button
                        className="navigation-item active"
                        onClick={() =>
                            navigate(
                                "/dashboard"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ▦
                        </span>

                        <span>
                            Dashboard
                        </span>
                    </button>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                "/shipments"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            □
                        </span>

                        <span>
                            My Shipments
                        </span>
                    </button>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                getAnalyticsRoute()
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ◫
                        </span>

                        <span>
                            Analytics
                        </span>
                    </button>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                "/route-optimization"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ⤢
                        </span>

                        <span>
                            Route Management
                        </span>
                    </button>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                "/reports-export"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ▤
                        </span>

                        <span>
                            Reports & Export
                        </span>
                    </button>

                    {(user?.role ===
                        "LOGISTICS_OPERATOR" ||
                        user?.role ===
                            "SUPPORT_AGENT" ||
                        user?.role ===
                            "ADMINISTRATOR") && (
                        <button
                            className="navigation-item"
                            onClick={() =>
                                navigate(
                                    "/pod-verification"
                                )
                            }
                        >
                            <span className="navigation-icon">
                                ✓
                            </span>

                            <span>
                                Proof of Delivery
                            </span>
                        </button>
                    )}

                    {user?.role ===
                        "ADMINISTRATOR" && (
                        <>
                            <div className="navigation-title extra-title">
                                ADMINISTRATION
                            </div>

                            <button
                                className="navigation-item"
                                onClick={() =>
                                    navigate(
                                        "/user-management"
                                    )
                                }
                            >
                                <span className="navigation-icon">
                                    ♙
                                </span>

                                <span>
                                    User Management
                                </span>
                            </button>
                        </>
                    )}

                    <div className="navigation-title extra-title">
                        ACCOUNT
                    </div>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                "/profile"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ◯
                        </span>

                        <span>
                            My Profile
                        </span>
                    </button>

                    <button
                        className="navigation-item"
                        onClick={() =>
                            navigate(
                                "/settings"
                            )
                        }
                    >
                        <span className="navigation-icon">
                            ⚙
                        </span>

                        <span>
                            Account Settings
                        </span>
                    </button>

                </nav>

                <div className="sidebar-bottom">

                    <div className="help-card">

                        <div className="help-icon">
                            ?
                        </div>

                        <div>
                            <strong>
                                Need help?
                            </strong>

                            <span>
                                Contact support
                            </span>
                        </div>

                    </div>

                    <button
                        className="sidebar-logout"
                        onClick={
                            handleLogout
                        }
                    >
                        <span>
                            ↪
                        </span>

                        <span>
                            Logout
                        </span>
                    </button>

                </div>

            </aside>


            {/* =================================================
                MAIN AREA
            ================================================= */}

            <div className="main-area">

                {/* =================================================
                    TOPBAR
                ================================================= */}

                <header className="topbar">

                    <div className="search-box">

                        <span className="search-symbol">
                            ⌕
                        </span>

                        <input
                            type="text"
                            placeholder="Search shipments..."
                        />

                    </div>


                    <div className="topbar-right">

                        {/* NOTIFICATIONS */}

                        <div className="notification-wrapper">

                            <button
                                className="notification-button"
                                onClick={() =>
                                    setShowNotifications(
                                        !showNotifications
                                    )
                                }
                            >
                                🔔

                                {unreadCount >
                                    0 && (
                                    <span className="notification-badge">
                                        {
                                            unreadCount
                                        }
                                    </span>
                                )}
                            </button>


                            {showNotifications && (
                                <div className="notification-panel">

                                    <div className="notification-panel-header">

                                        <div>
                                            <strong>
                                                Notifications
                                            </strong>

                                            <span>
                                                {
                                                    unreadCount
                                                }{" "}
                                                unread
                                            </span>
                                        </div>

                                        <button
                                            onClick={() =>
                                                setShowNotifications(
                                                    false
                                                )
                                            }
                                        >
                                            ×
                                        </button>

                                    </div>

                                    <div className="notification-list">

                                        {notifications.length ===
                                        0 ? (
                                            <div className="empty-notifications">
                                                No notifications
                                            </div>
                                        ) : (
                                            notifications
                                                .slice(
                                                    0,
                                                    6
                                                )
                                                .map(
                                                    (
                                                        notification
                                                    ) => (
                                                        <div
                                                            key={
                                                                notification.id
                                                            }
                                                            className={
                                                                notification.read
                                                                    ? "notification-item read"
                                                                    : "notification-item unread"
                                                            }
                                                            onClick={() =>
                                                                handleNotificationClick(
                                                                    notification
                                                                )
                                                            }
                                                        >

                                                            <div className="notification-dot"></div>

                                                            <div>

                                                                <strong>
                                                                    {
                                                                        notification.notificationType
                                                                    }
                                                                </strong>

                                                                <p>
                                                                    {
                                                                        notification.message
                                                                    }
                                                                </p>

                                                                {notification.createdAt && (
                                                                    <small>
                                                                        {new Date(
                                                                            notification.createdAt
                                                                        ).toLocaleString()}
                                                                    </small>
                                                                )}

                                                            </div>

                                                        </div>
                                                    )
                                                )
                                        )}

                                    </div>

                                </div>
                            )}

                        </div>


                        <div className="topbar-divider"></div>


                        {/* USER */}

                        <div className="topbar-user">

                            <div className="topbar-avatar">
                                {getUserInitial()}
                            </div>

                            <div className="topbar-user-details">

                                <strong>
                                    {getUserName()}
                                </strong>

                                <span>
                                    {getRoleName()}
                                </span>

                            </div>

                            <span className="topbar-chevron">
                                ⌄
                            </span>

                        </div>

                    </div>

                </header>


                {/* =================================================
                    PAGE CONTENT
                ================================================= */}

                <main className="content">

                    {/* =================================================
                        WELCOME
                    ================================================= */}

                    <section className="welcome-section">

                        <div>

                            <div className="page-label">
                                DASHBOARD
                            </div>

                            <h1>
                                Good morning,{" "}
                                {getUserName()}{" "}
                                <span>
                                    👋
                                </span>
                            </h1>

                            <p>
                                Here's what's
                                happening with your
                                shipments today.
                            </p>

                        </div>


                        {user?.role ===
                            "BUSINESS_CLIENT" && (
                            <button
                                className="create-button"
                                onClick={() => {
                                    setShowShipmentForm(
                                        true
                                    );

                                    setShipmentError(
                                        ""
                                    );
                                }}
                            >
                                <span>
                                    ＋
                                </span>

                                Create Shipment
                            </button>
                        )}

                    </section>


                    {/* =================================================
                        ERROR
                    ================================================= */}

                    {error && (
                        <div className="error-message">

                            <span>
                                !
                            </span>

                            {error}

                        </div>
                    )}


                    {/* =================================================
                        STATISTICS
                    ================================================= */}

                    <section className="statistics-grid">

                        <div className="stat-card">

                            <div className="stat-icon blue">
                                📦
                            </div>

                            <div className="stat-text">

                                <span>
                                    TOTAL SHIPMENTS
                                </span>

                                <strong>
                                    {loading
                                        ? "..."
                                        : totalShipments}
                                </strong>

                                <small>
                                    All shipments
                                </small>

                            </div>

                        </div>


                        <div className="stat-card">

                            <div className="stat-icon orange">
                                🚚
                            </div>

                            <div className="stat-text">

                                <span>
                                    IN TRANSIT
                                </span>

                                <strong>
                                    {loading
                                        ? "..."
                                        : inTransit}
                                </strong>

                                <small>
                                    Currently moving
                                </small>

                            </div>

                        </div>


                        <div className="stat-card">

                            <div className="stat-icon green">
                                ✓
                            </div>

                            <div className="stat-text">

                                <span>
                                    DELIVERED
                                </span>

                                <strong>
                                    {loading
                                        ? "..."
                                        : delivered}
                                </strong>

                                <small>
                                    Successfully delivered
                                </small>

                            </div>

                        </div>


                        <div className="stat-card">

                            <div className="stat-icon purple">
                                ◷
                            </div>

                            <div className="stat-text">

                                <span>
                                    PENDING
                                </span>

                                <strong>
                                    {loading
                                        ? "..."
                                        : pending}
                                </strong>

                                <small>
                                    Awaiting delivery
                                </small>

                            </div>

                        </div>

                    </section>


                    {/* =================================================
                        CREATE SHIPMENT FORM
                    ================================================= */}

                    {showShipmentForm && (
                        <section className="create-panel">

                            <div className="create-panel-header">

                                <div>

                                    <span>
                                        NEW SHIPMENT
                                    </span>

                                    <h2>
                                        Create New Shipment
                                    </h2>

                                    <p>
                                        Enter delivery and
                                        package details.
                                    </p>

                                </div>

                                <button
                                    className="close-button"
                                    type="button"
                                    onClick={() => {
                                        setShowShipmentForm(
                                            false
                                        );

                                        setShipmentError(
                                            ""
                                        );
                                    }}
                                >
                                    ×
                                </button>

                            </div>


                            {shipmentError && (
                                <div className="form-error">
                                    {shipmentError}
                                </div>
                            )}


                            <form
                                onSubmit={
                                    handleCreateShipment
                                }
                            >

                                <div className="form-section">

                                    <h3>
                                        Shipment Information
                                    </h3>

                                    <div className="form-grid">

                                        <div className="field">
                                            <label>
                                                Sender
                                            </label>

                                            <input
                                                type="text"
                                                name="sender"
                                                value={
                                                    shipmentForm.sender
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                                placeholder="Enter sender name"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Receiver
                                            </label>

                                            <input
                                                type="text"
                                                name="receiver"
                                                value={
                                                    shipmentForm.receiver
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                                placeholder="Enter receiver name"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Origin
                                            </label>

                                            <input
                                                type="text"
                                                name="origin"
                                                value={
                                                    shipmentForm.origin
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                                placeholder="Enter origin"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Destination
                                            </label>

                                            <input
                                                type="text"
                                                name="destination"
                                                value={
                                                    shipmentForm.destination
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                                placeholder="Enter destination"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Current Location
                                            </label>

                                            <input
                                                type="text"
                                                name="currentLocation"
                                                value={
                                                    shipmentForm.currentLocation
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                                placeholder="Current location"
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Estimated Delivery
                                            </label>

                                            <input
                                                type="datetime-local"
                                                name="estimatedDelivery"
                                                value={
                                                    shipmentForm.estimatedDelivery
                                                }
                                                onChange={
                                                    handleShipmentChange
                                                }
                                            />
                                        </div>

                                    </div>

                                </div>


                                <div className="form-section">

                                    <h3>
                                        Package Information
                                    </h3>

                                    <div className="form-grid package-grid">

                                        <div className="field">
                                            <label>
                                                Description
                                            </label>

                                            <input
                                                type="text"
                                                name="description"
                                                value={
                                                    packageForm.description
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                placeholder="e.g. Electronics"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Weight (kg)
                                            </label>

                                            <input
                                                type="number"
                                                name="weightKg"
                                                value={
                                                    packageForm.weightKg
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="0"
                                                step="0.01"
                                                placeholder="2.5"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Length (cm)
                                            </label>

                                            <input
                                                type="number"
                                                name="lengthCm"
                                                value={
                                                    packageForm.lengthCm
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="0"
                                                step="0.01"
                                                placeholder="30"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Width (cm)
                                            </label>

                                            <input
                                                type="number"
                                                name="widthCm"
                                                value={
                                                    packageForm.widthCm
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="0"
                                                step="0.01"
                                                placeholder="20"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Height (cm)
                                            </label>

                                            <input
                                                type="number"
                                                name="heightCm"
                                                value={
                                                    packageForm.heightCm
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="0"
                                                step="0.01"
                                                placeholder="10"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Quantity
                                            </label>

                                            <input
                                                type="number"
                                                name="quantity"
                                                value={
                                                    packageForm.quantity
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="1"
                                                required
                                            />
                                        </div>

                                        <div className="field">
                                            <label>
                                                Declared Value
                                            </label>

                                            <input
                                                type="number"
                                                name="declaredValue"
                                                value={
                                                    packageForm.declaredValue
                                                }
                                                onChange={
                                                    handlePackageChange
                                                }
                                                min="0"
                                                step="0.01"
                                                placeholder="15000"
                                                required
                                            />
                                        </div>

                                    </div>


                                    <label className="fragile-checkbox">

                                        <input
                                            type="checkbox"
                                            name="fragile"
                                            checked={
                                                packageForm.fragile
                                            }
                                            onChange={
                                                handlePackageChange
                                            }
                                        />

                                        <span>
                                            Fragile package
                                        </span>

                                    </label>

                                </div>


                                <div className="form-buttons">

                                    <button
                                        type="button"
                                        className="cancel-button"
                                        onClick={() => {
                                            setShowShipmentForm(
                                                false
                                            );

                                            setShipmentError(
                                                ""
                                            );
                                        }}
                                        disabled={
                                            creatingShipment
                                        }
                                    >
                                        Cancel
                                    </button>

                                    <button
                                        type="submit"
                                        className="submit-button"
                                        disabled={
                                            creatingShipment
                                        }
                                    >
                                        {creatingShipment
                                            ? "Creating..."
                                            : "Create Shipment"}
                                    </button>

                                </div>

                            </form>

                        </section>
                    )}


                    {/* =================================================
                        SHIPMENT OVERVIEW + RECENT SHIPMENTS
                    ================================================= */}

                    <section className="dashboard-two-column">


                        
{/* =================================================
    SHIPMENT OVERVIEW
================================================= */}

<div className="overview-card">

    <div className="card-header">

        <div>
            <h2>Shipment Overview</h2>

            <p>
                Current status of your shipments.
            </p>
        </div>

        <button
            onClick={() =>
                navigate("/shipments")
            }
        >
            View all →
        </button>

    </div>


    <div className="shipment-overview-content">

        {/* OVERALL SUMMARY */}

        <div className="overview-main">

            <div className="overview-circle">

                <div className="circle-inner">
                    <strong>
                        {totalShipments}
                    </strong>

                    <span>
                        Shipments
                    </span>
                </div>

            </div>

            <div className="overview-main-text">

                <strong>
                    Shipment Status
                </strong>

                <p>
                    Here's the current distribution
                    of your shipments.
                </p>

                <div className="delivery-rate">

                    <span>
                        Delivery rate
                    </span>

                    <strong>
                        {getPercentage(
                            delivered
                        )}%
                    </strong>

                </div>

            </div>

        </div>


        {/* STATUS GRID */}

        <div className="shipment-status-grid">

            {/* DELIVERED */}

            <div className="shipment-status-card delivered-status">

                <div className="status-card-icon">
                    ✓
                </div>

                <div>
                    <span>
                        Delivered
                    </span>

                    <strong>
                        {delivered}
                    </strong>
                </div>

            </div>


            {/* IN TRANSIT */}

            <div className="shipment-status-card transit-status">

                <div className="status-card-icon">
                    🚚
                </div>

                <div>
                    <span>
                        In Transit
                    </span>

                    <strong>
                        {inTransit}
                    </strong>
                </div>

            </div>


            {/* PENDING */}

            <div className="shipment-status-card pending-status">

                <div className="status-card-icon">
                    ◷
                </div>

                <div>
                    <span>
                        Pending
                    </span>

                    <strong>
                        {pending}
                    </strong>
                </div>

            </div>


            {/* OTHER */}

            <div className="shipment-status-card other-status">

                <div className="status-card-icon">
                    •
                </div>

                <div>
                    <span>
                        Other
                    </span>

                    <strong>
                        {Math.max(
                            0,
                            totalShipments -
                            delivered -
                            inTransit -
                            pending
                        )}
                    </strong>
                </div>

            </div>

        </div>

    </div>

</div>




                        {/* RECENT SHIPMENTS */}

                        <div className="recent-card">

                            <div className="card-header">

                                <div>

                                    <h2>
                                        Recent Shipments
                                    </h2>

                                    <p>
                                        Latest shipment
                                        activity.
                                    </p>

                                </div>

                                <button
                                    onClick={() =>
                                        navigate(
                                            "/shipments"
                                        )
                                    }
                                >
                                    View all →
                                </button>

                            </div>


                            <div className="recent-list">

                                {recentShipments.length ===
                                0 ? (
                                    <div className="empty-recent">

                                        <div>
                                            📦
                                        </div>

                                        <strong>
                                            No shipments yet
                                        </strong>

                                        <span>
                                            Your recent
                                            shipments will
                                            appear here.
                                        </span>

                                    </div>
                                ) : (
                                    recentShipments.map(
                                        (
                                            shipment
                                        ) => (
                                            <div
                                                className="recent-shipment"
                                                key={
                                                    shipment.id
                                                }
                                                onClick={() =>
                                                    navigate(
                                                        `/shipments/${shipment.id}`
                                                    )
                                                }
                                            >

                                                <div className="shipment-icon">
                                                    📦
                                                </div>

                                                <div className="shipment-info">

                                                    <strong>
                                                        {getTrackingId(
                                                            shipment
                                                        )}
                                                    </strong>

                                                    <span>
                                                        {
                                                            getOrigin(
                                                                shipment
                                                            )
                                                        }
                                                        {" "}
                                                        →
                                                        {" "}
                                                        {
                                                            getDestination(
                                                                shipment
                                                            )
                                                        }
                                                    </span>

                                                </div>

                                                <span
                                                    className={
                                                        "status-pill " +
                                                        getStatusClass(
                                                            shipment.status
                                                        )
                                                    }
                                                >
                                                    {
                                                        formatStatus(
                                                            shipment.status
                                                        )
                                                    }
                                                </span>

                                            </div>
                                        )
                                    )
                                )}

                            </div>

                        </div>

                    </section>


                    

                </main>

            </div>

        </div>
    );
}

export default Dashboard;