<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="48" height="48" rx="12" fill="url(#logoGrad)"/>
              <path d="M14 16h8l4 8-4 8h-8l4-8-4-8z" fill="#fff" opacity="0.9"/>
              <path d="M26 16h8l4 8-4 8h-8l4-8-4-8z" fill="#fff" opacity="0.6"/>
              <defs>
                <linearGradient id="logoGrad" x1="0" y1="0" x2="48" y2="48">
                  <stop offset="0%" stop-color="#d69e2e"/>
                  <stop offset="100%" stop-color="#b7791f"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="logo-text">玲珑</h1>
        </div>

        <div class="brand-slogan">
          <h2>让研发管理</h2>
          <h2>更加<span class="highlight">智慧</span></h2>
          <p class="brand-desc">
            基于 IPD 体系的一站式研发管理平台<br/>
            覆盖需求到交付的全生命周期
          </p>
        </div>

        <div class="brand-features">
          <div class="feature-item" v-for="(f, i) in features" :key="i" :style="{ animationDelay: `${0.3 + i * 0.1}s` }">
            <div class="feature-icon">
              <component :is="f.icon" />
            </div>
            <div class="feature-text">
              <span class="feature-title">{{ f.title }}</span>
              <span class="feature-desc">{{ f.desc }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 装饰背景 -->
      <div class="brand-decoration">
        <div class="deco-circle deco-1"></div>
        <div class="deco-circle deco-2"></div>
        <div class="deco-line deco-3"></div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-form-area">
      <div class="login-form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录到玲珑研发管理平台</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="form-group">
            <label class="form-label">用户名</label>
            <el-form-item prop="username" class="form-item-no-margin">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <el-form-item prop="password" class="form-item-no-margin">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
              />
            </el-form-item>
          </div>

          <div class="form-options">
            <el-checkbox v-model="remember">记住登录状态</el-checkbox>
          </div>

          <el-form-item class="form-item-no-margin">
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <div class="demo-hint">
            <el-icon><InfoFilled /></el-icon>
            <span>演示账号：admin / admin123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, shallowRef } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, InfoFilled, Aim, DataLine, DocumentChecked, ChatDotSquare } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const remember = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const features = shallowRef([
  { icon: Aim, title: '需求管理', desc: '从概念到实现的完整追踪' },
  { icon: DataLine, title: '敏捷迭代', desc: 'Sprint 规划与燃尽图追踪' },
  { icon: DocumentChecked, title: '质量门禁', desc: '评审与测试全覆盖' },
  { icon: ChatDotSquare, title: '高效协作', desc: '团队实时同步与通知' }
])

async function handleLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.loginAction(loginForm.username, loginForm.password)
        if (success) {
          ElMessage.success({ message: '登录成功，欢迎回来！', duration: 2000 })
          const redirect = route.query.redirect as string || '/'
          router.push(redirect)
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* =========================================
   左侧品牌区
   ========================================= */

.login-brand {
  flex: 1;
  background: linear-gradient(145deg, var(--color-primary-dark), var(--color-primary));
  padding: 60px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 60px;
  animation: fadeInDown 0.6s ease-out;
}

.logo-icon {
  width: 56px;
  height: 56px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(214, 158, 46, 0.4));
}

.logo-text {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
}

.brand-slogan {
  margin-bottom: 60px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.brand-slogan h2 {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
  margin-bottom: 4px;
}

.brand-slogan .highlight {
  color: var(--color-accent);
  position: relative;
}

.brand-slogan .highlight::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 0;
  width: 100%;
  height: 8px;
  background: rgba(214, 158, 46, 0.3);
  border-radius: 4px;
}

.brand-desc {
  margin-top: 20px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.8;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(8px);
  animation: fadeInUp 0.5s ease-out both;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(8px);
}

.feature-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(214, 158, 46, 0.2);
  border-radius: 10px;
  color: var(--color-accent);
}

.feature-icon .el-icon {
  font-size: 20px;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

/* 装饰元素 */
.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(214, 158, 46, 0.1);
}

.deco-1 {
  width: 400px;
  height: 400px;
  top: -150px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.deco-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  left: -50px;
  animation: float 6s ease-in-out infinite 1s;
}

.deco-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, rgba(214, 158, 46, 0.2), transparent);
  height: 1px;
}

.deco-3 {
  width: 100%;
  top: 30%;
  animation: shimmerLine 3s ease-in-out infinite;
}

@keyframes shimmerLine {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* =========================================
   右侧登录表单区
   ========================================= */

.login-form-area {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: var(--color-bg);
}

.login-form-container {
  width: 100%;
  max-width: 380px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.form-header p {
  font-size: 15px;
  color: var(--color-text-secondary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-item-no-margin {
  margin-bottom: 0 !important;
}

.form-item-no-margin :deep(.el-form-item__error) {
  padding-top: 4px;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 16px;
  border-radius: 10px !important;
  box-shadow: 0 0 0 1px var(--color-border) !important;
  transition: all 0.2s ease !important;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light) !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(26, 54, 93, 0.2) !important;
}

.custom-input :deep(.el-input__inner) {
  height: 44px;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-options :deep(.el-checkbox__label) {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px !important;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)) !important;
  border: none !important;
  letter-spacing: 4px;
  transition: all 0.3s ease !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 54, 93, 0.35) !important;
}

.login-btn:active {
  transform: translateY(0);
}

.form-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
}

.demo-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(26, 54, 93, 0.04);
  border-radius: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.demo-hint .el-icon {
  color: var(--color-accent);
}

/* =========================================
   响应式
   ========================================= */

@media (max-width: 1024px) {
  .login-brand {
    display: none;
  }
  
  .login-form-area {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-form-area {
    padding: 24px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
}
</style>
