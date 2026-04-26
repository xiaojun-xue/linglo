import { test, expect } from '../utils/auth';

/**
 * 评审管理 E2E 测试
 * 覆盖：CDCP/PDCP/TR4/ADCP 评审的创建和状态流转
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

test.describe.serial('评审管理', () => {
  test('导航到评审管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await expect(page.locator('.page-header h2')).toContainText('评审管理');
  });

  test('发起CDCP评审', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.click('button:has-text("发起评审")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="评审标题"]', 'v2.0 概念决策评审');
    // 类型默认CDCP，项目自动从store关联

    await page.click('.el-dialog button:has-text("创建")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('v2.0 概念决策评审');
  });

  test('评审状态为草稿', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('草稿');
  });

  test('提交评审', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("提交")').click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('待评审');
  });

  test('开始评审', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("开始")').click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('评审中');
  });

  test('评审决策-通过', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("决策")').click();
    await page.waitForTimeout(300);

    // 默认"通过"已选中
    await page.fill('.el-dialog textarea[placeholder*="评审结论"]', '概念方案通过，可进入计划阶段');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-table__body-wrapper')).toContainText('通过');
  });

  test('查看评审详情', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("详情")').first().click();
    await expect(page.locator('.el-dialog__title')).toContainText('评审详情');
    await expect(page.locator('.el-dialog')).toContainText('CDCP');
    await page.click('.el-dialog button:has-text("关闭")');
  });

  test('筛选评审类型', async ({ page }) => {
    await login(page);
    await page.goto('/reviews');
    await page.waitForTimeout(1000);
    // 按类型筛选CDCP
    const typeSelect = page.locator('.filter-card .el-select').first();
    await typeSelect.click();
    await page.waitForTimeout(300);
    await page.locator('.el-select-dropdown__item:has-text("CDCP"):visible').click();
    await page.waitForTimeout(300);
    await page.click('.filter-card button:has-text("搜索")');
    await page.waitForTimeout(1500);
    // 应看到CDCP的评审
    await expect(page.locator('.el-table__body-wrapper')).toContainText('CDCP');
  });
});
