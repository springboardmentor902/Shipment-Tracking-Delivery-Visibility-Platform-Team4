import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

import {
    getCurrentUser,
    getShipments,
    createShipment,
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
    // INITIAL LOAD
    // ========================================

    useEffect(() => {

        const currentUser = getCurrentUser();

        if (!currentUser) {
            navigate("/login");
            return;
        }

        setUser(currentUser);

        /*
         * Users who can access shipments.
         */

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
    // CREATE SHIPMENT
    // ========================================

    const handleCreateShipment = async (event) => {

        event.preventDefault();

        try {

            setCreatingShipment(true);
            setShipmentError("");

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
                    shipmentForm.currentLocation || null,

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
                "Shipment created:",
                createdShipment
            );

            setShipments(previous => [
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

            setShowShipmentForm(false);

        } catch (error) {

            console.error(
                "Create shipment error:",
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
                        onClick={handleLogout}
                    >
                        Logout
                    </button>

                </div>

            </div>


            {/* =================================
                SHIPMENT DASHBOARD
            ================================= */}

            {(
                user?.role === "CUSTOMER" ||
                user?.role === "BUSINESS_CLIENT" ||
                user?.role === "ADMINISTRATOR" ||
                user?.role === "LOGISTICS_OPERATOR"
            ) && (

                <>


                    {/* =================================
                        GENERAL ERROR
                    ================================= */}

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
                                {totalShipments}
                            </p>

                        </div>


                        <div className="stat-card">

                            <h3>
                                In Transit
                            </h3>

                            <p>
                                {inTransit}
                            </p>

                        </div>


                        <div className="stat-card">

                            <h3>
                                Delivered
                            </h3>

                            <p>
                                {delivered}
                            </p>

                        </div>


                        <div className="stat-card">

                            <h3>
                                Pending
                            </h3>

                            <p>
                                {pending}
                            </p>

                        </div>

                    </div>


                    {/* =================================
                        SHIPMENT HEADER
                    ================================= */}

                    <div className="create-shipment-header">

                        <h2>
                            My Shipments
                        </h2>


                        {user?.role === "BUSINESS_CLIENT" && (

                            <button
                                onClick={() => {

                                    setShowShipmentForm(true);

                                    setShipmentError("");

                                }}
                            >
                                + Create Shipment
                            </button>

                        )}

                    </div>


                    {/* =================================
                        CREATE SHIPMENT FORM
                    ================================= */}

                    {user?.role === "BUSINESS_CLIENT" &&
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

                                                setShipmentError("");

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


                    {/* =================================
                        SHIPMENTS
                    ================================= */}

                    <div className="shipment-section">

                        {loading ? (

                            <div className="loading">

                                <p>
                                    Loading shipments...
                                </p>

                            </div>

                        ) : error ? (

                            <div className="error-message">

                                {error}

                            </div>

                        ) : shipments.length === 0 ? (

                            <div className="no-shipments">

                                <p>
                                    No shipments found.
                                </p>

                            </div>

                        ) : (

                            <div className="shipment-list">

                                {shipments.map(
                                    shipment => (

                                        <div
                                            className="shipment-card"
                                            key={
                                                shipment.id
                                            }
                                            onClick={() =>
                                                navigate(
                                                    "/shipments/" +
                                                    shipment.id
                                                )
                                            }
                                            style={{
                                                cursor: "pointer"
                                            }}
                                        >

                                            <h3>

                                                Tracking Number:{" "}

                                                <span>
                                                    {
                                                        shipment.trackingNumber
                                                    }
                                                </span>

                                            </h3>


                                            <p>

                                                <strong>
                                                    From:
                                                </strong>{" "}

                                                {
                                                    shipment.origin
                                                }

                                            </p>


                                            <p>

                                                <strong>
                                                    To:
                                                </strong>{" "}

                                                {
                                                    shipment.destination
                                                }

                                            </p>


                                            <p>

                                                <strong>
                                                    Current Location:
                                                </strong>{" "}

                                                {
                                                    shipment.currentLocation ||
                                                    "Not available"
                                                }

                                            </p>


                                            <p>

                                                <strong>
                                                    Status:
                                                </strong>{" "}

                                                <span>
                                                    {
                                                        shipment.status
                                                    }
                                                </span>

                                            </p>


                                            <p>

                                                <strong>
                                                    Sender:
                                                </strong>{" "}

                                                {
                                                    shipment.sender
                                                }

                                            </p>


                                            <p>

                                                <strong>
                                                    Receiver:
                                                </strong>{" "}

                                                {
                                                    shipment.receiver
                                                }

                                            </p>


                                            <p>

                                                <strong>
                                                    Estimated Delivery:
                                                </strong>{" "}

                                                {
                                                    shipment.estimatedDelivery
                                                        ? new Date(
                                                            shipment.estimatedDelivery
                                                        ).toLocaleString()
                                                        : "Not available"
                                                }

                                            </p>

                                        </div>

                                    )
                                )}

                            </div>

                        )}

                    </div>

                </>

            )}

        </div>

    );
}


export default Dashboard;