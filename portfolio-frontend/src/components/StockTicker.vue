<template>
  <div class="stock-ticker-card">
    <h3 class="ticker-symbol">{{ symbol }}</h3>

    <div v-if="isLoading" class="loading">
      Loading market data...
    </div>

    <div v-else-if="errorMessage" class="error">
      {{ errorMessage }}
    </div>

    <div v-else-if="quoteData" class="quote-details">
      <div class="price">${{ quoteData.c.toFixed(2) }}</div>
      
      <div 
        class="change" 
        :class="{ 'positive': quoteData.d > 0, 'negative': quoteData.d < 0 }"
      >
        {{ quoteData.d > 0 ? '+' : '' }}{{ quoteData.d.toFixed(2) }} 
        ({{ quoteData.dp.toFixed(2) }}%)
      </div>
    </div>
  </div>
</template>

<script setup>
// Import tools
import { ref, onMounted } from 'vue';
// Import the API function we just created
import { getStockQuote } from '../apis/finnhubService.js';

// 'defineProps' allows parent pages (like Dashboard.vue) to pass data into this component.
const props = defineProps({
  symbol: {
    type: String,
    required: true // The component will throw a warning if don't give it a symbol
  }
});

// set up reactive variables to hold our component's state. 
const quoteData = ref(null);      // Holds the data returned from Finnhub
const isLoading = ref(true);      // Tracks if the loading spinner should show
const errorMessage = ref('');     // Holds any error text to show the user

// The 'fetchData' function does the actual work of calling our API service
const fetchData = async () => {
  // Reset states before fetching
  isLoading.value = true;
  errorMessage.value = '';

  try {
    // Await the data from our service, passing in the symbol from props
    quoteData.value = await getStockQuote(props.symbol);
  } catch (error) {
    // If the API service threw an error, catch it and update the UI
    errorMessage.value = "Failed to load quote.";
  } finally {
    // 'finally' runs whether the try succeeds or fails. 
    // We use it to turn off the loading state.
    isLoading.value = false;
  }
};

// 'onMounted' is a Vue lifecycle hook. It runs exactly once, immediately 
// after this component is inserted into the web page.
onMounted(() => {
  // As soon as the component appears, go fetch the data!
  fetchData();
});
</script>

<style scoped>
/* Scoped styles only affect this specific component, preventing CSS leaks */
.stock-ticker-card {
  border: 1px solid #333;
  border-radius: 8px;
  padding: 16px;
  width: 200px;
  background-color: #1e1e1e; /* Dark mode financial aesthetic */
  color: white;
  font-family: sans-serif;
}

.ticker-symbol {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  color: #888;
}

.price {
  font-size: 1.8rem;
  font-weight: bold;
}

.positive { color: #00c853; } /* Bullish Green */
.negative { color: #ff3d00; } /* Bearish Red */
</style>