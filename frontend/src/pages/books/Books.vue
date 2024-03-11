<script setup>
import {computed, getCurrentInstance, nextTick, onUnmounted, ref} from "vue";
import Controls from "@/pages/books/Controls.vue";
import {openPopup} from "@/popup.js";
import SummaryPopup from "@/components/SummaryPopup.vue";

const htmlHolder = ref();
const bookDisplay = ref();
const pageContent = ref("");
const currentBook = ref(null);
const currentPage = ref(1);

const zoomString = computed(() => autoZoom.value ? "Auto" : formatZoom(zoom.value));
const zoom = ref(1);
const autoZoom = ref(true);

const isDrawing = ref(false);
let eraser = false;
const canvasElement = ref();
const liveCanvas = ref();
const lineWidth = 10;
const alpha = 0.4;
const activeColor = ref(null);
let lines = [];
let mouseDown = false;
let start;
let saveId = null;
let fadeoutTimeoutId = null;
const showControls = ref(true);
const searchResults = ref();
const showResults = ref(false);
const {appContext} = getCurrentInstance();

function refreshControlsFadeout() {
  showControls.value = true;
  if (fadeoutTimeoutId !== null) {
    clearTimeout(fadeoutTimeoutId);
  }

  if (currentBook.value) {
    fadeoutTimeoutId = setTimeout(() => {
      showControls.value = false;
    }, 5000);
  }
}

function scheduleSave() {
  if (saveId !== null) {
    clearTimeout(saveId);
  }
  saveId = setTimeout(() => saveAnnotations(), 3000);
}

function runPendingSaveNow() {
  if (saveId !== null) {
    clearTimeout(saveId);
    saveAnnotations();
  }
}

function formatZoom(zoom) {
  return Math.round(zoom * 10) * 10 + "%";
}

async function openBook(id, page = 1) {
  const book = await fetch(`/api/books/${id}`);
  if (book.status > 200) return;

  currentBook.value = await book.json();

  if (currentBook.value === null || currentBook.value.id + "" !== id) {
    history.pushState(null, "", `/books?book=${currentBook.value.id}&page=${page}`);
  }

  await setPage(page);
  hideResults();
}

async function setPage(page) {
  runPendingSaveNow();
  page = Math.max(Math.min(page, currentBook.value["page-count"]), 1);
  currentPage.value = page;

  const html = await fetch(`/api/books/${currentBook.value["id"]}/${page}`);
  pageContent.value = await html.text();
  history.replaceState(history.state, "", `/books?book=${currentBook.value.id}&page=${page}`);
  localStorage.setItem("last-book", JSON.stringify({"book": currentBook.value.id, page: page}))

  await nextTick(() => {
    applyAutozoom();
    pageChanged();
  });
}

function setPencil(color, newEraser) {
  activeColor.value = color;
  eraser = newEraser;
  isDrawing.value = eraser || color !== null;
}

function onMouseDown(event) {
  refreshControlsFadeout();
  mouseDown = true;

  if (!isDrawing.value) return;

  start = {
    x: event.offsetX,
    y: event.offsetY,
  }
}

function onMouseUp(event) {
  mouseDown = false;

  if (activeColor.value !== null) {
    addLine(event.offsetX, event.offsetY);
    start = null;
  }

  scheduleSave();
}

function addLine(endX, endY) {
  lines.push({
    x1: start.x,
    y1: start.y,
    x2: endX,
    y2: endY,
    color: activeColor.value
  });

  redrawCanvas();
  clearLiveCanvas();
}

function cancelLine() {
  clearLiveCanvas();
  mouseDown = false;
}

function redrawCanvas() {
  const canvas = canvasElement.value;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  configureContext(ctx);

  for (let line of lines) {
    ctx.strokeStyle = line.color;

    ctx.beginPath();
    ctx.moveTo(line.x1, line.y1);
    ctx.lineTo(line.x2, line.y2);
    ctx.stroke();
  }
}

function clearLiveCanvas() {
  const canvas = liveCanvas.value;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  configureContext(ctx);
}

