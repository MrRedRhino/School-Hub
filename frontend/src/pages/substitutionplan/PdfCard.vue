<script setup>
import {computed, ref, watch} from "vue";

const props = defineProps(["day", "clazz"]);
const clazzRef = computed(() => props.clazz);
const data = ref("");
const error = ref(false);

function loadPlan() {
  error.value = false;
  data.value = "";
  fetch(`/api/plans/${encodeURIComponent(props.clazz)}/${props.day}?format=html`)
      .then(r => r.text().then(text => data.value = text))
      .catch(() => error.value = true);
}

watch(clazzRef, () => {
  loadPlan();
});

loadPlan();
</script>

<template>
  <div class="plan" v-if="data" v-html="data">
  </div>

  <div v-else class="loading">
    <img src="/src/assets/loading-137px.gif" alt="Ladeanimation">
    <h1>Lädt...</h1>
  </div>
</template>

<style scoped>
.plan {
  transform: translate(-35px, -45px);
  zoom: 0.8;
  height: 1050px;
}

.loading {
  text-align: center;
}

h1 {
  font-size: 18px;
  color: var(--text);
}
</style>