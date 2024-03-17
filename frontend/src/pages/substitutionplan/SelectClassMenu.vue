<script setup>
import {computed, ref} from "vue";
import {account, saveSettings, settings} from "@/auth.js";
import {closePopup} from "@/popup.js";

const errorMessage = ref("");
const availableClasses = ref([]);
const showAddPlanMenu = ref(false);
const selectedClass = computed({
  get: () => account.value ? settings["class"] : localStorage.getItem("substitution-class"),
  set: value => {
    if (account.value) {
      settings["class"] = value;
      saveSettings();
    } else {
      localStorage.setItem("substitution-class", value)
    }
  }

});
const addingPlan = ref(false);

const clazz = ref("");
const username = ref("");
const password = ref("");
const todayPlanUrl = ref("");
const tomorrowPlanUrl = ref("");

fetch("/api/plans").then(response => response.json().then(data => {
  availableClasses.value = data;
}));

async function addPlan() {
  addingPlan.value = true;
  errorMessage.value = "";

  const encodedClass = encodeURIComponent(clazz.value);
  const encodedUsername = encodeURIComponent(username.value);
  const encodedPassword = encodeURIComponent(password.value);
  const todayUrl = getPlanId(todayPlanUrl.value);
  const tomorrowUrl = getPlanId(tomorrowPlanUrl.value);
  try {
    const response = await fetch(`/api/plans/${encodedClass}?username=${encodedUsername}&password=${encodedPassword}&todayPlanId=${todayUrl}&tomorrowPlanId=${tomorrowUrl}`, {
      method: "PUT"
    });

    if (response.status === 200) {
      selectedClass.value = clazz.value;
      closePopup();
    } else {
      const responseText = await response.text();
      if (responseText === "Invalid credentials") {
        errorMessage.value = "Ungültige Anmeldedaten";
      } else if (responseText === "Invalid plan ids") {
        errorMessage.value = "Ungültige Plan Links";
      }
    }

  } catch (e) {
  }

  addingPlan.value = false;
}

function getPlanId(url) {
  const match = url.match(/(\d+)/);
  if (match) {
    return match[1];
  }
  return "";
}
</script>

<template>
  <a>Klasse: </a>
  <select v-model="selectedClass">
    <option v-for="clazz in availableClasses" :selected="clazz['class'] === selectedClass">{{ clazz["class"] }}</option>
  </select>

  <br>
  <br>
  <a class="not-found" @click="showAddPlanMenu = !showAddPlanMenu">Klasse nicht gefunden?</a>
  <div class="add-plan-wrapper" v-if="showAddPlanMenu">
    <button v-if="!account" class="button-primary">Melde dich an, um einen Plan hinzuzufügen</button>

    <div v-else>
      <input v-model="clazz" placeholder="Klasse z.B. G10a">
      <input v-model="username" placeholder="Fronter-Nutzername">
      <input v-model="password" placeholder="Fronter-Passwort" type="password">

      <input v-model="todayPlanUrl" placeholder="Heutiger Plan Link">
      <input v-model="tomorrowPlanUrl" placeholder="Morgiger Plan Link">
      <h2>In die beiden Link-Felder die Links zum jeweiligen Vertretungsplan eingetragen. Die Pläne findest du im
        Ressourcen Ordner des Kurses eurer Klasse.
        Im Bild ist erklärt, wie man die Links kopiert.</h2>
      <img src="@/assets/copy-link2.png">

      <button
          @click="addPlan"
          :disabled="addingPlan || clazz === '' || username === '' || password === '' || todayPlanUrl === '' || tomorrowPlanUrl === ''"
          class="button-primary">Hinzufügen
      </button>
      <h2 class="error-message" v-if="errorMessage">{{ errorMessage }}</h2>

      <h2>
        <strong>Warum muss ich meine Fronter Anmeldedaten eingeben?</strong><br>
        Die Pläne auf dieser Website werden von Fronter heruntergeladen. Dafür wird ein Fronter Account aus der Klasse
        benötigt, damit diese Website auf den Plan zugreifen kann. Aber keine Sorge: Dein Passwort wird sicher und
        verschlüsselt gespeichert.
      </h2>
    </div>
  </div>
</template>

<style scoped>
a {
  font-weight: normal;
  font-size: 18px;
  color: var(--text);
}

.not-found {
  color: cornflowerblue;
  cursor: pointer;
}

.button-primary {
  width: fit-content;
}

.add-plan-wrapper {
  margin-top: 10px;
}

h2 {
  color: var(--text);
  font-weight: normal;
  font-size: 18px;
  margin-bottom: 5px;
  margin-left: 5px;
}

input {
  height: 34px;
  background: var(--background);
  color: var(--text);
  border-radius: 10px;
  font-size: 16px;
  border: none;
  padding: 0 0 0 10px;
  display: block;
  margin-bottom: 10px;
}

img {
  display: block;
  margin-bottom: 10px;
}

.error-message {
  font-size: 18px;
  margin: 0;
  color: #ff2549;
}
</style>