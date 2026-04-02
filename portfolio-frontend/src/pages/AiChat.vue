<template>
  <div class="apple-layout">
    <div class="ambient-bg blob-1"></div>
    <div class="ambient-bg blob-2"></div>

    <AppSidebar />

    <main class="main-content">
      <header class="top-header animate-fade-in delay-1">
        <div class="header-titles">
          <h1 class="page-title text-truncate">AI Assistant</h1>
          <p class="datetime text-truncate">
            Ask about holdings, transactions, cash balance, and portfolio insights.
          </p>
        </div>
        <div class="header-actions">
          <button class="apple-btn btn-secondary" @click="clearMessages">
            Clear Chat
          </button>
        </div>
      </header>

      <div class="glass-card chat-card animate-fade-in delay-2">
        <div ref="messagesRef" class="chat-messages">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['chat-bubble', msg.role]"
          >
            <div class="bubble-role">
              {{ msg.role === 'assistant' ? 'Assistant' : 'You' }}
            </div>
            <div class="bubble-text">{{ msg.content }}</div>
          </div>

          <div v-if="loading" class="chat-bubble assistant">
            <div class="bubble-role">Assistant</div>
            <div class="bubble-text">Thinking...</div>
          </div>
        </div>

        <form class="chat-composer" @submit.prevent="handleSend">
          <textarea
            v-model="input"
            class="chat-textarea"
            placeholder="Ask something about your portfolio..."
            :disabled="loading"
            @keydown.enter.exact.prevent="handleSend"
          ></textarea>

          <div class="composer-actions">
            <button
              type="submit"
              class="apple-btn btn-primary send-btn"
              :disabled="loading || !input.trim()"
            >
              Send
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue';
import { sendChatMessage } from '../apis/chatService.js';
import AppSidebar from '../components/AppSidebar.vue';

const defaultUserId = 1;
const input = ref('');
const loading = ref(false);
const messagesRef = ref(null);

const messages = ref([
  {
    role: 'assistant',
    content:
      'Hello. I am your AI portfolio assistant. You can ask about your cash balance, holdings, recent transactions, and asset allocation.'
  }
]);

const scrollToBottom = async (smooth = true) => {
  await nextTick();

  if (!messagesRef.value) return;

  messagesRef.value.scrollTo({
    top: messagesRef.value.scrollHeight,
    behavior: smooth ? 'smooth' : 'auto'
  });
};

watch(
  () => messages.value.length,
  async () => {
    await scrollToBottom();
  }
);

const handleSend = async () => {
  const text = input.value.trim();
  if (!text || loading.value) return;

  messages.value.push({
    role: 'user',
    content: text
  });

  input.value = '';
  loading.value = true;

  try {
    const reply = await sendChatMessage(defaultUserId, text);

    messages.value.push({
      role: 'assistant',
      content: reply || 'No response received.'
    });
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: `Request failed: ${error.message}`
    });
  } finally {
    loading.value = false;
  }
};

const clearMessages = async () => {
  messages.value = [
    {
      role: 'assistant',
      content:
        'Hello. I am your AI portfolio assistant. You can ask about your cash balance, holdings, recent transactions, and asset allocation.'
    }
  ];

  await scrollToBottom(false);
};
</script>

<style scoped>
.apple-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  max-width: 100%;
  background-color: #000000;
  color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "SF Pro Display", sans-serif;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ambient-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.5;
  animation: float 20s ease-in-out infinite alternate;
  pointer-events: none;
}

.blob-1 {
  top: -10%;
  left: -10%;
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, #2c1a59 0%, rgba(0, 0, 0, 0) 70%);
}

.blob-2 {
  bottom: -20%;
  right: -10%;
  width: 60vw;
  height: 60vw;
  background: radial-gradient(circle, #0a2e3f 0%, rgba(0, 0, 0, 0) 70%);
}

@keyframes float {
  0% {
    transform: translate(0, 0) scale(1);
  }

  100% {
    transform: translate(5%, 5%) scale(1.1);
  }
}

.main-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  padding: clamp(1.5rem, 3vw, 3rem) clamp(2rem, 4vw, 4rem);
  overflow: hidden;
  z-index: 10;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: clamp(1.5rem, 3vh, 2rem);
  flex-wrap: wrap;
  gap: 1rem;
  min-height: 60px;
}

.header-titles {
  min-width: 0;
}

.page-title {
  font-size: clamp(2rem, 3vw, 2.5rem);
  font-weight: 700;
  margin: 0;
  letter-spacing: -1px;
}

.datetime {
  color: #a1a1a6;
  font-size: clamp(0.9rem, 1.2vw, 1.1rem);
  margin: 0.5rem 0 0 0;
  font-weight: 500;
  white-space: normal;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.apple-btn {
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.apple-btn:hover:not(:disabled) {
  transform: scale(1.03);
}

.apple-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.apple-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #ffffff;
  color: #000000;
  box-shadow: 0 4px 14px rgba(255, 255, 255, 0.2);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0 1.2rem;
  height: 42px;
}

.glass-card {
  background: rgba(30, 30, 32, 0.5);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
  overflow: hidden;
}

.chat-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  scroll-behavior: smooth;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.18) transparent;
}

.chat-messages::-webkit-scrollbar {
  width: 10px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.28);
  background-clip: padding-box;
}

.chat-bubble {
  max-width: 72%;
  border-radius: 18px;
  padding: 0.95rem 1rem;
  line-height: 1.5;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.chat-bubble.user {
  align-self: flex-end;
  background: #0a84ff;
  color: #ffffff;
}

.chat-bubble.assistant {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.07);
  color: #f5f5f7;
}

.bubble-role {
  font-size: 0.68rem;
  font-weight: 700;
  opacity: 0.8;
  margin-bottom: 0.35rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.bubble-text {
  white-space: pre-wrap;
  font-size: 0.95rem;
}

.chat-composer {
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 1rem 1.2rem 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  background: rgba(255, 255, 255, 0.02);
}

.chat-textarea {
  width: 100%;
  min-height: 96px;
  max-height: 180px;
  resize: vertical;
  border: none;
  outline: none;
  border-radius: 18px;
  padding: 0.95rem 1rem;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.chat-textarea::placeholder {
  color: #8e8e93;
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
}

.send-btn {
  height: 42px;
  padding: 0 1.3rem;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  opacity: 0;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.delay-1 {
  animation-delay: 0.1s;
}

.delay-2 {
  animation-delay: 0.2s;
}

@media (max-width: 900px) {
  .main-content {
    padding: 1rem;
  }

  .chat-bubble {
    max-width: 90%;
  }

  .chat-textarea {
    min-height: 84px;
  }
}
</style>
