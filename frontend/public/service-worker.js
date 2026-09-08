self.addEventListener("push", (event) => {

    if (!event.data) {
        return;
    }

    const data = event.data.json();

    const title =
        data.title || "ShipTrack Pro";

    const options = {
        body:
            data.message ||
            "You have a new shipment notification.",

        icon: "/vite.svg",

        badge: "/vite.svg",

        data: {
            url: "/dashboard"
        }
    };

    event.waitUntil(
        self.registration.showNotification(
            title,
            options
        )
    );
});


self.addEventListener("notificationclick", (event) => {

    event.notification.close();

    const url =
        event.notification.data?.url ||
        "/dashboard";

    event.waitUntil(
        clients.matchAll({
            type: "window",
            includeUncontrolled: true
        }).then((clientList) => {

            for (const client of clientList) {

                if ("focus" in client) {

                    client.navigate(url);

                    return client.focus();
                }
            }

            if (clients.openWindow) {
                return clients.openWindow(url);
            }

        })
    );
});