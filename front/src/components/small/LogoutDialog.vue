<template>
  <div class="dialog-trigger">
    <el-button class="logout-btn" icon="el-icon-switch-button" circle size="small" @click="buttonClicked" title="退出登录"></el-button>
    <el-dialog :visible.sync="dialogVisible" width="300px" :append-to-body="true" top="30vh">
      <div class="logout-content">
        <i class="el-icon-warning-outline warning-icon"></i>
        <h3>确定要退出吗？</h3>
        <p>您将需要重新登录才能继续学习。</p>
      </div>
      <div slot="footer" class="dialog-footer center-footer">
        <el-button @click="dialogVisible = false" class="cancel-btn">我再想想</el-button>
        <el-button type="danger" @click="log_out" class="confirm-btn">退出登录</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 逻辑完全不变
import { getCookie } from '../../util.js'
export default {
  name: 'LogoutDialog',
  data () {
    return {
      dialogVisible: false
    }
  },
  methods: {
    buttonClicked () {
      let status = getCookie('isLogin');
      if (status === 'false' || status === null || status === 'null') {
        this.$message.info('您尚未登录');
      } else {
        this.dialogVisible = true;
      }
    },
    log_out () {
      this.$emit('logout_success');
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>
.dialog-trigger {
  display: inline-block;
}
.logout-btn {
  background: transparent !important;
  border: 1px solid var(--danger-color) !important;
  color: var(--danger-color) !important;
}
.logout-btn:hover {
  background: var(--danger-color) !important;
  color: white !important;
}
.logout-content {
  text-align: center;
  padding: 10px 0;
}
.warning-icon {
  font-size: 48px;
  color: var(--warning-color);
  margin-bottom: 10px;
}
.logout-content h3 {
  margin: 10px 0 5px;
  color: var(--text-main);
}
.logout-content p {
  color: var(--text-muted);
  font-size: 14px;
}
.center-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}
.cancel-btn {
  background: transparent !important;
  border: 1px solid var(--glass-border) !important;
}
</style>
