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

function Dashboard() {

    const navigate = useNavigate();

    // ========================================
    // USER
    // ========================================

    const [user, setUser] = useState(null);

    // ========================================
    // SHIPMENTS
    // ========================================

    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // ========================================
    // CREATE SHIPMENT
    // ========================================

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

    // ========================================
    // PACKAGE
    // ========================================

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

    }, [navigate]);

    // ========================================
    // LOAD SHIPMENTS
    // ========================================

    const loadShipments = async () => {

        try {

            setLoading(true);
            setError("");

            const data = await getShipments();

            console.log(
                "Shipments received:",
                data
            );

            setShipments(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (error) {

            console.error(
                "Shipment fetch error:",
                error
            );

            setError(
                error.message ||
                "Failed to load shipments"
            );

        } finally {

            setLoading(false);

        }

    };

    // ========================================
    // SHIPMENT FORM CHANGE
    // ========================================

    const handleShipmentChange = (event) => {

        const {
            name,
            value
        } = event.target;

        setShipmentForm(previous => ({
            ...previous,
            [name]: value,
        }));

    };

    // ========================================
    // PACKAGE FORM CHANGE
    // ========================================

    const handlePackageChange = (event) => {

        const {
            name,
            value,
            type,
            checked
        } = event.target;

        setPackageForm(previous => ({
            ...previous,
            [name]:
                type === "checkbox"
                    ? checked
                    : value,
        }));

    };

    // ========================================
    // CREATE SHIPMENT + PACKAGE
    // ========================================

    const handleCreateShipment = async (event) => {

        event.preventDefault();

        try {

            setCreatingShipment(true);
            setShipmentError("");

            // ========================================
            // SHIPMENT DATA
            // ========================================

            const shipmentData = {

                sender:
                shipmentForm.sender,

                receiver:
                shipmentForm.receiver,

                origin:
                shipmentForm.origin,

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

            // ========================================
            // CREATE SHIPMENT
            // ========================================

            const createdShipment =
                await createShipment(
                    shipmentData
                );

            console.log(
                "Shipment created:",
                createdShipment
            );

            // ========================================
            // CHECK SHIPMENT ID
            // ========================================

            if (!createdShipment?.id) {

                throw new Error(
                    "Shipment was created but no shipment ID was returned."
                );

            }

            // ========================================
            // PACKAGE DATA
            // ========================================

            const packageData = {

                shipmentId:
                createdShipment.id,

                description:
                packageForm.description,

                weightKg:
                    Number(
                        packageForm.weightKg
                    ),

                lengthCm:
                    Number(
                        packageForm.lengthCm
                    ),

                widthCm:
                    Number(
                        packageForm.widthCm
                    ),

                heightCm:
                    Number(
                        packageForm.heightCm
                    ),

                quantity:
                    Number(
                        packageForm.quantity
                    ),

                declaredValue:
                    Number(
                        packageForm.declaredValue
                    ),

                fragile:
                packageForm.fragile,
            };

            console.log(
                "Creating package:",
                packageData
            );

            // ========================================
            // CREATE PACKAGE
            // ========================================

            await createPackage(
                packageData
            );

            // ========================================
            // UPDATE STATISTICS
            // ========================================

            setShipments(previous => [
                createdShipment,
                ...previous,
            ]);

            // ========================================
            // RESET SHIPMENT FORM
            // ========================================

            setShipmentForm({
                sender: "",
                receiver: "",
                origin: "",
                destination: "",
                currentLocation: "",
                estimatedDelivery: "",
            });

            // ========================================
            // RESET PACKAGE FORM
            // ========================================

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

        } catch (error) {

            console.error(
                "Create shipment/package error:",
                error
            );

            setShipmentError(
                error.message ||
                "Failed to create shipment"
            );

        } finally {

            setCreatingShipment(false);

        }

    };

    // ========================================
    // LOGOUT
    // ========================================

    const handleLogout = () => {

        logoutUser();

        navigate("/login");

    };

    // ========================================
    // STATISTICS
    // ========================================

    const totalShipments =
        shipments.length;

    const inTransit =
        shipments.filter(
            shipment =>
                shipment.status === "IN_TRANSIT" ||
                shipment.status === "OUT_FOR_DELIVERY"
        ).length;

    const delivered =
        shipments.filter(
            shipment =>
                shipment.status === "DELIVERED"
        ).length;

    const pending =
        shipments.filter(
            shipment =>
                shipment.status === "CREATED" ||
                shipment.status === "PICKED_UP"
        ).length;

    // ========================================
    // RENDER
    // ========================================

    return (

        <div className="dashboard">

            {/* =================================
                HEADER
            ================================= */}

            <div className="dashboard-header">

                <div>

                    <h1>
                        ShipTrack Pro
                    </h1>

                    <p>
                        Shipment Tracking Dashboard
                    </p>

                </div>

                <div className="user-section">

                    <span>

                        Welcome,{" "}

                        {
                            user?.fullName ||
                            user?.name ||
                            user?.email ||
                            "User"
                        }

                    </span>

                    <button
                        onClick={
                            handleLogout
                        }
                    >
                        Logout
                    </button>

                </div>

            </div>

            {/* =================================
                DASHBOARD CONTENT
            ================================= */}

            {(
                user?.role === "CUSTOMER" ||
                user?.role === "BUSINESS_CLIENT" ||
                user?.role === "ADMINISTRATOR" ||
                user?.role === "LOGISTICS_OPERATOR"
            ) && (

                <>

                    {/* GENERAL ERROR */}

                    {error && (

                        <div className="error-message">
                            {error}
                        </div>

                    )}

                    {/* =================================
                        STATISTICS
                    ================================= */}

                    <div className="stats-container">

                        <div className="stat-card">

                            <h3>
                                Total Shipments
                            </h3>

                            <p>
                                {
                                    loading
                                        ? "..."
                                        : totalShipments
                                }
                            </p>

                        </div>

                        <div className="stat-card">

                            <h3>
                                In Transit
                            </h3>

                            <p>
                                {
                                    loading
                                        ? "..."
                                        : inTransit
                                }
                            </p>

                        </div>

                        <div className="stat-card">

                            <h3>
                                Delivered
                            </h3>

                            <p>
                                {
                                    loading
                                        ? "..."
                                        : delivered
                                }
                            </p>

                        </div>

                        <div className="stat-card">

                            <h3>
                                Pending
                            </h3>

                            <p>
                                {
                                    loading
                                        ? "..."
                                        : pending
                                }
                            </p>

                        </div>

                    </div>

                    {/* =================================
                        SHIPMENT ACTIONS
                    ================================= */}

                    <div className="create-shipment-header">

                        <h2>
                            Shipment Overview
                        </h2>

                        <button
                            onClick={() =>
                                navigate(
                                    "/shipments"
                                )
                            }
                        >
                            View My Shipments
                        </button>

                        {user?.role ===
                            "BUSINESS_CLIENT" && (

                                <button
                                    onClick={() => {

                                        setShowShipmentForm(
                                            true
                                        );

                                        setShipmentError(
                                            ""
                                        );

                                    }}
                                >
                                    + Create Shipment
                                </button>

                            )}

                    </div>

                    {/* =================================
                        CREATE SHIPMENT FORM
                    ================================= */}

                    {user?.role ===
                        "BUSINESS_CLIENT" &&
                        showShipmentForm && (

                            <div className="create-shipment-form">

                                <h2>
                                    Create New Shipment
                                </h2>

                                {shipmentError && (

                                    <div className="error-message">
                                        {shipmentError}
                                    </div>

                                )}

                                <form
                                    onSubmit={
                                        handleCreateShipment
                                    }
                                >

                                    {/* SENDER */}

                                    <div>

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

                                    {/* RECEIVER */}

                                    <div>

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

                                    {/* ORIGIN */}

                                    <div>

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

                                    {/* DESTINATION */}

                                    <div>

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

                                    {/* CURRENT LOCATION */}

                                    <div>

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
                                            placeholder="Enter current location"
                                        />

                                    </div>

                                    {/* ESTIMATED DELIVERY */}

                                    <div>

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

                                    {/* =================================
                                        PACKAGE DETAILS
                                    ================================= */}

                                    <div className="package-section">

                                        <h3>
                                            Package Details
                                        </h3>

                                        {/* DESCRIPTION */}

                                        <div>

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

                                        {/* WEIGHT */}

                                        <div>

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
                                                placeholder="e.g. 2.5"
                                                min="0"
                                                step="0.01"
                                                required
                                            />

                                        </div>

                                        {/* LENGTH */}

                                        <div>

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
                                                placeholder="e.g. 30"
                                                min="0"
                                                step="0.01"
                                                required
                                            />

                                        </div>

                                        {/* WIDTH */}

                                        <div>

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
                                                placeholder="e.g. 20"
                                                min="0"
                                                step="0.01"
                                                required
                                            />

                                        </div>

                                        {/* HEIGHT */}

                                        <div>

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
                                                placeholder="e.g. 10"
                                                min="0"
                                                step="0.01"
                                                required
                                            />

                                        </div>

                                        {/* QUANTITY */}

                                        <div>

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

                                        {/* DECLARED VALUE */}

                                        <div>

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
                                                placeholder="e.g. 15000"
                                                min="0"
                                                step="0.01"
                                                required
                                            />

                                        </div>

                                        {/* FRAGILE */}

                                        <div>

                                            <label>

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

                                                {" "}
                                                Fragile

                                            </label>

                                        </div>

                                    </div>

                                    {/* FORM BUTTONS */}

                                    <div className="shipment-form-buttons">

                                        <button
                                            type="submit"
                                            disabled={
                                                creatingShipment
                                            }
                                        >

                                            {
                                                creatingShipment
                                                    ? "Creating..."
                                                    : "Create Shipment"
                                            }

                                        </button>

                                        <button
                                            type="button"
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

                                    </div>

                                </form>

                            </div>

                        )}

                </>

            )}

        </div>

    );
}

export default Dashboard;