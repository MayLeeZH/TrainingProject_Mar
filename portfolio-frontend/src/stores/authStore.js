import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // Global state variables
  const isAuthenticated = ref(false);
  const currentUser = ref(null);

  // The mock login function NO verification
  const login = (email) => {
    isAuthenticated.value = true;
    currentUser.value = { 
      name: email,
      avatar: '👤' 
    };
  };

  const logout = () => {
    isAuthenticated.value = false;
    currentUser.value = null;
  };

  return { isAuthenticated, currentUser, login, logout };
});