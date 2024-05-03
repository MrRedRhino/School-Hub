<script setup>
import Popup from "@/components/Popup.vue";
import {ref} from "vue";
import Placeholder from "@/components/Placeholder.vue";

const {page, book} = defineProps(["page", "book"]);
const summaryText = ref("");

fetch(`/api/books/${book}/${page}/summary`).then(r => r.text().then(text => {
  summaryText.value = text;
}));
</script>

<template>
  <Popup title="Zusammenfassung">
    <div class="content">
      <Placeholder v-if="summaryText === ''"/>
      {{ summaryText }}
    </div>
  </Popup>
</template>

<style scoped>
.content {
  background: var(--background);
  color: var(--text);
  border-radius: 10px;
  padding: 7px;
  overflow: scroll;
  min-height: 200px;
  max-height: calc(100vh - 200px);
  white-space: pre-wrap;
}
</style>