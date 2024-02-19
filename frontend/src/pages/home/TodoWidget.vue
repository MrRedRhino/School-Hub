<script setup>
import Widget from "@/pages/home/Widget.vue";
import {getCurrentInstance, ref} from "vue";
import {closePopup, openPopup} from "@/popup.js";
import EditTodoPopup from "@/components/EditTodoPopup.vue";
import {account, openLoginPage} from "@/auth.js";

const tasks = ref([]);
const completedTasks = ref([]);
const showCompletedTodos = ref(false);

const currentDate = ref(new Date().toISOString().split('T')[0]);
const showDateInput = ref(false);
const newTask = ref("");
const date = ref(currentDate);
const {appContext} = getCurrentInstance();

async function loadTasks() {
  const response = await fetch(`https://hub.pipeman.org/api/todos`);
  const body = await response.json();

  tasks.value = body["todos"];
  completedTasks.value = body["completedTodos"];
}

function formatDate(date) {
  const parsedDate = new Date(date);
  const dayOfWeek = parsedDate.getDay();
  const weekdays = ["So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"];

  const mm = parsedDate.getMonth() + 1;
  const dd = parsedDate.getDate();

  return `${weekdays[dayOfWeek]}, ${(dd > 9 ? '' : '0') + dd}.${(mm > 9 ? '' : '0') + mm}.${parsedDate.getFullYear()}`;
}

function isOverdue(date) {
  if (date === null) return false;

  const parsedDate = new Date(date);
  const currentDate = new Date();

  currentDate.setHours(0, 0, 0, 0);
  parsedDate.setHours(0, 0, 0, 0);

  return parsedDate <= currentDate;
}

async function addTask() {
  const rawTask = newTask.value;
  const task = encodeURIComponent(newTask.value);
  const formattedDate = new Date(date.value).toISOString().split('T')[0];
  const taskDate = showDateInput.value ? encodeURIComponent(formattedDate) : "";

  newTask.value = "";
  const response = await fetch(`https://hub.pipeman.org/api/todos/?task=${task}&date-due=${taskDate}`, {
    method: "PUT"
  });

  const json = await response.json();
  tasks.value.unshift({
    "id": json["id"],
    "task": rawTask,
    "dateDue": showDateInput.value ? formattedDate : null,
    "dateDone": null
  });
}

async function completeTask(event, id) {
  const response = await fetch(`https://hub.pipeman.org/api/todos/${id}`, {
    method: "DELETE"
  });

  if (response.status === 200) {
    setTimeout(() => {
      event.target.checked = false;
      moveTodo(tasks.value, completedTasks.value, id);
    }, 500);
  }
}

async function uncompleteTask(event, id) {
  const response = await fetch(`https://hub.pipeman.org/api/todos/${id}`, {
    method: "PUT"
  });

  if (response.status === 200) {
    setTimeout(() => {
      event.target.checked = true;
      moveTodo(completedTasks.value, tasks.value, id);
    }, 500);
  }
}

function moveTodo(sourceList, targetList, id) {
  for (let i = 0; i < sourceList.length; i++) {
    const task = sourceList[i];
    if (task.id === id) {
      task["dateDone"] = null;

      targetList.unshift(task);
      sourceList.splice(i, 1);
      break;
    }
  }
}

async function editTodo(task) {
  closePopup();

  const taskString = encodeURIComponent(task.task);
  const taskDate = task.dateDue ? encodeURIComponent(task.dateDue) : "";
  await fetch(`https://hub.pipeman.org/api/todos/${task.id}?task=${taskString}&date-due=${taskDate}`, {
    method: "PATCH"
  });
}

loadTasks();
</script>

