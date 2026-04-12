<template>
  <div class="test-start-wrapper">
    <el-card class="test-start-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>单词测试</h2>
            <div class="sub-text">一次拉取整套试卷，答题后立即反馈与错题复盘</div>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else class="content">
        <el-alert
          v-if="studied < 20"
          type="warning"
          :closable="false"
          show-icon
          title="基础不牢，地动山摇！起码背诵 20 词才能开启测试功能哦。"
          class="gate-alert"
        />

        <div class="stats">
          <div class="stat-item">
            <div class="stat-label">已学词数</div>
            <div class="stat-value">{{ studied }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">当前词书</div>
            <div class="stat-value">{{ selectedBookTitle || '-' }}</div>
          </div>
        </div>

        <el-form label-width="90px" class="form">
          <el-form-item label="词书范围">
            <el-select v-model="selectedBookTitle" placeholder="请选择词书" style="width: 280px">
              <el-option v-for="b in bookOptions" :key="b.title" :label="b.title" :value="b.title" />
            </el-select>
          </el-form-item>
          <el-form-item label="题量">
            <el-select v-model="selectedCount" placeholder="请选择题量" style="width: 280px">
              <el-option :label="'10 题'" :value="10" />
              <el-option :label="'20 题'" :value="20" />
              <el-option :label="'30 题'" :value="30" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="actions">
          <el-tooltip
            v-if="gateStudied"
            content="基础不牢，地动山摇！起码背诵 20 词才能开启测试功能哦。"
            placement="top"
          >
            <span>
              <el-button type="primary" class="start-btn" :disabled="true">开始测试</el-button>
            </span>
          </el-tooltip>

          <el-tooltip v-else-if="gateBook" content="请先选择词书" placement="top">
            <span>
              <el-button type="primary" class="start-btn" :disabled="true">开始测试</el-button>
            </span>
          </el-tooltip>

          <el-button v-else type="primary" class="start-btn" @click="goTest">开始测试</el-button>
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

const gateStudied = computed(() => studied.value < 20)
const gateBook = computed(() => !selectedBookTitle.value)

async function fetchUserBasics () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return

  const result = await getUserInfo(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取用户信息失败')
  }
  studied.value = Number(result.data?.studied || 0)

  const studying = result.data?.studying
  if (studying) {
    selectedBookTitle.value = studying
    setCookie('studying', studying, 7)
  }
}

async function fetchBooks () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return

  const result = await getBooks(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取词书列表失败')
  }
  books.value = Array.isArray(result.data) ? result.data : []
}

async function syncCurrentBook () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return

  const result = await getCurrentBook(username.value)
  if (result.code !== 200) return
  const title = result.data?.title
  if (!title) return

  selectedBookTitle.value = title
  setCookie('studying', title, 7)
}

function goTest () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    router.push('/')
    return
  }
  if (!selectedBookTitle.value) {
    ElMessage({ message: '请先选择词书', type: 'warning', duration: 1500, offset: 80 })
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
  } catch (e) {
    ElMessage({ message: e?.message || '加载失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.test-start-wrapper {
  padding: 20px;
}

.test-start-card {
  min-height: 520px;
  border-radius: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-main);
}

.sub-text {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.gate-alert {
  border-radius: 12px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.stat-label {
  color: var(--text-muted);
  font-size: 12px;
}

.stat-value {
  margin-top: 6px;
  color: var(--text-main);
  font-size: 18px;
  font-weight: 700;
}

.form {
  margin-top: 4px;
}

.actions {
  margin-top: 6px;
  display: flex;
  justify-content: center;
}

.start-btn {
  width: 280px;
  height: 44px;
}
</style>
