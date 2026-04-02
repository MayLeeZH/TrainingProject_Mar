<template>
  <div class="apple-layout">
    <div class="ambient-bg blob-1"></div>
    <div class="ambient-bg blob-2"></div>

    <AppSidebar />

    <main class="main-content">
      
      <header class="top-header animate-fade-in delay-1">
        <div class="header-titles">
          <h1 class="page-title text-truncate">Overview</h1>
          <p class="datetime text-truncate">Tuesday, March 31</p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-secondary" @click="$router.push('/ai-chat')">
            Open AI Assistant
          </button>
          <button class="apple-btn btn-primary global-add-btn" @click="isAddModalOpen = true">
            + Add Transaction
          </button>
        </div>
      </header>

      <div class="metrics-grid animate-fade-in delay-2">
         <div class="glass-card metric-card">
           <div class="label text-truncate">Total Assets</div>
           <div class="value text-truncate">{{ usdFormatter.format(totalAssets) }}</div>
         </div>


        <div class="glass-card metric-card">
          <div class="label text-truncate">Today's P&L</div>
          <div class="value-wrapper">
            <div
              class="value text-truncate"
              :class="todaysPnl >= 0 ? 'positive' : 'text-red'"
            >
              {{ todaysPnl >= 0 ? '+' : '-' }}{{ usdFormatter.format(Math.abs(todaysPnl)) }}
            </div>
            <span class="badge" :class="{ negative: todaysPnlRate < 0 }">
              {{ todaysPnlRate >= 0 ? '+' : '' }}{{ todaysPnlRate.toFixed(2) }}%
            </span>
          </div>
        </div>
      </div>

      <div class="charts-grid animate-fade-in delay-3">
        <div class="glass-card chart-card large">
          <div class="card-header portfolio-header">
            <div class="header-left">
              <h3 class="text-truncate">Portfolio Performance</h3>
              <label class="compare-toggle">
                <input type="checkbox" v-model="compareMode" />
                <span class="toggle-track">
                  <span class="toggle-thumb"></span>
                </span>
                <span class="toggle-label text-truncate">Compare S&P 500</span>
              </label>
            </div>
            <div class="segmented-control">
              <button :class="{ active: timeframe === '1D' }" @click="timeframe = '1D'">1D</button>
              <button :class="{ active: timeframe === '1W' }" @click="timeframe = '1W'">1W</button>
              <button :class="{ active: timeframe === '1M' }" @click="timeframe = '1M'">1M</button>
            </div>
          </div>
          <div class="real-market-chart-container">
            <MarketChart :compareMode="compareMode" :timeframe="timeframe" />
          </div>
        </div>

        <div class="glass-card chart-card small">
          <div class="card-header">
            <h3 class="text-truncate">Allocation</h3>
          </div>
          <div class="real-chart-container">
            <AllocationChart :allocation-data="allocationChartData" />
          </div>
        </div>
      </div>

      <div class="glass-card table-card animate-fade-in delay-4">
        <div class="card-header">
          <h3 class="text-truncate">Holdings</h3>
          <button class="apple-link text-truncate" @click="$router.push('/holdings')">View All ></button>
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
              <tr v-for="asset in holdings" :key="asset.id || asset.ticker" class="table-row">
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
import { getHoldings, getAssetDistribution } from '../apis/holdingService.js'
import MarketChart from '../components/MarketChart.vue';
import AllocationChart from '../components/AllocationChart.vue';
import AppSidebar from '../components/AppSidebar.vue';
// 引入全局弹窗组件
import AddTransactionModal from '../components/AddTransactionModal.vue';

// --- 控制弹窗的显示 ---
const isAddModalOpen = ref(false);

// --- 图表控制状态 ---
const compareMode = ref(false);
const timeframe = ref('1M');

const holdings = ref([])
const assetDistribution = ref([]);
const defaultUserId = 1;
const cashBalance = ref(0);


const usdFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD'
})

// --- 处理资产分布数据，转换成饼状图组件需要的格式 --- 
const allocationChartData = computed(() => {
  return assetDistribution.value.map((item) => ({
    name: mapAssetType(item.assetType),
    value: Number(item.amount || 0),
    proportion: Number(item.proportion || 0)
  }));
});

