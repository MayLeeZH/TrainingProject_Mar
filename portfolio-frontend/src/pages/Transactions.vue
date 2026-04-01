<template>
  <div class="apple-layout" @click="closeDropdown">
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
          <h1 class="page-title text-truncate">Transaction History</h1>
          <p class="datetime text-truncate">All past trades and transfers</p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-primary global-add-btn" @click="isAddModalOpen = true">
            + Add Transaction
          </button>
        </div>
      </header>

      <div class="glass-card table-card animate-fade-in delay-2">
        <p v-if="loadError" class="api-error">{{ loadError }}</p>
        <p v-else-if="loading" class="api-loading">加载中…</p>
        <div class="card-toolbar">
          <div class="search-box">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input type="text" v-model="searchQuery" class="apple-search-input" placeholder="Search by name or code..." />
          </div>
          </div>
        
        <div class="table-responsive">
          <table class="apple-table">
            <thead>
              <tr>
                <th class="col-time">Time</th>
                <th class="col-name">Name</th>
                <th class="col-code">Code</th>
                <th class="center col-type">
                  <div class="dropdown-wrapper">
                    <div class="filter-trigger" @click.stop="toggleDropdown">
                      Type 
                      <span class="chevron" :class="{ 'chevron-up': isDropdownOpen }">▼</span>
                      <span v-if="filterType !== 'All'" class="filter-dot"></span>
                    </div>
                    <div v-if="isDropdownOpen" class="glass-dropdown animate-fade-in-fast" @click.stop>
                      <div class="dropdown-item" :class="{ active: filterType === 'All' }" @click="setFilter('All')">All Types</div>
                      <div class="dropdown-item" :class="{ active: filterType === 'Buy' }" @click="setFilter('Buy')">Buy Only</div>
                      <div class="dropdown-item" :class="{ active: filterType === 'Sell' }" @click="setFilter('Sell')">Sell Only</div>
                    </div>
                  </div>
                </th>
                <th class="right col-qty">Quantity</th>
                <th class="right col-price">Price</th>
                <th class="right col-total">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredTransactions.length === 0">
                <td colspan="7" class="empty-state">No transactions found matching your criteria.</td>
              </tr>
              <tr v-for="txn in filteredTransactions" :key="txn.id" class="table-row">
                <td class="text-muted">{{ txn.time }}</td>
                <td class="font-medium">{{ txn.name }}</td>
                <td class="code-text">{{ txn.code }}</td>
                <td class="center">
                  <span :class="['type-pill', txn.type.toLowerCase() === 'buy' ? 'pill-buy' : 'pill-sell']">
                    {{ txn.type }}
                  </span>
                </td>
                <td class="right">{{ txn.quantity }}</td>
                <td class="right">{{ txn.price }}</td>
                <td class="right font-medium">{{ txn.total }}</td>
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
import AddTransactionModal from '../components/AddTransactionModal.vue';
import { fetchTransactionsByUser } from '../apis/portfolioApi.js';

const searchQuery = ref('');
const filterType = ref('All');
const isDropdownOpen = ref(false);
const isAddModalOpen = ref(false);

/** 当前登录用户 id，与后端 GET /transactions/users/{userId} 一致；可在 .env 里配置 VITE_DEFAULT_USER_ID */
const defaultUserId = Number(import.meta.env.VITE_DEFAULT_USER_ID) || 1;

const transactions = ref([]);
const loading = ref(false);
const loadError = ref('');

const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

function mapBackendRow(row) {
  const holding = row.holdingRecord || {};
  const typeRaw = (row.transactionType || '').toString();
  const typeLabel = typeRaw === 'SELL' ? 'Sell' : 'Buy';
  const qty = row.quantity ?? 0;
  const price = row.transactionalPrice ?? 0;
  const total = qty * price;
  let timeStr = '';
  if (row.time) {
    const d = new Date(row.time);
    timeStr = Number.isNaN(d.getTime())
      ? String(row.time)
      : `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`;
  }
  return {
    id: row.id,
    time: timeStr,
    // 优先用自己的字段，回退到关联持仓
    name: row.stockName || holding.assetName || '—',
    code: row.stockCode || holding.assetCode || '—',
    type: typeLabel,
    quantity: qty,
    price: usdFormatter.format(price),
    total: usdFormatter.format(total),
  };
}

async function loadTransactions() {
  loading.value = true;
  loadError.value = '';
  try {
    const list = await fetchTransactionsByUser(defaultUserId);
    transactions.value = Array.isArray(list) ? list.map(mapBackendRow) : [];
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
    transactions.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadTransactions();
});

const toggleDropdown = () => isDropdownOpen.value = !isDropdownOpen.value;
const closeDropdown = () => { if (isDropdownOpen.value) isDropdownOpen.value = false; };
const setFilter = (type) => { filterType.value = type; isDropdownOpen.value = false; };

const filteredTransactions = computed(() => {
  let result = transactions.value;
  if (filterType.value !== 'All') result = result.filter(txn => txn.type === filterType.value);
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(txn =>
      String(txn.name).toLowerCase().includes(query) || String(txn.code).toLowerCase().includes(query)
    );
  }
  return result;
});

