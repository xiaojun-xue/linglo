import { test, expect } from '../utils/auth';

/**
 * 项目管理 CRUD E2E 测试
 * 项目管理现已整合到工作台（Dashboard）页面
 * 左侧项目列表 + 右侧详情 + IPD流程线
 */

const TS = Date.now();
const PROJECT_A = `测试项目A-${TS}`;
const PROJECT_B = `测试项目B-${TS}`;

async function login(page) {
  await page.goto('/login');
  await page.fill('input[placeholder*="用户"]', 'admin');
  await page.fill('input[placeholder*="密码"]', 'admin123');
  await page.click('.login-btn');
  await page.waitForURL(/\/(dashboard|requirements)/, { timeout: 10_000 });
}

test.describe.serial('项目管理 CRUD（工作台）', () => {
  test('工作台显示项目列表和新建按钮', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1000);
    await expect(page.locator('.project-list-panel')).toBeVisible();
    await expect(page.locator('button:has-text("新建项目")')).toBeVisible();
  });

  test('空名称提交校验失败', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.click('button:has-text("新建项目")');
    await page.waitForTimeout(300);
    await page.locator('.el-dialog input[placeholder*="项目名称"]').clear();
    await page.locator('.el-dialog input[placeholder*="项目名称"]').blur();
    await page.click('.el-dialog button:has-text("创建")');
    await expect(page.locator('.el-form-item__error')).toBeVisible({ timeout: 3_000 });
    await page.click('.el-dialog button:has-text("取消")');
  });

  test('创建项目A', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.click('button:has-text("新建项目")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="项目名称"]', PROJECT_A);
    await page.fill('.el-dialog textarea[placeholder*="项目描述"]', '测试项目A描述');
    await page.waitForTimeout(500);
    await page.locator('.el-dialog button:has-text("创建")').click({ force: true });

    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.project-list-body')).toContainText(PROJECT_A);
  });

  test('创建项目B', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.click('button:has-text("新建项目")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="项目名称"]', PROJECT_B);
    await page.fill('.el-dialog textarea[placeholder*="项目描述"]', '测试项目B描述');
    await page.click('.el-dialog button:has-text("创建")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.project-list-body')).toContainText(PROJECT_B);
  });

  test('搜索项目', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    await page.fill('.search-input input', `项目B-${TS}`);
    await page.waitForTimeout(1000);
    const items = page.locator('.project-list-item');
    await expect(items).toHaveCount(1, { timeout: 5_000 });
    await expect(items.first()).toContainText(PROJECT_B);
  });

  test('选中项目显示详情和IPD流程', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    // 点击第一个项目列表项
    await page.locator('.project-list-item').first().click();
    await page.waitForTimeout(1000);
    // 应显示项目详情面板
    await expect(page.locator('.project-summary')).toBeVisible();
    // 应显示IPD流程进度条
    await expect(page.locator('.ipd-flow-section')).toBeVisible();
    await expect(page.locator('.ipd-stage')).toHaveCount(5);
  });

  test('编辑项目', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    // 搜索定位到项目A
    await page.fill('.search-input input', `项目A-${TS}`);
    await page.waitForTimeout(1000);
    // 选中项目
    await page.locator('.project-list-item').first().click();
    await page.waitForTimeout(500);
    // 点击编辑按钮
    await page.locator('.project-actions button:has-text("编辑")').click();
    await expect(page.locator('.el-dialog__title')).toContainText('编辑项目');
    const nameInput = page.locator('.el-dialog input[placeholder*="项目名称"]');
    await nameInput.fill(`${PROJECT_A}-已更新`);
    await page.click('.el-dialog button:has-text("保存")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.project-list-body')).toContainText(`${PROJECT_A}-已更新`);
  });

  test('删除项目', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    const itemsBefore = await page.locator('.project-list-item').count();
    // 选中最后一个项目
    await page.locator('.project-list-item').last().click();
    await page.waitForTimeout(500);
    // 点击删除按钮
    await page.locator('.project-actions button:has-text("删除")').click();
    // 确认对话框
    await page.locator('.el-message-box').locator('button:has-text("确定删除"), button:has-text("OK")').first().click();
    await page.waitForTimeout(1500);
    const itemsAfter = await page.locator('.project-list-item').count();
    expect(itemsAfter).toBe(itemsBefore - 1);
  });

  test('看板视图显示项目按阶段分列', async ({ page }) => {
    await login(page);
    await page.goto('/dashboard');
    await page.waitForTimeout(1500);
    // 切换到看板视图
    await page.locator('.view-toggle').locator('label:has-text("看板视图")').click();
    await page.waitForTimeout(500);
    await expect(page.locator('.kanban-board')).toBeVisible();
    // 应有5个阶段列
    await expect(page.locator('.kanban-column')).toHaveCount(5);
  });
});
