<template>
  <div class="books-container">
    <div class="page-header">
      <h2>我的单词书库</h2>
      <p>选择一本单词书，开启你的学习之旅</p>
    </div>

    <div class="books-grid">
      <div class="book-card" v-for="(item, index) in booksInfo" :key="index" :class="{ 'active': item.title === studying }">
        <div class="book-cover">
          <i class="el-icon-reading"></i>
          <div class="book-badge" v-if="item.title === studying">正在学习</div>
        </div>
        <div class="book-info">
          <h3 class="book-title">{{ item.title }}</h3>
          <div class="book-meta">
            <i class="el-icon-collection-tag"></i> 共 {{ item.num }} 词
          </div>
        </div>

        <div class="book-actions">
          <ChangeStudying v-if="item.title !== studying && item.title !== 'Private'" @changeStudying="change(item.title)"></ChangeStudying>
          <el-button size="small" type="primary" plain @click="viewWordBook(item)" class="action-btn">
            浏览
          </el-button>

          <template v-if="item.title === studying && item.title !== 'Private'">
            <el-button size="small" type="primary" @click="$router.push($router.options.routes[4].children[0].path)" class="action-btn">
              背诵
            </el-button>
            <el-button size="small" type="info" plain @click="$router.push($router.options.routes[3].children[0].path)" class="action-btn">
              计划
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 逻辑完全不变
import ChangeStudying from '../small/ChangeStudying'
import $ from 'jquery'
import { getCookie } from '../../util.js'
export default {
  name: 'WordBooks',
  components: { ChangeStudying },
  methods: {
    viewWordBook: function (info) {
      if (info.title !== 'Private') {
        this.$router.push({path: this.$router.options.routes[2].children[1].path, query: {num: info.num, title: info.title}})
      } else {
        this.$router.push({path: this.$router.options.routes[2].children[2].path, query: {num: info.num, title: info.title}})
      }
    },
    change: function (bookTitle) {
      $.ajax({
        type: 'GET',
        url: 'http://localhost:8080/Hello/change_book/' + bookTitle,
        crossDomain: true,
        xhrFields: {
          withCredentials: true
        },
        dataType: 'json',
        data: {},
        success: (result) => {
          if (result.info === 'success') {
            this.studying = bookTitle
          } else {
            alert('改变单词书失败')
          }
        },
        error: function () {
          alert('改变单词书失败')
        }
      })
    }
  },
  data () {
    return {
      booksInfo: [],
      numBooks: 0,
      studying: ''
    }
  },
  mounted: function () {
    $.ajax({
      type: 'GET',
      url: 'http://localhost:8080/Hello/get_books/' + getCookie('username'),
      crossDomain: true,
      xhrFields: {
        withCredentials: true
      },
      dataType: 'json',
      data: {},
      success: (result) => {
        this.numBooks = result.length;
        this.booksInfo = Array(0).concat(result);
        this.studying = getCookie('studying');
      },
      error: function () { alert('单词书加载失败'); }
    })
  }
}
</script>

<style scoped>
.books-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 40px;
  text-align: center;
}
.page-header h2 { font-size: 32px; margin-bottom: 10px; }
.page-header p { color: var(--text-muted); }

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
}

.book-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.book-card.active {
  border: 1px solid var(--accent-color);
  background: rgba(56, 189, 248, 0.1);
}

.book-cover {
  height: 140px;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--accent-color);
  position: relative;
}

.book-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--success-color);
  color: white;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.book-info {
  padding: 20px;
  flex-grow: 1;
}

.book-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--text-main);
}

.book-meta {
  color: var(--text-muted);
  font-size: 14px;
}

.book-actions {
  padding: 15px 20px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
}
</style>
