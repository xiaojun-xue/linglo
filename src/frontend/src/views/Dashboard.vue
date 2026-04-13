<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h2 class="welcome-title">
          早上好，<span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
          <span class="wave">👋</span>
        </h2>
        <p class="welcome-tip">今天是 {{ currentDate }}，继续为团队创造价值吧</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" @click="goCreate('requirement')">
          <el-icon><Plus /></el-icon>
          创建需求
        </el-button>
        <el-button @click="goCreate('project')">
          <el-icon><FolderAdd /></el-icon>
          新建项目
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="(stat, index) in stats"
        :key="stat.key"
        class="stat-card"
        :class="{ 'is-active': activeStat === stat.key }"
        :style="{ animationDelay: `${index * 0.08}s` }"
        @click="handleStatClick(stat)"
      >
        <div class="stat-selected-bar" :style="{ background: stat.color }"></div>
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :size="24" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'neutral'">
          <el-icon v-if="stat.trend > 0"><Top /></el-icon>
          <span>{{ stat.trend > 0 ? `+${stat.trend}` : stat.trend }}%</span>
        </div>
        <div class="stat-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="content-grid">
      <!-- 进行中的项目 -->
      <div class="content-card project-list">
        <div class="card-header">
          <h3 class="card-title">
            <el-icon><FolderOpened /></el-icon>
            进行中的项目
          </h3>
          <el-button type="primary" link @click="$router.push('/projects')">
            查看全部
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="card-body">
          <template v-if="loading">
            <div v-for="i in 3" :key="i" class="skeleton-item">
              <div class="skeleton" style="width: 60px; height: 16px;"></div>
              <div class="skeleton" style="width: 120px; height: 14px; margin-top: 8px;"></div>
              <div class="skeleton" style="width: 100%; height: 6px; margin-top: 12px;"></div>
            </div>
          </template>
          <template v-else-if="projects.length > 0">
            <div
              v-for="project in projects"
              :key="project.id"
              class="project-item"
              @click="$router.push(`/projects/${project.id}`)"
            >
              <div class="project-info">
                <span class="project-name">{{ project.name }}</span>
                <div class="project-meta">
                  <span class="stage-tag" :class="project.stage">
                    {{ stageMap[project.stage] || project.stage }}
                  </span>
                </div>
              </div>
              <div class="project-progress">
                <div class="progress-info">
                  <span class="progress-text">{{ project.progress || 0 }}%</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${project.progress || 0}%` }"></div>
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无进行中的项目" :image-size="80">
            <el-button type="primary" size="small" @click="goCreate('project')">创建第一个项目</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 待办事项 -->
      <div class="content-card todo-list">
        <div class="card-header">
          <h3 class="card-title">
            <el-icon><Bell /></el-icon>
            待我处理
          </h3>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0">
            <el-button type="primary" link @click="$router.push('/tasks?filter=assigned')">
              查看全部
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-badge>
        </div>
        <div class="card-body">
          <template v-if="loading">
            <div v-for="i in 3" :key="i" class="skeleton-item">
              <div class="skeleton" style="width: 80%; height: 16px;"></div>
              <div class="skeleton" style="width: 40%; height: 12px; margin-top: 8px;"></div>
            </div>
          </template>
          <template v-else-if="todos.length > 0">
            <div
              v-for="todo in todos"
              :key="todo.id"
              class="todo-item"
            >
              <el-checkbox v-model="todo.done" @change="handleTodoChange(todo)" />
              <div class="todo-content">
                <span class="todo-title" :class="{ done: todo.done }">{{ todo.title }}</span>
                <span class="todo-type">{{ todo.type }}</span>
              </div>
              <span class="todo-deadline" :class="{ overdue: isOverdue(todo.deadline) }">
                {{ formatDeadline(todo.deadline) }}
              </span>
            </div>
          </template>
          <el-empty v-else description="暂无待处理事项" :image-size="80">
            <template #image>
              <el-icon :size="48" color="#c0c4cc"><CircleCheck /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- Sprint 概览 -->
    <div class="content-card sprint-overview">
      <div class="card-header">
        <h3 class="card-title">
          <el-icon><Grid /></el-icon>
          当前 Sprint
        </h3>
        <el-button type="primary" link @click="$router.push('/sprints')">
          Sprint 管理
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="card-body">
        <template v-if="currentSprint">
          <div class="sprint-info">
            <div class="sprint-name">
              <span class="name">{{ currentSprint.name }}</span>
              <span class="status-tag" :class="currentSprint.status">{{ sprintStatusMap[currentSprint.status] }}</span>
            </div>
            <div class="sprint-date">
              {{ currentSprint.startDate }} ~ {{ currentSprint.endDate }}
            </div>
          </div>
          <div class="sprint-stats">
            <div class="sprint-stat">
              <span class="value">{{ currentSprint.taskCount || 0 }}</span>
              <span class="label">任务数</span>
            </div>
            <div class="sprint-stat">
              <span class="value">{{ currentSprint.completedCount || 0 }}</span>
              <span class="label">已完成</span>
            </div>
            <div class="sprint-stat">
              <span class="value">{{ currentSprint.bugCount || 0 }}</span>
              <span class="label">Bug数</span>
            </div>
          </div>
          <div class="sprint-progress">
            <div class="progress-header">
              <span>Sprint 进度</span>
              <span class="progress-value">{{ currentSprint.progress || 0 }}%</span>
            </div>
            <div class="progress-bar large">
              <div class="progress-fill" :style="{ width: `${currentSprint.progress || 0}%` }"></div>
            </div>
          </div>
        </template>
        <el-empty v-else description="当前无进行中的 Sprint" :image-size="80">
          <el-button type="primary" size="small" @click="$router.push('/sprints')">创建 Sprint</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  FolderOpened, Document, List, Guide, Plus, FolderAdd, Bell,
  ArrowRight, Top, CircleCheck
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const activeStat = ref<string | null>(null)
const stats = ref([
  { key: 'projects', label: '项目总数', value: 0, icon: FolderOpened, color: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)', path: '/projects', trend: 0 },
  { key: 'requirements', label: '需求总数', value: 0, icon: Document, color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.1)', path: '/requirements', trend: 12 },
  { key: 'tasks', label: '任务总数', value: 0, icon: List, color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.1)', path: '/tasks', trend: 5 },
  { key: 'reviews', label: '待评审', value: 0, icon: Guide, color: '#f59e0b', bgColor: 'rgba(245, 158, 11, 0.1)', path: '/reviews', trend: -3 }
])

const projects = ref<any[]>([])
const todos = ref<any[]>([])
const currentSprint = ref<any>(null)
const pendingCount = ref(0)

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日 ${['日', '一', '二', '三', '四', '五', '六'][now.getDay()]}期`
})

