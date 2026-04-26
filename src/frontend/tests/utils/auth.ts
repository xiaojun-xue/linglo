import { test as base, Page, APIRequestContext } from '@playwright/test';

export * from '@playwright/test';

/**
 * 自定义测试 fixture，包含已登录的页面上下文
 */
export const test = base.extend<{
  authenticatedPage: Page;
  api: APIRequestContext;
  testPrefix: string;
}>({
  // 每次测试前自动登录
  authenticatedPage: async ({ page, baseURL }, use) => {
    await page.goto(`${baseURL}/login`);
    await page.fill('input[placeholder*="用户"]', 'admin');
    await page.fill('input[placeholder*="密码"]', 'admin123');
    await page.click('.login-btn');
    await page.waitForURL(`${baseURL}/**`, { timeout: 10_000 });
    await use(page);
  },

  // API 请求上下文（自动携带 token）
  api: async ({ request }, use) => {
    const ctx = await request.newContext({
      baseURL: 'http://localhost:3000/api',
    });
    // 先登录获取 token
    const loginRes = await ctx.post('/auth/login', {
      data: { username: 'admin', password: 'admin123' },
    });
    const loginData = await loginRes.json();
    const token = loginData?.data?.accessToken;
    if (token) {
      await ctx.storageState({
        tokens: { accessToken: token },
      });
    }
    await use(ctx);
    await ctx.dispose();
  },

  // 统一测试数据前缀，方便清理
  testPrefix: async ({}, use) => {
    const prefix = `e2e_${Date.now()}_`;
    await use(prefix);
  },
});

/** 等待 Element Plus 加载完成 */
export async function waitForElementPlus(page: Page) {
  await page.waitForSelector('.el-loading-mask', { state: 'hidden', timeout: 10_000 }).catch(() => {});
  await page.waitForTimeout(500);
}

/** 截取全页面截图 */
export async function takeScreenshot(page: Page, name: string) {
  await page.screenshot({ path: `tests/screenshots/${name}.png`, fullPage: false });
}
