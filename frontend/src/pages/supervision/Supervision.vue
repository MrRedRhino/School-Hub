<script setup>
import {getCurrentInstance, nextTick, ref} from "vue";
import {openPopup} from "@/popup.js";
import AddSupervisionPopup from "@/pages/supervision/AddSupervisionPopup.vue";
import {requireAuth} from "@/auth.js";
import {formatName} from "@/main.js";

class NoTimeDate {
  year;
  month;
  day;

  constructor(year, month, day) {
    if (year instanceof Date) {
      this.year = year.getFullYear();
      this.month = year.getMonth();
      this.day = year.getDate();
    } else {
      this.year = year;
      this.month = month;
      this.day = day;
    }
  }

  toString() {
    return this.day + "." + this.month + "." + this.year;
  }
}

const {appContext} = getCurrentInstance();
const year = 2023;
const supervisions = ref({});
const floatingMonth = ref(null);
const currentMonthElement = ref();
const monthWrapper = ref();
const monthNames = [
  "Januar",
  "Februar",
  "März",
  "April",
  "Mai",
  "Juni",
  "Juli",
  "August",
  "September",
  "Oktober",
  "November",
  "Dezember"
];
const dayNames = [
  "So",
  "Mo",
  "Di",
  "Mi",
  "Do",
  "Fr",
  "Sa"
];

async function updateCalendar() {
  const newSupervisions = {};
  await fetch("/api/supervisions").then(r => r.json().then(json => {
    for (let key in json) {
      const split = key.split("-");
      const year = parseInt(split[0]);
      const month = parseInt(split[1])
      const day = parseInt(split[2]);

      const date = new NoTimeDate(year, month, day);
      newSupervisions[date] = json[key];
    }
  }));
  supervisions.value = newSupervisions;
}

function isCurrentDay(date) {
  const today = new Date();
  return date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate();
}

function getMonths() {
  const array = [];
  for (let i = 0; i < 10; i++) {
    array.push(i + 8);
  }
  return array;
}

function getPlaceholders(month) {
  const weekdayOffset = new Date(year, month, 1).getDay() - 1;
  return Math.max(0, weekdayOffset % 5);
}

function isWeekend(date) {
  return date.getDay() === 6 || date.getDay() === 0;
}

function getDates(month) {
  const array = [];
  const daysInMonth = new Date(year, month + 1, 0);
  for (let i = 0; i < daysInMonth.getDate(); i++) {
    array.push(new Date(year, month, i + 1));
  }
  return array;
}

function getSupervisors(date, breakNum) {
  const noTimeDate = new NoTimeDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
  let string = "";
  for (let name of supervisions.value[noTimeDate]?.[breakNum + ""] || []) {
    string += breakNum + ": " + formatName(name) + "\n";
  }
  return string;
}

function addSupervision(date) {
  requireAuth("dich einzutragen", appContext, () => {
    openPopup(AddSupervisionPopup, appContext, {
      date: new NoTimeDate(date.getFullYear(), date.getMonth() + 1, date.getDate()),
      supervisions: supervisions,
      updateCalendar: updateCalendar
    })
  })
}

document.body.onscroll = () => {
  const scrollTop = document.documentElement.scrollTop;
  const element = findCurrentMonthHeader(scrollTop);

  if (element.offsetTop - scrollTop < 0) {
    floatingMonth.value = element.innerText;
  } else {
    floatingMonth.value = null;
  }
};

function findCurrentMonthHeader(scroll) {
  let previousElement = null;
  for (let element of monthWrapper.value.getElementsByClassName("month-name")) {
    if (scroll < element.offsetTop - 120) {
      return previousElement;
    }
    previousElement = element;
  }
}

updateCalendar().then();
nextTick(() => {
  document.documentElement.scrollTop = currentMonthElement.value[0].offsetTop - 50;
});
</script>

<template>
  <div class="floating-month-name">
    <h1 :hidden="floatingMonth === null">{{ floatingMonth }}</h1>
  </div>

  <div ref="monthWrapper">
    <div class="month-wrapper" v-for="month in getMonths()">
      <a v-if="new Date(year, month).getMonth() === new Date().getMonth()"
         ref="currentMonthElement">{{ new Date(year, month).getMonth() }}</a>
      <h1 class="month-name">
        {{ monthNames[new Date(year, month).getMonth()] }} {{ new Date(year, month).getFullYear() }}</h1>
      <div class="days-wrapper">
        <div class="day-wrapper placeholder" v-for="i in getPlaceholders(month)">
        </div>

        <div class="day-wrapper"
             :class="{weekend: isWeekend(date), 'current-day': isCurrentDay(date)}"
             v-for="date in getDates(month)"
             @click="addSupervision(date)">
          <h1>{{ dayNames[date.getDay()] }} {{ date.getDate() }}</h1>

          <div class="data-wrapper">
            <h2>{{ getSupervisors(date, 1) }}</h2>
            <h2>{{ getSupervisors(date, 2) }}</h2>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.days-wrapper {
  flex-wrap: wrap;
  max-width: 950px;
  margin: auto;
  display: flex;
}

.day-wrapper {
  height: 170px;
  width: 170px;
  margin: 10px;
  background: var(--background-dark);
  padding: 10px;
  border-radius: 15px;
  color: var(--text);
  cursor: pointer;
  transition: scale 0.25s ease-in-out;
  white-space: pre;
}

.day-wrapper:hover {
  scale: 1.06;
}

.day-wrapper h1 {
  margin: 0 0 10px;
  user-select: none;
}

.day-wrapper h2 {
  margin: 0;
  font-size: 17px;
  color: var(--text);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
}

.placeholder {
  background: none;
  cursor: auto;
}

.current-day {
  background: var(--blue);
}

.weekend {
  display: none;
}

.data-wrapper {
  display: flex;
  flex-direction: column;
}

@media screen and (max-width: 970px) {
  .day-wrapper {
    height: 120px;
    width: 320px;
  }

  .days-wrapper {
    flex-direction: column;
  }

  .weekend {
    margin: 0;
    padding: 0;
    height: 15px;
    display: block;
    visibility: hidden;
  }

  .placeholder {
    margin: 0;
    padding: 0;
    height: 0;
  }

  .data-wrapper {
    display: flex;
    flex-direction: row;
  }

  .data-wrapper h2 {
    margin-right: 5px;
  }

  .header h1 {
    display: none;
  }
}

.month-name {
  text-align: center;
  color: var(--text);
  scroll-margin-top: 80px;
}

.floating-month-name {
  position: fixed;
  top: 60px;
  text-align: center;
  display: flex;
  align-items: center;
  width: 100%;
  flex-direction: column;
  pointer-events: none;
  z-index: 100;
}

.floating-month-name h1 {
  background: var(--background-dark);
  border-radius: 20px;
  padding: 10px;
  font-size: 23px;
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  color: var(--text);
}
</style>