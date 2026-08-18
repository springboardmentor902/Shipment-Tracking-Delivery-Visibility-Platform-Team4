import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Register.css";

import { registerUser } from "../services/authService";


function Register() {

    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        password: "",
        phone: "",
        role: "CUSTOMER",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");


    // ========================================
    // HANDLE INPUT CHANGE
    // ========================================

    const handleChange = (event) => {

        const {
            name,
            value
        } = event.target;

        setFormData(previous => ({
            ...previous,
            [name]: value,
        }));

    };


    // ========================================
    // HANDLE REGISTRATION
    // ========================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");

        try {

            setLoading(true);

            const response =
                await registerUser(formData);

            console.log(
                "Registration successful:",
                response
            );

            setSuccess(
                "Registration successful! Redirecting to login..."
            );

            setTimeout(() => {
                navigate("/login");
            }, 1200);

        } catch (error) {

            console.error(
                "Registration error:",
                error
            );

            setError(
                error.message ||
                "Registration failed"
            );

        } finally {

            setLoading(false);

        }

    };


    return (

        <div className="register-page">

            <div className="register-container">


                {/* =================================
                    LEFT SIDE
                ================================= */}

                <div className="register-info">

                    <div className="register-brand">
                        ShipTrack Pro
                    </div>

                    <h1>
                        Create Your Account
                    </h1>

                    <p>
                        Join the ShipTrack Pro platform
                        and manage your shipment
                        visibility efficiently.
                    </p>


                    <div className="role-info">

                        <h3>
                            Available User Types
                        </h3>


                        <div className="role-info-item">

                            <strong>
                                Customer
                            </strong>

                            <span>
                                Track and manage personal shipments.
                            </span>

                        </div>


                        <div className="role-info-item">

                            <strong>
                                Business Client
                            </strong>

                            <span>
                                Create and manage business shipments.
                            </span>

                        </div>


                        <div className="role-info-item">

                            <strong>
                                Logistics Operator
                            </strong>

                            <span>
                                Handle shipment operations and tracking.
                            </span>

                        </div>


                        <div className="role-info-item">

                            <strong>
                                Support Agent
                            </strong>

                            <span>
                                Support users and shipment-related requests.
                            </span>

                        </div>

                    </div>


                    <div className="admin-note">

                        <strong>
                            Administrator
                        </strong>

                        <p>
                            Administrator accounts are created
                            by the system and are not available
                            through public registration.
                        </p>

                    </div>

                </div>


                {/* =================================
                    REGISTER FORM
                ================================= */}

                <div className="register-card">

                    <div className="register-header">

                        <h2>
                            Register
                        </h2>

                        <p>
                            Enter your details to create
                            your ShipTrack Pro account.
                        </p>

                    </div>


                    {error && (

                        <div className="register-error">
                            {error}
                        </div>

                    )}


                    {success && (

                        <div className="register-success">
                            {success}
                        </div>

                    )}


                    <form
                        className="register-form"
                        onSubmit={handleSubmit}
                    >


                        {/* FULL NAME */}

                        <div className="form-group">

                            <label htmlFor="fullName">
                                Full Name
                            </label>

                            <input
                                id="fullName"
                                type="text"
                                name="fullName"
                                value={formData.fullName}
                                onChange={handleChange}
                                placeholder="Enter your full name"
                                required
                            />

                        </div>


                        {/* EMAIL */}

                        <div className="form-group">

                            <label htmlFor="email">
                                Email
                            </label>

                            <input
                                id="email"
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="Enter your email"
                                required
                            />

                        </div>


                        {/* PASSWORD */}

                        <div className="form-group">

                            <label htmlFor="password">
                                Password
                            </label>

                            <input
                                id="password"
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Minimum 8 characters"
                                minLength={8}
                                required
                            />

                        </div>


                        {/* PHONE */}

                        <div className="form-group">

                            <label htmlFor="phone">
                                Phone Number
                            </label>

                            <input
                                id="phone"
                                type="tel"
                                name="phone"
                                value={formData.phone}
                                onChange={handleChange}
                                placeholder="Enter your phone number"
                            />

                        </div>


                        {/* ROLE */}

                        <div className="form-group">

                            <label htmlFor="role">
                                Account Type
                            </label>

                            <select
                                id="role"
                                name="role"
                                value={formData.role}
                                onChange={handleChange}
                                required
                            >

                                <option value="CUSTOMER">
                                    Customer
                                </option>

                                <option value="BUSINESS_CLIENT">
                                    Business Client
                                </option>

                                <option value="LOGISTICS_OPERATOR">
                                    Logistics Operator
                                </option>

                                <option value="SUPPORT_AGENT">
                                    Support Agent
                                </option>

                            </select>

                        </div>


                        {/* ROLE DESCRIPTION */}

                        <div className="selected-role">

                            {formData.role === "CUSTOMER" && (
                                <>
                                    <strong>
                                        Customer
                                    </strong>

                                    <span>
                                        For users who want to track
                                        and manage their shipments.
                                    </span>
                                </>
                            )}


                            {formData.role === "BUSINESS_CLIENT" && (
                                <>
                                    <strong>
                                        Business Client
                                    </strong>

                                    <span>
                                        For businesses that create
                                        and manage shipments.
                                    </span>
                                </>
                            )}


                            {formData.role === "LOGISTICS_OPERATOR" && (
                                <>
                                    <strong>
                                        Logistics Operator
                                    </strong>

                                    <span>
                                        For users responsible for
                                        shipment operations and status updates.
                                    </span>
                                </>
                            )}


                            {formData.role === "SUPPORT_AGENT" && (
                                <>
                                    <strong>
                                        Support Agent
                                    </strong>

                                    <span>
                                        For users who handle
                                        support-related activities.
                                    </span>
                                </>
                            )}

                        </div>


                        {/* SUBMIT */}

                        <button
                            type="submit"
                            className="register-button"
                            disabled={loading}
                        >

                            {loading
                                ? "Creating Account..."
                                : "Create Account"
                            }

                        </button>

                    </form>


                    <div className="login-link">

                        <span>
                            Already have an account?
                        </span>

                        <button
                            type="button"
                            onClick={() =>
                                navigate("/login")
                            }
                        >
                            Login
                        </button>

                    </div>

                </div>

            </div>

        </div>
    );
}


export default Register;