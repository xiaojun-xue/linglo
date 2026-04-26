import { test, expect } from '../utils/auth';

/**
 * Sprint 管理 E2E 测试
 * 依赖：projects.spec.ts 已创建项目
 */

async function login(page) {
  await page.goto('/login');
  await page.fill('input[placeholder*="用户"]', 'admin');
  await page.fill('input[placeholder*="密码"]', 'admin123');
  await page.click('.login-btn');
  await page.waitForURL(/\/(dashboard|requirements)/, { timeout: 10_000 });
  await ensureProjectSelected(page);
}

async function ensureProjectSelected(page) {
  const hasProject = await page.evaluate(() => !!localStorage.getItem('selectedProjectId'));
  if (!hasProject) {
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    const item = page.locator('.project-list-item').first();
    if (await item.isVisible()) {
      await item.click();
      await page.waitForTimeout(500);
    }
  }
}

test.describe.serial('Sprint 管理', () => {
  test('导航到Sprint管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await expect(page.locator('.page-header h2')).toContainText('Sprint 管理');
  });

  test('创建Sprint', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);

    await page.click('button:has-text("创建 Sprint")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="Sprint 1"]', 'Sprint 1');
    await page.fill('.el-dialog textarea[placeholder*="Sprint目标"]', '完成核心登录功能');

    const dateInputs = page.locator('.el-dialog .el-date-editor input');
    await dateInputs.nth(0).fill('2026-04-15');
    await dateInputs.nth(0).press('Enter');
    await dateInputs.nth(1).fill('2026-04-30');
    await dateInputs.nth(1).press('Enter');

    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('Sprint 1');
  });

  test('Sprint状态为规划中', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('规划');
  });

  test('开始Sprint', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);
    await page.locator('.el-table__body-wrapper button:has-text("开始")').click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('进行');
  });

  test('当前Sprint详情卡片可见', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);
    await expect(page.locator('text=当前 Sprint 详情')).toBeVisible({ timeout: 5_000 });
  });

  test('完成Sprint', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);
    await page.locator('.el-table__body-wrapper button:has-text("完成")').click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('已完');
  });

  test('关闭Sprint', async ({ page }) => {
    await login(page);
    await page.goto('/sprints');
    await page.waitForTimeout(2000);
    await page.locator('.el-table__body-wrapper button:has-text("关闭")').click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('已关');
  });
});
