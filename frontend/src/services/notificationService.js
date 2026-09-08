const API_URL = "http://localhost:8080/api/notifications";

const getAuthHeaders = () => {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
};

export const getNotifications = async () => {
    const response = await fetch(API_URL, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error("Failed to fetch notifications");
    }

    return response.json();
};

export const markNotificationAsRead = async (id) => {
    const response = await fetch(
        `${API_URL}/${id}/read`,
        {
            method: "PATCH",
            headers: getAuthHeaders(),
        }
    );

    if (!response.ok) {
        throw new Error("Failed to mark notification as read");
    }

    return response.json();
};
export const registerPushSubscription = async () => {

    if (!("serviceWorker" in navigator)) {
        throw new Error(
            "Service workers are not supported by this browser."
        );
    }

    if (!("PushManager" in window)) {
        throw new Error(
            "Push notifications are not supported by this browser."
        );
    }

    const registration =
        await navigator.serviceWorker.register(
            "/service-worker.js"
        );

    console.log(
        "Service worker registered:",
        registration
    );

    const permission =
        await Notification.requestPermission();

    if (permission !== "granted") {

        throw new Error(
            "Notification permission was not granted."
        );
    }

    const existingSubscription =
        await registration.pushManager.getSubscription();

    if (existingSubscription) {

        await savePushSubscription(
            existingSubscription
        );

        return existingSubscription;
    }

    const vapidPublicKey =
        import.meta.env.VITE_VAPID_PUBLIC_KEY;

    if (!vapidPublicKey) {

        throw new Error(
            "VITE_VAPID_PUBLIC_KEY is not configured."
        );
    }

    const applicationServerKey =
        urlBase64ToUint8Array(
            vapidPublicKey
        );

    const subscription =
        await registration.pushManager.subscribe({

            userVisibleOnly: true,

            applicationServerKey
        });

    await savePushSubscription(
        subscription
    );

    return subscription;
};


const savePushSubscription = async (
    subscription
) => {

    const json =
        subscription.toJSON();

    const response =
        await fetch(
            "http://localhost:8080/api/push/subscribe",
            {
                method: "POST",

                headers: getAuthHeaders(),

                body: JSON.stringify({
                    endpoint: json.endpoint,

                    p256dh:
                        json.keys?.p256dh,

                    auth:
                        json.keys?.auth
                })
            }
        );

    if (!response.ok) {

        throw new Error(
            "Failed to save push subscription."
        );
    }

    return response.text();
};


export const unregisterPushSubscription =
    async () => {

        const response =
            await fetch(
                "http://localhost:8080/api/push/unsubscribe",
                {
                    method: "DELETE",
                    headers: getAuthHeaders()
                }
            );

        if (!response.ok) {

            throw new Error(
                "Failed to remove push subscription."
            );
        }

        return response.text();
    };


const urlBase64ToUint8Array =
    (base64String) => {

        const padding =
            "=".repeat(
                (4 - base64String.length % 4) % 4
            );

        const base64 =
            (
                base64String +
                padding
            )
                .replace(/-/g, "+")
                .replace(/_/g, "/");

        const rawData =
            window.atob(base64);

        const outputArray =
            new Uint8Array(
                rawData.length
            );

        for (
            let i = 0;
            i < rawData.length;
            ++i
        ) {

            outputArray[i] =
                rawData.charCodeAt(i);
        }

        return outputArray;
    };