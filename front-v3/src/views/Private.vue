<template>
  <div class="private-wrapper">
    <el-card class="private-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>生词本</h2>
            <div class="sub-text">你收藏的单词会显示在这里</div>
          </div>
          <el-button type="primary" plain :loading="loading" @click="reload">刷新</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else>
        <el-empty v-if="rows.length === 0" description="生词本为空" />

        <el-table v-else :data="rows" stripe class="private-table">
          <el-table-column prop="word" label="单词" min-width="120" />
          <el-table-column prop="poses" label="释义" min-width="220" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button type="danger" plain size="small" :loading="deletingWord === row.word" @click="remove(row)">
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
      type: 'warning'
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
}

.private-card {
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

.private-table {
  width: 100%;
}
</style>
