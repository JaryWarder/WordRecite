<template>
  <div id="app">
    <div class="background-animation"></div>
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
:root {
  --bg-gradient-start: #0f172a;
  --bg-gradient-end: #1e293b;
  --glass-bg: rgba(30, 41, 59, 0.7);
  --glass-border: rgba(255, 255, 255, 0.1);
  --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  --text-main: #f8fafc;
  --text-muted: #94a3b8;
  --accent-color: #38bdf8;
  --accent-hover: #0ea5e9;
  --accent-gradient: linear-gradient(135deg, #38bdf8 0%, #3b82f6 100%);
  --danger-color: #f43f5e;
  --success-color: #10b981;
  --warning-color: #f59e0b;
}

html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: var(--bg-gradient-start);
  color: var(--text-main);
  overflow-x: hidden;
}

#app {
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background: radial-gradient(circle at 50% 0%, #1e293b 0%, #0f172a 100%);
}

.el-card,
.el-dialog,
.el-menu,
.el-input__wrapper {
  border: 1px solid var(--glass-border) !important;
  background: var(--glass-bg) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  color: var(--text-main) !important;
  box-shadow: var(--glass-shadow) !important;
}
</style>
