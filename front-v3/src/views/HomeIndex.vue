<template>
  <div class="home-page">
    <!-- 背景光晕 -->
    <div class="halo halo-1"></div>
    <div class="halo halo-2"></div>

    <!-- Hero Section -->
    <section class="hero fade-in-up">
      <div class="hero-content">
        <div class="hero-text">
          <div class="badge">基于艾宾浩斯遗忘曲线</div>
          <h1 class="hero-title">Master English,<br /><span class="gradient-text">Scientifically.</span></h1>
          <p class="hero-sub">基于艾宾浩斯遗忘曲线，助力每一份努力都有迹可循。</p>
          <el-button class="cta-btn" @click="router.push('/start')">
            立即开始学习
            <el-icon class="btn-icon"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="hero-image">
          <img src="../assets/hero.png" alt="hero" />
        </div>
      </div>
    </section>

    <!-- Quick Glance -->
    <section class="section fade-in-up" style="animation-delay: 0.1s">
      <el-card class="glass-card glance-card">
        <!-- 已登录 -->
        <template v-if="isLogin">
          <div class="glance-logged">
            <div class="glance-item">
              <div class="glance-label">欢迎回来</div>
              <div class="glance-val accent">{{ username }}</div>
            </div>
            <div class="glance-divider"></div>
            <div class="glance-item">
              <div class="glance-label">当前词书</div>
              <div class="glance-val">{{ studyingBook || '未选择' }}</div>
            </div>
            <div class="glance-divider"></div>
            <div class="glance-item">
              <div class="glance-label">今日已学</div>
              <div class="glance-val success">继续加油 💪</div>
            </div>
          </div>
        </template>
        <!-- 游客态 -->
        <template v-else>
          <div class="glance-guest">
            <div class="glance-stat" v-for="stat in guestStats" :key="stat.label">
              <div class="stat-num">{{ stat.num }}</div>
              <div class="stat-desc">{{ stat.label }}</div>
            </div>
          </div>
        </template>
      </el-card>
    </section>

    <!-- Feature Highlights -->
    <section class="section fade-in-up" style="animation-delay: 0.2s">
      <h2 class="section-title">核心特性</h2>
      <div class="features-grid">
        <el-card class="glass-card feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon">{{ f.icon }}</div>
          <div class="feature-title">{{ f.title }}</div>
          <div class="feature-desc">{{ f.desc }}</div>
        </el-card>
      </div>
    </section>

    <!-- Daily Inspiration -->
    <section class="section fade-in-up" style="animation-delay: 0.3s">
      <el-card class="glass-card inspiration-card">
        <div class="inspiration-label">每日寄语</div>
        <blockquote class="inspiration-quote">"{{ quote.en }}"</blockquote>
        <p class="inspiration-trans">—— {{ quote.zh }}</p>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getUserInfo } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const { isLogin, username } = storeToRefs(userStore)

const studyingBook = ref('')

const guestStats = [
  { num: '10,000+', label: '词库收录' },
  { num: 'AI', label: '智能学情分析' },
  { num: '科学', label: '艾宾浩斯复习算法' },
]

const features = [
  { icon: '✦', title: 'AI 深度诊断', desc: '基于大模型的专属学情分析，精准定位薄弱点，给出个性化建议。' },
  { icon: '🔔', title: '科学记忆提醒', desc: '系统根据遗忘曲线自动触发复习通知，让记忆效率最大化。' },
  { icon: '📝', title: '多维词汇测试', desc: '快速拉取整卷测试，多角度检验掌握程度，查漏补缺一步到位。' },
]

const quote = {
  en: 'The secret of getting ahead is getting started.',
  zh: '成功的秘诀，在于开始行动。—— Mark Twain',
}

onMounted(async () => {
  userStore.restore()
  if (isLogin.value && username.value && username.value !== 'Guest') {
    try {
      const res = await getUserInfo(username.value)
      if (res.code === 200 && res.data?.studying && res.data.studying !== 'none') {
        studyingBook.value = res.data.studying
      }
    } catch (_) {}
  }
})
</script>

<style scoped>
.home-page {
  position: relative;
  max-width: 960px;
  margin: 0 auto;
  padding: 40px 24px 80px;
  overflow: hidden;
}

