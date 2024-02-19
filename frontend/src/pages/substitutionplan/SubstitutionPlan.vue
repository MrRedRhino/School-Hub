<script setup>
import PdfCard from "@/pages/substitutionplan/PdfCard.vue";
import {account} from "@/auth.js";
import Card from "@/pages/substitutionplan/Card.vue";

const htmlView = account.value ? account.value?.["settings"]?.["substitution-html-view"] : false;
</script>

<template>
  <div>
    <div v-if="htmlView">
      <PdfCard day="today"></PdfCard>
      <PdfCard day="tomorrow"></PdfCard>
    </div>

    <div v-else>
      <Card day="today"></Card>
      <Card day="tomorrow"></Card>
    </div>
  </div>
</template>

<style scoped>

</style>