import { useState } from "react";
import "./AccountSettings.css";

function AccountSettings() {
    const [notifications, setNotifications] = useState(true);
    const [emailAlerts, setEmailAlerts] = useState(true);
    const [smsAlerts, setSmsAlerts] = useState(false);

    return (
        <div className="settings-page">

            <div className="settings-header">
                <h1>⚙️ Account Settings</h1>
                <p>Manage your ShipTrack account preferences.</p>
            </div>

            <div className="settings-grid">

                <div className="settings-card">
                    <h2>🔔 Notification Preferences</h2>
                    <p>Choose how you want to receive shipment updates.</p>

                    <div className="setting-row">
                        <div>
                            <strong>Push Notifications</strong>
                            <span>Receive shipment updates in the app.</span>
                        </div>

                        <label className="switch">
                            <input
                                type="checkbox"
                                checked={notifications}
                                onChange={(e) =>
                                    setNotifications(e.target.checked)
                                }
                            />
                            <span className="slider"></span>
                        </label>
                    </div>

                    <div className="setting-row">
                        <div>
                            <strong>Email Alerts</strong>
                            <span>Receive delivery and ETA alerts by email.</span>
                        </div>

                        <label className="switch">
                            <input
                                type="checkbox"
                                checked={emailAlerts}
                                onChange={(e) =>
                                    setEmailAlerts(e.target.checked)
                                }
                            />
                            <span className="slider"></span>
                        </label>
                    </div>

                    <div className="setting-row">
                        <div>
                            <strong>SMS Alerts</strong>
                            <span>Receive important shipment alerts by SMS.</span>
                        </div>

                        <label className="switch">
                            <input
                                type="checkbox"
                                checked={smsAlerts}
                                onChange={(e) =>
                                    setSmsAlerts(e.target.checked)
                                }
                            />
                            <span className="slider"></span>
                        </label>
                    </div>
                </div>

                <div className="settings-card">
                    <h2>🔐 Account Security</h2>
                    <p>Keep your ShipTrack account secure.</p>

                    <div className="security-item">
                        <span>Password</span>
                        <strong>Protected</strong>
                    </div>

                    <div className="security-item">
                        <span>Authentication</span>
                        <strong>JWT Enabled</strong>
                    </div>

                    <div className="security-item">
                        <span>Account Status</span>
                        <strong className="active-status">ACTIVE</strong>
                    </div>
                </div>

            </div>
        </div>
    );
}

export default AccountSettings;