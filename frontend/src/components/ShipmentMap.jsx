import { GoogleMap, Marker, useJsApiLoader } from "@react-google-maps/api";

const containerStyle = {
    width: "100%",
    height: "500px",
};

const center = {
    lat: 16.5062,
    lng: 80.6480,
};

function ShipmentMap() {
    const { isLoaded, loadError } = useJsApiLoader({
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
    });

    if (loadError) {
        return <div>Google Maps failed to load.</div>;
    }

    if (!isLoaded) {
        return <div>Loading Google Maps...</div>;
    }

    return (
        <GoogleMap
            mapContainerStyle={containerStyle}
            center={center}
            zoom={7}
        >
            <Marker position={center} />
        </GoogleMap>
    );
}

export default ShipmentMap;