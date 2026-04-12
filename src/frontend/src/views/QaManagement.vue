<template>
  <div class="qa-page">
    <div class="page-header">
      <h2>测试管理</h2>
      <el-tabs v-model="activeTab" @tab-change="activeTab = $event">
        <el-tab-pane label="测试用例" name="cases" />
        <el-tab-pane label="缺陷管理" name="bugs" />
      </el-tabs>
    </div>

    <!-- 测试用例 -->
    <div v-show="activeTab === 'cases'">
      <el-card class="filter-card">
        <el-form :inline="true" :model="caseFilters">
          <el-form-item label="关键词"><el-input v-model="caseFilters.keyword" placeholder="搜索用例" clearable /></el-form-item>
          <el-form-item label="模块"><el-input v-model="caseFilters.module" placeholder="模块" clearable /></el-form-item>
          <el-form-item><el-button type="primary" @click="fetchCases">搜索</el-button><el-button @click="caseFilters = {keyword:'',module:''}; fetchCases()">重置</el-button></el-form-item>
        </el-form>
      </el-card>
      <el-card>
        <template #header><el-button type="primary" @click="showCaseDialog()"><el-icon><Plus /></el-icon>新建用例</el-button></template>
        <el-table :data="cases" v-loading="caseLoading" stripe>
          <el-table-column prop="caseNo" label="用例编号" width="120" />
          <el-table-column prop="title" label="用例标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="100" />
          <el-table-column prop="type" label="类型" width="80"><template #default="{row}">{{getTypeName(row.type)}}</template></el-table-column>
          <el-table-column prop="priority" label="优先级" width="80"><template #default="{row}"><el-tag :type="getPriorityTag(row.priority)" size="small">{{getPriorityName(row.priority)}}</el-tag></template></el-table-column>
          <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="getStatusTag(row.status)" size="small">{{getCaseStatusName(row.status)}}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button type="primary" link size="small" @click="showCaseDialog(row)">编辑</el-button><el-button type="danger" link size="small" @click="handleDeleteCase(row)">删除</el-button></template></el-table-column>
        </el-table>
        <el-pagination class="mt-16" v-model:current-page="casePage" v-model:page-size="caseSize" :total="caseTotal" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchCases" @current-change="fetchCases" />
      </el-card>
    </div>

    <!-- 缺陷管理 -->
    <div v-show="activeTab === 'bugs'">
      <el-card class="filter-card">
        <el-form :inline="true" :model="bugFilters">
          <el-form-item label="关键词"><el-input v-model="bugFilters.keyword" placeholder="搜索缺陷" clearable /></el-form-item>
          <el-form-item label="严重"><el-select v-model="bugFilters.severity" placeholder="全部" clearable><el-option label="致命" :value="0" /><el-option label="严重" :value="1" /><el-option label="一般" :value="2" /><el-option label="轻微" :value="3" /></el-select></el-form-item>
          <el-form-item label="状态"><el-select v-model="bugFilters.status" placeholder="全部" clearable><el-option label="新建" :value="1" /><el-option label="已确认" :value="2" /><el-option label="已分配" :value="3" /><el-option label="修复中" :value="4" /><el-option label="待验证" :value="5" /><el-option label="已关闭" :value="6" /></el-select></el-form-item>
          <el-form-item><el-button type="primary" @click="fetchBugs">搜索</el-button><el-button @click="bugFilters = {keyword:'',severity:undefined,status:undefined}; fetchBugs()">重置</el-button></el-form-item>
        </el-form>
      </el-card>
      <el-card>
        <template #header><el-button type="primary" @click="showBugDialog()"><el-icon><Plus /></el-icon>新建缺陷</el-button></template>
        <el-table :data="bugs" v-loading="bugLoading" stripe>
          <el-table-column prop="bugNo" label="缺陷编号" width="120" />
          <el-table-column prop="title" label="缺陷标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="severity" label="严重" width="80"><template #default="{row}"><el-tag :type="getSeverityTag(row.severity)" size="small">{{getSeverityName(row.severity)}}</el-tag></template></el-table-column>
          <el-table-column prop="priority" label="优先级" width="80"><template #default="{row}"><el-tag :type="getPriorityTag(row.priority)" size="small">{{getPriorityName(row.priority)}}</el-tag></template></el-table-column>
          <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="getBugStatusTag(row.status)" size="small">{{getBugStatusName(row.status)}}</el-tag></template></el-table-column>
          <el-table-column prop="assigneeName" label="负责人" width="100" />
          <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button type="primary" link size="small" @click="showBugDialog(row)">编辑</el-button><el-button type="success" link size="small" @click="changeBugStatus(row)">改状态</el-button><el-button type="danger" link size="small" @click="handleDeleteBug(row)">删除</el-button></template></el-table-column>
        </el-table>
        <el-pagination class="mt-16" v-model:current-page="bugPage" v-model:page-size="bugSize" :total="bugTotal" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchBugs" @current-change="fetchBugs" />
      </el-card>
    </div>

    <!-- 用例对话框 -->
    <el-dialog v-model="caseDialogVisible" :title="editingCase?.id ? '编辑用例' : '新建用例'" width="600px">
      <el-form ref="caseFormRef" :model="caseForm" label-width="100px">
        <el-form-item label="用例标题" prop="title" required><el-input v-model="caseForm.title" /></el-form-item>
        <el-form-item label="所属模块"><el-input v-model="caseForm.module" /></el-form-item>
        <el-form-item label="用例类型"><el-select v-model="caseForm.type"><el-option label="功能测试" :value="1" /><el-option label="接口测试" :value="2" /><el-option label="性能测试" :value="3" /><el-option label="安全测试" :value="4" /></el-select></el-form-item>
        <el-form-item label="优先级"><el-radio-group v-model="caseForm.priority"><el-radio :label="1">高</el-radio><el-radio :label="2">中</el-radio><el-radio :label="3">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="前置条件"><el-input v-model="caseForm.precondition" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="测试步骤"><el-input v-model="caseForm.steps" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="预期结果"><el-input v-model="caseForm.expectedResult" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="caseDialogVisible=false">取消</el-button><el-button type="primary" @click="handleSaveCase" :loading="caseSaving">确定</el-button></template>
    </el-dialog>

    <!-- 缺陷对话框 -->
    <el-dialog v-model="bugDialogVisible" :title="editingBug?.id ? '编辑缺陷' : '新建缺陷'" width="600px">
      <el-form ref="bugFormRef" :model="bugForm" label-width="100px">
        <el-form-item label="缺陷标题" prop="title" required><el-input v-model="bugForm.title" /></el-form-item>
        <el-form-item label="严重等级"><el-select v-model="bugForm.severity"><el-option label="致命" :value="0" /><el-option label="严重" :value="1" /><el-option label="一般" :value="2" /><el-option label="轻微" :value="3" /></el-select></el-form-item>
        <el-form-item label="优先级"><el-radio-group v-model="bugForm.priority"><el-radio :label="0">紧急</el-radio><el-radio :label="1">高</el-radio><el-radio :label="2">中</el-radio><el-radio :label="3">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="缺陷描述"><el-input v-model="bugForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="bugDialogVisible=false">取消</el-button><el-button type="primary" @click="handleSaveBug" :loading="bugSaving">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTestCaseList, createTestCase, updateTestCase, deleteTestCase, getBugList, createBug, updateBug, updateBugStatus, deleteBug } from '@/api/qa'

