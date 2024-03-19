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
      <button @click="openSummary" class="page-button summary-button">
        <svg class="svg-icon"
             fill="currentColor"
             viewBox="0 0 1024 1024">
          <path
              d="M501.824 32C303.552 32 141.504 176.992 141.504 357.76c0 23.712 2.816 47.104 8.32 69.856l-51.008 114.208a32 32 0 0 0 24.704 44.736c54.272 7.744 76.672 31.168 76.672 77.312v111.552a64 64 0 0 0 64 64h20.704a64 64 0 0 1 64 64V960a32 32 0 0 0 32 32h345.6a32 32 0 0 0 0-64h-313.6v-24.608a128 128 0 0 0-128-128h-20.736v-111.552c0-65.664-32.192-110.688-91.2-131.136l39.872-89.28a31.968 31.968 0 0 0 1.568-21.792 233.088 233.088 0 0 1-8.896-63.904c0-143.712 131.936-261.76 296.32-261.76s296.32 118.016 296.32 261.76a32 32 0 0 0 64 0C862.144 176.992 700.064 32 501.824 32zM904 448a32 32 0 0 0-32 32v360a32 32 0 0 0 64 0V480a32 32 0 0 0-32-32z"/>
          <path
              d="M673.888 466.656c-11.744-25.568-48.416-24.64-58.816 1.536l-132.8 333.76a32 32 0 0 0 59.488 23.68l32.608-81.92c0.576 0.032 1.088 0.32 1.664 0.32h154.848l38.176 83.104a31.968 31.968 0 1 0 58.144-26.72l-153.312-333.76zM599.68 680l47.264-118.72 54.528 118.72H599.68z"/>
        </svg>
      </button>

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
  background-color: #b0bac5;
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