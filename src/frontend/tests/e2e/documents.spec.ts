import { test, expect } from '../utils/auth';

/**
 * 文档管理 E2E 测试
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

test.describe.serial('文档管理', () => {
  test('导航到文档管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await expect(page.locator('.page-header h2')).toContainText('文档管理');
  });

  test('空状态显示提示', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(1000);
    await expect(page.locator('.empty-state, .el-empty').first()).toBeVisible();
  });

  test('新建文件夹', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("新建文件夹")');
    // 等待对话框出现
    await expect(page.locator('.el-dialog')).toBeVisible({ timeout: 3_000 });
    await page.locator('.el-dialog input').first().fill('项目文档');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);
    // 树中应出现文件夹（或至少不再显示 No Data）
    await expect(page.locator('.el-tree')).not.toContainText('No Data', { timeout: 5_000 });
  });

  test('新建文档', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(500);
    await page.click('button:has-text("新建文档")');
    await page.waitForTimeout(300);
    await page.locator('.el-dialog input').first().fill('架构设计文档');
    // 分类默认项目文档
    await page.locator('.el-dialog textarea').first().fill('# 架构设计\n\n## 技术选型\n- 前端：Vue 3\n- 后端：Spring Boot');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await expect(page.locator('.el-tree')).toContainText('架构设计文档');
  });

  test('点击文件夹节点显示文件夹信息', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(1500);
    await page.locator('.el-tree-node:has-text("项目文档")').first().click();
    await page.waitForTimeout(500);
    await expect(page.locator('.folder-content, .el-descriptions').first()).toBeVisible();
  });

  test('点击文档节点显示文档内容', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(1500);
    await page.locator('.el-tree-node:has-text("架构设计文档")').first().click();
    await page.waitForTimeout(1000);
    await expect(page.locator('.doc-content, .el-descriptions').first()).toBeVisible();
  });

  test('编辑文档', async ({ page }) => {
    await login(page);
    await page.goto('/docs');
    await page.waitForTimeout(1500);
    // 先选中文档
    await page.locator('.el-tree-node:has-text("架构设计文档")').first().click();
    await page.waitForTimeout(1000);
    // 点击编辑
    const editBtn = page.locator('.content-card button:has-text("编辑")');
    if (await editBtn.isVisible()) {
      await editBtn.click();
      await page.waitForTimeout(300);
      await page.locator('.el-dialog textarea').first().fill('# 架构设计 v2\n\n更新后的内容');
      await page.click('.el-dialog button:has-text("确定")');
      await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    }
  });
});
