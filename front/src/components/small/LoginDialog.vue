<template>
  <div class="dialog-trigger">
    <el-button class="glass-btn" size="small" @click="buttonClicked">登录</el-button>
    <el-dialog title="欢迎回来" :visible.sync="dialogFormVisible" width="420px" :append-to-body="true" custom-class="glass-dialog">
      <div class="dialog-body">
        <el-form :model="form" ref="login_form" status-icon :rules="login_rules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入您的用户名" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" placeholder="请输入密码" type="password" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <div class="forgot-password">
            <el-button type="text" @click="sendEmail">忘记密码?</el-button>
          </div>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submit('login_form')" :loading="loginLoading" class="submit-btn">立即登录</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import $ from 'jquery'
import { getCookie } from '../../util.js'
import '../../security.js'
export default {
  name: 'LoginDialog',
  data () {
    let usernameCheck = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入用户名'));
      } else {
        let usernameRE = /^[a-zA-Z]{1}([a-zA-Z0-9]|[_]){4,19}$/;
        if (usernameRE.test(value)) {
          callback();
        } else {
          callback(new Error('用户名格式错误：5-20个以字母开头，可包含数字和下划线的字符'));
        }
      }
    };
    let passwordCheck = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      }
      let pwdRE = /^(\w){6,20}$/;
      if (pwdRE.test(value)) {
        callback();
      } else {
        callback(new Error('密码格式错误：6-20位，可以包含数字、字母、下划线'));
      }
    };
    return {
      dialogFormVisible: false,
      loginLoading: false,
      form: {
        username: '',
        password: ''
      },
      login_rules: {
        username: [{ validator: usernameCheck, trigger: 'blur' }],
        password: [{ validator: passwordCheck, trigger: 'blur' }]
      }
    }
  },
  methods: {
    sendEmail: function () {
      if (this.form.username === '') {
        this.$message({
          message: '请先输入用户名',
          type: 'warning',
          duration: 2000,
          offset: 80
        });
      } else {
        $.ajax({
          type: 'GET',
          url: 'http://localhost:8080/Hello/send_email/' + this.form.username,
          crossDomain: true,
          xhrFields: {
            withCredentials: true
          },
          dataType: 'json',
          data: {},
          success: (result) => {
            if (result.info === 'success') {
              this.$message({
                message: '邮件已发送 密码重置为123456 请重新登录',
                type: 'success',
                duration: 3000, // 提示语较长，保留3秒
                offset: 80
              });
              this.dialogFormVisible = false;
            } else {
              this.$message({
                message: '发送失败',
                type: 'error',
                duration: 2000,
                offset: 80
              });
            }
          },
          error: () => {
            this.$message({
              message: '邮件发送失败',
              type: 'error',
              duration: 2000,
              offset: 80
            });
          }
        })
      }
    },
    buttonClicked: function () {
      let status = getCookie('isLogin');
      if (status === 'true') {
        this.$message({
          message: '您已登录',
          type: 'info',
          duration: 1500,
          offset: 80
        });
      } else {
        this.dialogFormVisible = true;
      }
    },
    submit_again (encrypted) {
      let toSubmit = {
        username: this.form.username,
        encrypted: encrypted
      };
      $.ajax({
        type: 'POST',
        url: 'http://localhost:8080/Hello/login_check',
        crossDomain: true,
        xhrFields: {
          withCredentials: true
        },
        dataType: 'json',
        data: { obj: JSON.stringify(toSubmit) },
        success: (result) => {
          if (result.info === 'success') {
            this.$message({
              message: '登录成功',
              type: 'success',
              duration: 1000, // 1秒极速反馈
              offset: 80 // 避开导航栏
            });
            this.dialogFormVisible = false;
            this.$emit('login_success', this.form.username);
          } else if (result.info === 'wrong_pwd') {
            this.$message({
              message: '密码错误',
              type: 'error',
              duration: 1000,
              offset: 80
            });
          } else if (result.info === 'not_found') {
            this.$message({
              message: '找不到用户',
              type: 'error',
              duration: 1000,
              offset: 80
            });
          } else {
            this.$message({
              message: '登录失败',
              type: 'error',
              duration: 1000,
              offset: 80
            });
          }
        },
        error: () => {
          this.$message({
            message: '登录失败',
            type: 'error',
            duration: 1000,
            offset: 80
          });
        }
      }).always(() => { this.loginLoading = false; });
    },
    submit: function (formName) {
      let canSubmit = false;
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          this.$message({
            message: '输入有误',
            type: 'error',
            duration: 1000,
            offset: 80
          });
          canSubmit = false;
        } else {
          canSubmit = true;
        }
      });
      if (canSubmit) {
        this.loginLoading = true;
        $.ajax({
          type: 'GET',
          url: 'http://localhost:8080/Hello/login_request',
          crossDomain: true,
          xhrFields: {
            withCredentials: true
          },
          dataType: 'json',
          data: {},
          success: (result) => {
            if (result.info === 'error') {
              this.$message({
                message: '登录失败',
                type: 'error',
                duration: 1000,
                offset: 80
              });
              this.loginLoading = false;
            } else {
              window.RSAUtils.setMaxDigits(200);
              let key = window.RSAUtils.getKeyPair(result.pub_exp, '', result.pub_mod);
              let encrypted = window.RSAUtils.encryptedString(key, this.form.password);
              this.submit_again(encrypted);
            }
          },
          error: () => {
            this.$message({
              message: '登录失败',
              type: 'error',
              duration: 1000,
              offset: 80
            });
            this.loginLoading = false;
          }
        })
      }
    }
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
