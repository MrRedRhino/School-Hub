<script setup>
import Popup from "@/components/Popup.vue";
import {closePopup} from "@/popup.js";
import {openLoginPage} from "@/auth.js";

const {action} = defineProps(["action"]);
</script>

<template>
<Popup title="Anmelden">
  <h2>Melde dich an, um {{ action }}</h2>

  <div class="action-row">
    <button @click="closePopup">Abbrechen</button>
    <button @click="openLoginPage()">Anmelden</button>
  </div>
</Popup>
</template>

<style scoped>

</style>