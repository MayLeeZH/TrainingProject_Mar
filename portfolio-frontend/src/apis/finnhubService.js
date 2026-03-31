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