const fetchAssetDistribution = async () => {
  try {
    const data = await getAssetDistribution(defaultUserId);
    assetDistribution.value = data;
  } catch (error) {
    console.error('Failed to fetch asset distribution:', error);
    assetDistribution.value = [];
  }
};


// --- 计算总资产（持仓市值 + 现金余额） ---
const totalAssets = computed(() => {
  const holdingsTotal = holdings.value.reduce((sum, item) => {
    const avgPrice = Number(item.costPrice || 0);
    const quantity = Number(item.quantity || 0);
    return sum + avgPrice * quantity;
  }, 0);
    return holdingsTotal + Number(cashBalance.value || 0);
});


/*
  Today's P&L = totalAssets - 所有持仓的昨日收盘价 * quantity 之和
*/
const yesterdayHoldingsTotal = computed(() => {
  return holdings.value.reduce((sum, item) => {
    const previousClose = Number(item.previousClose || 0);
    const quantity = Number(item.quantity || 0);
    return sum + previousClose * quantity;
  }, 0);
});

const todaysPnl = computed(() => {
  return totalAssets.value - yesterdayHoldingsTotal.value;
});

const todaysPnlRate = computed(() => {
  if (yesterdayHoldingsTotal.value === 0) return 0;
  return (todaysPnl.value / yesterdayHoldingsTotal.value) * 100;
});


const mapAssetType = (assetType) => {
  if (!assetType) return 'Unknown';

  const value = String(assetType).toUpperCase();

  if (value === 'STOCK') return 'Stock';
  if (value === 'CRYPTO') return 'Crypto';
  if (value === 'BOND') return 'Bond';
  if (value === 'FUND') return 'Fund';
  if (value === 'CASH') return 'Cash';

  return assetType;
};

// --- 处理弹窗提交的数据 ---
const handleNewTransaction = (txnData) => {
  console.log("New Transaction submitted from Dashboard:", txnData);
  // 在真实应用中，你可以在这里向后端发送请求，然后重新计算上方的 Total Assets
  // 目前我们只是在控制台打印，弹窗会自动优雅关闭
};

const fetchHoldings = async () => {
  try {
    const data = await getHoldings()

    holdings.value = data.map((item) => ({
      id: item.id,
      ticker: item.assetCode,
      apiSymbol: item.assetCode,
      name: item.assetName,
      type: mapAssetType(item.assetType),
      quantity: item.quantity,
      costPrice: item.avgPrice,
      price: 'Loading...',
      marketValue: '--',
      pnl: 0
    }))

    await fetchLiveHoldingsData()
  } catch (error) {
    console.error('Failed to fetch holdings:', error)
  }
}


const fetchLiveHoldingsData = async () => {
  for (const asset of holdings.value) {
    if (asset.apiSymbol === 'STATIC') continue;

    try {
      const data = await getStockQuote(asset.apiSymbol);

      if (data && Number.isFinite(data.c)) {
        const livePrice = data.c;

        asset.price = usdFormatter.format(livePrice);
        const liveMarketValue = livePrice * asset.quantity;
        asset.marketValue = usdFormatter.format(liveMarketValue);

        const returnPct = ((livePrice - asset.costPrice) / asset.costPrice) * 100;
        asset.pnl = parseFloat(returnPct.toFixed(2)); 
      }
      /*
        Finnhub quote:
        c = current price
        pc = previous close
      */
      if (Number.isFinite(data.pc)) {
          asset.previousClose = data.pc;
      }
    } catch (error) {
      console.error(`Failed to fetch live data for ${asset.ticker}:`, error);
      asset.price = 'Error';
    }
  }
};

onMounted(() => {
  fetchHoldings();
  fetchAssetDistribution();
});
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
.btn-secondary { background: rgba(255, 255, 255, 0.08); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.12); padding: 0 1.2rem; height: 42px; }
/* 全局统一的 Add 按钮尺寸 */
.global-add-btn { height: 42px; padding: 0 1.5rem; font-size: 0.95rem; display: inline-flex; align-items: center; justify-content: center; white-space: nowrap; }

