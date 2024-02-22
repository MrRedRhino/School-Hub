<script setup>
import Toggle from "@/components/Toggle.vue";
import {account, openLoginPage, settings, theme} from "@/auth.js";
import {computed, ref} from "vue";
import {disableNotifications, enableNotifications, loadNotifications} from "@/notifications.js";
import {deepCompare} from "../main.js";

const courseFilter = ref(account.value["course-filter"]);
const changed = computed(() => {
  return courseFilter.value !== account.value["course-filter"] || !deepCompare(settings, account.value?.settings);
});
const notificationsEnabled = ref(false);
const notificationsDisabled = ref(false);
const saveDisabled = ref(false);
const notificationError = ref("");

loadNotifications().then(enabled => notificationsEnabled.value = enabled);

async function toggleNotifications(enabled) {
  notificationsDisabled.value = true;
  notificationError.value = "";
  try {
    if (enabled) {
      const permission = await enableNotifications();
      if (!permission) {
        notificationError.value = "Erlaube dieser Website, Notifications zu versenden";
      }
    } else {
      await disableNotifications();
    }
  } catch (e) {
    notificationError.value = "Etwas ist schiefgelaufen";
  }

  notificationsDisabled.value = false;
}

async function save() {
  saveDisabled.value = true;

  await fetch("/api/account/settings", {
    method: "PATCH",
    body: JSON.stringify(settings)
  });
  Object.assign(account.value.settings, settings);

  await fetch("/api/account/course-filter", {
    method: "PATCH",
    body: courseFilter.value
  });
  account.value["course-filter"] = courseFilter.value;

  saveDisabled.value = false;
}

const spTables = computed({
  get: () => !settings["substitution-html-view"],
  set: newValue => settings["substitution-html-view"] = !newValue
});
</script>

<template>
  <div class="wrapper">
    <h1>Einstellungen</h1>

    <button v-if="!account" @click="openLoginPage()" class="not-logged-in">Anmelden</button>

    <div v-else>
      <h2>Erscheinung</h2>
      <div class="setting">
        <h2>Dark-Mode</h2>
        <select v-model="theme">
          <option value="dark">Dunkel</option>
          <option value="light">Hell</option>
          <option value="hub">Hub</option>
        </select>

        <!--        <Toggle @change="updateSettings" v-model="settings['theme']"></Toggle>-->
      </div>

      <h2>Allgemein</h2>
      <div class="setting column">
        <h2>Kurs-Filter</h2>
        <textarea v-model="courseFilter"></textarea>
      </div>

      <h2>Vertretungsplan</h2>
      <div class="setting">
        <h2>Tabellen-Ansicht</h2>
        <Toggle v-model="spTables"></Toggle>
      </div>

      <!--      <div class="setting">-->
      <!--        <h2>Notifications auf diesem Gerät</h2>-->
      <!--        <Toggle v-model="notificationsEnabled" @change="toggleNotifications" :disabled="notificationsDisabled"></Toggle>-->
      <!--      </div>-->
      <!--      <a>{{ notificationError }}</a>-->
      <br>

      <button @click="save"
              :disabled="saveDisabled || !changed"
              class="not-logged-in">{{ changed ? "Speichern" : "Gespeichert" }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.wrapper {
  color: var(--text);
  max-width: 400px;
}

.setting h2 {
  font-size: 18px;
  font-weight: normal;
  margin: 0 0 0 3px;
}

.column {
  flex-direction: column !important;
}

h2 {
  font-size: 20px;
  font-weight: bold;
  margin-left: 10px;
  margin-bottom: 14px;
  margin-top: 20px;
}

h1 {
  font-size: 24px;
  margin-left: 7px;
  margin-bottom: 20px;
}

h3 {
  margin-top: 0;
  margin-left: 14px;
}

.setting {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 6px;
  padding-left: 10px;
  padding-right: 10px;
}

.setting textarea {
  background: var(--background-dark);
  border: none;
  border-radius: 10px;
  resize: none;
  color: var(--text);
  width: 100%;
  height: 70px;
}

.not-logged-in {
  font-size: 18px;
  padding-left: 10px;
  padding-right: 10px;
  margin-left: 10px;
}

button {
  margin-top: 20px;
  background: var(--blue);
  border: none;
  border-radius: 10px;
  height: 34px;
  color: var(--text);
  min-width: 34px;
  margin-bottom: 10px;
  cursor: pointer;
  text-align: center;
  font-size: 18px;
  padding: 0 10px;
  margin-left: 10px;
}

button:disabled {
  background: var(--text-dark);
  cursor: unset;
}

a {
  color: #ce082a;
  margin-left: 20px;
  margin-top: 0;
}
</style>