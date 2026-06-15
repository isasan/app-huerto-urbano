<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { taskService } from '@/services/taskService.js'
import { gardenService } from '@/services/gardenService.js'
import { useToast } from '@/composables/useToast'
import TaskBoard from '@/components/tasks/TaskBoard.vue'
import BaseModal from '@/components/ui/BaseModal.vue'

const toast = useToast()
const tasks = ref([])
const gardens = ref([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const editingTask = ref(null)

const activeFilter = ref('all')
const activeType = ref('')
const currentPage = ref(1)
const PAGE_SIZE = 20

const form = reactive({
  title: '', type: 'RIEGO', dueDate: '', notes: '', gardenId: null
})
let formSnapshot = ''

const today = new Date().toISOString().split('T')[0]

const filteredTasks = computed(() => {
  let result = [...tasks.value]

  if (activeFilter.value === 'pending') result = result.filter(t => !t.completed)
  else if (activeFilter.value === 'completed') result = result.filter(t => t.completed)
  else if (activeFilter.value === 'overdue') result = result.filter(t => !t.completed && t.dueDate && t.dueDate < today)

  if (activeType.value) result = result.filter(t => t.type === activeType.value)

  return result.sort((a, b) => {
    if (!a.dueDate) return 1
    if (!b.dueDate) return -1
    return a.dueDate.localeCompare(b.dueDate)
  })
})

const totalPages = computed(() => Math.ceil(filteredTasks.value.length / PAGE_SIZE))

const pagedTasks = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredTasks.value.slice(start, start + PAGE_SIZE)
})

const overdueCount = computed(() =>
  tasks.value.filter(t => !t.completed && t.dueDate && t.dueDate < today).length
)

watch([activeFilter, activeType], () => { currentPage.value = 1 })

onMounted(async () => {
  loading.value = true
  try {
    ;[tasks.value, gardens.value] = await Promise.all([
      taskService.getAll(),
      gardenService.getAll()
    ])
    if (gardens.value.length > 0) form.gardenId = gardens.value[0].id
  } catch {
    toast.error('Error al cargar las tareas')
  } finally {
    loading.value = false
  }
})

function openCreate() {
  editingTask.value = null
  Object.assign(form, { title: '', type: 'RIEGO', dueDate: '', notes: '', gardenId: gardens.value[0]?.id ?? null })
  formSnapshot = JSON.stringify({ ...form })
  showForm.value = true
}

function openEdit(task) {
  editingTask.value = task
  Object.assign(form, {
    title: task.title,
    type: task.type,
    dueDate: task.dueDate || '',
    notes: task.notes || '',
    gardenId: task.gardenId
  })
  formSnapshot = JSON.stringify({ ...form })
  showForm.value = true
}

