import { useEffect, useState } from "react";
import {
    getAllUsers,
    updateUserRole,
    activateUser,
    deactivateUser,
    deleteUser
} from "../services/userService";
import "./UserManagement.css";

const ROLES = [
    "CUSTOMER",
    "BUSINESS_CLIENT",
    "LOGISTICS_OPERATOR",
    "SUPPORT_AGENT",
    "ADMINISTRATOR"
];

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [search, setSearch] = useState("");

    const loadUsers = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getAllUsers();
            setUsers(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Failed to load users");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadUsers();
    }, []);

    const showMessage = (text) => {
        setMessage(text);
        setTimeout(() => setMessage(""), 2500);
    };

    const handleRoleChange = async (id, role) => {
        try {
            await updateUserRole(id, role);
            showMessage("User role updated successfully");
            await loadUsers();
        } catch (err) {
            setError(err.message || "Failed to update role");
        }
    };

    const handleActivate = async (id) => {
        try {
            await activateUser(id);
            showMessage("User activated successfully");
            await loadUsers();
        } catch (err) {
            setError(err.message || "Failed to activate user");
        }
    };

    const handleDeactivate = async (id) => {
        const reason = window.prompt(
            "Enter a reason for deactivating this user:"
        );

        if (reason === null) {
            return;
        }

        try {
            await deactivateUser(id);
            showMessage("User deactivated successfully");
            await loadUsers();
        } catch (err) {
            setError(err.message || "Failed to deactivate user");
        }
    };

    const handleDelete = async (id, name) => {
        const confirmed = window.confirm(
            `Are you sure you want to delete ${name || "this user"}?`
        );

        if (!confirmed) {
            return;
        }

        try {
            await deleteUser(id);
            showMessage("User deleted successfully");
            await loadUsers();
        } catch (err) {
            setError(err.message || "Failed to delete user");
        }
    };

    const filteredUsers = users.filter((user) => {
        const value = search.toLowerCase();

        return (
            String(user.id || "").includes(value) ||
            String(user.fullName || "").toLowerCase().includes(value) ||
            String(user.email || "").toLowerCase().includes(value) ||
            String(user.role || "").toLowerCase().includes(value) ||
            String(user.status || "").toLowerCase().includes(value)
        );
    });

    return (
        <div className="user-management">
            <div className="user-management-header">
                <div>
                    <h1>User Management</h1>
                    <p>Manage ShipTrack users, roles and account status.</p>
                </div>

                <button
                    className="refresh-button"
                    onClick={loadUsers}
                >
                    Refresh
                </button>
            </div>

            {message && (
                <div className="success-message">
                    {message}
                </div>
            )}

            {error && (
                <div className="error-message">
                    {error}
                    <button onClick={() => setError("")}>×</button>
                </div>
            )}

            <div className="user-management-toolbar">
                <input
                    type="text"
                    placeholder="Search by name, email, role or status..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />

                <div className="user-count">
                    Total Users: <strong>{users.length}</strong>
                </div>
            </div>

            {loading ? (
                <div className="loading">
                    Loading users...
                </div>
            ) : filteredUsers.length === 0 ? (
                <div className="empty-state">
                    No users found.
                </div>
            ) : (
                <div className="users-table-container">
                    <table className="users-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>User</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Role</th>
                                <th>Status</th>
<th>Last Login</th>
<th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {filteredUsers.map((user) => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>

                                    <td>
                                        <strong>
                                            {user.fullName || "N/A"}
                                        </strong>
                                    </td>

                                    <td>{user.email || "N/A"}</td>

                                    <td>{user.phone || "N/A"}</td>

                                    <td>
                                        <select
                                            value={user.role || ""}
                                            onChange={(e) =>
                                                handleRoleChange(
                                                    user.id,
                                                    e.target.value
                                                )
                                            }
                                        >
                                            {ROLES.map((role) => (
                                                <option
                                                    key={role}
                                                    value={role}
                                                >
                                                    {role}
                                                </option>
                                            ))}
                                        </select>
                                    </td>

                                   <td>
    <span
        className={`status-badge ${String(
            user.status || ""
        ).toLowerCase()}`}
    >
        {user.status || "UNKNOWN"}
    </span>
</td>

<td>
    {user.lastLoginAt
        ? new Date(user.lastLoginAt).toLocaleString()
        : "Never"}
</td>



                                    <td>
                                        <div className="action-buttons">
                                            {String(user.status).toUpperCase() ===
                                            "ACTIVE" ? (
                                                <button
                                                    className="deactivate-button"
                                                    onClick={() =>
                                                        handleDeactivate(
                                                            user.id
                                                        )
                                                    }
                                                >
                                                    Deactivate
                                                </button>
                                            ) : (
                                                <button
                                                    className="activate-button"
                                                    onClick={() =>
                                                        handleActivate(
                                                            user.id
                                                        )
                                                    }
                                                >
                                                    Activate
                                                </button>
                                            )}

                                            <button
                                                className="delete-button"
                                                onClick={() =>
                                                    handleDelete(
                                                        user.id,
                                                        user.fullName
                                                    )
                                                }
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default UserManagement;