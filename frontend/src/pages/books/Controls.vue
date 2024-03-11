<script setup>

import {getCurrentInstance, ref} from "vue";
import {requireAuth} from "@/auth.js";

const {book, page, zoom} = defineProps(["book", "page", "zoom"]);
const emit = defineEmits(["set-page", "change-zoom", "change-page", "change-pencil", "open-summary"]);
const activePencil = ref(null);
const {appContext} = getCurrentInstance();

const colors = [
  {
    color: "#3084e3",
    border: "#71a7e7"
  },
  {
    color: "#48d272",
    "border": "#84ffa8"
  },
  {
    color: "#ea47b6",
    border: "#e569ba"
  },
  {
    color: "#ff6a00",
    border: "#ef9859"
  }
];

function pageInput(event) {
  const newPage = parseInt(event.target.value);
  if (!isNaN(newPage)) {
    emit("set-page", newPage);
  }
}

function nextPage() {
  emit("change-page", 1);
}

function previousPage() {
  emit("change-page", -1);
}

function increaseZoom() {
  emit("change-zoom", 10);
}

function decreaseZoom() {
  emit("change-zoom", -10);
}

function changePencil(color, index) {
  requireAuth("Anmerkungen hinzuzufügen", appContext, () => {
    emit("change-pencil", index === activePencil.value ? null : color);
    activePencil.value = index === activePencil.value ? null : index;
  });
}

function changeEraser() {
  activePencil.value = activePencil.value === -1 ? null : -1;
  emit("change-pencil", null, activePencil.value === -1);
}

function openSummary() {
  emit("open-summary");
}
</script>

<template>
  <div class="wrapper">
    <div class="row">
      <button @click="previousPage" class="page-button"><</button>
      <input class="page-input" :value="page" @input="pageInput($event)"><a> / {{ book["page-count"] }}</a>
      <button @click="nextPage" class="page-button">></button>
      <button @click="openSummary" class="page-button summary-button"></button>

      <!--      <br>-->
      <!--      <button class="page-button" @click="decreaseZoom">-</button>-->
      <!--      <a class="zoom">{{ zoom }}</a>-->
      <!--      <button class="page-button" @click="increaseZoom">+</button>-->
    </div>

    <div class="row">
      <button class="pencil-button" v-for="(color, index) in colors"
              :style="{'border-color': color.border, 'background-color': color.color}"
              :class="{active: activePencil === index}"
              @mousedown="changePencil(color.color, index)">
      </button>

      <br>
      <button class="pencil-button eraser" :class="{active: activePencil === -1}"
              @mousedown="changeEraser"
      ></button>
    </div>
  </div>
</template>

<style scoped>
.wrapper {
  background: var(--background-dark);
  position: fixed;
  bottom: 40px;
  padding: 5px;
  border-radius: 10px;
  color: var(--text);
  display: flex;
  flex-direction: column;
  width: fit-content;
  gap: 5px;
}

.summary-button {
  background-image: url('@/assets/ai-icon.svg');
  background-size: 90%;
  background-repeat: no-repeat;
  background-position: center;
}

.page-input {
  border-radius: 6px;
  width: 35px;
  background: var(--background);
  color: var(--text);
  border: none;
  font-size: 16px;
  padding: 0 0 0 3px;
  height: 21px;
}

.page-button {
  border: none;
  background-color: var(--blue);
  border-radius: 6px;
  color: var(--text);
  cursor: pointer;
  min-width: 21px;
  height: 21px;
  padding: 0;
  text-align: center;
  font-size: 14px;
}

.row {
  display: flex;
  gap: 5px;
}

.pencil-button {
  border: 1px solid;
  border-radius: 100%;
  width: 30px;
  height: 30px;
  cursor: pointer;
  background-image: url('@/assets/marker-icon.svg');
  background-position: center;
  background-size: 70%;
  background-repeat: no-repeat;
  transition: transform 0.2s ease-in-out;
}

.eraser {
  background-image: url('@/assets/eraser-icon.svg');
  background-color: var(--text);
  border-color: white;
}

.zoom {
  width: 40px;
  text-overflow: clip;
  text-align: center;
}

.active {
  transform: translateY(-3px);
}

a {
  margin: 0;
}
</style>