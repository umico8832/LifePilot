import { createRouter, createWebHistory } from 'vue-router'

import AuthView from '@/views/auth/AuthView.vue'
import HomeView from '@/views/HomeView.vue'
import SpaceView from '@/views/space/SpaceView.vue'
import FinanceView from '@/views/finance/FinanceView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/auth',
      name: 'auth',
      component: AuthView,
    },
    {
      path: '/spaces',
      name: 'spaces',
      component: SpaceView,
    },
    {
      path: '/finance',
      name: 'finance',
      component: FinanceView,
    },
  ],
})

export default router
