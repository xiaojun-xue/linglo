<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 添加用户
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="用户名/昵称/邮箱" clearable @clear="fetchUsers" @keyup.enter="fetchUsers" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable @change="fetchUsers">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="content-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :style="{ background: getAvatarColor(row.username) }">
                {{ row.username?.[0]?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roles" label="角色" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.roleCode" :type="getRoleTagType(role.roleCode)" size="small" class="role-tag">
              {{ role.roleName }}
            </el-tag>
            <span v-if="!row.roles?.length" class="no-role">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDate(row.lastLoginTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="showRoleDialog(row)">角色</el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="userDialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="500px" destroy-on-close>
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="显示昵称" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password placeholder="初始密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUserSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px" destroy-on-close>
      <div class="role-assign-info" v-if="selectedUser">
        <el-avatar :size="48" :style="{ background: getAvatarColor(selectedUser.username) }">
          {{ selectedUser.username?.[0]?.toUpperCase() }}
        </el-avatar>
        <span class="assign-username">{{ selectedUser.username }}</span>
      </div>
      <el-divider />
      <el-form label-width="80px">
        <el-form-item label="选择角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox v-for="role in allRoles" :key="role.roleCode" :label="role.roleCode" class="role-checkbox">
              <span class="role-name">{{ role.roleName }}</span>
              <span class="role-desc">{{ role.description }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getRoleList } from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const users = ref<any[]>([])
const allRoles = ref<any[]>([])

const filters = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 20, total: 0 })

// 用户表单
const userDialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref<FormInstance>()
const currentUserId = ref<number | null>(null)
const userForm = reactive({
  username: '',
  nickname: '',
  password: '',
  email: '',
  phone: '',
  status: 1
})
const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }]
}

// 角色分配
const roleDialogVisible = ref(false)
const selectedUser = ref<any>(null)
const selectedRoles = ref<string[]>([])

function formatDate(date: string): string {
  return new Date(date).toLocaleString('zh-CN')
}

function getAvatarColor(username: string): string {
  const colors = ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#ec4899']
  const idx = username.charCodeAt(0) % colors.length
  return colors[idx]
}

function getRoleTagType(roleCode: string): string {
  const map: Record<string, string> = {
    ADMIN: 'danger',
    PRODUCT_MANAGER: 'warning',
    PROJECT_MANAGER: 'primary',
    TECH_LEAD: 'success',
    DEVELOPER: 'info',
    QA_MANAGER: 'warning',
    QA: 'info',
    OPS: '',
    EXECUTIVE: 'danger',
    VIEWER: 'info'
  }
  return map[roleCode] || ''
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({ page: pagination.page - 1, size: pagination.size, keyword: filters.keyword })
    if (res.code === 200) {
      users.value = res.data.content || []
      pagination.total = res.data.totalElements || 0
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      allRoles.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  pagination.page = 1
  fetchUsers()
}

function showCreateDialog() {
  isEdit.value = false
  currentUserId.value = null
  Object.assign(userForm, { username: '', nickname: '', password: '', email: '', phone: '', status: 1 })
  userDialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  currentUserId.value = row.id
  Object.assign(userForm, {
    username: row.username,
    nickname: row.nickname,
    password: '',
    email: row.email,
    phone: row.phone,
    status: row.status
  })
  userDialogVisible.value = true
}

function showRoleDialog(row: any) {
  selectedUser.value = row
  selectedRoles.value = row.roles?.map((r: any) => r.roleCode) || []
  roleDialogVisible.value = true
}

async function handleUserSubmit() {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && currentUserId.value) {
          await updateUser(currentUserId.value, {
            nickname: userForm.nickname,
            email: userForm.email,
            phone: userForm.phone,
            status: userForm.status
          })
          ElMessage.success('更新成功')
        } else {
          await createUser(userForm)
          ElMessage.success('创建成功')
        }
        userDialogVisible.value = false
        fetchUsers()
      } catch (e: any) {
        ElMessage.error(e.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

async function handleRoleSubmit() {
  if (!selectedUser.value) return
  submitting.value = true
  try {
    // 通过更新用户角色
    await updateUser(selectedUser.value.id, { roleCodes: selectedRoles.value })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.users-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; color: #333; margin: 0; }
.filter-card { margin-bottom: 16px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.user-cell { display: flex; align-items: center; gap: 10px; }
.username { font-weight: 500; }

.role-tag { margin-right: 4px; margin-bottom: 4px; }
.no-role { color: #999; font-size: 12px; }

.role-assign-info { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.assign-username { font-weight: 600; font-size: 16px; }

.role-checkbox {
  display: flex;
  align-items: flex-start;
  width: 100%;
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
}
.role-checkbox:hover { background: #f5f7fa; }
.role-name { font-weight: 600; display: block; }
.role-desc { color: #999; font-size: 12px; display: block; }
</style>
