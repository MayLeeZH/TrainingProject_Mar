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

      <div class="summary-cards animate-fade-in delay-1">
        <div class="glass-card summary-card">
          <div class="summary-title">Total Holdings</div>
          <div class="summary-value">{{ totalHoldingsCount }}</div>
          <div class="summary-sub">Across {{ uniqueAssetClassesCount }} asset classes</div>
        </div>

        <div class="glass-card summary-card">
          <div class="summary-title">Total Assets</div>
          <div class="summary-value">{{ usdFormatter.format(totalAssetsValue) }}</div>
          <div class="summary-sub-split">
            <div class="split-item">
              <span class="split-label">Invested (Stocks, Bonds, Funds)</span>
              <span class="split-val">{{ usdFormatter.format(totalHoldingsValue) }}</span>
            </div>
            <div class="split-item">
              <span class="split-label">Cash Balance</span>
              <span class="split-val">{{ usdFormatter.format(cashBalance) }}</span>
            </div>
          </div>
        </div>

        <div class="glass-card summary-card">
          <div class="summary-title">Total Return</div>
          <div class="summary-value" :class="totalReturnRate >= 0 ? 'text-green' : 'text-red'">
            {{ totalReturnRate >= 0 ? '+' : '' }}{{ totalReturnRate.toFixed(2) }}%
          </div>
          <div class="summary-sub" :class="totalReturnDollar >= 0 ? 'text-green' : 'text-red'">
            {{ totalReturnDollar >= 0 ? '+' : '' }}{{ usdFormatter.format(Math.abs(totalReturnDollar)) }} All Time
          </div>
        </div>
      </div>

      <div class="glass-card table-card animate-fade-in delay-2">
        <div class="table-controls">
          <span class="sort-label">Sort by:</span>
          <button class="sort-chip" @click="toggleSort('total')" :class="{ active: sortKey === 'total' }">
            Total <span v-if="sortKey === 'total'">{{ sortAsc ? '↑' : '↓' }}</span>
          </button>
          <button class="sort-chip" @click="toggleSort('pnl')" :class="{ active: sortKey === 'pnl' }">
            Return <span v-if="sortKey === 'pnl'">{{ sortAsc ? '↑' : '↓' }}</span>
          </button>
          <button class="sort-chip" @click="toggleSort('dailyChange')" :class="{ active: sortKey === 'dailyChange' }">
            Daily Change <span v-if="sortKey === 'dailyChange'">{{ sortAsc ? '↑' : '↓' }}</span>
          </button>
        </div>

        <div class="table-responsive">
          <table class="apple-table">
            <thead>
              <tr>
                <th class="col-asset">Asset</th>
                <th class="col-code">Code</th>
                <th class="right col-cost">Avg Price</th>
                <th class="right col-qty">Quality</th>
                <th class="right col-price">Current Price</th>
                <th class="right col-value">Total</th>
                <th class="right col-return">Total Return</th>
                <th class="right col-daily">Daily Change</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="asset in enrichedHoldings" :key="asset.id || asset.ticker">
                <tr
                  class="table-row"
                  @click="toggleRow(asset.ticker)"
                  :class="{ 'is-expanded': expandedRow === asset.ticker }"
                >
                  <td>
                    <div class="asset-name">{{ asset.name }}</div>
                  </td>
                  <td>
                    <div class="asset-code font-medium">{{ asset.ticker }}</div>
                  </td>
                  <td class="right text-muted">{{ asset.cost }}</td>
                  <td class="right">{{ asset.quantity }}</td>
                  <td class="right">{{ asset.price }}</td>
                  <td class="right font-medium">{{ asset.marketValue }}</td>
                  <td class="right">
                    <span :class="asset.pnl > 0 ? 'text-green' : (asset.pnl < 0 ? 'text-red' : '')">
                      {{ asset.pnl > 0 ? '+' : '' }}{{ asset.pnl }}%
                    </span>
                  </td>
                  <td class="right">
                    <span :class="asset.dailyChangePct > 0 ? 'text-green' : (asset.dailyChangePct < 0 ? 'text-red' : '')">
                      {{ asset.dailyChangePct > 0 ? '+' : '' }}{{ asset.dailyChangePct !== undefined ? asset.dailyChangePct + '%' : '--' }}
                    </span>
                  </td>
                </tr>

                <tr v-if="expandedRow === asset.ticker" class="expanded-chart-row">
                  <td colspan="8">
                    <div class="expanded-content animate-fade-in-fast">
                      <div class="expanded-header">
                        <h4>{{ asset.ticker }} Trend</h4>
                        <button class="apple-link" @click.stop="toggleRow(asset.ticker)">Close</button>
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
import { getHoldings } from '../apis/holdingService.js';
import { getStockQuote } from '../apis/finnhubService.js';
import StockChart from '../components/StockChart.vue';
import AddTransactionModal from '../components/AddTransactionModal.vue';

