<template>
  <div class="books-wrapper">
    <el-card class="books-card">
      <template #header>
        <div class="card-header">
          <h2>选择学习书本</h2>
          <span class="sub-text">选择一本词书开始你的旅程</span>
        </div>
      </template>

      <div class="books-grid">
        <div
          v-for="book in bookList"
          :key="book.id"
          class="book-card"
          :class="{ active: studying === book.title }"
        >
          <div class="book-cover">
            <i class="el-icon-notebook-2"></i>
            <span class="book-badge" v-if="studying === book.title">当前</span>
          </div>

          <div class="book-info">
            <h3 class="book-title">{{ book.title }}</h3>
            <div class="book-meta">
              <span><i class="el-icon-collection-tag"></i> {{ book.num }} 词</span>
            </div>
          </div>

          <div class="book-actions">
            <el-button type="primary" plain class="action-btn" @click="chooseBook(book)">选择</el-button>
            <el-button type="success" plain class="action-btn" @click="goPlan">制定计划</el-button>
            <el-button type="warning" plain class="action-btn" @click="goStart">开始背诵</el-button>
          </div>
        </div>
      </div>

      <ChangeStudying v-if="studying === 'none'" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import ChangeStudying from '../components/small/ChangeStudying.vue'
import { getBooks, getCurrentBook } from '../api/books'
import { changeBook } from '../api/change'
import { useUserStore } from '../stores/user'
import { getCookie, setCookie } from '../utils/cookie'

const router = useRouter()

const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const studying = ref(getCookie('studying') || 'none')
const books = ref([])
const loading = ref(false)
const switching = ref(false)

const bookList = computed(() => {
  const list = Array.isArray(books.value) ? books.value : []
  return list
    .map((b, index) => ({
      id: index + 1,
      title: b?.title || '',
      num: b?.num ?? 0
    }))
    .filter((b) => Boolean(b.title))
})

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

  studying.value = title
  setCookie('studying', title, 7)
}

async function chooseBook (book) {
  if (switching.value) return

  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    return
  }

  switching.value = true
  try {
    const result = await changeBook({ username: username.value, newTitle: book.title })
    if (result.code !== 200) {
      throw new Error(result.msg || '切换词书失败')
    }
    studying.value = book.title
    setCookie('studying', book.title, 7)
    ElMessage({ message: `已切换为《${book.title}》`, type: 'success', duration: 1500, offset: 80 })
  } catch (e) {
    ElMessage({ message: e?.message || '切换词书失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    switching.value = false
  }
}

function goPlan () {
  router.push('/plan')
}

function goStart () {
  router.push({ path: '/start', query: { num: 20 } })
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchBooks()
    await syncCurrentBook()
  } catch (e) {
    ElMessage({ message: e?.message || '加载失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.books-wrapper {
  padding: 20px;
}

.books-card {
  min-height: 600px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-main);
}

.sub-text {
  color: var(--text-muted);
  font-size: 14px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.book-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.book-card.active {
  border: 1px solid var(--accent-color);
  background: rgba(56, 189, 248, 0.1);
}

.book-cover {
  height: 140px;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--accent-color);
  position: relative;
}

.book-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--success-color);
  color: white;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.book-info {
  padding: 20px;
  flex-grow: 1;
}

.book-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--text-main);
}

.book-meta {
  color: var(--text-muted);
  font-size: 14px;
}

.book-actions {
  padding: 15px 20px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
}
</style>
