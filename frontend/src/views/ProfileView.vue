<script setup>
import { ref, computed, onMounted } from 'vue'
import { userService } from '@/services/userService.js'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const toast = useToast()
const router = useRouter()

const AVATARS = ['👩‍🌾','👨‍🌾','🧑‍🌾','👴','👵','🧔','🌱','🌻','🍅','🐝','🌵','🦋','🍓','🍎','🐶']
const DEFAULT_AVATAR = '🐝'

const profile = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

const city = ref('')
const countryCode = ref('')
const hemisphere = ref('NORTE')
const avatar = ref(DEFAULT_AVATAR)
const showAvatarPicker = ref(false)

const pwCurrent = ref('')
const pwNew = ref('')
const pwConfirm = ref('')
const pwLoading = ref(false)
const pwError = ref('')

const showDeleteForm = ref(false)
const deleteUsername = ref('')
const deleteLoading = ref(false)
const deleteError = ref('')
const canDelete = computed(() => deleteUsername.value === authStore.user?.username)

onMounted(async () => {
  profileLoading.value = true
  try {
    profile.value = await userService.getProfile()
    city.value = profile.value.city || ''
    countryCode.value = profile.value.countryCode || ''
    hemisphere.value = profile.value.hemisphere || 'NORTE'
    avatar.value = profile.value.avatar || DEFAULT_AVATAR
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
      hemisphere: hemisphere.value,
      avatar: avatar.value
    }
    profile.value = await userService.updateProfile(payload)
    const updated = {
      ...authStore.user,
      city: profile.value.city,
      countryCode: profile.value.countryCode,
      hemisphere: profile.value.hemisphere,
      avatar: profile.value.avatar
    }
    authStore.user = updated
    localStorage.setItem('user', JSON.stringify(updated))
    showAvatarPicker.value = false
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

async function confirmDeleteAccount() {
  deleteError.value = ''
  deleteLoading.value = true
  try {
    await userService.deleteAccount(deleteUsername.value)
    authStore.logout()
    router.push('/login')
  } catch (e) {
    deleteError.value = e.response?.data?.message || 'Error al eliminar la cuenta'
  } finally {
    deleteLoading.value = false
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
      <!-- Cabecera avatar -->
      <div class="card border-0 shadow-sm mb-4 overflow-hidden animate-up">
        <div class="profile-hero text-center py-4">
          <div
            class="profile-avatar mx-auto mb-2 d-flex align-items-center justify-content-center"
            role="button"
            tabindex="0"
            @click="showAvatarPicker = !showAvatarPicker"
            @keydown.enter="showAvatarPicker = !showAvatarPicker"
            :title="showAvatarPicker ? 'Cerrar selector' : 'Cambiar avatar'"
          >{{ avatar }}</div>
          <div class="text-white-50 small mb-1">Haz clic para cambiar</div>
          <div class="fw-bold text-white">{{ profile.username }}</div>
          <div class="small mt-1">
            <span :class="`role-pill ${profile.role === 'ADMIN' ? 'admin' : 'user'}`">{{ profile.role }}</span>
          </div>
        </div>

        <!-- Picker de avatar (visible al hacer clic en el emoji) -->
        <div v-if="showAvatarPicker" class="card-body border-top avatar-picker animate-in">
          <p class="small text-muted mb-2 fw-semibold">Elige tu avatar:</p>
          <div class="d-flex flex-wrap gap-2">
            <span
              v-for="emoji in AVATARS"
              :key="emoji"
              role="button"
              tabindex="0"
              @click="avatar = emoji"
              @keydown.enter="avatar = emoji"
              class="avatar-option"
              :class="{ selected: avatar === emoji }"
            >{{ emoji }}</span>
          </div>
          <p class="small text-muted mt-2 mb-0">Pulsa <strong>Guardar cambios</strong> para confirmar.</p>
        </div>
      </div>

      <!-- Info básica -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <h5 class="card-title mb-3">Información de la cuenta</h5>
          <div class="d-flex align-items-center gap-3 mb-3">
            <span style="font-size:2rem">{{ avatar }}</span>
            <div>
              <div class="fw-semibold">{{ profile.username }}</div>
              <div class="text-muted small">{{ profile.email }}</div>
            </div>
          </div>
          <div class="row">
            <div class="col-4 text-muted">Rol</div>
            <div class="col-8">
              <span :class="`role-pill ${profile.role === 'ADMIN' ? 'admin' : 'user'}`">
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
              <label for="profile-city" class="form-label">Ciudad</label>
              <input id="profile-city" v-model="city" type="text" class="form-control"
                placeholder="Ej: Madrid, Barcelona, Buenos Aires" maxlength="100" />
              <div class="form-text">Se usará para mostrar el clima en el dashboard</div>
            </div>
            <div class="mb-3">
              <label for="profile-country" class="form-label">
                País <span class="text-muted small">(código ISO, opcional)</span>
              </label>
              <input
                id="profile-country"
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
              <label for="profile-hemisphere" class="form-label">Hemisferio</label>
              <select id="profile-hemisphere" v-model="hemisphere" class="form-select">
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
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <h5 class="card-title mb-3">Cambiar contraseña</h5>
          <div v-if="pwError" class="alert alert-danger py-2">{{ pwError }}</div>
          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <label for="pw-current" class="form-label">Contraseña actual <span class="text-danger">*</span></label>
              <input id="pw-current" v-model="pwCurrent" type="password" class="form-control" required />
            </div>
            <div class="mb-3">
              <label for="pw-new" class="form-label">Nueva contraseña <span class="text-danger">*</span></label>
              <input id="pw-new" v-model="pwNew" type="password" class="form-control" minlength="6" required />
              <div class="form-text">Mínimo 6 caracteres</div>
            </div>
            <div class="mb-3">
              <label for="pw-confirm" class="form-label">Confirmar nueva contraseña <span class="text-danger">*</span></label>
              <input id="pw-confirm" v-model="pwConfirm" type="password" class="form-control" required />
            </div>
            <button type="submit" class="btn btn-outline-success" :disabled="pwLoading">
              <span v-if="pwLoading" class="spinner-border spinner-border-sm me-2"></span>
              Cambiar contraseña
            </button>
          </form>
        </div>
      </div>

      <!-- Zona de peligro -->
      <div class="card border-0 shadow-sm mb-4 danger-zone">
        <div class="card-body">
          <h5 class="card-title mb-2 text-danger">
            <i class="bi bi-exclamation-triangle me-2"></i>Zona de peligro
          </h5>
          <p class="text-muted small mb-3">
            Eliminar tu cuenta borrará permanentemente todos tus huertos, parcelas, cultivos y cosechas.
            <strong>Esta acción es irreversible.</strong>
          </p>
          <button
            v-if="!showDeleteForm"
            class="btn btn-outline-danger btn-sm"
            @click="showDeleteForm = true"
          >Eliminar cuenta</button>

          <div v-if="showDeleteForm">
            <div v-if="deleteError" class="alert alert-danger py-2 small">{{ deleteError }}</div>
            <p class="small mb-2">
              Escribe tu usuario <strong>«{{ authStore.user?.username }}»</strong> para confirmar:
            </p>
            <input
              id="delete-username"
              v-model="deleteUsername"
              type="text"
              class="form-control form-control-sm mb-3"
              placeholder="Tu nombre de usuario"
              autocomplete="off"
              aria-label="Escribe tu nombre de usuario para confirmar"
            />
            <div class="d-flex gap-2">
              <button
                class="btn btn-danger btn-sm"
                :disabled="!canDelete || deleteLoading"
                @click="confirmDeleteAccount"
              >
                <span v-if="deleteLoading" class="spinner-border spinner-border-sm me-1"></span>
                Confirmar eliminación
              </button>
              <button
                class="btn btn-outline-secondary btn-sm"
                @click="showDeleteForm = false; deleteUsername = ''; deleteError = ''"
              >Cancelar</button>
            </div>
          </div>
        </div>
      </div>

    </template>

    <div v-else-if="profileError" class="alert alert-danger">{{ profileError }}</div>
  </div>
</template>

<style scoped>
.profile-hero {
  background: linear-gradient(135deg, var(--green-700), var(--green-500));
}
.profile-avatar {
  font-size: 3.5rem;
  width: 84px;
  height: 84px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  cursor: pointer;
  transition: transform var(--t-base), background var(--t-base);
}
.profile-avatar:hover {
  transform: scale(1.06);
  background: rgba(255, 255, 255, 0.3);
}
.role-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 12px;
  border-radius: var(--r-full);
  font-size: 0.72rem;
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: 0.03em;
}
.role-pill.admin { background: var(--amber-100); color: var(--amber-700); }
.role-pill.user  { background: var(--green-100); color: var(--green-700); }
.avatar-picker { background: var(--bg-subtle); }
.avatar-option {
  font-size: 1.8rem;
  cursor: pointer;
  border: 2px solid var(--border-subtle);
  background: var(--bg-card);
  border-radius: var(--r-xs);
  padding: 4px;
  line-height: 1;
  transition: border-color var(--t-fast), transform var(--t-fast);
}
.avatar-option:hover { transform: scale(1.1); }
.avatar-option.selected {
  border-color: var(--green-600);
  box-shadow: 0 0 0 2px var(--green-100);
}
.danger-zone {
  border: 1px solid var(--red-100) !important;
}
</style>
