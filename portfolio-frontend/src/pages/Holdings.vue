<template>
  <div class="apple-layout">
    <div class="ambient-bg blob-1"></div>
    <div class="ambient-bg blob-2"></div>

    <aside class="glass-sidebar animate-fade-in">
      <div class="brand">
        <h2 class="brand-title">PRO-GRAM</h2>
      </div>

      <nav class="nav-menu">
        <router-link to="/" class="nav-item" active-class="active" exact>
          <span class="text-truncate">Dashboard</span>
        </router-link>
        
        <router-link to="/holdings" class="nav-item" active-class="active">
          <span class="text-truncate">Holdings</span>
        </router-link>
        
        <a href="#" class="nav-item"><span class="text-truncate">Transactions</span></a>
        <a href="#" class="nav-item"><span class="text-truncate">Reports</span></a>
      </nav>

      <div class="user-profile">
        <img src="https://ui-avatars.com/api/?name=William+Zhang&background=ffffff&color=000000" alt="Avatar" class="avatar" />
        <div class="user-info text-truncate">
          <div class="name text-truncate">William Zhang</div>
          <div class="type text-truncate">VIP Client</div>
        </div>
      </div>
    </aside>

    <main class="main-content">
      
      <header class="top-header animate-fade-in delay-1">
        <div class="header-titles">
          <h1 class="page-title text-truncate">Portfolio Holdings</h1>
          <p class="datetime text-truncate">All Accounts • USD</p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-secondary text-truncate">Export CSV</button>
          <button class="apple-btn btn-primary text-truncate">Add Position</button>
        </div>
      </header>

      <div class="glass-card table-card animate-fade-in delay-2">
        <div class="card-header">
          <h3 class="text-truncate">Detailed Positions</h3>
        </div>
        
        <div class="table-responsive">
          <table class="apple-table">
            <thead>
              <tr>
                <th class="col-asset">Asset</th>
                <th class="right col-num">Shares</th>
                <th class="right col-num">Avg Cost</th>
                <th class="right col-num">Price</th>
                <th class="right col-value">Market Value</th>
                <th class="right col-return">Total Return</th>
                <th class="center col-bar">Performance</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="asset in enrichedHoldings" :key="asset.ticker" class="table-row">
                <td>
                  <div class="asset-identity">
                    <div class="asset-text-container">
                      <div class="asset-name">{{ asset.ticker }}</div>
                      <div class="asset-desc">{{ asset.name }}</div>
                    </div>
                  </div>
                </td>
                
                <td class="right">{{ asset.quantity }}</td>
                <td class="right text-muted">{{ asset.cost }}</td>
                <td class="right">{{ asset.price }}</td>
                <td class="right font-medium">{{ asset.marketValue }}</td>
                <td class="right">
                  <div class="return-data">
                    <span :class="asset.pnl > 0 ? 'text-green' : 'text-red'">
                      {{ asset.pnl > 0 ? '+' : '' }}{{ asset.pnl }}%
                    </span>
                    <span class="return-dollar text-muted">{{ asset.pnlDollar > 0 ? '+' : '' }}${{ asset.pnlDollar }}</span>
                  </div>
                </td>

                <td class="center">
                  <div class="mini-chart-container">
                    <div class="bar-half left-half">
                      <div v-if="asset.pnl < 0" class="bar fill-red" :style="{ width: getBarWidth(asset.pnl) + '%' }"></div>
                    </div>
                    <div class="zero-line"></div>
                    <div class="bar-half right-half">
                      <div v-if="asset.pnl > 0" class="bar fill-green" :style="{ width: getBarWidth(asset.pnl) + '%' }"></div>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
// 引入我们写好的 API 服务
import { getStockQuote } from '../apis/finnhubService.js';

