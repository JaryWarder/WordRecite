<template>
  <div class="test-start-wrapper">
    <div class="briefing-card">
      <div class="briefing-header">
        <i class="el-icon-aim"></i>
        <h2>词汇量测试</h2>
      </div>

      <div class="briefing-content">
        <p class="target-book">{{ msg }}</p>
        <div class="test-meta">
          <div class="meta-item">
            <span class="label">测试题量</span>
            <span class="value highlight">30</span>
          </div>
          <div class="meta-item">
            <span class="label">预计耗时</span>
            <span class="value">2 min</span>
          </div>
        </div>

        <el-alert
          v-if="!canTest && isReady"
          title="前置条件未满足"
          description="您需要至少掌握 30 个单词才能开启测试模块。请先前往背单词模块积累词汇量。"
          type="error"
          show-icon
          :closable="false"
          class="custom-alert">
        </el-alert>

        <div class="action-footer">
          <el-button
            @click="$router.push($router.options.routes[7].children[1].path)"
            :disabled="!canTest"
            type="primary"
            class="start-test-btn"
            :loading="!isReady">
            {{ isReady ? (canTest ? '开始挑战' : '条件不足') : '加载配置中...' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑完全不变
import $ from 'jquery'
import { getCookie, setCookie } from '../../util.js'
export default {
  name: 'TestStart',
  data () {
    return {
      title: '',
      num: 0,
      msg: '正在读取测试配置...',
      canTest: false,
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
          this.$message.warning('您尚未选择学习的单词书');
          this.$router.push('/wordbooks/list');
        } else if (result.info === 'success') {
          this.title = result.studying;
          setCookie('studying', this.title, 1);
          this.num = parseInt(result.num);
          this.canTest = parseInt(result.studied) >= 30;
          this.msg = `目标词库：${this.title}`;
          this.isReady = true;
        } else {
          this.$message.error('获取测试信息失败');
        }
      },
      error: () => { this.$message.error('请求失败，请检查网络连接'); }
    })
  }
}
</script>

<style scoped>
.test-start-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.briefing-card {
  width: 400px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
}

.briefing-header {
  background: rgba(0, 0, 0, 0.2);
  padding: 30px;
  text-align: center;
  color: var(--accent-color);
}
.briefing-header i { font-size: 40px; margin-bottom: 10px; }
.briefing-header h2 { margin: 0; color: var(--text-main); font-size: 24px; }

.briefing-content {
  padding: 30px;
}

.target-book {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 30px;
}

.test-meta {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.05);
  padding: 15px;
  border-radius: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.label { font-size: 12px; color: var(--text-muted); margin-bottom: 5px; }
.value { font-size: 20px; font-weight: bold; color: var(--text-main); }
.value.highlight { color: var(--accent-color); }

.start-test-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  letter-spacing: 2px;
}

.custom-alert {
  background: rgba(244, 63, 94, 0.1);
  border: 1px solid var(--danger-color);
  margin-bottom: 20px;
}
::v-deep .el-alert__title { color: var(--danger-color); font-weight: bold; }
::v-deep .el-alert__description { color: var(--text-main); }
::v-deep .el-alert__icon { color: var(--danger-color); }
</style>
