<script setup>
import { ref, onMounted, reactive, watch, nextTick } from 'vue'
import { Tooltip } from 'bootstrap'
import { adminService } from '@/services/adminService.js'
import { plantDetailService } from '@/services/plantDetailService.js'
import { calendarPlantService } from '@/services/calendarPlantService.js'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const authStore = useAuthStore()
const toast = useToast()

// ── Tabs ──────────────────────────────────────────────────────────────────
const activeTab = ref('stats')

// ── Stats & Users ─────────────────────────────────────────────────────────
const stats = ref(null)
const users = ref([])
const loading = ref(false)
const error = ref('')

const confirmRoleUser = ref(null)
const confirmDeleteUser = ref(null)
const deleteStage = ref(1)
const actionLoading = ref(false)

// ── Create user ────────────────────────────────────────────────────────────
const showCreateUser = ref(false)
const savingUser = ref(false)
const createUserForm = reactive({
  username: '', email: '', password: '', confirmPassword: '',
  role: 'USER', city: '', countryCode: '', hemisphere: 'NORTE'
})
let createUserSnapshot = ''
const createUserError = ref('')

function openCreateUser() {
  Object.assign(createUserForm, {
    username: '', email: '', password: '', confirmPassword: '',
    role: 'USER', city: '', countryCode: '', hemisphere: 'NORTE'
  })
  createUserError.value = ''
  createUserSnapshot = JSON.stringify({ ...createUserForm })
  showCreateUser.value = true
}

function tryCloseCreateUser() {
  const current = { ...createUserForm }
  delete current.confirmPassword
  const snap = JSON.parse(createUserSnapshot)
  delete snap.confirmPassword
  if (JSON.stringify(current) !== JSON.stringify(snap)) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showCreateUser.value = false
}

async function handleCreateUser() {
  createUserError.value = ''
  if (createUserForm.password !== createUserForm.confirmPassword) {
    createUserError.value = 'Las contraseñas no coinciden'
    return
  }
  savingUser.value = true
  try {
    const { confirmPassword, ...payload } = createUserForm
    const created = await adminService.createUser(payload)
    users.value.unshift(created)
    if (stats.value) stats.value.totalUsers++
    toast.success(`Usuario ${created.username} creado correctamente`)
    showCreateUser.value = false
  } catch (e) {
    createUserError.value = e.response?.data?.message || 'Error al crear el usuario'
  } finally {
    savingUser.value = false
  }
}

// ── Plant details ─────────────────────────────────────────────────────────
const plantDetails = ref([])
const detailsLoading = ref(false)
const showDetailForm = ref(false)
const editingDetail = ref(null)
const savingDetail = ref(false)

const detailForm = reactive({
  plantName: '', latinName: '', wateringFrequency: '', pests: '', wikipediaUrl: ''
})
let detailFormSnapshot = ''

// ── Calendar plants (from DB) ─────────────────────────────────────────────
const calendarPlants = ref([])
const showCalendarPlantForm = ref(false)
const editingCalendarPlant = ref(null)
const savingCalendarPlant = ref(false)

const MONTHS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']

const calendarPlantForm = reactive({
  plantName: '', emoji: '', hemisphere: 'NORTE', difficulty: 'MEDIA', daysToHarvest: 60,
  sewingMonths: [], transplantMonths: [], harvestMonths: [],
  latinName: '', wateringFrequency: '', pests: '', wikipediaUrl: ''
})
let calendarPlantSnapshot = ''

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const [statsData, usersData] = await Promise.all([
      adminService.getStats(),
      adminService.getAllUsers()
    ])
    stats.value = statsData
    users.value = usersData
  } catch {
    error.value = 'Error al cargar los datos del panel'
  } finally {
    loading.value = false
  }
})

async function loadPlantDetails() {
  if (plantDetails.value.length && calendarPlants.value.length) return
  detailsLoading.value = true
  try {
    const [details, calPlants] = await Promise.all([
      plantDetails.value.length ? Promise.resolve(plantDetails.value) : plantDetailService.getAll(),
      calendarPlantService.getAll()
    ])
    plantDetails.value = details
    calendarPlants.value = calPlants
  } catch {
    toast.error('Error al cargar los datos del calendario')
  } finally {
    detailsLoading.value = false
  }
}

function openEditDetail(detail) {
  editingDetail.value = detail
  Object.assign(detailForm, {
    plantName:         detail.plantName,
    latinName:         detail.latinName         || '',
    wateringFrequency: detail.wateringFrequency || '',
    pests:             detail.pests             || '',
    wikipediaUrl:      detail.wikipediaUrl      || ''
  })
  detailFormSnapshot = JSON.stringify({ ...detailForm })
  showDetailForm.value = true
}

