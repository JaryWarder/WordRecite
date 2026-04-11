<template>
  <div class="testing-container">
    <div class="question-card">
      <div class="progress-bar-top">
        <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
      </div>

      <div class="question-content">
        <h1 class="test-word">{{ word }}</h1>
        <p class="phonetic-hint" v-if="phonetic">{{ phonetic }}</p>
      </div>

      <div class="answer-section">
        <transition name="fade-in" mode="out-in">
          <div class="options-grid" v-if="!clicked">
            <el-button class="opt-btn btn-no" @click="clickNo">
              <span class="key-hint">A</span> 不认识
            </el-button>
            <el-button class="opt-btn btn-yes" @click="clickYes">
              <span class="key-hint">B</span> 认识
            </el-button>
          </div>

          <div class="next-action" v-else>
            <el-button type="primary" class="next-step-btn" @click="getNextOne">
              {{ isLastWord ? '查看成绩' : '下一题' }} <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑完全不变
import $ from 'jquery'
import { getCookie } from '../../util.js'
export default {
  name: 'TestCard',
  data () {
    return {
      total: 30,
      indexOfTotal: 0,
      word: 'Loading...',
      phonetic: '',
      words: [],
      phonetics: [],
      get: 0,
      clicked: false
    }
  },
  computed: {
    isLastWord () {
      return this.indexOfTotal === this.total - 1;
    },
    progressPercentage () {
      return Math.round(((this.indexOfTotal + 1) / this.total) * 100);
    }
  },
  methods: {
    clickYes: function () {
      this.get++;
      this.clicked = true;
    },
    clickNo: function () {
      this.clicked = true;
    },
    getNextOne: function () {
      if (this.isLastWord) {
        this.$router.push({path: this.$router.options.routes[7].children[2].path, query: {num: this.get}});
      } else {
        this.indexOfTotal++;
        this.word = this.words[this.indexOfTotal];
        this.phonetic = this.phonetics[this.indexOfTotal];
        this.clicked = false;
      }
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/get_test/' + getCookie('username'),
      crossDomain: true,
      xhrFields: { withCredentials: true },
      dataType: 'json',
      data: {},
      success: (result) => {
        if (result && result.length > 0) {
          for (let i = 0; i < result.length; i += 2) {
            this.words.push(result[i]);
            this.phonetics.push(result[i + 1]);
          }
          this.word = this.words[0];
          this.phonetic = this.phonetics[0];
        } else {
          this.$message.error('获取测试数据失败');
          this.$router.back();
        }
      },
      error: () => {
        this.$message.error('获取测试数据失败，请检查网络');
        this.$router.back();
      }
    })
  }
}
</script>

<style scoped>
.testing-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.question-card {
  width: 100%;
  max-width: 600px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.progress-bar-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
}

.progress-fill {
  height: 100%;
  background: var(--accent-color);
  transition: width 0.3s ease;
}

.question-content {
  text-align: center;
  margin: 40px 0 60px;
}

.test-word {
  font-size: 56px;
  margin: 0 0 10px;
  color: var(--text-main);
}

.phonetic-hint {
  color: var(--text-muted);
  font-size: 20px;
  font-family: serif;
}

.options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.opt-btn {
  height: 80px;
  font-size: 20px;
  border-radius: 16px !important;
  position: relative;
  border: none !important;
  color: white !important;
}

.btn-no { background: rgba(255, 255, 255, 0.1) !important; color: var(--text-muted) !important; }
.btn-no:hover { background: rgba(244, 63, 94, 0.2) !important; color: #f43f5e !important; }

.btn-yes { background: var(--accent-gradient) !important; }
.btn-yes:hover { opacity: 0.9; }

.key-hint {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 12px;
  opacity: 0.5;
}

.next-action {
  text-align: center;
}

.next-step-btn {
  width: 200px;
  height: 50px;
  border-radius: 25px !important;
  font-size: 18px;
}
</style>
