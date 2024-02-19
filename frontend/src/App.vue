<script setup>
import Profile from "@/components/Profile.vue";
</script>

<template>
  <div>
    <div class="header">
      <div class="links">
        <router-link to="/"><img src="@/assets/favicon.ico" height="32px" alt="Home"></router-link>
        <router-link to="/vertretungsplan">Vertretungsplan</router-link>
        <router-link to="/books">Bücher</router-link>
        <router-link to="/supervision">Aufsichten</router-link>
        <router-link to="/abifeier">Abifeier Reservierung</router-link>
      </div>

      <Profile class="profile"></Profile>
    </div>
    <div class="margin"></div>
    <router-view></router-view>
  </div>
</template>

<style scoped>
.header {
  display: flex;
  gap: 10px;
  background: var(--background-dark);
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  height: 64px;
  align-items: center;
  padding-left: 10px;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 5;
}

.links {
  display: flex;
  gap: 15px;
  overflow-x: scroll;
  padding-bottom: 15px;
  padding-top: 15px;
  align-items: center;
}

.margin {
  margin-bottom: 80px;
}

.profile {
  margin-left: auto;
  margin-right: 10px;
}

a {
  color: var(--text);
  text-decoration: none;
  font-weight: bold;
  cursor: pointer;
  transition-duration: 0.15s;
  font-size: 18px;
  text-align: center;
  white-space: nowrap;
}

a:active {
  scale: 0.95;
}

a.router-link-active {
  color: var(--text-dark);
}

body {
  background: var(--background);
}
</style>