const isAddModalOpen = ref(false);
const expandedRow = ref(null);
const holdings = ref([]);

// TODO: 这个应该灵活变动的，受到买入卖出影响
const cashBalance = ref(50000.0);

const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
const numFormatter = new Intl.NumberFormat('en-US');

const toggleRow = (ticker) => {
  expandedRow.value = expandedRow.value === ticker ? null : ticker;
};

const mapAssetType = (assetType) => {
  if (!assetType) return 'Unknown';

  const value = String(assetType).toUpperCase();

  if (value === 'STOCK') return 'Stock';
  if (value === 'CRYPTO') return 'Crypto';
  if (value === 'BOND') return 'Bond';
  if (value === 'FUND') return 'Fund';

  return assetType;
};

const totalHoldingsCount = computed(() => holdings.value.length);

const uniqueAssetClassesCount = computed(() => {
  const types = new Set(holdings.value.map((h) => h.type));
  return types.size;
});

const totalHoldingsValue = computed(() => {
  return holdings.value.reduce((sum, h) => sum + h.rawMarketValue, 0);
});

const totalAssetsValue = computed(() => totalHoldingsValue.value + cashBalance.value);

const totalCostValue = computed(() => {
  return holdings.value.reduce((sum, h) => sum + h.rawCostValue, 0);
});

const totalReturnRate = computed(() => {
  if (totalCostValue.value === 0) return 0;
  return ((totalHoldingsValue.value - totalCostValue.value) / totalCostValue.value) * 100;
});

const totalReturnDollar = computed(() => {
  return totalHoldingsValue.value - totalCostValue.value;
});

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
        asset.rawMarketValue = liveMarketValue;

        const returnPct = asset.costPrice === 0 ? 0 : ((livePrice - asset.costPrice) / asset.costPrice) * 100;
        asset.pnl = parseFloat(returnPct.toFixed(2));

        if (typeof data.dp === 'number') {
          asset.dailyChangePct = parseFloat(data.dp.toFixed(2));
        }

        const dollarDiff = (livePrice - asset.costPrice) * asset.quantity;
        asset.pnlDollar = numFormatter.format(Math.abs(dollarDiff).toFixed(2));
      }
    } catch (error) {
      console.error(`Failed to fetch live data for ${asset.ticker}:`, error);
      asset.price = 'Error';
    }
  }
};

const fetchHoldings = async () => {
  try {
    const data = await getHoldings();

    holdings.value = data.map((item) => {
      const quantity = Number(item.quantity || 0);
      const costPrice = Number(item.avgPrice || 0);
      const rawCostValue = quantity * costPrice;

      return {
        id: item.id,
        ticker: item.assetCode,
        apiSymbol: item.assetCode,
        name: item.assetName,
        type: mapAssetType(item.assetType),
        quantity,
        costPrice,
        cost: usdFormatter.format(costPrice),
        price: 'Loading...',
        marketValue: usdFormatter.format(rawCostValue),
        rawMarketValue: rawCostValue,
        rawCostValue,
        pnl: 0,
        pnlDollar: '0',
        dailyChangePct: 0
      };
    });

    await fetchLiveHoldingsData();
  } catch (error) {
    console.error('Failed to fetch holdings:', error);
  }
};

