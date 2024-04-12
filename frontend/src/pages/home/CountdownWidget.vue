<script setup>
import Widget from "@/pages/home/Widget.vue";
import Countdown from "@/components/Countdown.vue";
</script>

<template>
  <Widget title="Abi-Countdown">
    <h1>Französisch: <a>(22.04.2024)</a></h1>
    <Countdown date="2024-04-22 08:15"></Countdown>

    <h1>Deutsch: <a>(25.04.2024)</a></h1>
    <Countdown date="2024-04-25 08:15"></Countdown>

    <h1>3. Fach: <a>(03.05.2024)</a></h1>
    <Countdown date="2024-05-03 08:15"></Countdown>

    <h1>Mathe: <a>(07.05.2024)</a></h1>
    <Countdown date="2024-05-07 08:00"></Countdown>
  </Widget>
</template>

<style scoped>
h1 {
  font-size: 20px;
  margin-bottom: 0;
}

a {
  color: var(--text-mute);
  font-weight: normal;
}
</style>