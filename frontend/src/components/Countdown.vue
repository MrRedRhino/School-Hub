<script setup>
import {ref} from "vue";

const {date} = defineProps(["date"]);

const days = ref("0");
const hours = ref("0");
const minutes = ref("0");
const seconds = ref("0");
const ms = ref("000");

function padZeros(n) {
  if (n < 10) return "0" + n;
  return n;
}

function padMsZeroes(ms) {
  if (ms < 10) {
    return "00" + ms;
  }
  if (ms < 100) {
    return "0" + ms;
  }
  return ms;
}

function updateTime() {
  const diffMs = new Date(date) - new Date();
  const timeDifference = new Date(diffMs);

  days.value = padZeros(Math.round(diffMs / (1000 * 60 * 60 * 24)));
  hours.value = padZeros(timeDifference.getHours() - 1);
  minutes.value = padZeros(timeDifference.getMinutes());
  seconds.value = padZeros(timeDifference.getSeconds());
  ms.value = padMsZeroes(timeDifference.getMilliseconds());
}

setInterval(updateTime, 20);
updateTime();
</script>

<template>
  <div class="wrapper">
    <div class="unit">
      <h1>{{ days }}</h1>
      <h2>TAGE</h2>
    </div>
    <h1>:</h1>
    <div class="unit">
      <h1>{{ hours }}</h1>
      <h2>STD</h2>
    </div>
    <h1>:</h1>
    <div class="unit">
      <h1>{{ minutes }}</h1>
      <h2>MIN</h2>
    </div>
    <h1>:</h1>
    <div class="unit">
      <h1>{{ seconds }}</h1>
      <h2>SEC</h2>
    </div>
    <h1>:</h1>
    <div class="unit">
      <h1>{{ ms }}</h1>
      <h2>MS</h2>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin: 0;
  font-family: "JetBrains Mono", serif;
  font-weight: normal;
}

h2 {
  margin: 0;
  font-size: 16px;
  color: var(--text-dark);
}

.wrapper {
  display: flex;
  gap: 10px;
}

.unit {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>