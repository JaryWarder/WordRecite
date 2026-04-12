<template>
  <div class="main-layout">
    <div class="top-navbar">
      <div class="nav-content">
        <div class="logo-area">
          <i class="el-icon-notebook-2 logo-icon"></i>
          <span class="logo-text">WordRecite</span>
        </div>
        <el-menu
          :default-active="$route.path"
          mode="horizontal"
          background-color="transparent"
          text-color="var(--text-muted)"
          active-text-color="var(--accent-color)"
          class="nav-menu"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-sub-menu index="2">
            <template #title>学习</template>
            <el-menu-item index="/wordBooks">选择书本</el-menu-item>
            <el-menu-item index="/plan">制定计划</el-menu-item>
            <el-menu-item index="/start">背单词</el-menu-item>
            <el-menu-item index="/review">复习列表</el-menu-item>
            <el-menu-item index="/private">生词本</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3">
            <template #title>挑战</template>
            <el-menu-item index="/testStart">词汇测试</el-menu-item>
            <el-menu-item index="/progress">进度分析</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/userInfo">个人中心</el-menu-item>
        </el-menu>

        <div class="user-actions">
          <span class="username-display" v-if="isLogin">Hi, {{ username }}</span>
          <div class="action-buttons">
            <LoginDialog v-if="!isLogin" @login_success="change_login_name" />
            <SignUpDialog v-if="!isLogin" />
            <LogoutDialog v-if="isLogin" @logout_success="on_logout" />
          </div>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <transition name="fade-transform" mode="out-in">
        <router-view></router-view>
      </transition>
    </div>

    <footer class="app-footer">Designed by JaryWarder © {{ date }}</footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import LoginDialog from '../components/small/LoginDialog.vue'
import SignUpDialog from '../components/small/SignUpDialog.vue'
import LogoutDialog from '../components/small/LogoutDialog.vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const { username, isLogin } = storeToRefs(userStore)

const date = computed(() => new Date().getFullYear())

function change_login_name (name) {
  userStore.restore()
  userStore.username = name
}

function on_logout () {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  userStore.restore()
})
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.top-navbar {
  height: 70px;
  width: 100%;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border);
  position: fixed;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
}

.nav-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 800;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.logo-icon {
  margin-right: 10px;
  color: var(--accent-color);
}

.nav-menu {
  border-bottom: none !important;
  flex-grow: 1;
  display: flex;
  justify-content: center;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username-display {
  color: var(--text-muted);
  font-size: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-wrapper {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 100px 20px 60px;
  flex-grow: 1;
}

.app-footer {
  text-align: center;
  padding: 15px 0;
  font-size: 12px;
  color: var(--text-muted);
  border-top: 1px solid var(--glass-border);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(20px);
}
</style>
