<script setup>

import {closePopup} from "@/popup.js";

defineProps(["title"])
</script>

<template>
  <div class="overlay">
    <div class="popup" v-click-outside="closePopup">
      <div class="header">
        <h1>{{ title }}</h1>
        <button @click="closePopup">
          <svg width="800px" height="800px" viewBox="0 0 24 24" fill="none">
            <path
                d="M20.7457 3.32851C20.3552 2.93798 19.722 2.93798 19.3315 3.32851L12.0371 10.6229L4.74275 3.32851C4.35223 2.93798 3.71906 2.93798 3.32854 3.32851C2.93801 3.71903 2.93801 4.3522 3.32854 4.74272L10.6229 12.0371L3.32856 19.3314C2.93803 19.722 2.93803 20.3551 3.32856 20.7457C3.71908 21.1362 4.35225 21.1362 4.74277 20.7457L12.0371 13.4513L19.3315 20.7457C19.722 21.1362 20.3552 21.1362 20.7457 20.7457C21.1362 20.3551 21.1362 19.722 20.7457 19.3315L13.4513 12.0371L20.7457 4.74272C21.1362 4.3522 21.1362 3.71903 20.7457 3.32851Z"
                fill="currentColor"/>
          </svg>
        </button>
      </div>
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
  max-width: min(500px, 100vw);
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

<style scoped>
.header {
  display: flex;
}

.header button {
  background: none;
  width: 30px;
  height: 30px;
  margin: 0 0 0 auto;
  padding: 0;
}

.header button svg {
  width: 30px;
  height: 30px;
}
</style>
