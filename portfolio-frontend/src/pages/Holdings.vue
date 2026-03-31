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
        
        <router-link to="/transactions" class="nav-item" active-class="active">
          <span class="text-truncate">Transactions</span>
        </router-link>
        
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
          <h1 class="page-title text-truncate">Detailed Positions</h1>
          <p class="datetime text-truncate">Your complete asset breakdown</p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-primary global-add-btn" @click="isAddModalOpen = true">
            + Add Transaction
          </button>
        </div>
      </header>

      <div class="glass-card table-card animate-fade-in delay-2">
        <div class="table-responsive">
          <table class="apple-table">
            <thead>
              <tr>
                <th class="col-asset">Asset</th>
                <th class="right col-qty">Shares</th>
                <th class="right col-cost">Avg Cost</th>
                <th class="right col-price">Price</th>
                <th class="right col-value">Market Value</th>
                <th class="right col-return">Total Return</th>
                <th class="center col-chart">Performance</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="asset in enrichedHoldings" :key="asset.ticker">
                
                <tr 
                  class="table-row" 
                  @click="toggleRow(asset.ticker)"
                  :class="{ 'is-expanded': expandedRow === asset.ticker }"
                >
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

                <tr v-if="expandedRow === asset.ticker" class="expanded-chart-row">
                  <td colspan="7">
                    <div class="expanded-content animate-fade-in-fast">
                      <div class="expanded-header">
                        <h4>{{ asset.ticker }} Trend</h4>
                        <button class="apple-link" @click.stop="toggleRow(asset.ticker)">Close ✕</button>
                      </div>
                      
                      <div class="real-chart-container">
                        <StockChart :ticker="asset.ticker" :apiSymbol="asset.apiSymbol" :pnl="asset.pnl" />
                      </div>
                    </div>
                  </td>
                </tr>

              </template>
            </tbody>
          </table>
        </div>
      </div>

      <AddTransactionModal 
        v-model="isAddModalOpen" 
        @submit="handleNewTransaction" 
      />

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getStockQuote } from '../apis/finnhubService.js';
import StockChart from '../components/StockChart.vue';
import AddTransactionModal from '../components/AddTransactionModal.vue';

// ==========================================
// 状态管理
// ==========================================
const isAddModalOpen = ref(false);
const expandedRow = ref(null);

const toggleRow = (ticker) => {
  if (expandedRow.value === ticker) {
    expandedRow.value = null; 
  } else {
    expandedRow.value = ticker; 
  }
};

// ==========================================
// 核心数据源
// ==========================================
const mockHoldings = ref([
  { ticker: 'NVDA', apiSymbol: 'NVDA', name: 'NVIDIA Corp.', quantity: 150, costPrice: 420.00, cost: '$420.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'VOO', apiSymbol: 'VOO', name: 'Vanguard S&P 500', quantity: 400, costPrice: 440.00, cost: '$440.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'AAPL', apiSymbol: 'AAPL', name: 'Apple Inc.', quantity: 200, costPrice: 172.00, cost: '$172.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'TSLA', apiSymbol: 'TSLA', name: 'Tesla Inc.', quantity: 100, costPrice: 240.00, cost: '$240.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
  { ticker: 'US05Y', apiSymbol: 'STATIC', name: '5-Year Treasury', quantity: 500, costPrice: 100.00, cost: '$100.00', price: '$98.80', marketValue: '$49,400.00', pnl: -1.2, pnlDollar: '-600' },
  { ticker: 'SNOW', apiSymbol: 'SNOW', name: 'Snowflake Inc.', quantity: 50, costPrice: 210.00, cost: '$210.00', price: 'Loading...', marketValue: '--', pnl: 0, pnlDollar: '0' },
]);

const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
const numFormatter = new Intl.NumberFormat('en-US');

// ==========================================
// API 数据抓取与处理
// ==========================================
const fetchLiveHoldingsData = async () => {
  for (let asset of mockHoldings.value) {
    if (asset.apiSymbol === 'STATIC') continue;

    try {
      const data = await getStockQuote(asset.apiSymbol);
      
      if (data && data.c) {
        const livePrice = data.c;
        asset.price = usdFormatter.format(livePrice);
        const liveMarketValue = livePrice * asset.quantity;
        asset.marketValue = usdFormatter.format(liveMarketValue);

        const returnPct = ((livePrice - asset.costPrice) / asset.costPrice) * 100;
        asset.pnl = parseFloat(returnPct.toFixed(2));

        const dollarDiff = (livePrice - asset.costPrice) * asset.quantity;
        asset.pnlDollar = numFormatter.format(Math.abs(dollarDiff).toFixed(2)); 
      }
    } catch (error) {
      console.error(`Failed to fetch live data for ${asset.ticker}:`, error);
      asset.price = 'Error';
    }
  }
};

onMounted(() => {
  fetchLiveHoldingsData();
});

// 处理弹窗提交的新数据
const handleNewTransaction = (txnData) => {
  console.log("New Transaction submitted from Holdings page:", txnData);
  // 在真实应用中，你可以在这里根据买入/卖出，更新对应资产的持仓数量和成本价
};

// ==========================================
// 微型柱状图逻辑
// ==========================================
const maxAbsolutePnl = computed(() => {
  let max = 0;
  mockHoldings.value.forEach(asset => {
    const absVal = Math.abs(asset.pnl);
    if (absVal > max) max = absVal;
  });
  return max || 1; 
});

const enrichedHoldings = computed(() => mockHoldings.value);

const getBarWidth = (pnl) => {
  const percentage = (Math.abs(pnl) / maxAbsolutePnl.value) * 100;
  return percentage < 2 ? 2 : percentage; 
};
</script>

<style scoped>
/* =========================================================
   基础布局与背景
========================================================= */
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

/* 主内容区 & 统一 Header */
.main-content { flex: 1; min-width: 0; padding: clamp(1.5rem, 3vw, 3rem) clamp(2rem, 4vw, 4rem); overflow-y: auto; overflow-x: hidden; z-index: 10; box-sizing: border-box; }

.top-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: clamp(1.5rem, 3vh, 2rem); flex-wrap: wrap; gap: 1rem; min-height: 60px; }
.header-titles { min-width: 0; }
.page-title { font-size: clamp(2rem, 3vw, 2.5rem); font-weight: 700; margin: 0; letter-spacing: -1px; }
.datetime { color: #a1a1a6; font-size: clamp(0.9rem, 1.2vw, 1.1rem); margin: 0.5rem 0 0 0; font-weight: 500; }
.header-actions { display: flex; align-items: center; }

/* 按钮通用样式 */
.apple-btn { border-radius: 20px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.global-add-btn { height: 42px; padding: 0 1.5rem; font-size: 0.95rem; display: inline-flex; align-items: center; justify-content: center; white-space: nowrap; }

/* 卡片 */
.glass-card { background: rgba(30, 30, 32, 0.5); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 24px; padding: clamp(1.2rem, 2vw, 1.8rem); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2); box-sizing: border-box; overflow: hidden; }

/* 表格主体 */
.table-card { padding-top: 1.5rem; }
.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; }
.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 900px; }