function onMouseMove(event) {
  refreshControlsFadeout();
  if (!mouseDown) return;

  const pos = {
    x: event.offsetX,
    y: event.offsetY
  };

  if (eraser) {
    erase(pos);
  } else if (activeColor.value !== null) {
    drawPreview(pos);
  }
}

function drawPreview(pos) {
  const ctx = liveCanvas.value.getContext("2d");
  clearLiveCanvas();

  ctx.beginPath();
  ctx.moveTo(start.x, start.y);
  ctx.lineTo(pos.x, pos.y);
  ctx.stroke();
}

function erase(pos) {
  lines = lines.filter(item => !isIntersecting(pos, item));
  redrawCanvas();
}

function configureContext(ctx) {
  ctx.lineWidth = lineWidth;
  ctx.strokeStyle = activeColor.value;
  ctx.globalAlpha = alpha;
}

async function saveAnnotations() {
  saveId = null;
  await fetch(`/api/books/${currentBook.value["id"]}/${currentPage.value}/annotations`, {
    method: "PUT",
    body: JSON.stringify(lines)
  });
}

function loadAnnotations(annotations) {
  lines = annotations;
  redrawCanvas();
}

async function pageChanged() {
  const width = htmlHolder.value.clientWidth;
  const height = htmlHolder.value.clientHeight;
  canvasElement.value.width = width;
  liveCanvas.value.width = width;
  canvasElement.value.height = height;
  liveCanvas.value.height = height;

  const annotations = await fetch(`/api/books/${currentBook.value["id"]}/${currentPage.value}/annotations`);
  loadAnnotations(await annotations.json());
}

function applyAutozoom() {
  if (autoZoom.value) {
    zoom.value = computeAutoZoom();
  }
}

window.onresize = () => applyAutozoom();

function computeAutoZoom() {
  const scaledContent = htmlHolder.value;
  const scaledWrapper = bookDisplay.value;
  if (scaledWrapper === undefined) return;

  const wh = scaledWrapper.clientHeight;
  const ch = scaledContent.clientHeight;
  const ww = scaledWrapper.clientWidth;
  const cw = scaledContent.clientWidth;

  // return Math.min(1.3);
  return Math.min(ww / cw, wh / ch);
}

function changeZoom(delta) {
  const computedZoom = computeAutoZoom();
  if (delta < 0 && zoom.value - 0.1 + delta * 0.01 < computedZoom) {
    autoZoom.value = true;
    zoom.value = computedZoom;
  } else {
    autoZoom.value = false;
    zoom.value = (Math.round(zoom.value * 10) + delta / 10) / 10;
  }
}

function isIntersecting(circle, line) {
  const r = 3;

  const v1 = {
    x: line.x2 - line.x1,
    y: line.y2 - line.y1
  };
  const v2 = {
    x: line.x1 - circle.x,
    y: line.y1 - circle.y
  };

  const b = (v1.x * v2.x + v1.y * v2.y) * -2;
  const c = 2 * (v1.x * v1.x + v1.y * v1.y);
  const d = Math.sqrt(b * b - 2 * c * (v2.x * v2.x + v2.y * v2.y - r * r));

  if (isNaN(d)) return false;

  const u1 = (b - d) / c;
  const u2 = (b + d) / c;

  if (u1 <= 1 && u1 >= 0) return true;
  return u2 <= 1 && u2 >= 0;
}

async function changePage(delta) {
  await setPage(currentPage.value + delta);
}

async function search(event) {
  const query = encodeURIComponent(event.target.value);
  const response = await fetch(`/api/books/search-completions?query=${query}`)
  searchResults.value = await response.json();
}

async function searchEnterPressed() {
  const firstResult = searchResults.value[0];
  if (firstResult) {
    await openBook(firstResult['book']['id'], firstResult['page']);
  }
}

function hideResults() {
  showResults.value = false;
}

function keyDown(event) {
  if (document.activeElement !== document.body) return;
  if (event.metaKey || event.shiftKey) return;

  switch (event.key) {
    case "ArrowRight":
      changePage(1);
      break;
    case "ArrowLeft":
      changePage(-1);
      break;
  }
}

function openSummary() {
  openPopup(SummaryPopup, appContext, {page: currentPage.value, book: currentBook.value["id"]});
}

