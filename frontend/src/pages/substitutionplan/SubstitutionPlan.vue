<script setup>
import PdfCard from "@/pages/substitutionplan/PdfCard.vue";
import {account, settings} from "@/auth.js";
import Card from "@/pages/substitutionplan/Card.vue";
import {openPopup} from "@/popup.js";
import SelectClassPopup from "@/pages/substitutionplan/SelectClassPopup.vue";
import {getCurrentInstance, ref} from "vue";

const {appContext} = getCurrentInstance();
const htmlView = account.value ? settings["substitution-html-view"] : false;
const clazz = ref(account.value ? settings["class"] : localStorage.getItem("substitution-class"));

function updateClass() {
  clazz.value = account.value ? settings["class"] : localStorage.getItem("substitution-class");
}
</script>

<template>
  <div class="class-menu">
    <h2>Ausgewählter Plan: <strong>{{ clazz }}</strong> <a
        @click="openPopup(SelectClassPopup, appContext, {onClose: updateClass})">
      Ändern</a>
    </h2>
  </div>

  <div v-if="clazz">
    <div v-if="htmlView">
      <PdfCard :clazz="clazz" day="today"></PdfCard>
      <PdfCard :clazz="clazz" day="tomorrow"></PdfCard>
    </div>

    <div v-else>
      <Card :clazz="clazz" day="today"></Card>
      <Card :clazz="clazz" day="tomorrow"></Card>
    </div>
  </div>
</template>

<style scoped>
h2 {
  font-weight: normal;
  font-size: 18px;
  color: var(--text);
}

a {
  color: cornflowerblue;
  cursor: pointer;
}

.class-menu {
  margin-left: 15px;
}
</style>