.apple-table th { color: #a1a1a6; font-size: 0.8rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; padding: 0 1rem 1rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: left; }
.apple-table td { padding: 1.2rem 1rem; vertical-align: middle; }
.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; }
.apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }

.col-asset { width: 20%; } .col-qty { width: 10%; } .col-cost { width: 12%; } .col-price { width: 12%; } .col-value { width: 15%; } .col-return { width: 15%; } .col-chart { width: 16%; }

.apple-table .right { text-align: right; }
.apple-table .center { text-align: center; }
.font-medium { font-weight: 500; }
.text-muted { color: #8e8e93; font-size: 0.9rem; }
.text-green { color: #30d158; } .text-red { color: #ff453a; }

/* 资产信息 */
.asset-identity { display: flex; align-items: center; gap: 1rem; }
.asset-name { font-weight: 600; font-size: 1rem; }
.asset-desc { color: #8e8e93; font-size: 0.85rem; margin-top: 0.2rem; }
.return-data { display: flex; flex-direction: column; align-items: flex-end; gap: 0.2rem; }
.return-dollar { font-size: 0.85rem; }

/* 微型柱状图 */
.mini-chart-container { display: flex; align-items: center; width: 100%; height: 20px; position: relative; }
.zero-line { position: absolute; left: 50%; top: 0; bottom: 0; width: 1px; background: rgba(255,255,255,0.2); transform: translateX(-50%); z-index: 1; }
.bar-half { flex: 1; height: 100%; display: flex; align-items: center; }
.left-half { justify-content: flex-end; padding-right: 2px; }
.right-half { justify-content: flex-flex-start; padding-left: 2px; }
.bar { height: 6px; border-radius: 3px; transition: width 0.5s ease; }
.fill-green { background: #30d158; } .fill-red { background: #ff453a; }

/* EXPANDABLE ROWS (展开行样式) */
.table-row { cursor: pointer; transition: all 0.2s ease; border-bottom: 1px solid rgba(255,255,255,0.04); }
.table-row td { border-bottom: 1px solid rgba(255,255,255,0.04); transition: background 0.2s; }
.table-row:hover td { background: rgba(255,255,255,0.02); }
.table-row.is-expanded td { background: rgba(255, 255, 255, 0.04); border-bottom: none; }
.expanded-chart-row td { padding: 0; border-bottom: 1px solid rgba(255, 255, 255, 0.08); }
.expanded-content { background: rgba(0, 0, 0, 0.3); box-shadow: inset 0 5px 15px rgba(0, 0, 0, 0.3); padding: 1.5rem 2rem; border-radius: 0 0 16px 16px; margin-bottom: 1rem; }
.expanded-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.expanded-header h4 { margin: 0; font-size: 1.1rem; color: #fff; font-weight: 600; }
.real-chart-container { width: 100%; }

/* 链接样式 */
.apple-link { background: none; border: none; color: #0a84ff; font-size: 0.9rem; font-weight: 500; cursor: pointer; padding: 0; transition: opacity 0.2s; display: inline-flex; align-items: center; gap: 0.3rem;}
.apple-link:hover { opacity: 0.8; text-decoration: underline; }

/* 动画 */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { opacity: 0; animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.delay-1 { animation-delay: 0.1s; } .delay-2 { animation-delay: 0.2s; }
@keyframes fadeInFast { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in-fast { animation: fadeInFast 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
</style>