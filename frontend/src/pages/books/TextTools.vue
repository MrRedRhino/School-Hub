<script setup>
import {nextTick} from "vue";

const props = defineProps(["text"]);
const emit = defineEmits(["resize", "delete"]);
const colors = ["#3084e3", "#48d272", "#ff6a00", "#000000"]

function changeFontsize(delta) {
  props.text.s += delta;
  nextTick(() => emit("resize"));
}
</script>

<template>
  <div class="wrapper">
    <button class="color-button" v-for="color in colors" :style="{background: color}" @click="text.c = color">

    </button>

    <button @click="changeFontsize(2)">
      <svg width="800px" height="800px" viewBox="0 0 24 24" fill="none">
        <path d="M6 12H18M12 6V18" stroke-width="2" stroke-linecap="round" stroke="currentColor"
              stroke-linejoin="round"/>
      </svg>
    </button>
    <button @click="changeFontsize(-2)">
      <svg width="800px" height="800px" viewBox="0 0 24 24" fill="none">
        <path d="M6 12L18 12" stroke-width="2" stroke-linecap="round" stroke="currentColor" stroke-linejoin="round"/>
      </svg>
    </button>
    <button @click="emit('delete')">
      <svg width="800px" height="800px" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M10 12V17" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M14 12V17" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M4 7H20" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M6 10V18C6 19.6569 7.34315 21 9 21H15C16.6569 21 18 19.6569 18 18V10" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 5V7H9V5Z" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
  </div>
</template>

<style scoped>
.wrapper {
  position: absolute;
  z-index: 100;
  gap: 5px;
  display: grid;
  grid-auto-flow: column;
  background: var(--background);
  padding: 5px;
  border-radius: 5px;
  transform: translate(-10px, -35px);
}

button {
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  font-size: 20px;
  font-family: "JetBrains Mono", serif;
  color: var(--text);
  background: none;
  cursor: pointer;
}

.color-button {
  border-radius: 100%;
}

svg {
  width: 100%;
  height: 100%;
}
</style>