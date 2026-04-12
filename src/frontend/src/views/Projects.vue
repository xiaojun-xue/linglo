<template>
  <div class="projects-page">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 新建项目
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键�?>
          <el-input v-model="filters.keyword" placeholder="搜索项目" clearable @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="filters.stage" placeholder="全部" clearable>
            <el-option label="概念阶段" :value="1" />
            <el-option label="计划阶段" :value="2" />
            <el-option label="开发阶�? :value="3" />
            <el-option label="发布阶段" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状�?>
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option label="未开�? :value="1" />
            <el-option label="进行�? :value="2" />
            <el-option label="已暂�? :value="3" />
            <el-option label="已完�? :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="content-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="projectNo" label="项目编号" width="130" />
        <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">{{ getTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="stage" label="IPD阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="getStageTag(row.stage)" size="small">{{ getStageName(row.stage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状�? width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="180">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日�? width="110">
          <template #default="{ row }">{{ row.startDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="110">
          <template #default="{ row }">{{ row.endDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="$router.push(`/tasks?projectId=${row.id}`)">任务</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新建项目'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名�? />
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">敏捷开�?/el-radio>
            <el-radio :label="2">瀑布�?/el-radio>
            <el-radio :label="3">看板</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始日�? prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入项目描�? />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectList, createProject, updateProject, deleteProject } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const list = ref<any[]>([])
const filters = reactive({ keyword: '', stage: undefined as number | undefined, status: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive<any>({ name: '', type: 1, startDate: '', endDate: '', description: '' })
const currentId = ref<number | null>(null)

const rules = { name: [{ required: true, message: '请输入项目名�?, trigger: 'blur' }] }

function getTypeName(type: number) {
  return { 1: '敏捷', 2: '瀑布', 3: '看板' }[type] || '敏捷'
}

function getStageName(stage: number) {
  return { 1: '概念', 2: '计划', 3: '开�?, 4: '发布' }[stage] || '概念'
}

function getStageTag(stage: number) {
  return { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success' }[stage] || 'info'
}

function getStatusName(status: number) {
  return { 1: '未开�?, 2: '进行�?, 3: '已暂�?, 4: '已完�?, 5: '已归�? }[status] || '未开�?
}

function getStatusTag(status: number) {
  return { 1: 'info', 2: 'primary', 3: 'warning', 4: 'success', 5: 'info' }[status] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getProjectList({
      keyword: filters.keyword || undefined,
      stage: filters.stage,
      status: filters.status,
      page: pagination.page - 1,
      size: pagination.size
    })
    if (res.code === 200) {
      list.value = res.data.content || []
      pagination.total = res.data.totalElements || 0
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取项目列表失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.stage = undefined
  filters.status = undefined
  fetchList()
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { name: '', type: 1, startDate: '', endDate: '', description: '' })
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, { name: row.name, type: row.type, startDate: row.startDate, endDate: row.endDate, description: row.description })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && currentId.value) {
          await updateProject(currentId.value, form)
          ElMessage.success('更新成功')
        } else {
          await createProject(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (e: any) {
        ElMessage.error(e.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定删除该项目吗�?, '提示', { type: 'warning' })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.projects-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; }
.filter-card { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
