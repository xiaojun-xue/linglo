<template>
  <div class="dashboard">
    <h2 class="page-title">工作台</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card blue">
          <div class="stat-icon"><FolderOpened /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalProjects }}</div>
            <div class="stat-label">项目总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-icon"><Document /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalRequirements }}</div>
            <div class="stat-label">需求总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card orange">
          <div class="stat-icon"><List /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalTasks }}</div>
            <div class="stat-label">任务总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card purple">
          <div class="stat-icon"><CircleCheck /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingReviews }}</div>
            <div class="stat-label">待评审</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 项目列表 & 任务看板 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="content-card">
          <div class="card-header">
            <h3>进行中的项目</h3>
            <el-button type="primary" link @click="$router.push('/projects')">查看全部</el-button>
          </div>
          <el-table :data="projects" style="width: 100%" v-loading="loading" @row-click="selectProject" highlight-current-row>
            <el-table-column prop="projectNo" label="项目编号" width="120" />
            <el-table-column prop="name" label="项目名称" />
            <el-table-column prop="stage" label="阶段" width="100">
              <template #default="{ row }">
                <el-tag :type="getStageType(row.stage)">{{ getStageName(row.stage) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.progress || 0" :color="progressColor" />
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 任务看板 -->
        <div class="content-card mt-16" v-if="selectedProjectId">
          <TaskBoard :project-id="selectedProjectId" />
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="content-card">
          <div class="card-header">
            <h3>快捷操作</h3>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/requirements')">
              <div class="action-icon blue"><Document /></div>
              <span>创建需求</span>
            </div>
            <div class="action-item" @click="$router.push('/projects')">
              <div class="action-icon green"><FolderAdd /></div>
              <span>新建项目</span>
            </div>
            <div class="action-item" @click="$router.push('/tasks')">
              <div class="action-icon orange"><List /></div>
              <span>创建任务</span>
            </div>
            <div class="action-item" @click="$router.push('/sprints')">
              <div class="action-icon purple"><Timer /></div>
              <span>Sprint管理</span>
            </div>
          </div>
        </div>
        
        <div class="content-card mt-16">
          <div class="card-header">
            <h3>待我评审</h3>
            <el-button type="primary" link @click="$router.push('/reviews')">查看全部</el-button>
          </div>
          <div v-if="pendingReviews.length === 0" class="empty-text">暂无待评审项</div>
          <div v-else class="review-list">
            <div v-for="r in pendingReviews" :key="r.id" class="review-item" @click="$router.push('/reviews')">
              <span class="review-type">{{ getReviewTypeName(r.type) }}</span>
              <span class="review-title">{{ r.title }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProjectList } from '@/api/project'
import { getReviewList } from '@/api/review'
import TaskBoard from '@/components/TaskBoard.vue'

const loading = ref(false)
const projects = ref<any[]>([])
const selectedProjectId = ref<number | undefined>()
const pendingReviews = ref<any[]>([])

const stats = reactive({
  totalProjects: 0,
  totalRequirements: 0,
  totalTasks: 0,
  pendingReviews: 0
})

const progressColor = [
  { color: '#67C23A', percentage: 30 },
  { color: '#E6A23C', percentage: 70 },
  { color: '#F56C6C', percentage: 100 }
]

function getStageName(stage: number) {
  const names: Record<number, string> = { 1: '概念', 2: '计划', 3: '开发', 4: '发布' }
  return names[stage] || '未知'
}

function getStageType(stage: number) {
  const types: Record<number, string> = { 1: '', 2: 'warning', 3: 'primary', 4: 'success' }
  return types[stage] || ''
}

function getReviewTypeName(type: number) {
  return { 1: 'CDCP', 2: 'PDCP', 3: 'TR4', 4: 'ADCP' }[type] || '评审'
}

function selectProject(row: any) {
  selectedProjectId.value = row.id
}

async function fetchProjects() {
  loading.value = true
  try {
    const res = await getProjectList({ status: 2, page: 0, size: 5 })
    if (res.code === 200) {
      projects.value = res.data.content || []
      stats.totalProjects = res.data.totalElements || 0
      if (projects.value.length && !selectedProjectId.value) {
        selectedProjectId.value = projects.value[0].id
      }
    }
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchPendingReviews() {
  try {
    const res = await getReviewList({ status: 2, page: 0, size: 5 })
    if (res.code === 200) {
      pendingReviews.value = res.data.content || []
      stats.pendingReviews = res.data.totalElements || 0
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchProjects()
  fetchPendingReviews()
})
</script>

<style scoped>
.dashboard { padding: 20px; }
.page-title { font-size: 24px; margin-bottom: 20px; color: #333; }
.stat-cards { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; border-radius: 8px; color: #fff; }
.stat-card.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-card.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-card.purple { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon { font-size: 40px; opacity: 0.8; margin-right: 15px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { font-size: 14px; opacity: 0.9; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { font-size: 16px; color: #333; margin: 0; }
.mt-16 { margin-top: 16px; }
.quick-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.action-item { display: flex; flex-direction: column; align-items: center; padding: 20px; background: #f5f7fa; border-radius: 8px; cursor: pointer; transition: all 0.3s; min-width: 0; }
.action-item:hover { background: #e4e7ed; transform: translateY(-2px); }
.action-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; margin-bottom: 10px; flex-shrink: 0; }
.action-icon.blue { background: #409eff; }
.action-icon.green { background: #67c23a; }
.action-icon.orange { background: #e6a23c; }
.action-icon.purple { background: #955cff; }
.action-item span { font-size: 14px; color: #666; white-space: nowrap; }
.empty-text { text-align: center; color: #999; padding: 20px; }
.review-list { }
.review-item { display: flex; align-items: center; gap: 10px; padding: 10px; border-bottom: 1px solid #eee; cursor: pointer; }
.review-item:hover { background: #f5f7fa; }
.review-type { font-size: 12px; padding: 2px 6px; background: #409eff; color: #fff; border-radius: 3px; }
.review-title { font-size: 13px; color: #333; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
