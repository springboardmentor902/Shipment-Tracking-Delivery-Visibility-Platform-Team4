const API_BASE_URL = "http://localhost:8080/api";

function getAuthHeaders() {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
}

// Get all users - ADMINISTRATOR
export async function getAllUsers() {
    const response = await fetch(`${API_BASE_URL}/admin/users`, {
        method: "GET",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to fetch users");
    }

    return response.json();
}

// Get user by ID - ADMINISTRATOR
export async function getUserById(id) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
        method: "GET",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to fetch user");
    }

    return response.json();
}

// Update user role - ADMINISTRATOR
export async function updateUserRole(id, role) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/role`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify({ role })
    });

    if (!response.ok) {
        throw new Error("Failed to update user role");
    }

    return response.json();
}

// Update user status - ADMINISTRATOR
export async function updateUserStatus(id, status, reason = "") {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/status`, {
        method: "PATCH",
        headers: getAuthHeaders(),
        body: JSON.stringify({
            status,
            reason
        })
    });

    if (!response.ok) {
        throw new Error("Failed to update user status");
    }

    return response.json();
}

// Activate user
export async function activateUser(id) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/activate`, {
        method: "PUT",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to activate user");
    }

    return response.json();
}

// Deactivate user
export async function deactivateUser(id) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}/deactivate`, {
        method: "PUT",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to deactivate user");
    }

    return response.json();
}

// Delete user
export async function deleteUser(id) {
    const response = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to delete user");
    }

    return true;
}

// Get current user's profile
export async function getMyProfile() {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
        method: "GET",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Failed to fetch profile");
    }

    return response.json();
}

// Update current user's profile
export async function updateMyProfile(data) {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error("Failed to update profile");
    }

    return response.json();
}

// Change current user's password
export async function changeMyPassword(data) {
    const response = await fetch(`${API_BASE_URL}/users/me/password`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error("Failed to change password");
    }

    return response.json();
}