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
          <h1 class="page-title text-truncate">Overview</h1>
          <p class="datetime text-truncate">Tuesday, March 31</p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-secondary text-truncate">New Trade</button>
          <button class="apple-btn btn-primary text-truncate">Buy / Sell</button>
        </div>
      </header>

      <div class="metrics-grid animate-fade-in delay-2">
         <div class="glass-card metric-card">
           <div class="label text-truncate">Total Assets</div>
           <div class="value text-truncate">$3,248,500.00</div>
         </div>
         <div class="glass-card metric-card">
           <div class="label text-truncate">Today's P&L</div>
           <div class="value-wrapper">
             <div class="value positive text-truncate">+$15,240.00</div>
             <span class="badge">+0.66%</span>
           </div>
         </div>
      </div>

      <div class="charts-grid animate-fade-in delay-3">
        <div class="glass-card chart-card large">
          <div class="card-header">
            <h3 class="text-truncate">Net Value Trend</h3>
            <div class="segmented-control">
              <button>1M</button>
              <button class="active">3M</button>
              <button>1Y</button>
            </div>
          </div>
          <div class="chart-placeholder">
            <p>Interactive Chart</p>
          </div>
        </div>

        <div class="glass-card chart-card small">
          <div class="card-header">
            <h3 class="text-truncate">Allocation</h3>
          </div>
          <div class="chart-placeholder">
             <div class="mock-donut"></div>
          </div>
        </div>
      </div>

      <div class="glass-card table-card animate-fade-in delay-4">
        <div class="card-header">
          <h3 class="text-truncate">Holdings</h3>
          <button class="apple-link text-truncate">View All 􀆊</button>
        </div>
        
        <div class="table-responsive">
          <table class="apple-table">
            <thead>
              <tr>
                <th class="col-asset">Asset</th>
                <th class="col-type">Type</th>
                <th class="right col-qty">Qty</th>
                <th class="right col-price">Price</th>
                <th class="right col-value">Market Value</th>
                <th class="right col-return">Return</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="asset in mockHoldings" :key="asset.ticker" class="table-row">
                <td>
                  <div class="asset-identity">
                    <div class="asset-text-container">
                      <div class="asset-name">{{ asset.ticker }}</div>
                      <div class="asset-desc">{{ asset.name }}</div>
                    </div>
                  </div>
                </td>
                <td><span class="pill">{{ asset.type }}</span></td>
                <td class="right">{{ asset.quantity }}</td>
                <td class="right">{{ asset.price }}</td>
                <td class="right font-medium">{{ asset.marketValue }}</td>
                <td class="right">
                  <span :class="asset.pnl > 0 ? 'text-green' : 'text-red'">
                    {{ asset.pnl > 0 ? '+' : '' }}{{ asset.pnl }}%
                  </span>
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
import { ref, onMounted } from 'vue';
// 引入我们之前写好的 Finnhub API 服务
import { getStockQuote } from '../apis/finnhubService.js';

// 我们重构了数据源：增加了 costPrice (持仓成本价) 和 apiSymbol (用于请求API的精准代码)
const mockHoldings = ref([
  { ticker: 'VOO', apiSymbol: 'VOO', name: 'Vanguard S&P 500 ETF', type: 'Equity', quantity: 400, costPrice: 440.00, price: 'Loading...', marketValue: '--', pnl: 0 },
  { ticker: 'AAPL', apiSymbol: 'AAPL', name: 'Apple Inc.', type: 'Equity', quantity: 200, costPrice: 172.00, price: 'Loading...', marketValue: '--', pnl: 0 },
  // Finnhub 请求加密货币需要加前缀，比如 BINANCE:
  { ticker: 'BTC', apiSymbol: 'BINANCE:BTCUSDT', name: 'Bitcoin', type: 'Crypto', quantity: 0.5, costPrice: 58000.00, price: 'Loading...', marketValue: '--', pnl: 0 },
  // 免费API通常不包含债券数据，这里我们用 'STATIC' 标记让它保持静态展示
  { ticker: 'US05Y', apiSymbol: 'STATIC', name: '5-Year Treasury Bond', type: 'Bond', quantity: 500, costPrice: 100.00, price: '$98.80', marketValue: '$49,400.00', pnl: -1.2 },
]);

