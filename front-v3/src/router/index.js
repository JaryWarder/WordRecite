import { createRouter, createWebHistory } from 'vue-router'
import Index from '../views/Index.vue'
import HomeIndex from '../views/HomeIndex.vue'
import UserInfo from '../views/UserInfo.vue'
import WordBooks from '../views/WordBooks.vue'
import Plan from '../views/Plan.vue'
import Start from '../views/Start.vue'
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
        { path: 'review', component: HomeIndex, meta: { requireAuth: true } },
        { path: 'progress', component: HomeIndex, meta: { requireAuth: true } },
        { path: 'testStart', component: HomeIndex, meta: { requireAuth: true } }
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
