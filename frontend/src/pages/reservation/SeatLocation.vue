<script setup>
const {location} = defineProps(["location"]);
</script>

<template>
  <div class="location-wrapper">
    <h2 class="key">Reihe:</h2>
    <h2 class="value">{{ location.row }}</h2>
    <h2 class="key">Platz:</h2>
    <h2 class="value">{{ location.seat }}</h2>
  </div>
</template>

<style scoped>
.location-wrapper {
  display: grid;
  grid-template-columns: min-content min-content;
  grid-gap: 3px 6px;
}

.location-wrapper h2 {
  margin: 0;
  font-size: 19px;
  color: var(--text);
}

.location-wrapper .key {
  font-weight: bold;
}

.location-wrapper .value {
  font-weight: normal;
}
</style>