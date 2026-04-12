<template>
  <div id="app">
    <div class="background-animation"></div>
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getPendingCount } from './api/review'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()
let lastLoginUsername = ''

async function checkReviewPendingOnce () {
  userStore.restore()
  if (!userStore.isLogin) return
  const key = `review_pending_prompted:${userStore.username || ''}`
  if (sessionStorage.getItem(key)) return

  try {
    const resp = await getPendingCount()
    const count = Number(resp?.data?.count || 0)
    if (resp?.code !== 200 || count <= 0) return

    sessionStorage.setItem(key, '1')
    await ElMessageBox.confirm(
      `根据艾宾浩斯记忆曲线，您当前有 ${count} 个单词需要复习啦！`,
      '复习提醒',
      {
        confirmButtonText: '立即去复习',
        cancelButtonText: '稍后',
        type: 'warning',
        closeOnClickModal: false,
        closeOnPressEscape: true
      }
    )
    router.push('/review')
  } catch (e) {
  }
}

watch(
  () => userStore.isLogin,
  (isLogin) => {
    if (!isLogin) {
      if (lastLoginUsername) {
        sessionStorage.removeItem(`review_pending_prompted:${lastLoginUsername}`)
      }
      lastLoginUsername = ''
      return
    }
    lastLoginUsername = userStore.username || ''
    void checkReviewPendingOnce()
  }
)

onMounted(() => {
  void checkReviewPendingOnce()
})
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
