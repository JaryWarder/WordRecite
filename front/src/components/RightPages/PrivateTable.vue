<template>
  <div class="private-wrapper">
    <el-card class="private-card">
      <div slot="header" class="card-header">
        <h2 style="color: #303133"><i class="el-icon-notebook-1"></i> 我的生词本</h2>
        <span class="sub-text">积累是成功的基石</span>
      </div>

      <div v-if="tableData.length===0" class="empty-state">
        <div class="empty-icon"><i class="el-icon-folder-opened"></i></div>
        <h3>生词本还是空的</h3>
        <p>在背单词时点击“收藏”按钮，这里就会出现内容。</p>
      </div>

      <el-table
        v-else
        :data="tableData"
        style="width: 100%;"
        :header-cell-style="{background:'transparent', color:'#909399', borderBottom:'1px solid #ebeef5'}"
        :cell-style="{background:'transparent', color:'#606266', borderBottom:'1px solid #ebeef5'}"
        class="glass-table">
        <el-table-column prop="word" label="单词" width="200">
          <template slot-scope="scope">
            <span style="font-weight: bold; font-size: 16px; color: #409EFF;">{{ scope.row.word }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="poses" label="释义"></el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button
              type="danger"
              icon="el-icon-delete"
              circle
              size="mini"
              plain
              @click="deletePrivate(scope.row.word, scope.$index)"
              title="移出生词本">
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import $ from 'jquery'
import { getCookie } from '../../util.js'

export default {
  name: 'PrivateTable',
  data () {
    return {
      tableData: []
    }
  },
  methods: {
    deletePrivate: function (theWord, index) {
      this.$confirm('确定要从生词本中移除这个单词吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 先在前端移除，提升体验
        this.tableData.splice(index, 1);

        let submit = {
          word: theWord,
          username: getCookie('username')
        };
        $.ajax({
          type: 'GET',
          url: 'http://localhost:8080/Hello/delete_private/',
          crossDomain: true,
          xhrFields: { withCredentials: true },
          dataType: 'json',
          data: {obj: JSON.stringify(submit)},
          success: (result) => {
            if (result.info === 'success') {
              this.$message.success('删除成功');
            } else {
              this.$message.error('删除失败，请刷新重试');
            }
          },
          error: () => { this.$message.error('删除失败，请检查网络连接'); }
        })
      }).catch(() => {});
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/get_private/' + getCookie('username'),
      crossDomain: true,
      xhrFields: { withCredentials: true },
      dataType: 'json',
      data: {},
      success: (result) => {
        if (result) {
          result.forEach(item => {
            // 处理后端传来的格式
            let p = item.poses.replace(/\+\+/g, '').replace(/\*\*/g, '');
            this.tableData.push({ word: item.word, poses: p });
          });
        }
      },
      error: () => { this.$message.error('获取生词本失败'); }
    })
  }
}
</script>

<style scoped>
.private-wrapper {
  padding: 20px;
}
.private-card {
  min-height: 600px;
  /* 确保卡片有白色背景 (如果父组件背景是深色) */
  background-color: #fff;
  border: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 标题颜色在行内样式中定义为 #303133 */
.sub-text { color: #909399; font-size: 14px; }

/* 强制覆盖表格背景 */
::v-deep .el-table { background-color: transparent !important; }
::v-deep .el-table tr { background-color: transparent !important; }

/* 鼠标悬停时背景色：改为浅灰色（适应白底） */
::v-deep .el-table--enable-row-hover .el-table__body tr:hover > td {
  background-color: #f5f7fa !important;
}

/* 单元格分割线颜色：改为浅灰色 */
::v-deep .el-table td, ::v-deep .el-table th.is-leaf {
  border-bottom: 1px solid #ebeef5 !important;
}
::v-deep .el-table::before { display: none; }

.empty-state {
  text-align: center;
  padding: 100px 0;
}
.empty-icon {
  font-size: 60px;
  color: #c0c4cc;
  margin-bottom: 20px;
}
</style>
