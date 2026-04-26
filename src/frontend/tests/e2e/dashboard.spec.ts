import { test, expect } from '../utils/auth';

/**
 * Dashboard (工作台) E2E 测试
 * 工作台现为项目中心化布局：项目列表 + 项目详情 + IPD流程线 + 看板视图
 */

test.describe.serial('Dashboard 工作台', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(/\/(dashboard|\/)$/, { timeout: 10_000 });
    await page.goto('/dashboard');
    await page.waitForTimeout(1000);
  });

  test('欢迎区域显示用户名', async ({ page }) => {
    await expect(page.locator('.welcome-section')).toBeVisible();
    await expect(page.locator('.welcome-title')).toBeVisible();
  });

  test('统计卡片渲染', async ({ page }) => {
    const cards = page.locator('.stat-card');
    await expect(cards.first()).toBeVisible();
    await expect(cards).toHaveCount(4);
  });

  test('视图切换按钮和新建项目按钮可见', async ({ page }) => {
    await expect(page.locator('.view-toggle')).toBeVisible();
    await expect(page.locator('button:has-text("新建项目")')).toBeVisible();
  });

  test('项目列表面板可见', async ({ page }) => {
    await expect(page.locator('.project-list-panel')).toBeVisible();
    await expect(page.locator('.panel-title')).toContainText('项目列表');
  });

  test('侧边栏折叠和展开', async ({ page }) => {
    const aside = page.locator('.sidebar');
    // 点击折叠
    await page.locator('.collapse-btn').click();
    await page.waitForTimeout(500);
    await expect(aside).toHaveCSS('width', '64px');
    // 点击展开
    await page.locator('.collapse-btn').click();
    await page.waitForTimeout(500);
    await expect(aside).toHaveCSS('width', '220px');
  });

  test('切换到看板视图', async ({ page }) => {
    await page.locator('.view-toggle').locator('label:has-text("看板视图")').click();
    await page.waitForTimeout(500);
    await expect(page.locator('.kanban-board')).toBeVisible();
    await expect(page.locator('.kanban-column')).toHaveCount(5);
  });

  test('切换回详情视图', async ({ page }) => {
    // 先切看板
    await page.locator('.view-toggle').locator('label:has-text("看板视图")').click();
    await page.waitForTimeout(300);
    // 再切回详情
    await page.locator('.view-toggle').locator('label:has-text("详情视图")').click();
    await page.waitForTimeout(300);
    await expect(page.locator('.detail-layout')).toBeVisible();
  });
});
