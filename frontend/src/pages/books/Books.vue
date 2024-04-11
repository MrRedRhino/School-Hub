<script setup>
import {computed, getCurrentInstance, nextTick, onUnmounted, ref, watch} from "vue";
import Controls from "@/pages/books/Controls.vue";
import {openPopup} from "@/popup.js";
import SummaryPopup from "@/components/SummaryPopup.vue";
import {activeElement, hoveredElement} from "@/utils.js";
import TextTools from "@/pages/books/TextTools.vue";

const htmlHolder = ref();
const bookDisplay = ref();
const pageContent = ref("");
const currentBook = ref(null);
const page = ref(0);
const currentPage = computed({
  get: () => page.value,
  set: newValue => page.value = Math.max(Math.min(newValue, currentBook.value["page-count"]), 1),
});

const zoomString = computed(() => autoZoom.value ? "Auto" : formatZoom(zoom.value));
const zoom = ref(1);
const autoZoom = ref(true);

const isDrawing = computed(() => activePencil.value);
const canvasElement = ref();
const liveCanvas = ref();
const lineWidth = 10;
const alpha = 0.4;
const activePencil = ref(null);
const annotations = ref([]);

const editedTextElement = ref();
const editedText = ref(null);
let dragMode = null;
const readonly = ref(false);

let lastMousePos = {x: null, y: null};
let mouseDown = false;
let hasMoved = false;
let start;
let saveTimeout = null;

let fadeoutTimeoutId = null;
const controlsFadedIn = ref(true);
const controlsVisible = computed(() => {
  return controlsFadedIn.value || activeElement.value?.tagName === "INPUT" || !currentBook.value || controls.value.contains(hoveredElement.value);
});
const searchResults = ref([]);
const query = ref("");
const showResults = ref(false);
const controls = ref();

const {appContext} = getCurrentInstance();

function refreshControlsFadeout() {
  controlsFadedIn.value = true;
  clearTimeout(fadeoutTimeoutId);

  if (currentBook.value) {
    fadeoutTimeoutId = setTimeout(() => {
      controlsFadedIn.value = false;
    }, 5000);
  }
}

function scheduleSave() {
  clearTimeout(saveTimeout);
  saveTimeout = setTimeout(() => saveAnnotations(currentPage.value), 3000);
}

function runPendingSaveNow(page) {
  if (saveTimeout !== null) {
    clearTimeout(saveTimeout);
    saveAnnotations(page);
  }
}

function formatZoom(zoom) {
  return Math.round(zoom * 10) * 10 + "%";
}

async function openBook(id, page = 1) {
  const book = await fetch(`/api/books/${id}`);
  if (book.status > 200) return;
  runPendingSaveNow(currentPage.value);

  currentBook.value = await book.json();

  if (currentBook.value === null || currentBook.value.id + "" !== id) {
    history.pushState(null, "", `/books?book=${currentBook.value.id}&page=${page}`);
  }

  currentPage.value = page;
  hideResults();
}

async function setPage(page) {
  runPendingSaveNow();
  page = Math.max(Math.min(page, currentBook.value["page-count"]), 1);

  const html = await fetch(`/api/books/${currentBook.value["id"]}/${page}`);
  pageContent.value = await html.text();
  history.replaceState(history.state, "", `/books?book=${currentBook.value.id}&page=${page}`);
  localStorage.setItem("last-book", JSON.stringify({"book": currentBook.value.id, page: page}))

  await nextTick(() => {
    applyAutozoom();
    pageChanged();
  });
}

function onMouseDown(event) {
  if (!controlsFadedIn.value || isDrawing.value) {
    refreshControlsFadeout();
  } else {
    clearTimeout(fadeoutTimeoutId);
    controlsFadedIn.value = false;
  }
  mouseDown = true;
  lastMousePos = {
    x: event.clientX,
    y: event.clientY
  };
  hasMoved = false;

  if (!isDrawing.value) return;

  start = {
    x: event.offsetX,
    y: event.offsetY,
  };
}

function onMouseUp(event) {
  mouseDown = false;
  dragMode = null;

  if (start && activePencil.value && activePencil.value !== "eraser") {
    addLine(event.offsetX, event.offsetY);
    start = null;
  }
}

