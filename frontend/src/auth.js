import {computed, reactive, ref, watch} from "vue";
import {openPopup} from "@/popup.js";
import RequireAuthPopup from "@/components/RequireAuthPopup.vue";

export const account = ref(null);
export const settings = reactive({});
export const theme = computed({
    get: () => {
        const value = settings.theme || "dark";
        document.body.setAttribute("data-theme", value);
        return value;
    },
    set: value => {
        document.body.setAttribute("data-theme", value);
        settings.theme = value;
    }
});

watch(account, value => {
    Object.assign(settings, value["settings"]);
});

export function fetchAccount() {
    return fetch("/api/account").then(async r => {
        if (r.status === 200) {
            account.value = await r.json();
        } else {
            account.value = null;
        }
    });
}

export function requireAuth(action, appContext, callback) {
    if (account.value) {
        callback();
    } else {
        setTimeout(() => {
            openPopup(RequireAuthPopup, appContext, {
                action: action
            });
        });
    }
}

export function openLoginPage() {
    window.open("/login", "_blank");
}

export async function saveSettings() {
    await fetch("/api/account/settings", {
        method: "PATCH",
        body: JSON.stringify(settings)
    });
    Object.assign(account.value.settings, settings);
}

window.onstorage = async e => {
    if (e.key === "auth-token") {
        await fetchAccount();
    }
}
