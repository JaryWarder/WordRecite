<template>
  <div class="test-start-wrapper">
    <el-card class="test-start-card" shadow="never">
      <div class="header-section">
        <div class="title-group">
          <i class="el-icon-medal title-icon"></i>
          <h2>能力评估测试</h2>
        </div>
        <p class="sub-text">通过整套试卷拉取，精准定位记忆薄弱点</p>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else class="main-content">
        <div class="stats-vertical-stack">
          <div class="stat-box book-box">
            <div class="stat-content">
              <span class="stat-label"><i class="el-icon-notebook-2"></i> 当前词书</span>
              <div class="stat-value-large">{{ selectedBookTitle || '未选择' }}</div>
            </div>
            <div class="stat-bg-icon"><i class="el-icon-notebook-2"></i></div>
          </div>

          <div class="stat-box learned-box">
            <div class="stat-content">
              <span class="stat-label"><i class="el-icon-finished"></i> 词书学习进度</span>
              <div class="stat-value-medium">
                <span class="num">{{ studied }}</span>
                <span class="unit">/ {{ currentBookTotal }} Words</span>
              </div>
            </div>
            <el-progress 
              type="circle" 
              :percentage="learningPercentage" 
              :width="60" 
              :stroke-width="5"
              color="#38bdf8"
            >
              <template #default>
                <span class="percentage-text">{{ learningPercentage }}%</span>
              </template>
            </el-progress>
          </div>
        </div>

        <div class="divider">
          <span>测试配置</span>
        </div>

        <div class="config-section">
          <el-form label-position="top" class="custom-form">
            <div class="form-grid">
              <el-form-item label="词书范围">
                <el-select v-model="selectedBookTitle" placeholder="请选择词书" class="modern-select">
                  <template #prefix>
                    <i class="el-icon-collection"></i>
                  </template>
                  <el-option v-for="b in bookOptions" :key="b.title" :label="b.title" :value="b.title" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="单次题量">
                <el-select v-model="selectedCount" placeholder="请选择题量" class="modern-select">
                  <template #prefix>
                    <i class="el-icon-document-copy"></i>
                  </template>
                  <el-option :label="'10 题 (快速)'" :value="10" />
                  <el-option :label="'20 题 (标准)'" :value="20" />
                  <el-option :label="'30 题 (深度)'" :value="30" />
                </el-select>
              </el-form-item>
            </div>
          </el-form>
        </div>

        <div class="footer-actions">
          <el-alert
            v-if="studied < 20"
            type="warning"
            :closable="false"
            show-icon
            title="基础不牢，地动山摇！需背诵满 20 词方可解锁测试。"
            class="gate-alert"
          />

          <div class="btn-wrap">
            <el-tooltip
              v-if="gateStudied"
              content="起码背诵 20 词才能开启测试功能哦"
              placement="top"
            >
              <el-button type="primary" class="start-btn disabled" disabled>开始测试</el-button>
            </el-tooltip>

            <el-tooltip v-else-if="gateBook" content="请先选择词书" placement="top">
              <el-button type="primary" class="start-btn disabled" disabled>开始测试</el-button>
            </el-tooltip>

            <el-button v-else type="primary" class="start-btn animated-btn" @click="goTest">
              立即开始测试 <i class="el-icon-right"></i>
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { getBooks, getCurrentBook } from '../api/books'
import { getUserInfo } from '../api/user'
import { useUserStore } from '../stores/user'
import { getCookie, setCookie } from '../utils/cookie'

const router = useRouter()
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(false)
const studied = ref(0)
const selectedBookTitle = ref(getCookie('studying') || '')
const selectedCount = ref(10)
const books = ref([])

const bookOptions = computed(() => {
  const list = Array.isArray(books.value) ? books.value : []
  return list
    .map((b) => ({ title: b?.title || '', num: b?.num ?? 0 }))
    .filter((b) => Boolean(b.title))
})

// 修改点 1：获取当前选中词书的总词数
const currentBookTotal = computed(() => {
  const book = bookOptions.value.find(b => b.title === selectedBookTitle.value)
  return book ? book.num : 100 // 默认值设为100防止初次加载除以0
})

// 修改点 2：计算真实学习进度百分比
const learningPercentage = computed(() => {
  if (currentBookTotal.value <= 0) return 0
  const p = Math.round((studied.value / currentBookTotal.value) * 100)
  return Math.min(100, p)
})

const gateStudied = computed(() => studied.value < 20)
const gateBook = computed(() => !selectedBookTitle.value)

async function fetchUserBasics () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return
  const result = await getUserInfo(username.value)
  if (result.code === 200) {
    studied.value = Number(result.data?.studied || 0)
    const studying = result.data?.studying
    if (studying) {
      selectedBookTitle.value = studying
      setCookie('studying', studying, 7)
    }
  }
}

async function fetchBooks () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return
  const result = await getBooks(username.value)
  if (result.code === 200) {
    books.value = Array.isArray(result.data) ? result.data : []
  }
}

async function syncCurrentBook () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return
  const result = await getCurrentBook(username.value)
  if (result.code === 200 && result.data?.title) {
    selectedBookTitle.value = result.data.title
    setCookie('studying', result.data.title, 7)
  }
}

function goTest () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }
  router.push({
    path: '/test',
    query: {
      username: username.value,
      bookTitle: selectedBookTitle.value,
      count: String(selectedCount.value)
    }
  })
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchBooks(), fetchUserBasics(), syncCurrentBook()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* 样式保持之前的风格，仅微调进度条文字 */
.test-start-wrapper {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.test-start-card {
  width: 100%;
  max-width: 600px;
  border-radius: 24px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  padding: 10px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
}

.title-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 28px;
  color: var(--accent-color);
}

.header-section h2 {
  margin: 0;
  font-size: 26px;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 800;
}

.sub-text {
  color: var(--text-muted);
  font-size: 14px;
}

.stats-vertical-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 30px;
}

.stat-box {
  position: relative;
  overflow: hidden;
  padding: 24px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-box {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);
}

.stat-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 13px;
  margin-bottom: 10px;
}

.stat-value-large {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-main);
}

.stat-value-medium .num {
  font-size: 32px;
  font-weight: 800;
  color: var(--accent-color);
}

.stat-value-medium .unit {
  font-size: 14px;
  color: var(--text-muted);
  margin-left: 8px;
}

.percentage-text {
  font-size: 12px;
  font-weight: 700;
  color: var(--accent-color);
}

.divider {
  display: flex;
  align-items: center;
  margin: 30px 0 20px;
  color: var(--text-muted);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.divider::before, .divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--glass-border), transparent);
  margin: 0 15px;
}

.config-section {
  margin-bottom: 30px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.modern-select {
  width: 100% !important;
}

:deep(.el-form-item__label) {
  color: var(--text-muted) !important;
  font-size: 13px !important;
}

.footer-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.gate-alert {
  width: 100%;
  border-radius: 12px;
}

.btn-wrap {
  width: 100%;
  display: flex;
  justify-content: center;
}

.start-btn {
  width: 100%;
  max-width: 320px;
  height: 54px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 16px;
}

.animated-btn {
  background: var(--accent-gradient);
  box-shadow: 0 8px 20px rgba(56, 189, 248, 0.3);
}

@media (max-width: 480px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>