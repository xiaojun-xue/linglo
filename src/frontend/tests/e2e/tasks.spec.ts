import { test, expect } from '../utils/auth';

/**
 * 任务管理 CRUD E2E 测试
 * 依赖：需要已创建项目
 */

const TS = Date.now();
const TASK_A = `任务A-${TS}`;
const TASK_B = `任务B-${TS}`;
const TASK_C = `任务C-${TS}`;

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

/** 在 el-select 中选择指定文字的选项 */
async function selectOption(page, selectLocator, optionText: string) {
  await selectLocator.click();
  await page.waitForTimeout(500);
  await page.locator(`.el-select-dropdown__item:has-text("${optionText}"):visible`).click();
  await page.waitForTimeout(300);
}

/** 在 el-select 中选择第一个可见选项 */
async function selectFirstOption(page, selectLocator) {
  await selectLocator.click();
  await page.waitForTimeout(500);
  await page.locator('.el-select-dropdown__item:visible').first().click();
  await page.waitForTimeout(300);
}

test.describe.serial('任务管理 CRUD', () => {
  test('导航到任务管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await expect(page.locator('.page-header h2')).toContainText('任务管理');
  });

  test('创建任务A', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("创建任务")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="任务标题"]', TASK_A);

    // 优先级选"高"（index 1）
    await page.locator('.el-dialog .el-radio').nth(1).click();

    await page.fill('.el-dialog textarea[placeholder*="任务描述"]', '测试任务A描述');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText(TASK_A);
  });

  test('创建任务B-测试类型', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("创建任务")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="任务标题"]', TASK_B);
    // 类型选"测试"（任务类型是dialog中唯一的select）
    await selectOption(page, page.locator('.el-dialog .el-select').first(), '测试');

    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
  });

  test('创建任务C-设计类型', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("创建任务")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="任务标题"]', TASK_C);
    await selectOption(page, page.locator('.el-dialog .el-select').first(), '设计');

    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
  });

  test('按关键词搜索', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1000);
    await page.fill('input[placeholder*="搜索任务"]', `任务A-${TS}`);
    await page.click('.filter-card button:has-text("搜索")');
    await page.waitForTimeout(1500);
    const rows = page.locator('.el-table__body-wrapper .el-table__row');
    await expect(rows).toHaveCount(1);
    await expect(page.locator('.el-table__body-wrapper')).toContainText(TASK_A);
  });

  test('重置筛选', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1000);
    await page.fill('input[placeholder*="搜索任务"]', `任务A-${TS}`);
    await page.click('.filter-card button:has-text("搜索")');
    await page.waitForTimeout(1000);
    await page.click('.filter-card button:has-text("重置")');
    await page.waitForTimeout(1500);
    // 重置后应能看到所有任务（包括之前运行创建的）
    const rows = page.locator('.el-table__body-wrapper .el-table__row');
    const count = await rows.count();
    expect(count).toBeGreaterThanOrEqual(3);
  });

  test('编辑任务', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1500);
    // 搜索找到任务A
    await page.fill('input[placeholder*="搜索任务"]', `任务A-${TS}`);
    await page.click('.filter-card button:has-text("搜索")');
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper .el-table__row').first().locator('button:has-text("编辑")').click();
    await expect(page.locator('.el-dialog__title')).toContainText('编辑任务');
    await page.locator('.el-dialog input[placeholder*="任务标题"]').fill(`${TASK_A}-已更新`);
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText(`${TASK_A}-已更新`);
  });

  test('删除任务', async ({ page }) => {
    await login(page);
    await page.goto('/tasks');
    await page.waitForTimeout(1500);
    const rowsBefore = await page.locator('.el-table__body-wrapper .el-table__row').count();
    await page.locator('.el-table__body-wrapper .el-table__row').last().locator('button:has-text("删除")').click();
    // 确认对话框（默认英文 OK 或中文确定）
    await page.locator('.el-message-box').locator('button:has-text("OK"), button:has-text("确定")').first().click();
    await page.waitForTimeout(1500);
    const rowsAfter = await page.locator('.el-table__body-wrapper .el-table__row').count();
    expect(rowsAfter).toBe(rowsBefore - 1);
  });
});
