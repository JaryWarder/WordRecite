<template>
  <div class="review-wrapper">
    <el-card class="review-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>每日复习</h2>
            <div class="sub-text">今日需要复习的单词列表</div>
          </div>
          <el-button type="primary" plain :loading="loading" @click="reload">刷新</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else>
        <el-empty v-if="rows.length === 0" description="今日暂无复习任务" />

        <el-table v-else :data="rows" stripe class="review-table">
          <el-table-column prop="word" label="单词" min-width="120" />
          <el-table-column prop="poses" label="释义" min-width="220" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'no' ? 'danger' : row.status === 'yes' ? 'success' : 'info'">
                {{ row.status || '-' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { getDaily } from '../api/review'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(false)
const rows = ref([])

async function fetchDaily () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    rows.value = []
    return
  }

  const result = await getDaily(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取复习列表失败')
  }
  rows.value = Array.isArray(result.data) ? result.data : []
}

async function reload () {
  loading.value = true
  try {
    await fetchDaily()
  } catch (e) {
    ElMessage({ message: e?.message || '获取复习列表失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await reload()
})
</script>

<style scoped>
.review-wrapper {
  padding: 20px;
}

.review-card {
  min-height: 520px;
  border-radius: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
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

.review-table {
  width: 100%;
}
</style>
