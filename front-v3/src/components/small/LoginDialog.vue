<template>
  <div class="dialog-trigger">
    <el-button class="glass-btn" size="small" @click="buttonClicked">登录</el-button>
    <el-dialog
      v-model="dialogFormVisible"
      title="欢迎回来"
      width="420px"
      :append-to-body="true"
      class="glass-dialog"
    >
      <div class="dialog-body">
        <el-form ref="loginFormRef" :model="form" status-icon :rules="loginRules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入您的用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" placeholder="请输入密码" type="password" />
          </el-form-item>
          <div class="forgot-password">
            <el-button type="text" @click="onSendEmail">忘记密码?</el-button>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submit" :loading="loginLoading" class="submit-btn">立即登录</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/user'
import { encryptPasswordToHex } from '../../utils/rsa'
import { loginCheck, loginRequest, sendEmail } from '../../api/auth'

const emit = defineEmits(['login_success'])

const userStore = useUserStore()
const { isLogin } = storeToRefs(userStore)

const dialogFormVisible = ref(false)
const loginLoading = ref(false)
const loginFormRef = ref()

const form = reactive({
  username: '',
  password: ''
})

function usernameCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请输入用户名'))
  const usernameRE = /^[a-zA-Z]{1}([a-zA-Z0-9]|[_]){4,19}$/
  if (usernameRE.test(value)) return callback()
  return callback(new Error('用户名格式错误：5-20个以字母开头，可包含数字和下划线的字符'))
}

function passwordCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请输入密码'))
  const pwdRE = /^(\w){6,20}$/
  if (pwdRE.test(value)) return callback()
  return callback(new Error('密码格式错误：6-20位，可以包含数字、字母、下划线'))
}

const loginRules = {
  username: [{ validator: usernameCheck, trigger: 'blur' }],
  password: [{ validator: passwordCheck, trigger: 'blur' }]
}

function buttonClicked () {
  if (isLogin.value) {
    ElMessage({ message: '您已登录', type: 'info', duration: 1500, offset: 80 })
    return
  }
  dialogFormVisible.value = true
}

async function onSendEmail () {
  if (!form.username) {
    ElMessage({ message: '请先输入用户名', type: 'warning', duration: 2000, offset: 80 })
    return
  }
  try {
    const result = await sendEmail(form.username)
    if (result.code === 200) {
      ElMessage({ message: '邮件已处理（如配置邮箱则会发送），密码已重置为123456', type: 'success', duration: 3000, offset: 80 })
      dialogFormVisible.value = false
    } else {
      ElMessage({ message: result.msg || '发送失败', type: 'error', duration: 2000, offset: 80 })
    }
  } catch (e) {
    ElMessage({ message: '邮件发送失败', type: 'error', duration: 2000, offset: 80 })
  }
}

async function submit () {
  const valid = await loginFormRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) {
    ElMessage({ message: '输入有误', type: 'error', duration: 1000, offset: 80 })
    return
  }

  loginLoading.value = true
  try {
    const keyResp = await loginRequest()
    if (keyResp.code !== 200) {
      ElMessage({ message: keyResp.msg || '登录失败', type: 'error', duration: 1000, offset: 80 })
      return
    }

    const encrypted = encryptPasswordToHex({
      password: form.password,
      pubExpHex: keyResp.data.pub_exp,
      pubModHex: keyResp.data.pub_mod
    })

    const loginResp = await loginCheck({ username: form.username, encrypted })
    if (loginResp.code === 200) {
      userStore.setAuth({
        token: loginResp.data?.token,
        username: loginResp.data?.username || form.username
      })
      ElMessage({ message: '登录成功', type: 'success', duration: 1000, offset: 80 })
      dialogFormVisible.value = false
      emit('login_success', form.username)
      return
    }

    if (loginResp.code === 401) {
      ElMessage({ message: '密码错误', type: 'error', duration: 1000, offset: 80 })
      return
    }
    if (loginResp.code === 404) {
      ElMessage({ message: '找不到用户', type: 'error', duration: 1000, offset: 80 })
      return
    }

    ElMessage({ message: loginResp.msg || '登录失败', type: 'error', duration: 1000, offset: 80 })
  } catch (e) {
    ElMessage({ message: '登录失败', type: 'error', duration: 1000, offset: 80 })
  } finally {
    loginLoading.value = false
  }
}
</script>

<style scoped>
.dialog-trigger {
  display: inline-block;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.1) !important;
  color: var(--text-main) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.forgot-password {
  text-align: right;
  margin-top: -10px;
}

.submit-btn {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.cancel-btn {
  background: transparent !important;
  border: 1px solid var(--glass-border) !important;
  color: var(--text-muted) !important;
}
</style>
