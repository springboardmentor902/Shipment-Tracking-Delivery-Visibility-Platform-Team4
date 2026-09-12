const API_BASE_URL = "http://localhost:8080/api";

const getToken = () => localStorage.getItem("token");

const request = async (url) => {
    const response = await fetch(url, {
        headers: {
            Authorization: `Bearer ${getToken()}`,
            "Content-Type": "application/json",
        },
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Route request failed");
    }

    return response.json();
};

export const getOptimizedRoutes = (origin, destination) =>
    request(
        `${API_BASE_URL}/routes/optimization?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
    );

export const getBestRoute = (origin, destination) =>
    request(
        `${API_BASE_URL}/routes/optimization/best?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
    );

export const getShortestRoute = (origin, destination) =>
    request(
        `${API_BASE_URL}/routes/optimization/shortest?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
    );

export const getFastestRoute = (origin, destination) =>
    request(
        `${API_BASE_URL}/routes/optimization/fastest?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
    );

export const getRouteByType = (origin, destination, type) =>
    request(
        `${API_BASE_URL}/routes/optimization/type/${encodeURIComponent(type)}?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`
    );