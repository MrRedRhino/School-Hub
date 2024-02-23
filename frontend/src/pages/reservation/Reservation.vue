<script setup>

import {computed, getCurrentInstance, ref} from "vue";
import {openPopup} from "@/popup.js";
import SeatPopup from "@/pages/reservation/SeatPopup.vue";
import {account, requireAuth} from "@/auth.js";

const {appContext} = getCurrentInstance();
const seats = [
  {x: 5, y: 0, angle: 0, id: "R1"},
  {x: 65, y: 0, angle: 0, id: "R2"},
  {x: 125, y: 0, angle: 0, id: ""},
  {x: 185, y: 0, angle: 0, id: ""},
  {x: -55, y: 0, angle: 0, id: ""},
  {x: -115, y: 0, angle: 0, id: ""},
  {x: -175, y: 0, angle: 0, id: ""},
  {x: -235, y: 0, angle: 0, id: ""},

  {x: -25, y: 60, angle: 0, id: ""},
  {x: 35, y: 60, angle: 0, id: ""},
  {x: 95, y: 60, angle: 0, id: ""},
  {x: 155, y: 60, angle: 0, id: ""},
  {x: 215, y: 60, angle: 0, id: ""},
  {x: -85, y: 60, angle: 0, id: ""},
  {x: -145, y: 60, angle: 0, id: ""},
  {x: -205, y: 60, angle: 0, id: ""},
  {x: -265, y: 60, angle: 0, id: ""},

  {x: 5, y: 120, angle: 0, id: ""},
  {x: 65, y: 120, angle: 0, id: ""},
  {x: 125, y: 120, angle: 0, id: ""},
  {x: 185, y: 120, angle: 0, id: ""},
  {x: 245, y: 120, angle: 0, id: ""},
  {x: -55, y: 120, angle: 0, id: ""},
  {x: -115, y: 120, angle: 0, id: ""},
  {x: -175, y: 120, angle: 0, id: ""},
  {x: -235, y: 120, angle: 0, id: ""},
  {x: -295, y: 120, angle: 0, id: ""},

  {x: 35, y: 180, angle: 0, id: ""},
  {x: 95, y: 180, angle: 0, id: ""},
  {x: 155, y: 180, angle: 0, id: ""},
  {x: 215, y: 180, angle: 0, id: ""},
  {x: 275, y: 180, angle: 0, id: ""},
  {x: -85, y: 180, angle: 0, id: ""},
  {x: -145, y: 180, angle: 0, id: ""},
  {x: -205, y: 180, angle: 0, id: ""},
  {x: -265, y: 180, angle: 0, id: ""},
  {x: -325, y: 180, angle: 0, id: ""},

  {x: 5, y: 240, angle: 0, id: ""},
  {x: 65, y: 240, angle: 0, id: ""},
  {x: 125, y: 240, angle: 0, id: ""},
  {x: 185, y: 240, angle: 0, id: ""},
  {x: 245, y: 240, angle: 0, id: ""},
  {x: 305, y: 240, angle: 0, id: ""},
  {x: -55, y: 240, angle: 0, id: ""},
  {x: -115, y: 240, angle: 0, id: ""},
  {x: -175, y: 240, angle: 0, id: ""},
  {x: -235, y: 240, angle: 0, id: ""},
  {x: -295, y: 240, angle: 0, id: ""},
  {x: -355, y: 240, angle: 0, id: ""},

  {x: -25, y: 300, angle: 0, id: ""},
  {x: 35, y: 300, angle: 0, id: ""},
  {x: 95, y: 300, angle: 0, id: ""},
  {x: 155, y: 300, angle: 0, id: ""},
  {x: 215, y: 300, angle: 0, id: ""},
  {x: 275, y: 300, angle: 0, id: ""},
  {x: 335, y: 300, angle: 0, id: ""},
  {x: -85, y: 300, angle: 0, id: ""},
  {x: -145, y: 300, angle: 0, id: ""},
  {x: -205, y: 300, angle: 0, id: ""},
  {x: -265, y: 300, angle: 0, id: ""},
  {x: -325, y: 300, angle: 0, id: ""},
  {x: -385, y: 300, angle: 0, id: ""},

  {x: 5, y: 360, angle: 0, id: ""},
  {x: 65, y: 360, angle: 0, id: ""},
  {x: 125, y: 360, angle: 0, id: ""},
  {x: 185, y: 360, angle: 0, id: ""},
  {x: 245, y: 360, angle: 0, id: ""},
  {x: 305, y: 360, angle: 0, id: ""},
  {x: 365, y: 360, angle: 0, id: ""},
  {x: -55, y: 360, angle: 0, id: ""},
  {x: -115, y: 360, angle: 0, id: ""},
  {x: -175, y: 360, angle: 0, id: ""},
  {x: -235, y: 360, angle: 0, id: ""},
  {x: -295, y: 360, angle: 0, id: ""},
  {x: -355, y: 360, angle: 0, id: ""},
  {x: -415, y: 360, angle: 0, id: ""},

  {x: -25, y: 420, angle: 0, id: ""},
  {x: 35, y: 420, angle: 0, id: ""},
  {x: 95, y: 420, angle: 0, id: ""},
  {x: 155, y: 420, angle: 0, id: ""},
  {x: 215, y: 420, angle: 0, id: ""},
  {x: 275, y: 420, angle: 0, id: ""},
  {x: 335, y: 420, angle: 0, id: ""},
  {x: 395, y: 420, angle: 0, id: ""},
  {x: -85, y: 420, angle: 0, id: ""},
  {x: -145, y: 420, angle: 0, id: ""},
  {x: -205, y: 420, angle: 0, id: ""},
  {x: -265, y: 420, angle: 0, id: ""},
  {x: -325, y: 420, angle: 0, id: ""},
  {x: -385, y: 420, angle: 0, id: ""},
  {x: -445, y: 420, angle: 0, id: ""},

  {x: 5, y: 480, angle: 0, id: ""},
  {x: 65, y: 480, angle: 0, id: ""},
  {x: 125, y: 480, angle: 0, id: ""},
  {x: 185, y: 480, angle: 0, id: ""},
  {x: 245, y: 480, angle: 0, id: ""},
  {x: 305, y: 480, angle: 0, id: ""},
  {x: 365, y: 480, angle: 0, id: ""},
  {x: 425, y: 480, angle: 0, id: ""},
  {x: 485, y: 480, angle: 0, id: ""},
  {x: -55, y: 480, angle: 0, id: ""},
  {x: -115, y: 480, angle: 0, id: ""},
  {x: -175, y: 480, angle: 0, id: ""},
  {x: -235, y: 480, angle: 0, id: ""},
  {x: -295, y: 480, angle: 0, id: ""},
  {x: -355, y: 480, angle: 0, id: ""},
  {x: -415, y: 480, angle: 0, id: ""},
  {x: -475, y: 480, angle: 0, id: ""},
  {x: -535, y: 480, angle: 0, id: ""},


// x: 47
  // y: 38
  {x: 412, y: -101, angle: -40, id: ""},
  {x: 459, y: -139, angle: -40, id: ""},
  {x: 506, y: -177, angle: -40, id: ""},
  {x: 555, y: -215, angle: -40, id: ""},
  {x: 602, y: -253, angle: -40, id: ""},
  {x: 649, y: -291, angle: -40, id: ""},
  {x: 696, y: -329, angle: -40, id: ""},
  {x: 743, y: -367, angle: -40, id: ""},
  {x: 790, y: -405, angle: -40, id: ""},
  {x: 837, y: -443, angle: -40, id: ""},

  {x: 875, y: -396, angle: -40, id: ""},
  {x: 828, y: -358, angle: -40, id: ""},
  {x: 781, y: -320, angle: -40, id: ""},
  {x: 734, y: -282, angle: -40, id: ""},
  {x: 687, y: -244, angle: -40, id: ""},
  {x: 640, y: -206, angle: -40, id: ""},
  {x: 593, y: -168, angle: -40, id: ""},
  {x: 546, y: -130, angle: -40, id: ""},
  {x: 499, y: -92, angle: -40, id: ""},
  {x: 452, y: -54, angle: -40, id: ""},

  {x: 922, y: -349, angle: -40, id: ""},
  {x: 875, y: -311, angle: -40, id: ""},
  {x: 828, y: -273, angle: -40, id: ""},
  {x: 781, y: -235, angle: -40, id: ""},
  {x: 734, y: -197, angle: -40, id: ""},
  {x: 687, y: -159, angle: -40, id: ""},
  {x: 640, y: -121, angle: -40, id: ""},
  {x: 593, y: -83, angle: -40, id: ""},
  {x: 546, y: -45, angle: -40, id: ""},
  {x: 499, y: -7, angle: -40, id: ""},

  {x: 960, y: -302, angle: -40, id: ""},
  {x: 913, y: -264, angle: -40, id: ""},
  {x: 866, y: -226, angle: -40, id: ""},
  {x: 819, y: -188, angle: -40, id: ""},
  {x: 772, y: -150, angle: -40, id: ""},
  {x: 725, y: -112, angle: -40, id: ""},
  {x: 678, y: -74, angle: -40, id: ""},
  {x: 631, y: -36, angle: -40, id: ""},
  {x: 584, y: 2, angle: -40, id: ""},
  {x: 537, y: 40, angle: -40, id: ""},
  {x: 490, y: 78, angle: -40, id: ""},

  {x: 998, y: -255, angle: -40, id: ""},
  {x: 951, y: -217, angle: -40, id: ""},
  {x: 904, y: -179, angle: -40, id: ""},
  {x: 857, y: -141, angle: -40, id: ""},
  {x: 810, y: -103, angle: -40, id: ""},
  {x: 763, y: -65, angle: -40, id: ""},
  {x: 716, y: -27, angle: -40, id: ""},
  {x: 669, y: 11, angle: -40, id: ""},
  {x: 622, y: 49, angle: -40, id: ""},
  {x: 575, y: 87, angle: -40, id: ""},
  {x: 528, y: 125, angle: -40, id: ""},

  {x: 1036, y: -208, angle: -40, id: ""},
  {x: 989, y: -170, angle: -40, id: ""},
  {x: 942, y: -132, angle: -40, id: ""},
  {x: 895, y: -94, angle: -40, id: ""},
  {x: 848, y: -56, angle: -40, id: ""},
  {x: 801, y: -18, angle: -40, id: ""},
  {x: 754, y: 20, angle: -40, id: ""},
  {x: 707, y: 58, angle: -40, id: ""},
  {x: 660, y: 96, angle: -40, id: ""},
  {x: 613, y: 134, angle: -40, id: ""},
  {x: 566, y: 172, angle: -40, id: ""},
  {x: 519, y: 210, angle: -40, id: ""},

  {x: 1074, y: -161, angle: -40, id: ""},
  {x: 1027, y: -123, angle: -40, id: ""},
  {x: 980, y: -85, angle: -40, id: ""},
  {x: 933, y: -47, angle: -40, id: ""},
  {x: 886, y: -9, angle: -40, id: ""},
  {x: 839, y: 29, angle: -40, id: ""},
  {x: 792, y: 67, angle: -40, id: ""},
  {x: 745, y: 105, angle: -40, id: ""},
  {x: 698, y: 143, angle: -40, id: ""},
  {x: 651, y: 181, angle: -40, id: ""},
  {x: 604, y: 219, angle: -40, id: ""},
  {x: 557, y: 257, angle: -40, id: ""},

  {x: 1112, y: -114, angle: -40, id: ""},
  {x: 1065, y: -76, angle: -40, id: ""},
  {x: 1018, y: -38, angle: -40, id: ""},
  {x: 971, y: 0, angle: -40, id: ""},
  {x: 924, y: 38, angle: -40, id: ""},
  {x: 877, y: 76, angle: -40, id: ""},
  {x: 830, y: 114, angle: -40, id: ""},
  {x: 783, y: 152, angle: -40, id: ""},
  {x: 736, y: 190, angle: -40, id: ""},
  {x: 689, y: 228, angle: -40, id: ""},
  {x: 642, y: 266, angle: -40, id: ""},
  {x: 595, y: 304, angle: -40, id: ""},
  {x: 548, y: 342, angle: -40, id: ""},

  {x: 1150, y: -67, angle: -40, id: ""},
  {x: 1103, y: -29, angle: -40, id: ""},
  {x: 1056, y: 9, angle: -40, id: ""},
  {x: 1009, y: 47, angle: -40, id: ""},
  {x: 962, y: 85, angle: -40, id: ""},
  {x: 915, y: 123, angle: -40, id: ""},
  {x: 868, y: 161, angle: -40, id: ""},
  {x: 821, y: 199, angle: -40, id: ""},
  {x: 774, y: 237, angle: -40, id: ""},
  {x: 727, y: 275, angle: -40, id: ""},
  {x: 680, y: 313, angle: -40, id: ""},
  {x: 633, y: 351, angle: -40, id: ""},
  {x: 586, y: 389, angle: -40, id: ""},

  // gelbe Stühle fehlen
]
const reservations = ref({});

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

