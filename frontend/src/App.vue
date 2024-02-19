<script setup>
import Profile from "@/components/Profile.vue";
</script>

<template>
  <div>
    <div class="header">
      <div class="links">
        <router-link to="/">
          <svg viewBox="0 0 16 16" fill="currentColor"
               xmlns="http://www.w3.org/2000/svg"><path d="M15.45,7L14,5.551V2c0-0.55-0.45-1-1-1h-1c-0.55,0-1,0.45-1,1v0.553L9,0.555C8.727,0.297,8.477,0,8,0S7.273,0.297,7,0.555  L0.55,7C0.238,7.325,0,7.562,0,8c0,0.563,0.432,1,1,1h1v6c0,0.55,0.45,1,1,1h3v-5c0-0.55,0.45-1,1-1h2c0.55,0,1,0.45,1,1v5h3  c0.55,0,1-0.45,1-1V9h1c0.568,0,1-0.437,1-1C16,7.562,15.762,7.325,15.45,7z"/></svg>
        </router-link>
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

svg {
  width: 32px;
  height: 32px;
}

body {
  background: var(--background);
}
</style>