// 获取实时数据的核心函数
const fetchLiveHoldingsData = async () => {
  for (let asset of mockHoldings.value) {
    if (asset.apiSymbol === 'STATIC') continue;

    try {
      // 调用 API 获取最新报价
      const data = await getStockQuote(asset.apiSymbol);

      // Finnhub 返回的数据中，'c' 代表当前最新价格
      if (data && data.c) { 
        const livePrice = data.c;

        // 1. 更新现价
        asset.price = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(livePrice);

        // 2. 市值 = 现价 * 持仓量
        const liveMarketValue = livePrice * asset.quantity;
        asset.marketValue = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(liveMarketValue);

        // 3. 盈亏百分比 = (现价 - 成本价) / 成本价 * 100
        const returnPct = ((livePrice - asset.costPrice) / asset.costPrice) * 100;
        asset.pnl = parseFloat(returnPct.toFixed(2)); // 保留两位小数
      }
    } catch (error) {
      console.error(`Failed to fetch live data for ${asset.ticker}:`, error);
      asset.price = 'Error';
    }
  }
};

// 当页面加载完成时，立刻触发数据抓取
onMounted(() => {
  fetchLiveHoldingsData();
  
  setInterval(fetchLiveHoldingsData, 300000); 
});
</script>

<style scoped>
/* =========================================================
   APPLE DESIGN SYSTEM - FLUID & RESPONSIVE
========================================================= */

.apple-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  max-width: 100%;
  background-color: #000000;
  color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "SF Pro Display", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Ambient Background Blobs */
