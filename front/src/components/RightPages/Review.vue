<template>
  <div class="review-wrapper">
    <el-card class="review-card">
      <div slot="header" class="card-header">
        <h2><i class="el-icon-date"></i> 今日复习</h2>
        <span class="sub-text">温故而知新</span>
      </div>
      <el-tabs type="card" class="transparent-tabs">
        <el-tab-pane label="列表模式">
          <div v-if="tableData.length===0" class="empty-state">
            <div class="empty-icon"><i class="el-icon-coffee"></i></div>
            <h3>今天没有复习任务</h3>
            <p>享受你的闲暇时光，或者去背点新单词吧！</p>
          </div>
          <el-table
            v-else
            :data="tableData"
            style="width: 100%;"
            :header-cell-style="{background:'rgba(255,255,255,0.05)', color:'#94a3b8', borderBottom:'1px solid rgba(255,255,255,0.1)'}"
            :cell-style="{background:'transparent', color:'#f8fafc', borderBottom:'1px solid rgba(255,255,255,0.05)'}"
            class="glass-table">
            <el-table-column prop="word" label="单词" width="180">
              <template slot-scope="scope">
                <span style="font-weight: bold; color: var(--accent-color);">{{ scope.row.word }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="poses" label="释义"></el-table-column>
            <el-table-column prop="status" label="状态" width="120" align="center">
              <template slot-scope="scope">
                <span class="status-tag" :class="scope.row.status">
                  {{ scope.row.status === 'yes' ? '已掌握' : '模糊' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="卡片模式 (开发中)" disabled>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
// 逻辑保持不变
import $ from 'jquery'
import { getCookie } from '../../util.js'
export default {
  name: 'Review',
  data () {
    return {
      tableData: []
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/get_daily/' + getCookie('username'),
      crossDomain: true,
      xhrFields: {
        withCredentials: true
      },
      dataType: 'json',
      data: {},
      success: (result) => {
        if (result === null) {
          this.$message.error('获取复习单词失败');
        } else {
          result.forEach(item => {
            let p = item.poses.replace(/\+\+/g, '').replace(/\*\*/g, '');
            this.tableData.push({ word: item.word, poses: p, status: item.status });
          });
        }
      },
      error: () => { this.$message.error('获取复习单词失败，请检查网络'); }
    })
  }
}
</script>

<style scoped>
.review-wrapper {
  padding: 20px;
}

.review-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h2 { margin: 0; font-size: 20px; color: var(--text-main); }
.sub-text { color: var(--text-muted); font-size: 14px; }

/* 覆盖 Element UI 表格背景 - 关键部分 */
::v-deep .el-table {
  background-color: transparent !important;
}
::v-deep .el-table tr {
  background-color: transparent !important;
}
::v-deep .el-table--enable-row-hover .el-table__body tr:hover > td {
  background-color: rgba(255, 255, 255, 0.05) !important;
}
::v-deep .el-table td, ::v-deep .el-table th.is-leaf {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}
::v-deep .el-table::before {
  display: none;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}
.status-tag.yes {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
}
.status-tag.no {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}
.empty-icon {
  font-size: 60px;
  color: var(--text-muted);
  margin-bottom: 20px;
  opacity: 0.5;
}
</style>
