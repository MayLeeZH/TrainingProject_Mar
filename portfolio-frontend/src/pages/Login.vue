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
              id="email"
              v-model="email"
              type="text"
              placeholder="Enter your email"
              autocomplete="username"
              :class="{ 'has-error': errorMessage }"
            />
          </div>

          <div class="input-group">
            <label for="password">Password</label>
            <input
              id="password"
              v-model="password"
              type="password"
              placeholder="Enter your password"
              autocomplete="current-password"
              :class="{ 'has-error': errorMessage }"
            />
          </div>

          <div v-if="errorMessage" class="error-message shake-animation">
            {{ errorMessage }}
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
            <span v-else>Logging in...</span>
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

const router = useRouter();

const email = ref('');
const password = ref('');
const rememberMe = ref(false);
const errorMessage = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  errorMessage.value = '';

  if (!email.value || !password.value) {
    errorMessage.value = 'Please enter both email and password.';
    return;
  }

  isLoading.value = true;

  try {
    await new Promise((resolve) => setTimeout(resolve, 700));
    router.push('/dashboard');
  } catch (error) {
    errorMessage.value = 'Unable to continue. Please try again.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
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

.ambient-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.6;
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
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(5%, 5%) scale(1.1); }
}

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
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.brand-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.brand-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-titles {
  text-align: center;
  margin-bottom: 2rem;
}

.login-titles h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.4rem;
  font-weight: 600;
}

.login-titles p {
  margin: 0;
  color: #a1a1a6;
  font-size: 0.95rem;
}

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

.input-group input::placeholder {
  color: #636366;
}

.input-group input.has-error {
  border-color: #ff453a;
}

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

.checkbox-container input {
  display: none;
}

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

.checkbox-container input:checked ~ .checkmark {
  background: #0a84ff;
  border-color: #0a84ff;
}

.checkbox-container input:checked ~ .checkmark::after {
  content: "✓";
  color: white;
  font-size: 12px;
  font-weight: bold;
}

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

.apple-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 255, 255, 0.25);
}

.apple-btn:active:not(:disabled) {
  transform: translateY(0);
}

.apple-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-primary {
  background: #ffffff;
  color: #000000;
  box-shadow: 0 4px 14px rgba(255, 255, 255, 0.15);
}

.full-width {
  width: 100%;
  margin-top: 0.5rem;
}

.apple-link {
  color: #0a84ff;
  text-decoration: none;
  transition: 0.2s;
}

.apple-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.text-sm {
  font-size: 0.85rem;
}

.font-medium {
  font-weight: 500;
}

.login-footer {
  margin-top: 2rem;
  text-align: center;
  font-size: 0.9rem;
  color: #a1a1a6;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 1.5rem;
}

.error-message {
  color: #ff453a;
  background: rgba(255, 69, 58, 0.1);
  padding: 0.8rem;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 500;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-5px); }
  40%, 80% { transform: translateX(5px); }
}

.shake-animation {
  animation: shake 0.4s ease-in-out;
}
</style>
