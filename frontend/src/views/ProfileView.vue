<script setup>
import { ref, onMounted } from 'vue'
import { userService } from '@/services/userService.js'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const authStore = useAuthStore()
const toast = useToast()

const profile = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

const city = ref('')
const countryCode = ref('')
const hemisphere = ref('NORTE')

const pwCurrent = ref('')
const pwNew = ref('')
const pwConfirm = ref('')
const pwLoading = ref(false)
const pwError = ref('')

onMounted(async () => {
  profileLoading.value = true
  try {
    profile.value = await userService.getProfile()
    city.value = profile.value.city || ''
    countryCode.value = profile.value.countryCode || ''
    hemisphere.value = profile.value.hemisphere || 'NORTE'
  } catch {
    profileError.value = 'No se pudo cargar el perfil'
  } finally {
    profileLoading.value = false
  }
})

async function saveProfile() {
  profileError.value = ''
  profileLoading.value = true
  try {
    const payload = {
      city: city.value,
      countryCode: countryCode.value.toUpperCase() || null,
      hemisphere: hemisphere.value
    }
    profile.value = await userService.updateProfile(payload)
    authStore.user = {
      ...authStore.user,
      city: profile.value.city,
      countryCode: profile.value.countryCode,
      hemisphere: profile.value.hemisphere
    }
    toast.success('Perfil actualizado correctamente')
  } catch (e) {
    profileError.value = e.response?.data?.message || e.response?.data?.errors?.countryCode || 'Error al guardar el perfil'
  } finally {
    profileLoading.value = false
  }
}

async function changePassword() {
  pwError.value = ''
  if (pwNew.value !== pwConfirm.value) {
    pwError.value = 'Las contraseñas no coinciden'
    return
  }
  pwLoading.value = true
  try {
    await userService.changePassword({ currentPassword: pwCurrent.value, newPassword: pwNew.value })
    toast.success('Contraseña cambiada correctamente')
    pwCurrent.value = ''
    pwNew.value = ''
    pwConfirm.value = ''
  } catch (e) {
    pwError.value = e.response?.data?.message || 'Error al cambiar la contraseña'
  } finally {
    pwLoading.value = false
  }
}
</script>

<template>
  <div class="container py-4" style="max-width: 640px">
    <h2 class="mb-4"><i class="bi bi-person-gear me-2 text-success"></i>Mi Perfil</h2>

    <div v-if="profileLoading && !profile" class="text-center py-5">
      <div class="spinner-border text-success"></div>
    </div>

    <template v-else-if="profile">
      <!-- Info básica -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <h5 class="card-title mb-3">Información de la cuenta</h5>
          <div class="row mb-2">
            <div class="col-4 text-muted">Usuario</div>
            <div class="col-8 fw-semibold">{{ profile.username }}</div>
          </div>
          <div class="row mb-2">
            <div class="col-4 text-muted">Email</div>
            <div class="col-8">{{ profile.email }}</div>
          </div>
          <div class="row">
            <div class="col-4 text-muted">Rol</div>
            <div class="col-8">
              <span :class="profile.role === 'ADMIN' ? 'badge bg-warning text-dark' : 'badge bg-secondary'">
                {{ profile.role }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Ubicación -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <h5 class="card-title mb-3">Ubicación y hemisferio</h5>
          <div v-if="profileError" class="alert alert-danger py-2">{{ profileError }}</div>
          <form @submit.prevent="saveProfile">
            <div class="mb-3">
              <label class="form-label">Ciudad</label>
              <input v-model="city" type="text" class="form-control"
                placeholder="Ej: Madrid, Barcelona, Buenos Aires" maxlength="100" />
              <div class="form-text">Se usará para mostrar el clima en el dashboard</div>
            </div>
            <div class="mb-3">
              <label class="form-label">
                País <span class="text-muted small">(código ISO, opcional)</span>
              </label>
              <input
                v-model="countryCode"
                type="text"
                class="form-control"
                placeholder="Ej: ES, MX, AR, CO"
                maxlength="2"
                style="text-transform: uppercase; max-width: 120px"
              />
              <div class="form-text">
                Ayuda a distinguir ciudades con el mismo nombre en distintos países.
                Consulta los códigos en
                <a href="https://es.wikipedia.org/wiki/ISO_3166-1_alfa-2" target="_blank" rel="noopener">Wikipedia ISO 3166-1</a>.
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">Hemisferio</label>
              <select v-model="hemisphere" class="form-select">
                <option value="NORTE">Norte</option>
                <option value="SUR">Sur</option>
              </select>
              <div class="form-text">Afecta al calendario de siembra</div>
            </div>
            <button type="submit" class="btn btn-success" :disabled="profileLoading">
              <span v-if="profileLoading" class="spinner-border spinner-border-sm me-2"></span>
              Guardar cambios
            </button>
          </form>
        </div>
      </div>

      <!-- Cambiar contraseña -->
      <div class="card border-0 shadow-sm">
        <div class="card-body">
          <h5 class="card-title mb-3">Cambiar contraseña</h5>
          <div v-if="pwError" class="alert alert-danger py-2">{{ pwError }}</div>
          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <label class="form-label">Contraseña actual <span class="text-danger">*</span></label>
              <input v-model="pwCurrent" type="password" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">Nueva contraseña <span class="text-danger">*</span></label>
              <input v-model="pwNew" type="password" class="form-control" minlength="6" required />
              <div class="form-text">Mínimo 6 caracteres</div>
            </div>
            <div class="mb-3">
              <label class="form-label">Confirmar nueva contraseña <span class="text-danger">*</span></label>
              <input v-model="pwConfirm" type="password" class="form-control" required />
            </div>
            <button type="submit" class="btn btn-outline-success" :disabled="pwLoading">
              <span v-if="pwLoading" class="spinner-border spinner-border-sm me-2"></span>
              Cambiar contraseña
            </button>
          </form>
        </div>
      </div>
    </template>

    <div v-else-if="profileError" class="alert alert-danger">{{ profileError }}</div>
  </div>
</template>
