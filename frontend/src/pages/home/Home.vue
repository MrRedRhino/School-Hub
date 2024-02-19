<script setup>
import SubstitutionWidget from "@/pages/home/SubstitutionWidget.vue";
import TodoWidget from "@/pages/home/TodoWidget.vue";
</script>

<template>
  <div class="widgets">
    <TodoWidget></TodoWidget>
    <SubstitutionWidget></SubstitutionWidget>
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