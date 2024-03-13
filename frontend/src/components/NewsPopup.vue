<script setup>
import Popup from "@/components/Popup.vue";

const {data} = defineProps(["data"]);
</script>

<template>
  <Popup :title="data.title">
    <video v-if="data.video" :src="data.video" autoplay muted loop controls playsinline></video>

    <h2 class="news-description" v-html="data.description"></h2>
  </Popup>
</template>

<style scoped>
video {
  width: 100%;
}

h2 {
  white-space: pre-line;
}

ul {
  padding: 0;
}

li {
  list-style-type: circle;
}
</style>

<style>
.news-description ul {
  margin: 0;
  padding: 0;
  line-height: 0.5em;
}

.news-description li {
  list-style-type: "- ";
  margin-left: 10px;
  line-height: 1em;
}
</style>
