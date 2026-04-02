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

        <div class="form-group">
          <label>Asset Code</label>
          <input type="text" v-model="newTxn.code" placeholder="e.g. AAPL, NVDA" class="apple-input" />
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
import { getStockQuote } from '../apis/finnhubService.js';

// 接收父组件传来的 v-model (用于控制显示隐藏)
const props = defineProps({
  modelValue: Boolean
});

// 定义派发给父组件的事件
const emit = defineEmits(['update:modelValue', 'submit']);

const newTxn = ref({ type: 'Buy', code: '', quantity: '', price: '' });
const validationError = ref('');
const isSubmitting = ref(false);
const usdFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

const calculatedTotal = computed(() => {
  const qty = parseFloat(newTxn.value.quantity) || 0;
  const prc = parseFloat(newTxn.value.price) || 0;
  return usdFormatter.format(qty * prc);
});

const isFormValid = computed(() => {
  return newTxn.value.code.trim() !== '' && parseFloat(newTxn.value.quantity) > 0 && parseFloat(newTxn.value.price) > 0;
});

const closeModal = () => {
  emit('update:modelValue', false);
  // 延迟清空表单，等待动画结束
  setTimeout(() => { 
    newTxn.value = { type: 'Buy', code: '', quantity: '', price: '' }; 
    validationError.value = '';
    isSubmitting.value = false;
  }, 300);
};

const submit = async () => {
  if (!isFormValid.value) return;
  
  validationError.value = '';
  isSubmitting.value = true;
  
  const code = newTxn.value.code.toUpperCase().trim();
  
  try {
    const data = await getStockQuote(code);
    if (!data || typeof data.c !== 'number' || data.c === 0) {
      validationError.value = `Add failure: The asset code '${code}' does not exist.`;
      isSubmitting.value = false;
      return;
    }
  } catch (err) {
    validationError.value = `Add failure: The asset code '${code}' does not exist.`;
    isSubmitting.value = false;
    return;
  }
  
  const qty = parseFloat(newTxn.value.quantity);
  const prc = parseFloat(newTxn.value.price);
  
  // 将整理好的数据发送给所在页面
  emit('submit', {
    type: newTxn.value.type,
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
/* 包含 Modal 专用的所有样式，与外部隔离，保持干净 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); display: flex; justify-content: center; align-items: center; z-index: 9999; }
.modal-card { width: 90%; max-width: 420px; background: rgba(30, 30, 32, 0.85); border: 1px solid rgba(255,255,255,0.1); border-radius: 24px; padding: 2rem; display: flex; flex-direction: column; gap: 1.5rem; box-shadow: 0 20px 60px rgba(0,0,0,0.5); }
.modal-header { display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.3rem; font-weight: 600; letter-spacing: -0.5px; color: #fff; }
.icon-btn { background: none; border: none; color: #a1a1a6; font-size: 1.2rem; cursor: pointer; transition: color 0.2s; padding: 0; display: flex; align-items: center; justify-content: center; }
.icon-btn:hover { color: #fff; }
.modal-body { display: flex; flex-direction: column; gap: 1.2rem; }
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-row { display: flex; gap: 1rem; }
.half { flex: 1; }
.form-group label { font-size: 0.85rem; color: #a1a1a6; font-weight: 500; text-align: left; }
.apple-input { width: 100%; box-sizing: border-box; background: rgba(0, 0, 0, 0.3); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 10px; padding: 0.8rem 1rem; color: #fff; font-size: 1rem; font-family: inherit; outline: none; transition: all 0.2s ease; }
.apple-input:focus { border-color: #0a84ff; box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.25); background: rgba(0, 0, 0, 0.5); }
.apple-input::placeholder { color: #636366; }
.segmented-control { display: flex; background: rgba(0, 0, 0, 0.3); border-radius: 10px; padding: 4px; gap: 4px; }
.segment-btn { flex: 1; border: none; background: transparent; color: #a1a1a6; padding: 0.6rem 0; border-radius: 8px; font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: all 0.2s ease; }
.segment-btn:hover:not(.active) { color: #fff; background: rgba(255, 255, 255, 0.05); }
.segment-btn.active.buy-btn { background: #30d158; color: #000; box-shadow: 0 2px 8px rgba(48, 209, 88, 0.3); }
.segment-btn.active.sell-btn { background: #ff453a; color: #fff; box-shadow: 0 2px 8px rgba(255, 69, 58, 0.3); }
.total-display { background: rgba(255, 255, 255, 0.03); padding: 1rem; border-radius: 12px; border: 1px dashed rgba(255, 255, 255, 0.1); text-align: left; }
.total-amount { font-size: 1.8rem; font-weight: 700; color: #fff; letter-spacing: -1px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 0.5rem; }
.apple-btn { padding: 0.7rem 1.4rem; border-radius: 20px; font-weight: 600; font-size: 0.95rem; cursor: pointer; border: none; transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1); }
.apple-btn:hover:not(:disabled) { transform: scale(1.03); }
.apple-btn:active:not(:disabled) { transform: scale(0.97); }
.apple-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.2); }
.btn-secondary { background: rgba(255,255,255,0.1); color: #fff; }
@keyframes elasticDrop { 0% { opacity: 0; transform: translateY(-60px) scale(0.95); } 50% { opacity: 1; transform: translateY(12px) scale(1.02); } 75% { transform: translateY(-4px) scale(0.99); } 100% { transform: translateY(0) scale(1); } }
.animate-bounce-in { animation: elasticDrop 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; }
@keyframes fadeInFast { from { opacity: 0; } to { opacity: 1; } }
.animate-fade-in-fast { animation: fadeInFast 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
.validation-error { color: #ff453a; font-size: 0.9rem; padding: 0.5rem; text-align: center; margin: 0; font-weight: 500; }
</style>