// Store API key here.
const API_KEY = 'd75k1q9r01qk56kd2770d75k1q9r01qk56kd277g'; 
const BASE_URL = 'https://finnhub.io/api/v1';

// --- 全局内存缓存机制 ---
// 该 Map 存在于模块作用域中，只要用户没有主动刷新网页，缓存就会一直生效
const cache = new Map();

// 定义不同类型数据的缓存过期时间（TTL）
// - 个股行情：缓存 60 秒（保证数据相对实时）
// - 大盘分时/历史曲线：缓存 15 分钟（大盘趋势变化慢，这样切换时间单位能实现秒切秒看）
const TTL = {
  QUOTE: 60 * 1000,
  HISTORICAL: 15 * 60 * 1000
};

/**
 * 通用的带缓存的高阶函数包装器 (HOC)
 * @param {string} cacheKey 唯一键名
 * @param {function} fetchCallback 真实的异步拉取逻辑
 * @param {number} ttl 过期时间（毫秒）
 */
const fetchWithCache = async (cacheKey, fetchCallback, ttl) => {
  if (cache.has(cacheKey)) {
    const cachedItem = cache.get(cacheKey);
    // 判断缓存是否在有效期内
    if (Date.now() - cachedItem.timestamp < ttl) {
      // 命中缓存，直接丝滑返回内存中的数据
      console.log(`[Cache Hit ✅] ${cacheKey} (expires in ${Math.round((ttl - (Date.now() - cachedItem.timestamp))/1000)}s)`);
      return cachedItem.data;
    }
  }

  // 缓存失效或未命中，执行真实的向外网络请求
  console.log(`[Cache Miss ⏳] Fetching real data for ${cacheKey}...`);
  const data = await fetchCallback();
  
  // 只有请求成功并且拉到了非空数据，才写入缓存，防止报错或空数组污染缓存池
  if (data !== null && data !== undefined) {
    cache.set(cacheKey, { data, timestamp: Date.now() });
  }
  
  return data;
};

/**
 * Fetches the real-time quote for a specific stock ticker.
 * @param {string} ticker - The stock symbol (e.g., 'AAPL', 'TSLA')
 * @returns {object} The quote data from Finnhub
 */
export async function getStockQuote(ticker) {
  const cacheKey = `quote-${ticker}`;
  
  return fetchWithCache(cacheKey, async () => {
    try {
      const response = await fetch(`${BASE_URL}/quote?symbol=${ticker}&token=${API_KEY}`);
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error fetching quote for ${ticker}:`, error);
      throw error; 
    }
  }, TTL.QUOTE);
}

// 获取过去 N 天的真实历史收盘价
export const getStockHistoricalData = async (symbol, days = 30) => {
  const cacheKey = `historical-${symbol}-${days}d`;

  return fetchWithCache(cacheKey, async () => {
    const to = Math.floor(Date.now() / 1000); 
    const from = to - (days * 24 * 60 * 60); 

    try {
      const response = await fetch(`https://finnhub.io/api/v1/stock/candle?symbol=${symbol}&resolution=D&from=${from}&to=${to}&token=${API_KEY}`);
      const data = await response.json();
      
      if (data && data.s === 'ok') return data.c; 
      return null;
    } catch (error) {
      console.error(`Error fetching historical data for ${symbol}:`, error);
      return null;
    }
  }, TTL.HISTORICAL);
};

// 获取当日分时数据 (Intraday)
export const getMarketIntradayData = async (symbol = 'VOO') => {
  const cacheKey = `intraday-${symbol}`;

  return fetchWithCache(cacheKey, async () => {
    const to = Math.floor(Date.now() / 1000); 
    const from = to - (24 * 60 * 60); 

    try {
      const response = await fetch(`https://finnhub.io/api/v1/stock/candle?symbol=${symbol}&resolution=15&from=${from}&to=${to}&token=${API_KEY}`);
      const data = await response.json();
      
      if (data && data.s === 'ok') return data.c; 
      return null;
    } catch (error) {
      console.error(`Error fetching intraday data for ${symbol}:`, error);
      return null;
    }
  }, TTL.HISTORICAL);
};