// 重构数据源：增加 apiSymbol 用于请求，增加 costPrice (纯数字) 用于数学计算
const mockHoldings = ref([
  { ticker: 'NVDA', apiSymbol: 'NVDA', name: 'NVIDIA Corp.', quantity: 150, costPrice: 420.00, cost: '$420.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'VOO', apiSymbol: 'VOO', name: 'Vanguard S&P 500', quantity: 400, costPrice: 440.00, cost: '$440.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'AAPL', apiSymbol: 'AAPL', name: 'Apple Inc.', quantity: 200, costPrice: 172.00, cost: '$172.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'TSLA', apiSymbol: 'TSLA', name: 'Tesla Inc.', quantity: 100, costPrice: 240.00, cost: '$240.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'US05Y', apiSymbol: 'STATIC', name: '5-Year Treasury', quantity: 500, costPrice: 100.00, cost: '$100.00', price: '$98.80', marketValue: '$49,400.00', pnl: -1.2, pnlDollar: '-600' },
  { ticker: 'SNOW', apiSymbol: 'SNOW', name: 'Snowflake Inc.', quantity: 50, costPrice: 210.00, cost: '$210.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
]);

// 格式化工具
const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
const numFormatter = new Intl.NumberFormat('en-US');

// 抓取实时数据的核心逻辑
const fetchLiveHoldingsData = async () => {
  for (let asset of mockHoldings.value) {
    if (asset.apiSymbol === 'STATIC') continue;

    try {
      const data = await getStockQuote(asset.apiSymbol);
      
      if (data && data.c) {
        const livePrice = data.c;

        // 1. 更新价格和市值
        asset.price = usdFormatter.format(livePrice);
        const liveMarketValue = livePrice * asset.quantity;
        asset.marketValue = usdFormatter.format(liveMarketValue);

        // 2. 计算盈亏百分比 (用于决定柱状图长短和颜色)
        const returnPct = ((livePrice - asset.costPrice) / asset.costPrice) * 100;
        asset.pnl = parseFloat(returnPct.toFixed(2));

        // 3. 计算盈亏绝对金额 (美元)
        const dollarDiff = (livePrice - asset.costPrice) * asset.quantity;
        // 去掉负号，因为我们在 HTML 里手写了逻辑 (asset.pnlDollar > 0 ? '+' : '')，带负号会导致显示 "--600"
        asset.pnlDollar = numFormatter.format(Math.abs(dollarDiff).toFixed(2)); 
      }
    } catch (error) {
      console.error(`Failed to fetch live data for ${asset.ticker}:`, error);
      asset.price = 'Error';
    }
  }
};

// 页面挂载时立刻请求数据
onMounted(() => {
  fetchLiveHoldingsData();
});

// 计算所有股票中盈亏百分比绝对值的最大值，作为柱状图的 100% 宽度基准
const maxAbsolutePnl = computed(() => {
  let max = 0;
  mockHoldings.value.forEach(asset => {
    const absVal = Math.abs(asset.pnl);
    if (absVal > max) max = absVal;
  });
  return max || 1; // 避免除以 0 导致报错
});

const enrichedHoldings = computed(() => mockHoldings.value);

// 计算柱子宽度的函数：会根据实时数据动态变化！
const getBarWidth = (pnl) => {
  const percentage = (Math.abs(pnl) / maxAbsolutePnl.value) * 100;
  return percentage < 2 ? 2 : percentage; // 给个保底宽度 2% 保证肉眼可见
};
</script>

<style scoped>
/* 继承 Apple 基础布局样式 (与 Dashboard 保持一致) */
.apple-layout { display: flex; height: 100vh; width: 100vw; max-width: 100%; background-color: #000000; color: #f5f5f7; font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "SF Pro Display", sans-serif; overflow: hidden; position: relative; box-sizing: border-box; }
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ambient-bg { position: absolute; border-radius: 50%; filter: blur(100px); z-index: 0; opacity: 0.5; animation: float 20s ease-in-out infinite alternate; pointer-events: none; }
.blob-1 { top: -10%; left: -10%; width: 50vw; height: 50vw; background: radial-gradient(circle, #2c1a59 0%, rgba(0,0,0,0) 70%); }
.blob-2 { bottom: -20%; right: -10%; width: 60vw; height: 60vw; background: radial-gradient(circle, #0a2e3f 0%, rgba(0,0,0,0) 70%); }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(5%, 5%) scale(1.1); } }

/* 侧边栏 */
.glass-sidebar { width: clamp(220px, 18vw, 280px); flex-shrink: 0; background: rgba(30, 30, 32, 0.4); backdrop-filter: blur(40px); -webkit-backdrop-filter: blur(40px); border-right: 1px solid rgba(255, 255, 255, 0.08); display: flex; flex-direction: column; padding: clamp(1.5rem, 2vh, 2.5rem) 1.5rem; z-index: 10; box-sizing: border-box; }
.brand { display: flex; align-items: center; gap: 0.8rem; margin-bottom: clamp(2rem, 4vh, 3rem); padding-left: 0.5rem; }
.brand-title { font-size: clamp(1.1rem, 1.5vw, 1.4rem); font-weight: 700; margin: 0; letter-spacing: -0.5px; white-space: nowrap; }
.nav-menu { display: flex; flex-direction: column; gap: 0.4rem; overflow-y: auto; overflow-x: hidden; }
.nav-item { display: flex; align-items: center; justify-content: flex-start; text-align: left; padding: 0.8rem 1rem; color: #a1a1a6; text-decoration: none; border-radius: 12px; font-weight: 500; font-size: clamp(0.9rem, 1vw, 0.95rem); transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.nav-item:hover { background: rgba(255, 255, 255, 0.05); color: #fff; }
.nav-item.active { background: rgba(255, 255, 255, 0.1); color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.user-profile { margin-top: auto; display: flex; align-items: center; gap: 1rem; padding: 1rem; background: rgba(255, 255, 255, 0.03); border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.05); transition: transform 0.2s; cursor: pointer; overflow: hidden; }
.user-profile:hover { transform: scale(1.02); }
.avatar { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.user-info { min-width: 0; }
.user-info .name { font-size: 0.95rem; font-weight: 600; }
.user-info .type { font-size: 0.8rem; color: #a1a1a6; }

/* 主内容区 */
.main-content { flex: 1; min-width: 0; padding: clamp(1.5rem, 3vw, 3rem) clamp(2rem, 4vw, 4rem); overflow-y: auto; overflow-x: hidden; z-index: 10; box-sizing: border-box; }
.top-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: clamp(1.5rem, 3vh, 3rem); flex-wrap: wrap; gap: 1rem; }
.header-titles { min-width: 0; }
.page-title { font-size: clamp(2rem, 3vw, 2.5rem); font-weight: 700; margin: 0; letter-spacing: -1px; }
.datetime { color: #a1a1a6; font-size: clamp(0.9rem, 1.2vw, 1.1rem); margin: 0.5rem 0 0 0; font-weight: 500; }
.header-actions { display: flex; gap: 1rem; flex-shrink: 0; }
.apple-btn { padding: 0.7rem 1.4rem; border-radius: 20px; font-weight: 600; font-size: clamp(0.85rem, 1vw, 0.95rem); cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); white-space: nowrap; }
.apple-btn:hover { transform: scale(1.03); }
.apple-btn:active { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.btn-secondary { background: rgba(255, 255, 255, 0.1); color: #ffffff; backdrop-filter: blur(10px); }
.glass-card { background: rgba(30, 30, 32, 0.5); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 24px; padding: clamp(1.2rem, 2vw, 1.8rem); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2); box-sizing: border-box; overflow: hidden; }
.card-header { margin-bottom: 1.5rem; }
.card-header h3 { margin: 0; font-size: clamp(1.05rem, 1.2vw, 1.2rem); font-weight: 600; }

/* 表格布局 */
.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; }
.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 950px; table-layout: fixed; }

/* 列宽分配：专门给柱状图留出充足空间 */
.col-asset { width: 22%; }
.col-num { width: 12%; }
.col-value { width: 15%; }
.col-return { width: 15%; }
.col-bar { width: 12%; }

.apple-table th { color: #a1a1a6; font-size: 0.8rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; padding: 0 1.2rem 1rem 1.2rem; border-bottom: 1px solid rgba(255,255,255,0.08); white-space: nowrap; text-align: left; }
.apple-table td { padding: 1.2rem; border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle; white-space: nowrap; text-align: left; }
.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; }
.apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }

.table-row { transition: background 0.2s; }
.table-row:hover td { background: rgba(255,255,255,0.03); }

.apple-table .right { text-align: right; }
.apple-table .center { text-align: center; }
.font-medium { font-weight: 500; }
.text-muted { color: #8e8e93; font-size: 0.9rem; }

.asset-identity { display: flex; align-items: center; justify-content: flex-start; gap: 1rem; }
.asset-text-container { display: flex; flex-direction: column; align-items: flex-start; text-align: left; }
.asset-name { font-weight: 600; font-size: clamp(0.95rem, 1vw, 1.05rem); }
.asset-desc { font-size: 0.85rem; color: #a1a1a6; margin-top: 0.2rem; }

.return-data { display: flex; flex-direction: column; align-items: flex-end; }
.return-dollar { font-size: 0.85rem; margin-top: 0.1rem; }

.text-green { color: #30d158; font-weight: 600; }
.text-red { color: #ff453a; font-weight: 600; }

/* =========================================================
   INLINE MINI BAR CHART (纯 CSS 实现的双向柱状图)
========================================================= */
.mini-chart-container {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 100px;
  height: 24px;
  margin: 0 auto;
  position: relative;
}

/* 红色亏损条区域 (左侧，内容靠右对齐生长) */
.bar-half.left-half {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 让柱子从中心向左生长 */
}

/* 绿色盈利条区域 (右侧，内容靠左对齐生长) */
.bar-half.right-half {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start; /* 让柱子从中心向右生长 */
}

/* 中心的灰色 0 轴标线 */
.zero-line {
  width: 2px;
  height: 14px;
  background-color: #636366;
  margin: 0 2px;
  border-radius: 2px;
  z-index: 2;
}

/* bar chart的通用样式 */
.bar {
  height: 6px; /* bar的粗细 */
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1); /* 加载时的丝滑动画 */
}

/* 填充颜色 */
.fill-green { background-color: #30d158; }
.fill-red { background-color: #ff453a; }

/* 动画 */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { opacity: 0; animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
</style>