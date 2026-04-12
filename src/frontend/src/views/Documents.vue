<template>
  <div class="docs-page">
    <div class="page-header">
      <h2>文档管理</h2>
      <div>
        <el-button @click="showFolderDialog()"><el-icon><FolderAdd /></el-icon>新建文件夹</el-button>
        <el-button type="primary" @click="showDocDialog()"><el-icon><Document /></el-icon>新建文档</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="tree-card">
          <template #header><span>文档目录</span></template>
          <el-tree :data="docTree" node-key="id" :props="{label:'title',children:'children'}" default-expand-all @node-click="selectDoc">
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon v-if="data.isFolder"><Folder /></el-icon>
                <el-icon v-else><Document /></el-icon>
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="content-card">
          <template #header>
            <span>{{ currentDoc?.title || '文档列表' }}</span>
            <el-button v-if="currentDoc && !currentDoc.isFolder" type="primary" link @click="editDoc">编辑</el-button>
          </template>
          
          <div v-if="!currentDoc" class="empty-state">
            <el-empty description="请从左侧选择文档" />
          </div>
          
          <div v-else-if="currentDoc.isFolder" class="folder-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="文件夹名称">{{ currentDoc.title }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ currentDoc.creatorName || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div v-else class="doc-content">
            <el-descriptions :column="2" border size="small" class="mb-16">
              <el-descriptions-item label="文档标题">{{ currentDoc.title }}</el-descriptions-item>
              <el-descriptions-item label="分类">{{ getCategoryName(currentDoc.category) }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ currentDoc.creatorName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ currentDoc.createdAt }}</el-descriptions-item>
            </el-descriptions>
            <el-divider />
            <div class="doc-body" v-html="currentDoc.content || '<p style=\'color:#999\'>暂无内容</p>'"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文档对话框 -->
    <el-dialog v-model="docDialogVisible" :title="editingDoc?.id ? '编辑文档' : '新建文档'" width="700px">
      <el-form ref="docFormRef" :model="docForm" label-width="100px">
        <el-form-item label="文档标题" required><el-input v-model="docForm.title" /></el-form-item>
        <el-form-item label="文档分类"><el-select v-model="docForm.category"><el-option label="项目文档" :value="1" /><el-option label="需求文档" :value="2" /><el-option label="技术文档" :value="3" /><el-option label="流程文档" :value="4" /><el-option label="模板" :value="5" /><el-option label="其他" :value="6" /></el-select></el-form-item>
        <el-form-item label="文档内容"><el-input v-model="docForm.content" type="textarea" :rows="10" placeholder="支持 Markdown 格式" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="docDialogVisible=false">取消</el-button><el-button type="primary" @click="handleSaveDoc" :loading="docSaving">确定</el-button></template>
    </el-dialog>

    <!-- 文件夹对话框 -->
    <el-dialog v-model="folderDialogVisible" title="新建文件夹" width="400px">
      <el-form ref="folderFormRef" :model="folderForm"><el-form-item label="文件夹名称" required><el-input v-model="folderForm.title" /></el-form-item></el-form>
      <template #footer><el-button @click="folderDialogVisible=false">取消</el-button><el-button type="primary" @click="handleSaveFolder" :loading="folderSaving">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocTree, getDocById, createDoc, createDocFolder, updateDoc } from '@/api/doc'

const docTree = ref<any[]>([])
const currentDoc = ref<any>(null)
const docDialogVisible = ref(false)
const folderDialogVisible = ref(false)
const docSaving = ref(false)
const folderSaving = ref(false)
const editingDoc = ref<any>(null)
const docFormRef = ref()
const folderFormRef = ref()
const docForm = reactive({ title: '', category: 1, content: '' })
const folderForm = reactive({ title: '' })

function getCategoryName(c: number) {
  return { 1: '项目文档', 2: '需求文档', 3: '技术文档', 4: '流程文档', 5: '模板', 6: '其他' }[c] || '其他'
}

async function fetchDocTree() {
  try {
    const res = await getDocTree()
    if (res.code === 200) docTree.value = res.data || []
  } catch (e) { console.error(e) }
}

async function selectDoc(data: any) {
  if (data.isFolder) {
    currentDoc.value = data
  } else {
    try {
      const res = await getDocById(data.id)
      if (res.code === 200) currentDoc.value = res.data
    } catch (e) { console.error(e) }
  }
}

function showDocDialog(row?: any) {
  editingDoc.value = row || null
  if (row) Object.assign(docForm, { title: row.title, category: row.category, content: row.content })
  else Object.assign(docForm, { title: '', category: 1, content: '' })
  docDialogVisible.value = true
}

function showFolderDialog() {
  folderForm.title = ''
  folderDialogVisible.value = true
}

function editDoc() {
  if (currentDoc.value) showDocDialog(currentDoc.value)
}

async function handleSaveDoc() {
  if (!docForm.title) { ElMessage.warning('请输入文档标题'); return }
  docSaving.value = true
  try {
    if (editingDoc.value?.id) {
      await updateDoc(editingDoc.value.id, docForm)
    } else {
      await createDoc(docForm)
    }
    ElMessage.success('保存成功')
    docDialogVisible.value = false
    fetchDocTree()
    if (currentDoc.value?.id === editingDoc.value?.id) selectDoc(currentDoc.value)
  } catch (e: any) { ElMessage.error(e.message) }
  finally { docSaving.value = false }
}

async function handleSaveFolder() {
  if (!folderForm.title) { ElMessage.warning('请输入文件夹名称'); return }
  folderSaving.value = true
  try {
    await createDocFolder({ title: folderForm.title, isFolder: true })
    ElMessage.success('创建成功')
    folderDialogVisible.value = false
    fetchDocTree()
  } catch (e: any) { ElMessage.error(e.message) }
  finally { folderSaving.value = false }
}

onMounted(() => { fetchDocTree() })
</script>

<style scoped>
.docs-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; margin: 0; }
.tree-card { height: calc(100vh - 200px); overflow-y: auto; }
.content-card { min-height: calc(100vh - 200px); }
.tree-node { display: flex; align-items: center; gap: 6px; }
.empty-state { padding: 60px 0; }
.doc-body { line-height: 1.8; color: #333; }
.doc-body p { margin: 12px 0; }
.mb-16 { margin-bottom: 16px; }
</style>
