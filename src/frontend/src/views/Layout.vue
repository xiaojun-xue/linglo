<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img src="/logo.svg" alt="logo" v-if="!isCollapse" />
        <span v-if="!isCollapse">玲珑</span>
        <el-icon v-else><Grid /></el-icon>
      </div>
      
      <el-menu :default-active="activeMenu" :collapse="isCollapse" router class="sidebar-menu"
        background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><template #title>工作台</template></el-menu-item>
        <el-menu-item index="/requirements"><el-icon><Document /></el-icon><template #title>需求管理</template></el-menu-item>
        <el-menu-item index="/projects"><el-icon><FolderOpened /></el-icon><template #title>项目管理</template></el-menu-item>
        <el-menu-item index="/tasks"><el-icon><List /></el-icon><template #title>任务管理</template></el-menu-item>
        <el-menu-item index="/sprints"><el-icon><Timer /></el-icon><template #title>Sprint管理</template></el-menu-item>
        <el-menu-item index="/reviews"><el-icon><CircleCheck /></el-icon><template #title>评审管理</template></el-menu-item>
        <el-menu-item index="/qa"><el-icon><Bug /></el-icon><template #title>测试管理</template></el-menu-item>
        <el-menu-item index="/docs"><el-icon><Files /></el-icon><template #title>文档管理</template></el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        </div>
        <div class="header-right">
          <el-popover placement="bottom-end" :width="360" trigger="click">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="!unreadCount" class="notification-badge">
                <el-icon class="header-icon"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header"><span>通知中心</span><el-button type="primary" link size="small" @click="markAllRead" v-if="unreadCount">全部已读</el-button></div>
              <div class="notification-list" v-loading="notifLoading">
                <div v-if="notifications.length === 0" class="notification-empty">暂无通知</div>
                <div v-for="item in notifications" :key="item.id" class="notification-item" :class="{ unread: !item.isRead }" @click="viewNotification(item)">
                  <div class="notification-type">{{ getNotifTypeName(item.type) }}</div>
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
                </div>
              </div>
            </div>
          </el-popover>
          
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="User" />
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead as apiMarkAllRead } from '@/api/notification'
import { User } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const notifications = ref<any[]>([])
const unreadCount = ref(0)
const notifLoading = ref(false)

const activeMenu = computed(() => route.path)

async function fetchNotifications() {
  notifLoading.value = true
  try {
    const res = await getNotificationList({ page: 0, size: 10 })
    if (res.code === 200) notifications.value = res.data.content || []
  } catch (e) { console.error(e) }
  finally { notifLoading.value = false }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) unreadCount.value = res.data.count
  } catch (e) { console.error(e) }
}

async function markAllRead() {
  try { await apiMarkAllRead(); unreadCount.value = 0; notifications.value.forEach(n => n.isRead = true) } catch (e) { console.error(e) }
}

async function viewNotification(item: any) {
  if (!item.isRead) {
    try { await markAsRead(item.id); item.isRead = true; unreadCount.value = Math.max(0, unreadCount.value - 1) } catch (e) { console.error(e) }
  }
  const routes: Record<string, string> = { TASK: '/tasks', REVIEW: '/reviews', REQUIREMENT: '/requirements', PROJECT: '/projects' }
  if (routes[item.businessType]) router.push(routes[item.businessType])
}

function getNotifTypeName(type: number) { return { 1: '系统', 2: '任务', 3: '评审', 4: '需求', 5: '项目' }[type] || '通知' }
function formatTime(time: string) { return dayjs(time).fromNow() }

function handleCommand(command: string) {
  if (command === 'logout') { userStore.logout(); router.push('/login') }
  else if (command === 'profile') router.push('/profile')
}

let pollTimer: number

onMounted(() => {
  userStore.fetchUserInfo()
  fetchNotifications()
  fetchUnreadCount()
  pollTimer = window.setInterval(() => fetchUnreadCount(), 30000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.sidebar { background-color: #304156; transition: width 0.3s; overflow: hidden; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; font-size: 18px; font-weight: bold; border-bottom: 1px solid #3d4a5c; }
.logo img { width: 32px; height: 32px; }
.sidebar-menu { border-right: none; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.collapse-btn { font-size: 20px; cursor: pointer; color: #666; }
.header-right { display: flex; align-items: center; gap: 16px; }
.header-icon { font-size: 20px; cursor: pointer; color: #666; }
.notification-badge { cursor: pointer; }
.notification-panel { }
.notification-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #eee; margin-bottom: 8px; font-weight: 600; }
.notification-list { max-height: 400px; overflow-y: auto; }
.notification-empty { text-align: center; color: #999; padding: 20px; }
.notification-item { padding: 10px; border-radius: 4px; cursor: pointer; transition: background 0.2s; }
.notification-item:hover { background: #f5f7fa; }
.notification-item.unread { background: #ecf5ff; }
.notification-type { font-size: 11px; color: #409eff; margin-bottom: 4px; }
.notification-title { font-size: 13px; color: #333; margin-bottom: 4px; }
.notification-time { font-size: 11px; color: #999; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { color: #333; white-space: nowrap; }

/* 修复主内容区布局 */
.main-content { background-color: #f5f7fa; padding: 20px; overflow: auto; }

/* 确保 el-container 正确布局 */
:deep(.el-container) { height: 100vh; }
:deep(.el-aside) { overflow: hidden; }
</style>
