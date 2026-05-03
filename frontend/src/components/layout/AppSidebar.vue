<script setup>
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()

const navItems = [
  { to: '/dashboard', icon: 'bi-speedometer2', label: 'Dashboard' },
  { to: '/gardens',   icon: 'bi-tree',         label: 'Mis Huertos' },
  { to: '/tasks',     icon: 'bi-check2-square', label: 'Tareas' },
  { to: '/harvests',  icon: 'bi-basket',        label: 'Cosechas' },
  { to: '/',          icon: 'bi-calendar3',     label: 'Calendario' },
]
</script>

<template>
  <nav class="huerto-sidebar" aria-label="Menú lateral">
    <!-- Decorative leaf -->
    <div class="sidebar-accent" aria-hidden="true"></div>

    <ul class="sidebar-nav" role="list">
      <li v-for="item in navItems" :key="item.to" role="listitem">
        <RouterLink
          class="sidebar-link"
          :to="item.to"
          :exact-active-class="item.to === '/' ? 'active' : ''"
          :active-class="item.to !== '/' ? 'active' : ''"
          :aria-label="item.label"
        >
          <span class="sidebar-link-icon" aria-hidden="true">
            <i :class="`bi ${item.icon}`"></i>
          </span>
          <span class="sidebar-link-label">{{ item.label }}</span>
        </RouterLink>
      </li>

      <li v-if="authStore.isAdmin" class="admin-section" role="listitem">
        <div class="admin-divider" aria-hidden="true"></div>
        <RouterLink class="sidebar-link admin-link" to="/admin" aria-label="Administración">
          <span class="sidebar-link-icon" aria-hidden="true">
            <i class="bi bi-shield-lock"></i>
          </span>
          <span class="sidebar-link-label">Administración</span>
        </RouterLink>
      </li>
    </ul>

    <!-- Decorative bottom -->
    <div class="sidebar-bottom" aria-hidden="true">
      <span class="sidebar-leaf">🌿</span>
    </div>
  </nav>
</template>

<style scoped>
.huerto-sidebar {
  width: 220px;
  min-height: calc(100vh - 60px);
  background: var(--off-white);
  border-right: 1px solid var(--border-subtle);
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

/* Green glow accent at top */
.sidebar-accent {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--green-600), var(--green-400));
}

.sidebar-nav {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--r-sm);
  text-decoration: none;
  color: var(--text-muted);
  font-weight: 600;
  font-size: 0.88rem;
  transition: all var(--t-base);
  position: relative;
  overflow: hidden;
}
.sidebar-link::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: var(--green-600);
  border-radius: 0 2px 2px 0;
  transform: scaleY(0);
  transition: transform var(--t-base);
}
.sidebar-link:hover {
  background: var(--green-50);
  color: var(--green-700);
}
.sidebar-link.active {
  background: linear-gradient(135deg, var(--green-600) 0%, var(--green-500) 100%);
  color: white;
  box-shadow: 0 3px 12px rgba(5, 150, 105, 0.3);
}
.sidebar-link.active::before {
  transform: scaleY(1);
}

.sidebar-link-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
  border-radius: var(--r-xs);
  transition: all var(--t-base);
}
.sidebar-link:hover .sidebar-link-icon {
  background: rgba(5, 150, 105, 0.12);
}
.sidebar-link.active .sidebar-link-icon {
  background: rgba(255, 255, 255, 0.2);
}

.sidebar-link-label {
  font-family: var(--font-display);
  letter-spacing: -0.01em;
}

.admin-section {
  margin-top: auto;
  padding-top: 8px;
}
.admin-divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 10px 4px 12px;
}
.admin-link {
  color: var(--red-600);
}
.admin-link:hover {
  background: var(--red-100);
  color: var(--red-700);
}
.admin-link.active {
  background: linear-gradient(135deg, var(--red-600), #F87171);
  color: white;
  box-shadow: 0 3px 12px rgba(220, 38, 38, 0.25);
}

.sidebar-bottom {
  text-align: center;
  padding-top: 20px;
  margin-top: auto;
}
.sidebar-leaf {
  font-size: 1.8rem;
  opacity: 0.18;
  display: block;
  animation: leafFloat 5s ease-in-out infinite;
}
</style>
