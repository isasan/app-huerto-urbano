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
  <div class="auth-page">
    <div class="auth-orb auth-orb-1" aria-hidden="true"></div>
    <div class="auth-orb auth-orb-2" aria-hidden="true"></div>

    <div class="auth-card" style="max-width: 470px;">
      <div class="text-center">
        <RouterLink to="/" class="auth-brand">
          <span aria-hidden="true">🌿</span> HuertoApp
        </RouterLink>
        <h1 class="auth-title">Crear cuenta</h1>
        <p class="auth-sub">Empieza a cuidar tu huerto hoy mismo</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="row g-3">
          <div class="col-12">
            <label class="form-label" for="reg-username">Usuario <span class="text-danger" aria-hidden="true">*</span></label>
            <div class="input-icon">
              <i class="bi bi-person" aria-hidden="true"></i>
              <input id="reg-username" v-model="form.username" class="form-control" required minlength="3" autocomplete="username" placeholder="usuario123" />
            </div>
          </div>
          <div class="col-12">
            <label class="form-label" for="reg-email">Email <span class="text-danger" aria-hidden="true">*</span></label>
            <div class="input-icon">
              <i class="bi bi-envelope" aria-hidden="true"></i>
              <input id="reg-email" v-model="form.email" type="email" class="form-control" required autocomplete="email" placeholder="tu@email.com" />
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reg-password">Contraseña <span class="text-danger" aria-hidden="true">*</span></label>
            <div class="input-icon">
              <i class="bi bi-lock" aria-hidden="true"></i>
              <input id="reg-password" v-model="form.password" type="password" class="form-control" required minlength="6" autocomplete="new-password" placeholder="Mín. 6 caracteres" />
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reg-confirm">Confirmar <span class="text-danger" aria-hidden="true">*</span></label>
            <div class="input-icon">
              <i class="bi bi-lock-fill" aria-hidden="true"></i>
              <input id="reg-confirm" v-model="form.confirmPassword" type="password" class="form-control" required minlength="6" autocomplete="new-password" placeholder="Repite la contraseña" />
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reg-city">Ciudad</label>
            <div class="input-icon">
              <i class="bi bi-geo-alt" aria-hidden="true"></i>
              <input id="reg-city" v-model="form.city" class="form-control" placeholder="Madrid" />
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="reg-hemisphere">Hemisferio</label>
            <div class="input-icon">
              <i class="bi bi-globe-americas" aria-hidden="true"></i>
              <select id="reg-hemisphere" v-model="form.hemisphere" class="form-select">
                <option value="NORTE">Norte</option>
                <option value="SUR">Sur</option>
              </select>
            </div>
          </div>
        </div>

        <div v-if="error" class="auth-error mt-3" role="alert">
          <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
          {{ error }}
        </div>

        <button type="submit" class="btn btn-success w-100 mt-4" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          <i v-else class="bi bi-stars me-2" aria-hidden="true"></i>
          Crear mi cuenta
        </button>
      </form>

      <p class="auth-alt">
        ¿Ya tienes cuenta?
        <RouterLink to="/login">Inicia sesión</RouterLink>
      </p>
    </div>
  </div>
</template>
