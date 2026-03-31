// Store API key here.
const API_KEY = 'd75k1q9r01qk56kd2770d75k1q9r01qk56kd277g'; 
const BASE_URL = 'https://finnhub.io/api/v1';

/**
 * Fetches the real-time quote for a specific stock ticker.
 * @param {string} ticker - The stock symbol (e.g., 'AAPL', 'TSLA')
 * @returns {object} The quote data from Finnhub
 */
export async function getStockQuote(ticker) {
  try {
    // use native fetch to call the Finnhub endpoint.
    // We pass the ticker symbol and your API key directly in the URL.
    const response = await fetch(`${BASE_URL}/quote?symbol=${ticker}&token=${API_KEY}`);
    
    // Check server responded with an error
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Convert the raw response into a usable JavaScript object
    const data = await response.json();
    
    return data; 
  } catch (error) {
    // If the network fails or the API rejects the call, we catch the error 
    // doesn't crash the whole application.
    console.error(`Error fetching quote for ${ticker}:`, error);
    
    //throw the error again Vue component knows
    throw error; 
  }
}

// 新增：获取过去 N 天的真实历史收盘价
export const getStockHistoricalData = async (symbol, days = 30) => {
  // Finnhub 的历史数据接口需要 UNIX 时间戳（秒）
  const to = Math.floor(Date.now() / 1000); 
  const from = to - (days * 24 * 60 * 60); 

  try {
    // resolution=D 代表获取"天"级别的数据 (Daily)
    const response = await fetch(`https://finnhub.io/api/v1/stock/candle?symbol=${symbol}&resolution=D&from=${from}&to=${to}&token=${API_KEY}`);
    const data = await response.json();
    
    // API 返回的 s === 'ok' 表示成功，'c' 数组里装的就是每天的收盘价
    if (data && data.s === 'ok') {
      return data.c; 
    }
    return null; // 如果周末或没数据返回 null
  } catch (error) {
    console.error(`Error fetching historical data for ${symbol}:`, error);
    return null;
  }
};