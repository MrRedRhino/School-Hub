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

async function updateList() {
  const response = await fetch(`/api/todos`);
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
  const task = encodeURIComponent(newTask.value);
  const formattedDate = new Date(date.value).toISOString().split('T')[0];
  const taskDate = showDateInput.value ? encodeURIComponent(formattedDate) : "";

  newTask.value = "";
  await fetch(`/api/todos/?task=${task}&date-due=${taskDate}`, {
    method: "PUT"
  });
  await updateList();
}

async function completeTask(event, id) {
  const response = await fetch(`/api/todos/${id}`, {
    method: "DELETE"
  });

  if (response.status === 200) {
    setTimeout(() => {
      event.target.checked = false;
      updateList();
    }, 500);
  }
}

async function uncompleteTask(event, id) {
  const response = await fetch(`/api/todos/${id}`, {
    method: "PUT"
  });

  if (response.status === 200) {
    setTimeout(() => {
      event.target.checked = true;
      updateList();
    }, 500);
  }
}

async function editTodo(task) {
  closePopup();

  const taskString = encodeURIComponent(task.task);
  const taskDate = task.dateDue ? encodeURIComponent(task.dateDue) : "";
  await fetch(`/api/todos/${task.id}?task=${taskString}&date-due=${taskDate}`, {
    method: "PATCH"
  });
  await updateList();
}

function openBook(book, page) {
  window.open(`/books/?book=${book}&page=${page}`, "_blank");
}

updateList();
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
      <label class="container" :class="{'wide': task['referencedBook']}"  v-for="task in tasks" :for="task.id">
        <div>
          <a :class="{overdue: isOverdue(task.dateDue)}">{{ task.task }}</a>
          <a v-if="task.dateDue !== null" class="date">Bis {{ formatDate(task.dateDue) }}</a>
        </div>

        <button v-if="task['referencedBook']" class="edit-button"
                @click="openBook(task['referencedBook']['book-id'], task['referencedBook']['page'])">
          <svg width="32px" height="32px" viewBox="0 0 24 24" fill="none">
            <path
                d="M4 19V6.2C4 5.0799 4 4.51984 4.21799 4.09202C4.40973 3.71569 4.71569 3.40973 5.09202 3.21799C5.51984 3 6.0799 3 7.2 3H16.8C17.9201 3 18.4802 3 18.908 3.21799C19.2843 3.40973 19.5903 3.71569 19.782 4.09202C20 4.51984 20 5.0799 20 6.2V17H6C4.89543 17 4 17.8954 4 19ZM4 19C4 20.1046 4.89543 21 6 21H20M9 7H15M9 11H15M19 17V21"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <button class="edit-button" @click="openPopup(EditTodoPopup, appContext, {
            'current-date': currentDate,
            task: task,
            submit: editTodo
          })">
          <svg width="28px" height="28px" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <g>
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M20.8477 1.87868C19.6761 0.707109 17.7766 0.707105 16.605 1.87868L2.44744 16.0363C2.02864 16.4551 1.74317 16.9885 1.62702 17.5692L1.03995 20.5046C0.760062 21.904 1.9939 23.1379 3.39334 22.858L6.32868 22.2709C6.90945 22.1548 7.44285 21.8693 7.86165 21.4505L22.0192 7.29289C23.1908 6.12132 23.1908 4.22183 22.0192 3.05025L20.8477 1.87868ZM18.0192 3.29289C18.4098 2.90237 19.0429 2.90237 19.4335 3.29289L20.605 4.46447C20.9956 4.85499 20.9956 5.48815 20.605 5.87868L17.9334 8.55027L15.3477 5.96448L18.0192 3.29289ZM13.9334 7.3787L3.86165 17.4505C3.72205 17.5901 3.6269 17.7679 3.58818 17.9615L3.00111 20.8968L5.93645 20.3097C6.13004 20.271 6.30784 20.1759 6.44744 20.0363L16.5192 9.96448L13.9334 7.3787Z"
                    fill="currentColor"></path>
            </g>
          </svg>
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
  width: calc(100% - 34px);
}

.wide div {
  width: calc(100% - 68px);
}

.edit-button {
  color: var(--text);
  background: none;
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