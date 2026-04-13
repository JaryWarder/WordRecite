<template>
  <div class="progress-page">
    <el-skeleton v-if="loading" :rows="10" animated />
    
    <div v-else-if="!dashboardData" class="empty-state">
      <el-empty description="暂无学习数据，请先选择一本词书进行学习" />
    </div>

    <div v-else class="dashboard-content">
      <!-- 顶部卡片 -->
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

      <!-- 图表区 -->
      <div class="charts-container">
        <!-- 饼图：记忆阶段分布 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>记忆阶段分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>

        <!-- 折线图：最近学习趋势 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>最近学习趋势</span>
              <el-radio-group v-model="days" size="small" @change="fetchData">
                <el-radio-button :value="7">7天</el-radio-button>
                <el-radio-button :value="30">30天</el-radio-button>
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
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardData } from '../api/progress'
import { getUserInfo } from '../api/user'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const { username } = storeToRefs(userStore)

const loading = ref(true)
const dashboardData = ref(null)
const currentBook = ref('')
const days = ref(30)

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const initCharts = () => {
  if (pieChart) pieChart.dispose()
  if (lineChart) lineChart.dispose()

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const stageDist = dashboardData.value.stageDist || {}
    const data = Object.keys(stageDist).map(key => ({
      name: `阶段 ${key}`,
      value: stageDist[key]
    }))
    
    pieChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)'
      },
      legend: {
        bottom: '5%',
        left: 'center',
        textStyle: { color: 'var(--text-main)' }
      },
      series: [
        {
          name: '记忆阶段',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: 'var(--glass-bg)',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold',
              color: 'var(--text-main)'
            }
          },
          labelLine: {
            show: false
          },
          data
        }
      ]
    })
  }

  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    const series = dashboardData.value.series || { dates: [], dailyLearned: [], dailyReviewed: [] }
    
    lineChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['新学', '复习'],
        textStyle: { color: 'var(--text-main)' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: series.dates,
        axisLabel: { color: 'var(--text-muted)' }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: 'var(--text-muted)' },
        splitLine: {
          lineStyle: {
            color: 'rgba(255, 255, 255, 0.1)'
          }
        }
      },
      series: [
        {
          name: '新学',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#38bdf8' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(56, 189, 248, 0.5)' },
              { offset: 1, color: 'rgba(56, 189, 248, 0)' }
            ])
          },
          data: series.dailyLearned
        },
        {
          name: '复习',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#34d399' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(52, 211, 153, 0.5)' },
              { offset: 1, color: 'rgba(52, 211, 153, 0)' }
            ])
          },
          data: series.dailyReviewed
        }
      ]
    })
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    userStore.restore()
    if (!username.value || username.value === 'Guest') {
      loading.value = false
      return
    }
    
    const userRes = await getUserInfo(username.value)
    if (userRes.code !== 200 || !userRes.data?.studying || userRes.data.studying === 'none') {
      dashboardData.value = null
      loading.value = false
      return
    }
    
    currentBook.value = userRes.data.studying
    
    const res = await getDashboardData(currentBook.value, days.value)
    if (res.code === 200) {
      dashboardData.value = res.data
      await nextTick()
      initCharts()
    } else {
      dashboardData.value = null
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('获取进度数据失败')
  } finally {
    loading.value = false
  }
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

const handleResize = () => {
  if (pieChart) pieChart.resize()
  if (lineChart) lineChart.resize()
}

</script>

<style scoped>
.progress-page {
  padding: 20px;
  min-height: 100%;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.top-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
  background: var(--glass-bg);
  border: 1px solid rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 10px 0;
}

.stat-title {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-val {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-main);
}

.text-primary {
  color: #38bdf8;
}

.text-success {
  color: #34d399;
}

.text-warning {
  color: #fbbf24;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

@media (max-width: 900px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  border-radius: 16px;
  background: var(--glass-bg);
  border: 1px solid rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-main);
  font-weight: bold;
}

.chart {
  width: 100%;
  height: 350px;
}
</style>
