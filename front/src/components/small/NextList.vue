<template>
  <div style="cursor: default">
    <el-button type="success" @click="dialogVisible = true" icon="el-icon-finished" style="width: 100%; border-radius: 12px;">
      完成本组，提交进度
    </el-button>
    <el-dialog title="确认提交" :visible.sync="dialogVisible" width="360px" :append-to-body="true" top="30vh">
      <div class="confirm-content">
        <i class="el-icon-success success-icon"></i>
        <p>恭喜完成本组学习！提交后将记录进度并开始下一组。</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="small">稍等</el-button>
        <el-button type="primary" @click="nextClicked" size="small">确认提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 逻辑保持不变
export default {
  name: 'NextList',
  data () {
    return { dialogVisible: false }
  },
  methods: {
    nextClicked: function () {
      this.dialogVisible = false;
      this.$emit('nextList');
    }
  }
}
</script>

<style scoped>
.confirm-content {
  text-align: center;
  padding: 10px 0;
}
.success-icon {
  font-size: 48px;
  color: var(--success-color);
  margin-bottom: 15px;
}
.confirm-content p {
  color: var(--text-main);
  line-height: 1.5;
}
</style>
