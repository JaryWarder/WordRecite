<template>
  <div class="progress-page">
    <el-skeleton v-if="loading" :rows="10" animated />
    
    <div v-else-if="!dashboardData" class="empty-state">
      <el-empty description="暂无学习数据，请先选择一本词书进行学习" />
    </div>

    <div v-else class="dashboard-content">
      <el-card class="ai-card">
        <template #header>
          <div class="card-header ai-card-header">
            <span class="ai-title">
              <span class="ai-icon">✦</span> AI 学情诊断
            </span>
            <el-button
              class="ai-btn"
              :loading="aiLoading"
              @click="fetchAiAnalysis"
            >获取深度分析</el-button>
          </div>
        </template>
        <div class="ai-body">
          <el-skeleton v-if="aiLoading" :rows="3" animated />
          <el-empty v-else-if="!aiText" description="点击上方按钮，获取专属学情分析" :image-size="60" />
          <div v-else class="ai-text" v-html="formattedAiText"></div>
        </div>
      </el-card>

      <div class="top-cards">
        <el-card class="stat-card">
          <div class="stat-title">当前词书</div>
          <div class="stat-val text-primary">{{ currentBook }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-title">总词汇量</div>
          <div class="stat-val">{{ dashboardData.bookSize }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-title">已学习</div>
          <div class="stat-val">{{ dashboardData.learnedUnique }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-title">已掌握</div>
          <div class="stat-val text-success">{{ dashboardData.masteredCount }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-title">待复习</div>
          <div class="stat-val text-warning">{{ dashboardData.dueCount }}</div>
        </el-card>
      </div>

      <div class="charts-container">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>记忆阶段分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>

        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>最近学习趋势</span>
              <el-radio-group v-model="days" size="small" @change="fetchData">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="lineChartRef" class="chart"></div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import MarkdownIt from 'markdown-it'
import { getDashboardData, getAiAnalysis } from '../api/progress'
import { getUserInfo } from '../api/user'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'

const md = new MarkdownIt()
const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(true)
const dashboardData = ref(null)
const currentBook = ref('')
const days = ref(30)
const aiLoading = ref(false)
const aiText = ref('')

const formattedAiText = computed(() => md.render(aiText.value || ''))

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const initCharts = () => {
  if (pieChart) pieChart.dispose()
  if (lineChart) lineChart.dispose()

  // 1. 渲染饼图 (记忆阶段分布)
  if (pieChartRef.value && dashboardData.value?.stageDist) {
    pieChart = echarts.init(pieChartRef.value)
    
    const rawDist = dashboardData.value.stageDist
    const stageMap = new Map()
    rawDist.forEach(item => {
      const s = item.stage ?? item.stg
      const c = item.count ?? item.cnt ?? 0
      stageMap.set(Number(s), Number(c))
    })

    const pieData = [0, 1, 2, 3, 4, 5].map(s => ({
      name: `阶段 ${s}`,
      value: stageMap.get(s) || 0
    }))

    const stageColors = ['#5470c6', '#91cc75', '#5b5c7e', '#fac858', '#14b8a6', '#f59e0b']
    
    pieChart.setOption({
      backgroundColor: 'transparent',
      color: stageColors,
      tooltip: { 
        trigger: 'item', 
        formatter: '{b} : {c} 词 ({d}%)',
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        borderColor: 'rgba(56, 189, 248, 0.4)',
        textStyle: { color: '#f8fafc' }
      },
      // --- 关键修改：使用两个 Legend 强制 3+3 排列 ---
      legend: [
        {
          data: ['阶段 0', '阶段 1', '阶段 2'],
          bottom: '8%', // 第一行位置
          left: 'center',
          itemGap: 25,
          textStyle: { color: '#94a3b8', fontSize: 12 },
          itemWidth: 12,
          itemHeight: 12
        },
        {
          data: ['阶段 3', '阶段 4', '阶段 5'],
          bottom: '2%', // 第二行位置
          left: 'center',
          itemGap: 25,
          textStyle: { color: '#94a3b8', fontSize: 12 },
          itemWidth: 12,
          itemHeight: 12
        }
      ],
      series: [
        {
          name: '记忆阶段',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '35%'], // 圆心进一步上移，防止压到两行图例
          avoidLabelOverlap: false,
          itemStyle: { 
            borderRadius: 8, 
            borderColor: 'rgba(30, 41, 59, 0.7)', 
            borderWidth: 2 
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold',
              color: '#f8fafc',
              formatter: '{b}\n{c} 词'
            }
          },
          data: pieData
        }
      ]
    })
  }

  // 2. 渲染折线图 (最近学习趋势)
  if (lineChartRef.value && dashboardData.value?.series) {
    lineChart = echarts.init(lineChartRef.value)
    const rawSeries = dashboardData.value.series
    const learnedArr = rawSeries.dailyLearned || []
    const reviewedArr = rawSeries.dailyReviewed || []
    
    const xDates = learnedArr.map(item => item.date || '')
    const learnedData = learnedArr.map(item => Number(item.count ?? 0))
    const reviewedData = reviewedArr.map(item => Number(item.count ?? 0))
    
    lineChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      legend: {
        data: ['新学', '复习'],
        textStyle: { color: '#94a3b8' },
        top: '5%'
      },
      grid: { left: '5%', right: '8%', bottom: '15%', top: '20%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xDates,
        axisLabel: { color: '#94a3b8', rotate: xDates.length > 7 ? 45 : 0 },
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.05)' } }
      },
      series: [
        {
          name: '新学',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#38bdf8' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(56, 189, 248, 0.3)' },
              { offset: 1, color: 'rgba(56, 189, 248, 0)' }
            ])
          },
          data: learnedData
        },
        {
          name: '复习',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#34d399' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(52, 211, 153, 0.3)' },
              { offset: 1, color: 'rgba(52, 211, 153, 0)' }
            ])
          },
          data: reviewedData
        }
      ]
    })
  }
}

