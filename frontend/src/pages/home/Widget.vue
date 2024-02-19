<script setup>

const {title} = defineProps(["title"]);
</script>

<template>
  <div class="wrapper">
    <h1>{{ title }}</h1>
    <slot></slot>
  </div>
</template>

<style scoped>
.wrapper {
  border-radius: 20px;
  background: var(--background-dark);
  max-width: 500px;
  padding: 10px 10px 10px 15px;
  color: var(--text);
}

h1 {
  color: var(--blue);
  font-weight: normal;
  font-size: 24px;
  margin: 0 0 10px;
}
</style>