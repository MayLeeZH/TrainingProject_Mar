/**
 * 与 Spring Boot 后端通信。开发环境通过 Vite 代理把 /api 转发到 http://localhost:8080
 */
const base = import.meta.env.VITE_API_BASE || '/api';

/**
 * @param {number|string} userId
 * @returns {Promise<Array>}
 */
export async function fetchTransactionsByUser(userId) {
  const url = `${base}/transactions/users/${userId}`;
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`请求失败 ${res.status}: ${text || url}`);
  }
  return res.json();
}