.ambient-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.5;
  animation: float 20s ease-in-out infinite alternate;
  pointer-events: none; 
}
.blob-1 { top: -10%; left: -10%; width: 50vw; height: 50vw; background: radial-gradient(circle, #2c1a59 0%, rgba(0,0,0,0) 70%); }
.blob-2 { bottom: -20%; right: -10%; width: 60vw; height: 60vw; background: radial-gradient(circle, #0a2e3f 0%, rgba(0,0,0,0) 70%); }

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(5%, 5%) scale(1.1); }
}

/* 2. SIDEBAR */
.glass-sidebar {
  width: clamp(220px, 18vw, 280px);
  flex-shrink: 0; 
  background: rgba(30, 30, 32, 0.4);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  padding: clamp(1.5rem, 2vh, 2.5rem) 1.5rem;
  z-index: 10;
  box-sizing: border-box;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: clamp(2rem, 4vh, 3rem);
  padding-left: 0.5rem;
}

/* 移除了图标相关的样式 */
.brand-title { 
  font-size: clamp(1.1rem, 1.5vw, 1.4rem); 
  font-weight: 700; 
  margin: 0; 
  letter-spacing: -0.5px;
  white-space: nowrap; 
}

.nav-menu { display: flex; flex-direction: column; gap: 0.4rem; overflow-y: auto; overflow-x: hidden; }

.nav-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 1rem;
  color: #a1a1a6;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 500;
  font-size: clamp(0.9rem, 1vw, 0.95rem);
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  /* 确保文字左对齐 */
  text-align: left;
  justify-content: flex-start;
}

/* 移除了导航项中的图标样式 */
.nav-item:hover { background: rgba(255, 255, 255, 0.05); color: #fff; }
.nav-item.active { background: rgba(255, 255, 255, 0.1); color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.user-profile {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: transform 0.2s;
  cursor: pointer;
  overflow: hidden; 
}
.user-profile:hover { transform: scale(1.02); }

.avatar { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.user-info { min-width: 0; }
.user-info .name { font-size: 0.95rem; font-weight: 600; }
.user-info .type { font-size: 0.8rem; color: #a1a1a6; }

/* 3. MAIN CONTENT */
.main-content {
  flex: 1;
  min-width: 0; 
  padding: clamp(1.5rem, 3vw, 3rem) clamp(2rem, 4vw, 4rem);
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 10;
  box-sizing: border-box;
}

.top-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-end; 
  margin-bottom: clamp(1.5rem, 3vh, 3rem); 
  flex-wrap: wrap; 
  gap: 1rem;
}
.header-titles { min-width: 0; }
.page-title { font-size: clamp(2rem, 3vw, 2.5rem); font-weight: 700; margin: 0; letter-spacing: -1px; }
.datetime { color: #a1a1a6; font-size: clamp(0.9rem, 1.2vw, 1.1rem); margin: 0.5rem 0 0 0; font-weight: 500; }

.header-actions { display: flex; gap: 1rem; flex-shrink: 0; }

.apple-btn {
  padding: 0.7rem 1.4rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: clamp(0.85rem, 1vw, 0.95rem);
  cursor: pointer;
  border: none;
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  white-space: nowrap;
}
.apple-btn:hover { transform: scale(1.03); }
.apple-btn:active { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.btn-secondary { background: rgba(255, 255, 255, 0.1); color: #ffffff; backdrop-filter: blur(10px); }

/* GLASS CARDS */
.glass-card {
  background: rgba(30, 30, 32, 0.5);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: clamp(1.2rem, 2vw, 1.8rem);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s cubic-bezier(0.25, 1, 0.5, 1), box-shadow 0.3s ease;
  box-sizing: border-box;
  overflow: hidden; 
}
.glass-card:hover { transform: translateY(-4px); box-shadow: 0 14px 45px rgba(0, 0, 0, 0.3); }

/* METRICS */
.metrics-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); 
  gap: clamp(1rem, 2vw, 1.5rem); 
  margin-bottom: clamp(1rem, 2vw, 1.5rem); 
}
.metric-card { display: flex; flex-direction: column; justify-content: center; }
.metric-card .label { color: #a1a1a6; font-size: clamp(0.85rem, 1vw, 0.95rem); font-weight: 500; margin-bottom: 0.8rem; }

.value-wrapper { display: flex; align-items: center; gap: 0.8rem; flex-wrap: wrap; }
.metric-card .value { font-size: clamp(1.8rem, 2.5vw, 2.2rem); font-weight: 700; letter-spacing: -1px; }

.positive { color: #fff; }
.badge { font-size: 0.9rem; padding: 0.3rem 0.6rem; border-radius: 8px; background: rgba(48, 209, 88, 0.15); color: #30d158; font-weight: 600; white-space: nowrap;}

/* CHARTS */
.charts-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); 
  gap: clamp(1rem, 2vw, 1.5rem); 
  margin-bottom: clamp(1rem, 2vw, 1.5rem); 
}
@media (min-width: 1024px) {
  .charts-grid { grid-template-columns: 2fr 1fr; }
}

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; gap: 1rem;}
.card-header h3 { margin: 0; font-size: clamp(1.05rem, 1.2vw, 1.2rem); font-weight: 600; min-width: 0;}

.segmented-control { background: rgba(255,255,255,0.08); padding: 4px; border-radius: 10px; display: flex; flex-shrink: 0;}
.segmented-control button { background: transparent; border: none; color: #a1a1a6; padding: 0.4rem 1rem; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; white-space: nowrap; font-size: 0.85rem;}
.segmented-control button.active { background: #636366; color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.2); }

.chart-placeholder { height: clamp(200px, 30vh, 260px); display: flex; justify-content: center; align-items: center; color: #636366; font-weight: 500; background: rgba(0,0,0,0.2); border-radius: 16px;}
.mock-donut { width: clamp(120px, 15vw, 160px); height: clamp(120px, 15vw, 160px); border-radius: 50%; border: 25px solid rgba(255,255,255,0.1); border-top-color: #0a84ff; border-right-color: #30d158; }


/* ==========================================
   TABLE (Full Data Visibility)
========================================== */
.apple-link { background: none; border: none; color: #0a84ff; font-weight: 500; font-size: 0.95rem; cursor: pointer; transition: 0.2s; white-space: nowrap;}
.apple-link:hover { opacity: 0.8; }

.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; }

.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 900px; table-layout: fixed; }

.col-asset { width: 28%; }
.col-type { width: 12%; }
.col-qty { width: 15%; }
.col-price { width: 15%; }
.col-value { width: 16%; }
.col-return { width: 14%; }

.apple-table th { 
  color: #a1a1a6; 
  font-size: 0.85rem; 
  font-weight: 500; 
  text-transform: uppercase; 
  letter-spacing: 0.5px; 
  padding: 0 1.5rem 1rem 1.5rem; 
  border-bottom: 1px solid rgba(255,255,255,0.08); 
  white-space: nowrap; 
  text-align: left
}

.apple-table td { 
  padding: 1.2rem 1.5rem; 
  border-bottom: 1px solid rgba(255,255,255,0.04); 
  vertical-align: middle; 
  white-space: nowrap; 
  text-align: left
}
.apple-table .right { text-align: right; }

.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; }
.apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }

.table-row { transition: background 0.2s; }
.table-row:hover td { background: rgba(255,255,255,0.02); }

.apple-table .right { text-align: right; }
.font-medium { font-weight: 500; }

.asset-identity { 
  display: flex; 
  align-items: center; 
  justify-content: flex-start; /* 强制整个资产块靠左容器边缘 */
  gap: 1rem; 
}

.asset-text-container { 
  display: flex; 
  flex-direction: column; 
  align-items: flex-start; /* 核心：强制上下两行文字以左侧为基准对齐 */
  text-align: left; /* 强制文本本身左对齐 */
}
.asset-name { font-weight: 600; font-size: clamp(0.95rem, 1vw, 1.05rem); }
.asset-desc { font-size: 0.85rem; color: #a1a1a6; margin-top: 0.2rem; }

.pill { background: rgba(255,255,255,0.1); padding: 0.3rem 0.8rem; border-radius: 12px; font-size: 0.8rem; font-weight: 600; color: #e5e5ea; display: inline-block; box-sizing: border-box;}

.text-green { color: #30d158; font-weight: 600; }
.text-red { color: #ff453a; font-weight: 600; }

/* ANIMATIONS */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  opacity: 0;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
.delay-4 { animation-delay: 0.4s; }
</style>