/* 按钮通用样式 */
.apple-btn { border-radius: 20px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.apple-link { background: none; border: none; color: #0a84ff; font-size: 0.9rem; font-weight: 500; cursor: pointer; padding: 0; transition: opacity 0.2s; }
.apple-link:hover { opacity: 0.8; }

/* 卡片与网格系统 */
.glass-card { background: rgba(30, 30, 32, 0.5); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 24px; padding: clamp(1.2rem, 2vw, 1.8rem); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2); box-sizing: border-box; overflow: hidden; }

.metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: clamp(1rem, 2vw, 1.5rem); margin-bottom: clamp(1.5rem, 3vh, 2rem); }
.metric-card { display: flex; flex-direction: column; gap: 0.5rem; padding: 1.5rem; }
.metric-card .label { color: #a1a1a6; font-size: 0.95rem; font-weight: 500; }
.metric-card .value { font-size: clamp(1.8rem, 2.5vw, 2.2rem); font-weight: 700; letter-spacing: -0.5px; }
.value-wrapper { display: flex; align-items: baseline; gap: 0.8rem; flex-wrap: wrap; }
.value.positive { color: #30d158; }
.badge { background: rgba(48, 209, 88, 0.2); color: #30d158; padding: 0.2rem 0.6rem; border-radius: 8px; font-size: 0.85rem; font-weight: 600; }
.badge.negative { background: rgba(255, 69, 58, 0.2); color: #ff453a;}
.charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: clamp(1rem, 2vw, 1.5rem); margin-bottom: clamp(1.5rem, 3vh, 2rem); }
@media (max-width: 1024px) { .charts-grid { grid-template-columns: 1fr; } }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; }
.portfolio-header { align-items: flex-start; flex-wrap: wrap; gap: 1rem; }
.header-left { display: flex; flex-direction: column; gap: 0.5rem; }
.compare-toggle { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; }
.compare-toggle input { display: none; }
.toggle-track { width: 32px; height: 18px; background: rgba(255,255,255,0.1); border-radius: 9px; position: relative; transition: 0.2s; border: 1px solid rgba(255,255,255,0.1); }
.toggle-thumb { position: absolute; top: 1px; left: 1px; width: 14px; height: 14px; background: #a1a1a6; border-radius: 50%; transition: 0.2s; }
.compare-toggle input:checked + .toggle-track { background: #0a84ff; border-color: #0a84ff; }
.compare-toggle input:checked + .toggle-track .toggle-thumb { transform: translateX(14px); background: #ffffff; }
.toggle-label { font-size: 0.85rem; color: #a1a1a6; font-weight: 500; }
.real-market-chart-container, .real-chart-container { width: 100%; }

.segmented-control { display: flex; background: rgba(0, 0, 0, 0.3); border-radius: 8px; padding: 4px; }
.segmented-control button { background: transparent; border: none; color: #a1a1a6; padding: 0.3rem 0.8rem; border-radius: 6px; font-size: 0.85rem; font-weight: 500; cursor: pointer; transition: 0.2s; }
.segmented-control button.active { background: rgba(255, 255, 255, 0.15); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.2); }

/* 表格样式 */
.table-card { padding-top: 1.5rem; }
.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; }
.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 600px; }
.apple-table th { color: #a1a1a6; font-size: 0.8rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; padding: 0 1rem 1rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: left; }
.apple-table td { padding: 1.2rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle; }
.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; }
.apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }

.col-asset { width: 35%; } .col-type { width: 10%; } .col-qty { width: 10%; } .col-price { width: 15%; } .col-value { width: 15%; } .col-return { width: 15%; }

.table-row { transition: background 0.2s; cursor: default; }
.table-row:hover td { background: rgba(255,255,255,0.03); }
.apple-table .right { text-align: right; }
.font-medium { font-weight: 500; }
.text-green { color: #30d158; } .text-red { color: #ff453a; }

.asset-identity { display: flex; align-items: center; gap: 1rem; }
.asset-name { font-weight: 600; font-size: 1rem; }
.asset-desc { color: #8e8e93; font-size: 0.85rem; margin-top: 0.2rem; }
.pill { background: rgba(255, 255, 255, 0.1); padding: 0.3rem 0.8rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; }

/* 页面加载动画 */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { opacity: 0; animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.delay-1 { animation-delay: 0.1s; } .delay-2 { animation-delay: 0.2s; } .delay-3 { animation-delay: 0.3s; } .delay-4 { animation-delay: 0.4s; }
</style>
