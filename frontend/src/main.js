import './assets/base.css'

import {createApp} from 'vue/dist/vue.esm-bundler.js'
import App from './App.vue'
import {createRouter, createWebHistory} from 'vue-router'

import Books from '@/pages/books/Books.vue'
import Login from "@/pages/Login.vue";
import NotFound from "@/pages/NotFound.vue";
import ClickOutsideDirective from "@/ClickOutsideDirective.js";
import SubstitutionPlan from "@/pages/substitutionplan/SubstitutionPlan.vue";
import Home from "@/pages/home/Home.vue";
import {account, fetchAccount} from "@/auth.js";
import Settings from "@/pages/Settings.vue";
import Supervision from "@/pages/supervision/Supervision.vue";
import Reservation from "@/pages/reservation/Reservation.vue";

const routes = [
    {path: "/", component: Home, meta: {title: ""}},
    {path: "/books", component: Books, meta: {title: "Bücher"}},
    {path: "/vertretungsplan", component: SubstitutionPlan, meta: {title: "Vertretungsplan"}},
    {path: "/login", component: Login, meta: {title: "Login"}},
    {path: "/supervision", component: Supervision, meta: {title: "Supervision"}},
    {path: "/settings", component: Settings, meta: {title: "Einstellungen"}},
    {path: "/reservation", component: Reservation, meta: {title: "Sitzplatz Reservierung"}},
    {path: "/:pathMatch(.*)*", component: NotFound, meta: {title: "404"}}
];
let lastPath = null;

const router = createRouter({
    history: createWebHistory(),
    routes: routes
});

function collectAnalytics() {
    if (lastPath !== location.href) {
        lastPath = location.href;

        if (process.env.NODE_ENV === "production") {
            const uid = account.value?.id || "no-auth";
            fetch(`https://analytics.pipeman.org/api/gather?uid=${uid}&url=${location.href}&referrer=${document.referrer}`, {
                method: "POST"
            }).then();
        }
    }
}

router.beforeEach((to, from, next) => {
    setPageTitle(to.meta.title);
    next();
});

router.afterEach(to => {
    collectAnalytics();
})

export function setPageTitle(title) {
    if (title === "") {
        document.title = "Pipeman";
    } else {
        document.title = title + " - Pipeman";
    }
}

fetchAccount().then(() => {
    const app = createApp(App);
    app.directive("click-outside", ClickOutsideDirective);
    app.use(router);
    app.mount('#app');
});

export function deepCompare(arg1, arg2) {
    if (Object.prototype.toString.call(arg1) === Object.prototype.toString.call(arg2)) {
        if (Object.prototype.toString.call(arg1) === '[object Object]' || Object.prototype.toString.call(arg1) === '[object Array]') {
            if (Object.keys(arg1).length !== Object.keys(arg2).length) {
                return false;
            }
            return (Object.keys(arg1).every(function (key) {
                return deepCompare(arg1[key], arg2[key]);
            }));
        }
        return (arg1 === arg2);
    }
    return false;
}

export function formatName(name, short = true) {
    const split = name.split(", ");
    if (split.length === 1) return name;
    const surname = split[1].split(" ")[0];
    return surname + " " + split[0];
}
