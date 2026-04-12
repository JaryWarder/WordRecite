<template>
  <div class="study-wrapper">
    <el-card class="study-card">
      <div class="progress-header">
        <div class="progress-text">进度 {{ completed }} / {{ totalNum }}</div>
        <el-progress :percentage="progressPercentage" :show-text="false" :stroke-width="6" color="#38bdf8" />
      </div>

      <div class="word-display-area">
        <div class="word-row">
          <h1 class="main-word">{{ word }}</h1>
          <div class="favorite-wrap">
            <el-tooltip :content="isFavorited ? '已收藏（点击取消）' : '收藏到生词本'" placement="top">
              <el-button
                class="favorite-btn"
                circle
                :type="isFavorited ? 'warning' : 'default'"
                :plain="!isFavorited"
                :loading="checkingFavorite || togglingFavorite"
                :disabled="favoriteDisabled"
                @click="toggleFavorite"
              >
                <i :class="isFavorited ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              </el-button>
            </el-tooltip>
            <span class="favorite-label" :class="{ on: isFavorited }">{{ isFavorited ? '已收藏' : '收藏' }}</span>
          </div>
        </div>
        <div class="phonetic-badge" v-if="phonetic">{{ phonetic }}</div>
      </div>

      <div class="options-row">
        <el-switch v-model="autoNext" active-text="自动下一词" inactive-text="手动下一词" />
      </div>

      <div class="interaction-area">
        <div class="decision-buttons" v-if="!revealed">
          <el-button class="choice-btn no-btn" :disabled="loadingWord" @click="chooseNo">
            <i class="el-icon-close"></i> 不认识
          </el-button>
          <el-button class="choice-btn yes-btn" :disabled="loadingWord" @click="chooseYes">
            <i class="el-icon-check"></i> 认识
          </el-button>
        </div>

        <div class="result-actions" v-else>
          <el-button type="primary" class="action-btn next-btn" :disabled="loadingWord || isLastWord" @click="goNext">
            下一个 <i class="el-icon-right"></i>
          </el-button>
          <el-button type="success" class="action-btn" :loading="submitting" v-if="isLastWord" @click="submitCurrentList">
            提交本组学习
          </el-button>
        </div>
      </div>

      <transition name="el-zoom-in-top">
        <div class="definition-box" v-show="revealed">
          <div class="def-scroll-area">
            <div v-if="poses.length === 0" class="empty-def">暂无释义</div>
            <div v-else v-for="(item, index) in poses" :key="index" class="pos-row">
              <span class="pos-tag">{{ item.pos }}</span>
              <div class="exp-text">
                <span v-for="(exp, i) in item.exps" :key="i">{{ exp }}<br v-if="i < item.exps.length - 1" /></span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { memorizeWord, submitList } from '../api/memorize'
import { addPrivate, checkPrivate, deletePrivate } from '../api/private'
import { useUserStore } from '../stores/user'
import { getCookie } from '../utils/cookie'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loadingWord = ref(false)
const submitting = ref(false)
const autoNext = ref(false)
const autoNextTimer = ref(null)

const totalNum = ref(0)
const revealed = ref(false)

const word = ref('Loading...')
const phonetic = ref('')
const wordId = ref(0)
const poses = ref([])

const isFavorited = ref(false)
const checkingFavorite = ref(false)
const togglingFavorite = ref(false)
const favoriteCheckRequestId = ref(0)

const submit = reactive({
  user: '',
  studying: '',
  num: 0,
  indexOfTotal: 1
})

const list = reactive({
  yes: [],
  no: []
})

const completed = computed(() => Math.max(submit.indexOfTotal - 1, 0))

const isLastWord = computed(() => totalNum.value > 0 && submit.indexOfTotal > totalNum.value)

const progressPercentage = computed(() => {
  if (totalNum.value <= 0) return 0
  return Math.min(Math.round((completed.value / totalNum.value) * 100), 100)
})

const favoriteDisabled = computed(() => {
  if (loadingWord.value) return true
  if (!word.value || word.value === 'Loading...') return true
  return false
})

function clearAutoNextTimer () {
  if (autoNextTimer.value) {
    clearTimeout(autoNextTimer.value)
    autoNextTimer.value = null
  }
}

