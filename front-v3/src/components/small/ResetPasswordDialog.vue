<template>
  <el-dialog
    v-model="visible"
    title="重置密码"
    width="440px"
    :append-to-body="true"
    :close-on-click-modal="false"
    @closed="onClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="off" />
      </el-form-item>

      <el-form-item label="验证码" prop="code">
        <div class="code-row">
          <el-input v-model="form.code" placeholder="请输入 6 位验证码" maxlength="6" autocomplete="off" />
          <el-button
            class="send-btn"
            :disabled="countdown > 0 || sendingCode"
            :loading="sendingCode"
            @click="onSendCode"
          >
            {{ countdown > 0 ? `${countdown}s 后重试` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password placeholder="6-20位，字母/数字/下划线" autocomplete="off" />
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入新密码" autocomplete="off" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="cancel-btn" @click="visible = false">取消</el-button>
        <el-button type="primary" class="submit-btn" :loading="submitting" @click="onSubmit">确认重置</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sendEmail, loginRequest, resetPassword } from '../../api/auth'
import { encryptPasswordToHex } from '../../utils/rsa'

const visible = defineModel({ default: false })

const formRef = ref()
const sendingCode = ref(false)
const submitting = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = reactive({
  username: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (/^[a-zA-Z][a-zA-Z0-9_]{4,19}$/.test(value)) return callback()
        callback(new Error('用户名格式错误'))
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^\w{6,20}$/, message: '6-20位，可包含字母、数字、下划线', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) return callback(new Error('两次密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function startCountdown () {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function onSendCode () {
  if (!form.username) {
    ElMessage({ message: '请先输入用户名', type: 'warning', duration: 2000, offset: 80 })
    return
  }
  sendingCode.value = true
  try {
    const res = await sendEmail(form.username)
    if (res.code === 200) {
      ElMessage({ message: '验证码已发送，请查收邮件', type: 'success', duration: 3000, offset: 80 })
      startCountdown()
    } else {
      ElMessage({ message: res.msg || '发送失败', type: 'error', duration: 2000, offset: 80 })
    }
  } catch {
    ElMessage({ message: '发送失败，请稍后重试', type: 'error', duration: 2000, offset: 80 })
  } finally {
    sendingCode.value = false
  }
}

async function onSubmit () {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 1. 获取 RSA 公钥
    const keyResp = await loginRequest()
    if (keyResp.code !== 200) {
      ElMessage({ message: keyResp.msg || '获取公钥失败', type: 'error', duration: 2000, offset: 80 })
      return
    }

    // 2. 加密新密码
    const newEncrypted = encryptPasswordToHex({
      password: form.newPassword,
      pubExpHex: keyResp.data.pub_exp,
      pubModHex: keyResp.data.pub_mod
    })

    // 3. 提交重置
    const res = await resetPassword({
      username: form.username,
      code: form.code,
      newEncrypted
    })

    if (res.code === 200) {
      ElMessage({ message: '密码重置成功，请重新登录', type: 'success', duration: 2000, offset: 80 })
      visible.value = false
    } else {
      ElMessage({ message: res.msg || '重置失败', type: 'error', duration: 2000, offset: 80 })
    }
  } catch {
    ElMessage({ message: '重置失败，请稍后重试', type: 'error', duration: 2000, offset: 80 })
  } finally {
    submitting.value = false
  }
}

function onClosed () {
  formRef.value?.resetFields()
  countdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}
</script>

<style scoped>
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  background: var(--accent-gradient) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600 !important;
  white-space: nowrap;
}

.send-btn:disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
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

.submit-btn {
  flex: 1;
}
</style>
