<template>
  <div class="progress-wrapper">
    <div class="dashboard-header">
      <h2>学习数据分析</h2>
    </div>

    <div class="dashboard-grid">
      <div class="left-column">
        <el-card class="dashboard-card">
          <div v-if="dataLoaded" class="charts-container">
            <div class="chart-box">
              <div id="pieChart" class="echart-instance"></div>
            </div>
            <div class="divider-horizontal"></div>
            <div class="chart-box">
              <div id="barChart" class="echart-instance"></div>
            </div>
          </div>
          <div v-else class="loading-state">
            <div class="loading-spinner"><i class="el-icon-loading"></i></div>
            <p>正在同步数据库统计...</p>
          </div>
        </el-card>
      </div>

      <div class="right-column">
        <el-card class="dashboard-card ai-card">
          <div slot="header" class="ai-header">
            <span><i class="el-icon-cpu"></i> AI 学习诊断</span>
            <el-tag size="mini" type="success" effect="dark">Online</el-tag>
          </div>
          <div class="ai-content">
            <div v-if="!aiResult && !aiLoading" class="ai-placeholder">
              <i class="el-icon-data-analysis large-icon"></i>
              <p>点击下方按钮，AI 将根据您的数据库记录<br>生成个性化学习周报与建议。</p>
            </div>

            <div v-else class="ai-response-box">
              <div class="ai-avatar">
                <i class="el-icon-s-custom"></i>
              </div>
              <div class="ai-text">
                <p v-html="formattedAiResult"></p>
                <span v-if="aiLoading" class="typing-cursor">|</span>
              </div>
            </div>
          </div>

          <div class="ai-footer">
            <el-button
              type="primary"
              class="ai-btn"
              :loading="aiLoading"
              @click="generateAiReport"
              :disabled="!dataLoaded">
              {{ aiLoading ? '正在分析数据...' : '生成 AI 诊断报告' }}
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import echarts from 'echarts'
import $ from 'jquery'
import { getCookie } from '../../util.js'

