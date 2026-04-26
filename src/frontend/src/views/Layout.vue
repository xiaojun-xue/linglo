<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <!-- Logo 区域 -->
      <div class="sidebar-header">
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="48" height="48" rx="12" fill="url(#sidebarLogoGrad)"/>
              <path d="M14 16h8l4 8-4 8h-8l4-8-4-8z" fill="#fff" opacity="0.9"/>
              <path d="M26 16h8l4 8-4 8h-8l4-8-4-8z" fill="#fff" opacity="0.6"/>
              <defs>
                <linearGradient id="sidebarLogoGrad" x1="0" y1="0" x2="48" y2="48">
                  <stop offset="0%" stop-color="#d69e2e"/>
                  <stop offset="100%" stop-color="#b7791f"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <transition name="fade-slide">
            <span v-if="!isCollapse" class="logo-text">玲珑</span>
          </transition>
        </div>
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="18">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapse"
        :collapse-transition="false"
        class="sidebar-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-menu-item index="/requirements">
          <el-icon><Document /></el-icon>
          <template #title>需求管理</template>
        </el-menu-item>

        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <template #title>任务管理</template>
        </el-menu-item>

        <el-menu-item index="/sprints">
          <el-icon><Grid /></el-icon>
          <template #title>Sprint管理</template>
        </el-menu-item>

        <el-menu-item index="/reviews">
          <el-icon><Guide /></el-icon>
          <template #title>评审管理</template>
        </el-menu-item>

        <el-menu-item index="/qa">
          <el-icon><Passed /></el-icon>
          <template #title>测试管理</template>
        </el-menu-item>

        <el-menu-item index="/documents">
          <el-icon><Reading /></el-icon>
          <template #title>文档管理</template>
        </el-menu-item>

        <!-- 系统管理分组（仅管理员可见） -->
        <el-sub-menu v-if="isAdmin" index="/system" class="admin-menu-group">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/users">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-divider class="menu-divider" />
      </el-menu>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <transition name="fade-slide">
              <div v-if="!isCollapse" class="user-detail">
                <span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
                <span class="user-role">{{ isAdmin ? '管理员' : userRoleLabel }}</span>
              </div>
            </transition>
            <transition name="fade-slide">
              <el-icon v-if="!isCollapse" class="user-arrow"><ArrowDown /></el-icon>
            </transition>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人设置
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="main-header">
        <div class="header-left">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <div v-if="projectStore.hasProject && currentRoute !== '/'" class="project-indicator">
            <el-icon><FolderOpened /></el-icon>
            <span class="indicator-name">{{ projectStore.projectName }}</span>
            <el-button text size="small" @click="$router.push('/')">切换</el-button>
          </div>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-button :icon="Bell" circle class="header-btn" @click="goNotifications" />
          </el-badge>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useProjectStore } from '@/stores/project'
import {
  HomeFilled, Document, FolderOpened, List, Grid, Guide, Finished, Reading,
  User, Bell, Fold, Expand, ArrowDown, SwitchButton, Setting, UserFilled, CircleCheck as Passed
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const projectStore = useProjectStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

const currentRoute = computed(() => route.path)

const pageTitleMap: Record<string, string> = {
  '/': '工作台',
  '/requirements': '需求管理',
  '/tasks': '任务管理',
  '/sprints': 'Sprint 管理',
  '/reviews': '评审管理',
  '/qa': '测试管理',
  '/documents': '文档管理',
  '/profile': '个人设置',
  '/users': '用户管理'
}

const pageTitle = computed(() => pageTitleMap[currentRoute.value] || '玲珑')

const isAdmin = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.some((r: any) => r.roleCode === 'ADMIN' || r === 'ADMIN' || r === 'ROLE_ADMIN')
})

const roleNameMap: Record<string, string> = {
  ADMIN: '管理员',
  PRODUCT_MANAGER: '产品经理',
  PROJECT_MANAGER: '项目经理',
  TECH_LEAD: '技术负责人',
  DEVELOPER: '开发者',
  QA_MANAGER: '测试经理',
  QA: '测试人员',
  OPS: '运维人员',
  EXECUTIVE: '高层管理',
  VIEWER: '访客'
}

const userRoleLabel = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (!roles.length) return '普通用户'
  const firstRole = roles[0]
  const code = firstRole.roleCode || firstRole
  return roleNameMap[code] || String(code)
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function goNotifications() {
  router.push('/notifications')
}

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logoutAction()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}

onMounted(() => {
  // 初始化项目列表
  projectStore.fetchProjects()
  // 加载未读通知数
  userStore.fetchUnreadCount?.().then((count: number) => {
    unreadCount.value = count
  }).catch(() => {})
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

/* =========================================
   侧边栏
   ========================================= */

.sidebar {
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
}

/* Logo 区域 */
.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 8px rgba(214, 158, 46, 0.3));
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 3px;
  white-space: nowrap;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

/* 菜单 */
.sidebar-menu {
  flex: 1;
  border-right: none !important;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 8px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 0;
  border-radius: 10px;
  padding-left: 16px !important;
  transition: all 0.2s ease;
  position: relative;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  background: rgba(214, 158, 46, 0.2) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: var(--color-accent);
  border-radius: 0 3px 3px 0;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding-left: 22px !important;
  justify-content: center;
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin-right: 0;
}

.menu-divider {
  border-color: rgba(255, 255, 255, 0.08) !important;
  margin: 16px 12px !important;
}

/* 管理员菜单组 */
.admin-menu-group :deep(.el-sub-menu__title) {
  color: rgba(214, 158, 46, 0.9) !important;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.admin-menu-group :deep(.el-sub-menu__title .el-icon) {
  color: #d69e2e !important;
}

/* 用户信息 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
  min-width: 0;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.08);
}

.user-avatar {
  background: rgba(214, 158, 46, 0.3) !important;
  border: 2px solid rgba(214, 158, 46, 0.5);
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.user-arrow {
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

/* =========================================
   主内容区
   ========================================= */

.main-container {
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  overflow: hidden;
}

.main-header {
  height: 64px !important;
  padding: 0 24px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border-light);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.project-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 16px;
  padding: 4px 12px;
  background: rgba(26, 54, 93, 0.06);
  border-radius: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.project-indicator .el-icon {
  color: var(--color-primary);
  font-size: 14px;
}

.indicator-name {
  font-weight: 500;
  color: var(--color-text-primary);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn {
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border) !important;
  background: transparent !important;
  transition: all 0.2s ease;
}

.header-btn:hover {
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
  background: rgba(26, 54, 93, 0.05) !important;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--color-bg);
}

/* =========================================
   过渡动画
   ========================================= */

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

.page-enter-active {
  animation: fadeInUp 0.3s ease-out;
}

.page-leave-active {
  animation: fadeInUp 0.15s ease-in reverse;
}

/* =========================================
   下拉菜单
   ========================================= */

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}
</style>