const stageMap: Record<string, string> = {
  concept: '概念阶段',
  plan: '计划阶段',
  development: '开发阶段',
  verification: '验证阶段',
  launch: '发布阶段'
}

const sprintStatusMap: Record<string, string> = {
  planning: '规划中',
  active: '进行中',
  completed: '已完成'
}

function isOverdue(date?: string): boolean {
  if (!date) return false
  return new Date(date) < new Date()
}

function formatDeadline(date?: string): string {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = Math.ceil((d.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
  if (diff < 0) return `${Math.abs(diff)}天前`
  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  return `${diff}天后`
}

function handleTodoChange(todo: any) {
  ElMessage.success(todo.done ? '任务已完成' : '任务已恢复')
}

function goToPage(path: string) {
  router.push(path)
}

function handleStatClick(stat: any) {
  activeStat.value = stat.key
  setTimeout(() => {
    router.push(stat.path)
  }, 150)
}

function goCreate(type: string) {
  if (type === 'requirement') {
    router.push('/requirements?action=create')
  } else {
    router.push('/projects?action=create')
  }
}

async function fetchDashboardData() {
  loading.value = true
  try {
    // 加载统计数据
    const [projectsRes, requirementsRes, tasksRes, reviewsRes] = await Promise.all([
      fetch('/api/projects?status=2&page=0&size=100', {
        headers: { Authorization: `Bearer ${userStore.token}` }
      }).then(r => r.json()).catch(() => ({ data: { content: [], totalElements: 0 } })),
      fetch('/api/requirements?page=0&size=100', {
        headers: { Authorization: `Bearer ${userStore.token}` }
      }).then(r => r.json()).catch(() => ({ data: { content: [], totalElements: 0 } })),
      fetch('/api/tasks?page=0&size=100', {
        headers: { Authorization: `Bearer ${userStore.token}` }
      }).then(r => r.json()).catch(() => ({ data: { content: [], totalElements: 0 } })),
      fetch('/api/reviews?status=2&page=0&size=100', {
        headers: { Authorization: `Bearer ${userStore.token}` }
      }).then(r => r.json()).catch(() => ({ data: { content: [], totalElements: 0 } }))
    ])

    stats.value[0].value = projectsRes.data?.totalElements || 0
    stats.value[1].value = requirementsRes.data?.totalElements || 0
    stats.value[2].value = tasksRes.data?.totalElements || 0
    stats.value[3].value = reviewsRes.data?.totalElements || 0

    // 加载项目列表
    if (projectsRes.data?.content?.length > 0) {
      projects.value = projectsRes.data.content.slice(0, 5)
    }

    // 加载待办事项
    if (tasksRes.data?.content?.length > 0) {
      todos.value = tasksRes.data.content.slice(0, 5).map((t: any) => ({
        id: t.id,
        title: t.title,
        type: '任务',
        deadline: t.deadline,
        done: t.status === 'done'
      }))
    }

    // 加载当前 Sprint
    fetch('/api/sprints?status=active&page=0&size=1', {
      headers: { Authorization: `Bearer ${userStore.token}` }
    }).then(r => r.json())
      .then(res => {
        if (res.data?.content?.length > 0) {
          currentSprint.value = res.data.content[0]
        }
      }).catch(() => {})

    pendingCount.value = reviewsRes.data?.totalElements || 0

  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* =========================================
   欢迎区
   ========================================= */

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  animation: fadeInUp 0.5s ease-out;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.welcome-title .user-name {
  color: var(--color-primary);
}

.welcome-title .wave {
  display: inline-block;
  animation: wave 1s ease-in-out infinite;
  margin-left: 4px;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-10deg); }
}

.welcome-tip {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.quick-actions .el-button {
  padding: 12px 20px;
  border-radius: 10px;
}

.quick-actions .el-button .el-icon {
  margin-right: 6px;
}

/* =========================================
   统计卡片
   ========================================= */

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.5s ease-out both;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-light);
}

.stat-card:hover::before {
  opacity: 0.6;
}

/* 选中状态 - 左侧高亮条 */
.stat-selected-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  transform: scaleY(0);
  transform-origin: center;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card.is-active .stat-selected-bar,
.stat-card:hover .stat-selected-bar {
  transform: scaleY(1);
}

/* 选中状态 - 背景高亮 */
.stat-card.is-active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(26, 54, 93, 0.1), var(--shadow-lg);
  transform: translateY(-4px);
}

