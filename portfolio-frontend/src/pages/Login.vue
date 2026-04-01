<template>
  <div class="apple-layout">
    <div class="ambient-bg blob-1"></div>
    <div class="ambient-bg blob-2"></div>

    <div class="login-wrapper animate-fade-in">
      <div class="glass-card login-card">
        
        <div class="brand-header">
          
          <h2 class="brand-title">PRO-GRAM</h2>
        </div>

        <div class="login-titles">
          <h3>Welcome back</h3>
          <p>Enter your details to access your portfolio.</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          
          <div class="input-group">
            <label for="email">Email or Username</label>
            <input 
              type="text" 
              id="email" 
              v-model="email" 
              placeholder="Enter your email" 
              autocomplete="username"
              :class="{ 'has-error': errorMessage }"
            />
          </div>

          <div class="input-group">
            <label for="password">Password</label>
            <input 
              type="password" 
              id="password" 
              v-model="password" 
              placeholder="••••••••" 
              autocomplete="current-password"
              :class="{ 'has-error': errorMessage }"
            />
          </div>

          <div v-if="errorMessage" class="error-message shake-animation">
            􀇾 {{ errorMessage }}
          </div>

          <div class="form-actions">
            <label class="checkbox-container">
              <input type="checkbox" v-model="rememberMe" />
              <span class="checkmark"></span>
              Remember me
            </label>
            <a href="#" class="apple-link text-sm">Forgot Password?</a>
          </div>

          <button type="submit" class="apple-btn btn-primary full-width" :disabled="isLoading">
            <span v-if="!isLoading">Sign In</span>
            <span v-else class="loading-spinner">Logging in...</span>
          </button>
        </form>

        <div class="login-footer">
          <p>Don't have an account? <a href="#" class="apple-link font-medium">Sign up now</a></p>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// 引入您的权限 Store (根据您之前的 App.vue 代码，假设路径如下)
import { useAuthStore } from '../stores/authStore'; 

const router = useRouter();
const authStore = useAuthStore();

// 表单状态
const email = ref('');
const password = ref('');
const rememberMe = ref(false);
const errorMessage = ref('');
const isLoading = ref(false);

// 处理登录逻辑
const handleLogin = async () => {
  // 1. 重置错误信息
  errorMessage.value = '';

  // 2. 简单的非空表单验证
  if (!email.value || !password.value) {
    errorMessage.value = 'Please enter both email and password.';
    return;
  }

  // 3. 开启加载动画效果
  isLoading.value = true;

  try {
    // 模拟网络请求延迟 (1秒钟)，让用户感受到高级的加载过程
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 假设这里的邮箱和密码验证通过，调用 store 更新登录状态
    // 如果您的 authStore 里有 login() 方法，请调用它。
    // 如果没有，直接将 isAuthenticated 设为 true 即可。
    if (authStore.login) {
      authStore.login(email.value, password.value);
    } else {
      authStore.isAuthenticated = true; 
    }

    // 4. 登录成功，丝滑跳转到 Dashboard (首页)
    router.push('/');
    
  } catch (error) {
    errorMessage.value = 'Invalid email or password. Please try again.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* =========================================================
   APPLE DESIGN SYSTEM - LOGIN PAGE
========================================================= */

.apple-layout {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-color: #000000;
  color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "SF Pro Display", sans-serif;
  overflow: hidden;
  position: relative;
}

/* Ambient Background Blobs */
.ambient-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.6;
  animation: float 20s ease-in-out infinite alternate;
  pointer-events: none; 
}
.blob-1 { top: -10%; left: -10%; width: 50vw; height: 50vw; background: radial-gradient(circle, #2c1a59 0%, rgba(0,0,0,0) 70%); }
.blob-2 { bottom: -20%; right: -10%; width: 60vw; height: 60vw; background: radial-gradient(circle, #0a2e3f 0%, rgba(0,0,0,0) 70%); }

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(5%, 5%) scale(1.1); }
}

/* 登录容器与卡片 */
.login-wrapper {
  z-index: 10;
  width: 100%;
  max-width: 400px;
  padding: 0 1.5rem;
}

.glass-card {
  background: rgba(30, 30, 32, 0.65);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 2.5rem 2rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255,255,255,0.1);
}

/* 品牌区域 */
.brand-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  margin-bottom: 2rem;
}
.brand-logo { width: 36px; height: 36px; color: #fff; }
.brand-title { font-size: 1.5rem; font-weight: 700; margin: 0; letter-spacing: -0.5px; }

/* 标题区域 */
.login-titles {
  text-align: center;
  margin-bottom: 2rem;
}
.login-titles h3 { margin: 0 0 0.5rem 0; font-size: 1.4rem; font-weight: 600; }
.login-titles p { margin: 0; color: #a1a1a6; font-size: 0.95rem; }

/* 表单输入框 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.input-group label {
  font-size: 0.85rem;
  font-weight: 500;
  color: #a1a1a6;
}

.input-group input {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 0.9rem 1rem;
  color: #fff;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.2s ease;
  outline: none;
}
.input-group input:focus {
  border-color: #0a84ff;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.25);
  background: rgba(0, 0, 0, 0.5);
}
.input-group input::placeholder { color: #636366; }
.input-group input.has-error { border-color: #ff453a; }

/* 表单辅助操作 */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -0.2rem;
  margin-bottom: 0.5rem;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #a1a1a6;
  cursor: pointer;
  user-select: none;
}
.checkbox-container input { display: none; }
.checkmark {
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: 0.2s;
}
.checkbox-container input:checked ~ .checkmark { background: #0a84ff; border-color: #0a84ff; }
.checkbox-container input:checked ~ .checkmark::after {
  content: "✓";
  color: white;
  font-size: 12px;
  font-weight: bold;
}

/* 按钮样式 */
.apple-btn {
  padding: 0.9rem 1.4rem;
  border-radius: 14px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  display: flex;
  justify-content: center;
  align-items: center;
}
.apple-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255,255,255,0.25); }
.apple-btn:active:not(:disabled) { transform: translateY(0); }
.apple-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.btn-primary { background: #ffffff; color: #000000; box-shadow: 0 4px 14px rgba(255,255,255,0.15); }
.full-width { width: 100%; margin-top: 0.5rem; }

/* 链接与文字 */
.apple-link { color: #0a84ff; text-decoration: none; transition: 0.2s; }
.apple-link:hover { opacity: 0.8; text-decoration: underline; }
.text-sm { font-size: 0.85rem; }
.font-medium { font-weight: 500; }

.login-footer {
  margin-top: 2rem;
  text-align: center;
  font-size: 0.9rem;
  color: #a1a1a6;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 1.5rem;
}

/* 错误与动画 */
.error-message {
  color: #ff453a;
  background: rgba(255, 69, 58, 0.1);
  padding: 0.8rem;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in { animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; }

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-5px); }
  40%, 80% { transform: translateX(5px); }
}
.shake-animation { animation: shake 0.4s ease-in-out; }
</style>