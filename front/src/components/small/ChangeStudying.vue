<template>
  <div style="display: inline-block; width: 100%;">
    <el-button type="success" plain @click="dialogVisible = true" style="width: 100%;">
      选择此书
    </el-button>
    <el-dialog title="切换学习目标" :visible.sync="dialogVisible" width="360px" :append-to-body="true" top="30vh">
      <div class="change-content">
        <el-alert
          title="注意：切换单词书将重置您当前的每日学习计划。"
          type="warning"
          :closable="false"
          show-icon>
        </el-alert>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" @click="changeClicked" size="small">确认切换</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 逻辑完全不变
export default {
  name: 'ChangeStudying',
  data () {
    return { dialogVisible: false }
  },
  methods: {
    changeClicked: function () {
      this.dialogVisible = false;
      this.$emit('changeStudying');
    }
  }
}
</script>

<style scoped>
.change-content {
  padding: 10px 0;
}
/* 覆盖 el-alert 样式以适应深色主题 */
::v-deep .el-alert--warning {
  background-color: rgba(245, 158, 11, 0.1);
  color: #fbbf24;
}
</style>
