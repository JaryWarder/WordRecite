<template>
  <div class="dialog-trigger">
    <el-button type="danger" plain size="small" @click="dialogVisible = true">注销账号</el-button>
    <el-dialog v-model="dialogVisible" width="360px" :append-to-body="true" top="25vh">
      <div class="content">
        <h3>确定要注销账号吗？</h3>
        <p>该操作不可恢复（接口待联调）。</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认注销</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['delete_success'])
const dialogVisible = ref(false)

function confirmDelete () {
  ElMessage({ message: '注销接口待联调', type: 'warning', duration: 1500, offset: 80 })
  dialogVisible.value = false
  emit('delete_success')
}
</script>

<style scoped>
.dialog-trigger {
  display: inline-block;
}

.content {
  text-align: center;
}

.content h3 {
  margin: 0 0 8px;
}

.content p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
