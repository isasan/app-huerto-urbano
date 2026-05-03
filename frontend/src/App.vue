<script setup>
import { useAuthStore } from '@/stores/auth'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import GlobalToast from '@/components/GlobalToast.vue'

const authStore = useAuthStore()
</script>

<template>
  <div id="app">
    <AppNavbar v-if="authStore.isAuthenticated" />
    <div class="app-layout">
      <div v-if="authStore.isAuthenticated" class="d-none d-md-block">
        <AppSidebar />
      </div>
      <main class="app-main">
        <RouterView v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>
    <GlobalToast />
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  background-color: var(--bg-app);
}

.app-layout {
  display: flex;
  min-height: 100vh;
}

.app-main {
  flex: 1;
  padding: 28px 24px;
  min-width: 0;
}

@media (max-width: 767px) {
  .app-main {
    padding: 20px 16px;
  }
}
</style>
