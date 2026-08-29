import "./App.css";

import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import ShipmentDetail from "./pages/ShipmentDetail";
import Shipments from "./pages/Shipments";
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

                {/* SHIPMENT DETAILS */}

                <Route
                    path="/shipments/:id"
                    element={
                        <ProtectedRoute>
                            <ShipmentDetail />
                        </ProtectedRoute>
                    }
                />

            </Routes>

        </BrowserRouter>

    );
}

export default App;