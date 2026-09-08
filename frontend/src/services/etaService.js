const API_BASE_URL = "http://localhost:8080/api";

export const getETAPrediction = async (shipmentId) => {

    const token = localStorage.getItem("token");

    const response = await fetch(
        `${API_BASE_URL}/eta/${shipmentId}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        }
    );

    if (!response.ok) {

        const errorText = await response.text();

        throw new Error(
            errorText ||
            `Failed to get ETA for shipment ${shipmentId}`
        );
    }

    return await response.json();
};