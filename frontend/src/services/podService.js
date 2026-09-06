const API_BASE_URL = "http://localhost:8080/api";

// ========================================
// CREATE PROOF OF DELIVERY
// ========================================

export const createProofOfDelivery = async (
    shipmentId,
    deliveredTo,
    deliveryNotes,
    signature,
    photo
) => {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Authentication token not found");
    }

    const formData = new FormData();

    formData.append("deliveredTo", deliveredTo);

    if (deliveryNotes) {
        formData.append("deliveryNotes", deliveryNotes);
    }

    if (signature) {
        formData.append("signature", signature);
    }

    if (photo) {
        formData.append("photo", photo);
    }

    const response = await fetch(
        `${API_BASE_URL}/pod/${shipmentId}`,
        {
            method: "POST",

            headers: {
                Authorization: `Bearer ${token}`,
            },

            body: formData,
        }
    );

    const text = await response.text();

    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            console.log("Create POD response:", text);
        }
    }

    if (!response.ok) {
        throw new Error(
            data.message ||
            data.error ||
            text ||
            "Failed to submit Proof of Delivery"
        );
    }

    return data;
};


// ========================================
// GET PROOF OF DELIVERY
// ========================================

export const getProofOfDelivery = async (
    shipmentId
) => {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Authentication token not found");
    }

    const response = await fetch(
        `${API_BASE_URL}/pod/${shipmentId}`,
        {
            method: "GET",

            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
            },
        }
    );

    const text = await response.text();

    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            console.log("Get POD response:", text);
        }
    }

    if (!response.ok) {
        throw new Error(
            data.message ||
            data.error ||
            text ||
            "Proof of Delivery not found"
        );
    }

    return data;
};


// ========================================
// GET PENDING POD VERIFICATIONS
// ========================================

export const getPendingPODs = async () => {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("You are not logged in.");
    }

    const response = await fetch(
        `${API_BASE_URL}/pod/pending`,
        {
            method: "GET",

            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
            },
        }
    );

    const text = await response.text();

    let data = [];

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            console.error(
                "Pending POD response:",
                text
            );
        }
    }

    if (!response.ok) {
        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to load pending PODs (${response.status})`
        );
    }

    return Array.isArray(data)
        ? data
        : [];
};


// ========================================
// VERIFY / REJECT PROOF OF DELIVERY
// ========================================

export const verifyProofOfDelivery = async (
    shipmentId,
    verified,
    rejectionReason = ""
) => {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("You are not logged in.");
    }

    const response = await fetch(
        `${API_BASE_URL}/pod/${shipmentId}/verify`,
        {
            method: "PATCH",

            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },

            body: JSON.stringify({
                verified: verified,
                rejectionReason: rejectionReason,
            }),
        }
    );

    const text = await response.text();

    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            console.log(
                "Verify POD response:",
                text
            );
        }
    }

    if (!response.ok) {
        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to verify POD (${response.status})`
        );
    }

    return data;
};