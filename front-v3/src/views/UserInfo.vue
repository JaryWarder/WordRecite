<template>
  <div class="user-page">
    <el-skeleton v-if="loading" :rows="10" animated />

    <div v-else class="layout">
      <el-card class="left-card">
        <div class="profile">
          <el-avatar :size="72" class="avatar">{{ avatarText }}</el-avatar>
          <div class="name">{{ displayName }}</div>
          <div class="email">{{ user.email || '-' }}</div>
        </div>

        <div class="security">
          <el-button type="primary" class="primary-btn" @click="openPwdDialog">修改密码</el-button>
        </div>

        <div class="bottom-actions">
          <el-button type="danger" plain class="danger-btn" @click="logout">退出登录</el-button>
        </div>
      </el-card>

      <el-card class="right-card">
        <div class="dash-header">
          <div>
            <div class="dash-title">学习数据看板</div>
            <div class="dash-sub">正在学习：{{ studyingTitle }}</div>
          </div>
          <el-button type="primary" plain :loading="reloading" @click="reload">刷新</el-button>
        </div>

        <div class="stats-grid">
          <div class="stat">
            <div class="stat-label">学习计划</div>
            <div class="stat-value">{{ planValue }}</div>
          </div>
          <div class="stat">
            <div class="stat-label">已学习</div>
            <div class="stat-value">{{ studiedValue }}</div>
          </div>
          <div class="stat">
            <div class="stat-label">已掌握</div>
            <div class="stat-value">{{ masteredValue }}</div>
          </div>
          <div class="stat">
            <div class="stat-label">待复习</div>
            <div class="stat-value">{{ dueValue }}</div>
          </div>
        </div>

        <div class="progress-box">
          <div class="progress-text">
            <span>总体进度</span>
            <span>{{ progressText }}</span>
          </div>
          <el-progress :percentage="progressPercentage" :show-text="false" :stroke-width="8" color="#38bdf8" />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSubmitting" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { getCurrentBook } from '../api/books'
import { loginRequest } from '../api/auth'
import { useUserStore } from '../stores/user'
import { getUserInfo, updatePassword } from '../api/user'
import { encryptPasswordToHex } from '../utils/rsa'
import { getDashboardData } from '../api/progress'

const router = useRouter()
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
  mastered: null,
  due: null
})

const studyingBookNum = ref(0)

const displayName = computed(() => user.username || username.value || 'Guest')
const avatarText = computed(() => String(displayName.value || 'G').slice(0, 1).toUpperCase())
const studyingTitle = computed(() => user.studying || 'none')
const planValue = computed(() => Number(user.plan || 0))
const studiedValue = computed(() => Number(user.studied || 0))
const masteredValue = computed(() => Number(user.mastered || 0))
const dueValue = computed(() => Number(user.due || 0))

const progressPercentage = computed(() => {
  const total = Number(studyingBookNum.value || 0)
  const done = studiedValue.value
  if (!total || total <= 0) return 0
  return Math.min(100, Math.max(0, Math.round((done / total) * 100)))
})

const progressText = computed(() => {
  const total = Number(studyingBookNum.value || 0)
  const done = studiedValue.value
  if (!total || total <= 0) return '0%'
  return `${done} / ${total}`
})

const pwdDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value === pwdForm.oldPassword) {
          callback(new Error('新密码不能与旧密码相同'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

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
  user.studying = data.studying || ''
}

async function fetchDashboard () {
  if (!user.studying || user.studying === 'none') {
    return
  }
  try {
    const res = await getDashboardData(user.studying)
    if (res.code === 200 && res.data) {
      const d = res.data
      user.plan = d.bookSize ?? 0
      user.studied = d.learnedUnique ?? 0
      user.mastered = d.masteredCount ?? 0
      user.due = d.dueCount ?? 0
    }
  } catch (err) {
    console.error('Failed to fetch dashboard data', err)
  }
}

async function fetchStudyingBook () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    studyingBookNum.value = 0
    return
  }
  const result = await getCurrentBook(username.value)
  if (result.code !== 200) {
    studyingBookNum.value = 0
    return
  }
  studyingBookNum.value = Number(result.data?.num || 0)
}

async function reload () {
  reloading.value = true
  try {
    await Promise.all([fetchUserInfo(), fetchStudyingBook()])
    await fetchDashboard()
    ElMessage({ message: '已刷新', type: 'success', duration: 1000, offset: 80 })
  } catch (e) {
    ElMessage({ message: e?.message || '获取用户信息失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    reloading.value = false
  }
}

function openPwdDialog () {
  pwdDialogVisible.value = true
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  if (pwdFormRef.value) pwdFormRef.value.clearValidate()
}

function logout () {
  userStore.logout()
  router.push('/')
}

async function submitPassword () {
  if (pwdSubmitting.value) return
  if (!pwdFormRef.value) return

  await pwdFormRef.value.validate()
  pwdSubmitting.value = true
  try {
    const keyResp = await loginRequest()
    if (keyResp.code !== 200) {
      throw new Error(keyResp.msg || '获取公钥失败')
    }
    const pubExpHex = keyResp.data?.pub_exp
    const pubModHex = keyResp.data?.pub_mod
    if (!pubExpHex || !pubModHex) {
      throw new Error('公钥信息缺失')
    }

    const oldEncrypted = encryptPasswordToHex({ password: pwdForm.oldPassword, pubExpHex, pubModHex })
    const newEncrypted = encryptPasswordToHex({ password: pwdForm.newPassword, pubExpHex, pubModHex })

    const resp = await updatePassword({ oldEncrypted, newEncrypted })
    if (resp.code !== 200) {
      throw new Error(resp.msg || '修改密码失败')
    }

    ElMessage({ message: '密码修改成功，请重新登录', type: 'success', duration: 1500, offset: 80 })
    pwdDialogVisible.value = false
    userStore.logout()
    router.push('/')
  } catch (e) {
    ElMessage({ message: e?.message || '修改密码失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchUserInfo(), fetchStudyingBook()])
    await fetchDashboard()
  } catch (e) {
    ElMessage({ message: e?.message || '获取用户信息失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.user-page {
  padding: 20px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  align-items: stretch;
}

.left-card,
.right-card {
  border-radius: 20px;
  min-height: 520px;
}

.left-card {
  display: flex;
  flex-direction: column;
}

.profile {
  padding: 10px 6px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar {
  background: rgba(56, 189, 248, 0.18);
  color: var(--accent-color);
  font-weight: 900;
}

.name {
  font-size: 22px;
  font-weight: 900;
  color: var(--text-main);
  text-align: center;
}

.email {
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
}

.security {
  margin-top: 18px;
  display: flex;
  justify-content: center;
}

.primary-btn {
  width: 240px;
  height: 42px;
}

.bottom-actions {
  margin-top: auto;
  padding-top: 18px;
  display: flex;
  justify-content: center;
}

.danger-btn {
  width: 240px;
  height: 42px;
}

.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.dash-title {
  font-size: 18px;
  font-weight: 900;
  color: var(--text-main);
}

.dash-sub {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 12px 0 18px;
}

.stat {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.stat-label {
  color: var(--text-muted);
  font-size: 12px;
}

.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 900;
  color: var(--accent-color);
  line-height: 1.2;
}

.progress-box {
  margin-top: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.progress-text {
  display: flex;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 13px;
  margin-bottom: 10px;
}
</style>
