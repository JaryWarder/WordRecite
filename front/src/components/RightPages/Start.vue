<template>
  <div class="start-container">
    <div class="mission-card">
      <div class="mission-header">
        <h2>今日任务</h2>
        <p class="mission-date">{{ msg }}</p>
      </div>

      <div class="mission-stats">
        <div class="stat-item">
          <span class="stat-label">计划学习</span>
          <div class="stat-number">{{ plan }}</div>
          <span class="stat-unit">词</span>
        </div>
      </div>

      <div class="action-area">
        <el-button
          @click="$router.push({path: $router.options.routes[4].children[1].path, query: {num: num}})"
          type="primary"
          class="start-button-lg"
          :disabled="!isReady"
          icon="el-icon-video-play">
          {{ isReady ? '开始专注学习' : '数据准备中...' }}
        </el-button>
        <p class="tip-text" v-if="isReady">保持专注，每一个单词都是进步</p>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑完全不变
import $ from 'jquery'
import { getCookie, setCookie } from '../../util.js'
export default {
  name: 'Start',
  data () {
    return {
      title: '',
      num: 0,
      plan: 0,
      msg: '正在加载学习信息...',
      isReady: false
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/start_info/' + getCookie('username'),
      crossDomain: true,
      xhrFields: { withCredentials: true },
      dataType: 'json',
      data: {},
      success: (result) => {
        if (result.info === 'no_book') {
          this.$message.warning('请先选择一本要学习的单词书');
          this.$router.push('/wordbooks/list');
        } else if (result.info === 'success') {
          this.title = result.studying;
          setCookie('studying', this.title, 1);
          this.num = parseInt(result.num);
          this.plan = parseInt(result.plan);
          this.msg = `当前正在学习：《${this.title}》 | 词库总量：${this.num}`;
          this.isReady = true;
        } else {
          this.$message.error('获取学习信息失败');
        }
      },
      error: () => { this.$message.error('请求失败，请检查网络连接'); }
    })
  }
}
</script>

<style scoped>
.start-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.mission-card {
  width: 100%;
  max-width: 500px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
}

.mission-header h2 {
  font-size: 32px;
  margin: 0 0 10px;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.mission-date {
  color: var(--text-muted);
  font-size: 14px;
}

.mission-stats {
  margin: 50px 0;
}

.stat-number {
  font-size: 96px;
  font-weight: 700;
  line-height: 1;
  color: var(--text-main);
  text-shadow: 0 0 30px rgba(56, 189, 248, 0.3);
}

.stat-label, .stat-unit {
  color: var(--text-muted);
  font-size: 16px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.start-button-lg {
  width: 100%;
  height: 60px;
  font-size: 20px;
  border-radius: 30px;
  margin-bottom: 20px;
}

.tip-text {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
