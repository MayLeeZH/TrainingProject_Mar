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