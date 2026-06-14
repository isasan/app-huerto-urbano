<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

const authStore = useAuthStore()
const router = useRouter()
const { isDark, toggle: toggleTheme } = useTheme()

function logout() {
  authStore.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <nav class="huerto-nav" aria-label="Navegación principal">
    <div class="huerto-nav-inner">
      <!-- Brand -->
      <RouterLink class="huerto-brand" to="/" aria-label="HuertoApp - Inicio">
        <span class="brand-icon" aria-hidden="true">🌿</span>
        <span class="brand-name">HuertoApp</span>
      </RouterLink>

      <!-- Mobile toggle -->
      <button
        class="mobile-toggle"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navMenu"
        aria-controls="navMenu"
        aria-expanded="false"
        aria-label="Abrir menú de navegación"
      >
        <i class="bi bi-list"></i>
      </button>

      <!-- Collapsible section: only mobile nav links -->
      <div class="collapse navbar-collapse" id="navMenu">
        <ul class="mobile-nav d-md-none" role="list">
          <li>
            <RouterLink class="mobile-link" to="/dashboard" data-bs-toggle="collapse" data-bs-target="#navMenu">
              <i class="bi bi-speedometer2" aria-hidden="true"></i>
              <span>Dashboard</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink class="mobile-link" to="/gardens" data-bs-toggle="collapse" data-bs-target="#navMenu">
              <i class="bi bi-tree" aria-hidden="true"></i>
              <span>Mis Huertos</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink class="mobile-link" to="/tasks" data-bs-toggle="collapse" data-bs-target="#navMenu">
              <i class="bi bi-check2-square" aria-hidden="true"></i>
              <span>Tareas</span>
            </RouterLink>
          </li>
          <li>
            <RouterLink class="mobile-link" to="/harvests" data-bs-toggle="collapse" data-bs-target="#navMenu">
              <i class="bi bi-basket" aria-hidden="true"></i>
              <span>Cosechas</span>
            </RouterLink>
          </li>
          <li v-if="authStore.isAdmin">
            <RouterLink class="mobile-link" to="/admin" data-bs-toggle="collapse" data-bs-target="#navMenu">
              <i class="bi bi-shield-lock" aria-hidden="true"></i>
              <span>Administración</span>
            </RouterLink>
          </li>
          <li class="mobile-divider" aria-hidden="true"></li>
        </ul>
      </div>

      <!-- Always-visible right side: theme toggle + admin badge + user dropdown -->
      <div class="nav-actions d-flex align-items-center gap-2">
        <button
          class="theme-toggle"
          type="button"
          :aria-label="isDark ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'"
          :title="isDark ? 'Tema claro' : 'Tema oscuro'"
          @click="toggleTheme"
        >
          <i class="bi" :class="isDark ? 'bi-sun-fill' : 'bi-moon-stars-fill'" aria-hidden="true"></i>
        </button>

        <span v-if="authStore.isAdmin" class="admin-badge">
          <i class="bi bi-shield-check" aria-hidden="true"></i>
          <span>Admin</span>
        </span>

        <!-- User dropdown -->
        <div class="dropdown">
          <button
            class="user-btn"
            id="userDropdown"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            aria-haspopup="true"
            :aria-label="`Menú de usuario: ${authStore.user?.username}`"
          >
            <span class="user-avatar" aria-hidden="true">
              {{ authStore.user?.avatar ?? '🐝' }}
            </span>
            <span class="user-name d-none d-sm-inline">{{ authStore.user?.username }}</span>
            <i class="bi bi-chevron-down user-chevron" aria-hidden="true"></i>
          </button>
          <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown" role="menu">
            <li role="menuitem">
              <RouterLink class="dropdown-item" to="/profile">
                <i class="bi bi-person-gear" aria-hidden="true"></i>
                Mi Perfil
              </RouterLink>
            </li>
            <li v-if="authStore.isAdmin" role="menuitem">
              <RouterLink class="dropdown-item text-danger" to="/admin">
                <i class="bi bi-shield-lock" aria-hidden="true"></i>
                Panel Admin
              </RouterLink>
            </li>
            <li role="separator"><hr class="dropdown-divider" /></li>
            <li role="menuitem">
              <button class="dropdown-item text-danger" @click="logout">
                <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                Cerrar Sesión
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.huerto-nav {
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-xs);
  position: sticky;
  top: 0;
  z-index: 1030;
  height: 60px;
}

.huerto-nav-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 100%;
  padding: 0 20px;
  max-width: 100%;
}

/* Brand */
.huerto-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}
.brand-icon {
  font-size: 1.5rem;
  animation: leafFloat 4s ease-in-out infinite;
  display: block;
}
.brand-name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.2rem;
  color: var(--green-600);
  letter-spacing: -0.02em;
}

/* Mobile toggle */
.mobile-toggle {
  display: none;
  background: none;
  border: 1.5px solid var(--stone-300);
  border-radius: var(--r-xs);
  padding: 6px 10px;
  color: var(--text-muted);
  font-size: 1.3rem;
  cursor: pointer;
  margin-left: auto;
  transition: all var(--t-fast);
}
.mobile-toggle:hover {
  border-color: var(--green-500);
  color: var(--green-600);
}
@media (max-width: 767px) {
  .mobile-toggle { display: flex; align-items: center; }
}

/* Mobile nav */
.mobile-nav {
  list-style: none;
  padding: 8px 0;
  margin: 0;
}
.mobile-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: var(--r-xs);
  transition: background var(--t-fast);
}
.mobile-link:hover,
.mobile-link.router-link-active {
  background: var(--green-50);
  color: var(--green-700);
}
.mobile-divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 6px 12px;
}

/* Bootstrap collapse override */
.navbar-collapse {
  flex-grow: 1;
}
@media (max-width: 767px) {
  .navbar-collapse.show,
  .navbar-collapse.collapsing {
    position: absolute;
    top: 60px;
    left: 0; right: 0;
    background: var(--bg-elevated);
    border-bottom: 1px solid var(--border-subtle);
    box-shadow: var(--shadow-md);
    padding: 8px 12px 16px;
    z-index: 1000;
  }
}

/* Theme toggle */
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--r-full);
  border: 1.5px solid var(--border-subtle);
  background: var(--bg-subtle);
  color: var(--text-muted);
  font-size: 1rem;
  cursor: pointer;
  transition: all var(--t-base);
}
.theme-toggle:hover {
  border-color: var(--green-400);
  color: var(--green-600);
  background: var(--green-50);
}
.theme-toggle:hover i {
  transform: rotate(20deg);
}
.theme-toggle i {
  transition: transform var(--t-base);
}

/* Admin badge */
.admin-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  background: var(--amber-100);
  color: var(--amber-700);
  border-radius: var(--r-full);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.78rem;
}

/* User button */
.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--stone-100);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--r-full);
  padding: 5px 12px 5px 5px;
  cursor: pointer;
  transition: all var(--t-base);
  color: var(--text-primary);
}
.user-btn:hover {
  background: var(--green-50);
  border-color: var(--green-400);
}
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--stone-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}
.user-name {
  font-weight: 600;
  font-size: 0.88rem;
  color: var(--text-primary);
}
.user-chevron {
  font-size: 0.7rem;
  color: var(--text-light);
  transition: transform var(--t-fast);
}
.user-btn[aria-expanded="true"] .user-chevron {
  transform: rotate(180deg);
}
</style>