function tryCloseDetailForm() {
  if (JSON.stringify({ ...detailForm }) !== detailFormSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showDetailForm.value = false
  editingDetail.value = null
}

async function handleDetailSave() {
  savingDetail.value = true
  try {
    const updated = await plantDetailService.update(editingDetail.value.id, { ...detailForm })
    const idx = plantDetails.value.findIndex(d => d.id === editingDetail.value.id)
    if (idx !== -1) plantDetails.value[idx] = updated
    toast.success(`Ficha de ${updated.plantName} actualizada`)
    showDetailForm.value = false
    editingDetail.value = null
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar la ficha')
  } finally {
    savingDetail.value = false
  }
}

// ── Calendar plant form ────────────────────────────────────────────────────
function openCreateCalendarPlant() {
  editingCalendarPlant.value = null
  Object.assign(calendarPlantForm, {
    plantName: '', emoji: '', hemisphere: 'NORTE', difficulty: 'MEDIA', daysToHarvest: 60,
    sewingMonths: [], transplantMonths: [], harvestMonths: [],
    latinName: '', wateringFrequency: '', pests: '', wikipediaUrl: ''
  })
  calendarPlantSnapshot = JSON.stringify(snapshotCalendarPlantForm())
  showCalendarPlantForm.value = true
}

function openEditCalendarPlant(plant) {
  editingCalendarPlant.value = plant
  Object.assign(calendarPlantForm, {
    plantName:         plant.plantName,
    emoji:             plant.emoji,
    hemisphere:        plant.hemisphere,
    difficulty:        plant.difficulty,
    daysToHarvest:     plant.daysToHarvest,
    sewingMonths:      [...(plant.sewingMonths || [])],
    transplantMonths:  [...(plant.transplantMonths || [])],
    harvestMonths:     [...(plant.harvestMonths || [])],
    latinName:         plant.latinName         || '',
    wateringFrequency: plant.wateringFrequency || '',
    pests:             plant.pests             || '',
    wikipediaUrl:      plant.wikipediaUrl      || ''
  })
  calendarPlantSnapshot = JSON.stringify(snapshotCalendarPlantForm())
  showCalendarPlantForm.value = true
}

function snapshotCalendarPlantForm() {
  return {
    ...calendarPlantForm,
    sewingMonths:     [...calendarPlantForm.sewingMonths],
    transplantMonths: [...calendarPlantForm.transplantMonths],
    harvestMonths:    [...calendarPlantForm.harvestMonths]
  }
}

function tryCloseCalendarPlantForm() {
  if (JSON.stringify(snapshotCalendarPlantForm()) !== calendarPlantSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showCalendarPlantForm.value = false
  editingCalendarPlant.value = null
}

async function handleCalendarPlantSave() {
  savingCalendarPlant.value = true
  try {
    const payload = { ...calendarPlantForm }
    if (editingCalendarPlant.value) {
      const updated = await calendarPlantService.update(editingCalendarPlant.value.id, payload)
      const idx = calendarPlants.value.findIndex(p => p.id === editingCalendarPlant.value.id)
      if (idx !== -1) calendarPlants.value[idx] = updated
      toast.success(`Planta ${updated.plantName} actualizada`)
    } else {
      const created = await calendarPlantService.create(payload)
      calendarPlants.value.push(created)
      toast.success(`Planta ${created.plantName} añadida al calendario`)
    }
    showCalendarPlantForm.value = false
    editingCalendarPlant.value = null
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar la planta')
  } finally {
    savingCalendarPlant.value = false
  }
}

const confirmDeleteCalendarPlant = ref(null)
const deletingCalendarPlant = ref(false)

function openDeleteCalendarPlant(plant) {
  confirmDeleteCalendarPlant.value = plant
}

async function doDeleteCalendarPlant() {
  if (!confirmDeleteCalendarPlant.value) return
  deletingCalendarPlant.value = true
  try {
    await calendarPlantService.delete(confirmDeleteCalendarPlant.value.id)
    calendarPlants.value = calendarPlants.value.filter(p => p.id !== confirmDeleteCalendarPlant.value.id)
    toast.success(`Planta ${confirmDeleteCalendarPlant.value.plantName} eliminada`)
    confirmDeleteCalendarPlant.value = null
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al eliminar la planta')
  } finally {
    deletingCalendarPlant.value = false
  }
}

function toggleMonth(arr, month) {
  const idx = arr.indexOf(month)
  if (idx === -1) arr.push(month)
  else arr.splice(idx, 1)
}

// ── Users helpers ─────────────────────────────────────────────────────────
function isCurrentUser(user) {
  return authStore.user?.username === user.username
}
function openRoleConfirm(user) { confirmRoleUser.value = user }
function openDeleteConfirm(user) {
  deleteStage.value = 1
  confirmDeleteUser.value = user
}
function closeModals() {
  confirmRoleUser.value = null
  confirmDeleteUser.value = null
  deleteStage.value = 1
}

async function confirmChangeRole() {
  if (!confirmRoleUser.value) return
  actionLoading.value = true
  try {
    const newRole = confirmRoleUser.value.role === 'ADMIN' ? 'USER' : 'ADMIN'
    const updated = await adminService.changeRole(confirmRoleUser.value.id, newRole)
    const idx = users.value.findIndex(u => u.id === updated.id)
    if (idx !== -1) users.value[idx] = updated
    toast.success(`Rol de ${updated.username} cambiado a ${updated.role}`)
    closeModals()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al cambiar el rol')
    closeModals()
  } finally {
    actionLoading.value = false
  }
}

async function confirmDelete() {
  if (!confirmDeleteUser.value) return
  if (deleteStage.value === 1) { deleteStage.value = 2; return }
  actionLoading.value = true
  try {
    const username = confirmDeleteUser.value.username
    await adminService.deleteUser(confirmDeleteUser.value.id)
    users.value = users.value.filter(u => u.id !== confirmDeleteUser.value.id)
    if (stats.value) stats.value.totalUsers--
    toast.success(`Usuario ${username} eliminado correctamente`)
    closeModals()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al eliminar el usuario')
    closeModals()
  } finally {
    actionLoading.value = false
  }
}

const statCards = [
  { key: 'totalUsers',    label: 'Usuarios',   icon: 'bi-people',        color: 'text-primary' },
  { key: 'totalGardens',  label: 'Huertos',    icon: 'bi-house-heart',   color: 'text-success' },
  { key: 'totalCrops',    label: 'Cultivos',   icon: 'bi-flower1',       color: 'text-warning' },
  { key: 'totalHarvests', label: 'Cosechas',   icon: 'bi-basket',        color: 'text-info' },
  { key: 'totalTasks',    label: 'Tareas',     icon: 'bi-check2-square', color: 'text-secondary' }
]

// ── Tooltips ──────────────────────────────────────────────────────────────
const calendarPlantModalRef = ref(null)
let activeTooltips = []

watch(showCalendarPlantForm, async (val) => {
  if (val) {
    await nextTick()
    activeTooltips = Array.from(
      calendarPlantModalRef.value?.querySelectorAll('[data-bs-toggle="tooltip"]') ?? []
    ).map(el => new Tooltip(el, { trigger: 'hover focus' }))
  } else {
    activeTooltips.forEach(t => t.dispose())
    activeTooltips = []
  }
})
</script>

<template>
  <div>
    <h4 class="mb-4">
      <i class="bi bi-shield-lock me-2 text-danger"></i>Panel de Administración
    </h4>

    <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
      {{ error }}
      <button type="button" class="btn-close" @click="error = ''"></button>
    </div>

    <!-- Tabs -->
    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'stats' }" @click="activeTab = 'stats'">
          <i class="bi bi-bar-chart me-1"></i>Estadísticas
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">
          <i class="bi bi-people me-1"></i>Usuarios
        </button>
      </li>
      <li class="nav-item">
        <button
          class="nav-link"
          :class="{ active: activeTab === 'calendar' }"
          @click="activeTab = 'calendar'; loadPlantDetails()"
        >
          <i class="bi bi-journal-text me-1"></i>Calendario
        </button>
      </li>
    </ul>

    <!-- Tab: Estadísticas -->
    <section v-if="activeTab === 'stats'">
      <h6 class="text-muted text-uppercase fw-semibold mb-3" style="font-size:.75rem;letter-spacing:.05em">
        Estadísticas globales
      </h6>
      <div v-if="loading" class="text-center py-3">
        <div class="spinner-border spinner-border-sm text-success"></div>
      </div>
      <div v-else class="row g-3">
        <div v-for="card in statCards" :key="card.key" class="col-6 col-md-4 col-lg">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-body text-center py-3">
              <i :class="`bi ${card.icon} fs-2 ${card.color}`"></i>
              <div class="fs-3 fw-bold mt-1">{{ stats?.[card.key] ?? '—' }}</div>
              <div class="text-muted small">{{ card.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Tab: Usuarios -->
    <section v-if="activeTab === 'users'">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="text-muted text-uppercase fw-semibold mb-0" style="font-size:.75rem;letter-spacing:.05em">
          Gestión de usuarios
        </h6>
        <button class="btn btn-success btn-sm" @click="openCreateUser">
          <i class="bi bi-person-plus me-1"></i>Nuevo usuario
        </button>
      </div>
      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <div v-if="loading" class="text-center py-4">
            <div class="spinner-border spinner-border-sm text-success"></div>
          </div>
          <div v-else-if="users.length === 0" class="text-center py-5">
            <div class="fs-2 mb-2">👤</div>
            <p class="text-muted mb-2">No hay usuarios registrados</p>
            <button class="btn btn-success btn-sm" @click="openCreateUser">
              <i class="bi bi-person-plus me-1"></i>Crear primer usuario
            </button>
          </div>
          <div v-else class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th class="ps-3">Usuario</th>
                  <th>Email</th>
                  <th>Rol</th>
                  <th>Ciudad</th>
                  <th>Hemisferio</th>
                  <th>Registrado</th>
                  <th class="text-end pe-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td class="ps-3">
                    <span class="fw-semibold">{{ user.username }}</span>
                    <span v-if="isCurrentUser(user)" class="badge bg-secondary ms-2" title="Tú">tú</span>
                  </td>
                  <td class="text-muted small">{{ user.email }}</td>
                  <td>
                    <span :class="`badge ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-success'}`">
                      {{ user.role }}
                    </span>
                  </td>
                  <td class="text-muted small">{{ user.city || '—' }}</td>
                  <td class="text-muted small">{{ user.hemisphere || '—' }}</td>
                  <td class="text-muted small">{{ user.createdAt?.substring(0, 10) }}</td>
                  <td class="text-end pe-3">
                    <button
                      class="btn btn-sm btn-outline-secondary me-1"
                      :disabled="isCurrentUser(user)"
                      :title="isCurrentUser(user) ? 'No puedes cambiar tu propio rol' : `Cambiar a ${user.role === 'ADMIN' ? 'USER' : 'ADMIN'}`"
                      @click="openRoleConfirm(user)"
                    >
                      <i class="bi bi-arrow-left-right"></i>
                    </button>
                    <button
                      class="btn btn-sm btn-outline-danger"
                      :disabled="isCurrentUser(user)"
                      :title="isCurrentUser(user) ? 'No puedes eliminarte a ti mismo' : 'Eliminar usuario'"
                      @click="openDeleteConfirm(user)"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <!-- Tab: Calendario -->
    <section v-if="activeTab === 'calendar'">
      <div v-if="detailsLoading" class="text-center py-4">
        <div class="spinner-border spinner-border-sm text-success"></div>
      </div>

      <template v-else>
        <!-- Plantas del sistema (fichas editables) -->
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h6 class="text-muted text-uppercase fw-semibold mb-0" style="font-size:.75rem;letter-spacing:.05em">
            Plantas del sistema <span class="badge bg-secondary ms-1">{{ plantDetails.length }}</span>
          </h6>
        </div>

        <div class="card border-0 shadow-sm mb-4">
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="ps-3">Planta</th>
                    <th>Nombre latino</th>
                    <th>Riego</th>
                    <th>Plagas</th>
                    <th>Wikipedia</th>
                    <th class="text-end pe-3">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="d in plantDetails" :key="d.id">
                    <td class="ps-3 fw-semibold">{{ d.plantName }}</td>
                    <td class="text-muted fst-italic small">{{ d.latinName || '—' }}</td>
                    <td class="small">{{ d.wateringFrequency || '—' }}</td>
                    <td class="small text-muted" style="max-width:180px">
                      <span class="text-truncate d-inline-block" style="max-width:170px">
                        {{ d.pests || '—' }}
                      </span>
                    </td>
                    <td>
                      <a v-if="d.wikipediaUrl" :href="d.wikipediaUrl" target="_blank" rel="noopener" class="text-success small">
                        <i class="bi bi-box-arrow-up-right"></i>
                      </a>
                      <span v-else class="text-muted small">—</span>
                    </td>
                    <td class="text-end pe-3">
                      <button class="btn btn-sm btn-outline-primary" @click="openEditDetail(d)">
                        <i class="bi bi-pencil me-1"></i>Editar ficha
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Plantas añadidas (de BD) -->
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h6 class="text-muted text-uppercase fw-semibold mb-0" style="font-size:.75rem;letter-spacing:.05em">
            Plantas añadidas <span class="badge bg-success ms-1">{{ calendarPlants.length }}</span>
          </h6>
          <button class="btn btn-success btn-sm" @click="openCreateCalendarPlant">
            <i class="bi bi-plus-lg me-1"></i>Nueva planta
          </button>
        </div>

        <div class="card border-0 shadow-sm">
          <div class="card-body p-0">
            <div v-if="calendarPlants.length === 0" class="text-center py-5">
              <div class="fs-2 mb-2">🌱</div>
              <p class="text-muted mb-2">Aún no has añadido plantas personalizadas</p>
              <button class="btn btn-success btn-sm" @click="openCreateCalendarPlant">
                <i class="bi bi-plus-lg me-1"></i>Añadir primera planta
              </button>
            </div>
            <div v-else class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="ps-3">Planta</th>
                    <th>Hemisferio</th>
                    <th>Dificultad</th>
                    <th>Siembra</th>
                    <th>Trasplante</th>
                    <th>Cosecha</th>
                    <th class="text-end pe-3">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in calendarPlants" :key="p.id">
                    <td class="ps-3">
                      <span class="me-1">{{ p.emoji }}</span>
                      <span class="fw-semibold">{{ p.plantName }}</span>
                    </td>
                    <td class="small text-muted">{{ p.hemisphere }}</td>
                    <td>
                      <span :class="`badge ${p.difficulty === 'FACIL' ? 'bg-success' : p.difficulty === 'DIFICIL' ? 'bg-danger' : 'bg-warning text-dark'}`" style="font-size:.65rem">
                        {{ p.difficulty === 'FACIL' ? 'Fácil' : p.difficulty === 'DIFICIL' ? 'Difícil' : 'Media' }}
                      </span>
                    </td>
                    <td class="small text-muted">
                      {{ p.sewingMonths?.length ? p.sewingMonths.join(', ') : '—' }}
                    </td>
                    <td class="small text-muted">
                      {{ p.transplantMonths?.length ? p.transplantMonths.join(', ') : '—' }}
                    </td>
                    <td class="small text-muted">
                      {{ p.harvestMonths?.length ? p.harvestMonths.join(', ') : '—' }}
                    </td>
                    <td class="text-end pe-3">
                      <button class="btn btn-sm btn-outline-primary me-1" @click="openEditCalendarPlant(p)">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger" @click="openDeleteCalendarPlant(p)">
                        <i class="bi bi-trash"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </template>
    </section>

    <!-- Modal: Crear usuario -->
    <div v-if="showCreateUser" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.45)" @click.self="tryCloseCreateUser">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title"><i class="bi bi-person-plus me-2"></i>Nuevo usuario</h5>
            <button type="button" class="btn-close btn-close-white" @click="tryCloseCreateUser"></button>
          </div>
          <form @submit.prevent="handleCreateUser">
            <div class="modal-body">
              <div v-if="createUserError" class="alert alert-danger py-2 small">{{ createUserError }}</div>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Nombre de usuario <span class="text-danger">*</span></label>
                  <input v-model="createUserForm.username" class="form-control" required minlength="3" maxlength="50" placeholder="usuario123" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Email <span class="text-danger">*</span></label>
                  <input v-model="createUserForm.email" type="email" class="form-control" required placeholder="usuario@email.com" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Contraseña <span class="text-danger">*</span></label>
                  <input v-model="createUserForm.password" type="password" class="form-control" required minlength="6" placeholder="Mínimo 6 caracteres" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Confirmar contraseña <span class="text-danger">*</span></label>
                  <input v-model="createUserForm.confirmPassword" type="password" class="form-control" required placeholder="Repite la contraseña" />
                </div>
                <div class="col-md-4">
                  <label class="form-label">Rol</label>
                  <select v-model="createUserForm.role" class="form-select">
                    <option value="USER">Usuario</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label">Hemisferio</label>
                  <select v-model="createUserForm.hemisphere" class="form-select">
                    <option value="NORTE">Norte</option>
                    <option value="SUR">Sur</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label">Código país</label>
                  <input v-model="createUserForm.countryCode" class="form-control" maxlength="2" placeholder="ES" />
                </div>
                <div class="col-12">
                  <label class="form-label">Ciudad</label>
                  <input v-model="createUserForm.city" class="form-control" placeholder="Madrid" />
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" @click="tryCloseCreateUser">Cancelar</button>
              <button type="submit" class="btn btn-success" :disabled="savingUser">
                <span v-if="savingUser" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="bi bi-check-lg me-1"></i>Crear usuario
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal: Cambiar rol -->
    <div v-if="confirmRoleUser" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.4)">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title"><i class="bi bi-arrow-left-right me-2"></i>Cambiar rol</h5>
            <button type="button" class="btn-close" @click="closeModals"></button>
          </div>
          <div class="modal-body">
            ¿Cambiar el rol de <strong>{{ confirmRoleUser.username }}</strong> de
            <span :class="`badge ${confirmRoleUser.role === 'ADMIN' ? 'bg-danger' : 'bg-success'}`">{{ confirmRoleUser.role }}</span>
            a
            <span :class="`badge ${confirmRoleUser.role === 'ADMIN' ? 'bg-success' : 'bg-danger'}`">{{ confirmRoleUser.role === 'ADMIN' ? 'USER' : 'ADMIN' }}</span>?
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" @click="closeModals">Cancelar</button>
            <button class="btn btn-primary btn-sm" :disabled="actionLoading" @click="confirmChangeRole">
              <span v-if="actionLoading" class="spinner-border spinner-border-sm me-1"></span>
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Eliminar usuario -->
    <div v-if="confirmDeleteUser" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.4)">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-danger">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>
              {{ deleteStage === 1 ? '¿Seguro?' : '¿Muy seguro?' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModals"></button>
          </div>
          <div class="modal-body">
            <div v-if="deleteStage === 1">
              ¿Eliminar el usuario <strong>{{ confirmDeleteUser.username }}</strong>?
              Se borrarán todos sus huertos, cultivos, tareas y cosechas.
            </div>
            <div v-else class="text-danger fw-semibold">
              <i class="bi bi-exclamation-circle me-1"></i>
              Esta acción es <strong>irreversible</strong>. ¿Confirmas que quieres eliminar a
              <strong>{{ confirmDeleteUser.username }}</strong> y todos sus datos?
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" @click="closeModals">Cancelar</button>
            <button class="btn btn-danger btn-sm" :disabled="actionLoading" @click="confirmDelete">
              <span v-if="actionLoading" class="spinner-border spinner-border-sm me-1"></span>
              {{ deleteStage === 1 ? 'Sí, eliminar' : 'Sí, estoy seguro' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Editar ficha de planta del sistema -->
    <div v-if="showDetailForm" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.4)" @click.self="tryCloseDetailForm">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-journal-text me-2"></i>Ficha — {{ editingDetail?.plantName }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="tryCloseDetailForm"></button>
          </div>
          <form @submit.prevent="handleDetailSave">
            <div class="modal-body">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Nombre latino</label>
                  <input v-model="detailForm.latinName" class="form-control" placeholder="Solanum lycopersicum" />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Frecuencia de riego</label>
                  <input v-model="detailForm.wateringFrequency" class="form-control" placeholder="3-4 veces/semana" />
                </div>
                <div class="col-12">
                  <label class="form-label">Plagas <span class="text-muted small">(separadas por comas)</span></label>
                  <input v-model="detailForm.pests" class="form-control" placeholder="Pulgón, Mosca blanca, Trips" />
                </div>
                <div class="col-12">
                  <label class="form-label">Enlace Wikipedia</label>
                  <input v-model="detailForm.wikipediaUrl" type="url" class="form-control" placeholder="https://es.wikipedia.org/wiki/..." />
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" @click="tryCloseDetailForm">Cancelar</button>
              <button type="submit" class="btn btn-success" :disabled="savingDetail">
                <span v-if="savingDetail" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="bi bi-check-lg me-1"></i>Guardar ficha
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal: Crear / Editar planta del calendario -->
    <div v-if="showCalendarPlantForm" ref="calendarPlantModalRef" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.45)" @click.self="tryCloseCalendarPlantForm">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-plus-circle me-2"></i>
              {{ editingCalendarPlant ? `Editar: ${editingCalendarPlant.plantName}` : 'Nueva planta' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="tryCloseCalendarPlantForm"></button>
          </div>
          <form @submit.prevent="handleCalendarPlantSave">
            <div class="modal-body" style="max-height:62vh;overflow-y:auto;">

              <!-- Sección 1: Datos básicos -->
              <div class="d-flex align-items-center gap-2 mb-3">
                <span class="badge bg-success rounded-pill px-2">1</span>
                <h6 class="text-uppercase fw-semibold mb-0 text-muted" style="font-size:.7rem;letter-spacing:.06em">Datos básicos</h6>
              </div>

              <div class="row g-3 mb-4">

                <!-- Nombre -->
                <div class="col-md-6">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Nombre <span class="text-danger">*</span>
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Nombre común en español, tal como aparecerá en el calendario. Debe ser único en el sistema. Ej: Albahaca, Lavanda, Jengibre."></i>
                  </label>
                  <input v-model="calendarPlantForm.plantName" class="form-control" required placeholder="ej. Albahaca" />
                </div>

                <!-- Emoji -->
                <div class="col-md-6">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Icono (emoji) <span class="text-danger">*</span>
                    <span v-if="calendarPlantForm.emoji" class="ms-1" style="font-size:1.3rem;line-height:1">{{ calendarPlantForm.emoji }}</span>
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Abre el selector de emojis del sistema: Windows → tecla Win + punto (.) | Mac → Cmd + Ctrl + Space. También puedes copiar uno de Emojipedia y pegarlo aquí."></i>
                  </label>
                  <div class="input-group">
                    <input v-model="calendarPlantForm.emoji" class="form-control" required placeholder="🌿" maxlength="4" />
                    <a href="https://emojipedia.org/es/plants"
                       target="_blank" rel="noopener"
                       class="input-group-text text-success text-decoration-none"
                       title="Buscar emojis de plantas en Emojipedia">
                      <i class="bi bi-search me-1"></i><span class="small">Buscar emoji</span>
                    </a>
                  </div>
                </div>

                <!-- Hemisferio -->
                <div class="col-md-4">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Hemisferio <span class="text-danger">*</span>
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Norte: España, Europa, Asia, Norteamérica. Sur: Australia, Sudamérica, África del Sur. Las estaciones son opuestas. Si la planta se cultiva en ambos hemisferios, añádela dos veces."></i>
                  </label>
                  <select v-model="calendarPlantForm.hemisphere" class="form-select" required>
                    <option value="NORTE">🌍 Norte</option>
                    <option value="SUR">🌏 Sur</option>
                  </select>
                </div>

                <!-- Dificultad -->
                <div class="col-md-4">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Dificultad <span class="text-danger">*</span>
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Fácil: poca atención, ideal para principiantes (lechuga, rábano). Media: riegos regulares y algo de experiencia (tomate, pimiento). Difícil: cuidados específicos o microclima controlado (alcachofa, espárrago)."></i>
                  </label>
                  <select v-model="calendarPlantForm.difficulty" class="form-select" required>
                    <option value="FACIL">🟢 Fácil</option>
                    <option value="MEDIA">🟡 Media</option>
                    <option value="DIFICIL">🔴 Difícil</option>
                  </select>
                </div>

                <!-- Días hasta cosecha -->
                <div class="col-md-4">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Días hasta cosecha <span class="text-danger">*</span>
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Días desde la siembra hasta la primera cosecha. Ejemplos: lechuga → 45, tomate → 90, zanahoria → 75, espárrago → 730 (2 años)."></i>
                  </label>
                  <div class="input-group">
                    <input v-model.number="calendarPlantForm.daysToHarvest" type="number" class="form-control" required min="1" max="999" />
                    <span class="input-group-text text-muted small">días</span>
                  </div>
                </div>
              </div>

              <hr class="my-3">

              <!-- Sección 2: Meses de actividad -->
              <div class="d-flex align-items-center gap-2 mb-3">
                <span class="badge bg-success rounded-pill px-2">2</span>
                <h6 class="text-uppercase fw-semibold mb-0 text-muted" style="font-size:.7rem;letter-spacing:.06em">Meses de actividad</h6>
                <i class="bi bi-info-circle text-muted"
                   style="cursor:help;font-size:.85rem"
                   data-bs-toggle="tooltip"
                   data-bs-placement="top"
                   title="Pulsa los meses para activarlos o desactivarlos. Cada categoría es independiente: una planta puede sembrarse en marzo y cosecharse en julio. Deja vacío si la categoría no aplica."></i>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-12">
                  <label class="form-label small fw-semibold text-success mb-1">🌱 Siembra — meses para sembrar la semilla directamente en tierra</label>
                  <div class="d-flex flex-wrap gap-1">
                    <button v-for="(name, i) in MONTHS" :key="`sow-${i}`" type="button"
                      :class="`btn btn-sm ${calendarPlantForm.sewingMonths.includes(i+1) ? 'btn-success' : 'btn-outline-success'}`"
                      @click="toggleMonth(calendarPlantForm.sewingMonths, i+1)">{{ name }}</button>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold text-warning mb-1">🌿 Trasplante — meses para mover plántulas al lugar definitivo</label>
                  <div class="d-flex flex-wrap gap-1">
                    <button v-for="(name, i) in MONTHS" :key="`trans-${i}`" type="button"
                      :class="`btn btn-sm ${calendarPlantForm.transplantMonths.includes(i+1) ? 'btn-warning' : 'btn-outline-warning'}`"
                      @click="toggleMonth(calendarPlantForm.transplantMonths, i+1)">{{ name }}</button>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold mb-1" style="color:#fd7e14">🌾 Cosecha — meses en los que se puede recolectar el fruto</label>
                  <div class="d-flex flex-wrap gap-1">
                    <button v-for="(name, i) in MONTHS" :key="`harv-${i}`" type="button"
                      :class="`btn btn-sm ${calendarPlantForm.harvestMonths.includes(i+1) ? 'text-white' : 'btn-outline-warning'}`"
                      :style="calendarPlantForm.harvestMonths.includes(i+1) ? 'background-color:#fd7e14;border-color:#fd7e14' : ''"
                      @click="toggleMonth(calendarPlantForm.harvestMonths, i+1)">{{ name }}</button>
                  </div>
                </div>
              </div>

              <hr class="my-3">

              <!-- Sección 3: Ficha informativa -->
              <div class="d-flex align-items-center gap-2 mb-3">
                <span class="badge bg-secondary rounded-pill px-2">3</span>
                <h6 class="text-uppercase fw-semibold mb-0 text-muted" style="font-size:.7rem;letter-spacing:.06em">
                  Ficha informativa <span class="fw-normal">(opcional)</span>
                </h6>
                <i class="bi bi-info-circle text-muted"
                   style="cursor:help;font-size:.85rem"
                   data-bs-toggle="tooltip"
                   data-bs-placement="top"
                   title="Esta información aparece cuando el usuario pulsa 'Ver ficha' en el calendario. Si la dejas vacía, la ficha mostrará un mensaje indicando que aún no hay datos."></i>
              </div>

              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Nombre científico
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Nombre en latín de la especie. Aparece en cursiva bajo el nombre común. Ej: Solanum lycopersicum (tomate), Lactuca sativa (lechuga), Daucus carota (zanahoria)."></i>
                  </label>
                  <input v-model="calendarPlantForm.latinName" class="form-control" placeholder="ej. Ocimum basilicum" />
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Frecuencia de riego
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Describe con qué frecuencia regar. Ej: «Riego moderado, 2-3 veces/semana. En verano aumentar frecuencia. Evitar encharcamiento.»"></i>
                  </label>
                  <input v-model="calendarPlantForm.wateringFrequency" class="form-control" placeholder="ej. 3-4 veces por semana" />
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Plagas y enfermedades a vigilar
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Escribe las amenazas separadas por comas. Cada una aparecerá como etiqueta individual en la ficha. Ej: Pulgón, Mosca blanca, Trips, Mildiu, Oídio."></i>
                  </label>
                  <input v-model="calendarPlantForm.pests" class="form-control" placeholder="ej. Pulgón, Araña roja, Oídio" />
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold d-flex align-items-center gap-1">
                    Enlace a Wikipedia
                    <i class="bi bi-info-circle text-muted ms-1"
                       style="cursor:help;font-size:.85rem"
                       data-bs-toggle="tooltip"
                       data-bs-placement="top"
                       title="Ve a es.wikipedia.org, busca la planta, y copia la URL completa de la barra del navegador. Ej: https://es.wikipedia.org/wiki/Solanum_lycopersicum"></i>
                  </label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-wikipedia text-muted"></i></span>
                    <input v-model="calendarPlantForm.wikipediaUrl" type="url" class="form-control" placeholder="https://es.wikipedia.org/wiki/Ocimum_basilicum" />
                    <a href="https://es.wikipedia.org"
                       target="_blank" rel="noopener"
                       class="input-group-text text-success text-decoration-none"
                       title="Abrir Wikipedia en español">
                      <i class="bi bi-box-arrow-up-right me-1"></i><span class="small">Abrir</span>
                    </a>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" @click="tryCloseCalendarPlantForm">Cancelar</button>
              <button type="submit" class="btn btn-success" :disabled="savingCalendarPlant">
                <span v-if="savingCalendarPlant" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="bi bi-check-lg me-1"></i>
                {{ editingCalendarPlant ? 'Guardar cambios' : 'Añadir planta' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal: Confirmar eliminar planta del calendario -->
    <div v-if="confirmDeleteCalendarPlant" class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,.4)">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-danger">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title"><i class="bi bi-exclamation-triangle me-2"></i>Eliminar planta</h5>
            <button type="button" class="btn-close btn-close-white" @click="confirmDeleteCalendarPlant = null"></button>
          </div>
          <div class="modal-body">
            ¿Eliminar <strong>{{ confirmDeleteCalendarPlant.emoji }} {{ confirmDeleteCalendarPlant.plantName }}</strong> del calendario?
            Dejará de aparecer en el calendario público.
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" @click="confirmDeleteCalendarPlant = null">Cancelar</button>
            <button class="btn btn-danger btn-sm" :disabled="deletingCalendarPlant" @click="doDeleteCalendarPlant">
              <span v-if="deletingCalendarPlant" class="spinner-border spinner-border-sm me-1"></span>
              Eliminar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
