<template>
  <div class="reviews-page">
    <div class="page-header">
      <h2>评审管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 发起评审
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="评审类型">
          <el-select v-model="filters.type" placeholder="全部" clearable>
            <el-option label="CDCP 概念决策" :value="1" />
            <el-option label="PDCP 计划决策" :value="2" />
            <el-option label="TR4 技术评审" :value="3" />
            <el-option label="ADCP 可发布评审" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option label="草稿" :value="1" />
            <el-option label="待评审" :value="2" />
            <el-option label="评审中" :value="3" />
            <el-option label="通过" :value="4" />
            <el-option label="条件通过" :value="5" />
            <el-option label="拒绝" :value="6" />
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
        <el-table-column prop="reviewNo" label="评审编号" width="130" />
        <el-table-column prop="title" label="评审标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="decision" label="决策" width="100">
          <template #default="{ row }">
            <span v-if="row.decision" :class="'decision-tag decision-' + row.decision">{{ getDecisionName(row.decision) }}</span>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledAt" label="计划时间" width="160">
          <template #default="{ row }">{{ row.scheduledAt ? formatDate(row.scheduledAt) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 2" type="primary" link size="small" @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 3" type="success" link size="small" @click="showDecideDialog(row)">决策</el-button>
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

    <!-- 创建评审对话框 -->
    <el-dialog v-model="dialogVisible" title="发起评审" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="评审标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入评审标题" />
        </el-form-item>
        <el-form-item label="评审类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width:100%">
            <el-option label="CDCP 概念决策评审" :value="1" />
            <el-option label="PDCP 计划决策评审" :value="2" />
            <el-option label="TR4 技术评审" :value="3" />
            <el-option label="ADCP 可发布评审" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划评审时间">
          <el-date-picker v-model="form.scheduledAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- 评审详情对话框 -->
    <el-dialog v-model="detailVisible" title="评审详情" width="700px">
      <el-descriptions :column="2" border v-if="currentReview">
        <el-descriptions-item label="评审编号">{{ currentReview.reviewNo }}</el-descriptions-item>
        <el-descriptions-item label="评审类型">{{ getTypeName(currentReview.type) }}</el-descriptions-item>
        <el-descriptions-item label="评审状态">{{ getStatusName(currentReview.status) }}</el-descriptions-item>
        <el-descriptions-item label="评审决策">{{ currentReview.decision ? getDecisionName(currentReview.decision) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="计划时间" :span="2">{{ currentReview.scheduledAt ? formatDate(currentReview.scheduledAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="评审结论" :span="2">{{ currentReview.conclusion || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评审决策对话框 -->
    <el-dialog v-model="decideDialogVisible" title="评审决策" width="500px">
      <el-form label-width="100px">
        <el-form-item label="决策结果">
          <el-radio-group v-model="decideForm.decision">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">条件通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评审结论">
          <el-input v-model="decideForm.conclusion" type="textarea" :rows="4" placeholder="请输入评审结论" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="decideDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDecide" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewList, createReview, submitReview, startReview, decideReview } from '@/api/review'
import { useProjectStore } from '@/stores/project'
import dayjs from 'dayjs'

const projectStore = useProjectStore()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const decideDialogVisible = ref(false)
const formRef = ref()

const list = ref<any[]>([])
const currentReview = ref<any>(null)
const filters = reactive({ type: undefined as number | undefined, status: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const form = reactive<any>({ title: '', type: 1, scheduledAt: '' })
const decideForm = reactive<any>({ decision: 1, conclusion: '' })

const rules = {
  title: [{ required: true, message: '请输入评审标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择评审类型', trigger: 'change' }]
}

function getTypeName(type: number) { return { 1: 'CDCP', 2: 'PDCP', 3: 'TR4', 4: 'ADCP' }[type] || '未知' }
function getTypeTag(type: number) { return { 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[type] || 'info' }
function getStatusName(status: number) { return { 1: '草稿', 2: '待评审', 3: '评审中', 4: '通过', 5: '条件通过', 6: '拒绝', 7: '已撤回' }[status] || '未知' }
function getStatusTag(status: number) { return { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: 'warning', 6: 'danger', 7: 'info' }[status] || 'info' }
function getDecisionName(d: number) { return { 1: '通过', 2: '条件通过', 3: '拒绝' }[d] || '-' }
function formatDate(d: string) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }

async function fetchList() {
  loading.value = true
  try {
    const res = await getReviewList({ type: filters.type, status: filters.status, projectId: projectStore.selectedProjectId || undefined, page: pagination.page - 1, size: pagination.size })
    if (res.code === 200) { list.value = res.data.content || []; pagination.total = res.data.totalElements || 0 }
  } catch (e: any) { ElMessage.error(e.message || '获取评审列表失败') }
  finally { loading.value = false }
}

function resetFilters() { filters.type = undefined; filters.status = undefined; fetchList() }

function showCreateDialog() { Object.assign(form, { title: '', type: 1, scheduledAt: '' }); dialogVisible.value = true }

function showDetail(row: any) { currentReview.value = row; detailVisible.value = true }

async function handleCreate() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try { await createReview({ ...form, projectId: projectStore.selectedProjectId }); ElMessage.success('创建成功'); dialogVisible.value = false; fetchList() }
      catch (e: any) { ElMessage.error(e.message || '创建失败') }
      finally { submitting.value = false }
    }
  })
}

async function handleSubmit(row: any) {
  try { await submitReview(row.id); ElMessage.success('提交成功'); fetchList() }
  catch (e: any) { ElMessage.error(e.message || '提交失败') }
}

async function handleStart(row: any) {
  try { await startReview(row.id); ElMessage.success('评审已开始'); fetchList() }
  catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

function showDecideDialog(row: any) { currentReview.value = row; decideForm.decision = 1; decideForm.conclusion = ''; decideDialogVisible.value = true }

async function handleDecide() {
  try {
    await decideReview(currentReview.value.id, decideForm.decision, decideForm.conclusion)
    ElMessage.success('决策完成')
    decideDialogVisible.value = false
    fetchList()
  } catch (e: any) { ElMessage.error(e.message || '决策失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.reviews-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; }
.filter-card { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.decision-tag { padding: 2px 8px; border-radius: 3px; font-size: 12px; color: #fff; }
.decision-1 { background: #67c23a; } .decision-2 { background: #e6a23c; } .decision-3 { background: #f56c6c; }
</style>
