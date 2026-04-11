/**
 * App.vue - 全局样式重构
 * 风格：Cyber-Glass (深空玻璃拟态)
 */
<template>
  <div id="app">
    <div class="background-animation"></div>
    <router-view/>
  </div>
</template>

<script>
import $ from 'jquery'
export default {
  name: 'App',
  data () {
    return {
      serverData: 'no data'
    }
  },
  methods: {
    getData () {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/getperson/33333',
        dataType: 'json',
        crossDomain: true,
        xhrFields: {
          withCredentials: true
        },
        data: {},
        success: (result) => {
          this.serverData = JSON.stringify(result);
        },
        error: function () {
          alert('error');
        }
      })
    }
  }
}
</script>

<style>
/* --- 全局样式重置与变量定义 --- */
:root {
  /* 背景色系 - 深邃星空 */
  --bg-gradient-start: #0f172a;
  --bg-gradient-end: #1e293b;

  /* 玻璃拟态卡片背景 */
  --glass-bg: rgba(30, 41, 59, 0.7);
  --glass-border: rgba(255, 255, 255, 0.1);
  --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);

  /* 文字颜色 */
  --text-main: #f8fafc;
  --text-muted: #94a3b8;

  /* 强调色 - 青色霓虹 */
  --accent-color: #38bdf8;
  --accent-hover: #0ea5e9;
  --accent-gradient: linear-gradient(135deg, #38bdf8 0%, #3b82f6 100%);

  /* 功能色 */
  --danger-color: #f43f5e;
  --success-color: #10b981;
  --warning-color: #f59e0b;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
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

/* 背景动画层 */
.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background: radial-gradient(circle at 50% 0%, #1e293b 0%, #0f172a 100%);
}

/* --- Element UI 组件全局深度覆盖 (玻璃化) --- */

/* 去除默认边框和背景 */
.el-card, .el-dialog, .el-menu, .el-input__inner {
  border: 1px solid var(--glass-border) !important;
  background: var(--glass-bg) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  color: var(--text-main) !important;
  box-shadow: var(--glass-shadow) !important;
}

.el-card {
  border-radius: 16px !important;
}

.el-card__header {
  border-bottom: 1px solid var(--glass-border) !important;
}

/* 按钮样式重塑 */
.el-button--primary {
  background: var(--accent-gradient) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(56, 189, 248, 0.3);
  font-weight: 600;
  transition: all 0.3s ease;
}

.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 189, 248, 0.5);
}

.el-button--text {
  color: var(--accent-color) !important;
}

/* 输入框 */
.el-input__inner {
  background: rgba(0, 0, 0, 0.2) !important;
  color: white !important;
  border-radius: 8px !important;
}
.el-input__inner:focus {
  border-color: var(--accent-color) !important;
}

/* 弹窗 */
.el-dialog {
  border-radius: 20px !important;
}
.el-dialog__title {
  color: var(--text-main) !important;
  font-weight: bold;
}
</style>
