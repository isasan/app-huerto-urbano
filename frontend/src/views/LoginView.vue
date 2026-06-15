<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(form)
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Credenciales incorrectas'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-orb auth-orb-1" aria-hidden="true"></div>
    <div class="auth-orb auth-orb-2" aria-hidden="true"></div>

    <div class="auth-card" style="max-width: 410px;">
      <div class="text-center">
        <RouterLink to="/" class="auth-brand">
          <span aria-hidden="true">🌿</span> HuertoApp
        </RouterLink>
        <h1 class="auth-title">Iniciar sesión</h1>
        <p class="auth-sub">Bienvenido de nuevo a tu huerto</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="form-label" for="login-username">Usuario</label>
          <div class="input-icon">
            <i class="bi bi-person" aria-hidden="true"></i>
            <input
              id="login-username"
              v-model="form.username"
              type="text"
              class="form-control"
              required
              autocomplete="username"
              placeholder="Tu nombre de usuario"
            />
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="login-password">Contraseña</label>
          <div class="input-icon">
            <i class="bi bi-lock" aria-hidden="true"></i>
            <input
              id="login-password"
              v-model="form.password"
              type="password"
              class="form-control"
              required
              autocomplete="current-password"
              placeholder="••••••••"
            />
          </div>
        </div>

        <div v-if="error" class="auth-error mb-3" role="alert">
          <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
          {{ error }}
        </div>

        <button type="submit" class="btn btn-success w-100" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          <i v-else class="bi bi-box-arrow-in-right me-2" aria-hidden="true"></i>
          Entrar
        </button>
      </form>

      <p class="auth-alt">
        ¿No tienes cuenta?
        <RouterLink to="/register">Regístrate</RouterLink>
      </p>
    </div>
  </div>
</template>
