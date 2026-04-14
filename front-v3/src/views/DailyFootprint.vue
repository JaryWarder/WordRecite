<template>
  <div class="page" v-loading.fullscreen.lock="loading">
    <el-card class="card">
      <template #header>
        <div class="header">
          <div>
            <div class="title">每日足迹</div>
            <div class="sub">查看某一天学过/复习过的具体单词清单</div>
          </div>
        </div>
      </template>

      <div class="toolbar-row">
        <div class="picker-container">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            :clearable="false"
            size="default"
            class="custom-date-picker"
            @change="fetchDaily"
          />
        </div>
        
        <div class="side-actions">
          <el-tag type="success" effect="plain" class="action-item stat-tag">
            {{ items.length }} 词
          </el-tag>
          <el-button 
            type="primary" 
            plain 
            class="action-item refresh-btn"
            :loading="loading" 
            @click="fetchDaily"
          >
            刷新
          </el-button>
        </div>
      </div>

      <el-empty v-if="!loading && items.length === 0" description="这一天没有学习记录" />

      <div v-else class="list">
        <el-card v-for="it in parsedItems" :key="`${it.origin}-${it.wordId}`" class="item" shadow="never">
          <div class="item-head">
            <div class="word">{{ it.word }}</div>
            <div class="tags">
              <el-tag size="small" class="origin-tag">{{ it.origin }}</el-tag>
              <el-tag v-if="it.status === 'yes'" size="small" effect="plain" type="success">
                <i class="el-icon-check"></i> 已掌握
              </el-tag>
              <el-tag v-else-if="it.status === 'no'" size="small" effect="plain" type="danger">
                <i class="el-icon-close"></i> 需加强
              </el-tag>
            </div>
          </div>

          <div class="def">
            <div v-if="it.blocks.length === 0" class="def-empty">-</div>
            <div v-else class="def-body">
              <div v-for="(block, idx) in it.blocks" :key="idx" class="def-block">
                <div v-if="block.tag" class="def-tag">{{ block.tag }}</div>
                <div class="def-lines">
                  <div
                    v-for="(line, li) in block.lines"
                    :key="li"
                    class="def-line"
                    :class="{ example: line.kind === 'example' }"
                  >
                    {{ line.text }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDailyList } from '../api/progress'
import { useUserStore } from '../stores/user'
import { formatDefinition } from '../utils/definition'

const userStore = useUserStore()
const loading = ref(false)
const selectedDate = ref(formatDate(new Date()))
const items = ref([])

const parsedItems = computed(() => {
  return items.value.map((it) => ({
    ...it,
    blocks: formatDefinition(it?.poses || '')
  }))
})

function formatDate (d) {
  const dt = d instanceof Date ? d : new Date()
  const yyyy = dt.getFullYear()
  const mm = String(dt.getMonth() + 1).padStart(2, '0')
  const dd = String(dt.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

async function fetchDaily () {
  userStore.restore()
  if (!userStore.isLogin) return
  if (!selectedDate.value) return
  loading.value = true
  try {
    const resp = await getDailyList(selectedDate.value)
    if (resp.code !== 200) throw new Error(resp.msg || '获取每日足迹失败')
    items.value = Array.isArray(resp.data) ? resp.data : []
  } catch (e) {
    ElMessage({ message: e?.message || '获取每日足迹失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void fetchDaily()
})
</script>

<style scoped>
.page { padding: 20px; }
.card { border-radius: 24px; background: var(--glass-bg) !important; border: 1px solid var(--glass-border) !important; }
.header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 22px; font-weight: 800; color: var(--text-main); }
.sub { margin-top: 4px; color: var(--text-muted); font-size: 13px; }

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 900px;
  margin: 0 auto 30px;
  padding: 0 10px;
}

.picker-container { flex-shrink: 0; }
.side-actions { display: flex; align-items: center; gap: 12px; }

.action-item {
  height: 34px !important;
  line-height: 32px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px !important;
  font-weight: 600;
  border-radius: 8px !important;
}

.stat-tag {
  font-size: 13px;
  background: rgba(16, 185, 129, 0.1) !important;
  border: 1px solid rgba(16, 185, 129, 0.3) !important;
}

.origin-tag {
  background: rgba(56, 189, 248, 0.15) !important;
  border: 1px solid rgba(56, 189, 248, 0.4) !important;
  color: var(--accent-color) !important;
  font-weight: 700;
  padding: 0 10px;
}

.list { display: flex; flex-direction: column; gap: 16px; }
.item { border-radius: 18px; background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.06); transition: all 0.3s ease; }
.item:hover { background: rgba(255, 255, 255, 0.05); border-color: rgba(56, 189, 248, 0.2); }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }

.word {
  font-size: 28px;
  font-weight: 900;
  color: var(--accent-color);
  letter-spacing: 0.5px;
}

.tags { display: flex; gap: 8px; }
.def-body { display: flex; flex-direction: column; gap: 10px; }

/* --- 修复点：词性和释义紧凑排版 --- */
.def-block {
  display: grid;
  grid-template-columns: auto 1fr; /* 宽度随内容自适应，不再固定 */
  gap: 12px; /* 词性与中文之间的间距 */
}

.def-tag {
  font-size: 14px;
  font-weight: 800;
  color: var(--accent-color);
  opacity: 0.8;
  min-width: 32px; /* 保证基本的对齐视觉感 */
}

.def-line {
  font-size: 17px;
  color: var(--text-main);
  line-height: 1.6;
}

.def-line.example {
  font-size: 14px;
  color: var(--text-muted);
  font-style: italic;
}
</style>