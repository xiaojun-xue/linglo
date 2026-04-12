<template>
  <div class="tasks-page">
    <div class="page-header">
      <h2>任务管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 创建任务
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索任务" clearable @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="filters.projectId" placeholder="全部" clearable>
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option label="待办" value="todo" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="待测试" value="in_test" />
            <el-option label="完成" value="done" />
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
        <el-table-column prop="taskNo" label="任务编号" width="130" />
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">{{ getTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.priority)" size="small">{{ getPriorityName(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="dueDate" label="截止日期" width="110" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '创建任务'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择" style="width: 100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="开发" :value="1" />
            <el-option label="测试" :value="2" />
            <el-option label="设计" :value="3" />
            <el-option label="文档" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :label="0">紧急</el-radio>
            <el-radio :label="1">高</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
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
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'
import { getProjectList } from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const list = ref<any[]>([])
const projects = ref<any[]>([])
const filters = reactive({ keyword: '', projectId: undefined as number | undefined, status: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive<any>({ title: '', projectId: undefined, type: 1, priority: 2, dueDate: '', description: '' })
const currentId = ref<number | null>(null)

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }]
}

function getTypeName(type: number) { return { 1: '开发', 2: '测试', 3: '设计', 4: '文档', 5: '其他' }[type] || '其他' }
function getPriorityName(p: number) { return { 0: '紧急', 1: '高', 2: '中', 3: '低' }[p] || '中' }
function getPriorityTag(p: number) { return { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'info' }[p] || 'info' }
function getStatusName(s: string) { return { todo: '待办', in_progress: '进行中', in_test: '待测试', done: '完成', closed: '已关闭' }[s] || s }
function getStatusTag(s: string) { return { todo: 'info', in_progress: 'primary', in_test: 'warning', done: 'success', closed: 'info' }[s] || 'info' }

async function fetchList() {
  loading.value = true
  try {
    const res = await getTaskList({
      keyword: filters.keyword || undefined,
      projectId: filters.projectId,
      status: filters.status || undefined,
      page: pagination.page - 1,
      size: pagination.size
    })
    if (res.code === 200) {
      list.value = res.data.content || []
      pagination.total = res.data.totalElements || 0
    }
  } catch (e: any) { ElMessage.error(e.message || '获取任务列表失败') }
  finally { loading.value = false }
}

async function fetchProjects() {
  try {
    const res = await getProjectList({ page: 0, size: 100 })
    if (res.code === 200) projects.value = res.data.content || []
  } catch (e) { console.error('获取项目列表失败', e) }
}

function resetFilters() { filters.keyword = ''; filters.projectId = undefined; filters.status = ''; fetchList() }

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { title: '', projectId: undefined, type: 1, priority: 2, dueDate: '', description: '' })
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, { title: row.title, projectId: row.projectId, type: row.type, priority: row.priority, dueDate: row.dueDate, description: row.description })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && currentId.value) { await updateTask(currentId.value, form); ElMessage.success('更新成功') }
        else { await createTask(form); ElMessage.success('创建成功') }
        dialogVisible.value = false
        fetchList()
      } catch (e: any) { ElMessage.error(e.message || '操作失败') }
      finally { submitting.value = false }
    }
  })
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定删除该任务吗？', '提示', { type: 'warning' })
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

onMounted(() => { fetchList(); fetchProjects() })
</script>

<style scoped>
.tasks-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; }
.filter-card { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
