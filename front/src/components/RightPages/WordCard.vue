<template>
  <div class="study-wrapper">
    <div class="study-card">
      <div class="progress-header">
        <span class="progress-text">进度 {{ submit.indexOfTotal - 1 }} / {{ totalNum }}</span>
        <el-progress
          :percentage="progressPercentage"
          :show-text="false"
          :stroke-width="6"
          color="#38bdf8"
          class="custom-progress">
        </el-progress>
      </div>

      <div class="word-display-area">
        <h1 class="main-word">{{ word }}</h1>
        <div class="phonetic-badge" v-if="phonetic">
          {{ phonetic }}
        </div>
      </div>

      <div class="interaction-area">
        <transition name="fade-scale" mode="out-in">
          <div class="decision-buttons" v-if="!clicked">
            <el-button class="choice-btn no-btn" @click="addToNo">
              <i class="el-icon-close"></i> 不认识
            </el-button>
            <el-button class="choice-btn yes-btn" @click="addToYes">
              <i class="el-icon-check"></i> 认识
            </el-button>
          </div>

          <div class="result-actions" v-else>
            <div class="nav-buttons">
              <el-button type="primary" plain icon="el-icon-star-off" @click="addPrivate" class="action-btn">
                收藏
              </el-button>
              <el-button type="primary" @click="getNextOne" :disabled="isLastWord" class="action-btn next-btn">
                下一个 <i class="el-icon-right"></i>
              </el-button>
            </div>
            <div class="submit-wrapper" v-if="isLastWord">
              <NextList @nextList="getNextList"></NextList>
            </div>
          </div>
        </transition>
      </div>

      <transition name="el-zoom-in-top">
        <div class="definition-box" v-show="clicked">
          <div class="def-scroll-area">
            <div v-for="(item, index) in poses" :key="index" class="pos-row">
              <span class="pos-tag">{{ item.pos }}</span>
              <div class="exp-text">
                <span v-for="(exp, i) in item.exps" :key="i">{{ exp }}<br v-if="i < item.exps.length-1"/></span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import NextList from '../small/NextList'
import $ from 'jquery'
import { getCookie } from '../../util.js'

export default {
  name: 'WordCard',
  components: { NextList },
  data () {
    return {
      totalNum: 20,
      clicked: false,
      word: 'Loading...',
      id: 0,
      phonetic: '',
      poses: null,
      studying: '',
      submit: {
        studying: '',
        user: '',
        num: 0,
        indexOfTotal: 1
      },
      list: {
        yes: [],
        no: []
      }
    }
  },
  computed: {
    isLastWord () {
      return this.submit.indexOfTotal > this.totalNum
    },
    progressPercentage () {
      if (this.totalNum === 0) return 0
      const completed = this.submit.indexOfTotal - 1
      return Math.min(Math.round((completed / this.totalNum) * 100), 100)
    }
  },
  methods: {
    addPrivate () {
      let submitData = { word: this.word, studying: this.submit.studying, username: getCookie('username'), id: this.id }
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/add_private',
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {obj: JSON.stringify(submitData)},
        success: (result) => {
          if (result.info === 'duplicate') this.$message.warning('该单词已在生词本中')
          else if (result.info === 'success') this.$message.success('已加入生词本')
          else this.$message.error('添加失败')
        },
        error: () => { this.$message.error('添加失败，请检查网络') }
      })
    },
    addToYes () {
      this.clicked = true
      this.list.yes.push({ username: this.submit.user, status: 'yes', word: this.word, id: this.id })
      this.submit.indexOfTotal++
    },
    addToNo () {
      this.clicked = true
      this.list.no.push({ username: this.submit.user, status: 'no', word: this.word, id: this.id })
      this.submit.indexOfTotal++
    },
    getNextList () {
      // --- 【逻辑修复关键点】 ---
      // 计算本次提交的总单词数，确保后端 updateStudied 正确执行
      let currentTotalNum = this.list.yes.length + this.list.no.length;
      let toSubmit = {
        yes: this.list.yes,
        no: this.list.no,
        totalNum: currentTotalNum, // <--- 这里加上了 totalNum
        indexOfTotal: this.submit.indexOfTotal
      }
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/submit_list',
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {obj: JSON.stringify(toSubmit)},
        success: (result) => {
          if (result.info === 'success') {
            this.$message.success('本组学习完成，即将开始下一组')
            window.location.reload()
          } else {
            this.$message.error('提交清单失败')
          }
        },
        error: () => { this.$message.error('提交清单失败，请检查网络') }
      })
    },
    fetchWordData () {
      this.clicked = false
      this.poses = null
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/memorize',
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {obj: JSON.stringify(this.submit)},
        success: (result) => {
          if (result.info === 'success') {
            this.totalNum = parseInt(result.totalNum)
            this.word = result.word
            this.phonetic = result.phonetic
            this.id = parseInt(result.id)
            this.poses = []
            let arr1 = result.poses.split('**{')
            for (let i = 0; i < arr1.length; i++) {
              let pair = arr1[i].split('}++')
              if (i === 0) { pair[0] = pair[0].replace('{', '') }
              if (pair.length === 1) {
                pair[0] = pair[0].replace('}', '')
                this.poses.push({pos: pair[0], exps: ['']})
              } else {
                let exps = pair[1].split('**++')
                exps[0] = exps[0].replace('++', '')
                exps[exps.length - 1] = exps[exps.length - 1].replace('**', '')
                this.poses.push({pos: pair[0], exps: exps})
              }
            }
          } else {
            this.$message.error('获取下一个单词失败')
          }
        },
        error: () => { this.$message.error('获取下一个单词失败，请检查网络') }
      })
    },
    getNextOne () {
      if (this.isLastWord) return
      this.fetchWordData()
    }
  },
  mounted () {
    this.submit.user = getCookie('username')
    this.submit.studying = getCookie('studying')
    this.submit.num = this.$route.query.num
    this.fetchWordData()
  }
}
</script>

