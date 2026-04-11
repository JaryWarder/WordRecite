/**
 * HomeIndex.vue - 首页展示
 * 风格：Hero Banner + 特性网格
 */
<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="logo-wrapper">
        <i class="el-icon-notebook-2 hero-icon"></i>
      </div>
      <h1 class="main-title">WordRecite</h1>
      <p class="subtitle">打破记忆边界，构建你的词汇宇宙</p>
      <div class="hero-description">
        融合艾宾浩斯遗忘曲线与现代交互体验，为您打造最沉浸的英语学习环境。
      </div>
    </div>

    <div class="features-grid">
      <div class="feature-card">
        <div class="icon-box"><i class="el-icon-collection"></i></div>
        <h3>海量词书</h3>
        <p>覆盖四六级、考研、托福、GRE等主流词库，支持自定义生词本。</p>
      </div>
      <div class="feature-card">
        <div class="icon-box"><i class="el-icon-data-line"></i></div>
        <h3>科学记忆</h3>
        <p>智能算法安排复习计划，精准捕捉记忆临界点。</p>
      </div>
      <div class="feature-card">
        <div class="icon-box"><i class="el-icon-trophy"></i></div>
        <h3>多维测试</h3>
        <p>提供多种测试模式，实时检验学习成果，巩固记忆。</p>
      </div>
      <div class="feature-card">
        <div class="icon-box"><i class="el-icon-pie-chart"></i></div>
        <h3>可视化数据</h3>
        <p>全方位的数据追踪，让每一次进步都清晰可见。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeIndex'
}
</script>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.hero-section {
  margin-bottom: 80px;
  animation: fadeInDown 1s ease-out;
}

.logo-wrapper {
  width: 100px;
  height: 100px;
  background: var(--accent-gradient);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 10px 30px rgba(56, 189, 248, 0.4);
}

.hero-icon {
  font-size: 50px;
  color: #fff;
}

.main-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 10px;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 20px;
  color: var(--accent-color);
  margin-bottom: 20px;
  font-weight: 500;
}

.hero-description {
  max-width: 600px;
  margin: 0 auto;
  color: var(--text-muted);
  line-height: 1.6;
}

/* 特性网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 30px;
}

.feature-card {
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid var(--glass-border);
  padding: 30px 20px;
  border-radius: 20px;
  transition: transform 0.3s ease, background 0.3s ease;
  backdrop-filter: blur(10px);
}

.feature-card:hover {
  transform: translateY(-10px);
  background: rgba(30, 41, 59, 0.8);
  border-color: var(--accent-color);
}

.icon-box {
  width: 50px;
  height: 50px;
  background: rgba(56, 189, 248, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: var(--accent-color);
  font-size: 24px;
}

.feature-card h3 {
  margin: 0 0 10px;
  color: var(--text-main);
}

.feature-card p {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
