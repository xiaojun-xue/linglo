import { test, expect } from '../utils/auth';

/**
 * 通知系统 + 个人中心 + 退出登录 E2E 测试
 */

async function login(page) {
  await page.goto('/login');
  await page.fill('input[placeholder*="用户"]', 'admin');
  await page.fill('input[placeholder*="密码"]', 'admin123');
  await page.click('.login-btn');
  await page.waitForURL(/\/(dashboard|\/)$/, { timeout: 10_000 });
}

test.describe.serial('个人中心', () => {
  test('从下拉菜单进入个人设置', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(1000);
    // 点击侧边栏底部用户信息
    await page.locator('.sidebar-footer .user-info').click();
    await page.locator('.el-dropdown-menu >> text=个人设置').click();
    await expect(page).toHaveURL(/\/profile/, { timeout: 5_000 });
  });

  test('个人中心显示用户信息', async ({ page }) => {
    await login(page);
    await page.goto('/profile');
    await page.waitForTimeout(1500);
    // 页面应包含用户名信息
    await expect(page.locator('body')).toContainText('admin');
  });
});

test.describe.serial('退出登录', () => {
  test('点击退出登录跳转到登录页', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(1000);
    await page.locator('.sidebar-footer .user-info').click();
    await page.locator('.el-dropdown-menu >> text=退出登录').click();
    // 确认退出对话框
    await page.locator('.el-message-box button:has-text("确定")').click();
    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });

  test('退出后token被清除', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(1000);
    await page.locator('.sidebar-footer .user-info').click();
    await page.locator('.el-dropdown-menu >> text=退出登录').click();
    await page.locator('.el-message-box button:has-text("确定")').click();
    await page.waitForURL(/\/login/, { timeout: 5_000 });
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeNull();
  });

  test('退出后访问受保护页面被重定向', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });
});
