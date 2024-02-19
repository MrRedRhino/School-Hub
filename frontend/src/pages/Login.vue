<script setup>
import {ref} from "vue";
import {useRouter} from "vue-router";
import {fetchAccount} from "@/auth.js";

const code = ref("");
const codeInvalid = ref(false);
const codeSent = ref(false);

const name = ref("");
const nameInvalid = ref(false);
const nameDisabled = ref(false);
const showCode = ref(false);
const router = useRouter();

async function sendCode() {
  if (name.value.length < 4) {
    nameInvalid.value = true;
    return;
  }

  const response = await fetch(`https://hub.pipeman.org/api/login/?name=${encodeURIComponent(name.value)}`, {
    method: "POST"
  });

  if (response.status === 400) {
    nameInvalid.value = true;
  } else {
    showCode.value = true;
  }
}

async function sendCodeButtonPressed() {
  nameDisabled.value = true;
  await sendCode();
  nameDisabled.value = false;
  codeSent.value = true;
}

async function login() {
  codeInvalid.value = code.value.length <= 0;

  const response = await fetch(`https://hub.pipeman.org/api/login/verify?code=${encodeURIComponent(code.value)}`, {
    method: "POST"
  });

  if (response.status === 401) {
    codeInvalid.value = true;
    return;
  }

  const body = await response.json();
  localStorage.setItem("auth-token", body["token"]);

  const urlParams = new URLSearchParams(window.location.search);
  const redirect = urlParams.get("redirect");
  if (redirect !== null) {
    location.href = redirect;
  } else {
    window.close();
    await router.push("/");
  }

  await fetchAccount();
}
</script>

<template>
  <div class="wrapper">
    <div class="login">
      <h2>Dein Fronter-Name</h2>
      <input v-model.trim="name" placeholder="Chuck Norris">
      <h3 v-if="nameInvalid">Unbekannter Name. Probiere "Vorname Nachname"</h3>
      <button class="button-primary" @click="sendCodeButtonPressed" :disabled="nameDisabled">{{codeSent ? "Erneut senden" : "Code senden"}}</button>

      <div v-if="showCode">
        <h4>Trage den Code, der Dir über Fronter geschickt wurde, hier ein:</h4>
        <input v-model.trim="code" placeholder="Code">
        <h3 v-if="codeInvalid">Ungültiger Code</h3>
        <button class="button-primary" @click="login" :disabled="code.length === 0">Anmelden</button>
      </div>

      <a v-else @click="showCode = true">Ich habe schon einen code</a>

      <br>
    </div>
  </div>
</template>

<style scoped>
a {
  color: var(--blue);
  text-decoration: underline;
  cursor: pointer;
}

.login {
  width: 350px;
  color: var(--text);
}

.wrapper {
  display: flex;
  align-items: center;
  flex-direction: column;
  margin-top: 20%;
}

.login h2 {
  margin-bottom: 10px;
  font-size: 19px;
}

.login h3 {
  color: red;
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 16px;
  padding-left: 10px;
}

.login h4 {
  margin-bottom: 7px;
  padding-left: 10px;
}

.login input {
  background: var(--background-dark);
  border: none;
  border-radius: 10px;
  height: 34px;
  font-size: 16px;
  color: var(--text);
  padding-left: 10px;
  width: 100%;
}

.button-primary {
  margin-top: 7px;
  margin-bottom: 7px;
}
</style>