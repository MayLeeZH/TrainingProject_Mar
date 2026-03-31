import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../pages/Dashboard.vue';
import Holdings from '../pages/Holdings.vue';
import Transactions from '../pages/Transactions.vue'; // <--- 引入新页面

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Dashboard', component: Dashboard },
    { path: '/holdings', name: 'Holdings', component: Holdings },
    { path: '/transactions', name: 'Transactions', component: Transactions } // <--- 添加路由规则
  ]
});

export default router;