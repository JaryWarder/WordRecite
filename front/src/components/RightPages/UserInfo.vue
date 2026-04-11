<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <div slot="header" class="card-header">
        <h2>个人资料</h2>
        <div class="header-actions">
           <DeleteDialog @delete_success="on_delete"></DeleteDialog>
        </div>
      </div>

      <div class="avatar-area">
        <div class="avatar-circle">
          <i class="el-icon-user-solid"></i>
        </div>
        <h3>{{ form.username }}</h3>
      </div>

      <el-form ref="reset_form" :model="form" status-icon :rules="reset_rules" label-position="top" class="profile-form">
        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled class="readonly-input"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" :disabled="!isModifyClicked" :class="{'readonly-input': !isModifyClicked}"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" :disabled="!isModifyClicked" :class="{'readonly-input': !isModifyClicked}"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历" prop="education">
              <el-select v-model="form.education" :disabled="!isModifyClicked" style="width: 100%;">
                <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <transition name="el-zoom-in-top">
          <div v-if="isModifyClicked" class="password-section">
            <div class="section-title">修改密码 (可选)</div>
            <el-form-item label="旧密码" prop="old_pwd">
              <el-input v-model="form.old_pwd" placeholder="验证身份需要" type="password"></el-input>
            </el-form-item>
            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="新密码" prop="new_pwd">
                  <el-input v-model="form.new_pwd" type="password"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                 <el-form-item label="确认密码" prop="new_pwd_reenter">
                  <el-input v-model="form.new_pwd_reenter" type="password"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </transition>

        <div class="form-footer">
          <template v-if="!isModifyClicked">
            <el-button type="primary" icon="el-icon-edit" @click="isModifyClicked = true" style="width: 150px;">
              编辑资料
            </el-button>
          </template>
          <template v-else>
            <el-button @click="cancelModify">取消</el-button>
            <el-button type="primary" @click="submitReset('reset_form')">保存更改</el-button>
          </template>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
// 逻辑保持不变
import DeleteDialog from '../small/DeleteDialog'
import $ from 'jquery'
import { getCookie, delCookie } from '../../util.js'

export default {
  name: 'UserInfo',
  components: { DeleteDialog },
  data () {
    let emailCheck = (rule, value, callback) => {
      let emailRE = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/
      if (emailRE.test(value)) {
        callback()
      } else {
        callback(new Error('邮箱格式错误'))
      }
    }
    let passwordCheck = (rule, value, callback) => {
      if (value === '' && this.isModifyClicked) {
        callback(new Error('请输入密码'))
      }
      let pwdRE = /^(\w){6,20}$/
      if (pwdRE.test(value) || value === '') {
        callback()
      } else {
        callback(new Error('密码格式错误：6-20位，可以包含数字、字母、下划线'))
      }
    }
    let reenterCheck = (rule, value, callback) => {
      if (value !== this.form.new_pwd) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    let phoneCheck = (rule, value, callback) => {
      let phoneRE = /^[1][34578][0-9]{9}$/
      if (phoneRE.test(value)) {
        callback()
      } else {
        callback(new Error('手机号码格式错误'))
      }
    }
    return {
      isModifyClicked: false,
      originalForm: {},
      options: [
        { value: 'primary', label: '小学' },
        { value: 'junior', label: '初中' },
        { value: 'senior', label: '高中' },
        { value: 'undergraduate', label: '本科' },
        { value: 'graduate', label: '本科以上' }
      ],
      form: {
        username: '',
        email: '',
        education: '',
        phone: '',
        old_pwd: '',
        new_pwd: '',
        new_pwd_reenter: ''
      },
      reset_rules: {
        email: [{ validator: emailCheck, trigger: 'blur' }],
        new_pwd: [{ validator: passwordCheck, trigger: 'blur' }],
        new_pwd_reenter: [{ validator: reenterCheck, trigger: 'blur' }],
        phone: [{ validator: phoneCheck, trigger: 'blur' }]
      }
    }
  },
  methods: {
    on_delete () {
      delCookie('username', 1)
      delCookie('isLogin', 1)
      this.$router.replace('/')
    },
    cancelModify () {
      this.isModifyClicked = false
      this.form = { ...this.originalForm, old_pwd: '', new_pwd: '', new_pwd_reenter: '' }
      this.$refs['reset_form'].clearValidate()
    },
    submitReset (formName) {
      if (this.isModifyClicked) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            $.ajax({
              type: 'GET',
              url: 'http://localhost:8080/Hello/submit_reset',
              crossDomain: true,
              xhrFields: { withCredentials: true },
              dataType: 'json',
              data: {obj: JSON.stringify(this.form)},
              success: (result) => {
                if (result.info === 'success') {
                  this.$message.success('修改成功')
                  this.isModifyClicked = false
                  this.fetchUserInfo()
                } else if (result.info === 'email_existed') {
                  this.$message.warning('邮箱已被注册')
                } else if (result.info === 'wrong_pwd') {
                  this.$message.error('旧密码错误')
                } else {
                  this.$message.error('更改失败')
                }
              },
              error: () => {
                this.$message.error('提交失败，请检查网络')
              }
            })
          } else {
            this.$message.error('输入信息有误')
            return false
          }
        })
      } else {
        this.isModifyClicked = true
      }
    },
    fetchUserInfo () {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/get_userInfo/' + getCookie('username'),
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {},
        success: (result) => {
          this.form.username = result.username
          this.form.email = result.email
          this.form.education = result.education
          this.form.phone = result.phone
          this.originalForm = { ...this.form }
        },
        error: () => {
          this.$message.error('获取用户信息失败')
        }
      })
    }
  },
  mounted () {
    this.fetchUserInfo()
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.profile-card {
  width: 100%;
  max-width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h2 { margin: 0; color: var(--text-main); font-size: 20px; }

.avatar-area {
  text-align: center;
  margin: 20px 0 40px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  background: var(--glass-border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 40px;
  color: var(--text-muted);
}

.avatar-area h3 {
  margin: 0;
  color: var(--text-main);
}

.profile-form {
  padding: 0 20px;
}

/* 只读状态下的输入框样式优化 */
.readonly-input >>> .el-input__inner {
  border-color: transparent !important;
  background: transparent !important;
  padding-left: 0;
  color: var(--text-muted) !important;
  cursor: default;
}

.password-section {
  background: rgba(0, 0, 0, 0.2);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  color: var(--accent-color);
  margin-bottom: 15px;
}

.form-footer {
  text-align: center;
  margin-top: 30px;
}
</style>
