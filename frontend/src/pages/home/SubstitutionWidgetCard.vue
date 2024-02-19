<script setup>
import {ref} from "vue";

const {day} = defineProps(["day"]);
const emit = defineEmits(["date"]);

const expanded = ref(false);
const content = ref();
const messageElement = ref();

fetch(`https://hub.pipeman.org/api/plans/for-you/${day}`).then(r => r.json().then(json => {
  content.value = json;
  emit("date", json["date"]);
}));

function isOverflown(element) {
  if (element === undefined) {
    return false;
  }
  return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
}
</script>

<template>
  <div v-if="content">
    <div @click="expanded = true">
      <a ref="messageElement" :class="{expanded: expanded}">{{ content["message"] }}</a>
      <a v-if="!expanded && isOverflown(messageElement)" class="more-button">...mehr</a>
    </div>

    <hr>
    <ul>
      <li v-for="line in content['for-you']">
        {{ line }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
a {
  display: block;
  font-size: 18px;
  height: 21px;
  width: 100%;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

ul {
  padding-left: 10px;
  margin-bottom: 5px;
}

li {
  list-style: "- ";
  font-size: 18px;
}

.more-button {
  font-weight: bold;
  color: var(--text-dark);
  cursor: pointer;
}

.expanded {
  white-space: pre-wrap;
  height: fit-content;
}
</style>