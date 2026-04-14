<template>
  <div class="main-layout">
    <nav 
      class="top-navbar" 
      :class="{ 'is-floating': isScrolled }"
    >
      <div class="nav-content">
        <div class="logo-area" @click="router.push('/')">
          <div class="logo-icon-box">
            <i class="el-icon-notebook-2 logo-icon"></i>
          </div>
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
          <el-menu-item index="/">
            <div class="menu-item-inner">首页</div>
          </el-menu-item>
          
          <el-sub-menu index="2" teleported>
            <template #title>
              <div class="menu-item-inner">学习</div>
            </template>
            <el-menu-item index="/wordBooks">选择书本</el-menu-item>
            <el-menu-item index="/plan">制定计划</el-menu-item>
            <el-menu-item index="/start">背单词</el-menu-item>
            <el-menu-item index="/review">复习列表</el-menu-item>
            <el-menu-item index="/dailyFootprint">每日足迹</el-menu-item>
            <el-menu-item index="/private">生词本</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="3" teleported>
            <template #title>
              <div class="menu-item-inner">挑战</div>
            </template>
            <el-menu-item index="/testStart">词汇测试</el-menu-item>
            <el-menu-item index="/progress">进度分析</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/userInfo">
            <div class="menu-item-inner">个人中心</div>
          </el-menu-item>
        </el-menu>

        <div class="user-actions">
          <div class="user-pill" v-if="isLogin">
            <el-avatar :size="24" class="nav-avatar">{{ username.charAt(0).toUpperCase() }}</el-avatar>
            <span class="username-display">{{ username }}</span>
          </div>
          <div class="action-buttons">
            <LoginDialog v-if="!isLogin" @login_success="change_login_name" />
            <SignUpDialog v-if="!isLogin" />
            <LogoutDialog v-if="isLogin" @logout_success="on_logout" />
          </div>
        </div>
      </div>
    </nav>

    <div class="content-wrapper">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <footer class="app-footer">Designed by JaryWarder © {{ date }}</footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
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
const isScrolled = ref(false)

const handleScroll = () => {
  // 滚动判定，触发形态切换
  isScrolled.value = window.scrollY > 10
}

function change_login_name (name) {
  userStore.restore()
  userStore.username = name
}

function on_logout () {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  userStore.restore()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* --- 核心优化：导航栏色彩与长度控制 --- */
.top-navbar {
  height: 64px;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  transition: all 0.4s ease;
  /* 初始态：透明底色，使用主背景色 #0f172a 采样 */
  background: rgba(15, 23, 42, 0.4); 
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

/* 浮动态：显著缩短长度，增强融入感 */
.top-navbar.is-floating {
  top: 12px;
  height: 56px;
  /* 缩短导航栏总长度 */
  width: auto;
  min-width: 800px; 
  padding: 0 10px;
  border-radius: 28px;
  /* 使用单一深色，与背景融为一体 */
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6);
}

.nav-content {
  width: 100%;
  max-width: 1000px; /* 进一步限制内容宽度 */
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon-box {
  background: var(--accent-gradient);
  padding: 5px;
  border-radius: 8px;
  margin-right: 10px;
  display: flex;
}

.logo-icon { color: #fff; font-size: 16px; }

.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: var(--accent-color);
  letter-spacing: -0.2px;
}

/* 菜单布局 */
.nav-menu {
  border: none !important;
  flex-grow: 1;
  display: flex;
  justify-content: center;
  height: 100%;
}

:deep(.el-menu-item), 
:deep(.el-sub-menu__title) {
  height: 100% !important;
  line-height: 1 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0 15px !important;
  border-bottom: none !important;
}

.menu-item-inner {
  font-size: 14px;
  font-weight: 600;
  /* 解决位置靠上的垂直补正 */
  margin-top: 2px;
}

:deep(.el-menu-item.is-active) {
  color: var(--accent-color) !important;
  font-weight: 700;
}

/* 用户状态 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px 3px 4px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.nav-avatar {
  background: var(--accent-gradient);
  font-size: 11px;
}

.username-display {
  color: var(--text-muted);
  font-size: 12px;
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
  padding: 25px 0;
  font-size: 12px;
  color: var(--text-muted);
  opacity: 0.5;
}
</style>