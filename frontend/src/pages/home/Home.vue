<script setup>
import SubstitutionWidget from "@/pages/home/SubstitutionWidget.vue";
import TodoWidget from "@/pages/home/TodoWidget.vue";
import CountdownWidget from "@/pages/home/CountdownWidget.vue";
</script>

<template>
  <div class="widgets">
    <TodoWidget></TodoWidget>
    <SubstitutionWidget></SubstitutionWidget>
    <CountdownWidget></CountdownWidget>
  </div>
</template>

<style scoped>
.widgets {
  width: 100vw;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.widgets * {
  margin-bottom: 15px;
  width: min(500px, calc(100% - 20px));
}
</style>