<template>
  <div class="projects-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-tabs">
          <span
            v-for="tab in tabs"
            :key="tab.value"
            class="tab-item"
            :class="{ active: currentTab === tab.value }"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
            <span class="tab-count" v-if="tab.count > 0">{{ tab.count }}</span>
          </span>
        </div>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建项目
        </el-button>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="projects-content">
      <template v-if="loading">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="skeleton" style="width: 40%; height: 20px;"></div>
          <div class="skeleton" style="width: 70%; height: 14px; margin-top: 12px;"></div>
          <div class="skeleton" style="width: 100%; height: 8px; margin-top: 16px;"></div>
        </div>
      </template>

      <template v-else-if="projects.length > 0">
        <div class="projects-grid">
          <div
            v-for="project in projects"
            :key="project.id"
            class="project-card"
            @click="goDetail(project)"
          >
            <div class="card-header">
              <div class="project-icon" :style="{ background: getProjectColor(project.stage) }">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, project)">
                <el-button text class="more-btn" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>编辑
                    </el-dropdown-item>
                    <el-dropdown-item command="archive" divided>
                      <el-icon><Box /></el-icon>归档
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" style="color: #dc2626;">
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <div class="card-body">
              <h3 class="project-name">{{ project.name }}</h3>
              <p class="project-desc">{{ project.description || '暂无描述' }}</p>

              <div class="project-meta">
                <span class="stage-tag" :class="project.stage">
                  {{ stageMap[project.stage] || project.stage }}
                </span>
                <span class="project-date">{{ formatDate(project.createdAt) }}</span>
              </div>
            </div>

            <div class="card-footer">
              <div class="progress-section">
                <div class="progress-header">
                  <span>进度</span>
                  <span class="progress-value">{{ project.progress || 0 }}%</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${project.progress || 0}%` }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <span class="pagination-info">
            共 {{ total }} 个项目
          </span>
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="60" cy="60" r="56" fill="url(#emptyGrad)" opacity="0.1"/>
            <path d="M45 35h30l15 15H30l15-15z" fill="url(#emptyGrad)" opacity="0.6"/>
            <path d="M30 50v30h60V50H30zm15 15h30v15H45V65z" fill="url(#emptyGrad)"/>
            <rect x="50" y="72" width="20" height="3" rx="1.5" fill="url(#emptyGrad)" opacity="0.4"/>
            <rect x="55" y="78" width="10" height="3" rx="1.5" fill="url(#emptyGrad)" opacity="0.4"/>
            <defs>
              <linearGradient id="emptyGrad" x1="0" y1="0" x2="120" y2="120">
                <stop offset="0%" stop-color="#1a365d"/>
                <stop offset="100%" stop-color="#d69e2e"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h3 class="empty-title">暂无项目</h3>
        <p class="empty-desc">创建第一个项目，开启您的研发管理之旅</p>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建项目
        </el-button>
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
        class="project-form"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="IPD 阶段" prop="stage">
          <el-select v-model="form.stage" placeholder="请选择项目阶段" style="width: 100%">
            <el-option v-for="(label, value) in stageMap" :key="value" :label="label" :value="value" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  Search, Plus, FolderOpened, MoreFilled, Edit, Delete, Box
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const projects = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')
const currentTab = ref('all')

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentProjectId = ref<number | null>(null)

const form = reactive({
  name: '',
  description: '',
  stage: 'concept'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  stage: [{ required: true, message: '请选择项目阶段', trigger: 'change' }]
}

const tabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '概念阶段', value: 'concept', count: 0 },
  { label: '计划阶段', value: 'plan', count: 0 },
  { label: '开发阶段', value: 'development', count: 0 },
  { label: '验证阶段', value: 'verification', count: 0 },
  { label: '已发布', value: 'launch', count: 0 }
])

const stageMap: Record<string, string> = {
  concept: '概念阶段',
  plan: '计划阶段',
  development: '开发阶段',
  verification: '验证阶段',
  launch: '已发布'
}

const stageColors: Record<string, string> = {
  concept: 'rgba(59, 130, 246, 0.15)',
  plan: 'rgba(139, 92, 246, 0.15)',
  development: 'rgba(16, 185, 129, 0.15)',
  verification: 'rgba(245, 158, 11, 0.15)',
  launch: 'rgba(236, 72, 153, 0.15)'
}

function getProjectColor(stage: string): string {
  return stageColors[stage] || stageColors.concept
}

function formatDate(date?: string): string {
  if (!date) return ''
  return date.split('T')[0]
}

function handleSearch() {
  currentPage.value = 1
  fetchProjects()
}

function handleTabChange(tab: string) {
  currentTab.value = tab
  currentPage.value = 1
  fetchProjects()
}

function handlePageChange() {
  fetchProjects()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', description: '', stage: 'concept' })
  currentProjectId.value = null
  dialogVisible.value = true
}

function handleCommand(command: string, project: any) {
  if (command === 'edit') {
    isEdit.value = true
    currentProjectId.value = project.id
    Object.assign(form, {
      name: project.name,
      description: project.description,
      stage: project.stage
    })
    dialogVisible.value = true
  } else if (command === 'archive') {
    ElMessageBox.confirm('确定要归档该项目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      ElMessage.success('项目已归档')
      fetchProjects()
    }).catch(() => {})
  } else if (command === 'delete') {
    ElMessageBox.confirm('确定要删除该项目吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }).then(() => {
      deleteProject(project.id)
    }).catch(() => {})
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value && currentProjectId.value) {
          await updateProject(currentProjectId.value)
        } else {
          await createProject()
        }
        dialogVisible.value = false
        ElMessage.success(isEdit.value ? '项目已更新' : '项目已创建')
        fetchProjects()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

async function fetchProjects() {
  loading.value = true
  try {
    const stageFilterMap: Record<string, string> = {
      concept: '1',
      plan: '2',
      development: '3',
      verification: '3',
      launch: '4'
    }
    const params = new URLSearchParams({
      page: String(currentPage.value - 1),
      size: String(pageSize.value)
    })
    if (currentTab.value !== 'all' && stageFilterMap[currentTab.value]) {
      params.set('stage', stageFilterMap[currentTab.value])
    }
    if (searchKeyword.value) {
      params.set('keyword', searchKeyword.value)
    }

    const res = await fetch(`/api/projects?${params}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    }).then(r => r.json())

    projects.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