/* 背景光晕 */
.halo {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
  animation: breathe 6s ease-in-out infinite;
}
.halo-1 {
  width: 500px; height: 500px;
  top: -100px; left: -100px;
  background: radial-gradient(circle, rgba(56,189,248,0.12) 0%, transparent 70%);
}
.halo-2 {
  width: 400px; height: 400px;
  bottom: 0; right: -80px;
  background: radial-gradient(circle, rgba(99,102,241,0.1) 0%, transparent 70%);
  animation-delay: 3s;
}
@keyframes breathe {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}

/* 通用动画 */
.fade-in-up {
  animation: fadeInUp 0.6s ease both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.section {
  position: relative;
  z-index: 1;
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 16px;
}

/* 毛玻璃卡片 */
.glass-card {
  border-radius: 20px !important;
  background: var(--glass-bg) !important;
  border: 1px solid var(--glass-border) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  box-shadow: var(--glass-shadow) !important;
}

/* Hero */
.hero {
  position: relative;
  z-index: 1;
  margin-bottom: 48px;
}
.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}
.hero-text {
  flex: 1;
}
.badge {
  display: inline-block;
  font-size: 12px;
  color: var(--accent-color);
  border: 1px solid rgba(56,189,248,0.4);
  border-radius: 20px;
  padding: 3px 12px;
  margin-bottom: 16px;
  background: rgba(56,189,248,0.08);
}
.hero-title {
  font-size: clamp(28px, 5vw, 48px);
  font-weight: 800;
  line-height: 1.2;
  color: var(--text-main);
  margin: 0 0 16px;
}
.gradient-text {
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-sub {
  font-size: 15px;
  color: var(--text-muted);
  margin: 0 0 28px;
  line-height: 1.7;
}
.cta-btn {
  background: var(--accent-gradient) !important;
  border: none !important;
  color: #fff !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  padding: 12px 28px !important;
  border-radius: 12px !important;
  height: auto !important;
  transition: box-shadow 0.3s, transform 0.2s !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.cta-btn:hover {
  box-shadow: 0 0 24px rgba(56,189,248,0.5) !important;
  transform: translateY(-2px) !important;
}
.btn-icon { font-size: 16px; }
.hero-image {
  flex-shrink: 0;
  width: 280px;
}
.hero-image img {
  width: 100%;
  border-radius: 20px;
  opacity: 0.9;
}
@media (max-width: 680px) {
  .hero-image { display: none; }
}

/* Quick Glance */
.glance-card { padding: 8px 0; }
.glance-logged {
  display: flex;
  align-items: center;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;
  padding: 8px 0;
}
.glance-item { text-align: center; }
.glance-label { font-size: 13px; color: var(--text-muted); margin-bottom: 6px; }
.glance-val { font-size: 20px; font-weight: 700; color: var(--text-main); }
.glance-val.accent { color: var(--accent-color); }
.glance-val.success { color: var(--success-color); }
.glance-divider {
  width: 1px; height: 40px;
  background: var(--glass-border);
}
.glance-guest {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;
  padding: 8px 0;
}
.glance-stat { text-align: center; }
.stat-num {
  font-size: 26px;
  font-weight: 800;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-desc { font-size: 13px; color: var(--text-muted); margin-top: 4px; }

/* Features */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
@media (max-width: 680px) {
  .features-grid { grid-template-columns: 1fr; }
}
.feature-card { text-align: center; padding: 8px 0; }
.feature-icon { font-size: 28px; margin-bottom: 10px; }
.feature-title { font-size: 15px; font-weight: 700; color: var(--text-main); margin-bottom: 8px; }
.feature-desc { font-size: 13px; color: var(--text-muted); line-height: 1.6; }

/* Inspiration */
.inspiration-card { text-align: center; padding: 8px 0; }
.inspiration-label {
  font-size: 12px;
  color: var(--accent-color);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 16px;
}
.inspiration-quote {
  font-size: 18px;
  font-style: italic;
  color: var(--text-main);
  margin: 0 0 12px;
  line-height: 1.7;
}
.inspiration-trans {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
</style>
