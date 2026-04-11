<template>
  <div class="dialog-trigger">
    <el-button class="logout-btn" circle size="small" @click="buttonClicked" title="退出登录">退出</el-button>
    <el-dialog v-model="dialogVisible" width="300px" :append-to-body="true" top="30vh">
      <div class="logout-content">
        <i class="el-icon-warning-outline warning-icon"></i>
        <h3>确定要退出吗？</h3>
        <p>您将需要重新登录才能继续学习。</p>
      </div>
      <template #footer>
        <div class="dialog-footer center-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">我再想想</el-button>
          <el-button type="danger" @click="log_out" class="confirm-btn">退出登录</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/user'

const emit = defineEmits(['logout_success'])

const dialogVisible = ref(false)
const userStore = useUserStore()
const { isLogin } = storeToRefs(userStore)

function buttonClicked () {
  if (!isLogin.value) {
    ElMessage.info('您尚未登录')
    return
  }
  dialogVisible.value = true
}

function log_out () {
  emit('logout_success')
  dialogVisible.value = false
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