.stat-card.is-active::before {
  opacity: 1;
}

/* 数字颜色随卡片主题变化 */
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
  display: block;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-value,
.stat-card.is-active .stat-value {
  transform: scale(1.05);
}

/* 箭头指示器 */
.stat-arrow {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%) translateX(8px);
  opacity: 0;
  transition: all 0.3s ease;
  color: var(--color-primary);
}

.stat-card:hover .stat-arrow,
.stat-card.is-active .stat-arrow {
  opacity: 1;
  transform: translateY(-50%) translateX(0);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
  display: block;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  display: block;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 20px;
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.stat-trend.up {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.stat-trend.neutral {
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}

/* =========================================
   内容网格
   ========================================= */

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.content-card {
  background: var(--color-bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  animation: fadeInUp 0.5s ease-out 0.2s both;
}

.sprint-overview {
  animation-delay: 0.3s;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-title .el-icon {
  font-size: 18px;
  color: var(--color-primary);
}

.arrow {
  margin-left: 4px;
  transition: transform 0.2s ease;
}

.content-card:hover .arrow {
  transform: translateX(4px);
}

.card-body {
  padding: 16px 24px 20px;
}

/* =========================================
   项目列表
   ========================================= */

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: all 0.2s ease;
}

.project-item:last-child {
  border-bottom: none;
}

.project-item:hover {
  padding-left: 12px;
  background: rgba(26, 54, 93, 0.02);
  border-radius: 8px;
}

.project-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  display: block;
  margin-bottom: 6px;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stage-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stage-tag.plan { background: rgba(139, 92, 246, 0.1); color: #7c3aed; }
.stage-tag.development { background: rgba(16, 185, 129, 0.1); color: #059669; }
.stage-tag.verification { background: rgba(245, 158, 11, 0.1); color: #d97706; }
.stage-tag.launch { background: rgba(236, 72, 153, 0.1); color: #db2777; }

.project-progress {
  width: 140px;
  text-align: right;
}

.progress-text {
  font-size: 12px;
  color: var(--color-text-secondary);
  display: block;
  margin-bottom: 6px;
}

.progress-bar {
  height: 6px;
  background: var(--color-border-light);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar.large {
  height: 8px;
  border-radius: 4px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  border-radius: 3px;
  transition: width 0.6s ease-out;
}

/* =========================================
   待办事项
   ========================================= */

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 14px;
  color: var(--color-text-primary);
  display: block;
  transition: all 0.2s ease;
}

.todo-title.done {
  text-decoration: line-through;
  color: var(--color-text-muted);
}

.todo-type {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
  display: block;
}

.todo-deadline {
  font-size: 12px;
  color: var(--color-text-secondary);
  padding: 4px 8px;
  background: var(--color-border-light);
  border-radius: 4px;
  flex-shrink: 0;
}

.todo-deadline.overdue {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

/* =========================================
   Sprint 概览
   ========================================= */

.sprint-info {
  margin-bottom: 20px;
}

.sprint-name {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.sprint-name .name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.status-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.status-tag.active {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.status-tag.completed {
  background: rgba(139, 92, 246, 0.1);
  color: #7c3aed;
}

.sprint-date {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.sprint-stats {
  display: flex;
  gap: 32px;
  padding: 16px 0;
  border-top: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 20px;
}

.sprint-stat {
  text-align: center;
}

.sprint-stat .value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  display: block;
}

.sprint-stat .label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.sprint-progress .progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.sprint-progress .progress-value {
  font-weight: 600;
  color: var(--color-primary);
}

/* =========================================
   空状态
   ========================================= */

:deep(.el-empty__description) {
  margin-top: 12px;
}

.skeleton-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.skeleton-item:last-child {
  border-bottom: none;
}

/* =========================================
   响应式
   ========================================= */

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
