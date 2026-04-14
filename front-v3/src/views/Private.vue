<template>
  <div class="private-wrapper">
    <el-card class="private-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <i class="el-icon-collection-tag header-icon"></i>
            <div>
              <h2>生词本</h2>
              <div class="sub-text">你收藏的单词会显示在这里</div>
            </div>
          </div>
          <el-button 
            type="primary" 
            plain 
            class="refresh-btn" 
            :loading="loading" 
            @click="reload"
          >
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else>
        <el-empty v-if="rows.length === 0" description="生词本为空" :image-size="100" />

        <el-table 
          v-else 
          :data="rows" 
          class="private-table"
          :header-cell-style="{ background: 'rgba(255, 255, 255, 0.05)', color: 'var(--text-main)' }"
        >
          <el-table-column prop="word" label="单词" min-width="140">
            <template #default="{ row }">
              <span class="word-text">{{ row.word }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="poses" label="释义" min-width="300">
            <template #default="{ row }">
              <div class="poses-text">{{ row.poses }}</div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button 
                type="danger" 
                link
                class="remove-link"
                :loading="deletingWord === row.word" 
                @click="remove(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { deletePrivate, getPrivate } from '../api/private'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(false)
const rows = ref([])
const deletingWord = ref('')

async function fetchPrivate () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    rows.value = []
    return
  }
  const result = await getPrivate(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取生词本失败')
  }
  rows.value = Array.isArray(result.data) ? result.data : []
}

async function reload () {
  loading.value = true
  try {
    await fetchPrivate()
  } catch (e) {
    ElMessage({ message: e?.message || '获取生词本失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
}

async function remove (row) {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    return
  }

  try {
    await ElMessageBox.confirm(`确认移除「${row.word}」吗？`, '提示', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'glass-message-box'
    })
  } catch {
    return
  }

  deletingWord.value = row.word
  try {
    const result = await deletePrivate({ username: username.value, word: row.word })
    if (result.code !== 200) {
      throw new Error(result.msg || '移除失败')
    }
    rows.value = rows.value.filter((x) => x.word !== row.word)
    ElMessage({ message: '已移除', type: 'success', duration: 1200, offset: 80 })
  } catch (e) {
    ElMessage({ message: e?.message || '移除失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    deletingWord.value = ''
  }
}

onMounted(async () => {
  await reload()
})
</script>

<style scoped>
.private-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.private-card {
  width: 100%;
  max-width: 1000px;
  min-height: 520px;
  border-radius: 24px;
  /* 已经在 App.vue 定义了全局毛玻璃，这里确保应用 */
  background: var(--glass-bg) !important;
  border: 1px solid var(--glass-border) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  color: var(--accent-color);
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-main);
}

.sub-text {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.refresh-btn {
  border-radius: 10px;
}

/* 表格毛玻璃化核心 CSS */
.private-table {
  width: 100%;
  background-color: transparent !important;
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: transparent !important;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.05) !important;
  --el-table-border-color: var(--glass-border);
  --el-table-text-color: var(--text-main);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-table__inner-wrapper::before) {
  display: none; /* 去掉表格底部白线 */
}

.word-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-color);
}

.poses-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main);
  opacity: 0.9;
}

.remove-link {
  font-weight: 600;
  transition: all 0.3s;
}

.remove-link:hover {
  filter: brightness(1.2);
  text-decoration: underline;
}

/* 适配移动端 */
@media (max-width: 768px) {
  .private-card {
    border-radius: 16px;
  }
  .word-text {
    font-size: 16px;
  }
}
</style>