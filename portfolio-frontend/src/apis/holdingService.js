// --- 模块级内存缓存 ---
// 只要用户没有刷新页面，缓存就一直生效（与 finnhubService.js 风格一致）
const _cache = new Map();
const ALLOCATION_TTL = 5 * 60 * 1000; // Allocation 图缓存 5 分钟

function _getCached(key) {
  if (!_cache.has(key)) return null;
  const { data, timestamp } = _cache.get(key);
  if (Date.now() - timestamp > ALLOCATION_TTL) {
    _cache.delete(key);
    return null;
  }
  console.log(`[Cache Hit ✅] holdingService: ${key} (expires in ${Math.round((ALLOCATION_TTL - (Date.now() - timestamp)) / 1000)}s)`);
  return data;
}

function _setCache(key, data) {
  _cache.set(key, { data, timestamp: Date.now() });
}

/** 主动清除某个缓存（例如 add transaction 后让 Allocation 强制刷新） */
export function invalidateCache(key) {
  _cache.delete(key);
}

export async function getHoldings() {
  const response = await fetch('/api/holds')

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const result = await response.json()

  if (Array.isArray(result)) {
    return result
  }

  if (result && Array.isArray(result.data)) {
    return result.data
  }

  throw new Error('Unexpected holdings response format')
}

export async function getAssetDistribution(userId = 1) {
  const cacheKey = `asset-distri-${userId}`;

  // 命中缓存直接返回，不再请求后端
  const cached = _getCached(cacheKey);
  if (cached) return cached;

  console.log(`[Cache Miss ⏳] holdingService: fetching asset distribution for userId=${userId}...`);
  const response = await fetch(`/api/holds/asset-distri/${userId}`);

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const result = await response.json();

  if (result && Array.isArray(result.data)) {
    _setCache(cacheKey, result.data);
    return result.data;
  }

  throw new Error('Unexpected asset distribution response format');
}

export async function getUserCashBalance(userId = 1) {
  try {
    const response = await fetch(`/api/users/${userId}`);
    if (response.ok) {
      const result = await response.json();
      if (result && result.data && result.data.amount !== undefined) {
        return result.data.amount;
      }
    }
  } catch (err) {
    console.warn("Failed to fetch user direct API. Trying fallback...", err);
  }

  // Fallback: 如果后端没重启，/api/users/{id} 404/500，我们可以从 /api/holds 获取的记录中顺带提取 user 数据
  try {
    const response2 = await fetch('/api/holds');
    if (response2.ok) {
      const result2 = await response2.json();
      const list = result2.data || result2;
      if (Array.isArray(list) && list.length > 0 && list[0].user && list[0].user.amount !== undefined) {
        return list[0].user.amount;
      }
    }
  } catch (err) {
    // ignore
  }

  return 0; // Fallback
}