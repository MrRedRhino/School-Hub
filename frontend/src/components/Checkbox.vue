<script setup>
const emit = defineEmits(["change"]);

function change(event) {
  emit("change", event.target.checked);
}
</script>

<template>
  <label class="container">
    <slot></slot>
    <input type="checkbox" @change="change($event)">
    <span class="checkmark"></span>
  </label>
</template>

<style scoped>
.container {
  display: block;
  position: relative;
  padding-left: 35px;
  margin-bottom: 12px;
  cursor: pointer;
  font-size: 22px;
  user-select: none;
}

.container input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: absolute;
  top: 0;
  left: 0;
  height: 25px;
  width: 25px;
  background-color: var(--background);
  border-radius: 6px;
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.container input:checked ~ .checkmark:after {
  display: block;
}

.container .checkmark:after {
  left: 9px;
  top: 5px;
  width: 5px;
  height: 10px;
  border: solid var(--text);
  border-width: 0 3px 3px 0;
  transform: rotate(45deg);
}
</style>