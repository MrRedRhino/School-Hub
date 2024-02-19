<script setup>
import Widget from "@/pages/home/Widget.vue";
import SubstitutionWidgetCard from "@/pages/home/SubstitutionWidgetCard.vue";
import {ref} from "vue";

const dates = ["", ""];
const title = ref("Vertretungsplan");
const scroll = ref(0);

function setDateToday(date) {
  dates[0] = date;
  updateScroll(0);
}

function setDateTomorrow(date) {
  dates[1] = date;
}

function onScroll(event) {
  const target = event.target;
  const maxScroll = target.scrollWidth - target.clientWidth;
  const pos = Math.round(target.scrollLeft / maxScroll);
  updateScroll(pos);
}

function updateScroll(newScroll) {
  title.value = `Vertretungsplan ${dates[newScroll]}`;
  scroll.value = newScroll;
}
</script>

<template>
  <Widget :title="title">
    <div class="cards" @scroll="onScroll($event)">
      <div class="card">
        <SubstitutionWidgetCard @date="setDateToday" day="today"></SubstitutionWidgetCard>
      </div>
      <div class="card">
        <SubstitutionWidgetCard @date="setDateTomorrow" day="tomorrow"></SubstitutionWidgetCard>
      </div>
    </div>

    <div class="dots-wrapper">
      <div class="dots">
        <div :class="{dot: true, active: scroll === 0}"></div>
        <div :class="{dot: true, active: scroll === 1}"></div>
      </div>
    </div>
  </Widget>
</template>

<style scoped>
.card {
  min-width: 100%;
  scroll-snap-align: start;
}

.cards {
  display: flex;
  flex-direction: row;
  overflow-x: scroll;
  scroll-snap-type: x mandatory;
  padding-bottom: 10px;
}

.dots-wrapper {
  display: flex;
  align-items: center;
  flex-direction: column;
}

.dot {
  height: 10px;
  width: 10px;
  border-radius: 100%;
  background: var(--text-dark);
}

.dot.active {
  background: var(--text);
}

.dots {
  display: flex;
  flex-direction: row;
  gap: 6px;
}
</style>