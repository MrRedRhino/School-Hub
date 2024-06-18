<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import {openPopup} from "@/popup.js";
import SeatPopup from "@/pages/reservation/SeatPopup.vue";
import {account, requireAuth} from "@/auth.js";
import PinchZoom from "@/components/PinchZoom.vue";
import SeatLocation from "@/pages/reservation/SeatLocation.vue";
import LoadingSpinner from "@/components/LoadingSpinner.vue";

const {appContext} = getCurrentInstance();
const seats = ref([]);

const mailAddress = ref();
const sendingMail = ref(false);
const invalidMail = ref(false);
const mailMessage = ref();

const reservations = ref({});
const ownReservationsShown = ref(false);
const ownReservations = computed(() => {
  const result = [];
  for (const reservation in reservations.value) {
    if (reservations.value[reservation] === account.value.name) {
      const parts = reservation.split("-");
      result.push({row: parts[0], seat: parts[1]});
    }
  }
  return result;
});

let maxSeats = null;
const maxSeatsReached = computed(() => {
  let count = 0;
  for (const reservation in reservations.value) {
    if (reservations.value[reservation] === account.value.name) {
      count++;
    }
  }
  return maxSeats !== null && count >= maxSeats;
});

const connected = ref(false);
let eventSource;
let reconnect = null;

function connect() {
  eventSource = new EventSource("/api/reservations/live");

  eventSource.addEventListener("set_reservations", event => {
    const newSeats = {};
    const data = JSON.parse(event.data);
    for (let reservation of data["reservations"]) {
      newSeats[reservation["seat"]] = reservation["name"];
    }
    reservations.value = newSeats;
    maxSeats = data["max-seats"];
    connected.value = true;
  });

  eventSource.addEventListener("remove_reservation", event => {
    const json = JSON.parse(event.data);
    delete reservations.value[json["seat"]];
  });

  eventSource.addEventListener("add_reservation", event => {
    const newSeat = JSON.parse(event.data);
    reservations.value[newSeat["seat"]] = newSeat["name"];
  });

  clearTimeout(reconnect);
  eventSource.onerror = () => {
    connected.value = false;
    reconnect = setTimeout(connect, 2000);
  };
}

connect();

function reserveSeat(location) {
  requireAuth("einen Platz zu reservieren", appContext, () => {
    openPopup(SeatPopup, appContext, {
      location: location,
      id: getId(location),
      reservations: reservations,
      "max-seats-reached": maxSeatsReached
    });
  });
}

function showReservations() {
  requireAuth("Reservierungen anzuschauen", appContext, () => {
    ownReservationsShown.value = !ownReservationsShown.value;
  });
}

function hideReservations() {
  ownReservationsShown.value = false;
}

function getId(location) {
  return location.row + "-" + location.seat;
}

function getColor(reservation) {
  if (account.value && reservation === account.value.name) {
    return "#ff2046";
  } else if (reservation === undefined) {
    return "#00bf4b";
  }
  return "#B3B3B3";
}

async function sendMail() {
  mailMessage.value = "";
  invalidMail.value = false;
  sendingMail.value = true;
  const response = await fetch("/api/reservations/send-mail?email-address=" + encodeURIComponent(mailAddress.value), {
    method: "POST"
  });
  sendingMail.value = false;


  invalidMail.value = response.status !== 200;
  if (response.status === 200) {
    mailMessage.value = "Mail wurde verschickt";
    mailAddress.value = "";
  } else {
    mailMessage.value = "Ungültige Mail Adresse";
  }
}

watch(mailAddress, () => {
  invalidMail.value = false;
});

fetch("/api/reservations/seats")
    .then(r => r.json())
    .then(newSeats => seats.value = newSeats);
</script>

<template>
  <div>
    <LoadingSpinner v-if="seats.length === 0 || !connected"></LoadingSpinner>

    <div class="plan-wrapper">
      <div class="reservations-wrapper" v-click-outside="hideReservations">
        <button class="open-reservations" @click="showReservations">Meine Reservierungen {{ ownReservations.length }} /
          {{ maxSeats }}
        </button>
        <Transition>
          <div class="reservations" v-if="ownReservationsShown">
            <SeatLocation v-for="reservation in ownReservations" :location="reservation"></SeatLocation>
            <h1 v-if="ownReservations.length === 0">Keine Reservierungen</h1>

            <div v-if="ownReservations.length > 0">
              <h1>Als E-Mail senden:</h1>
              <input v-model="mailAddress" :class="{invalid: invalidMail}" type="email" placeholder="E-Mail Adresse">
              <button @click="sendMail" :disabled="sendingMail">Senden</button>
              <h2 v-if="mailMessage">{{ mailMessage }}</h2>
            </div>
          </div>
        </Transition>
      </div>

      <PinchZoom v-if="seats.length > 0 && connected">
        <div v-for="seat in seats"
             class="seat"
             :style="{transform: `translate(${seat.x}px, ${seat.y}px) rotate(${seat.angle + 'deg'})`, background: getColor(reservations[getId(seat.location)])}"
             @click="reserveSeat(seat.location)">
        </div>

        <svg class="stage-svg" width="1600" height="800">
          <polygon points="50, 50, 500, 560, 1120, 560, 1550, 50" fill="#B3B3B3"/>
          <text x="650" font-size="100" y="350">Bühne</text>
        </svg>
      </PinchZoom>
    </div>
  </div>
</template>

<style scoped>
.plan-wrapper {
  height: calc(100dvh - 130px);
  background: var(--background-dark);
  margin: 10px;
  border-radius: 20px;
  overflow: hidden;
}

.stage-svg {
  z-index: 11;
  position: relative;
  transform: translate(-800px, -650px);
  pointer-events: none;
}

.reservations {
  margin-top: 4px;
  display: grid;
  background: var(--background-dark);
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  border-radius: 10px;
  padding: 10px 10px;
  row-gap: 10px;
}

.reservations.v-enter-active, .v-leave-active {
  transition: opacity 0.1s, scale 0.1s;
}

.reservations.v-enter-from, .v-leave-to {
  opacity: 0;
  scale: 0.9;
}

.reservations h1 {
  color: var(--text);
  font-size: 18px;
  font-weight: normal;
  margin: 0 0 5px;
}

.reservations h2 {
  color: var(--text);
  font-size: 16px;
  font-weight: normal;
  margin: 5px 0 0 4px;
}

.seat {
  position: absolute;
  width: 50px;
  height: 50px;
  border-top-right-radius: 50%;
  border-top-left-radius: 50%;
  cursor: pointer;
}

.reservations-wrapper {
  position: absolute;
  width: 309px;
  z-index: 9;
  margin: 10px;
}

.open-reservations {
  width: 100%;
  margin: 0;
  position: relative;
  z-index: 8;
}

input {
  height: 34px;
  background: var(--background);
  color: var(--text);
  border-radius: 10px;
  width: 220px;
  font-size: 16px;
  border: none;
  padding: 0 0 0 10px;
}

input.invalid {
  outline: #ff2549 solid 2px;
}

button {
  height: 34px;
  border-radius: 10px;
  border: none;
  background: var(--blue);
  color: var(--text);
  font-size: 16px;
  cursor: pointer;
  margin-left: 5px;
  transition: scale 0.1s;
}

button:active {
  scale: 0.97;
}

button:disabled {
  background: rgba(255, 255, 255, 0.4);
  cursor: default;
}
</style>