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

      <div class="picker">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          :clearable="false"
          size="large"
          @change="fetchDaily"
        />
      </div>

      <div class="meta">
        <el-tag type="info" effect="plain">{{ selectedDate }}</el-tag>
        <el-tag type="success" effect="plain">{{ items.length }} 词</el-tag>
        <el-button plain type="primary" size="small" :loading="loading" @click="fetchDaily">刷新</el-button>
      </div>

      <el-empty v-if="!loading && items.length === 0" description="这一天没有学习记录" />

      <div v-else class="list">
        <el-card v-for="it in parsedItems" :key="`${it.origin}-${it.wordId}`" class="item" shadow="never">
          <div class="item-head">
            <div class="word">{{ it.word }}</div>
            <div class="tags">
              <el-tag size="small" effect="plain" type="info">{{ it.origin }}</el-tag>
              <el-tag v-if="it.status === 'yes'" size="small" effect="plain" type="success">
                <i class="el-icon-check"></i>
                已掌握
              </el-tag>
              <el-tag v-else-if="it.status === 'no'" size="small" effect="plain" type="danger">
                <i class="el-icon-close"></i>
                需加强
              </el-tag>
              <el-tag v-else size="small" effect="plain" type="info">{{ it.status || '-' }}</el-tag>
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
  return items.value.map((it) => {
    return {
      ...it,
      blocks: formatDefinition(it?.poses || '')
    }
  })
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
    if (resp.code !== 200) {
      throw new Error(resp.msg || '获取每日足迹失败')
    }
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
.page {
  padding: 20px;
}

.card {
  border-radius: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
}

.sub {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.picker {
  display: flex;
  justify-content: center;
  margin: 6px 0 14px;
}

.meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.word {
  font-size: 26px;
  font-weight: 900;
  letter-spacing: 0.2px;
  color: var(--text-main);
}

.tags {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.def {
  padding-top: 6px;
}

.def-empty {
  color: var(--text-muted);
}

.def-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.def-block {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: start;
}

.def-tag {
  font-size: 14px;
  font-weight: 800;
  color: var(--accent-color);
}

.def-lines {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.def-line {
  font-size: 16px;
  color: var(--text-main);
  word-break: break-word;
  line-height: 1.5;
}

.def-line.example {
  font-size: 14px;
  color: var(--text-muted);
}
</style>

