// workbook.html → workbook.pdf (Playwright Chromium)
// 실행: NODE_PATH=$(npm root -g) node render.cjs
const { chromium } = require('playwright');
const path = require('path');

const src = path.join(__dirname, '..', 'output', 'workbook.html');
const out = process.argv[2] || path.join(__dirname, '..', 'output', 'workbook.pdf');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('file://' + src, { waitUntil: 'load' });
  await page.pdf({ path: out, format: 'A4', printBackground: true, preferCSSPageSize: true });
  await browser.close();
  console.log('wrote', out);
})();