const stageToInt: Record<string, number> = {
  concept: 1,
  plan: 2,
  development: 3,
  verification: 3,
  launch: 4
}

function buildProjectPayload() {
  return {
    name: form.name,
    description: form.description,
    stage: stageToInt[form.stage] ?? 1
  }
}

async function createProject() {
  const res = await fetch('/api/projects', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${userStore.token}`
    },
    body: JSON.stringify(buildProjectPayload())
  })
  if (!res.ok) throw new Error('创建失败')
}

async function updateProject(id: number) {
  const res = await fetch(`/api/projects/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${userStore.token}`
    },
    body: JSON.stringify(buildProjectPayload())
  })
  if (!res.ok) throw new Error('更新失败')
}

async function deleteProject(id: number) {
  const res = await fetch(`/api/projects/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  if (!res.ok) throw new Error('删除失败')
  ElMessage.success('项目已删除')
  fetchProjects()
}

function goDetail(project: any) {
  router.push(`/projects/${project.id}`)
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.projects-page {
  animation: fadeInUp 0.4s ease-out;
}

/* =========================================
   页面头部
   ========================================= */

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--color-bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.header-tabs {
  display: flex;
  gap: 8px;
}

.tab-item {
  padding: 8px 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-item:hover {
  background: rgba(26, 54, 93, 0.05);
  color: var(--color-text-primary);
}

.tab-item.active {
  background: var(--color-primary);
  color: #fff;
}

.tab-count {
  font-size: 12px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.tab-item:not(.active) .tab-count {
  background: rgba(26, 54, 93, 0.08);
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 260px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px !important;
}

/* =========================================
   项目网格
   ========================================= */

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.project-card {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease-out both;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.project-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.project-icon .el-icon {
  font-size: 24px;
}

.more-btn {
  padding: 4px !important;
  color: var(--color-text-muted) !important;
}

.more-btn:hover {
  background: rgba(26, 54, 93, 0.05) !important;
  color: var(--color-text-primary) !important;
}

.card-body {
  margin-bottom: 16px;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.project-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stage-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stage-tag.plan { background: rgba(139, 92, 246, 0.1); color: #7c3aed; }
.stage-tag.development { background: rgba(16, 185, 129, 0.1); color: #059669; }
.stage-tag.verification { background: rgba(245, 158, 11, 0.1); color: #d97706; }
.stage-tag.launch { background: rgba(236, 72, 153, 0.1); color: #db2777; }

.project-date {
  font-size: 12px;
  color: var(--color-text-muted);
}

.card-footer {
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.progress-value {
  font-weight: 600;
  color: var(--color-primary);
}

/* =========================================
   分页
   ========================================= */

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding: 16px 20px;
  background: var(--color-bg-card);
  border-radius: 12px;
}

.pagination-info {
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* =========================================
   空状态
   ========================================= */

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: var(--color-bg-card);
  border-radius: 16px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}

/* =========================================
   表单
   ========================================= */

.project-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--color-text-primary);
}

.project-form :deep(.el-input__wrapper),
.project-form :deep(.el-textarea__inner) {
  border-radius: 10px !important;
}

/* =========================================
   骨架屏
   ========================================= */

.skeleton-card {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
}
</style>
