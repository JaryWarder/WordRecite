import { createRouter, createWebHistory } from 'vue-router'
import Index from '../views/Index.vue'
import HomeIndex from '../views/HomeIndex.vue'
import UserInfo from '../views/UserInfo.vue'
import WordBooks from '../views/WordBooks.vue'
import Plan from '../views/Plan.vue'
import Start from '../views/Start.vue'
import Review from '../views/Review.vue'
import Progress from '../views/Progress.vue'
import TestStart from '../views/TestStart.vue'
import Test from '../views/Test.vue'
import WordCard from '../views/WordCard.vue'
import Private from '../views/Private.vue'
import DailyFootprint from '../views/DailyFootprint.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Index,
      children: [
        { path: '', component: HomeIndex },
        { path: 'userInfo', component: UserInfo, meta: { requireAuth: true } },
        { path: 'wordBooks', component: WordBooks, meta: { requireAuth: true } },
        { path: 'plan', component: Plan, meta: { requireAuth: true } },
        { path: 'start', component: Start, meta: { requireAuth: true } },
        { path: 'review', component: Review, meta: { requireAuth: true } },
        { path: 'dailyFootprint', component: DailyFootprint, meta: { requireAuth: true } },
        { path: 'private', component: Private, meta: { requireAuth: true } },
        { path: 'progress', component: Progress, meta: { requireAuth: true } },
        { path: 'testStart', component: TestStart, meta: { requireAuth: true } },
        { path: 'test', component: Test, meta: { requireAuth: true } },
        { path: 'wordCard', component: WordCard, meta: { requireAuth: true } }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.restore()
  const requireAuth = Boolean(to.meta && to.meta.requireAuth)
  if (requireAuth && !userStore.isLogin) {
    next('/')
    return
  }
  next()
})

export default router
