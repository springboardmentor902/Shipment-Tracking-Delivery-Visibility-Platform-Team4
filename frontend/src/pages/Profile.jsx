
import { useEffect, useState } from "react";
import {
    getMyProfile,
    updateMyProfile,
    changeMyPassword
} from "../services/userService";
import "./Profile.css";

function Profile() {
    const [profile, setProfile] = useState(null);

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [changingPassword, setChangingPassword] = useState(false);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        loadProfile();
    }, []);

    const loadProfile = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getMyProfile();

            setProfile(data);
            setFullName(data.fullName || "");
            setEmail(data.email || "");
            setPhone(data.phone || "");
        } catch (err) {
            setError(err.message || "Failed to load profile");
        } finally {
            setLoading(false);
        }
    };

    const handleProfileUpdate = async (e) => {
        e.preventDefault();

        try {
            setSaving(true);
            setError("");
            setMessage("");

            const updated = await updateMyProfile({
                fullName,
                email,
                phone
            });

            setProfile(updated);

            // Keep local storage user data synchronized if present
            const storedUser = localStorage.getItem("user");

            if (storedUser) {
                const parsedUser = JSON.parse(storedUser);

                localStorage.setItem(
                    "user",
                    JSON.stringify({
                        ...parsedUser,
                        ...updated
                    })
                );
            }

            setMessage("Profile updated successfully.");
        } catch (err) {
            setError(err.message || "Failed to update profile");
        } finally {
            setSaving(false);
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();

        if (!currentPassword || !newPassword || !confirmPassword) {
            setError("Please fill in all password fields.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("New password and confirmation password do not match.");
            return;
        }

        if (newPassword.length < 6) {
            setError("New password must be at least 6 characters.");
            return;
        }

        try {
            setChangingPassword(true);
            setError("");
            setMessage("");

            await changeMyPassword({
                currentPassword,
                newPassword
            });

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            setMessage("Password changed successfully.");
        } catch (err) {
            setError(err.message || "Failed to change password");
        } finally {
            setChangingPassword(false);
        }
    };

    if (loading) {
        return (
            <div className="profile-page">
                <div className="profile-loading">
                    Loading profile...
                </div>
            </div>
        );
    }

    return (
        <div className="profile-page">

            <div className="profile-header">
                <div>
                    <h1>My Profile</h1>
                    <p>View and manage your ShipTrack account.</p>
                </div>
            </div>

            {message && (
                <div className="profile-success">
                    {message}
                </div>
            )}

            {error && (
                <div className="profile-error">
                    {error}
                    <button onClick={() => setError("")}>×</button>
                </div>
            )}

            <div className="profile-grid">

                {/* Profile Information */}
                <div className="profile-card">

                    <div className="profile-card-header">
                        <h2>Profile Information</h2>
                        <p>Update your personal information.</p>
                    </div>

                    <form onSubmit={handleProfileUpdate}>

                        <div className="profile-avatar">
                            {fullName
                                ? fullName.charAt(0).toUpperCase()
                                : "U"}
                        </div>

                        <div className="profile-field">
                            <label>Full Name</label>
                            <input
                                type="text"
                                value={fullName}
                                onChange={(e) =>
                                    setFullName(e.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="profile-field">
                            <label>Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) =>
                                    setEmail(e.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="profile-field">
                            <label>Phone</label>
                            <input
                                type="text"
                                value={phone}
                                onChange={(e) =>
                                    setPhone(e.target.value)
                                }
                            />
                        </div>

                        <div className="profile-readonly-grid">

                            <div>
                                <span>Role</span>
                                <strong>
                                    {profile?.role || "N/A"}
                                </strong>
                            </div>

                            <div>
                                <span>Status</span>
                                <strong>
                                    {profile?.status || "N/A"}
                                </strong>
                            </div>

                        </div>

                        <button
                            type="submit"
                            className="profile-save-button"
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : "Save Changes"}
                        </button>

                    </form>
                </div>

                {/* Change Password */}
                <div className="profile-card">

                    <div className="profile-card-header">
                        <h2>Change Password</h2>
                        <p>Keep your account secure.</p>
                    </div>

                    <form onSubmit={handlePasswordChange}>

                        <div className="profile-field">
                            <label>Current Password</label>
                            <input
                                type="password"
                                value={currentPassword}
                                onChange={(e) =>
                                    setCurrentPassword(e.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="profile-field">
                            <label>New Password</label>
                            <input
                                type="password"
                                value={newPassword}
                                onChange={(e) =>
                                    setNewPassword(e.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="profile-field">
                            <label>Confirm New Password</label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={(e) =>
                                    setConfirmPassword(e.target.value)
                                }
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="profile-password-button"
                            disabled={changingPassword}
                        >
                            {changingPassword
                                ? "Changing..."
                                : "Change Password"}
                        </button>

                    </form>
                </div>

            </div>
        </div>
    );
}

export default Profile;

