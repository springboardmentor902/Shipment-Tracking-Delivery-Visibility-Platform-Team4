const API_URL = "http://localhost:8080/api/notifications";

const getAuthHeaders = () => {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
};

export const getNotifications = async () => {
    const response = await fetch(API_URL, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error("Failed to fetch notifications");
    }

    return response.json();
};

export const markNotificationAsRead = async (id) => {
    const response = await fetch(
        `${API_URL}/${id}/read`,
        {
            method: "PATCH",
            headers: getAuthHeaders(),
        }
    );

    if (!response.ok) {
        throw new Error("Failed to mark notification as read");
    }

    return response.json();
};