function parsePoses (posesStr) {
  if (typeof posesStr !== 'string' || !posesStr) return []
  try {
    const result = []
    const arr1 = posesStr.split('**{')
    for (let i = 0; i < arr1.length; i++) {
      const pair = arr1[i].split('}++')
      if (i === 0 && pair[0]) pair[0] = pair[0].replace('{', '')
      const pos = (pair[0] || '').replace('}', '').trim()
      if (!pos) continue

      if (pair.length === 1) {
        result.push({ pos, exps: [''] })
        continue
      }

      const right = pair[1] || ''
      const exps = right.split('**++').map((s) => s)
      if (exps.length > 0) exps[0] = exps[0].replace('++', '')
      if (exps.length > 0) exps[exps.length - 1] = exps[exps.length - 1].replace('**', '')
      const cleaned = exps.map((s) => String(s || '').trim()).filter((s) => s.length > 0)
      result.push({ pos, exps: cleaned.length > 0 ? cleaned : [''] })
    }
    return result
  } catch {
    return []
  }
}

async function fetchWordData () {
  clearAutoNextTimer()
  if (loadingWord.value) return
  if (isLastWord.value) return

  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    router.replace('/')
    return
  }

  if (!submit.studying || submit.num <= 0) {
    ElMessage({ message: '缺少学习参数，请从开始页进入', type: 'warning', duration: 2000, offset: 80 })
    router.replace('/start')
    return
  }

  loadingWord.value = true
  revealed.value = false
  poses.value = []
  isFavorited.value = false
  try {
    const result = await memorizeWord({
      user: submit.user,
      studying: submit.studying,
      num: submit.num,
      indexOfTotal: submit.indexOfTotal
    })
    if (result.code !== 200) {
      throw new Error(result.msg || '获取单词失败')
    }
    const data = result.data || {}
    if (data.info === 'end') {
      totalNum.value = Math.max(completed.value, 0)
      return
    }
    totalNum.value = Number(data.totalNum || 0)
    word.value = data.word || ''
    phonetic.value = data.phonetic || ''
    wordId.value = Number(data.id || 0)
    poses.value = parsePoses(data.poses)
    await refreshFavoriteState()
  } catch (e) {
    ElMessage({ message: e?.message || '获取单词失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    loadingWord.value = false
  }
}

async function refreshFavoriteState () {
  userStore.restore()
  if (!username.value || username.value === 'Guest') return
  if (!word.value || word.value === 'Loading...') return

  const requestId = favoriteCheckRequestId.value + 1
  favoriteCheckRequestId.value = requestId
  checkingFavorite.value = true
  try {
    const result = await checkPrivate(username.value, word.value)
    if (requestId !== favoriteCheckRequestId.value) return
    if (result.code !== 200) {
      throw new Error(result.msg || '检查收藏状态失败')
    }
    isFavorited.value = Boolean(result.data)
  } catch (e) {
    if (requestId !== favoriteCheckRequestId.value) return
    ElMessage({ message: e?.message || '检查收藏状态失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    if (requestId === favoriteCheckRequestId.value) {
      checkingFavorite.value = false
    }
  }
}

async function toggleFavorite () {
  if (togglingFavorite.value) return

  userStore.restore()
  if (!username.value || username.value === 'Guest') {
    ElMessage({ message: '请先登录', type: 'warning', duration: 1500, offset: 80 })
    return
  }
  if (!word.value || word.value === 'Loading...') return

  togglingFavorite.value = true
  try {
    if (!isFavorited.value) {
      const obj = {
        username: username.value,
        origin: submit.studying,
        studying: submit.studying,
        id: wordId.value,
        word: word.value
      }
      const result = await addPrivate(obj)
      if (result.code !== 200) {
        throw new Error(result.msg || '收藏失败')
      }
      const info = result.data
      isFavorited.value = true
      ElMessage({
        message: info === 'duplicate' ? '已在生词本中' : '已收藏到生词本',
        type: 'success',
        duration: 1200,
        offset: 80
      })
    } else {
      const obj = {
        username: username.value,
        word: word.value,
        origin: submit.studying,
        studying: submit.studying,
        id: wordId.value
      }
      const result = await deletePrivate(obj)
      if (result.code !== 200) {
        throw new Error(result.msg || '取消收藏失败')
      }
      isFavorited.value = false
      ElMessage({ message: '已从生词本移除', type: 'success', duration: 1200, offset: 80 })
    }
  } catch (e) {
    ElMessage({ message: e?.message || '操作失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    togglingFavorite.value = false
  }
}

function recordResult (status) {
  if (!word.value) return
  list[status].push({ username: submit.user, status, word: word.value, id: wordId.value })
}

function scheduleAutoNext () {
  clearAutoNextTimer()
  if (!autoNext.value) return
  if (isLastWord.value) return
  autoNextTimer.value = setTimeout(() => {
    autoNextTimer.value = null
    fetchWordData()
  }, 650)
}

function chooseYes () {
  recordResult('yes')
  submit.indexOfTotal++
  revealed.value = true
  scheduleAutoNext()
}

function chooseNo () {
  recordResult('no')
  submit.indexOfTotal++
  revealed.value = true
  scheduleAutoNext()
}

function goNext () {
  fetchWordData()
}

async function submitCurrentList () {
  clearAutoNextTimer()
  if (submitting.value) return

  userStore.restore()
  if (!username.value || username.value === 'Guest') return

  submitting.value = true
  try {
    const currentTotalNum = list.yes.length + list.no.length
    const toSubmit = {
      user: submit.user,
      totalNum: currentTotalNum,
      yes: list.yes,
      no: list.no
    }
    const result = await submitList(toSubmit)
    if (result.code !== 200) {
      throw new Error(result.msg || '提交清单失败')
    }
    ElMessage({ message: '本组学习已提交', type: 'success', duration: 1500, offset: 80 })
    router.replace('/start')
  } catch (e) {
    ElMessage({ message: e?.message || '提交清单失败', type: 'error', duration: 2000, offset: 80 })
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  userStore.restore()
  submit.user = username.value && username.value !== 'Guest' ? username.value : getCookie('username') || ''
  submit.studying = String(route.query.studying || getCookie('studying') || '')
  submit.num = Number(route.query.num || 0)
  await fetchWordData()
})

onBeforeUnmount(() => {
  clearAutoNextTimer()
})
</script>

<style scoped>
.study-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.study-card {
  width: 100%;
  max-width: 520px;
  border-radius: 24px;
  padding: 20px;
}

.progress-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 22px;
}

.progress-text {
  text-align: right;
  color: var(--text-muted);
  font-size: 12px;
}

.word-display-area {
  text-align: center;
  margin-bottom: 18px;
}

.word-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.favorite-wrap {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.main-word {
  font-size: 46px;
  font-weight: 700;
  margin: 0 0 10px;
  background: linear-gradient(180deg, #fff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.favorite-btn {
  border-radius: 999px;
}

.favorite-label {
  font-size: 13px;
  color: var(--text-muted);
  user-select: none;
}

.favorite-label.on {
  color: #f59e0b;
  font-weight: 600;
}

.phonetic-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.22);
  color: var(--text-main);
  font-size: 14px;
}

.options-row {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.interaction-area {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.decision-buttons {
  display: flex;
  gap: 12px;
  width: 100%;
}

.choice-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
}

.yes-btn {
  background: rgba(34, 197, 94, 0.14);
  border: 1px solid rgba(34, 197, 94, 0.28);
  color: #22c55e;
}

.no-btn {
  background: rgba(239, 68, 68, 0.14);
  border: 1px solid rgba(239, 68, 68, 0.28);
  color: #ef4444;
}

.result-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.action-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
}

.definition-box {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 14px 14px;
}

.def-scroll-area {
  max-height: 220px;
  overflow: auto;
}

.pos-row {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.pos-row:last-child {
  border-bottom: none;
}

.pos-tag {
  flex: 0 0 auto;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.2);
  color: var(--text-main);
  font-size: 12px;
  height: fit-content;
}

.exp-text {
  flex: 1;
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.6;
}

.empty-def {
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
  padding: 12px 0;
}
</style>
