<script setup>
import {ref} from "vue";

const checked = defineModel();
const emit = defineEmits(["change"]);
const disabled = defineProps(["disabled"]);

function change(event) {
  emit("change", event);
}
</script>

<template>
  <label class="switch">
    <input type="checkbox" id="theme-toggle" v-model="checked" @change="change($event)" :disabled="disabled === true ? true : null">
    <span class="slider round"></span>
  </label>
</template>

<style scoped>
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 28px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--background-dark);
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
}

input:checked + .slider {
  background-color: var(--blue);
}

input:focus + .slider {
  box-shadow: 0 0 1px var(--blue);
}

input:checked + .slider:before {
  transform: translateX(24px);
}

.slider.round {
  border-radius: 32px;
}

.slider.round:before {
  border-radius: 50%;
}
</style>