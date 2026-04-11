<template>
  <div class="result-wrapper">
    <div class="result-card">
      <div class="result-header">
        <i class="el-icon-medal-1"></i>
        <h2>测试完成</h2>
      </div>

      <div class="score-display">
        <el-progress
          type="circle"
          :percentage="percent"
          :width="180"
          :stroke-width="15"
          :color="progressColors"
          :format="formatText"
          class="score-circle">
        </el-progress>
        <div class="score-summary">
          <p>答对 <b>{{ correctCount }}</b> 题 / 共 {{ totalCount }} 题</p>
          <p class="evaluation">{{ evaluateText }}</p>
        </div>
      </div>

      <div class="result-actions">
        <el-button @click="$router.push('/')" plain class="action-btn">返回首页</el-button>
        <el-button @click="$router.push($router.options.routes[7].children[0].path)" type="primary" class="action-btn">再测一次</el-button>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑基本不变，增加了一个计算评价属性
export default {
  name: 'TestResult',
  data () {
    return {
      percent: 0,
      correctCount: 0,
      totalCount: 30,
      progressColors: [
        {color: '#f43f5e', percentage: 40},
        {color: '#fbbf24', percentage: 70},
        {color: '#10b981', percentage: 90},
        {color: '#38bdf8', percentage: 100}
      ]
    }
  },
  computed: {
    evaluateText () {
      if (this.percent >= 90) return '词汇大师！令人惊叹的表现。';
      if (this.percent >= 70) return '基础扎实，继续保持。';
      if (this.percent >= 60) return '及格通过，还有进步空间。';
      return '请加强练习，再接再厉。';
    }
  },
  methods: {
    formatText () {
      return `${this.percent}%`;
    }
  },
  mounted: function () {
    this.correctCount = this.$route.query.num || 0;
    this.percent = Math.round(this.correctCount * 100 / this.totalCount);
  }
}
</script>

<style scoped>
.result-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.result-card {
  width: 400px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  backdrop-filter: blur(20px);
}

.result-header {
  color: var(--accent-color);
  margin-bottom: 30px;
}
.result-header i { font-size: 40px; margin-bottom: 10px; }
.result-header h2 { margin: 0; color: var(--text-main); }

.score-display {
  margin-bottom: 40px;
}

/* 穿透修改环形进度条文字样式 */
::v-deep .el-progress__text {
  color: var(--text-main) !important;
  font-size: 36px !important;
  font-weight: bold;
}

.score-summary {
  margin-top: 20px;
}
.score-summary p { margin: 5px 0; color: var(--text-muted); }
.score-summary b { color: var(--accent-color); font-size: 18px; }
.evaluation { color: var(--text-main) !important; font-size: 14px; margin-top: 10px !important; }

.result-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.action-btn {
  width: 120px;
  border-radius: 20px !important;
}
</style>
