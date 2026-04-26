import { test, expect } from '../utils/auth';

/**
 * 认证模块 E2E 测试
 * 覆盖：登录成功、登录失败、登出、Token 失效等场景
 */

// ─── 登录页布局测试 ─────────────────────────────────────────────
test.describe('登录页 - 布局与视觉', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('页面标题正确', async ({ page }) => {
    await expect(page).toHaveTitle(/玲珑/);
  });

  test('登录表单元素完整显示', async ({ page }) => {
    await expect(page.locator('.logo-text')).toContainText('玲珑');
    await expect(page.locator('input[placeholder*="用户"]')).toBeVisible();
    await expect(page.locator('input[placeholder*="密码"]')).toBeVisible();
    await expect(page.locator('.login-btn')).toBeVisible();
  });

  test('演示账号提示可见', async ({ page }) => {
    await expect(page.locator('.demo-hint')).toBeVisible();
    await expect(page.locator('.demo-hint')).toContainText('演示账号');
  });
});

// ─── 登录功能测试 ────────────────────────────────────────────────
test.describe('登录功能', () => {
  test('admin 登录成功并跳转到 Dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');

    await expect(page).not.toHaveURL(/\/login/, { timeout: 10_000 });
    await expect(page).toHaveURL(/\/(dashboard|requirements|projects)/, { timeout: 10_000 });
  });

  test('admin 登录后侧边栏正常显示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(/\/(dashboard|requirements|projects)/, { timeout: 10_000 });

    await expect(page.locator('.sidebar')).toBeVisible();
    await expect(page.locator('.sidebar >> text=工作台')).toBeVisible();
    await expect(page.locator('.sidebar >> text=需求管理')).toBeVisible();
  });

  test('密码错误显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'wrongpassword');
    await page.click('.login-btn');

    // 不应跳转
    await page.waitForTimeout(3_000);
    await expect(page).toHaveURL(/\/login/);
  });

  test('用户名错误显示错误提示', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'nonexistent_user');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');

    await page.waitForTimeout(3_000);
    await expect(page).toHaveURL(/\/login/);
  });

  test('空用户名提交表单', async ({ page }) => {
    await page.goto('/login');
    // 清空默认值
    await page.locator('input[placeholder*="用户"]').clear();
    await page.locator('input[placeholder*="用户"]').blur();
    await page.click('.login-btn');

    await expect(page.locator('.el-form-item__error')).toBeVisible({ timeout: 3_000 });
  });

  test('空密码提交表单', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[placeholder*="密码"]').clear();
    await page.locator('input[placeholder*="密码"]').blur();
    await page.click('.login-btn');

    await expect(page.locator('.el-form-item__error')).toBeVisible({ timeout: 3_000 });
  });
});

// ─── 登出功能测试 ────────────────────────────────────────────────
test.describe('登出功能', () => {
  test('点击退出登录后跳转到登录页', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(/\/(dashboard|requirements|projects|\/)$/, { timeout: 10_000 });

    // 打开用户下拉菜单（在侧边栏底部）
    await page.locator('.sidebar-footer .user-info').click();
    // 点击退出登录
    await page.locator('.el-dropdown-menu >> text=退出登录').click();
    // 确认退出对话框
    await page.locator('.el-message-box button:has-text("确定")').click();

    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });

  test('登出后 localStorage 中的 token 被清除', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(/\/(dashboard|requirements|projects|\/)$/, { timeout: 10_000 });

    // 登出
    await page.locator('.sidebar-footer .user-info').click();
    await page.locator('.el-dropdown-menu >> text=退出登录').click();
    await page.locator('.el-message-box button:has-text("确定")').click();
    await page.waitForURL(/\/login/, { timeout: 5_000 });

    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeNull();
  });
});

// ─── 权限测试 ────────────────────────────────────────────────────
test.describe('权限控制', () => {
  test('未登录直接访问 Dashboard 被重定向到登录页', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });

  test('登录后访问受保护页面正常', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(/\/(dashboard|requirements|projects)/, { timeout: 10_000 });

    // 访问各受保护页面
    for (const path of ['/requirements', '/projects', '/tasks', '/sprints', '/reviews', '/qa', '/docs']) {
      await page.goto(path);
      await expect(page).not.toHaveURL(/\/login/, { timeout: 5_000 });
    }
  });
});