<style scoped>
.study-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.study-card {
  width: 100%;
  max-width: 500px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.progress-header {
  margin-bottom: 40px;
}

.progress-text {
  display: block;
  text-align: right;
  color: var(--text-muted);
  font-size: 12px;
  margin-bottom: 5px;
}

.word-display-area {
  text-align: center;
  margin-bottom: 50px;
}

.main-word {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 10px;
  background: linear-gradient(180deg, #fff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.phonetic-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  color: var(--accent-color);
  font-family: 'Times New Roman', serif;
  font-size: 18px;
}

.interaction-area {
  min-height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.decision-buttons {
  display: flex;
  gap: 20px;
  width: 100%;
}

.choice-btn {
  flex: 1;
  height: 60px;
  font-size: 18px;
  border: none !important;
  color: white !important;
  border-radius: 16px !important;
  transition: transform 0.2s;
}

.choice-btn:hover {
  transform: scale(1.05);
}

.no-btn {
  background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%) !important;
  box-shadow: 0 4px 20px rgba(244, 63, 94, 0.4);
}

.yes-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.4);
}

.result-actions {
  width: 100%;
}

.nav-buttons {
  display: flex;
  gap: 15px;
}

.action-btn {
  height: 50px;
  font-size: 16px;
  border-radius: 12px !important;
}

.next-btn {
  flex: 1;
}

.submit-wrapper {
  margin-top: 20px;
  text-align: center;
}

.definition-box {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
}

.def-scroll-area {
  max-height: 200px;
  overflow-y: auto;
  padding-right: 5px;
}

/* 自定义滚动条 */
.def-scroll-area::-webkit-scrollbar {
  width: 4px;
}
.def-scroll-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.pos-row {
  display: flex;
  margin-bottom: 12px;
}

.pos-tag {
  color: var(--accent-color);
  font-weight: bold;
  width: 50px;
  flex-shrink: 0;
}

.exp-text {
  color: var(--text-main);
  line-height: 1.5;
}

/* 动画效果 */
.fade-scale-enter-active, .fade-scale-leave-active {
  transition: all 0.3s ease;
}
.fade-scale-enter {
  opacity: 0;
  transform: scale(0.9);
}
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
