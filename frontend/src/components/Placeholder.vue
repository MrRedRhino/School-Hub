<script setup>

</script>

<template>
  <div style="width: 90%"></div>
  <div style="width: 80%"></div>
  <div style="width: 94%"></div>
  <div style="width: 85%"></div>
  <div style="width: 87%"></div>
  <div style="width: 97%"></div>
  <div style="width: 86%"></div>
  <div style="width: 50%"></div>
</template>

<style scoped>
div {
  height: 17px;
  background: linear-gradient(90deg, var(--text-dark) 1%, var(--text) 100%);
  background-size: 400%;
  margin-bottom: 10px;
  border-radius: 3px;
  animation: loading 1500ms ease-in-out infinite alternate;
}

@keyframes loading {
  from {
    background-position-x: 10%;
  }
  to {
    background-position-x: 100%;
  }
}
</style>