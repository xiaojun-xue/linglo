<template>
  <div class="task-board">
    <div class="board-header">
      <h3>任务看板</h3>
      <el-select v-model="currentSprintId" placeholder="选择Sprint" size="small" @change="onSprintChange" style="width:200px">
        <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
    </div>
    
    <div class="board-columns">
      <div class="board-column" 
           v-for="column in columns" 
           :key="column.key"
           @dragover.prevent
           @drop="onDrop($event, column.key)">
        <div class="column-header">
          <span class="column-title">{{ column.name }}</span>
          <span class="column-count">{{ getColumnTasks(column.key).length }}</span>
        </div>
        <div class="column-content">
          <div class="task-card"
               v-for="task in getColumnTasks(column.key)"
               :key="task.id"
               draggable="true"
               @dragstart="onDragStart($event, task)"
               @click="showTaskDetail(task)">
            <div class="task-header">
              <span class="task-no">{{ task.taskNo }}</span>
              <el-tag :type="getPriorityType(task.priority)" size="small">{{ getPriorityName(task.priority) }}</el-tag>
            </div>
            <div class="task-title">{{ task.title }}</div>
            <div class="task-footer">
              <el-avatar :size="20" v-if="task.assigneeName">{{ task.assigneeName[0] }}</el-avatar>
              <span v-if="task.dueDate" class="task-due" :class="{ overdue: isOverdue(task.dueDate) }">
                {{ task.dueDate }}
              </span>
            </div>
          </div>
          <div class="add-task-btn" @click="$emit('add-task', column.key)">
            <el-icon><Plus /></el-icon> 添加任务
          </div>
        </div>
      </div>
    </div>

    <!-- 任务详情抽屉 -->
    <el-drawer v-model="detailVisible" title="任务详情" size="500px">
      <div class="task-detail" v-if="currentTask">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务编号">{{ currentTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务标题">{{ currentTask.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ getTypeName(currentTask.type) }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentTask.priority)" size="small">{{ getPriorityName(currentTask.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-select v-model="currentTask.status" size="small" @change="updateTaskStatus">
              <el-option label="待办" value="todo" />
              <el-option label="进行中" value="in_progress" />
              <el-option label="待测试" value="in_test" />
              <el-option label="完成" value="done" />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentTask.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentTask.dueDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentTask.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskBoard, updateTaskStatus as apiUpdateStatus } from '@/api/task'
import { getSprintList } from '@/api/sprint'

const props = defineProps<{
  projectId?: number
}>()

const emit = defineEmits(['add-task', 'refresh'])

const sprints = ref<any[]>([])
const currentSprintId = ref<number | undefined>()
const boardData = ref<Record<string, any[]>>({})
const currentTask = ref<any>(null)
const detailVisible = ref(false)
const draggingTask = ref<any>(null)

const columns = [
  { key: 'todo', name: '待办' },
  { key: 'in_progress', name: '进行中' },
  { key: 'in_test', name: '待测试' },
  { key: 'done', name: '完成' }
]

function getColumnTasks(status: string) {
  return boardData.value[status] || []
}

function getPriorityName(p: number) {
  return { 0: '紧急', 1: '高', 2: '中', 3: '低' }[p] || '中'
}

function getPriorityType(p: number) {
  return { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'info' }[p] || 'info'
}

function getTypeName(type: number) {
  return { 1: '开发', 2: '测试', 3: '设计', 4: '文档', 5: '其他' }[type] || '其他'
}

function isOverdue(date: string) {
  return new Date(date) < new Date()
}

async function fetchSprints() {
  if (!props.projectId) return
  try {
    const res = await getSprintList(props.projectId)
    if (res.code === 200) {
      sprints.value = res.data || []
      // 默认选中进行中的Sprint
      const activeSprint = sprints.value.find((s: any) => s.status === 2)
      if (activeSprint) currentSprintId.value = activeSprint.id
      else if (sprints.value.length) currentSprintId.value = sprints.value[0].id
    }
  } catch (e) {
    console.error('获取Sprint失败', e)
  }
}

async function fetchBoard() {
  try {
    const res = await getTaskBoard({ projectId: props.projectId, sprintId: currentSprintId.value })
    if (res.code === 200) {
      boardData.value = res.data
    }
  } catch (e) {
    console.error('获取看板失败', e)
  }
}

function onSprintChange() {
  fetchBoard()
  emit('refresh')
}

function onDragStart(event: DragEvent, task: any) {
  draggingTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

async function onDrop(event: DragEvent, newStatus: string) {
  event.preventDefault()
  if (!draggingTask.value || draggingTask.value.status === newStatus) return
  
  try {
    await apiUpdateStatus(draggingTask.value.id, newStatus)
    ElMessage.success('状态已更新')
    fetchBoard()
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  }
  draggingTask.value = null
}

function showTaskDetail(task: any) {
  currentTask.value = { ...task }
  detailVisible.value = true
}

async function updateTaskStatus() {
  if (!currentTask.value) return
  try {
    await apiUpdateStatus(currentTask.value.id, currentTask.value.status)
    ElMessage.success('状态已更新')
    fetchBoard()
    detailVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  }
}

watch(() => props.projectId, () => {
  fetchSprints()
}, { immediate: true })

watch(currentSprintId, () => {
  if (currentSprintId.value) fetchBoard()
})
</script>

<style scoped>
.task-board {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.board-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.board-columns {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  min-height: 400px;
}

.board-column {
  flex: 1;
  min-width: 220px;
  max-width: 280px;
  background: #eee;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.column-header {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ddd;
}

.column-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.column-count {
  background: #ccc;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.column-content {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
}

.task-card {
  background: #fff;
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.2s;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transform: translateY(-1px);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.task-no {
  font-size: 12px;
  color: #999;
}

.task-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-due {
  font-size: 11px;
  color: #666;
}

.task-due.overdue {
  color: #f56c6c;
}

.add-task-btn {
  padding: 8px;
  text-align: center;
  color: #999;
  cursor: pointer;
  font-size: 13px;
  border-radius: 4px;
  border: 1px dashed #ccc;
}

.add-task-btn:hover {
  background: #e4e7ed;
  color: #409eff;
}
</style>
