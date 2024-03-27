<script setup>

import Widget from "@/pages/home/Widget.vue";
import Countdown from "@/components/Countdown.vue";
</script>

<template>
  <Widget title="Abi-Countdown">
    <h1>Französisch: </h1>
    <Countdown date="2024-04-22 08:15"></Countdown>

    <h1>Deutsch: </h1>
    <Countdown date="2024-04-25 08:15"></Countdown>

    <h1>3. Fach: </h1>
    <Countdown date="2024-05-03 08:15"></Countdown>

    <h1>Mathe: </h1>
    <Countdown date="2024-05-07 08:00"></Countdown>
  </Widget>
</template>

<style scoped>
h1 {
  font-size: 20px;
  margin-bottom: 0;
}
</style>