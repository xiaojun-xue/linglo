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
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部" @change="fetchSprints" clearable>
            <el-option label="规划中" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已关闭" :value="4" />
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
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="110" />
            <el-table-column prop="endDate" label="结束日期" width="110" />
            <el-table-column prop="totalPoint" label="总故事点" width="100">
              <template #default="{ row }">{{ row.totalPoint || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" type="success" link size="small" @click="handleStart(row)">开始</el-button>
                <el-button v-if="row.status === 2" type="warning" link size="small" @click="handleComplete(row)">完成</el-button>
                <el-button v-if="row.status === 3" type="info" link size="small" @click="handleClose(row)">关闭</el-button>
                <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="10">
        <el-card v-if="projectStore.selectedProject">
          <template #header>
            <span>项目信息</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="项目名称">{{ projectStore.selectedProject?.name }}</el-descriptions-item>
            <el-descriptions-item label="IPD阶段">
              <el-tag size="small" :type="getStageTag(projectStore.selectedProject?.stage)">
                {{ getStageName(projectStore.selectedProject?.stage) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="进度">
              <el-progress :percentage="projectStore.selectedProject?.progress || 0" :stroke-width="10" />
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
            <el-descriptions-item label="时间范围">{{ currentSprint.startDate }} ~ {{ currentSprint.endDate }}</el-descriptions-item>
            <el-descriptions-item label="总故事点">{{ currentSprint.totalPoint || '-' }}</el-descriptions-item>
            <el-descriptions-item label="已完成故事点">{{ currentSprint.completedPoint || '-' }}</el-descriptions-item>
          </el-descriptions>
          <BurndownChart :project-id="projectStore.selectedProjectId" />
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
        <el-form-item label="开始日期" prop="startDate">
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
import { useProjectStore } from '@/stores/project'
import BurndownChart from '@/components/BurndownChart.vue'

const projectStore = useProjectStore()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const sprints = ref<any[]>([])
const statusFilter = ref<number | undefined>()
const currentId = ref<number | null>(null)

const form = reactive<any>({ name: '', goal: '', startDate: '', endDate: '' })

const rules = {
  name: [{ required: true, message: '请输入Sprint名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const currentSprint = computed(() => sprints.value.find(s => s.status === 2))
const filteredSprints = computed(() => sprints.value.filter(s => !statusFilter.value || s.status === statusFilter.value))

function getStatusName(status: number) {
  const map: Record<number, string> = { 1: '规划中', 2: '进行中', 3: '已完成', 4: '已关闭' }
  return map[status] || '未知'
}

function getStatusTag(status: number) {
  const map: Record<number, string> = { 1: 'info', 2: 'primary', 3: 'success', 4: '' }
  return map[status] || 'info'
}

function getStageName(stage: number) {
  const map: Record<number, string> = { 0: '概念', 1: '计划', 2: '开发', 3: '发布' }
  return map[stage] || '未知'
}

function getStageTag(stage: number) {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return map[stage] || 'info'
}

async function fetchSprints() {
  if (!projectStore.selectedProjectId) return
  loading.value = true
  try {
    const res = await getSprintList(projectStore.selectedProjectId)
    if (res.code === 200) sprints.value = res.data || []
  } catch (e: any) { ElMessage.error(e.message || '获取Sprint失败') }
  finally { loading.value = false }
}

function showCreateDialog() {
  if (!projectStore.selectedProjectId) { ElMessage.warning('请先选择项目'); return }
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
        const data = { ...form, projectId: projectStore.selectedProjectId }
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
    ElMessage.success('Sprint已开始')
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleComplete(row: any) {
  try {
    await completeSprint(row.id)
    ElMessage.success('Sprint已完成')
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleClose(row: any) {
  try {
    await closeSprint(row.id)
    ElMessage.success('Sprint已关闭')
    fetchSprints()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

onMounted(() => {
  fetchSprints()
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
