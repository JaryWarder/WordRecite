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
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import ChangeStudying from '../components/small/ChangeStudying.vue'
import { getCookie, setCookie } from '../utils/cookie'

const router = useRouter()

const bookList = computed(() => [
  { id: 1, title: 'CET-4', num: 2000 },
  { id: 2, title: 'CET-6', num: 2500 },
  { id: 3, title: 'TOEFL', num: 3500 }
])

const studying = ref(getCookie('studying') || 'none')

function chooseBook (book) {
  studying.value = book.title
  setCookie('studying', book.title, 7)
  ElMessage({ message: `已选择《${book.title}》（接口待联调）`, type: 'success', duration: 1500, offset: 80 })
}

function goPlan () {
  router.push('/plan')
}

function goStart () {
  router.push({ path: '/start', query: { num: 20 } })
}
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
