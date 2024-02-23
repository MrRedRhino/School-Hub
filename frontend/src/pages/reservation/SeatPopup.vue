<script setup>
import Popup from "@/components/Popup.vue";
import {computed} from "vue";
import {account} from "@/auth.js";
import {formatName} from "@/main.js";

const {seatId, reservations, maxSeatsReached} = defineProps(["seat-id", "reservations", "max-seats-reached"]);
const reservist = computed(() => {
  const reservation = reservations.value[seatId];
  if (reservation === undefined) return null;
  return reservation === account.value.name ? "Dir" : formatName(reservation);
});
const canNotReserve = computed(() => maxSeatsReached.value && reservations.value[seatId] === undefined);

async function toggleReservation() {
  await fetch(`/api/reservations/${seatId}`, {
    method: reservations.value[seatId] ? "DELETE" : "PUT"
  });
}
</script>

<template>
  <Popup :title="`Platz ${seatId} reservieren`">
    <h2>Reserviert von: <a>{{ reservist ? reservist : "Nicht reserviert" }}</a></h2>
    <h2 class="error" v-if="canNotReserve">Die maximale Anzahl an Reservierungen ist erreicht</h2>

    <button :disabled="canNotReserve || reservations.value[seatId] && reservations.value[seatId] !== account.name"
            @click="toggleReservation">
      {{ reservations.value[seatId] === account.name ? "Stornieren" : "Reservieren" }}
    </button>
  </Popup>
</template>

<style scoped>
h2 {
  font-weight: normal;
}

a {
  font-weight: bold;
}

button {
  width: 180px;
}

.error {
  margin-top: 0;
  color: #ff2549;
}
</style>
