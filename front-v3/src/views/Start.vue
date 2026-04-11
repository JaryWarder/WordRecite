<template>
  <div class="start-wrapper">
    <el-card class="start-card">
      <template #header>
        <div class="header">
          <h2><i class="el-icon-caret-right"></i> 开始背单词</h2>
          <span class="sub">{{ studying === 'none' ? '请先选择词书' : `当前词书：${studying}` }}</span>
        </div>
      </template>

      <div class="content">
        <div class="plan-box">
          <div class="plan-title">本次学习目标</div>
          <div class="plan-number">{{ num }}</div>
          <div class="plan-unit">词</div>
        </div>

        <div class="tips">
          <i class="el-icon-info"></i>
          <span>点击开始后进入单词卡片模式（接口待联调时使用模拟数据）。</span>
        </div>

        <el-button type="primary" class="start-btn" :disabled="studying === 'none'" @click="goWordCard">
          开始学习
        </el-button>
      </div>
    </el-card>

    <ChangeStudying v-if="studying === 'none'" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ChangeStudying from '../components/small/ChangeStudying.vue'
import { getCookie } from '../utils/cookie'

const router = useRouter()
const route = useRoute()

const studying = computed(() => getCookie('studying') || 'none')
const num = computed(() => Number(route.query.num || 20))

function goWordCard () {
  router.push({ path: '/wordCard', query: { num: num.value } })
}
</script>

<style scoped>
.start-wrapper {
  padding: 20px;
}

.start-card {
  min-height: 420px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-main);
}

.sub {
  color: var(--text-muted);
  font-size: 13px;
}

.content {
  padding: 30px 10px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.plan-box {
  text-align: center;
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  width: 280px;
}

.plan-title {
  color: var(--text-muted);
  font-size: 13px;
}

.plan-number {
  font-size: 64px;
  font-weight: 800;
  color: var(--accent-color);
  line-height: 1.2;
}

.plan-unit {
  color: var(--text-muted);
  font-size: 13px;
}

.tips {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
  background: rgba(255, 255, 255, 0.04);
  padding: 10px 14px;
  border-radius: 10px;
}

.start-btn {
  width: 280px;
  height: 44px;
}
</style>

