<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card class="content-card">
      <el-descriptions title="个人信息" :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.userInfo?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ userStore.userInfo?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in userStore.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userStore.userInfo?.status === 1 ? 'success' : 'danger'" size="small">
            {{ userStore.userInfo?.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  if (!userStore.userInfo?.username) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.profile-page { padding: 20px; }
.profile-page h2 { font-size: 24px; margin-bottom: 20px; color: #333; }
</style>