function addLine(endX, endY) {
  annotations.value.push({
    t: "l",
    x1: start.x,
    y1: start.y,
    x2: endX,
    y2: endY,
    c: activePencil.value
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

  for (let line of annotations.value) {
    if (line.t === "l") {
      ctx.strokeStyle = line.c;

      ctx.beginPath();
      ctx.moveTo(line.x1, line.y1);
      ctx.lineTo(line.x2, line.y2);
      ctx.stroke();
    }
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

  const movementX = event.clientX - lastMousePos.x;
  const movementY = event.clientY - lastMousePos.y;

  if (editedText.value) {
    if (dragMode === "left") {
      editedText.value.x += movementX / zoom.value;
      editedText.value.w += -movementX / zoom.value;
      textAreaInput(editedTextElement.value);
      scheduleSave();
    } else if (dragMode === "right") {
      editedText.value.w += movementX / zoom.value;
      textAreaInput(editedTextElement.value);
      scheduleSave();
    } else if (dragMode === "center") {
      editedText.value.x += movementX / zoom.value;
      editedText.value.y += movementY / zoom.value;
      readonly.value = true;
      scheduleSave();
    }
  } else if (activePencil.value === "eraser") {
    erase(pos);
    scheduleSave();
  } else if (activePencil.value !== null) {
    drawPreview(pos);
    scheduleSave();
  }

  lastMousePos = {
    x: event.clientX,
    y: event.clientY
  };
  hasMoved = true;
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
  annotations.value = annotations.value.filter(item => item.t !== "l" || !isIntersecting(pos, item));
  redrawCanvas();
}

function configureContext(ctx) {
  ctx.lineWidth = lineWidth;
  ctx.strokeStyle = activePencil.value;
  ctx.globalAlpha = alpha;
}

async function saveAnnotations(page, keepalive = false) {
  saveTimeout = null;

  const annotationsCopy = annotations.value.slice();
  if (editedText.value) {
    annotationsCopy.push(editedText.value);
  }

  await fetch(`/api/books/${currentBook.value["id"]}/${page}/annotations`, {
    method: "PUT",
    body: JSON.stringify(annotationsCopy),
    keepalive: keepalive
  });
}

async function pageChanged() {
  const width = htmlHolder.value.clientWidth;
  const height = htmlHolder.value.clientHeight;
  canvasElement.value.width = width;
  liveCanvas.value.width = width;
  canvasElement.value.height = height;
  liveCanvas.value.height = height;

  const response = await fetch(`/api/books/${currentBook.value["id"]}/${currentPage.value}/annotations`);
  const newAnnotations = await response.json();
  annotations.value = [];

  newAnnotations.forEach(item => {
    if (item.t === "l") {
      annotations.value.push({
        t: item.t || "l",
        x1: item.x1,
        y1: item.y1,
        x2: item.x2,
        y2: item.y2,
        c: item.c || item.color
      });
    } else {
      annotations.value.push({
        t: item.t || "t",
        x: item.x,
        y: item.y,
        c: item.c,
        v: item.v,
        w: item.w,
        s: item.s
      });
    }
  });

  redrawCanvas();
  refreshControlsFadeout();
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

async function search() {
  const encodedQuery = encodeURIComponent(query.value);
  const response = await fetch(`/api/books/search-completions?query=${encodedQuery}`)
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
      currentPage.value++;
      break;
    case "ArrowLeft":
      currentPage.value--;
      break;
    case "Backspace":
      if (editedText.value) {
        deleteCurrentText();
      }
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
// window.addEventListener("pointermove", onMouseMove);
// window.addEventListener("pointerdown", onMouseDown);
// window.addEventListener("pointerup", onMouseUp);
// window.addEventListener("pointercancel", cancelLine);

onUnmounted(() => {
  window.removeEventListener("popstate", loadBookFromUrl, false);
  window.removeEventListener("keydown", keyDown);
  // window.removeEventListener("pointermove", onMouseMove);
  // window.removeEventListener("pointerdown", onMouseDown);
  // window.removeEventListener("pointerup", onMouseUp);
  // window.removeEventListener("pointercancel", cancelLine);
});

function deselectEditedText() {
  if (editedText.value.v.trim() === "") {
    editedText.value.v = "Text bearbeiten";
  }
  annotations.value.push(editedText.value);
  editedText.value = null;
  dragMode = null;
  scheduleSave();
}

function editText(text) {
  annotations.value.splice(annotations.value.indexOf(text), 1);
  editedText.value = text;
  activePencil.value = null;
  readonly.value = true;
  nextTick(() => textAreaInput(editedTextElement.value));
}

function startDragging(target) {
  if (target.getAttribute("drag-side") !== "center" || readonly.value) {
    dragMode = target.getAttribute("drag-side");
  }
}

function endDragging(target) {
  if (target.getAttribute("drag-side") === 'center' && !hasMoved) {
    readonly.value = false;
  }
}

function textEdited(newValue) {
  editedText.value.v = newValue;
  scheduleSave();
}

function createNewText() {
  const width = htmlHolder.value.clientWidth;
  const height = htmlHolder.value.clientHeight;
  const textWidth = 126;

  const newText = {
    t: "t",
    x: (width - textWidth) / 2,
    y: height / 2 - 8,
    c: "#000",
    v: "Text bearbeiten",
    w: textWidth,
    s: 16
  };
  annotations.value.push(newText);
  editText(newText);
}

function deleteCurrentText() {
  deselectEditedText();
  annotations.value.splice(annotations.value.indexOf(editedText.value), 1);
}

function textAreaInput(element) {
  element.style.height = "";
  element.style.height = element.scrollHeight + "px";
  textEdited(element.value);
}

watch(currentPage, (value, oldValue) => {
  runPendingSaveNow(oldValue);
  return setPage(value);
});
</script>

<template>
  <div class="center" v-click-outside="hideResults">
    <div class="search" :style="{opacity: controlsVisible ? 1 : 0, 'pointer-events': controlsVisible ? 'all' : 'none'}">
      <input @input="search()"
             v-model="query"
             @keydown.enter="searchEnterPressed"
             placeholder="Bücher suchen"
             @focusin="showResults = true">

      <ul v-if="showResults">
        <li v-for="result in searchResults" @click="openBook(result['book']['id'], result['page'])">
          <a class="name">{{ result["book"]["title"] }}</a>
          <a class="subject">S. {{ result["page"] }}</a>
          <p class="subject">{{ result["book"]["subject"] }}</p>
        </li>
        <!--        <li class="upload-suggestion" v-if="searchResults.length <= 2 && query !== ''">-->
        <!--          <a class="name">Buch nicht gefunden?</a>-->
        <!--          <p class="subject">Drücke hier, um es hochzuladen</p>-->
        <!--        </li>-->
      </ul>
    </div>
  </div>

  <div class="book-wrapper"
       v-if="currentBook"
       ref="bookDisplay"
       @pointermove="onMouseMove"
       @pointerdown="onMouseDown"
       @pointerup="onMouseUp"
       @pointercancel="cancelLine">

    <div class="book" :style="{scale: zoom}" ref="htmlHolder">
      <canvas ref="liveCanvas" :class="{'no-pointer-events': !isDrawing}">
      </canvas>
      <canvas ref="canvasElement">
      </canvas>
      <div class="text-annotations">
        <a :style="{transform: `translate(${text.x}px, ${text.y}px)`, 'font-size': `${text.s}px`, color: text.c, width: `${text.w}px`} "
           v-for="text in annotations.filter(a => a.t === 't')"
           @click="editText(text)">
          {{ text.v }}
        </a>

        <a v-if="editedText" class="editing" v-click-outside="deselectEditedText"
           :style="{transform: `translate(${editedText.x - 1}px, ${editedText.y - 1}px)`, width: `${editedText.w}px`}"
           @pointerdown="startDragging($event.target)"
           @pointerup="endDragging($event.target)">

          <TextTools v-if="editedText" :text="editedText" @delete="deleteCurrentText"
                     @resize="textAreaInput(editedTextElement)"></TextTools>

          <textarea drag-side="center" :style="{'font-size': `${editedText.s}px`, color: editedText.c}"
                    ref="editedTextElement" :readonly="readonly" rows="1" @input="textAreaInput($event.target)"
                    :value="editedText.v"></textarea>

          <div drag-side="left" class="drag left"></div>
          <div drag-side="right" class="drag right"></div>
        </a>
      </div>
      <div v-html="pageContent"></div>
    </div>
  </div>
  <div ref="controls" v-if="currentBook" class="center"
       :style="{opacity: controlsVisible ? 1 : 0, 'pointer-events': controlsVisible ? 'all' : 'none'}">
    <Controls @change-zoom="changeZoom"
              @open-summary="openSummary"
              @add-text="createNewText"
              v-model:active-pencil="activePencil"
              :book="currentBook"
              v-model:page="currentPage"
              :zoom="zoomString">
    </Controls>
  </div>
</template>

<style scoped>
.search {
  position: fixed;
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

.search .upload-suggestion {
  color: var(--text-dark);
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
  height: calc(100dvh - 90px);
  justify-content: center;
  overflow: hidden;
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

/*noinspection CssUnusedSymbol*/
.page-content {
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

.text-annotations a {
  position: absolute;
  z-index: 100;
  padding-left: 7px;
  padding-right: 7px;
  overflow-wrap: anywhere;
}

.text-annotations .editing {
  border: 1px solid cornflowerblue;
}

textarea {
  padding: 0;
  resize: none;
  border: none;
  width: 100%;
  background: none;
  touch-action: none;
}

.text-annotations .editing .drag {
  border-radius: 50%;
  border: none;
  position: absolute;
  top: 50%;
  width: 10px;
  height: 10px;
  background: cornflowerblue;
  cursor: col-resize;
  touch-action: none;
}

.text-annotations .editing .drag.left {
  left: 0;
  transform: translate(-50%, -50%);
}

.text-annotations .editing .drag.right {
  right: 0;
  transform: translate(50%, -50%);
}
</style>