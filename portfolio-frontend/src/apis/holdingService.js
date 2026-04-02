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
  const response = await fetch(`/api/holds/asset-distri/${userId}`);

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const result = await response.json();

  if (result && Array.isArray(result.data)) {
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