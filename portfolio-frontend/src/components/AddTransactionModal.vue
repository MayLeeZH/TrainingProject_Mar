<template>
  <div v-if="modelValue" class="modal-overlay animate-fade-in-fast" @click="closeModal">
    <div class="glass-card modal-card animate-bounce-in" @click.stop>
      
      <div class="modal-header">
        <h3>New Transaction</h3>
        <button class="icon-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>Type</label>
          <div class="segmented-control">
            <button :class="{ active: newTxn.type === 'Buy' }" @click="newTxn.type = 'Buy'" class="segment-btn buy-btn">Buy</button>
            <button :class="{ active: newTxn.type === 'Sell' }" @click="newTxn.type = 'Sell'" class="segment-btn sell-btn">Sell</button>
          </div>
        </div>

        <!-- Smart Asset Search Field -->
        <div class="form-group search-wrapper">
          <label>Asset</label>

          <!-- Selected chip: shown after user picks an asset -->
          <div v-if="selectedAsset" class="selected-chip">
            <span class="chip-code">{{ selectedAsset.displaySymbol }}</span>
            <span class="chip-name">{{ selectedAsset.description }}</span>
            <button class="chip-clear" @click="clearSelection">✕</button>
          </div>

          <!-- Search input: shown when nothing is selected -->
          <div v-else class="search-input-wrap">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              type="text"
              v-model="searchQuery"
              @input="onSearchInput"
              @focus="onInputFocus"
              @blur="onInputBlur"
              placeholder="Search by name or code (e.g. Apple, AAPL)"
              class="apple-input search-input"
              autocomplete="off"
            />
            <span v-if="isSearching" class="search-spinner">⏳</span>
          </div>

          <!-- Dropdown results -->
          <div v-if="showDropdown" class="search-dropdown">
            <div v-if="isSearching" class="dd-state">Searching...</div>
            <div v-else-if="searchResults.length === 0 && searchQuery.trim().length >= 1" class="dd-state">No results for "{{ searchQuery }}"</div>
            <div
              v-for="result in searchResults"
              :key="result.symbol"
              class="dd-item"
              @mousedown.prevent="selectAsset(result)"
            >
              <span class="dd-symbol">{{ result.displaySymbol }}</span>
              <span class="dd-name">{{ result.description }}</span>
              <span class="dd-type">{{ result.type }}</span>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group half">
            <label>Quantity</label>
            <input type="number" v-model="newTxn.quantity" placeholder="0.00" class="apple-input" min="0" step="0.01" />
          </div>
          <div class="form-group half">
            <label>Price (USD)</label>
            <input type="number" v-model="newTxn.price" placeholder="0.00" class="apple-input" min="0" step="0.01" />
          </div>
        </div>

        <div class="form-group total-display">
          <label>Total Value</label>
          <div class="total-amount">{{ calculatedTotal }}</div>
        </div>
      </div>

      <p v-if="validationError" class="validation-error">{{ validationError }}</p>

      <div class="modal-footer">
        <button class="apple-btn btn-secondary" @click="closeModal" :disabled="isSubmitting">Cancel</button>
        <button class="apple-btn btn-primary" @click="submit" :disabled="!isFormValid || isSubmitting">
          {{ isSubmitting ? 'Validating...' : 'Confirm' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getStockQuote, searchSymbol } from '../apis/finnhubService.js';

const props = defineProps({ modelValue: Boolean });
const emit = defineEmits(['update:modelValue', 'submit']);

const newTxn = ref({ type: 'Buy', quantity: '', price: '' });
const validationError = ref('');
const isSubmitting = ref(false);

// --- Smart search state ---
const searchQuery = ref('');
const searchResults = ref([]);
const selectedAsset = ref(null); // { symbol, displaySymbol, description, type }
const isSearching = ref(false);
const showDropdown = ref(false);
let searchTimer = null;

const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

const calculatedTotal = computed(() => {
  const qty = parseFloat(newTxn.value.quantity) || 0;
  const prc = parseFloat(newTxn.value.price) || 0;
  return usdFormatter.format(qty * prc);
});

const isFormValid = computed(() => {
  return selectedAsset.value !== null
    && parseFloat(newTxn.value.quantity) > 0
    && parseFloat(newTxn.value.price) > 0;
});

// --- Search handlers ---
function onSearchInput() {
  clearTimeout(searchTimer);
  const q = searchQuery.value.trim();
  if (q.length < 1) {
    searchResults.value = [];
    showDropdown.value = false;
    return;
  }
  isSearching.value = true;
  showDropdown.value = true;
  searchTimer = setTimeout(() => doSearch(q), 400);
}

function onInputFocus() {
  if (searchQuery.value.trim().length >= 1) showDropdown.value = true;
}

function onInputBlur() {
  // Small delay so mousedown on dropdown item fires first
  setTimeout(() => { showDropdown.value = false; }, 150);
}

async function doSearch(q) {
  try {
    const results = await searchSymbol(q);
    searchResults.value = results;
  } catch {
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
}

function selectAsset(result) {
  selectedAsset.value = result;
  searchQuery.value = '';
  searchResults.value = [];
  showDropdown.value = false;
  validationError.value = '';
}

function clearSelection() {
  selectedAsset.value = null;
  searchQuery.value = '';
  searchResults.value = [];
}

// --- Modal lifecycle ---
const closeModal = () => {
  emit('update:modelValue', false);
  setTimeout(() => {
    newTxn.value = { type: 'Buy', quantity: '', price: '' };
    validationError.value = '';
    isSubmitting.value = false;
    selectedAsset.value = null;
    searchQuery.value = '';
    searchResults.value = [];
  }, 300);
};

// --- Submit with price validation ---
const submit = async () => {
  if (!isFormValid.value) return;

  validationError.value = '';
  isSubmitting.value = true;

  const code = selectedAsset.value.displaySymbol.toUpperCase();

  try {
    const data = await getStockQuote(code);
    if (!data || typeof data.c !== 'number' || data.c === 0) {
      validationError.value = 'add failure';
      isSubmitting.value = false;
      return;
    }
  } catch {
    validationError.value = 'add failure';
    isSubmitting.value = false;
    return;
  }

  const qty = parseFloat(newTxn.value.quantity);
  const prc = parseFloat(newTxn.value.price);

  emit('submit', {
    type: newTxn.value.type,
    name: selectedAsset.value.description,
    code: code,
    quantity: qty,
    price: prc,
    total: qty * prc
  });

  isSubmitting.value = false;
  closeModal();
};
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); display: flex; justify-content: center; align-items: center; z-index: 9999; }
.modal-card { width: 90%; max-width: 440px; background: rgba(30, 30, 32, 0.85); border: 1px solid rgba(255,255,255,0.1); border-radius: 24px; padding: 2rem; display: flex; flex-direction: column; gap: 1.5rem; box-shadow: 0 20px 60px rgba(0,0,0,0.5); }
.modal-header { display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.3rem; font-weight: 600; letter-spacing: -0.5px; color: #fff; }
.icon-btn { background: none; border: none; color: #a1a1a6; font-size: 1.2rem; cursor: pointer; transition: color 0.2s; padding: 0; }
.icon-btn:hover { color: #fff; }
.modal-body { display: flex; flex-direction: column; gap: 1.2rem; }
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-row { display: flex; gap: 1rem; }
.half { flex: 1; }
.form-group label { font-size: 0.85rem; color: #a1a1a6; font-weight: 500; text-align: left; }
.apple-input { width: 100%; box-sizing: border-box; background: rgba(0, 0, 0, 0.3); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 10px; padding: 0.8rem 1rem; color: #fff; font-size: 1rem; font-family: inherit; outline: none; transition: all 0.2s ease; }
.apple-input:focus { border-color: #0a84ff; box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.25); background: rgba(0, 0, 0, 0.5); }
.apple-input::placeholder { color: #636366; }

/* Smart search */
.search-wrapper { position: relative; }
.search-input-wrap { position: relative; display: flex; align-items: center; }
.search-icon { position: absolute; left: 12px; width: 16px; height: 16px; color: #636366; pointer-events: none; flex-shrink: 0; }
.search-input { padding-left: 2.4rem !important; padding-right: 2rem !important; }
.search-spinner { position: absolute; right: 12px; font-size: 0.85rem; }

/* Selected chip */
.selected-chip { display: flex; align-items: center; gap: 0.6rem; background: rgba(10, 132, 255, 0.12); border: 1px solid rgba(10, 132, 255, 0.35); border-radius: 10px; padding: 0.75rem 1rem; }
.chip-code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-weight: 700; font-size: 0.95rem; color: #0a84ff; flex-shrink: 0; }
.chip-name { font-size: 0.9rem; color: #e5e5ea; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chip-clear { margin-left: auto; background: none; border: none; color: #636366; font-size: 0.9rem; cursor: pointer; padding: 0 0 0 0.5rem; flex-shrink: 0; transition: color 0.2s; }
.chip-clear:hover { color: #ff453a; }

/* Dropdown */
.search-dropdown { position: absolute; top: calc(100% + 6px); left: 0; right: 0; background: rgba(40, 40, 42, 0.96); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 12px; padding: 6px; box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6); z-index: 10000; }
.dd-state { padding: 0.8rem 1rem; font-size: 0.85rem; color: #636366; text-align: center; }
.dd-item { display: flex; align-items: center; gap: 0.6rem; padding: 0.65rem 0.9rem; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
.dd-item:hover { background: rgba(10, 132, 255, 0.2); }
.dd-symbol { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-weight: 700; font-size: 0.9rem; color: #fff; min-width: 52px; flex-shrink: 0; }
.dd-name { font-size: 0.85rem; color: #a1a1a6; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dd-type { font-size: 0.75rem; color: #636366; flex-shrink: 0; background: rgba(255,255,255,0.06); padding: 0.1rem 0.5rem; border-radius: 6px; }

/* Segmented control */
.segmented-control { display: flex; background: rgba(0, 0, 0, 0.3); border-radius: 10px; padding: 4px; gap: 4px; }
.segment-btn { flex: 1; border: none; background: transparent; color: #a1a1a6; padding: 0.6rem 0; border-radius: 8px; font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: all 0.2s ease; }
.segment-btn:hover:not(.active) { color: #fff; background: rgba(255, 255, 255, 0.05); }
.segment-btn.active.buy-btn { background: #30d158; color: #000; box-shadow: 0 2px 8px rgba(48, 209, 88, 0.3); }
.segment-btn.active.sell-btn { background: #ff453a; color: #fff; box-shadow: 0 2px 8px rgba(255, 69, 58, 0.3); }

/* Total display */
.total-display { background: rgba(255, 255, 255, 0.03); padding: 1rem; border-radius: 12px; border: 1px dashed rgba(255, 255, 255, 0.1); text-align: left; }
.total-amount { font-size: 1.8rem; font-weight: 700; color: #fff; letter-spacing: -1px; }

/* Footer */
.modal-footer { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 0.5rem; }
.apple-btn { padding: 0.7rem 1.4rem; border-radius: 20px; font-weight: 600; font-size: 0.95rem; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.apple-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.btn-secondary { background: rgba(255,255,255,0.1); color: #fff; }

/* Validation error */
.validation-error { color: #ff453a; font-size: 0.9rem; padding: 0.5rem; text-align: center; margin: 0; font-weight: 500; }

/* Animations */
@keyframes elasticDrop { 0% { opacity: 0; transform: translateY(-60px) scale(0.95); } 50% { opacity: 1; transform: translateY(12px) scale(1.02); } 75% { transform: translateY(-4px) scale(0.99); } 100% { transform: translateY(0) scale(1); } }
.animate-bounce-in { animation: elasticDrop 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; }
@keyframes fadeInFast { from { opacity: 0; } to { opacity: 1; } }
.animate-fade-in-fast { animation: fadeInFast 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
</style>