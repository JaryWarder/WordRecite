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
            :disabled="studying === 'none'"
          />
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
          :loading="saving"
          class="save-btn"
          round
        >
          保存学习计划
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { submitPlan } from '../api/plan'
import { useUserStore } from '../stores/user'
import { getCookie } from '../utils/cookie'

const router = useRouter()
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const numWords = ref(20)
const studying = ref(getCookie('studying') || 'none')
const saving = ref(false)

const msg = computed(() => (studying.value === 'none' ? '请先选择一本单词书' : `针对：《${studying.value}》`))

async function submit_plan () {
  if (saving.value) return

  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    return
  }

  saving.value = true
  try {
    const result = await submitPlan({ user: username.value, numWords: numWords.value })
    if (result.code !== 200) {
      throw new Error(result.msg || '保存学习计划失败')
    }
    ElMessage({ message: '学习计划已保存', type: 'success', duration: 1500, offset: 80 })
    router.push({ path: '/start', query: { num: numWords.value } })
  } catch (e) {
    ElMessage({ message: e?.message || '保存学习计划失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    saving.value = false
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

.card-header h2 {
  margin: 0 0 10px;
  color: var(--text-main);
}

.card-header p {
  color: var(--text-muted);
  font-size: 14px;
}

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
