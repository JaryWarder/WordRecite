<template>
  <div class="next-wrapper">
    <el-button type="success" class="submit-btn" @click="submitAndNext">
      提交并开始下一组
    </el-button>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const emit = defineEmits(['nextList'])

function submitAndNext () {
  ElMessage({ message: '本组学习完成（接口待联调），即将开始下一组', type: 'success', duration: 1500, offset: 80 })
  emit('nextList')
}
</script>

<style scoped>
.next-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.submit-btn {
  width: 100%;
}
</style>
