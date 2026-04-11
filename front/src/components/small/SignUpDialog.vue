<template>
  <div class="dialog-trigger">
    <el-button type="primary" size="small" @click="dialogFormVisible = true">注册账号</el-button>
    <el-dialog title="创建新账户" :visible.sync="dialogFormVisible" width="460px" :append-to-body="true">
      <div class="dialog-body">
        <el-form :model="form" ref="signup_form" status-icon :rules="signup_rules" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
               <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="设置用户名"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电话号码" prop="phone">
                <el-input v-model="form.phone" placeholder="输入手机号"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="form.email" placeholder="example@mail.com"></el-input>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="6位以上"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="reenter">
                <el-input v-model="form.reenter" type="password" placeholder="重复密码"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="当前学历">
            <el-select v-model="form.education" placeholder="请选择" style="width: 100%;">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submit('signup_form')" class="submit-btn">立即注册</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import $ from 'jquery'
export default {
  name: 'SignUpDialog',
  data () {
    let emailCheck = (rule, value, callback) => {
      let emailRE = /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/;
      if (emailRE.test(value)) {
        callback();
      } else {
        callback(new Error('邮箱格式错误'));
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
    let reenterCheck = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };
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
    let phoneCheck = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入手机号码'));
      } else {
        let phoneRE = /^[1][34578][0-9]{9}$/;
        if (phoneRE.test(value)) {
          callback();
        } else {
          callback(new Error('手机号码格式错误'));
        }
      }
    };
    return {
      dialogFormVisible: false,
      options: [
        { value: 'primary', label: '小学' },
        { value: 'junior', label: '初中' },
        { value: 'senior', label: '高中' },
        { value: 'undergraduate', label: '本科' },
        { value: 'graduate', label: '本科以上' }
      ],
      form: {
        email: '',
        password: '',
        reenter: '',
        username: '',
        phone: '',
        education: ''
      },
      signup_rules: {
        email: [{ validator: emailCheck, trigger: 'blur' }],
        password: [{ validator: passwordCheck, trigger: 'blur' }],
        reenter: [{ validator: reenterCheck, trigger: 'blur' }],
        username: [{ validator: usernameCheck, trigger: 'blur' }],
        phone: [{ validator: phoneCheck, trigger: 'blur' }]
      }
    }
  },
  methods: {
    submit: function (formName) {
      let canSubmit = false
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          this.$message({
            message: '输入有误',
            type: 'error',
            duration: 2000,
            offset: 80
          });
          canSubmit = false
        } else {
          canSubmit = true
        }
      });
      if (canSubmit) {
        $.ajax({
          type: 'GET',
          url: 'http://localhost:8080/Hello/submit_signup',
          crossDomain: true,
          xhrFields: {
            withCredentials: true
          },
          dataType: 'json',
          data: {obj: JSON.stringify(this.form)},
          success: (result) => {
            if (result.info === 'success') {
              this.$message({
                message: '注册成功',
                type: 'success',
                duration: 1500,
                offset: 80
              });
              this.dialogFormVisible = false
            } else if (result.info === 'user_existed') {
              this.$message({
                message: '用户名已经存在',
                type: 'warning',
                duration: 2000,
                offset: 80
              });
            } else if (result.info === 'email_existed') {
              this.$message({
                message: '邮箱已被注册',
                type: 'warning',
                duration: 2000,
                offset: 80
              });
            } else {
              this.$message({
                message: '注册失败',
                type: 'error',
                duration: 2000,
                offset: 80
              });
            }
          },
          error: () => {
            this.$message({
              message: '注册失败',
              type: 'error',
              duration: 2000,
              offset: 80
            });
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
