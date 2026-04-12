<template>
  <div class="sprints-page">
    <div class="page-header">
      <h2>Sprint 管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 创建 Sprint
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="所属项�?>
          <el-select v-model="selectedProject" placeholder="选择项目" @change="fetchSprints" style="width:250px">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状�?>
          <el-select v-model="statusFilter" placeholder="全部" @change="fetchSprints" clearable>
            <el-option label="规划�? :value="1" />
            <el-option label="进行�? :value="2" />
            <el-option label="已完�? :value="3" />
            <el-option label="已关�? :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card class="content-card">
          <el-table :data="filteredSprints" v-loading="loading" stripe>
            <el-table-column prop="sprintNo" label="编号" width="120" />
            <el-table-column prop="name" label="Sprint名称" min-width="150" />
            <el-table-column prop="status" label="状�? width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日�? width="110" />
            <el-table-column prop="endDate" label="结束日期" width="110" />
            <el-table-column prop="totalPoint" label="总故事点" width="100">
              <template #default="{ row }">{{ row.totalPoint || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" type="success" link size="small" @click="handleStart(row)">开�?/el-button>
                <el-button v-if="row.status === 2" type="warning" link size="small" @click="handleComplete(row)">完成</el-button>
                <el-button v-if="row.status === 3" type="info" link size="small" @click="handleClose(row)">关闭</el-button>
                <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="10">
        <el-card v-if="selectedProject">
          <template #header>
            <span>项目信息</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="项目名称">{{ selectedProjectInfo?.name }}</el-descriptions-item>
            <el-descriptions-item label="项目类型">{{ selectedProjectInfo?.type }}</el-descriptions-item>
            <el-descriptions-item label="IPD阶段">
              <el-tag size="small" :type="getStageTag(selectedProjectInfo?.stage)">
                {{ getStageName(selectedProjectInfo?.stage) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="进度">
              <el-progress :percentage="selectedProjectInfo?.progress || 0" :stroke-width="10" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <el-card class="mt-16" v-if="currentSprint">
          <template #header>
            <span>当前 Sprint 详情</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="Sprint名称">{{ currentSprint.name }}</el-descriptions-item>
            <el-descriptions-item label="目标">{{ currentSprint.goal || '-' }}</el-descriptions-item>
            <el-descriptions-item label="时间范围">{{ currentSprint.startDate }} �?{{ currentSprint.endDate }}</el-descriptions-item>
            <el-descriptions-item label="总故事点">{{ currentSprint.totalPoint || '-' }}</el-descriptions-item>
            <el-descriptions-item label="已完成故事点">{{ currentSprint.completedPoint || '-' }}</el-descriptions-item>
          </el-descriptions>
          <BurndownChart :project-id="selectedProject" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑Sprint' : '创建Sprint'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="Sprint名称" prop="name">
          <el-input v-model="form.name" placeholder="如：Sprint 1" />
        </el-form-item>
        <el-form-item label="Sprint目标" prop="goal">
          <el-input v-model="form.goal" type="textarea" :rows="3" placeholder="请输入Sprint目标" />
        </el-form-item>
        <el-form-item label="开始日�? prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSprintList, createSprint, updateSprint, startSprint, completeSprint, closeSprint } from '@/api/sprint'
import { getProjectList } from '@/api/project'
import BurndownChart from '@/components/BurndownChart.vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const projects = ref<any[]>([])
const sprints = ref<any[]>([])
const selectedProject = ref<number | undefined>()
const statusFilter = ref<number | undefined>()
const currentId = ref<number | null>(null)

const form = reactive<any>({ name: '', goal: '', startDate: '', endDate: '' })

const rules = {
  name: [{ required: true, message: '请输入Sprint名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日�?, trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const selectedProjectInfo = computed(() => projects.value.find(p => p.id === selectedProject.value))
const currentSprint = computed(() => sprints.value.find(s => s.status === 2))
const filteredSprints = computed(() => sprints.value.filter(s => !statusFilter.value || s.status === statusFilter.value))

function getStatusName(status: number) {
  return { 1: '规划�?, 2: '进行�?, 3: '已完�?, 4: '已关�? }[status] || '未知'
}

function getStatusTag(status: number) {
  return { 1: 'info', 2: 'primary', 3: 'success', 4: '' }[status] || 'info'
}

function getStageName(stage: number) {
  return { 1: '概念', 2: '计划', 3: '开�?, 4: '发布' }[stage] || '未知'
}

function getStageTag(stage: number) {
  return { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success' }[stage] || 'info'
}

async function fetchProjects() {
  try {
    const res = await getProjectList({ page: 0, size: 100 })
    if (res.code === 200) {
      projects.value = res.data.content || []
      if (projects.value.length) selectedProject.value = projects.value[0].id
    }
  } catch (e) { console.error(e) }
}

async function fetchSprints() {
  if (!selectedProject.value) return
  loading.value = true
  try {
    const res = await getSprintList(selectedProject.value)
    if (res.code === 200) sprints.value = res.data || []
  } catch (e: any) { ElMessage.error(e.message || '获取Sprint失败') }
  finally { loading.value = false }
}

function showCreateDialog() {
  if (!selectedProject.value) { ElMessage.warning('请先选择项目'); return }
  isEdit.value = false
  Object.assign(form, { name: '', goal: '', startDate: '', endDate: '' })
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, { name: row.name, goal: row.goal, startDate: row.startDate, endDate: row.endDate })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = { ...form, projectId: selectedProject.value }
        if (isEdit.value && currentId.value) {
          await updateSprint(currentId.value, data)
          ElMessage.success('更新成功')
        } else {
          await createSprint(data)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchSprints()
      } catch (e: any) { ElMessage.error(e.message || '操作失败') }
      finally { submitting.value = false }
    }
  })
}

async function handleStart(row: any) {
  try {
    await startSprint(row.id)
    ElMessage.success('Sprint已开�?)
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleComplete(row: any) {
  try {
    await completeSprint(row.id)
    ElMessage.success('Sprint已完�?)
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleClose(row: any) {
  try {
    await closeSprint(row.id)
    ElMessage.success('Sprint已关�?)
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

onMounted(() => {
  fetchProjects().then(() => fetchSprints())
})
</script>

<style scoped>
.sprints-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; margin: 0; }
.filter-card { margin-bottom: 16px; }
.content-card { }
.mt-16 { margin-top: 16px; }
</style>
