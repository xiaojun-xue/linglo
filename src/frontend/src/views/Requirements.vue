<template>
  <div class="requirements-page">
    <div class="page-header">
      <h2>需求管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 创建需求
      </el-button>
    </div>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索需求" clearable @keyup.enter="fetchList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option label="收集" value="collected" />
            <el-option label="已分析" value="analyzed" />
            <el-option label="开发" value="develop" />
            <el-option label="测试" value="test" />
            <el-option label="已发布" value="released" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filters.priority" placeholder="全部" clearable>
            <el-option label="P0-必须" :value="0" />
            <el-option label="P1-重要" :value="1" />
            <el-option label="P2-一般" :value="2" />
            <el-option label="P3-可选" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 需求列表 -->
    <el-card class="content-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="reqNo" label="编号" width="130" />
        <el-table-column prop="title" label="需求标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <span :class="'priority-tag p' + row.priority">{{ getPriorityName(row.priority) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTag(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提出人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑需求' : '创建需求'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入需求标题" />
        </el-form-item>
        <el-form-item label="需求类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="功能需求" :value="1" />
            <el-option label="技术需求" :value="2" />
            <el-option label="体验需求" :value="3" />
            <el-option label="缺陷" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求来源" prop="source">
          <el-select v-model="form.source" placeholder="请选择">
            <el-option label="客户" :value="1" />
            <el-option label="销售" :value="2" />
            <el-option label="内部" :value="3" />
            <el-option label="竞品" :value="4" />
            <el-option label="战略" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :label="0">P0 必须</el-radio>
            <el-radio :label="1">P1 重要</el-radio>
            <el-radio :label="2">P2 一般</el-radio>
            <el-radio :label="3">P3 可选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入需求描述" />
        </el-form-item>
        <el-form-item label="验收标准" prop="acceptance">
          <el-input v-model="form.acceptance" type="textarea" :rows="3" placeholder="请输入验收标准" />
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
import { getRequirementList, createRequirement, updateRequirement, deleteRequirement } from '@/api/requirement'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const list = ref<any[]>([])
const filters = reactive({ keyword: '', status: '', priority: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive<any>({ title: '', type: 1, source: 3, priority: 2, description: '', acceptance: '' })
const currentId = ref<number | null>(null)

const rules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择需求类型', trigger: 'change' }]
}

function getTypeName(type: number) {
  const map: Record<number, string> = { 1: '功能', 2: '技术', 3: '体验', 4: '缺陷' }
  return map[type] || '功能'
}

function getTypeTag(type: number) {
  const map: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' }
  return map[type] || 'info'
}

function getPriorityName(priority: number) {
  const map: Record<number, string> = { 0: 'P0', 1: 'P1', 2: 'P2', 3: 'P3' }
  return map[priority] || 'P2'
}

function getStatusName(status: string) {
  const map: Record<string, string> = {
    collected: '收集', analyzed: '已分析', reviewed: '已评审',
    design: '设计', develop: '开发', test: '测试', released: '已发布', closed: '已关闭'
  }
  return map[status] || status
}

function getStatusTag(status: string) {
  const map: Record<string, string> = {
    collected: 'info', analyzed: '', develop: 'primary', test: 'warning', released: 'success'
  }
  return map[status] || 'info'
}

function formatDate(date: string) {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getRequirementList({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      priority: filters.priority,
      page: pagination.page - 1,
      size: pagination.size
    })
    if (res.code === 200) {
      list.value = res.data.content || []
      pagination.total = res.data.totalElements || 0
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取需求列表失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  filters.priority = undefined
  fetchList()
}

function showCreateDialog() {
  isEdit.value = false
  Object.assign(form, { title: '', type: 1, source: 3, priority: 2, description: '', acceptance: '' })
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, { title: row.title, type: row.type, source: row.source, priority: row.priority, description: row.description, acceptance: row.acceptance })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && currentId.value) {
          await updateRequirement(currentId.value, form)
          ElMessage.success('更新成功')
        } else {
          await createRequirement(form)
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
    await ElMessageBox.confirm('确定删除该需求吗？', '提示', { type: 'warning' })
    await deleteRequirement(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.requirements-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; }
.filter-card { margin-bottom: 16px; }
.content-card { }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
