<script setup>

import {closePopup} from "@/popup.js";

defineProps(["title"])
</script>

<template>
  <div class="overlay">
    <div class="popup" v-click-outside="closePopup">
      <h1>{{ title }}</h1>
      <slot>

      </slot>
    </div>
  </div>
</template>

<style>
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 100;
  background: rgba(0, 0, 0, 0.15);
  animation: fade-in 0.3s;
}

@keyframes fade-in {
  from {
    background: rgba(0, 0, 0, 0);
  }
  to {
    background: rgba(0, 0, 0, 0.2);
  }
}

.popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90vw;
  background: var(--background-dark);
  padding: 10px;
  border-radius: 20px;
  color: var(--text);
  max-width: min(800px, 100vw);
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  z-index: 101;
}

.popup h1 {
  margin: 0 0 8px 5px;
  font-size: 25px;
}

.popup h2 {
  font-weight: normal;
  margin-bottom: 3px;
  margin-left: 2px;
  font-size: 19px;
}


.popup input {
  height: 34px;
  background: var(--background);
  color: var(--text);
  border-radius: 10px;
  width: calc(100% - 10px);
  font-size: 16px;
  border: none;
  padding: 0 0 0 10px;
}

.popup button {
  width: 100%;
  height: 34px;
  border-radius: 10px;
  border: none;
  background: var(--blue);
  color: var(--text);
  font-size: 18px;
  margin-top: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}

.popup button:disabled {
  background: rgba(255, 255, 255, 0.4);
}

.popup .action-row {
  display: flex;
  gap: 10px;
}
</style>