export default {
  name: 'Progress',
  data () {
    return {
      pieChart: null,
      barChart: null,
      dataLoaded: false,
      aiLoading: false,
      aiResult: '',
      opinionData: [{value: 0, name: '已学习'}, {value: 0, name: '待学习'}],
      barChartData: [0, 0, 0, 0, 0, 0, 0],
      // 存储从服务器获取的日期信息
      serverDateInfo: {
        dateStr: '', // 如：2025年11月29日 星期六
        timestamp: 0 
      },
      dateLabels: [], // 图表 X 轴标签
      totalStudied: 0,
      totalToStudy: 0
    }
  },
  computed: {
    formattedAiResult () {
      let text = this.aiResult;
      text = text.replace(/```html/gi, '').replace(/```/g, '');
      text = text.trim();
      text = text.replace(/\n\s*\n/g, '\n');
      if (!text.includes('<div') && !text.includes('<h3')) {
         text = text.replace(/\n/g, '<br>');
      }
      return text;
    }
  },
  methods: {
    // 根据服务器时间戳生成最近7天标签
    generateDateLabels(serverTimestamp) {
      const days = [];
      const baseDate = new Date(parseInt(serverTimestamp));
      
      for (let i = 6; i >= 0; i--) {
        const d = new Date(baseDate);
        d.setDate(baseDate.getDate() - i);
        
        const month = (d.getMonth() + 1).toString().padStart(2, '0');
        const day = d.getDate().toString().padStart(2, '0');
        let label = `${month}-${day}`;
        if (i === 0) label = '今天';
        days.push(label);
      }
      return days;
    },

    async generateAiReport () {
      this.aiLoading = true;
      this.aiResult = '';

      // 使用从后端获取的服务器时间戳作为基准
      // 如果没有获取到（比如后端挂了），降级使用本地时间
      const baseTimestamp = parseInt(this.serverDateInfo.timestamp || new Date().getTime());
      const baseDate = new Date(baseTimestamp);
      const weekMap = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

      // ★★★ 核心修复：构建详细的每日数据（日期+星期+数量） ★★★
      // 我们倒推最近7天，把每一天是星期几算出来，拼成字符串喂给 AI
      const dailyDetails = this.barChartData.map((count, index) => {
        // index=0 是6天前，index=6 是今天
        const d = new Date(baseTimestamp);
        d.setDate(baseDate.getDate() - (6 - index));
        
        const dateStr = `${d.getMonth() + 1}/${d.getDate()}`;
        const weekStr = weekMap[d.getDay()];
        return `${dateStr}(${weekStr}): ${count}词`;
      });

      const todayInfo = this.serverDateInfo.dateStr || '未知日期';

      // 构建 Prompt：把 dailyDetails 塞进去
      const prompt = `
        你是一位严格但幽默的英语私教。
        【当前时间】：${todayInfo}
        
        【最近7天详细学习记录】：
        ${JSON.stringify(dailyDetails)}
        
        【统计】：
        - 累计已学：${this.totalStudied} 词
        - 剩余词汇：${this.totalToStudy} 词
        
        请严格根据上述【详细记录】中的星期和数据进行分析（不要自己瞎编是周几）：
        1. 输出一份【HTML格式】的诊断报告（不要使用Markdown代码块）。
        2. 结构要求：
           <h3 class="report-title">📝 ${todayInfo.split(' ')[0]} 诊断书</h3>
           <div class="report-grid">
             <div class="report-item"><b>📈 趋势分析：</b>（结合具体星期几点评，比如"周六是你状态最好的一天"）</div>
             <div class="report-item"><b>✨ 高光时刻：</b>（指出数据最高的那一天是星期几）</div>
             <div class="report-item"><b>💀 致命槽点：</b>（指出哪几天挂零了，语气要辛辣）</div>
             <div class="report-item"><b>💊 拯救方案：</b>（结合当前进度给出建议）</div>
           </div>
        
        字数控制在200字以内。
      `;

      try {
        // [IMPORTANT] API Key has been removed for security before committing to GitHub.
        // Please replace with your actual API Key or use environment variables.
        const API_KEY = 'YOUR_DEEPSEEK_API_KEY'; 
        const API_URL = 'https://api.deepseek.com/chat/completions'; 

        const response = await fetch(API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${API_KEY}`
          },
          body: JSON.stringify({
            model: "deepseek-chat",
            messages: [{ role: "user", content: prompt }],
            temperature: 0.7,
            stream: false
          })
        });

        const data = await response.json();
        
        if (data.error) {
          this.aiResult = `<span style='color: red'>API 报错: ${data.error.message}</span>`;
        } else if (data.choices && data.choices.length > 0) {
          this.aiResult = data.choices[0].message.content;
        } else {
          this.aiResult = "<span style='color: red'>API 返回格式异常</span>";
        }

      } catch (error) {
        this.aiResult = `<span style='color: red'>网络请求失败: ${error.message}</span>`;
      } finally {
        this.aiLoading = false;
      }
    },

    drawPie (id) {
      this.pieChart = echarts.init(document.getElementById(id));
      this.pieChart.setOption({
        backgroundColor: 'transparent',
        title: { text: '完成度', left: 'center', bottom: 0, textStyle: { color: '#94a3b8', fontSize: 14 } },
        tooltip: { trigger: 'item' },
        color: ['#38bdf8', '#334155'],
        series: [{
            type: 'pie', radius: ['60%', '80%'], center: ['50%', '45%'],
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: '20', fontWeight: 'bold', color: '#fff' } },
            data: this.opinionData
        }]
      })
    },
    
    drawBar (id) {
      this.barChart = echarts.init(document.getElementById(id));
      this.barChart.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '0%', right: '0%', bottom: '0%', top: '10%', containLabel: true },
        // X轴使用动态日期标签
        xAxis: [{ type: 'category', data: this.dateLabels, axisLine: { show: false }, axisLabel: { color: '#94a3b8', interval: 0, fontSize: 10 } }],
        yAxis: [{ type: 'value', show: false }],
        series: [{ type: 'bar', barWidth: '50%', data: this.barChartData, itemStyle: { color: '#38bdf8', barBorderRadius: [4, 4, 0, 0] } }]
      })
    },
    
    // --- 页面初始化流程 ---
    async initPage() {
      try {
        // 1. 先从后端获取标准时间
        const timeResponse = await fetch('http://localhost:8080/Hello/get_server_date');
        const timeData = await timeResponse.json();
        
        this.serverDateInfo.dateStr = timeData.date;
        this.serverDateInfo.timestamp = timeData.timestamp;
        this.dateLabels = this.generateDateLabels(timeData.timestamp);
      } catch (e) {
        console.error('获取服务器时间失败，降级为本地时间', e);
        const d = new Date();
        this.serverDateInfo.dateStr = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`;
        this.dateLabels = this.generateDateLabels(d.getTime());
      }

      // 2. 获取进度数据
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/get_progress/' + getCookie('username'),
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        success: (result) => {
          if (result.info === 'success') {
            this.totalStudied = parseInt(result.studied);
            this.totalToStudy = parseInt(result.toStudy);
            this.opinionData[0].value = this.totalStudied;
            this.opinionData[1].value = this.totalToStudy;
            
            this.barChartData = [
              parseInt(result.day1), parseInt(result.day2), parseInt(result.day3),
              parseInt(result.day4), parseInt(result.day5), parseInt(result.day6),
              parseInt(result.day7)
            ];
            
            this.dataLoaded = true;
            this.$nextTick(() => {
              this.drawPie('pieChart');
              this.drawBar('barChart');
            })
          }
        },
        error: () => { this.$message.error('数据加载失败'); }
      });
    }
  },
  mounted () {
    this.initPage();
    window.addEventListener('resize', () => {
      this.pieChart && this.pieChart.resize();
      this.barChart && this.barChart.resize();
    });
  }
}
</script>