const activeTab = ref('cases')
const caseLoading = ref(false)
const bugLoading = ref(false)
const caseDialogVisible = ref(false)
const bugDialogVisible = ref(false)
const caseSaving = ref(false)
const bugSaving = ref(false)
const caseFormRef = ref()
const bugFormRef = ref()
const editingCase = ref<any>(null)
const editingBug = ref<any>(null)

const cases = ref<any[]>([])
const bugs = ref<any[]>([])
const caseFilters = reactive({ keyword: '', module: '' })
const bugFilters = reactive({ keyword: '', severity: undefined as number | undefined, status: undefined as number | undefined })
const casePage = ref(1)
const caseSize = ref(20)
const caseTotal = ref(0)
const bugPage = ref(1)
const bugSize = ref(20)
const bugTotal = ref(0)

const caseForm = reactive({ title: '', module: '', type: 1, priority: 2, precondition: '', steps: '', expectedResult: '' })
const bugForm = reactive({ title: '', severity: 2, priority: 2, description: '' })

function getTypeName(t: number) { return { 1: '功能', 2: '接口', 3: '性能', 4: '安全' }[t] || '功能' }
function getPriorityName(p: number) { return { 0: '紧急', 1: '高', 2: '中', 3: '低' }[p] || '中' }
function getPriorityTag(p: number) { return { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'info' }[p] || 'info' }
function getSeverityName(s: number) { return { 0: '致命', 1: '严重', 2: '一般', 3: '轻微' }[s] || '一般' }
function getSeverityTag(s: number) { return { 0: 'danger', 1: 'warning', 2: '', 3: 'info' }[s] || 'info' }
function getCaseStatusName(s: number) { return { 1: '设计', 2: '待评审', 3: '通过', 4: '禁用' }[s] || '设计' }
function getCaseStatusTag(s: number) { return { 1: 'info', 2: 'warning', 3: 'success', 4: '' }[s] || 'info' }
function getBugStatusName(s: number) { return { 1: '新建', 2: '已确认', 3: '已分配', 4: '修复中', 5: '待验证', 6: '已关闭', 7: '已拒绝' }[s] || '新建' }
function getBugStatusTag(s: number) { return { 1: 'info', 2: 'warning', 3: 'primary', 4: '', 5: 'warning', 6: 'success', 7: '' }[s] || 'info' }

