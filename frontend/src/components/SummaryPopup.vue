<script setup>
import Popup from "@/components/Popup.vue";
import {ref} from "vue";

const {page, book} = defineProps(["page", "book"]);
const summaryText = ref("Einen Moment bitte...");

fetch(`https://hub.pipeman.org/api/books/${book}/${page}/summary`).then(r => r.text().then(text => {
  // summaryText.value = text;
  summaryText.value = "Im Forum Geschichte 12 Bayern wurde über die Geschichte Bayerns diskutiert. Es wurde auf die Bedeutung Bayerns in der Vergangenheit eingegangen und die Frage nach der eigenen Identität gestellt. Es wurden verschiedene Themen wie die Wittelsbacher Dynastie, die bayerische Monarchie und die Rolle Bayerns im Nationalsozialismus behandelt. Die Diskussionsteilnehmer zeigten Interesse an der Geschichte Bayerns und tauschten ihr Wissen aus. Im Forum Geschichte 12 Bayern wurde über die Geschichte Bayerns diskutiert. Es wurde auf die Bedeutung Bayerns in der Vergangenheit eingegangen und die Frage nach der eigenen Identität gestellt. Es wurden verschiedene Themen wie die Wittelsbacher Dynastie, die bayerische Monarchie und die Rolle Bayerns im Nationalsozialismus behandelt. Die Diskussionsteilnehmer zeigten Interesse an der Geschichte Bayerns und tauschten ihr Wissen aus."
}));
</script>

<template>
  <Popup title="Zusammenfassung">
    <h2>{{ summaryText }}</h2>
  </Popup>
</template>

<style scoped>
 h2 {
   background: black;
   color: var(--text);
   border-radius: 10px;
   padding: 7px;
   overflow: scroll;
   min-height: 200px;
   max-height: calc(100vh - 200px);
 }
</style>