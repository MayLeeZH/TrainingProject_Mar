// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../pages/Dashboard.vue';
import Holdings from '../pages/Holdings.vue';

const router = createRouter({
  // 使用 HTML5 模式的路由（没有难看的 # 号）
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Dashboard', component: Dashboard },
    { path: '/holdings', name: 'Holdings', component: Holdings }
  ]
});

export default router;