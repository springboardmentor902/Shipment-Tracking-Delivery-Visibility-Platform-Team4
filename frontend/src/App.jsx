import "./App.css";

import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";
import UserManagement from "./pages/UserManagement";
import Login from "./pages/Login";
import AccountSettings from "./pages/AccountSettings";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import ShipmentDetail from "./pages/ShipmentDetail";
import Shipments from "./pages/Shipments";
import ReportsExport from "./pages/ReportsExport";
import CustomerAnalytics from "./pages/CustomerAnalytics";
import BusinessAnalytics from "./pages/BusinessAnalytics";
import AdminAnalytics from "./pages/AdminAnalytics";
import PODVerification from "./pages/PODVerification";
import Profile from "./pages/Profile";
import RouteOptimization from "./pages/RouteOptimization";
function ProtectedRoute({ children }) {

    const token = localStorage.getItem("token");

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

function App() {

    return (

        <BrowserRouter>

            <Routes>

                {/* LOGIN */}

                <Route
                    path="/login"
                    element={<Login />}
                />

                {/* REGISTER */}

                <Route
                    path="/register"
                    element={<Register />}
                />

                {/* DASHBOARD */}

                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />

                {/* DEFAULT */}

                <Route
                    path="/"
                    element={
                        <Navigate
                            to="/login"
                            replace
                        />
                    }
                />
                <Route
                    path="/shipments"
                    element={
                        <ProtectedRoute>
                            <Shipments />
                        </ProtectedRoute>
                    }
                />
                <Route
    path="/settings"
    element={<AccountSettings />}
/>

                {/* SHIPMENT DETAILS */}

                <Route
                    path="/shipments/:id"
                    element={
                        <ProtectedRoute>
                            <ShipmentDetail />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/customer/analytics"
                    element={
                        <ProtectedRoute>
                            <CustomerAnalytics />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/business/analytics"
                    element={
                        <ProtectedRoute>
                            <BusinessAnalytics />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/analytics"
                    element={
                        <ProtectedRoute>
                            <AdminAnalytics />
                        </ProtectedRoute>
                    }
                />
                <Route
    path="/pod-verification"
    element={
        <ProtectedRoute>
            <PODVerification />
        </ProtectedRoute>
    }
/>
<Route
    path="/user-management"
    element={<UserManagement />}
/>
                <Route
    path="/admin/users"
    element={
        <ProtectedRoute>
            <UserManagement />
        </ProtectedRoute>
    }
/>
<Route path="/profile" element={<Profile />} />

<Route
    path="/reports-export"
    element={
        <ProtectedRoute>
            <ReportsExport />
        </ProtectedRoute>
    }
/>
<Route
    path="/route-optimization"
    element={
        <ProtectedRoute>
            <RouteOptimization />
        </ProtectedRoute>
    }
/>

            </Routes>

        </BrowserRouter>

    );
}

export default App;