<script setup>
import Popup from "@/components/Popup.vue";
import {account} from "@/auth.js";
import {reactive} from "vue";
import {formatName} from "@/main.js";

const {date, supervisions, updateCalendar} = defineProps(["date", "supervisions", "updateCalendar"]);
const entries = reactive(supervisions[date] || {});

function formatNames(breakNum) {
  const entries = supervisions.value[date]?.[breakNum]?.map(n => formatName(n));
  return !entries ? "" : "- " + entries.join("\n- ");
}

function cantSignIn(breakNum) {
  const entry = supervisions.value[date]?.[breakNum];
  return entry && !entry.includes(account.value["name"]) && entry.length >= 2;
}

function isSignedIn(breakNum) {
  const entry = supervisions.value[date]?.[breakNum];
  return entry && entry.includes(account.value["name"]);
}

function getButtonText(breakNum) {
  return isSignedIn(breakNum) ? "Austragen" : "Eintragen";
}

async function signIn(breakNum) {
  const dateString = `${date.year}-${date.month}-${date.day}`;
  await fetch(`https://hub.pipeman.org/api/supervisions/${dateString}?break=${breakNum}`, {
    method: isSignedIn(breakNum) ? "DELETE" : "PUT"
  });

  await updateCalendar();
}
</script>

<template>
  <Popup :title="`Trage dich ein für ${date}`">
    <h2>Pause 1:</h2>
    {{ supervisions[date] }}
    <h3>{{ formatNames(1) }}</h3>
    <button :disabled="cantSignIn(1)" @click="signIn(1)">{{ getButtonText(1) }}</button>

    <h2>Pause 2:</h2>
    <h3>{{ formatNames(2) }}</h3>
    <button :disabled="cantSignIn(2)" @click="signIn(2)">{{ getButtonText(2) }}</button>
  </Popup>
</template>

<style scoped>
button {
  width: 100px;
}

h2 {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 24px;
}

h3 {
  margin: 5px;
  font-weight: normal;
  font-size: 15px;
  white-space: pre;
}
</style>