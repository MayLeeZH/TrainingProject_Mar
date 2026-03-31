<template>
  <div class="login-wrapper">
    <form @submit.prevent="handleLogin" class="login-box">
      
      <div class="brand-header">
        <h2 class="brand-title">PRO-GRAM</h2>
        <span class="brand-subtitle">PRIVATE BANKING</span>
      </div>

      <p class="subtitle">Client Portal Access</p>

      <div class="input-group">
        <label for="email">Email Address</label>
        <input id="email" v-model="email" type="email" required placeholder="client@example.com" />
      </div>

      <div class="input-group">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" required placeholder="••••••••" />
      </div>

      <button type="submit" class="login-btn">Secure Login</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/authStore';

const authStore = useAuthStore();

const email = ref('');
const password = ref('');

const handleLogin = () => {
  // Updates the store, telling App.vue to hide this screen and show the Dashboard
  authStore.login(email.value); 
};
</script>

<style scoped>
/* WealthOS Premium Dark Theme */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #0a0a0c; /* Matches Dashboard background */
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.login-box {
  background-color: #141417; /* Matches Dashboard cards */
  padding: 3rem;
  border-radius: 12px;
  border: 1px solid #222;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.brand-header {
  text-align: center;
  margin-bottom: 2rem;
}

.brand-title {
  color: #d4af37; /* Gold logo */
  font-size: 2rem;
  margin: 0 0 0.2rem 0;
  font-weight: bold;
}

.brand-subtitle {
  color: #8a8a93;
  font-size: 0.85rem;
  letter-spacing: 2px;
}

.subtitle { 
  color: #8a8a93; 
  font-size: 0.95rem; 
  margin-bottom: 2rem; 
  text-align: center;
  border-bottom: 1px solid #222;
  padding-bottom: 1rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5rem;
}

label { margin-bottom: 0.5rem; font-size: 0.85rem; color: #8a8a93; text-transform: uppercase; letter-spacing: 0.5px;}

input {
  padding: 0.85rem;
  border-radius: 6px;
  border: 1px solid #333;
  background-color: #0a0a0c;
  color: white;
  font-size: 1rem;
  transition: all 0.2s ease;
}

input:focus { 
  outline: none; 
  border-color: #d4af37; /* Gold focus ring */
  box-shadow: 0 0 0 1px #d4af37;
}

.login-btn {
  width: 100%;
  padding: 1rem;
  background-color: #d4af37; /* Gold Button */
  color: #000;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  margin-top: 1rem;
  transition: opacity 0.2s;
}

.login-btn:hover { 
  opacity: 0.9; 
}
</style>