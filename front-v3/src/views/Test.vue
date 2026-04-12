<template>
  <div class="test-wrapper" v-loading.fullscreen.lock="loading">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>单词测试</h2>
            <div class="sub-text">{{ headerSub }}</div>
          </div>
          <div class="header-right">
            <el-tag type="info" effect="plain">用时 {{ formatTime(elapsedSec) }}</el-tag>
            <el-tag type="success" effect="plain">得分 {{ score }} / {{ totalCount }}</el-tag>
          </div>
        </div>
      </template>

      <div v-if="!isFinished" class="quiz">
        <div class="progress">
          <div class="progress-text">当前 {{ currentIndex + 1 }} / 共 {{ totalCount }} 题</div>
          <el-progress :percentage="progressPercentage" :stroke-width="8" :show-text="false" color="#38bdf8" />
        </div>

        <div class="stem">
          <div v-if="definitionBlocks.length === 0" class="def-empty">-</div>
          <div v-else class="def">
            <div v-for="(block, idx) in definitionBlocks" :key="idx" class="def-block">
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

        <div class="options">
          <el-button
            v-for="opt in currentQuestion?.options || []"
            :key="opt"
            class="option-btn"
            :type="getOptionType(opt)"
            plain
            :disabled="locked"
            @click="choose(opt)"
          >
            {{ opt }}
          </el-button>
        </div>
      </div>

      <div v-else class="result">
        <div class="result-hero">
          <div class="accuracy">{{ accuracy }}%</div>
          <div class="result-meta">
            <el-tag type="success" effect="plain">总得分 {{ score }} / {{ totalCount }}</el-tag>
            <el-tag type="info" effect="plain">用时 {{ formatTime(elapsedSec) }}</el-tag>
          </div>
        </div>

        <el-divider content-position="left">错题复盘</el-divider>

        <el-empty v-if="wrongList.length === 0" description="全对了！继续保持" />

        <el-table v-else :data="wrongList" stripe class="wrong-table">
          <el-table-column label="#" width="60">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="poses" label="释义" min-width="240" />
          <el-table-column prop="chosen" label="你的选择" min-width="140">
            <template #default="{ row }">
              <el-tag type="danger" effect="plain">{{ row.chosen }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="answer" label="正确答案" min-width="140">
            <template #default="{ row }">
              <el-tag type="success" effect="plain">{{ row.answer }}</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="result-actions">
          <el-button type="primary" @click="goBack">返回上一页</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { generateTest, submitTest } from '../api/test'
import { formatDefinition } from '../utils/definition'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const score = ref(0)
const locked = ref(false)
const selectedOption = ref('')
const isFinished = ref(false)
const elapsedSec = ref(0)
const history = ref([])
const submitted = ref(false)

let timerId = null

const username = computed(() => String(route.query.username || ''))
const bookTitle = computed(() => String(route.query.bookTitle || ''))
const totalCount = computed(() => questions.value.length || Number(route.query.count || 0) || 0)

const currentQuestion = computed(() => questions.value[currentIndex.value])

const progressPercentage = computed(() => {
  const total = totalCount.value
  if (!total) return 0
  return Math.round(((currentIndex.value + 1) / total) * 100)
})

const accuracy = computed(() => {
  const total = totalCount.value
  if (!total) return 0
  return Math.round((score.value / total) * 100)
})

const wrongList = computed(() => history.value.filter((h) => !h.correct))

const headerSub = computed(() => {
  const ct = totalCount.value ? `${totalCount.value} 题` : ''
  const bt = bookTitle.value ? `词书：${bookTitle.value}` : ''
  return [bt, ct].filter(Boolean).join(' · ') || '请完成本次测试'
})

const definitionBlocks = computed(() => formatDefinition(currentQuestion.value?.poses || ''))

function formatTime (sec) {
  const s = Math.max(0, Number(sec) || 0)
  const mm = String(Math.floor(s / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

function getOptionType (opt) {
  if (!locked.value) return 'primary'
  const answer = currentQuestion.value?.answer
  if (opt === answer) return 'success'
  if (opt === selectedOption.value && selectedOption.value !== answer) return 'danger'
  return 'info'
}

function choose (opt) {
  if (locked.value) return
  locked.value = true
  selectedOption.value = opt

  const q = currentQuestion.value
  const answer = q?.answer
  const isCorrect = Boolean(answer && opt === answer)
  if (isCorrect) score.value += 1

  history.value.push({
    id: q?.id ?? currentIndex.value + 1,
    poses: q?.poses || '',
    chosen: opt,
    answer: answer || '',
    correct: isCorrect
  })

  window.setTimeout(() => {
    if (currentIndex.value + 1 >= questions.value.length) {
      isFinished.value = true
      locked.value = true
      if (timerId) window.clearInterval(timerId)
      void submitServerHistory()
      return
    }
    currentIndex.value += 1
    locked.value = false
    selectedOption.value = ''
  }, 1200)
}

async function submitServerHistory () {
  if (submitted.value) return
  submitted.value = true

  const b = bookTitle.value
  if (!b) return

  const posesMap = new Map()
  for (const h of history.value) {
    const id = Number(h?.id)
    if (!id) continue
    posesMap.set(id, String(h?.poses || ''))
  }

  const payload = {
    bookTitle: b,
    history: history.value.map((h) => ({
      wordId: h?.id,
      chosen: h?.chosen
    }))
  }

  try {
    const result = await submitTest(payload)
    if (result.code !== 200) {
      throw new Error(result.msg || '提交失败')
    }
    const data = result.data || {}
    const serverHistory = Array.isArray(data.history) ? data.history : []
    if (serverHistory.length) {
      history.value = serverHistory.map((it) => {
        const wid = Number(it?.wordId)
        return {
          id: wid,
          poses: posesMap.get(wid) || '',
          chosen: String(it?.chosen || ''),
          answer: String(it?.answer || ''),
          correct: Boolean(it?.correct)
        }
      })
    }
    if (typeof data.correctCount === 'number') {
      score.value = data.correctCount
    }
    ElMessage({ message: '已同步到服务器', type: 'success', duration: 1600, offset: 80 })
  } catch (e) {
    submitted.value = false
    ElMessage({ message: e?.message || '提交失败，未同步到服务器', type: 'warning', duration: 2000, offset: 80 })
  }
}

function goBack () {
  router.back()
}

async function loadPaper () {
  const u = username.value
  const b = bookTitle.value
  const c = Number(route.query.count || 0)

  if (!u || !b || !c) {
    ElMessage({ message: '缺少测试参数，请从测试配置页进入', type: 'warning', duration: 2000, offset: 80 })
    router.back()
    return
  }

  loading.value = true
  try {
    const result = await generateTest(u, b, c)
    if (result.code !== 200) {
      throw new Error(result.msg || '生成试卷失败')
    }
    const list = Array.isArray(result.data) ? result.data : []
    questions.value = list
    currentIndex.value = 0
    score.value = 0
    locked.value = false
    selectedOption.value = ''
    isFinished.value = false
    history.value = []

    elapsedSec.value = 0
    if (timerId) window.clearInterval(timerId)
    timerId = window.setInterval(() => {
      elapsedSec.value += 1
    }, 1000)
  } catch (e) {
    ElMessage({ message: e?.message || '生成试卷失败', type: 'error', duration: 2000, offset: 80 })
    router.back()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadPaper()
})

onBeforeUnmount(() => {
  if (timerId) window.clearInterval(timerId)
})
</script>

<style scoped>
.test-wrapper {
  padding: 20px;
}

.test-card {
  min-height: 560px;
  border-radius: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-main);
}

.sub-text {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.quiz {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.progress {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 13px;
}

.stem {
  padding: 22px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
  text-align: left;
  line-height: 1.4;
  min-height: 120px;
  max-height: 240px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.def-empty {
  text-align: center;
  color: var(--text-muted);
}

.def {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  font-size: 18px;
  color: var(--text-main);
  word-break: break-word;
}

.def-line.example {
  font-size: 15px;
  color: var(--text-muted);
}

.options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.option-btn {
  width: 240px;
  height: 46px;
  border-radius: 12px;
  font-weight: 700;
}

.result {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(56, 189, 248, 0.08);
  border: 1px solid rgba(56, 189, 248, 0.18);
}

.accuracy {
  font-size: 56px;
  font-weight: 900;
  color: var(--accent-color);
  line-height: 1;
}

.result-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.wrong-table {
  width: 100%;
}

.result-actions {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}
</style>
