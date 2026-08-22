const API_URL = "http://localhost:8080/api";

// ========================================
// REGISTER USER
// ========================================

export async function registerUser(userData) {

    const response = await fetch(
        `${API_URL}/auth/register`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
            },

            body: JSON.stringify(userData),
        }
    );

    const text = await response.text();

    let data = {};

    if (text) {

        try {
            data = JSON.parse(text);
        } catch {
            console.log("Backend response:", text);
        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Registration failed with status ${response.status}`
        );

    }

    return data;
}


// ========================================
// LOGIN
// ========================================

export async function loginUser(credentials) {

    const response = await fetch(
        `${API_URL}/auth/login`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
            },

            body: JSON.stringify(credentials),
        }
    );

    const text = await response.text();

    let data = {};

    if (text) {

        try {
            data = JSON.parse(text);
        } catch {
            console.log("Backend response:", text);
        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Login failed with status ${response.status}`
        );

    }

    if (!data.token) {

        throw new Error(
            "Login succeeded but no token was returned."
        );

    }

    localStorage.setItem(
        "token",
        data.token
    );

    if (data.user) {

        localStorage.setItem(
            "user",
            JSON.stringify(data.user)
        );

    }

    return data;
}


// ========================================
// GET TOKEN
// ========================================

export function getToken() {

    return localStorage.getItem(
        "token"
    );

}


// ========================================
// GET CURRENT USER
// ========================================

export function getCurrentUser() {

    const user =
        localStorage.getItem("user");

    if (!user) {

        return null;

    }

    try {

        return JSON.parse(user);

    } catch {

        return null;

    }

}


// ========================================
// LOGOUT
// ========================================

export function logoutUser() {

    localStorage.removeItem(
        "token"
    );

    localStorage.removeItem(
        "user"
    );

}


// ========================================
// GET SHIPMENTS
// ========================================

export async function getShipments() {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/shipments`,
        {
            method: "GET",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    const text =
        await response.text();

    let data = [];

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            console.log(
                "Backend response:",
                text
            );

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to fetch shipments (${response.status})`
        );

    }

    return data;

}


// ========================================
// CREATE SHIPMENT
// ========================================

export async function createShipment(
    shipmentData
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/shipments`,
        {
            method: "POST",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    `Bearer ${token}`,
            },

            body: JSON.stringify(
                shipmentData
            ),
        }
    );

    const text =
        await response.text();

    let data = {};

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            console.log(
                "Backend response:",
                text
            );

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to create shipment (${response.status})`
        );

    }

    return data;

}


// ========================================
// GET SHIPMENT BY ID
// ========================================

export async function getShipmentById(
    shipmentId
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/shipments/${shipmentId}`,
        {
            method: "GET",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    const text =
        await response.text();

    let data = {};

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            console.log(
                "Backend response:",
                text
            );

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to fetch shipment (${response.status})`
        );

    }

    return data;

}


// ========================================
// CANCEL SHIPMENT
// ========================================

export async function cancelShipment(
    shipmentId
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/shipments/${shipmentId}`,
        {
            method: "DELETE",

            headers: {
                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    const text =
        await response.text();

    let data = {};

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {
            // DELETE may return empty response
        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to cancel shipment (${response.status})`
        );

    }

    return data;

}


// ========================================
// GET TRACKING EVENTS
// ========================================

export async function getTrackingEvents(
    shipmentId
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/tracking/shipments/${shipmentId}/events`,
        {
            method: "GET",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    const text =
        await response.text();

    let data = [];

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            console.log(
                "Tracking API response:",
                text
            );

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to fetch tracking events (${response.status})`
        );

    }

    return Array.isArray(data)
        ? data
        : [];

}


// ========================================
// CREATE TRACKING EVENT
// ========================================

export async function createTrackingEvent(
    shipmentId,
    eventData
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/tracking/shipments/${shipmentId}/events`,
        {
            method: "POST",

            headers: {
                "Content-Type":
                    "application/json",

                "Authorization":
                    `Bearer ${token}`,
            },

            body: JSON.stringify(
                eventData
            ),
        }
    );

    const text =
        await response.text();

    let data = {};

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            console.log(
                "Tracking API response:",
                text
            );

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to create tracking event (${response.status})`
        );

    }

    return data;

}


// ========================================
// UPDATE SHIPMENT STATUS
// ========================================

export async function updateShipmentStatus(
    shipmentId,
    status
) {

    const token = getToken();

    if (!token) {

        throw new Error(
            "You are not logged in."
        );

    }

    const response = await fetch(
        `${API_URL}/shipments/${shipmentId}/status?status=${encodeURIComponent(status)}`,
        {
            method: "PATCH",

            headers: {
                "Authorization":
                    `Bearer ${token}`,
            },
        }
    );

    const text =
        await response.text();

    let data = {};

    if (text) {

        try {

            data = JSON.parse(text);

        } catch {

            data = {};

        }

    }

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            text ||
            `Failed to update shipment status (${response.status})`
        );

    }

    return data;

}
export async function createPackage(packageData) {

    const token = localStorage.getItem("token");

    const response = await fetch(
        "http://localhost:8080/api/packages",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(packageData),
        }
    );

    if (!response.ok) {

        const errorText = await response.text();

        throw new Error(
            errorText || "Failed to create package"
        );
    }

    return response.json();
}