async function loadBookFromUrl() {
  const params = new URL(location.href).searchParams;
  if (params.has("book")) {
    await openBook(params.get("book"), parseInt(params.get("page") || 1));
  } else {
    const item = localStorage.getItem("last-book");
    if (item !== null) {
      const lastBook = JSON.parse(item);
      await openBook(lastBook.book, lastBook.page);
    }
  }
}

loadBookFromUrl();

window.addEventListener("popstate", loadBookFromUrl, false);
window.addEventListener("keydown", keyDown);

onUnmounted(() => {
  window.removeEventListener("popstate", loadBookFromUrl, false);
  window.removeEventListener("keydown", keyDown);
});
</script>

<template>
  <div class="center" v-click-outside="hideResults">
    <div class="search" :style="{opacity: showControls ? 1 : 0, 'pointer-events': showControls ? 'all' : 'none'}">
      <input @input="search($event)"
             @keydown.enter="searchEnterPressed"
             placeholder="Bücher suchen"
             @focusin="showResults = true">

      <ul v-if="showResults">
        <li v-for="result in searchResults" @click="openBook(result['book']['id'], result['page'])">
          <a class="name">{{ result["book"]["title"] }}</a>
          <a class="subject">S. {{ result["page"] }}</a>
          <p class="subject">{{ result["book"]["subject"] }}</p>
        </li>
      </ul>
    </div>
  </div>

  <div style="height: calc(100vh - 100px); overflow: clip">
    <div class="book-wrapper"
         v-if="currentBook"
         ref="bookDisplay"
         @pointermove="refreshControlsFadeout"
         @pointerdown="refreshControlsFadeout">

      <div class="book" :style="{scale: zoom}" ref="htmlHolder">
        <canvas ref="liveCanvas"
                :class="{'no-pointer-events': !isDrawing}"
                @pointerdown="onMouseDown($event)"
                @pointermove="onMouseMove($event)"
                @pointerup="onMouseUp($event)"
                @pointercancel="cancelLine">
        </canvas>
        <canvas ref="canvasElement">
        </canvas>

        <div v-html="pageContent"></div>
      </div>
    </div>
  </div>
  <div v-if="currentBook" class="center" :style="{opacity: showControls ? 1 : 0, 'pointer-events': showControls ? 'all' : 'none'}">
    <Controls @change-page="changePage" @set-page="setPage" @change-zoom="changeZoom" @change-pencil="setPencil" @open-summary="openSummary"
              :book="currentBook"
              :page="currentPage"
              :zoom="zoomString">
    </Controls>
  </div>
</template>

<style scoped>
.search {
  position: absolute;
  background: var(--background-dark);
  width: calc(100vw - 20px);
  max-width: 300px;
  z-index: 12;
  border-radius: 10px;
  transition: opacity 0.4s;
}

.search ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.search li {
  color: var(--text);
  padding-top: 7px;
  padding-bottom: 7px;
  cursor: pointer;
}

.search li:hover {
  background: rgba(0, 0, 0, 0.15);
}

.search p {
  margin-top: 0;
  margin-bottom: 0;
}

.search .name {
  font-weight: bold;
  padding-left: 10px;
}

.search .subject {
  color: var(--text);
  font-size: 16px;
  padding-left: 10px;
}

.search input {
  background: var(--background);
  border: none;
  border-radius: 6px;
  color: var(--text);
  font-size: 16px;
  padding: 5px;
  margin: 10px;
  width: calc(100% - 20px);
}

.book-wrapper {
  display: flex;
  height: calc(100vh - 180px);
  justify-content: center;
}

.book {
  border: none;
  transform-origin: 50% 0;
  box-sizing: border-box;
  /*height: 10px;*/
  height: fit-content;
}

canvas {
  position: absolute;
  z-index: 5;
  touch-action: pinch-zoom;
}

canvas:last-of-type {
  pointer-events: none;
}

.page-content {
  /*width: fit-content;*/
  background: white;
}

.center {
  display: flex;
  flex-direction: row;
  justify-content: center;
  transition: opacity 0.4s;
}

.no-pointer-events {
  pointer-events: none;
}
</style>