<style scoped>
.progress-wrapper { padding: 20px; }
.dashboard-header { text-align: center; margin-bottom: 30px; }
.dashboard-header h2 { margin: 0 0 5px; color: var(--text-main); }
.dashboard-header p { color: var(--text-muted); font-size: 14px; }

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .dashboard-grid { grid-template-columns: 1fr; }
}

.dashboard-card {
  height: 100%;
  min-height: 450px;
  border-radius: 20px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
}

.charts-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
}
.chart-box { flex: 1; min-height: 200px; width: 100%; }
.echart-instance { width: 100%; height: 100%; }
.divider-horizontal { height: 1px; width: 100%; background: var(--glass-border); margin: 20px 0; }

.ai-card { display: flex; flex-direction: column; }
.ai-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; color: var(--accent-color); font-weight: bold; }
.ai-content { flex: 1; padding: 20px; overflow-y: auto; max-height: 350px; }

.ai-placeholder {
  text-align: center;
  color: var(--text-muted);
  margin-top: 60px;
}
.large-icon { font-size: 48px; margin-bottom: 15px; opacity: 0.5; }

.ai-response-box { display: flex; gap: 15px; }
.ai-avatar { 
  width: 40px; height: 40px; border-radius: 50%; 
  background: var(--accent-gradient); 
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: white; flex-shrink: 0;
}
.ai-text {
  background: rgba(255, 255, 255, 0.05);
  padding: 15px;
  border-radius: 0 15px 15px 15px;
  color: var(--text-main);
  line-height: 1.6;
  font-size: 14px;
}

.ai-footer { padding: 20px; border-top: 1px solid var(--glass-border); }
.ai-btn { width: 100%; border-radius: 12px; font-weight: bold; background: var(--accent-gradient); border: none; }
.ai-btn:hover { opacity: 0.9; }

.loading-state { height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: var(--accent-color); }
.typing-cursor { display: inline-block; animation: blink 1s infinite; margin-left: 2px; }
@keyframes blink { 50% { opacity: 0; } }

/* --- AI 报告专用样式 --- */
::v-deep .report-title {
  color: var(--accent-color);
  margin: 0 0 15px 0;
  font-size: 18px;
  text-align: center;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.2);
  padding-bottom: 10px;
}

::v-deep .report-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

::v-deep .report-item {
  background: rgba(255, 255, 255, 0.08); 
  padding: 12px;
  border-radius: 8px;
  border-left: 3px solid var(--accent-color); 
  font-size: 14px;
  line-height: 1.6;
  color: #e2e8f0;
}

::v-deep .report-item b {
  color: #38bdf8; 
  font-size: 15px;
  display: block; 
  margin-bottom: 4px;
}

::v-deep .report-item:nth-child(3) {
  border-left-color: #f43f5e;
}
::v-deep .report-item:nth-child(3) b {
  color: #f43f5e;
}

::v-deep .report-item:nth-child(4) {
  border-left-color: #10b981;
}
::v-deep .report-item:nth-child(4) b {
  color: #10b981;
}
</style>