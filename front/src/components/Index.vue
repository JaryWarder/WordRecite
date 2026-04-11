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
          router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-submenu index="2">
            <template slot="title">学习</template>
            <el-menu-item :index="$router.options.routes[2].children[0].path">选择书本</el-menu-item>
            <el-menu-item :index="$router.options.routes[3].children[0].path">制定计划</el-menu-item>
            <el-menu-item :index="$router.options.routes[4].children[0].path">背单词</el-menu-item>
            <el-menu-item :index="$router.options.routes[5].children[0].path">复习列表</el-menu-item>
          </el-submenu>
          <el-submenu index="3">
            <template slot="title">挑战</template>
            <el-menu-item :index="$router.options.routes[7].children[0].path">词汇测试</el-menu-item>
            <el-menu-item :index="$router.options.routes[6].children[0].path">进度分析</el-menu-item>
          </el-submenu>
          <el-menu-item :index="$router.options.routes[1].children[0].path">个人中心</el-menu-item>
        </el-menu>

        <div class="user-actions">
          <span class="username-display" v-if="isLogin">Hi, {{ username }}</span>
          <div class="action-buttons">
            <LoginDialog v-if="!isLogin" @login_success="change_login_name"></LoginDialog>
            <SignUpDialog v-if="!isLogin"></SignUpDialog>
            <LogoutDialog v-if="isLogin" @logout_success="on_logout"></LogoutDialog>
          </div>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <transition name="fade-transform" mode="out-in">
        <router-view></router-view>
      </transition>
    </div>

    <footer class="app-footer">
      Designed by JaryWarder © {{ date }}
    </footer>
  </div>
</template>

<script>
// 逻辑保持完全不变
import LoginDialog from './small/LoginDialog'
import SignUpDialog from './small/SignUpDialog'
import LogoutDialog from './small/LogoutDialog'
import { delCookie, setCookie, getCookie } from '../util.js'
export default {
  name: 'Index',
  components: { LoginDialog, SignUpDialog, LogoutDialog },
  data () {
    return {
      username: 'Guest',
      isLogin: false,
      tableData: Array(10).fill({date: '2016-05-02', name: 'ABC', address: 'Address'}),
      date: ''
    }
  },
  methods: {
    change_login_name (param) {
      this.username = param;
      this.isLogin = true;
    },
    on_logout () {
      this.username = 'guest';
      this.isLogin = false;
      delCookie('username', 1);
      setCookie('isLogin', 'false', 1);
      console.log('after logout, cookies: ', document.cookie);
      this.$router.push(this.$router.options.routes[0].children[0].path);
    }
  },
  mounted: function () {
    let nowDate = new Date();
    let year = nowDate.getFullYear();
    this.date = year;

    // 1. 获取 username cookie
    let userCookie = getCookie('username');

    // 2. 检查 cookie 是否有效
    // 注意：有些浏览器存 null 会变成字符串 "null"，所以两个都要判断
    if (userCookie !== null && userCookie !== 'null' && userCookie !== '') {
      // 如果存在有效的 cookie，恢复登录状态
      this.username = userCookie;
      this.isLogin = true;
    } else {
      // 否则保持未登录状态
      this.isLogin = false;
    }

    console.log('Index mounted check. User:', this.username, 'Login:', this.isLogin);
  }
}
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
  color: var(--accent-color); /* Fallback */
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
  gap: 15px;
}

.username-display {
  color: var(--text-main);
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.content-wrapper {
  margin-top: 90px; /* Navbar height + spacing */
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 20px;
}

.app-footer {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 12px;
}

/* 路由切换动画 */
.fade-transform-enter-active, .fade-transform-leave-active {
  transition: all 0.3s ease;
}
.fade-transform-enter {
  opacity: 0;
  transform: translateY(20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