let eventSource;
let reconnect = null;

function connect() {
  eventSource = new EventSource("http://localhost:4000/api/reservations/live");

  eventSource.addEventListener("set_reservations", event => {
    const newSeats = {};
    const data = JSON.parse(event.data);
    for (let reservation of data["reservations"]) {
      newSeats[reservation["seat"]] = reservation["name"];
    }
    reservations.value = newSeats;
    maxSeats = data["max-seats"];
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
    reconnect = setTimeout(connect, 2000);
  };
}

connect();

function reserveSeat(seat) {
  requireAuth("einen Platz zu reservieren", appContext, () => {
    openPopup(SeatPopup, appContext, {"seat-id": seat, reservations: reservations, "max-seats-reached": maxSeatsReached});
  });
}
</script>

<template>
  <h1>{{ maxSeatsReached }}</h1>

  <div class="plan">
    <div v-for="seat in seats"
         class="seat"
         :style="{transform: `translate(${seat.x}px, ${seat.y}px) rotate(${seat.angle + 'deg'})`, background: reservations[seat.id] ? 'white' : 'green'}"
         @click="reserveSeat(seat.id)">
    </div>
  </div>
</template>

<style scoped>
.seat {
  position: absolute;
  width: 50px;
  height: 50px;
  border-top-right-radius: 50%;
  border-top-left-radius: 50%;
  cursor: pointer;
}

.plan {
  transform: translate(700px, 600px);
}
</style>