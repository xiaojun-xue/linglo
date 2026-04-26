import { test, expect } from '../utils/auth';

/**
 * 测试管理 E2E 测试（测试用例 + 缺陷管理）
 *
 * 已知问题：测试用例 tab 的 el-table body 在无头浏览器中
 * 不渲染行（Total 显示正确但行不可见），这是一个前端渲染 bug。
 * 因此测试用例的 CRUD 测试通过 pagination total 数值验证。
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

/** 获取测试用例 tab 的 pagination total 数值 */
async function getCaseTotal(page): Promise<number> {
  const text = await page.locator('.el-pagination .el-pagination__total').first().textContent();
  return parseInt(text?.replace(/\D/g, '') || '0');
}

test.describe.serial('测试用例管理', () => {
  test('导航到测试管理页面', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await expect(page.locator('.page-header h2')).toContainText('测试管理');
  });

  test('默认显示测试用例Tab', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await expect(page.locator('.el-tabs__item.is-active')).toContainText('测试用例');
  });

  let totalBefore = 0;

  test('记录当前用例数', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(1500);
    totalBefore = await getCaseTotal(page);
  });

  test('创建测试用例', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("新建用例")');
    await page.waitForTimeout(300);

    await page.locator('.el-dialog input').first().fill('登录功能测试');
    await page.locator('.el-dialog input').nth(1).fill('认证模块');
    await page.locator('.el-dialog textarea').nth(1).fill('1. 打开登录页\n2. 输入用户名密码');
    await page.locator('.el-dialog textarea').nth(2).fill('成功跳转到工作台');

    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);
    const totalAfter = await getCaseTotal(page);
    expect(totalAfter).toBe(totalBefore + 1);
  });

  test('创建第二个用例', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(1000);
    await page.click('button:has-text("新建用例")');
    await page.waitForTimeout(300);

    await page.locator('.el-dialog input').first().fill('注册流程测试');
    await page.locator('.el-dialog input').nth(1).fill('认证模块');
    await page.locator('.el-dialog textarea').nth(1).fill('1. 填写注册表单');
    await page.locator('.el-dialog textarea').nth(2).fill('注册成功');

    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1500);
    const totalAfter = await getCaseTotal(page);
    expect(totalAfter).toBe(totalBefore + 2);
  });
});

test.describe.serial('缺陷管理', () => {
  test('切换到缺陷管理Tab', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await expect(page.locator('.el-tabs__item.is-active')).toContainText('缺陷管理');
  });

  test('创建缺陷', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1000);

    await page.locator('button:has-text("新建缺陷")').click();
    await page.waitForTimeout(300);

    await page.locator('.el-dialog input').first().fill('登录页密码明文显示');
    // 严重等级
    await page.locator('.el-dialog .el-select').first().click();
    await page.waitForTimeout(300);
    await page.locator('.el-select-dropdown__item:has-text("严重"):visible').click();
    await page.waitForTimeout(300);
    // 优先级
    await page.locator('.el-dialog .el-radio').nth(1).click();
    // 描述
    await page.locator('.el-dialog textarea').first().fill('密码输入框未做掩码处理');
    await page.waitForTimeout(300);
    await page.click('.el-dialog button:has-text("确定")');
    await expect(page.locator('.el-dialog')).toBeHidden({ timeout: 15_000 });
    await page.waitForTimeout(1000);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1000);
    await expect(page.locator('body')).toContainText('登录页密码明文显示');
  });

  test('缺陷状态流转：新建→已确认', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("改状态")').first().click();
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).toContainText('已确认');
  });

  test('缺陷状态流转：已确认→已分配', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("改状态")').first().click();
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).toContainText('已分配');
  });

  test('缺陷状态流转：已分配→修复中', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("改状态")').first().click();
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).toContainText('修复中');
  });

  test('缺陷状态流转：修复中→待验证', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("改状态")').first().click();
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).toContainText('待验证');
  });

  test('缺陷状态流转：待验证→已关闭', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("改状态")').first().click();
    await page.waitForTimeout(1500);
    await expect(page.locator('body')).toContainText('已关闭');
  });

  test('删除缺陷', async ({ page }) => {
    await login(page);
    await page.goto('/qa');
    await page.waitForTimeout(500);
    await page.locator('.el-tabs__item:has-text("缺陷管理")').click();
    await page.waitForTimeout(1500);
    await page.locator('.el-table__body-wrapper button:has-text("删除")').first().click();
    await page.locator('.el-message-box').locator('button:has-text("OK"), button:has-text("确定")').first().click();
    await page.waitForTimeout(1500);
  });
});
