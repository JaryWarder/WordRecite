<template>
  <div style="cursor: default; display: inline-block;">
    <el-button type="danger" plain icon="el-icon-delete" @click="dialogVisible = true">
      永久注销账号
    </el-button>

    <el-dialog :visible.sync="dialogVisible" width="360px" :append-to-body="true" top="30vh">
      <div class="delete-content">
        <div class="icon-box">
          <i class="el-icon-delete-solid"></i>
        </div>
        <h3>危险操作</h3>
        <p>此操作将永久删除您的所有学习记录和个人信息，且<b>无法恢复</b>。</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="danger" @click="deleteUser" size="medium">确认注销</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 逻辑完全不变
import { getCookie } from '../../util.js'
import $ from 'jquery'

export default {
  name: 'DeleteDialog',
  data () {
    return {
      dialogVisible: false
    }
  },
  methods: {
    deleteUser () {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/delete_user/' + getCookie('username'),
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {},
        success: (result) => {
          if (result.info === 'success') {
            this.$message.success('注销成功')
            this.$emit('delete_success')
          } else {
            this.$message.error('注销失败')
          }
        },
        error: () => {
          this.$message.error('注销失败，请检查网络')
        }
      })
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
.delete-content {
  text-align: center;
}
.icon-box {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(244, 63, 94, 0.1);
  color: var(--danger-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin: 0 auto 20px;
}
.delete-content h3 {
  color: var(--text-main);
  margin-bottom: 10px;
}
.delete-content p {
  color: var(--text-muted);
  line-height: 1.5;
}
</style>