const handleNewTransaction = (txnData) => {
  const today = new Date();
  const timeStr = `${today.getFullYear()}/${String(today.getMonth() + 1).padStart(2, '0')}/${String(today.getDate()).padStart(2, '0')}`;
  const nameMap = { AAPL: 'Apple Inc.', NVDA: 'NVIDIA Corp.', VOO: 'Vanguard S&P 500', BTC: 'Bitcoin', TSLA: 'Tesla Inc.' };
  const txnName = nameMap[txnData.code] || `${txnData.code} Asset`;
  transactions.value.unshift({
    id: Date.now(),
    time: timeStr,
    name: txnName,
    code: txnData.code,
    type: txnData.type,
    quantity: txnData.quantity,
    price: usdFormatter.format(txnData.price),
    total: usdFormatter.format(txnData.total),
  });
};
</script>

<style scoped>
/* =========================================================
   基础布局
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

/* 全局统一的 Add 按钮尺寸 */
.global-add-btn { height: 42px; padding: 0 1.5rem; font-size: 0.95rem; display: inline-flex; align-items: center; justify-content: center; white-space: nowrap; }

.glass-card { background: rgba(30, 30, 32, 0.5); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 24px; padding: clamp(1.2rem, 2vw, 1.8rem); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2); box-sizing: border-box; overflow: hidden; }
.table-card { padding-top: 1.5rem; }

.api-error { color: #ff453a; font-size: 0.9rem; margin: 0 0 1rem 0; }
.api-loading { color: #a1a1a6; font-size: 0.9rem; margin: 0 0 1rem 0; }

/* 工具栏与搜索框 */
.card-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
.search-box { position: relative; width: 100%; max-width: 300px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #636366; width: 18px; height: 18px; }
.apple-search-input { width: 100%; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 10px; padding: 0.6rem 1rem 0.6rem 2.4rem; color: #fff; font-size: 0.95rem; outline: none; transition: all 0.2s ease; box-sizing: border-box; }
.apple-search-input:focus { background: rgba(255, 255, 255, 0.08); border-color: #0a84ff; box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.2); }
.apple-search-input::placeholder { color: #636366; }

.apple-btn { border-radius: 20px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }

/* 下拉菜单 (Dropdown Filter) */
.dropdown-wrapper { position: relative; display: inline-block; }
.filter-trigger { cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; position: relative; }
.filter-trigger:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
.chevron { font-size: 0.7rem; transition: transform 0.2s ease; }
.chevron-up { transform: rotate(180deg); }
.filter-dot { position: absolute; top: 4px; right: 2px; width: 6px; height: 6px; background-color: #0a84ff; border-radius: 50%; box-shadow: 0 0 4px rgba(10, 132, 255, 0.6); }
.glass-dropdown { position: absolute; top: calc(100% + 8px); left: 50%; transform: translateX(-50%); background: rgba(40, 40, 42, 0.85); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; padding: 6px; min-width: 120px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5); z-index: 100; text-align: left; }
.dropdown-item { padding: 8px 12px; border-radius: 8px; color: #f5f5f7; font-size: 0.85rem; cursor: pointer; transition: background 0.1s; font-weight: 500; text-transform: none; letter-spacing: 0;}
.dropdown-item:hover { background: #0a84ff; color: white; }
.dropdown-item.active { background: rgba(255, 255, 255, 0.1); color: #0a84ff; }
.dropdown-item.active:hover { background: #0a84ff; color: white; }

/* 表格样式 */
.table-responsive { overflow-x: auto; width: 100%; -webkit-overflow-scrolling: touch; overflow-y: visible; }
.apple-table { width: 100%; border-collapse: separate; border-spacing: 0; min-width: 800px; table-layout: fixed; }
.col-time { width: 14%; } .col-name { width: 22%; } .col-code { width: 12%; } .col-type { width: 12%; } .col-qty { width: 12%; } .col-price { width: 14%; } .col-total { width: 14%; }
.apple-table th { color: #a1a1a6; font-size: 0.8rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; padding: 0 1rem 1rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.08); white-space: nowrap; text-align: left; }
.apple-table td { padding: 1.2rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle; white-space: nowrap; text-align: left; }
.apple-table th:first-child, .apple-table td:first-child { padding-left: 0; } .apple-table th:last-child, .apple-table td:last-child { padding-right: 0; }
.table-row { transition: background 0.2s; cursor: default; } .table-row:hover td { background: rgba(255,255,255,0.03); }
.apple-table .right { text-align: right; } .apple-table .center { text-align: center; }
.font-medium { font-weight: 500; } .text-muted { color: #8e8e93; font-size: 0.9rem; }
.code-text { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, monospace; color: #a1a1a6; font-size: 0.9rem; }
.type-pill { padding: 0.3rem 0.8rem; border-radius: 12px; font-size: 0.8rem; font-weight: 600; display: inline-block; }
.pill-buy { background: rgba(48, 209, 88, 0.15); color: #30d158; }
.pill-sell { background: rgba(255, 69, 58, 0.15); color: #ff453a; }
.empty-state { text-align: center; color: #636366; padding: 3rem 0 !important; font-style: italic; }

/* 页面加载动画 */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { opacity: 0; animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.delay-1 { animation-delay: 0.1s; } .delay-2 { animation-delay: 0.2s; }
@keyframes fadeInFast { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in-fast { animation: fadeInFast 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
</style>