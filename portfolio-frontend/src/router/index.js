import { createRouter, createWebHistory } from 'vue-router';
import Login from '../pages/Login.vue';
import Dashboard from '../pages/Dashboard.vue';
import Holdings from '../pages/Holdings.vue';
import Transactions from '../pages/Transactions.vue';
import AiChat from '../pages/AiChat.vue';
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/dashboard', name: 'Dashboard', component: Dashboard },
    { path: '/holdings', name: 'Holdings', component: Holdings },
    { path: '/transactions', name: 'Transactions', component: Transactions },
    { path: '/ai-chat', name: 'AiChat', component: AiChat }
  ]
});

export default router;
