// navigator.serviceWorker.register("https://hub.pipeman.org/assets/notification-worker.js").then();

export async function loadNotifications() {
    const serviceWorker = await navigator.serviceWorker.ready;
    const subscription = await serviceWorker.pushManager.getSubscription();
    return subscription !== null;
}

export async function enableNotifications() {
    const permission = await Notification.requestPermission() === "granted";
    if (permission) {
        await subscribe();
    }
    return permission;
}

export async function disableNotifications() {
    const serviceWorker = await navigator.serviceWorker.ready;
    const subscription = await serviceWorker.pushManager.getSubscription();

    const endpoint = encodeURIComponent(subscription.endpoint);
    await fetch("https://hub.pipeman.org/api/subscriptions/?endpoint=" + endpoint, {
        method: "DELETE"
    });

    await subscription.unsubscribe();
}

export async function subscribe() {
    const serviceWorker = await navigator.serviceWorker.ready;

    const subscription = await serviceWorker.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: "BEuSKswUwot0f81ljXwzZUBPVu9-pL0Te_-mujD_tsAUxTa2Iivs692KeOrHCUe2GmrgskOmbFf9gNZscrS_HQU"
    });

    const jsonSub = JSON.parse(JSON.stringify(subscription));
    await fetch("https://hub.pipeman.org/api/subscriptions", {
        method: "PUT",
        body: JSON.stringify({
            "endpoint": subscription.endpoint,
            "key": jsonSub["keys"]["p256dh"],
            "auth": jsonSub["keys"]["auth"]
        })
    });
}
