export async function sendChatMessage(userId, message) {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId,
      message
    })
  });

  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`Chat request failed: ${response.status} ${text}`);
  }

  const result = await response.json();
  return result.reply;
}
