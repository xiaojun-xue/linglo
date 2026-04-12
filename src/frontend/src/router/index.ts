import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '工作台', requiresAuth: true } },
      { path: 'requirements', name: 'Requirements', component: () => import('@/views/Requirements.vue'), meta: { title: '需求管理', requiresAuth: true } },
      { path: 'projects', name: 'Projects', component: () => import('@/views/Projects.vue'), meta: { title: '项目管理', requiresAuth: true } },
      { path: 'tasks', name: 'Tasks', component: () => import('@/views/Tasks.vue'), meta: { title: '任务管理', requiresAuth: true } },
      { path: 'sprints', name: 'Sprints', component: () => import('@/views/Sprints.vue'), meta: { title: 'Sprint管理', requiresAuth: true } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/Reviews.vue'), meta: { title: '评审管理', requiresAuth: true } },
      { path: 'qa', name: 'QaManagement', component: () => import('@/views/QaManagement.vue'), meta: { title: '测试管理', requiresAuth: true } },
      { path: 'docs', name: 'Documents', component: () => import('@/views/Documents.vue'), meta: { title: '文档管理', requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || ''} 玲珑`.trim()
  if (to.meta.requiresAuth !== false) {
    const userStore = useUserStore()
    if (!userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