// 其余 API 请求逻辑保持不变
const fetchAiAnalysis = async () => {
  aiLoading.value = true
  aiText.value = ''
  try {
    const res = await getAiAnalysis(currentBook.value)
    if (res.code === 200) {
      aiText.value = res.data || ''
    } else {
      ElMessage.error(res.msg || 'AI 分析失败')
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('AI 分析请求失败')
  } finally {
    aiLoading.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    userStore.restore()
    if (!username.value || username.value === 'Guest') return
    const userRes = await getUserInfo(username.value)
    if (userRes.code !== 200 || !userRes.data?.studying || userRes.data.studying === 'none') {
      dashboardData.value = null
      return
    }
    currentBook.value = userRes.data.studying
    const res = await getDashboardData(currentBook.value, days.value)
    if (res.code === 200) {
      dashboardData.value = res.data
    } else {
      dashboardData.value = null
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('获取进度数据失败')
  } finally {
    loading.value = false 
    await nextTick() 
    if (dashboardData.value) {
      initCharts() 
    }
  }
}

const handleResize = () => {
  if (pieChart) pieChart.resize()
  if (lineChart) lineChart.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (pieChart) pieChart.dispose()
  if (lineChart) lineChart.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.progress-page { padding: 20px; min-height: 100%; }
.empty-state { display: flex; justify-content: center; align-items: center; height: 60vh; }
.dashboard-content { display: flex; flex-direction: column; gap: 20px; }
.top-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px; }
.stat-card { border-radius: 16px; background: var(--glass-bg); border: 1px solid var(--glass-border); backdrop-filter: blur(10px); text-align: center; padding: 10px 0; }
.stat-title { color: var(--text-muted); font-size: 14px; margin-bottom: 8px; }
.stat-val { font-size: 24px; font-weight: bold; color: var(--text-main); }
.text-primary { color: #38bdf8; }
.text-success { color: #34d399; }
.text-warning { color: #fbbf24; }
.charts-container { display: grid; grid-template-columns: 1fr 2fr; gap: 20px; }
@media (max-width: 900px) { .charts-container { grid-template-columns: 1fr; } }
.chart-card { border-radius: 16px; background: var(--glass-bg); border: 1px solid var(--glass-border); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; color: var(--text-main); font-weight: bold; padding: 15px 20px; border-bottom: 1px solid var(--glass-border); }
.chart { width: 100%; height: 400px; padding: 10px; }
.ai-card { border-radius: 16px; background: linear-gradient(135deg, rgba(56, 189, 248, 0.08), rgba(99, 102, 241, 0.08)); border: 1px solid rgba(99, 102, 241, 0.35); backdrop-filter: blur(12px); box-shadow: 0 0 18px rgba(99, 102, 241, 0.15); }
.ai-card-header { background: transparent; }
.ai-title { font-size: 16px; font-weight: bold; color: #a5b4fc; display: flex; align-items: center; gap: 6px; }
.ai-icon { color: #818cf8; font-size: 18px; }
.ai-btn { background: linear-gradient(90deg, #6366f1, #38bdf8); border: none; color: #fff; font-weight: 600; border-radius: 8px; padding: 6px 18px; }
.ai-btn:hover { opacity: 0.85; }
.ai-body { min-height: 80px; padding: 4px 0; }
.ai-text { color: #e2e8f0; font-size: 15px; line-height: 1.9; letter-spacing: 0.02em; }
</style>