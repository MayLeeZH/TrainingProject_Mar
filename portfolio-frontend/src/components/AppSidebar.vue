<template>
  <aside class="glass-sidebar">
    <div class="brand">
      <h2 class="brand-title">PRO-GRAM</h2>
    </div>

    <nav class="nav-menu">
      <router-link to="/dashboard" class="nav-item" active-class="active">
        <span class="text-truncate">Dashboard</span>
      </router-link>

      <router-link to="/holdings" class="nav-item" active-class="active">
        <span class="text-truncate">Holdings</span>
      </router-link>

      <router-link to="/transactions" class="nav-item" active-class="active">
        <span class="text-truncate">Transactions</span>
      </router-link>

      <router-link to="/ai-chat" class="nav-item" active-class="active">
        <span class="text-truncate">AI Assistant</span>
      </router-link>

      <!-- <a href="#" class="nav-item">
        <span class="text-truncate">Reports</span>
      </a> -->
    </nav>

    <div class="user-profile">
      <img
        src="https://ui-avatars.com/api/?name=William+Zhang&background=ffffff&color=000000"
        alt="Avatar"
        class="avatar"
      />
      <div class="user-info text-truncate">
        <div class="name text-truncate">William Zhang</div>
        <div class="type text-truncate">VIP Client</div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.glass-sidebar {
  width: clamp(220px, 18vw, 280px);
  flex-shrink: 0;
  background: rgba(30, 30, 32, 0.4);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  padding: clamp(1.5rem, 2vh, 2.5rem) 1.5rem;
  z-index: 10;
  box-sizing: border-box;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: clamp(2rem, 4vh, 3rem);
  padding-left: 0.5rem;
}

.brand-title {
  font-size: clamp(1.1rem, 1.5vw, 1.4rem);
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
  white-space: nowrap;
  color: #f5f5f7;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  text-align: left;
  padding: 0.8rem 1rem;
  color: #a1a1a6;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 500;
  font-size: clamp(0.9rem, 1vw, 0.95rem);
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-profile {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: transform 0.2s;
  cursor: pointer;
  overflow: hidden;
}

.user-profile:hover {
  transform: scale(1.02);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  min-width: 0;
}

.user-info .name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #f5f5f7;
}

.user-info .type {
  font-size: 0.8rem;
  color: #a1a1a6;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 900px) {
  .glass-sidebar {
    display: none;
  }
}
</style>
