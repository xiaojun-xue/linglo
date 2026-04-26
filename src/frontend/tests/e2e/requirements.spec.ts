import { test, expect } from '../utils/auth';

/**
 * 需求管理 E2E 测试
 * 覆盖：IR/SR/AR 层级创建、5W2H结构化描述、状态流转、导出
 */

const TS = Date.now();
const IR_TITLE = `用户登录场景_${TS}`;
const SR_TITLE = `登录验证码功能_${TS}`;
const AR_TITLE = `验证码UI组件_${TS}`;

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

test.describe.serial('需求管理 - IR/SR/AR层级', () => {
  test('导航到需求管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await expect(page.locator('.page-header h2')).toContainText('需求管理');
  });

  test('新建IR按钮可见', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await expect(page.locator('button:has-text("新建IR")')).toBeVisible();
  });

  test('创建IR需求（含5W2H）', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.click('button:has-text("新建IR")');
    await page.waitForTimeout(300);

    // 对话框应显示 IR 层级标签
    await expect(page.locator('.el-dialog')).toContainText('IR');

    // 填写标题
    await page.fill('.el-dialog input[placeholder*="需求标题"]', IR_TITLE);

    // 5W2H 字段
    await page.fill('.el-dialog input[placeholder*="目标用户"]', '普通注册用户');
    await page.fill('.el-dialog textarea[placeholder*="需要什么功能"]', '安全的登录验证流程');
    await page.fill('.el-dialog input[placeholder*="时间要求"]', '2026年Q2');
    await page.fill('.el-dialog input[placeholder*="使用场景"]', 'Web端和移动端');
    await page.fill('.el-dialog textarea[placeholder*="业务动机"]', '提升账户安全性，降低被盗风险');
    await page.fill('.el-dialog textarea[placeholder*="实现方式"]', '图形验证码+短信验证码双因素');
    await page.fill('.el-dialog input[placeholder*="规模"]', '影响所有用户登录流程');

    // 其他信息
    await page.fill('.el-dialog textarea[placeholder*="需求描述"]', '用户登录时增加验证码验证');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);

    // 树中应出现IR
    await expect(page.locator('.el-tree')).toContainText(IR_TITLE);
  });

  test('点击IR节点显示5W2H详情', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    // 点击IR节点
    await page.locator('.el-tree-node:has-text("' + IR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    // 应显示5W2H结构化描述
    await expect(page.locator('.detail-panel')).toContainText('5W2H');
    await expect(page.locator('.detail-panel')).toContainText('普通注册用户');
    await expect(page.locator('.detail-panel')).toContainText('提升账户安全性');
  });

  test('从IR分解SR', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    // 选中IR
    await page.locator('.el-tree-node:has-text("' + IR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    // 点击分解SR按钮
    await page.click('button:has-text("分解SR")');
    await page.waitForTimeout(300);

    // 对话框应标示SR层级
    await expect(page.locator('.el-dialog')).toContainText('SR');

    await page.fill('.el-dialog input[placeholder*="需求标题"]', SR_TITLE);
    await page.fill('.el-dialog textarea[placeholder*="需求描述"]', '实现图形验证码生成和校验');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);

    // 树中应出现SR作为IR的子节点
    await expect(page.locator('.el-tree')).toContainText(SR_TITLE);
  });

  test('从SR分解AR', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    // 选中SR
    await page.locator('.el-tree-node:has-text("' + SR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    // 点击分解AR按钮
    await page.click('button:has-text("分解AR")');
    await page.waitForTimeout(300);

    await page.fill('.el-dialog input[placeholder*="需求标题"]', AR_TITLE);
    await page.fill('.el-dialog textarea[placeholder*="需求描述"]', '开发验证码输入组件和样式');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);

    await expect(page.locator('.el-tree')).toContainText(AR_TITLE);
  });

  test('需求状态推进：新增→评审中', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    // 选中IR
    await page.locator('.el-tree-node:has-text("' + IR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    // 点击推进状态按钮
    await page.click('button:has-text("推进到")');
    await page.waitForTimeout(1500);

    // 应显示评审中
    await expect(page.locator('.detail-panel')).toContainText('评审中');
  });

  test('需求状态推进：评审中→已归档', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    await page.locator('.el-tree-node:has-text("' + IR_TITLE + '")').first().click();
    await page.waitForTimeout(500);
    await page.click('button:has-text("推进到")');
    await page.waitForTimeout(1500);

    await expect(page.locator('.detail-panel')).toContainText('已归档');
  });

  test('导出说明书按钮可见', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await expect(page.locator('button:has-text("导出说明书")')).toBeVisible();
  });

  test('编辑IR需求', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    await page.locator('.el-tree-node:has-text("' + IR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    await page.locator('.detail-toolbar button:has-text("编辑")').click();
    await page.waitForTimeout(300);

    const titleInput = page.locator('.el-dialog input[placeholder*="需求标题"]');
    await titleInput.fill(IR_TITLE + '-已更新');
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);

    await expect(page.locator('.el-tree')).toContainText(IR_TITLE + '-已更新');
  });

  test('删除AR需求', async ({ page }) => {
    await login(page);
    await page.goto('/requirements');
    await page.waitForTimeout(1500);

    await page.locator('.el-tree-node:has-text("' + AR_TITLE + '")').first().click();
    await page.waitForTimeout(500);

    await page.locator('.detail-toolbar button:has-text("删除")').click();
    await page.locator('.el-message-box').locator('button:has-text("OK"), button:has-text("确定")').first().click();
    await page.waitForTimeout(1500);

    // AR应该从树中消失
    await expect(page.locator('.el-tree')).not.toContainText(AR_TITLE);
  });
});