onMounted(() => {
  fetchHoldings();
});

const handleNewTransaction = (txnData) => {
  console.log('New Transaction submitted from Holdings page:', txnData);
};

const sortKey = ref(null);
const sortAsc = ref(false);

const toggleSort = (key) => {
  if (sortKey.value === key) {
    if (!sortAsc.value) {
      sortAsc.value = true;
    } else {
      sortKey.value = null;
      sortAsc.value = false;
    }
  } else {
    sortKey.value = key;
    sortAsc.value = false;
  }
};

const enrichedHoldings = computed(() => {
  const result = [...holdings.value];

  if (sortKey.value) {
    result.sort((a, b) => {
      let valA;
      let valB;

      if (sortKey.value === 'total') {
        valA = a.rawMarketValue;
        valB = b.rawMarketValue;
      } else if (sortKey.value === 'pnl') {
        valA = a.pnl;
        valB = b.pnl;
      } else if (sortKey.value === 'dailyChange') {
        valA = a.dailyChangePct;
        valB = b.dailyChangePct;
      }

      if (valA === valB) return 0;
      if (sortAsc.value) return valA > valB ? 1 : -1;
      return valA < valB ? 1 : -1;
    });
  }

  return result;
});
</script>

<style scoped>
/* =========================================================
   鍩虹甯冨眬涓庤儗鏅?
========================================================= */
.apple-layout { display: flex; height: 100vh; width: 100vw; max-width: 100%; background-color: #000000; color: #f5f5f7; font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "SF Pro Display", sans-serif; overflow: hidden; position: relative; box-sizing: border-box; }
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ambient-bg { position: absolute; border-radius: 50%; filter: blur(100px); z-index: 0; opacity: 0.5; animation: float 20s ease-in-out infinite alternate; pointer-events: none; }
.blob-1 { top: -10%; left: -10%; width: 50vw; height: 50vw; background: radial-gradient(circle, #2c1a59 0%, rgba(0,0,0,0) 70%); }
.blob-2 { bottom: -20%; right: -10%; width: 60vw; height: 60vw; background: radial-gradient(circle, #0a2e3f 0%, rgba(0,0,0,0) 70%); }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(5%, 5%) scale(1.1); } }

/* 渚ц竟鏍?*/
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

/* 涓诲唴瀹瑰尯 & 缁熶竴 Header */
.main-content { flex: 1; min-width: 0; padding: clamp(1.5rem, 3vw, 3rem) clamp(2rem, 4vw, 4rem); overflow-y: auto; overflow-x: hidden; z-index: 10; box-sizing: border-box; }

.top-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: clamp(1.5rem, 3vh, 2rem); flex-wrap: wrap; gap: 1rem; min-height: 60px; }

/* 椤堕儴姒傝鍗＄墖 */
.summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; margin-bottom: 2rem; }
.summary-card { display: flex; flex-direction: column; padding: 1.5rem; }
.summary-title { color: #a1a1a6; font-size: 0.9rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.8rem; }
.summary-value { font-size: 2rem; font-weight: 700; letter-spacing: -0.5px; margin-bottom: 0.5rem; }
.summary-sub { color: #8e8e93; font-size: 0.9rem; margin-top: auto; }
.summary-sub-split { display: flex; flex-direction: column; gap: 0.6rem; margin-top: auto; border-top: 1px solid rgba(255, 255, 255, 0.08); padding-top: 1rem; }
.split-item { display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem; }
.split-label { color: #a1a1a6; }
.split-val { font-weight: 600; }

@media (max-width: 1024px) {
  .summary-cards { grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); }
}
.header-titles { min-width: 0; }
.page-title { font-size: clamp(2rem, 3vw, 2.5rem); font-weight: 700; margin: 0; letter-spacing: -1px; }
.datetime { color: #a1a1a6; font-size: clamp(0.9rem, 1.2vw, 1.1rem); margin: 0.5rem 0 0 0; font-weight: 500; }
.header-actions { display: flex; align-items: center; }

/* 鎸夐挳閫氱敤鏍峰紡 */
.apple-btn { border-radius: 20px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.global-add-btn { height: 42px; padding: 0 1.5rem; font-size: 0.95rem; display: inline-flex; align-items: center; justify-content: center; white-space: nowrap; }

/* 鍗＄墖 */
.glass-card { background: rgba(30, 30, 32, 0.5); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 24px; padding: clamp(1.2rem, 2vw, 1.8rem); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2); box-sizing: border-box; overflow: hidden; }

/* 琛ㄦ牸鎺у埗锛堟帓搴忔寜閽瓑锛?*/
.table-controls { display: flex; align-items: center; gap: 0.8rem; padding: 0 0.5rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.08); margin-bottom: 1rem; }
.sort-label { color: #a1a1a6; font-size: 0.9rem; font-weight: 500; }
.sort-chip { background: rgba(255,255,255,0.05); color: #a1a1a6; border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 0.4rem 0.8rem; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; display: inline-flex; align-items: center; gap: 0.3rem; }
.sort-chip:hover { background: rgba(255,255,255,0.1); color: #fff; }
.sort-chip.active { background: rgba(255,255,255,0.15); color: #fff; border-color: rgba(255,255,255,0.3); }

/* 琛ㄦ牸涓讳綋 */
.table-card { padding-top: 1.5rem; }
.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; }
.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 900px; }

.apple-table th { color: #a1a1a6; font-size: 0.8rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; padding: 0 1rem 1rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: left; }
.apple-table td { padding: 1.2rem 1rem; vertical-align: middle; }
.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; }
.apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }

.col-asset { width: 16%; } .col-code { width: 12%; } .col-cost { width: 12%; } .col-qty { width: 12%; } .col-price { width: 12%; } .col-value { width: 14%; } .col-return { width: 11%; } .col-daily { width: 11%; }

.apple-table .right { text-align: right; }
.apple-table .center { text-align: center; }
.font-medium { font-weight: 500; }
.text-muted { color: #8e8e93; font-size: 0.9rem; }
.text-green { color: #30d158; } .text-red { color: #ff453a; }

/* 璧勪骇淇℃伅 */
.asset-name { font-weight: 600; font-size: 0.95rem; }
.asset-code { border: 1px solid rgba(255,255,255,0.1); padding: 0.2rem 0.5rem; border-radius: 6px; display: inline-block; font-size: 0.8rem; background: rgba(255,255,255,0.03); color: #e5e5ea; }

/* EXPANDABLE ROWS (灞曞紑琛屾牱寮? */
.table-row { cursor: pointer; transition: all 0.2s ease; border-bottom: 1px solid rgba(255,255,255,0.04); }
.table-row td { border-bottom: 1px solid rgba(255,255,255,0.04); transition: background 0.2s; }
.table-row:hover td { background: rgba(255,255,255,0.02); }
.table-row.is-expanded td { background: rgba(255, 255, 255, 0.04); border-bottom: none; }
.expanded-chart-row td { padding: 0; border-bottom: 1px solid rgba(255, 255, 255, 0.08); }
.expanded-content { background: rgba(0, 0, 0, 0.3); box-shadow: inset 0 5px 15px rgba(0, 0, 0, 0.3); padding: 1.5rem 2rem; border-radius: 0 0 16px 16px; margin-bottom: 1rem; }
.expanded-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.expanded-header h4 { margin: 0; font-size: 1.1rem; color: #fff; font-weight: 600; }
.real-chart-container { width: 100%; }

/* 閾炬帴鏍峰紡 */
.apple-link { background: none; border: none; color: #0a84ff; font-size: 0.9rem; font-weight: 500; cursor: pointer; padding: 0; transition: opacity 0.2s; display: inline-flex; align-items: center; gap: 0.3rem;}
.apple-link:hover { opacity: 0.8; text-decoration: underline; }

/* 鍔ㄧ敾 */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { opacity: 0; animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.delay-1 { animation-delay: 0.1s; } .delay-2 { animation-delay: 0.2s; }
@keyframes fadeInFast { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in-fast { animation: fadeInFast 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
</style>