function tryCloseForm() {
  if (JSON.stringify({ ...form }) !== formSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showForm.value = false
}

async function handleSave() {
  saving.value = true
  try {
    if (editingTask.value) {
      const updated = await taskService.update(editingTask.value.id, { ...form })
      const idx = tasks.value.findIndex(t => t.id === editingTask.value.id)
      if (idx !== -1) tasks.value[idx] = updated
      toast.success('Tarea actualizada')
    } else {
      const created = await taskService.create({ ...form })
      tasks.value.push(created)
      toast.success('Tarea creada')
    }
    showForm.value = false
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar la tarea')
  } finally {
    saving.value = false
  }
}

function toggleTask(id) {
  const t = tasks.value.find(t => t.id === id)
  if (t) t.completed = !t.completed
  taskService.toggleComplete(id).then(updated => {
    const idx = tasks.value.findIndex(t => t.id === id)
    if (idx !== -1) tasks.value[idx] = updated
  }).catch(() => {
    const t = tasks.value.find(t => t.id === id)
    if (t) t.completed = !t.completed
    toast.error('Error al actualizar la tarea')
  })
}

async function deleteTask(id) {
  if (!confirm('¿Eliminar esta tarea?')) return
  try {
    await taskService.delete(id)
    tasks.value = tasks.value.filter(t => t.id !== id)
    toast.success('Tarea eliminada')
  } catch {
    toast.error('Error al eliminar la tarea')
  }
}
</script>

<template>
  <div class="tasks-view">
    <div class="page-header animate-up">
      <div>
        <h1 class="page-title"><span aria-hidden="true">✅</span> Tareas</h1>
        <p class="page-sub">Planifica los cuidados de tu huerto</p>
      </div>
      <button class="btn btn-success" @click="openCreate">
        <i class="bi bi-plus-lg me-1"></i>Nueva tarea
      </button>
    </div>

    <!-- Filtros -->
    <div class="filter-bar animate-up" role="group" aria-label="Filtrar tareas">
      <div class="seg-toggle">
        <button
          v-for="f in [
            { key: 'all', label: 'Todas' },
            { key: 'pending', label: 'Pendientes' },
            { key: 'completed', label: 'Completadas' },
            { key: 'overdue', label: 'Vencidas' }
          ]"
          :key="f.key"
          class="seg-btn"
          :class="{ active: activeFilter === f.key }"
          :aria-pressed="activeFilter === f.key"
          @click="activeFilter = f.key"
        >
          {{ f.label }}
          <span v-if="f.key === 'overdue' && overdueCount > 0" class="seg-count">{{ overdueCount }}</span>
        </button>
      </div>
      <select v-model="activeType" class="form-select form-select-sm w-auto" aria-label="Filtrar por tipo">
        <option value="">Todos los tipos</option>
        <option value="RIEGO">💧 Riego</option>
        <option value="FERTILIZAR">🌱 Fertilizar</option>
        <option value="PODAR">✂️ Podar</option>
        <option value="TRATAR">🛡️ Tratar</option>
        <option value="OTRO">📋 Otro</option>
      </select>
    </div>

    <div v-if="loading" class="row g-3" aria-busy="true" aria-label="Cargando tareas">
      <div v-for="i in 3" :key="i" class="col-md-6 col-xl-4">
        <div class="task-skel panel-card">
          <div class="skel skel-head"></div>
          <div class="skel skel-row"></div>
          <div class="skel skel-row"></div>
        </div>
      </div>
    </div>

    <template v-else>
      <div
        v-if="filteredTasks.length === 0"
        class="empty-state panel-card"
        style="min-height: 220px"
        role="status"
      >
        <span class="empty-icon" aria-hidden="true">✅</span>
        <p class="fw-semibold mb-1" style="font-size: 0.95rem">No hay tareas aquí</p>
        <p class="mb-3" style="font-size: 0.85rem; color: var(--text-light)">
          {{ activeFilter === 'all' ? 'Crea tu primera tarea para organizar el huerto' : 'Prueba con otro filtro' }}
        </p>
        <button v-if="activeFilter === 'all'" class="btn btn-success" @click="openCreate">
          <i class="bi bi-plus-lg me-1"></i>Nueva tarea
        </button>
      </div>

      <TaskBoard
        v-else
        :tasks="pagedTasks"
        @toggle="toggleTask"
        @edit="openEdit"
        @delete="deleteTask"
      />

      <!-- Paginación -->
      <nav v-if="totalPages > 1" class="d-flex justify-content-center align-items-center gap-3 mt-4">
        <button class="btn btn-sm btn-outline-secondary" :disabled="currentPage === 1" @click="currentPage--">
          <i class="bi bi-chevron-left"></i>
        </button>
        <span class="small text-muted">Página {{ currentPage }} de {{ totalPages }}</span>
        <button class="btn btn-sm btn-outline-secondary" :disabled="currentPage === totalPages" @click="currentPage++">
          <i class="bi bi-chevron-right"></i>
        </button>
      </nav>
    </template>

    <!-- Modal crear/editar tarea -->
    <BaseModal
      :show="showForm"
      :title="editingTask ? 'Editar tarea' : 'Nueva tarea'"
      :icon="editingTask ? 'bi-pencil' : 'bi-check2-square'"
      size="lg"
      scrollable
      @close="tryCloseForm"
    >
      <form @submit.prevent="handleSave">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Título <span class="text-danger">*</span></label>
            <input v-model="form.title" class="form-control" required />
          </div>
          <div class="col-md-3">
            <label class="form-label">Tipo <span class="text-danger">*</span></label>
            <select v-model="form.type" class="form-select">
              <option value="RIEGO">💧 Riego</option>
              <option value="FERTILIZAR">🌱 Fertilizar</option>
              <option value="PODAR">✂️ Podar</option>
              <option value="TRATAR">🛡️ Tratar</option>
              <option value="OTRO">📋 Otro</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label">Fecha límite</label>
            <input v-model="form.dueDate" type="date" class="form-control" />
          </div>
          <div class="col-md-6">
            <label class="form-label">Huerto <span class="text-danger">*</span></label>
            <select v-model.number="form.gardenId" class="form-select" required>
              <option v-for="g in gardens" :key="g.id" :value="g.id">{{ g.name }}</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Notas</label>
            <input v-model="form.notes" class="form-control" />
          </div>
        </div>
        <div class="d-flex gap-2 mt-3 justify-content-end">
          <button type="button" class="btn btn-outline-secondary" @click="tryCloseForm">Cancelar</button>
          <button type="submit" class="btn btn-success" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
            {{ editingTask ? 'Guardar cambios' : 'Crear tarea' }}
          </button>
        </div>
      </form>
    </BaseModal>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.page-title {
  font-family: var(--font-display) !important;
  font-size: 1.6rem !important;
  font-weight: 800 !important;
  color: var(--text-primary) !important;
  margin-bottom: 4px;
}
.page-sub {
  font-size: 0.88rem;
  color: var(--text-muted);
  margin: 0;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.seg-toggle {
  display: flex;
  flex-wrap: wrap;
  background: var(--bg-subtle);
  border-radius: var(--r-full);
  padding: 3px;
  gap: 3px;
}
.seg-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: var(--r-full);
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 0.84rem;
  border: none;
  cursor: pointer;
  transition: all var(--t-base);
  background: transparent;
  color: var(--text-muted);
}
.seg-btn.active {
  background: var(--green-600);
  color: var(--text-on-accent);
  box-shadow: 0 2px 8px rgba(5,150,105,0.3);
}
.seg-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--r-full);
  font-size: 0.7rem;
  font-weight: 800;
  background: var(--red-600);
  color: var(--text-on-accent);
}
.task-skel { padding: 18px; gap: 12px; }
.skel-head { height: 22px; width: 50%; }
.skel-row  { height: 38px; width: 100%; }
</style>
