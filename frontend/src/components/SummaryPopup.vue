<script setup>
import Popup from "@/components/Popup.vue";
import {ref} from "vue";

const {page, book} = defineProps(["page", "book"]);
const summaryText = ref("Einen Moment bitte...");

fetch(`/api/books/${book}/${page}/summary`).then(r => r.text().then(text => {
  summaryText.value = text;
}));
</script>

<template>
  <Popup title="Zusammenfassung">
    <h2>{{ summaryText }}</h2>
  </Popup>
</template>

<style scoped>
 h2 {
   background: var(--background);
   color: var(--text);
   border-radius: 10px;
   padding: 7px;
   overflow: scroll;
   min-height: 200px;
   max-height: calc(100vh - 200px);
 }
</style>