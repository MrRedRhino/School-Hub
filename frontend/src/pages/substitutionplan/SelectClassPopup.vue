<script setup>

import Popup from "@/components/Popup.vue";
import SelectClassMenu from "@/pages/substitutionplan/SelectClassMenu.vue";
import {closePopup} from "@/popup.js";
</script>

<template>
  <Popup title="Plan auswählen">
    <div>
      <SelectClassMenu></SelectClassMenu>
      <button @click="closePopup">Fertig</button>
    </div>
  </Popup>
</template>

<style scoped>
div {
  height: 80vh;
  overflow: scroll;
}
</style>