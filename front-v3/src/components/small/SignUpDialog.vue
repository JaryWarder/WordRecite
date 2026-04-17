<template>
  <div class="dialog-trigger">
    <el-button type="primary" size="small" @click="dialogFormVisible = true">注册账号</el-button>
    <el-dialog v-model="dialogFormVisible" title="创建新账户" width="460px" :append-to-body="true">
      <div class="dialog-body">
        <el-form ref="signupFormRef" :model="form" status-icon :rules="signupRules" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="设置用户名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电话号码" prop="phone">
                <el-input v-model="form.phone" placeholder="输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="form.email" placeholder="example@mail.com" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="6位以上" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="reenter">
                <el-input v-model="form.reenter" type="password" placeholder="重复密码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="当前学历">
            <el-select v-model="form.education" placeholder="请选择" style="width: 100%;">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submit" class="submit-btn">立即注册</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitSignup } from '../../api/auth'

const dialogFormVisible = ref(false)
const signupFormRef = ref()

const options = [
  { value: 'primary', label: '小学' },
  { value: 'junior', label: '初中' },
  { value: 'senior', label: '高中' },
  { value: 'undergraduate', label: '本科' },
  { value: 'graduate', label: '本科以上' }
]

const form = reactive({
  email: '',
  password: '',
  reenter: '',
  username: '',
  phone: '',
  education: ''
})

function emailCheck (rule, value, callback) {
  const emailRE = /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/
  if (emailRE.test(value)) return callback()
  return callback(new Error('邮箱格式错误'))
}

function passwordCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请输入密码'))
  const pwdRE = /^(\w){6,20}$/
  if (pwdRE.test(value)) return callback()
  return callback(new Error('密码格式错误：6-20位，可以包含数字、字母、下划线'))
}

function reenterCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请再次输入密码'))
  if (value !== form.password) return callback(new Error('两次输入密码不一致'))
  return callback()
}

function usernameCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请输入用户名'))
  const usernameRE = /^[a-zA-Z]{1}([a-zA-Z0-9]|[_]){4,19}$/
  if (usernameRE.test(value)) return callback()
  return callback(new Error('用户名格式错误：5-20个以字母开头，可包含数字和下划线的字符'))
}

function phoneCheck (rule, value, callback) {
  if (value === '') return callback(new Error('请输入手机号码'))
  const phoneRE = /^1[3-9]\d{9}$/
  if (phoneRE.test(value)) return callback()
  return callback(new Error('手机号码格式错误：请输入1开头的11位有效手机号'))
}

const signupRules = {
  email: [{ validator: emailCheck, trigger: 'blur' }],
  password: [{ validator: passwordCheck, trigger: 'blur' }],
  reenter: [{ validator: reenterCheck, trigger: 'blur' }],
  username: [{ validator: usernameCheck, trigger: 'blur' }],
  phone: [{ validator: phoneCheck, trigger: 'blur' }]
}

async function submit () {
  const valid = await signupFormRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) {
    ElMessage({ message: '输入有误', type: 'error', duration: 2000, offset: 80 })
    return
  }

  try {
    const result = await submitSignup(form)
    if (result.code === 200) {
      ElMessage({ message: '注册成功', type: 'success', duration: 1500, offset: 80 })
      dialogFormVisible.value = false
      return
    }
    if (result.msg === 'user_existed') {
      ElMessage({ message: '用户名已经存在', type: 'warning', duration: 2000, offset: 80 })
      return
    }
    if (result.msg === 'email_existed') {
      ElMessage({ message: '邮箱已被注册', type: 'warning', duration: 2000, offset: 80 })
      return
    }
    ElMessage({ message: '注册失败', type: 'error', duration: 2000, offset: 80 })
  } catch (e) {
    ElMessage({ message: '注册失败', type: 'error', duration: 2000, offset: 80 })
  }
}
</script>

<style scoped>
.dialog-trigger {
  display: inline-block;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.submit-btn {
  min-width: 100px;
}

.cancel-btn {
  background: transparent !important;
  border: 1px solid var(--glass-border) !important;
  color: var(--text-muted) !important;
}
</style>
