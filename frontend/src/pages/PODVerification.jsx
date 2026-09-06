import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    getPendingPODs,
    getProofOfDelivery,
    verifyProofOfDelivery,
} from "../services/podService";

import "./PODVerification.css";

const API_BASE_URL = "http://localhost:8080";

function PODVerification() {

    const navigate = useNavigate();

    const [pendingPODs, setPendingPODs] = useState([]);
    const [selectedPOD, setSelectedPOD] = useState(null);

    const [loading, setLoading] = useState(true);
    const [detailLoading, setDetailLoading] = useState(false);

    const [error, setError] = useState("");
    const [actionLoading, setActionLoading] = useState(false);

    const [rejectionReason, setRejectionReason] = useState("");

    // ========================================
    // LOAD PENDING PODs
    // ========================================

    const loadPendingPODs = async () => {

        try {

            setLoading(true);
            setError("");

            const data = await getPendingPODs();

            console.log("Pending PODs:", data);

            setPendingPODs(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {

            console.error(
                "Failed to load pending PODs:",
                err
            );

            setError(
                err.message ||
                "Failed to load pending Proofs of Delivery."
            );

        } finally {

            setLoading(false);

        }
    };


    // ========================================
    // OPEN POD DETAILS
    // ========================================

    const handleOpenPOD = async (pod) => {

        try {

            setDetailLoading(true);
            setError("");

            /*
             * The pending response may already contain
             * complete POD information.
             *
             * If shipmentId is available, fetch the
             * complete POD again.
             */

            const shipmentId =
                pod.shipmentId ||
                pod.shipment?.id;

            if (shipmentId) {

                const completePOD =
                    await getProofOfDelivery(
                        shipmentId
                    );

                console.log(
                    "Complete POD:",
                    completePOD
                );

                setSelectedPOD(
                    completePOD
                );

            } else {

                setSelectedPOD(pod);

            }

        } catch (err) {

            console.error(
                "Failed to load POD details:",
                err
            );

            /*
             * Even if the detail API fails,
             * display the information returned
             * by the pending endpoint.
             */

            setSelectedPOD(pod);

            setError(
                err.message ||
                "Failed to load POD details."
            );

        } finally {

            setDetailLoading(false);

        }
    };


    // ========================================
    // CLOSE DETAIL
    // ========================================

    const handleCloseDetails = () => {

        setSelectedPOD(null);
        setRejectionReason("");
        setError("");

    };


    // ========================================
    // GET SHIPMENT ID
    // ========================================

    const getShipmentId = (pod) => {

        return (
            pod?.shipmentId ||
            pod?.shipment?.id ||
            pod?.shipment?.shipmentId
        );

    };


    // ========================================
    // APPROVE POD
    // ========================================

    const handleApprove = async () => {

        if (!selectedPOD) {
            return;
        }

        const shipmentId =
            getShipmentId(selectedPOD);

        if (!shipmentId) {

            setError(
                "Shipment ID not found for this POD."
            );

            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to approve this Proof of Delivery?"
            );

        if (!confirmed) {
            return;
        }

        try {

            setActionLoading(true);
            setError("");

            await verifyProofOfDelivery(
                shipmentId,
                true,
                ""
            );

            alert(
                "Proof of Delivery approved successfully."
            );

            setSelectedPOD(null);

            setRejectionReason("");

            await loadPendingPODs();

        } catch (err) {

            console.error(
                "Approve POD error:",
                err
            );

            setError(
                err.message ||
                "Failed to approve Proof of Delivery."
            );

        } finally {

            setActionLoading(false);

        }
    };


    // ========================================
    // REJECT POD
    // ========================================

    const handleReject = async () => {

        if (!selectedPOD) {
            return;
        }

        const shipmentId =
            getShipmentId(selectedPOD);

        if (!shipmentId) {

            setError(
                "Shipment ID not found for this POD."
            );

            return;
        }

        if (!rejectionReason.trim()) {

            setError(
                "Please enter a rejection reason."
            );

            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to reject this Proof of Delivery?"
            );

        if (!confirmed) {
            return;
        }

        try {

            setActionLoading(true);
            setError("");

            await verifyProofOfDelivery(
                shipmentId,
                false,
                rejectionReason.trim()
            );

            alert(
                "Proof of Delivery rejected."
            );

            setSelectedPOD(null);

            setRejectionReason("");

            await loadPendingPODs();

        } catch (err) {

            console.error(
                "Reject POD error:",
                err
            );

            setError(
                err.message ||
                "Failed to reject Proof of Delivery."
            );

        } finally {

            setActionLoading(false);

        }
    };


    // ========================================
    // INITIAL LOAD
    // ========================================

    useEffect(() => {

        loadPendingPODs();

    }, []);


    // ========================================
    // IMAGE URL
    // ========================================

    const getFileUrl = (fileUrl) => {

        if (!fileUrl) {
            return null;
        }

        if (
            fileUrl.startsWith("http://") ||
            fileUrl.startsWith("https://")
        ) {
            return fileUrl;
        }

        return `${API_BASE_URL}${fileUrl.startsWith("/") ? "" : "/"}${fileUrl}`;

    };


    // ========================================
    // FIND SIGNATURE
    // ========================================

    const getSignatureUrl = (pod) => {

        return (
            pod?.signatureUrl ||
            pod?.signaturePath ||
            pod?.signature ||
            pod?.recipientSignature ||
            null
        );

    };


    // ========================================
    // FIND PHOTO
    // ========================================

    const getPhotoUrl = (pod) => {

        return (
            pod?.photoUrl ||
            pod?.photoPath ||
            pod?.photo ||
            pod?.deliveryPhoto ||
            null
        );

    };


    // ========================================
    // LOADING
    // ========================================

    if (loading) {

        return (
            <div className="pod-verification-page">

                <div className="pod-verification-container">

                    <h1>
                        POD Verification Queue
                    </h1>

                    <p>
                        Loading pending Proofs of Delivery...
                    </p>

                </div>

            </div>
        );
    }


    // ========================================
    // RENDER
    // ========================================

    return (

        <div className="pod-verification-page">

            <div className="pod-verification-container">

                {/* HEADER */}

                <div className="pod-page-header">

                    <div>

                        <button
                            className="pod-back-button"
                            onClick={() =>
                                navigate("/dashboard")
                            }
                        >
                            ← Dashboard
                        </button>

                        <h1>
                            Proof of Delivery Verification
                        </h1>

                        <p>
                            Review and verify pending delivery confirmations.
                        </p>

                    </div>

                    <button
                        className="pod-refresh-button"
                        onClick={loadPendingPODs}
                        disabled={loading}
                    >
                        Refresh
                    </button>

                </div>


                {/* ERROR */}

                {error && (

                    <div className="pod-error">
                        {error}
                    </div>

                )}


                {/* QUEUE */}

                {!selectedPOD && (

                    <div className="pod-queue-card">

                        <div className="pod-queue-header">

                            <div>

                                <h2>
                                    Verification Queue
                                </h2>

                                <p>
                                    Proofs waiting for Support/Admin verification
                                </p>

                            </div>

                            <span className="pod-count">
                                {pendingPODs.length}
                            </span>

                        </div>


                        {pendingPODs.length === 0 ? (

                            <div className="pod-empty">

                                <h3>
                                    No Pending Proofs
                                </h3>

                                <p>
                                    There are currently no Proofs of Delivery
                                    waiting for verification.
                                </p>

                            </div>

                        ) : (

                            <div className="pod-list">

                                {pendingPODs.map(
                                    (pod, index) => {

                                        const shipmentId =
                                            getShipmentId(pod);

                                        return (

                                            <div
                                                className="pod-list-item"
                                                key={
                                                    pod.id ||
                                                    shipmentId ||
                                                    index
                                                }
                                            >

                                                <div className="pod-list-info">

                                                    <h3>
                                                        Shipment #
                                                        {shipmentId || "N/A"}
                                                    </h3>

                                                    <p>
                                                        <strong>
                                                            Delivered To:
                                                        </strong>{" "}
                                                        {
                                                            pod.deliveredTo ||
                                                            pod.recipientName ||
                                                            "Not available"
                                                        }
                                                    </p>

                                                    <p>
                                                        <strong>
                                                            Delivered At:
                                                        </strong>{" "}
                                                        {
                                                            pod.deliveredAt
                                                                ? new Date(
                                                                    pod.deliveredAt
                                                                ).toLocaleString()
                                                                : "Not available"
                                                        }
                                                    </p>

                                                    <span className="pod-pending-badge">
                                                        PENDING VERIFICATION
                                                    </span>

                                                </div>


                                                <button
                                                    className="pod-view-button"
                                                    onClick={() =>
                                                        handleOpenPOD(
                                                            pod
                                                        )
                                                    }
                                                >
                                                    View Details
                                                </button>

                                            </div>

                                        );

                                    }
                                )}

                            </div>

                        )}

                    </div>

                )}


                {/* DETAIL VIEW */}

                {selectedPOD && (

                    <div className="pod-detail-card">

                        <div className="pod-detail-header">

                            <div>

                                <button
                                    className="pod-back-button"
                                    onClick={
                                        handleCloseDetails
                                    }
                                >
                                    ← Back to Queue
                                </button>

                                <h2>
                                    Proof of Delivery Details
                                </h2>

                            </div>

                            <span className="pod-pending-badge">
                                PENDING VERIFICATION
                            </span>

                        </div>


                        {/* DETAILS */}

                        <div className="pod-info-grid">

                            <div className="pod-info-item">

                                <span>
                                    Shipment ID
                                </span>

                                <strong>
                                    {
                                        getShipmentId(
                                            selectedPOD
                                        ) || "N/A"
                                    }
                                </strong>

                            </div>


                            <div className="pod-info-item">

                                <span>
                                    Delivered To
                                </span>

                                <strong>
                                    {
                                        selectedPOD.deliveredTo ||
                                        selectedPOD.recipientName ||
                                        "Not available"
                                    }
                                </strong>

                            </div>


                            <div className="pod-info-item">

                                <span>
                                    Delivery Notes
                                </span>

                                <strong>
                                    {
                                        selectedPOD.deliveryNotes ||
                                        "No notes provided"
                                    }
                                </strong>

                            </div>


                            <div className="pod-info-item">

                                <span>
                                    Delivered At
                                </span>

                                <strong>
                                    {
                                        selectedPOD.deliveredAt
                                            ? new Date(
                                                selectedPOD.deliveredAt
                                            ).toLocaleString()
                                            : "Not available"
                                    }
                                </strong>

                            </div>

                        </div>


                        {/* SIGNATURE + PHOTO */}

                        <div className="pod-files-section">

                            <div className="pod-file-card">

                                <h3>
                                    Recipient Signature
                                </h3>

                                {detailLoading ? (

                                    <p>
                                        Loading signature...
                                    </p>

                                ) : getSignatureUrl(
                                    selectedPOD
                                ) ? (

                                    <img
                                        className="pod-signature-image"
                                        src={
                                            getFileUrl(
                                                getSignatureUrl(
                                                    selectedPOD
                                                )
                                            )
                                        }
                                        alt="Recipient Signature"
                                    />

                                ) : (

                                    <div className="pod-file-empty">
                                        Signature not available
                                    </div>

                                )}

                            </div>


                            <div className="pod-file-card">

                                <h3>
                                    Delivery Photo
                                </h3>

                                {detailLoading ? (

                                    <p>
                                        Loading delivery photo...
                                    </p>

                                ) : getPhotoUrl(
                                    selectedPOD
                                ) ? (

                                    <img
                                        className="pod-delivery-image"
                                        src={
                                            getFileUrl(
                                                getPhotoUrl(
                                                    selectedPOD
                                                )
                                            )
                                        }
                                        alt="Delivery Proof"
                                    />

                                ) : (

                                    <div className="pod-file-empty">
                                        Delivery photo not available
                                    </div>

                                )}

                            </div>

                        </div>


                        {/* REJECTION REASON */}

                        <div className="pod-rejection-section">

                            <label>
                                Rejection Reason
                            </label>

                            <textarea
                                value={
                                    rejectionReason
                                }
                                onChange={(event) =>
                                    setRejectionReason(
                                        event.target.value
                                    )
                                }
                                placeholder="Enter reason only if rejecting the POD..."
                                rows="4"
                            />

                        </div>


                        {/* ACTIONS */}

                        <div className="pod-verification-actions">

                            <button
                                className="pod-approve-button"
                                onClick={
                                    handleApprove
                                }
                                disabled={
                                    actionLoading
                                }
                            >
                                {actionLoading
                                    ? "Processing..."
                                    : "✓ Approve POD"
                                }
                            </button>


                            <button
                                className="pod-reject-button"
                                onClick={
                                    handleReject
                                }
                                disabled={
                                    actionLoading
                                }
                            >
                                {actionLoading
                                    ? "Processing..."
                                    : "✕ Reject POD"
                                }
                            </button>

                        </div>

                    </div>

                )}

            </div>

        </div>
    );
}

export default PODVerification;