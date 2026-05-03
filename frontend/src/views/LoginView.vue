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
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="card shadow border-0" style="width: 400px;">
      <div class="card-body p-4">
        <div class="text-center mb-4">
          <span class="fs-1">🌱</span>
          <h4 class="fw-bold mt-2">Iniciar sesión</h4>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label">Usuario</label>
            <input v-model="form.username" type="text" class="form-control" required autocomplete="username" />
          </div>
          <div class="mb-3">
            <label class="form-label">Contraseña</label>
            <input v-model="form.password" type="password" class="form-control" required autocomplete="current-password" />
          </div>

          <div v-if="error" class="alert alert-danger py-2 small">{{ error }}</div>

          <button type="submit" class="btn btn-success w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Entrar
          </button>
        </form>

        <p class="text-center mt-3 mb-0 small">
          ¿No tienes cuenta?
          <RouterLink to="/register" class="text-success fw-semibold">Regístrate</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
