<template>
  <div class="plan-wrapper">
    <div class="plan-card">
      <div class="card-header">
        <h2>学习计划设定</h2>
        <p>{{ msg }}</p>
      </div>

      <div class="plan-content">
        <div class="current-plan">
          <span class="label">每日目标</span>
          <div class="number">{{ numWords }}</div>
          <span class="unit">词</span>
        </div>

        <div class="slider-area">
          <el-slider
            v-model="numWords"
            :min="5"
            :max="100"
            :step="5"
            show-stops
            :disabled="studying === 'none'">
          </el-slider>
          <div class="slider-labels">
            <span>5词 (轻松)</span>
            <span>100词 (挑战)</span>
          </div>
        </div>

        <div class="tips-box">
          <i class="el-icon-info"></i>
          <p>建议每天学习20-30个单词，保持循序渐进。</p>
        </div>

        <el-button
          type="primary"
          @click="submit_plan"
          :disabled="studying === 'none' || studying === 'Private'"
          class="save-btn"
          round>
          保存学习计划
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getCookie } from '../../util.js'
import $ from 'jquery'
export default {
  name: 'Plan',
  data () {
    return {
      numWords: 20,
      studying: 'none',
      num: 0,
      msg: '正在加载单词书信息...'
    }
  },
  methods: {
    submit_plan: function () {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/submit_plan/' + this.numWords,
        crossDomain: true,
        xhrFields: {
          withCredentials: true
        },
        dataType: 'json',
        data: {},
        success: (result) => {
          if (result.info === 'success') {
            // 修改点 1：成功提示
            this.$message({
              message: '计划制定成功！',
              type: 'success',
              duration: 1500,
              offset: 80
            });
          } else {
            // 修改点 2：失败提示
            this.$message({
              message: '制定计划失败',
              type: 'error',
              duration: 2000,
              offset: 80
            });
          }
        },
        error: () => {
          // 修改点 3：网络错误提示
          this.$message({
            message: '请求失败，请检查网络',
            type: 'error',
            duration: 2000,
            offset: 80
          });
        }
      })
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/get_book/' + getCookie('username'),
      crossDomain: true,
      xhrFields: {
        withCredentials: true
      },
      dataType: 'json',
      data: {},
      success: (result) => {
        if (result === null) {
          // 修改点 4：警告提示
          this.$message({
            message: '请先选择一本单词书',
            type: 'warning',
            duration: 2000,
            offset: 80
          });
          this.$router.push({ path: '/wordbooks/list' });
        } else {
          this.studying = result.title;
          this.num = result.num;
          this.msg = `针对：《${this.studying}》`;
        }
      },
      error: () => {
        // 修改点 5：获取信息失败提示
        this.$message({
          message: '获取单词书信息失败',
          type: 'error',
          duration: 2000,
          offset: 80
        });
      }
    })
  }
}
</script>

<style scoped>
.plan-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.plan-card {
  width: 100%;
  max-width: 500px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 30px;
  text-align: center;
}

.card-header h2 { margin: 0 0 10px; color: var(--text-main); }
.card-header p { color: var(--text-muted); font-size: 14px; }

.current-plan {
  margin: 40px 0;
}

.current-plan .number {
  font-size: 72px;
  font-weight: bold;
  color: var(--accent-color);
  line-height: 1;
}

.slider-area {
  padding: 0 20px;
  margin-bottom: 40px;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 10px;
}

.tips-box {
  background: rgba(255, 255, 255, 0.05);
  padding: 15px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 30px;
}

.save-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
}
</style>
