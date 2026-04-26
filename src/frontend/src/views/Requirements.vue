<template>
  <div class="requirements-page">
    <div class="page-header">
      <h2>需求管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog(1, null)">
          <el-icon><Plus /></el-icon> 新建IR
        </el-button>
        <el-dropdown @command="handleExport" trigger="click">
          <el-button>
            <el-icon><Download /></el-icon> 导出说明书
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="system">系统需求分析说明书</el-dropdown-item>
              <el-dropdown-item command="func">功能需求分析说明书</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-layout">
      <!-- 左侧：需求树 -->
      <div class="tree-panel">
        <div class="tree-header">
          <el-input v-model="searchText" placeholder="搜索需求..." clearable size="small" prefix-icon="Search" />
        </div>
        <div class="tree-body" v-loading="treeLoading">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :expand-on-click-node="false"
            :default-expand-all="true"
            :filter-node-method="filterNode"
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node-content">
                <el-tag size="small" :type="getLevelTagType(data.level)" class="level-tag">
                  {{ getLevelName(data.level) }}
                </el-tag>
                <span class="node-title" :title="data.title">{{ data.title }}</span>
                <el-tag size="small" :type="getStatusTagType(data.status)" class="status-tag">
                  {{ getStatusName(data.status) }}
                </el-tag>
              </div>
            </template>
          </el-tree>
          <el-empty v-if="!treeLoading && treeData.length === 0" description="暂无需求，点击「新建IR」创建" />
        </div>
      </div>

      <!-- 右侧：需求详情 -->
      <div class="detail-panel">
        <template v-if="selectedReq">
          <!-- 需求操作栏 -->
          <div class="detail-toolbar">
            <div class="req-identity">
              <el-tag :type="getLevelTagType(selectedReq.level)" effect="dark">
                {{ getLevelName(selectedReq.level) }}
              </el-tag>
              <span class="req-no">{{ selectedReq.reqNo }}</span>
              <el-tag :type="getStatusTagType(selectedReq.status)">{{ getStatusName(selectedReq.status) }}</el-tag>
            </div>
            <div class="toolbar-actions">
              <!-- 分解子需求 -->
              <el-button
                v-if="selectedReq.level < 3"
                type="success" size="small"
                @click="showCreateDialog(selectedReq.level + 1, selectedReq.id)"
              >
                <el-icon><Plus /></el-icon>
                分解{{ selectedReq.level === 1 ? 'SR' : 'AR' }}
              </el-button>
              <!-- 推进状态 -->
              <el-button
                v-if="getNextStatus(selectedReq.status)"
                type="warning" size="small"
                @click="handleAdvanceStatus(selectedReq.id)"
              >
                推进到「{{ getNextStatusName(selectedReq.status) }}」
              </el-button>
              <el-button type="primary" size="small" @click="showEditDialog(selectedReq)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(selectedReq)">删除</el-button>
            </div>
          </div>

          <!-- 需求详情卡片 -->
          <el-card class="detail-card">
            <h3>{{ selectedReq.title }}</h3>

            <!-- IR 的 5W2H 结构化描述 -->
            <template v-if="selectedReq.level === 1">
              <el-divider content-position="left">5W2H 结构化描述</el-divider>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="Who（目标用户）" :span="1">
                  {{ selectedReq.who || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="What（需要什么）" :span="1">
                  {{ selectedReq.what || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="When（时间要求）" :span="1">
                  {{ selectedReq.whenDesc || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="Where（使用场景）" :span="1">
                  {{ selectedReq.whereDesc || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="Why（业务动机）" :span="2">
                  {{ selectedReq.whyDesc || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="How（实现路径）" :span="2">
                  {{ selectedReq.howDesc || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="How Much（规模范围）" :span="2">
                  {{ selectedReq.howMuch || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </template>

            <el-divider content-position="left">基本信息</el-divider>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="需求类型">
                <el-tag size="small" :type="getTypeTag(selectedReq.type)">{{ getTypeName(selectedReq.type) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="优先级">
                <span :class="'priority-tag p' + selectedReq.priority">{{ getPriorityName(selectedReq.priority) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="需求来源">{{ getSourceName(selectedReq.source) }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDate(selectedReq.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="需求描述" :span="2">
                <div class="desc-text">{{ selectedReq.description || '-' }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="验收标准" :span="2">
                <div class="desc-text">{{ selectedReq.acceptance || '-' }}</div>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 子需求列表 -->
            <template v-if="selectedReq.children && selectedReq.children.length > 0">
              <el-divider content-position="left">
                子需求（{{ selectedReq.level === 1 ? 'SR' : 'AR' }}）
              </el-divider>
              <el-table :data="selectedReq.children" stripe size="small">
                <el-table-column prop="reqNo" label="编号" width="140" />
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="priority" label="优先级" width="80">
                  <template #default="{ row }">
                    <span :class="'priority-tag p' + row.priority">{{ getPriorityName(row.priority) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </el-card>
        </template>

        <template v-else>
          <div class="empty-detail">
            <el-empty description="请从左侧选择需求查看详情">
              <el-button type="primary" @click="showCreateDialog(1, null)">新建IR需求</el-button>
            </el-empty>
          </div>
        </template>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="需求层级">
          <el-tag :type="getLevelTagType(form.level)" effect="dark">{{ getLevelFullName(form.level) }}</el-tag>
          <span v-if="form.parentId" style="margin-left: 12px; color: #999; font-size: 12px;">
            父需求ID: {{ form.parentId }}
          </span>
        </el-form-item>

        <el-form-item label="需求标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入需求标题" />
        </el-form-item>

        <!-- IR 层级显示 5W2H 表单 -->
        <template v-if="form.level === 1">
          <el-divider content-position="left">5W2H 结构化描述</el-divider>
          <el-form-item label="Who（目标用户）">
            <el-input v-model="form.who" placeholder="谁是目标用户/干系人？" />
          </el-form-item>
          <el-form-item label="What（做什么）">
            <el-input v-model="form.what" type="textarea" :rows="2" placeholder="需要什么功能或能力？" />
          </el-form-item>
          <el-form-item label="When（何时）">
            <el-input v-model="form.whenDesc" placeholder="时间要求/期望交付时间" />
          </el-form-item>
          <el-form-item label="Where（何处）">
            <el-input v-model="form.whereDesc" placeholder="使用场景/环境是什么？" />
          </el-form-item>
          <el-form-item label="Why（为什么）">
            <el-input v-model="form.whyDesc" type="textarea" :rows="2" placeholder="业务动机/价值是什么？" />
          </el-form-item>
          <el-form-item label="How（如何）">
            <el-input v-model="form.howDesc" type="textarea" :rows="2" placeholder="建议的实现方式/路径" />
          </el-form-item>
          <el-form-item label="How Much（多少）">
            <el-input v-model="form.howMuch" placeholder="规模/成本/范围估计" />
          </el-form-item>
        </template>

        <el-divider content-position="left" v-if="form.level === 1">其他信息</el-divider>

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
            <el-radio :value="0">P0 必须</el-radio>
            <el-radio :value="1">P1 重要</el-radio>
            <el-radio :value="2">P2 一般</el-radio>
            <el-radio :value="3">P3 可选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入需求描述" />
        </el-form-item>
        <el-form-item label="验收标准" prop="acceptance">
          <el-input v-model="form.acceptance" type="textarea" :rows="2" placeholder="请输入验收标准" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导出预览对话框 -->
    <el-dialog v-model="exportDialogVisible" :title="exportDoc.title" width="800px" destroy-on-close>
      <div class="export-preview" ref="exportContentRef">
        <h2 style="text-align: center;">{{ exportDoc.title }}</h2>
        <p style="text-align: center; color: #999;">生成时间：{{ exportDoc.generatedAt }}</p>

        <template v-for="(section, idx) in exportDoc.sections" :key="idx">
          <template v-if="exportType === 'system'">
            <h3>{{ idx + 1 }}. {{ section.ir?.title }}</h3>
            <p><strong>编号：</strong>{{ section.ir?.reqNo }}</p>
            <p><strong>描述：</strong>{{ section.ir?.description || '-' }}</p>

            <h4>5W2H 分析</h4>
            <table class="export-table">
              <tr v-for="(val, key) in section.structured" :key="key">
                <td class="label-cell">{{ key }}</td>
                <td>{{ val }}</td>
              </tr>
            </table>

            <h4>系统需求分解（SR）</h4>
            <table class="export-table" v-if="section.systemRequirements?.length">
              <tr><th>编号</th><th>标题</th><th>描述</th><th>状态</th></tr>
              <tr v-for="sr in section.systemRequirements" :key="sr.id">
                <td>{{ sr.reqNo }}</td>
                <td>{{ sr.title }}</td>
                <td>{{ sr.description || '-' }}</td>
                <td>{{ getStatusName(sr.status) }}</td>
              </tr>
            </table>
            <p v-else style="color: #999;">暂无SR分解</p>
          </template>

          <template v-if="exportType === 'func'">
            <h3>{{ idx + 1 }}. {{ section.sr?.title }}</h3>
            <p><strong>编号：</strong>{{ section.sr?.reqNo }}</p>
            <p><strong>描述：</strong>{{ section.sr?.description || '-' }}</p>
            <p v-if="section.parentIr"><strong>来源IR：</strong>{{ section.parentIr?.title }}（{{ section.parentIr?.reqNo }}）</p>

            <h4>分配需求分解（AR）</h4>
            <table class="export-table" v-if="section.allocationRequirements?.length">
              <tr><th>编号</th><th>标题</th><th>描述</th><th>状态</th></tr>
              <tr v-for="ar in section.allocationRequirements" :key="ar.id">
                <td>{{ ar.reqNo }}</td>
                <td>{{ ar.title }}</td>
                <td>{{ ar.description || '-' }}</td>
                <td>{{ getStatusName(ar.status) }}</td>
              </tr>
            </table>
            <p v-else style="color: #999;">暂无AR分解</p>
          </template>
          <el-divider v-if="idx < exportDoc.sections.length - 1" />
        </template>
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">打印/导出PDF</el-button>
      </template>
    </el-dialog>

    <!-- 导出选择对话框 -->
    <el-dialog v-model="exportSelectVisible" :title="exportSelectTitle" width="500px">
      <p style="margin-bottom: 12px;">请选择要导出的{{ exportType === 'system' ? 'IR' : 'SR' }}需求：</p>
      <el-checkbox-group v-model="selectedExportIds">
        <div v-for="item in exportCandidates" :key="item.id" style="margin-bottom: 8px;">
          <el-checkbox :value="item.id">
            <el-tag size="small" :type="getLevelTagType(item.level)">{{ getLevelName(item.level) }}</el-tag>
            {{ item.reqNo }} - {{ item.title }}
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <el-empty v-if="exportCandidates.length === 0"
        :description="'暂无' + (exportType === 'system' ? 'IR' : 'SR') + '需求'" />
      <template #footer>
        <el-button @click="exportSelectVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedExportIds.length === 0" @click="doExport">确定导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search } from '@element-plus/icons-vue'
import {
  getRequirementTree, createRequirement, updateRequirement, deleteRequirement,
  advanceRequirementStatus, exportSystemSpec, exportFuncSpec
} from '@/api/requirement'
import { useProjectStore } from '@/stores/project'
import dayjs from 'dayjs'

const projectStore = useProjectStore()

const treeLoading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const treeRef = ref()
const searchText = ref('')
const exportContentRef = ref()

const treeData = ref<any[]>([])
const selectedReq = ref<any>(null)
const currentEditId = ref<number | null>(null)

const treeProps = { children: 'children', label: 'title' }

// 导出相关
const exportDialogVisible = ref(false)
const exportSelectVisible = ref(false)
const exportType = ref<'system' | 'func'>('system')
const exportDoc = ref<any>({ title: '', generatedAt: '', sections: [] })
const exportCandidates = ref<any[]>([])
const selectedExportIds = ref<number[]>([])

const exportSelectTitle = computed(() =>
  exportType.value === 'system' ? '选择IR导出系统需求分析说明书' : '选择SR导出功能需求分析说明书'
)

const form = reactive<any>({
  title: '', type: 1, source: 3, priority: 2, description: '', acceptance: '',
  level: 1, parentId: null as number | null,
  who: '', what: '', whenDesc: '', whereDesc: '', whyDesc: '', howDesc: '', howMuch: ''
})

const rules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择需求类型', trigger: 'change' }]
}

const dialogTitle = computed(() => {
  if (isEdit.value) return '编辑需求'
  const levelNames: Record<number, string> = { 1: 'IR（初始需求）', 2: 'SR（系统需求）', 3: 'AR（分配需求）' }
  return `新建 ${levelNames[form.level] || '需求'}`
})

// ==================== 工具函数 ====================

function getLevelName(level: number) {
  return { 1: 'IR', 2: 'SR', 3: 'AR' }[level] || 'REQ'
}

function getLevelFullName(level: number) {
  return { 1: 'IR - 初始需求', 2: 'SR - 系统需求', 3: 'AR - 分配需求' }[level] || '需求'
}

function getLevelTagType(level: number) {
  return { 1: 'danger', 2: 'warning', 3: '' }[level] || 'info'
}

function getStatusName(status: string) {
  const map: Record<string, string> = {
    new: '新增', reviewing: '评审中', archived: '已归档',
    testing: '测试中', accepted: '已验收'
  }
  return map[status] || status
}

function getStatusTagType(status: string) {
  const map: Record<string, string> = {
    new: 'info', reviewing: 'warning', archived: '',
    testing: 'primary', accepted: 'success'
  }
  return map[status] || 'info'
}

function getNextStatus(status: string): string | null {
  const map: Record<string, string> = {
    new: 'reviewing', reviewing: 'archived', archived: 'testing', testing: 'accepted'
  }
  return map[status] || null
}

function getNextStatusName(status: string): string {
  const next = getNextStatus(status)
  return next ? getStatusName(next) : ''
}

function getTypeName(type: number) {
  return { 1: '功能', 2: '技术', 3: '体验', 4: '缺陷' }[type] || '功能'
}

function getTypeTag(type: number) {
  return { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' }[type] || 'info'
}

function getPriorityName(priority: number) {
  return { 0: 'P0', 1: 'P1', 2: 'P2', 3: 'P3' }[priority] || 'P2'
}

function getSourceName(source: number) {
  return { 1: '客户', 2: '销售', 3: '内部', 4: '竞品', 5: '战略' }[source] || '-'
}

function formatDate(date: string) {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

function filterNode(value: string, data: any) {
  if (!value) return true
  return data.title?.toLowerCase().includes(value.toLowerCase()) ||
         data.reqNo?.toLowerCase().includes(value.toLowerCase())
}

// ==================== 数据获取 ====================

async function fetchTree() {
  if (!projectStore.selectedProjectId) return
  treeLoading.value = true
  try {
    const res = await getRequirementTree(projectStore.selectedProjectId)
    if (res.code === 200) {
      treeData.value = res.data || []
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取需求树失败')
  } finally {
    treeLoading.value = false
  }
}

// ==================== 事件处理 ====================

function handleNodeClick(data: any) {
  selectedReq.value = data
}

function showCreateDialog(level: number, parentId: number | null) {
  isEdit.value = false
  currentEditId.value = null
  Object.assign(form, {
    title: '', type: 1, source: 3, priority: 2, description: '', acceptance: '',
    level, parentId,
    who: '', what: '', whenDesc: '', whereDesc: '', whyDesc: '', howDesc: '', howMuch: ''
  })
  dialogVisible.value = true
}

function showEditDialog(req: any) {
  isEdit.value = true
  currentEditId.value = req.id
  Object.assign(form, {
    title: req.title, type: req.type, source: req.source, priority: req.priority,
    description: req.description, acceptance: req.acceptance,
    level: req.level, parentId: req.parentId,
    who: req.who || '', what: req.what || '', whenDesc: req.whenDesc || '',
    whereDesc: req.whereDesc || '', whyDesc: req.whyDesc || '',
    howDesc: req.howDesc || '', howMuch: req.howMuch || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value && currentEditId.value) {
        await updateRequirement(currentEditId.value, form)
        ElMessage.success('更新成功')
      } else {
        await createRequirement({ ...form, projectId: projectStore.selectedProjectId })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await fetchTree()
      // 刷新后重新选中
      if (selectedReq.value) {
        await nextTick()
        const found = findNodeById(treeData.value, selectedReq.value.id)
        if (found) selectedReq.value = found
      }
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

function findNodeById(nodes: any[], id: number): any {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

async function handleAdvanceStatus(id: number) {
  try {
    await advanceRequirementStatus(id)
    ElMessage.success('状态推进成功')
    await fetchTree()
    if (selectedReq.value) {
      await nextTick()
      const found = findNodeById(treeData.value, id)
      if (found) selectedReq.value = found
    }
  } catch (e: any) {
    ElMessage.error(e.message || '状态推进失败')
  }
}

async function handleDelete(req: any) {
  const childCount = req.children?.length || 0
  const msg = childCount > 0
    ? `该需求下有 ${childCount} 个子需求，删除将级联删除所有子需求，确定吗？`
    : '确定删除该需求吗？'
  try {
    await ElMessageBox.confirm(msg, '提示', { type: 'warning' })
    await deleteRequirement(req.id)
    ElMessage.success('删除成功')
    selectedReq.value = null
    fetchTree()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ==================== 导出功能 ====================

function handleExport(command: string) {
  exportType.value = command as 'system' | 'func'
  // 收集候选需求
  const targetLevel = command === 'system' ? 1 : 2
  const candidates: any[] = []
  function collect(nodes: any[]) {
    for (const n of nodes) {
      if (n.level === targetLevel) candidates.push(n)
      if (n.children?.length) collect(n.children)
    }
  }
  collect(treeData.value)
  exportCandidates.value = candidates
  selectedExportIds.value = candidates.map((c: any) => c.id) // 默认全选
  exportSelectVisible.value = true
}

async function doExport() {
  exportSelectVisible.value = false
  try {
    let res: any
    if (exportType.value === 'system') {
      res = await exportSystemSpec(selectedExportIds.value)
    } else {
      res = await exportFuncSpec(selectedExportIds.value)
    }
    if (res.code === 200) {
      exportDoc.value = res.data
      exportDialogVisible.value = true
    }
  } catch (e: any) {
    ElMessage.error(e.message || '导出失败')
  }
}

function handlePrint() {
  const content = exportContentRef.value
  if (!content) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`
    <html><head><title>${exportDoc.value.title}</title>
    <style>
      body { font-family: "Microsoft YaHei", sans-serif; padding: 40px; line-height: 1.8; }
      h2 { text-align: center; }
      h3 { color: #333; border-bottom: 1px solid #eee; padding-bottom: 8px; }
      h4 { color: #666; }
      table { width: 100%; border-collapse: collapse; margin: 12px 0; }
      th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
      th { background: #f5f5f5; }
      .label-cell { width: 180px; background: #fafafa; font-weight: bold; }
    </style>
    </head><body>${content.innerHTML}</body></html>
  `)
  win.document.close()
  win.print()
}

// ==================== 生命周期 ====================

watch(searchText, (val) => {
  treeRef.value?.filter(val)
})

onMounted(() => {
  fetchTree()
})
</script>

<style scoped>
.requirements-page { padding: 20px; height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-shrink: 0; }
.page-header h2 { font-size: 24px; color: #333; margin: 0; }
.header-actions { display: flex; gap: 8px; }

.main-layout { display: flex; gap: 16px; flex: 1; min-height: 0; }

/* 左侧树 */
.tree-panel {
  width: 360px; flex-shrink: 0;
  background: #fff; border-radius: 8px; border: 1px solid #e4e7ed;
  display: flex; flex-direction: column;
}
.tree-header { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.tree-body { flex: 1; overflow-y: auto; padding: 8px; }

.tree-node-content {
  display: flex; align-items: center; gap: 6px; width: 100%; overflow: hidden;
}
.level-tag { flex-shrink: 0; }
.node-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.status-tag { flex-shrink: 0; }

/* 右侧详情 */
.detail-panel {
  flex: 1; min-width: 0;
  background: #fff; border-radius: 8px; border: 1px solid #e4e7ed;
  display: flex; flex-direction: column; overflow-y: auto;
}
.detail-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-bottom: 1px solid #f0f0f0; flex-shrink: 0;
}
.req-identity { display: flex; align-items: center; gap: 8px; }
.req-no { font-family: monospace; font-size: 13px; color: #666; }
.toolbar-actions { display: flex; gap: 6px; }

.detail-card { margin: 16px; border: none; box-shadow: none; }
.detail-card h3 { margin: 0 0 16px; font-size: 18px; color: #333; }
.desc-text { white-space: pre-wrap; color: #555; line-height: 1.6; }

.empty-detail { flex: 1; display: flex; align-items: center; justify-content: center; }

/* 优先级标签 */
.priority-tag { font-weight: bold; font-size: 12px; }
.priority-tag.p0 { color: #f56c6c; }
.priority-tag.p1 { color: #e6a23c; }
.priority-tag.p2 { color: #409eff; }
.priority-tag.p3 { color: #909399; }

/* 导出预览 */
.export-preview { max-height: 60vh; overflow-y: auto; padding: 0 16px; }
.export-table { width: 100%; border-collapse: collapse; margin: 12px 0; }
.export-table th, .export-table td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 13px; }
.export-table th { background: #f5f5f5; }
.export-table .label-cell { width: 180px; background: #fafafa; font-weight: bold; }
</style>