async function fetchCases() {
  caseLoading.value = true
  try {
    const res = await getTestCaseList({ keyword: caseFilters.keyword || undefined, module: caseFilters.module || undefined, page: casePage.value - 1, size: caseSize.value })
    if (res.code === 200) { cases.value = res.data.content || []; caseTotal.value = res.data.totalElements || 0 }
  } catch (e: any) { ElMessage.error(e.message) }
  finally { caseLoading.value = false }
}

async function fetchBugs() {
  bugLoading.value = true
  try {
    const res = await getBugList({ keyword: bugFilters.keyword || undefined, severity: bugFilters.severity, status: bugFilters.status, page: bugPage.value - 1, size: bugSize.value })
    if (res.code === 200) { bugs.value = res.data.content || []; bugTotal.value = res.data.totalElements || 0 }
  } catch (e: any) { ElMessage.error(e.message) }
  finally { bugLoading.value = false }
}

function showCaseDialog(row?: any) {
  editingCase.value = row || null
  if (row) Object.assign(caseForm, { title: row.title, module: row.module, type: row.type, priority: row.priority, precondition: row.precondition, steps: row.steps, expectedResult: row.expectedResult })
  else Object.assign(caseForm, { title: '', module: '', type: 1, priority: 2, precondition: '', steps: '', expectedResult: '' })
  caseDialogVisible.value = true
}

function showBugDialog(row?: any) {
  editingBug.value = row || null
  if (row) Object.assign(bugForm, { title: row.title, severity: row.severity, priority: row.priority, description: row.description })
  else Object.assign(bugForm, { title: '', severity: 2, priority: 2, description: '' })
  bugDialogVisible.value = true
}

async function handleSaveCase() {
  caseSaving.value = true
  try {
    if (editingCase.value?.id) await updateTestCase(editingCase.value.id, caseForm)
    else await createTestCase(caseForm)
    ElMessage.success('保存成功')
    caseDialogVisible.value = false
    fetchCases()
  } catch (e: any) { ElMessage.error(e.message) }
  finally { caseSaving.value = false }
}

async function handleSaveBug() {
  bugSaving.value = true
  try {
    if (editingBug.value?.id) await updateBug(editingBug.value.id, bugForm)
    else await createBug(bugForm)
    ElMessage.success('保存成功')
    bugDialogVisible.value = false
    fetchBugs()
  } catch (e: any) { ElMessage.error(e.message) }
  finally { bugSaving.value = false }
}

async function handleDeleteCase(row: any) {
  try { await ElMessageBox.confirm('确定删除该用例？', '提示', { type: 'warning' }); await deleteTestCase(row.id); ElMessage.success('删除成功'); fetchCases() } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message) }
}

async function handleDeleteBug(row: any) {
  try { await ElMessageBox.confirm('确定删除该缺陷？', '提示', { type: 'warning' }); await deleteBug(row.id); ElMessage.success('删除成功'); fetchBugs() } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message) }
}

async function changeBugStatus(row: any) {
  const nextStatus = { 1: 2, 2: 3, 3: 4, 4: 5, 5: 6 }[row.status]
  if (!nextStatus) { ElMessage.info('该状态无法继续流转'); return }
  try {
    await updateBugStatus(row.id, nextStatus)
    ElMessage.success('状态已更新')
    fetchBugs()
  } catch (e: any) { ElMessage.error(e.message) }
}

onMounted(() => { fetchCases(); fetchBugs() })
</script>

<style scoped>
.qa-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; margin: 0; }
.filter-card { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
