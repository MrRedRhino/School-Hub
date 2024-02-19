<script setup>
import {account, fetchAccount, openLoginPage} from "../auth.js";
import {ref} from "vue";
import {useRouter} from "vue-router";

const showOptions = ref(false);
const router = useRouter();

function imageSrc() {
  const name = encodeURIComponent(account.value["name"]);
  return `https://api.dicebear.com/7.x/adventurer-neutral/svg?seed=${name}&radius=50&size=32&glassesProbability=30`;
}

async function logout() {
  await fetch("https://hub.pipeman.org/api/account/logout", {
    method: "POST"
  });
  hideOptions();
  await fetchAccount();
}

function hideOptions() {
  showOptions.value = false;
}

function openSettings() {
  router.push("/settings");
  hideOptions();
}
</script>

<template>
  <div v-click-outside="hideOptions">
    <button v-if="account" class="button-primary pfp" @click="showOptions = !showOptions">
      <img :src="imageSrc()" alt="pfp">
    </button>

    <button v-else class="button-primary login" @click="openLoginPage">
      Anmelden
    </button>

    <div v-if="showOptions" class="options">
      <h2>Angemeldet als</h2>
      <h1>{{ account["name"] }}</h1>
      <hr>
      <h3 @click="openSettings">Einstellungen</h3>
      <h3 @click="logout">Abmelden</h3>
    </div>
  </div>
</template>

<style scoped>
.pfp {
  border-radius: 100%;
  width: 32px;
  height: 32px;
  padding: 0;
  position: relative;
  z-index: 10;
}

.login {
  width: fit-content;
  padding-left: 10px;
  padding-right: 10px;
}

.options {
  position: fixed;
  background: var(--background-dark);
  right: 10px;
  border-radius: 10px;
  color: var(--text);
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  z-index: 1;
}

.options * {
  margin-left: 10px;
  margin-right: 10px;
}

.options hr {
  margin-left: 0;
  margin-right: 0;
}

.options h2 {
  font-size: 14px;
  font-weight: normal;
  margin-bottom: 6px;
  color: var(--text-dark);
}

.options h1 {
  margin-top: 6px;
  font-size: 17px;
}

.options h3 {
  margin-top: 6px;
  margin-bottom: 6px;
  font-size: 17px;
  font-weight: normal;
  cursor: pointer;
}
</style>