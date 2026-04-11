<template>
  <div class="alert-wrapper">
    <el-alert
      title="未选择书本"
      type="warning"
      show-icon
      :closable="false"
      class="custom-alert"
    >
      <template #default>
        <div class="alert-content">
          <span>请先选择要学习的书本后再继续。</span>
          <el-button type="primary" size="small" @click="goToWordBooks">去选择</el-button>
        </div>
      </template>
    </el-alert>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goToWordBooks () {
  router.push('/wordBooks')
}
</script>

<style scoped>
.alert-wrapper {
  margin: 20px 0;
}

.alert-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.custom-alert {
  border-radius: 12px;
  background: rgba(245, 158, 11, 0.15) !important;
  border: 1px solid rgba(245, 158, 11, 0.3) !important;
}

:deep(.el-alert__title) {
  font-size: 15px;
  color: var(--warning-color);
}
</style>
