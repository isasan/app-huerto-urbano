<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const form = reactive({
  username: '', email: '', password: '', confirmPassword: '', city: '', hemisphere: 'NORTE'
})
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''

  if (form.password !== form.confirmPassword) {
    error.value = 'Las contraseñas no coinciden'
    return
  }

  loading.value = true
  try {
    const { confirmPassword, ...payload } = form
    await authStore.register(payload)
    router.push({ name: 'login' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al registrarse'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="card shadow border-0" style="width: 450px;">
      <div class="card-body p-4">
        <div class="text-center mb-4">
          <span class="fs-1">🌱</span>
          <h4 class="fw-bold mt-2">Crear cuenta</h4>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">Usuario *</label>
              <input v-model="form.username" class="form-control" required minlength="3" />
            </div>
            <div class="col-12">
              <label class="form-label">Email *</label>
              <input v-model="form.email" type="email" class="form-control" required />
            </div>
            <div class="col-12">
              <label class="form-label">Contraseña *</label>
              <input v-model="form.password" type="password" class="form-control" required minlength="6" />
            </div>
            <div class="col-12">
              <label class="form-label">Confirmar contraseña *</label>
              <input v-model="form.confirmPassword" type="password" class="form-control" required minlength="6" />
            </div>
            <div class="col-md-6">
              <label class="form-label">Ciudad</label>
              <input v-model="form.city" class="form-control" placeholder="Madrid" />
            </div>
            <div class="col-md-6">
              <label class="form-label">Hemisferio</label>
              <select v-model="form.hemisphere" class="form-select">
                <option value="NORTE">Norte</option>
                <option value="SUR">Sur</option>
              </select>
            </div>
          </div>

          <div v-if="error" class="alert alert-danger py-2 small mt-3">{{ error }}</div>

          <button type="submit" class="btn btn-success w-100 mt-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Registrarse
          </button>
        </form>

        <p class="text-center mt-3 mb-0 small">
          ¿Ya tienes cuenta?
          <RouterLink to="/login" class="text-success fw-semibold">Inicia sesión</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