<template>
  <Widget v-if="account" title="To-Do">
    <div class="add-task-wrapper">
      <input type="text" placeholder="Neue Aufgabe..." v-model.trim="newTask">
      <button :disabled="newTask.length === 0" @click="addTask">+</button>
    </div>
    <div class="add-task-wrapper" v-if="newTask.length > 0">
      <label class="container">
        <a>Abgabe:</a>
        <input type="checkbox" v-model="showDateInput">
        <span class="checkmark"></span>
      </label>
      <input :class="{enabled: showDateInput}"
             :disabled="!showDateInput"
             type="date"
             :min="currentDate"
             v-model="date">
    </div>
    <hr>

    <div class="tabs">
      <h2 :class="{active: showCompletedTodos}" @click="showCompletedTodos = false">Unerledigt</h2>
      <h2 :class="{active: !showCompletedTodos}" @click="showCompletedTodos = true">Erledigt</h2>
    </div>

    <div class="list" v-if="!showCompletedTodos">
      <label class="container" v-for="task in tasks" :for="task.id">
        <div>
          <a :class="{overdue: isOverdue(task.dateDue)}">{{ task.task }}</a>
          <a v-if="task.dateDue !== null" class="date">Bis {{ formatDate(task.dateDue) }}</a>
        </div>

        <button class="edit-button" @click="openPopup(EditTodoPopup, appContext, {
            'current-date': currentDate,
            task: task,
            submit: editTodo
          })">
        </button>

        <input :id="task.id" type="checkbox" @change="completeTask($event, task.id)">
        <span class="checkmark"></span>
      </label>

      <h1 v-if="tasks.length === 0">Kein To-Do 🎉</h1>
    </div>

    <div class="list" v-if="showCompletedTodos">
      <label class="container" v-for="task in completedTasks" @change="completeTask(task.id)">
        <div>
          <a :class="{overdue: isOverdue(task.dateDue)}">{{ task.task }}</a>
          <a v-if="task.dateDone !== null" class="date">Erledigt am {{ formatDate(task.dateDone) }}</a>
        </div>

        <input type="checkbox" checked="checked" @change="uncompleteTask($event, task.id)">
        <span class="checkmark"></span>
      </label>
      <h1 v-if="completedTasks.length === 0">Kein To-Do 🎉</h1>
    </div>
  </Widget>

  <Widget v-else title="To-Do">
    <button @click="openLoginPage()" class="not-logged-in">Melde Dich an, um To-Dos zu speichern</button>
  </Widget>
</template>

<style scoped>
.not-logged-in {
  font-size: 18px;
  padding-left: 10px;
  padding-right: 10px;
}

.container div {
  display: flex;
  flex-direction: column;
  width: calc(100% - 30px)
}

.edit-button {
  color: var(--text);
  background-image: url("@/assets/edit-icon.svg");
  background-repeat: no-repeat;
  background-size: 70%;
  background-position: center;
  background-color: transparent;
  width: 30px;
  height: 30px;
  margin: 0 0 0 auto;
  transition-duration: 0.1s;
}

.edit-button:active {
  scale: 0.9;
}

.tabs {
  display: flex;
  justify-content: space-evenly;
  margin-bottom: 13px;
}

.tabs h2 {
  font-weight: normal;
  margin: 0;
  font-size: 18px;
  color: var(--text-dark);
  cursor: pointer;
}

.tabs h2.active {
  color: var(--text);
}

.add-task-wrapper {
  display: flex;
  gap: 10px;
}

input {
  background: var(--background);
  border: none;
  border-radius: 10px;
  height: 34px;
  font-size: 16px;
  color: var(--text);
  padding-left: 10px;
  width: calc(100% - 10px);
  margin-bottom: 10px;
}

h1 {
  font-size: 20px;
  margin-bottom: 0;
}

input[type=date] {
  width: fit-content;
  margin-bottom: 0;
  color: var(--text-dark);
}

input[type=date].enabled {
  width: fit-content;
  margin-bottom: 0;
  color: var(--text);
}

button {
  background: var(--blue);
  border: none;
  border-radius: 10px;
  height: 34px;
  font-size: 25px;
  color: var(--text);
  min-width: 34px;
  margin-bottom: 10px;
  cursor: pointer;
  text-align: center;
  padding: 0;
}

button:disabled {
  background-color: var(--background);
  color: var(--text-dark);
}

a {
  font-size: 18px;
  display: inline;
}

.list a {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.date {
  display: block;
  font-size: 14px;
  color: var(--text-dark);
}

.overdue {
  color: #ff2549;
}

.container {
  display: flex;
  position: relative;
  padding-left: 35px;
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
</style>