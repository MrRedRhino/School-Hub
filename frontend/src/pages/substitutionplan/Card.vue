<script setup>
import {ref} from "vue";

const {day} = defineProps(["day"]);
const data = ref(null);
const error = ref(false);

fetch(`/api/plans/${day}?format=json`)
    .then(r => r.json().then(json => data.value = json))
    .catch(() => error.value = true);
</script>

<template>
  <div :class="{card: true, error: error}">
    <div v-if="data">
      <h2>{{day === "today" ? "Heute" : "Morgen"}} {{ data["date"] }}</h2>
      <h3>{{ data["information"] === "" ? "Keine Mitteilungen" : data["information"] }}</h3>

      <table>
        <thead>
        <tr>
          <th>Klasse</th>
          <th>Std</th>
          <th>Vertretung</th>
          <th>Lehrer</th>
          <th>Raum</th>
          <th>Sonstiges</th>
        </tr>
        </thead>

        <tr v-for="s in data['substitutions']">
          <td>{{ s["class"] }}</td>
          <td>{{ s["lesson"] }}</td>
          <td>{{ s["substitution"] }}</td>
          <td>{{ s["teacher"] }}</td>
          <td>{{ s["room"] }}</td>
          <td>{{ s["other"] }}</td>
        </tr>
      </table>
    </div>

    <div v-else-if="error">
      <h1>Der Plan konnte nicht geladen werden</h1>
    </div>

    <div v-else class="loading">
      <img src="/src/assets/loading-137px.gif" alt="Ladeanimation">
      <h1>Lädt...</h1>
    </div>
  </div>
</template>

<style scoped>
.card {
  padding: 15px;
  background-color: var(--background-dark);
  border-radius: 20px;
  max-width: 1140px;
  color: var(--text);
  margin-bottom: 20px;
  margin-left: 10px;
}

.loading {
  text-align: center;
}

h1 {
  font-size: 18px;
}

div.error {
  color: var(--text);
  background: #730b1c;
  border: 2px solid #ce082a;
}

h2 {
  color: var(--blue);
  font-size: 25px;
  margin: 0;
}

h3 {
  font-size: 18px;
  margin-bottom: 30px;
  font-weight: normal;
  white-space: pre-wrap;
}

table {
  border-collapse: collapse;
  width: 100%;
}

tr, td, th {
  border: 2px solid var(--text);
  padding: 8px;
  color: var(--text);
  font-size: 16px;
}
</style>