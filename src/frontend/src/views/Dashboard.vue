<template>
  <div class="dashboard">
    <!-- 欢迎区域 + 视图切换 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h2 class="welcome-title">
          {{ greeting }}，<span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
        </h2>
        <p class="welcome-tip">今天是 {{ currentDate }}，继续为团队创造价值吧</p>
      </div>
      <div class="view-actions">
        <el-radio-group v-model="viewMode" size="default" class="view-toggle">
          <el-radio-button value="detail">
            <el-icon><List /></el-icon>
            详情视图
          </el-radio-button>
          <el-radio-button value="kanban">
            <el-icon><Grid /></el-icon>
            看板视图
          </el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
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
        :style="{ animationDelay: `${index * 0.08}s` }"
      >
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :size="22" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- ========== 详情视图 ========== -->
    <div v-if="viewMode === 'detail'" class="detail-view">
      <div class="detail-layout">
        <!-- 左侧：项目列表 -->
        <div class="project-list-panel">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><FolderOpened /></el-icon>
              项目列表
              <span class="count-badge">{{ projectStore.projects.length }}</span>
            </h3>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目..."
              :prefix-icon="Search"
              clearable
              size="small"
              class="search-input"
            />
          </div>
          <div class="project-list-body">
            <template v-if="projectStore.loading">
              <div v-for="i in 4" :key="i" class="skeleton-item">
                <div class="skeleton" style="width: 60%; height: 16px;"></div>
                <div class="skeleton" style="width: 40%; height: 12px; margin-top: 6px;"></div>
              </div>
            </template>
            <template v-else-if="filteredProjects.length > 0">
              <div
                v-for="project in filteredProjects"
                :key="project.id"
                class="project-list-item"
                :class="{ 'is-selected': projectStore.selectedProjectId === project.id }"
                @click="handleSelectProject(project)"
              >
                <div class="item-main">
                  <span class="item-name">{{ project.name }}</span>
                  <span class="item-stage" :class="'stage-' + project.stage">
                    {{ stageNameMap[project.stage] || '未知' }}
                  </span>
                </div>
                <div class="item-meta">
                  <span class="item-no">{{ project.projectNo }}</span>
                  <span class="item-progress">{{ project.progress || 0 }}%</span>
                </div>
                <div class="item-progress-bar">
                  <div class="fill" :style="{ width: `${project.progress || 0}%` }"></div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无项目" :image-size="60">
              <el-button type="primary" size="small" @click="showCreateDialog">创建项目</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 右侧：选中项目详情 -->
        <div class="project-detail-panel">
          <template v-if="projectStore.selectedProject">
            <!-- IPD 流程进度条 -->
            <div class="ipd-flow-section">
              <h3 class="section-title">IPD 流程进度</h3>
              <div class="ipd-flow">
                <div
                  v-for="(stage, index) in ipdStages"
                  :key="stage.value"
                  class="ipd-stage"
                  :class="{
                    'is-current': projectStore.selectedProject.stage === stage.value,
                    'is-done': projectStore.selectedProject.stage > stage.value,
                    'is-future': projectStore.selectedProject.stage < stage.value
                  }"
                >
                  <div class="stage-node">
                    <div class="node-circle">
                      <el-icon v-if="projectStore.selectedProject.stage > stage.value"><Check /></el-icon>
                      <span v-else>{{ index + 1 }}</span>
                    </div>
                    <span class="stage-label">{{ stage.label }}</span>
                  </div>
                  <div v-if="stage.review" class="stage-review" :class="getReviewStatus(stage.reviewType)">
                    {{ stage.review }}
                    <el-icon v-if="getReviewStatus(stage.reviewType) === 'passed'"><CircleCheck /></el-icon>
                    <el-icon v-else-if="getReviewStatus(stage.reviewType) === 'pending'"><Clock /></el-icon>
                  </div>
                  <div v-if="index < ipdStages.length - 1" class="stage-connector" :class="{ done: projectStore.selectedProject.stage > stage.value }"></div>
                </div>
              </div>
              <div class="flow-actions">
                <el-button
                  v-if="projectStore.selectedProject.stage < 5"
                  type="primary"
                  size="small"
                  @click="handleAdvanceStage(false)"
                  :loading="advanceLoading"
                >
                  推进到下一阶段
                </el-button>
                <el-button
                  v-if="projectStore.selectedProject.stage < 5 && isAdmin"
                  type="warning"
                  size="small"
                  plain
                  @click="handleAdvanceStage(true)"
                  :loading="advanceLoading"
                >
                  管理员强制推进
                </el-button>
                <el-tag v-if="projectStore.selectedProject.stage >= 5" type="success" size="large">已发布</el-tag>
              </div>
            </div>

            <!-- 项目信息 + 概要统计 -->
            <div class="project-summary">
              <div class="summary-header">
                <div class="project-title-row">
                  <h2 class="project-title">{{ projectStore.selectedProject.name }}</h2>
                  <div class="project-actions">
                    <el-button text @click="showEditDialog(projectStore.selectedProject)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-button>
                    <el-button text type="danger" @click="handleDeleteProject(projectStore.selectedProject)">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </div>
                </div>
                <p class="project-desc">{{ projectStore.selectedProject.description || '暂无描述' }}</p>
                <div class="project-info-tags">
                  <el-tag size="small">{{ projectStore.selectedProject.projectNo }}</el-tag>
                  <el-tag size="small" :type="statusTypeMap[projectStore.selectedProject.status] || 'info'">
                    {{ statusNameMap[projectStore.selectedProject.status] || '未知' }}
                  </el-tag>
                </div>
              </div>

              <!-- 概要统计卡片 -->
              <div class="summary-stats">
                <div class="summary-stat-card">
                  <el-icon :size="20" color="#8b5cf6"><Document /></el-icon>
                  <div class="stat-detail">
                    <span class="stat-num">{{ projectStats.totalRequirements || 0 }}</span>
                    <span class="stat-name">需求</span>
                  </div>
                </div>
                <div class="summary-stat-card">
                  <el-icon :size="20" color="#10b981"><Tickets /></el-icon>
                  <div class="stat-detail">
                    <span class="stat-num">{{ projectStats.totalTasks || 0 }}</span>
                    <span class="stat-name">任务</span>
                  </div>
                </div>
                <div class="summary-stat-card">
                  <el-icon :size="20" color="#3b82f6"><Finished /></el-icon>
                  <div class="stat-detail">
                    <span class="stat-num">{{ projectStats.doneTasks || 0 }}</span>
                    <span class="stat-name">已完成</span>
                  </div>
                </div>
                <div class="summary-stat-card">
                  <el-icon :size="20" color="#f59e0b"><PieChart /></el-icon>
                  <div class="stat-detail">
                    <span class="stat-num">{{ projectStats.progress || 0 }}%</span>
                    <span class="stat-name">进度</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 待处理任务 -->
            <div class="pending-section">
              <div class="section-header">
                <h3 class="section-title">
                  <el-icon><Bell /></el-icon>
                  近期任务
                </h3>
                <el-button type="primary" link @click="$router.push('/tasks')">
                  查看全部 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
              <div class="task-list">
                <template v-if="recentTasks.length > 0">
                  <div v-for="task in recentTasks" :key="task.id" class="task-item">
                    <span class="task-status-dot" :class="'status-' + task.status"></span>
                    <span class="task-title">{{ task.title }}</span>
                    <span class="task-assignee">{{ task.assigneeName || '-' }}</span>
                  </div>
                </template>
                <el-empty v-else description="暂无任务" :image-size="40" />
              </div>
            </div>
          </template>

          <!-- 未选中项目 -->
          <div v-else class="no-project-selected">
            <el-icon :size="64" color="#c0c4cc"><FolderOpened /></el-icon>
            <h3>请从左侧选择一个项目</h3>
            <p>选中后可查看项目详情、IPD流程进度和相关统计</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 看板视图 ========== -->
    <div v-else class="kanban-view">
      <div class="kanban-board">
        <div
          v-for="stage in ipdStages"
          :key="stage.value"
          class="kanban-column"
        >
          <div class="column-header" :class="'stage-' + stage.value">
            <span class="column-title">{{ stage.label }}</span>
            <span class="column-count">{{ getStageProjects(stage.value).length }}</span>
          </div>
          <div class="column-body">
            <div
              v-for="project in getStageProjects(stage.value)"
              :key="project.id"
              class="kanban-card"
              :class="{ 'is-selected': projectStore.selectedProjectId === project.id }"
              @click="handleSelectProject(project)"
            >
              <div class="kanban-card-name">{{ project.name }}</div>
              <div class="kanban-card-meta">
                <span class="kanban-card-no">{{ project.projectNo }}</span>
              </div>
              <div class="kanban-card-progress">
                <div class="mini-progress-bar">
                  <div class="fill" :style="{ width: `${project.progress || 0}%` }"></div>
                </div>
                <span class="progress-text">{{ project.progress || 0 }}%</span>
              </div>
              <div v-if="stage.review" class="kanban-card-review" :class="getReviewStatusForProject(project.id, stage.reviewType)">
                {{ stage.review }}
                <span v-if="getReviewStatusForProject(project.id, stage.reviewType) === 'passed'">✓</span>
              </div>
            </div>
            <div v-if="getStageProjects(stage.value).length === 0" class="kanban-empty">
              暂无项目
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑项目' : '新建项目'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useProjectStore } from '@/stores/project'
import {
  getProjectList, createProject as apiCreateProject, updateProject as apiUpdateProject,
  deleteProject as apiDeleteProject, getProjectStatistics, advanceProjectStage
} from '@/api/project'
import {
  FolderOpened, Document, Tickets, List, Grid, Plus, Search, Edit, Delete,
  Bell, ArrowRight, Check, CircleCheck, Clock, Finished, PieChart, Guide
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const projectStore = useProjectStore()

// ── 视图状态 ──
const viewMode = ref<'detail' | 'kanban'>('detail')
const searchKeyword = ref('')
const advanceLoading = ref(false)

// ── 统计 ──
const globalStats = reactive({ projects: 0, requirements: 0, tasks: 0, reviews: 0 })
const projectStats = ref<any>({})
const recentTasks = ref<any[]>([])
const reviewsCache = ref<Record<string, any[]>>({})

// ── 对话框 ──
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentProjectId = ref<number | null>(null)
const form = reactive({ name: '', description: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

// ── IPD阶段定义 ──
const ipdStages = [
  { value: 1, label: '概念', review: 'CDCP', reviewType: 1 },
  { value: 2, label: '计划', review: 'PDCP', reviewType: 2 },
  { value: 3, label: '开发', review: 'TR4', reviewType: 3 },
  { value: 4, label: '验证', review: 'ADCP', reviewType: 4 },
  { value: 5, label: '发布', review: null, reviewType: null }
]

const stageNameMap: Record<number, string> = {
  1: '概念阶段', 2: '计划阶段', 3: '开发阶段', 4: '验证阶段', 5: '已发布'
}

const statusNameMap: Record<number, string> = {
  1: '未开始', 2: '进行中', 3: '暂停', 4: '已完成', 5: '已归档'
}

const statusTypeMap: Record<number, string> = {
  1: 'info', 2: '', 3: 'warning', 4: 'success', 5: 'info'
}

// ── 计算属性 ──
const stats = computed(() => [
  { key: 'projects', label: '项目总数', value: globalStats.projects, icon: FolderOpened, color: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)' },
  { key: 'requirements', label: '需求总数', value: globalStats.requirements, icon: Document, color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.1)' },
  { key: 'tasks', label: '任务总数', value: globalStats.tasks, icon: Tickets, color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.1)' },
  { key: 'reviews', label: '待评审', value: globalStats.reviews, icon: Guide, color: '#f59e0b', bgColor: 'rgba(245, 158, 11, 0.1)' }
])

const filteredProjects = computed(() => {
  if (!searchKeyword.value) return projectStore.projects
  const kw = searchKeyword.value.toLowerCase()
  return projectStore.projects.filter((p: any) =>
    p.name?.toLowerCase().includes(kw) || p.projectNo?.toLowerCase().includes(kw)
  )
})

const isAdmin = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.some((r: any) => r.code === 'ADMIN' || r === 'ADMIN')
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日 星期${'日一二三四五六'[now.getDay()]}`
})

// ── 项目操作 ──
function handleSelectProject(project: any) {
  projectStore.selectProject(project.id)
}

function getStageProjects(stage: number) {
  return projectStore.projects.filter((p: any) => p.stage === stage)
}

function getReviewStatus(reviewType: number | null): string {
  if (!reviewType || !projectStore.selectedProjectId) return 'none'
  const key = `${projectStore.selectedProjectId}-${reviewType}`
  const reviews = reviewsCache.value[key]
  if (!reviews) return 'none'
  const passed = reviews.some((r: any) => r.decision === 1 || r.decision === 2)
  return passed ? 'passed' : 'pending'
}

function getReviewStatusForProject(projectId: number, reviewType: number | null): string {
  if (!reviewType) return 'none'
  const key = `${projectId}-${reviewType}`
  const reviews = reviewsCache.value[key]
  if (!reviews) return 'none'
  return reviews.some((r: any) => r.decision === 1 || r.decision === 2) ? 'passed' : 'pending'
}

async function handleAdvanceStage(force: boolean) {
  if (!projectStore.selectedProjectId) return
  advanceLoading.value = true
  try {
    const res = await advanceProjectStage(projectStore.selectedProjectId, force)
    if (res.code === 200 && res.data?.advanced) {
      const msg = res.data.skipped ? '已强制推进阶段（跳过评审）' : '阶段推进成功'
      ElMessage.success(msg)
      await projectStore.fetchProjects()
      await loadProjectDetail()
    } else {
      ElMessage.warning(res.data?.message || '无法推进阶段')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    advanceLoading.value = false
  }
}

// ── CRUD ──
function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', description: '' })
  currentProjectId.value = null
  dialogVisible.value = true
}

function showEditDialog(project: any) {
  isEdit.value = true
  currentProjectId.value = project.id
  Object.assign(form, { name: project.name, description: project.description || '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentProjectId.value) {
        const res = await apiUpdateProject(currentProjectId.value, form)
        if (res.code !== 200) throw new Error(res.message || '更新失败')
        ElMessage.success('项目已更新')
      } else {
        const res = await apiCreateProject(form)
        if (res.code !== 200) throw new Error(res.message || '创建失败')
        ElMessage.success('项目已创建')
      }
      dialogVisible.value = false
      await projectStore.fetchProjects()
      await fetchGlobalStats()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDeleteProject(project: any) {
  ElMessageBox.confirm('确定要删除该项目吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      const res = await apiDeleteProject(project.id)
      if (res.code !== 200) throw new Error(res.message)
      ElMessage.success('项目已删除')
      if (projectStore.selectedProjectId === project.id) {
        projectStore.clearSelection()
      }
      await projectStore.fetchProjects()
      await fetchGlobalStats()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  }).catch(() => {})
}

// ── 数据加载 ──
async function fetchGlobalStats() {
  try {
    const [projectsRes, requirementsRes, tasksRes, reviewsRes] = await Promise.all([
      fetch('/api/projects?page=0&size=1', { headers: { Authorization: `Bearer ${userStore.token}` } }).then(r => r.json()).catch(() => ({ data: { totalElements: 0 } })),
      fetch('/api/requirements?page=0&size=1', { headers: { Authorization: `Bearer ${userStore.token}` } }).then(r => r.json()).catch(() => ({ data: { totalElements: 0 } })),
      fetch('/api/tasks?page=0&size=1', { headers: { Authorization: `Bearer ${userStore.token}` } }).then(r => r.json()).catch(() => ({ data: { totalElements: 0 } })),
      fetch('/api/reviews?status=2&page=0&size=1', { headers: { Authorization: `Bearer ${userStore.token}` } }).then(r => r.json()).catch(() => ({ data: { totalElements: 0 } }))
    ])
    globalStats.projects = projectsRes.data?.totalElements || 0
    globalStats.requirements = requirementsRes.data?.totalElements || 0
    globalStats.tasks = tasksRes.data?.totalElements || 0
    globalStats.reviews = reviewsRes.data?.totalElements || 0
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

async function loadProjectDetail() {
  const id = projectStore.selectedProjectId
  if (!id) {
    projectStats.value = {}
    recentTasks.value = []
    return
  }
  try {
    // 加载项目统计
    const statsRes = await getProjectStatistics(id)
    if (statsRes.code === 200) {
      projectStats.value = statsRes.data
    }
    // 加载近期任务
    const tasksRes = await fetch(`/api/tasks?projectId=${id}&page=0&size=5`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    }).then(r => r.json()).catch(() => ({ data: { content: [] } }))
    recentTasks.value = tasksRes.data?.content || []

    // 加载该项目的评审状态（针对当前阶段及之前阶段）
    const reviewsRes = await fetch(`/api/reviews?projectId=${id}&page=0&size=100`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    }).then(r => r.json()).catch(() => ({ data: { content: [] } }))
    const reviews = reviewsRes.data?.content || []
    // 按 projectId-type 缓存
    for (const type of [1, 2, 3, 4]) {
      const key = `${id}-${type}`
      reviewsCache.value[key] = reviews.filter((r: any) => r.type === type)
    }
  } catch (e) {
    console.error('加载项目详情失败:', e)
  }
}

// ── 监听选中项目变化 ──
watch(() => projectStore.selectedProjectId, () => {
  loadProjectDetail()
})

onMounted(async () => {
  await projectStore.fetchProjects()
  await fetchGlobalStats()
  if (projectStore.selectedProjectId) {
    loadProjectDetail()
  }
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
  margin-bottom: 24px;
  animation: fadeInUp 0.5s ease-out;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.welcome-title .user-name {
  color: var(--color-primary);
}

.welcome-tip {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.view-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-toggle :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* =========================================
   统计卡片
   ========================================= */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  animation: fadeInUp 0.5s ease-out both;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  display: block;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
  display: block;
}

/* =========================================
   详情视图
   ========================================= */
.detail-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  min-height: 500px;
}

/* 项目列表面板 */
.project-list-panel {
  background: var(--color-bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 280px);
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px 0;
}

.panel-title .el-icon {
  color: var(--color-primary);
}

.count-badge {
  font-size: 11px;
  background: var(--color-primary);
  color: #fff;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 500;
}

.search-input {
  width: 100%;
}

.project-list-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
}

.project-list-item {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
  border: 2px solid transparent;
}

.project-list-item:hover {
  background: rgba(26, 54, 93, 0.04);
}

.project-list-item.is-selected {
  background: rgba(26, 54, 93, 0.08);
  border-color: var(--color-primary);
}

.item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-stage {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  flex-shrink: 0;
  margin-left: 8px;
}

.item-stage.stage-1 { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.item-stage.stage-2 { background: rgba(139, 92, 246, 0.1); color: #7c3aed; }
.item-stage.stage-3 { background: rgba(16, 185, 129, 0.1); color: #059669; }
.item-stage.stage-4 { background: rgba(245, 158, 11, 0.1); color: #d97706; }
.item-stage.stage-5 { background: rgba(236, 72, 153, 0.1); color: #db2777; }

.item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.item-progress-bar {
  height: 3px;
  background: var(--color-border-light);
  border-radius: 2px;
  overflow: hidden;
}

.item-progress-bar .fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  border-radius: 2px;
  transition: width 0.4s ease;
}

/* 项目详情面板 */
.project-detail-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.no-project-selected {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: var(--color-text-secondary);
  text-align: center;
}

.no-project-selected h3 {
  margin: 16px 0 8px;
  color: var(--color-text-primary);
}

/* =========================================
   IPD 流程进度条
   ========================================= */
.ipd-flow-section {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ipd-flow {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  position: relative;
  margin-bottom: 20px;
}

.ipd-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.stage-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;
}

.node-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  border: 3px solid var(--color-border-light);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  transition: all 0.3s ease;
}

.ipd-stage.is-done .node-circle {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.ipd-stage.is-current .node-circle {
  border-color: var(--color-primary);
  color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(26, 54, 93, 0.15);
}

.stage-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.ipd-stage.is-done .stage-label,
.ipd-stage.is-current .stage-label {
  color: var(--color-text-primary);
  font-weight: 600;
}

.stage-review {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 3px;
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}

.stage-review.passed {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.stage-review.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.stage-connector {
  position: absolute;
  top: 20px;
  left: calc(50% + 24px);
  right: calc(-50% + 24px);
  height: 3px;
  background: var(--color-border-light);
  z-index: 0;
}

.stage-connector.done {
  background: var(--color-primary);
}

.flow-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* =========================================
   项目概要
   ========================================= */
.project-summary {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
}

.project-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.project-actions {
  display: flex;
  gap: 4px;
}

.project-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 8px 0 12px;
}

.project-info-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.summary-stat-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: rgba(26, 54, 93, 0.03);
  border-radius: 10px;
}

.stat-detail {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
}

.stat-name {
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}

/* =========================================
   待处理任务
   ========================================= */
.pending-section {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header .section-title {
  margin: 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.task-item:last-child {
  border-bottom: none;
}

.task-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #94a3b8;
}

.task-status-dot.status-1 { background: #94a3b8; }
.task-status-dot.status-2 { background: #3b82f6; }
.task-status-dot.status-3 { background: #10b981; }
.task-status-dot.status-4 { background: #6b7280; }

.task-title {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-assignee {
  font-size: 12px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

/* =========================================
   看板视图
   ========================================= */
.kanban-board {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  min-height: 400px;
}

.kanban-column {
  background: rgba(26, 54, 93, 0.03);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.column-header {
  padding: 14px 16px;
  font-weight: 600;
  font-size: 14px;
  border-radius: 12px 12px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-header.stage-1 { background: rgba(59, 130, 246, 0.1); color: #2563eb; }
.column-header.stage-2 { background: rgba(139, 92, 246, 0.1); color: #7c3aed; }
.column-header.stage-3 { background: rgba(16, 185, 129, 0.1); color: #059669; }
.column-header.stage-4 { background: rgba(245, 158, 11, 0.1); color: #d97706; }
.column-header.stage-5 { background: rgba(236, 72, 153, 0.1); color: #db2777; }

.column-count {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.6);
  padding: 2px 8px;
  border-radius: 10px;
}

.column-body {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kanban-card {
  background: var(--color-bg-card);
  border-radius: 10px;
  padding: 14px;
  box-shadow: var(--shadow-sm);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.kanban-card:hover {
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,0.1));
  transform: translateY(-1px);
}

.kanban-card.is-selected {
  border-color: var(--color-primary);
}

.kanban-card-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}

.kanban-card-meta {
  margin-bottom: 8px;
}

.kanban-card-no {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.kanban-card-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-progress-bar {
  flex: 1;
  height: 4px;
  background: var(--color-border-light);
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-bar .fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  border-radius: 2px;
}

.progress-text {
  font-size: 11px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.kanban-card-review {
  margin-top: 8px;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}

.kanban-card-review.passed {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.kanban-empty {
  text-align: center;
  padding: 20px 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* =========================================
   骨架屏
   ========================================= */
.skeleton-item {
  padding: 12px;
  border-radius: 8px;
}

.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* =========================================
   响应式
   ========================================= */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .detail-layout {
    grid-template-columns: 280px 1fr;
  }
  .kanban-board {
    grid-template-columns: repeat(3, 1fr);
  }
  .summary-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .kanban-board {
    grid-template-columns: 1fr;
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
