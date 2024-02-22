<script setup>
import Popup from "@/components/Popup.vue";

const {seatId} = defineProps(["seat-id"]);

function toggleReservation() {

}

</script>

<template>
<Popup :title="`Platz ${seatId} reservieren`">

</Popup>
</template>

<style scoped>

</style>