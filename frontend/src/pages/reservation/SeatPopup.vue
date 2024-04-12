<script setup>
import Popup from "@/components/Popup.vue";
import {computed} from "vue";
import {account} from "@/auth.js";
import {formatName} from "@/main.js";
import SeatLocation from "@/pages/reservation/SeatLocation.vue";

const {
  location,
  id,
  reservations,
  maxSeatsReached
} = defineProps(["location", "id", "reservations", "max-seats-reached"]);
const reservist = computed(() => {
  const reservation = reservations.value[id];
  if (reservation === undefined) return null;
  return reservation === account.value.name ? "Dir" : formatName(reservation);
});
const canNotReserve = computed(() => maxSeatsReached.value && reservations.value[id] === undefined);

async function toggleReservation() {
  await fetch(`/api/reservations/${id}`, {
    method: reservations.value[id] ? "DELETE" : "PUT"
  });
}
</script>

<template>
  <Popup :title="`Platz reservieren`">
    <SeatLocation style="margin-left: 8px" :location="location"></SeatLocation>

    <h2>Reserviert von: <a>{{ reservist ? reservist : "Nicht reserviert" }}</a></h2>
    <h2 class="error" v-if="canNotReserve">Die maximale Anzahl an Reservierungen ist erreicht</h2>

    <button :disabled="canNotReserve || reservations.value[id] && reservations.value[id] !== account.name"
            @click="toggleReservation">
      {{ reservations.value[id] === account.name ? "Stornieren" : "Reservieren" }}
    </button>
  </Popup>
</template>

<style scoped>
h2 {
  margin-left: 8px;
  margin-top: 10px;
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
