<script setup>
import Popup from "@/components/Popup.vue";
import {ref} from "vue";
import {closePopup} from "@/popup.js";

const {currentDate, task, submit} = defineProps(["current-date", "task", "submit"]);

const newDate = ref(task.dateDue || currentDate);
const dateDueSet = ref(task.dateDue !== null);
const newTask = ref(task.task);

function doSubmit() {
  task.dateDue = dateDueSet.value ? newDate.value : null;
  task.task = newTask.value;
  submit(task);
}
</script>

<template>
  <Popup title="To-Do bearbeiten">
    <h2>Aufgabe:</h2>
    <input v-model="newTask">

    <label class="container">
      <a>Abgabe:</a>
      <input type="checkbox" v-model="dateDueSet">
      <span class="checkmark"></span>
    </label>
    <input :class="{enabled: dateDueSet}"
           :disabled="!dateDueSet"
           type="date"
           :min="currentDate"
           v-model="newDate">

    <div class="action-row">
      <button @click="closePopup()">Abbrechen</button>
      <button @click="doSubmit">Ok</button>
    </div>
  </Popup>
</template>

<style scoped>
.container {
  position: relative;
  padding-left: 29px;
  margin-bottom: 10px;
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

input[type=date] {
  width: fit-content;
  margin-bottom: 0;
  color: var(--text-dark);
  margin-top: 15px;
  padding-left: unset;
}

input[type=date].enabled {
  width: fit-content;
  color: var(--text);
  margin-bottom: 10px;
}

a {
  font-weight: normal;
  margin-bottom: 3px;
  margin-left: 2px;
  font-size: 19px;
  margin-right: 5px;
}
</style>