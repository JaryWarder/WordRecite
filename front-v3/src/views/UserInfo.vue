<template>
  <div class="user-info-wrapper">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <div class="title">个人中心</div>
          <div class="subtitle" v-if="username">Hi, {{ username }}</div>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else class="content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">
            {{ user.username || username }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ user.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="学习计划">
            {{ user.plan ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="正在学习">
            {{ user.studying || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="已学习">
            {{ user.studied ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="已完成">
            {{ user.finished ?? '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="actions">
          <el-button type="primary" @click="reload" :loading="reloading">刷新</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../stores/user'
import { getUserInfo } from '../api/user'

const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(true)
const reloading = ref(false)
const user = reactive({
  username: '',
  email: '',
  plan: null,
  studying: '',
  studied: null,
  finished: null
})

async function fetchUserInfo () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    return
  }
  const result = await getUserInfo(username.value)
  if (result.code !== 200) {
    throw new Error(result.msg || '获取用户信息失败')
  }
  const data = result.data || {}
  user.username = data.username || ''
  user.email = data.email || ''
  user.plan = data.plan ?? null
  user.studying = data.studying || ''
  user.studied = data.studied ?? null
  user.finished = data.finished ?? null
}

async function reload () {
  reloading.value = true
  try {
    await fetchUserInfo()
    ElMessage({ message: '已刷新', type: 'success', duration: 1000, offset: 80 })
  } catch (e) {
    ElMessage({ message: e?.message || '获取用户信息失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    reloading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchUserInfo()
  } catch (e) {
    ElMessage({ message: e?.message || '获取用户信息失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.user-info-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.user-card {
  width: 100%;
  max-width: 700px;
  border-radius: 20px;
}

.card-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
}

.subtitle {
  color: var(--text-muted);
  font-size: 13px;
}

.actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

