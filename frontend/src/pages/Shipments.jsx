import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

import {
    getCurrentUser,
    getShipments,
} from "../services/authService";

function Shipments() {

    const navigate = useNavigate();

    const [user, setUser] = useState(null);
    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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

    return (

        <div className="dashboard">

            {/* HEADER */}

            <div className="dashboard-header">

                <div>

                    <h1>
                        My Shipments
                    </h1>

                    <p>
                        View and track your shipments
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
                        onClick={() =>
                            navigate("/dashboard")
                        }
                    >
                        Back to Dashboard
                    </button>

                </div>

            </div>

            {/* SHIPMENT CONTENT */}

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

                        {user?.role === "BUSINESS_CLIENT" && (

                            <button
                                onClick={() =>
                                    navigate("/dashboard")
                                }
                            >
                                Create Shipment
                            </button>

                        )}

                    </div>

                ) : (

                    <div className="shipment-list">

                        {shipments.map(
                            shipment => (

                                <div
                                    className="shipment-card"
                                    key={shipment.id}
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

        </div>

    );
}

export default Shipments;