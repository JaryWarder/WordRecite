<template>
  <div class="info-container">
    <div class="word-detail-card">
      <div class="word-header">
        <div class="header-left">
          <h1 class="main-word">{{ word }}</h1>
          <div class="phonetic-box">
            <span class="phonetic-text">{{ phonetic }}</span>
            <a :href="pron" target="_blank" class="audio-btn" title="发音">
              <i class="el-icon-mic"></i>
            </a>
          </div>
        </div>
        <div class="book-tag">
          <i class="el-icon-collection"></i> {{ bookInfo.title }}
        </div>
      </div>

      <div class="divider"></div>

      <div class="content-section">
        <h3 class="section-label">释义解析</h3>
        <div class="pos-list">
          <div v-for="(item, index) in poses" :key="index" class="pos-item">
            <div class="pos-tag">{{ item.pos }}</div>
            <ul class="exp-list">
              <li v-for="(exp, i) in item.exps" :key="i">{{ exp }}</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="content-section" v-if="sentences && sentences.length > 0">
        <h3 class="section-label">经典例句</h3>
        <div class="sentences-list">
          <div v-for="(item, index) in sentences" :key="index" class="sentence-item">
            <i class="el-icon-chat-dot-round"></i>
            <span>{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="detail-footer">
        <el-button-group>
          <el-button type="primary" plain @click="getLastOne" :disabled="bookInfo.id === 0" icon="el-icon-arrow-left">上一个</el-button>
          <el-button type="primary" plain @click="getNextOne" :disabled="bookInfo.id === (bookInfo.num - 1)">下一个 <i class="el-icon-arrow-right el-icon--right"></i></el-button>
        </el-button-group>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑完全不变
import $ from 'jquery'
export default {
  name: 'WordInfo',
  data () {
    return {
      word: '',
      phonetic: '',
      pron: '',
      poses: null,
      sentences: null,
      bookInfo: { id: 0, title: '', num: 0 },
      msg: ''
    }
  },
  mounted: function () {
    this.bookInfo.title = this.$route.query.title;
    this.bookInfo.num = this.$route.query.num;
    this.msg = `书名：《${this.bookInfo.title}》  总词数：${this.bookInfo.num}`;
    this.getWordInfo();
  },
  methods: {
    getLastOne: function () {
      if (this.bookInfo.id > 0) {
        this.bookInfo.id--;
        this.getWordInfo();
      }
    },
    getNextOne: function () {
      if (this.bookInfo.id < this.bookInfo.num - 1) {
        this.bookInfo.id++;
        this.getWordInfo();
      }
    },
    getWordInfo: function () {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/get_wordInfo',
        crossDomain: true,
        xhrFields: { withCredentials: true },
        dataType: 'json',
        data: {obj: JSON.stringify(this.bookInfo)},
        success: (result) => {
          if (result === null) {
            this.$message.error('获取单词信息失败');
          } else {
            this.word = result.word;
            this.phonetic = result.phonetic;
            this.pron = result.pron;

            let arr = result.sentences.split('**++');
            arr[0] = arr[0].replace('++', '');
            arr[arr.length - 1] = arr[arr.length - 1].replace('**', '');
            this.sentences = Array(0).concat(arr);

            this.poses = [];
            let arr1 = result.poses.split('**{');
            for (let i = 0; i < arr1.length; i++) {
              let pair = arr1[i].split('}++');
              if (i === 0) { pair[0] = pair[0].replace('{', ''); }
              if (pair.length === 1) {
                pair[0] = pair[0].replace('}', '');
                this.poses.push({pos: pair[0], exps: ['']});
              } else {
                let exps = pair[1].split('**++');
                exps[0] = exps[0].replace('++', '');
                exps[exps.length - 1] = exps[exps.length - 1].replace('**', '');
                this.poses.push({ pos: pair[0], exps: exps });
              }
            }
          }
        },
        error: () => { this.$message.error('获取单词信息失败，请检查网络'); }
      })
    }
  }
}
</script>

<style scoped>
.info-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.word-detail-card {
  width: 100%;
  max-width: 800px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 40px;
  position: relative;
}

.word-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.main-word {
  font-size: 48px;
  margin: 0;
  color: var(--text-main);
  letter-spacing: 1px;
}

.phonetic-box {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.phonetic-text {
  font-family: 'Times New Roman', serif;
  font-size: 20px;
  color: var(--text-muted);
}

.audio-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(56, 189, 248, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
  text-decoration: none;
  transition: all 0.2s;
}
.audio-btn:hover {
  background: var(--accent-color);
  color: white;
}

.book-tag {
  background: rgba(0, 0, 0, 0.3);
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-muted);
}

.divider {
  height: 1px;
  background: var(--glass-border);
  margin: 30px 0;
}

.section-label {
  font-size: 14px;
  color: var(--accent-color);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.pos-item {
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
}

.pos-tag {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  height: fit-content;
  font-weight: bold;
  font-size: 14px;
  min-width: 40px;
  text-align: center;
}

.exp-list, .sentences-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
}

.exp-list li {
  margin-bottom: 5px;
  line-height: 1.6;
}

.sentence-item {
  background: rgba(0, 0, 0, 0.2);
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
  color: var(--text-muted);
}

.detail-footer {
  margin-top: 40px;
  text-align: center;
}
</style>
