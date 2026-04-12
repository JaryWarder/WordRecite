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
          <div class="plan-number">{{ plan }}</div>
          <div class="plan-unit">词</div>
        </div>

        <div class="progress-box" v-if="studying !== 'none'">
          <div class="progress-text">已学 {{ studied }} / {{ plan }}</div>
          <el-progress :percentage="progressPercentage" :show-text="false" :stroke-width="6" color="#38bdf8" />
        </div>

        <div class="tips">
          <i class="el-icon-info"></i>
          <span>{{ tipsText }}</span>
        </div>

        <el-button type="primary" class="start-btn" :loading="loading" :disabled="startDisabled" @click="goWordCard">
          开始学习
        </el-button>
      </div>
    </el-card>

    <ChangeStudying v-if="studying === 'none'" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { startInfo } from '../api/start'
import ChangeStudying from '../components/small/ChangeStudying.vue'
import { useUserStore } from '../stores/user'
import { getCookie, setCookie } from '../utils/cookie'

const router = useRouter()
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(false)
const studying = ref(getCookie('studying') || 'none')
const plan = ref(0)
const studied = ref(0)
const totalBookNum = ref(0)

const startDisabled = computed(() => studying.value === 'none' || plan.value <= 0 || loading.value)

const progressPercentage = computed(() => {
  if (plan.value <= 0) return 0
  const completed = Math.min(Math.max(studied.value, 0), plan.value)
  return Math.min(Math.round((completed / plan.value) * 100), 100)
})

const tipsText = computed(() => {
  if (studying.value === 'none') return '请先选择词书'
  if (plan.value <= 0) return '请先制定学习计划'
  return '点击开始后进入单词卡片模式'
})

async function fetchStartInfo () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return

  const result = await startInfo(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取开始信息失败')
  }

  const data = result.data || {}
  if (data.info === 'no_book') {
    studying.value = 'none'
    setCookie('studying', 'none', 7)
    plan.value = 0
    studied.value = 0
    totalBookNum.value = 0
    return
  }

  const backendStudying = data.studying
  if (backendStudying) {
    studying.value = backendStudying
    setCookie('studying', backendStudying, 7)
  }

  plan.value = Number(data.plan || 0)
  studied.value = Number(data.studied || 0)
  totalBookNum.value = Number(data.num || 0)
}

function goWordCard () {
  if (studying.value === 'none') return
  if (plan.value <= 0) {
    ElMessage({ message: '请先制定学习计划', type: 'warning', duration: 1500, offset: 80 })
    router.push('/plan')
    return
  }
  if (totalBookNum.value <= 0) {
    ElMessage({ message: '词书信息异常，请稍后重试', type: 'error', duration: 2000, offset: 80 })
    return
  }
  router.push({ path: '/wordCard', query: { num: totalBookNum.value, studying: studying.value } })
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchStartInfo()
  } catch (e) {
    ElMessage({ message: e?.message || '获取开始信息失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
})
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

.progress-box {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
}

.progress-text {
  display: flex;
  justify-content: space-between;
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
