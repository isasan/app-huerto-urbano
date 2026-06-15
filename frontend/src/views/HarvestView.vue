<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { harvestService } from '@/services/harvestService.js'
import { cropService } from '@/services/cropService.js'
import { useToast } from '@/composables/useToast'
import BaseModal from '@/components/ui/BaseModal.vue'

const toast = useToast()
const harvests = ref([])
const crops = ref([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const filterCropId = ref(null)
const currentPage = ref(1)
const PAGE_SIZE = 20

const emptyForm = () => ({
  harvestDate: new Date().toISOString().split('T')[0],
  quantity: null,
  unit: 'kg',
  quality: 'BUENA',
  notes: '',
  cropId: null
})

const form = reactive(emptyForm())
let formSnapshot = ''

onMounted(async () => {
  loading.value = true
  try {
    const [h, c] = await Promise.all([harvestService.getAll(), cropService.getAll()])
    harvests.value = h.sort((a, b) => b.harvestDate.localeCompare(a.harvestDate))
    crops.value = c
    if (c.length > 0) form.cropId = c[0].id
  } catch {
    toast.error('Error al cargar las cosechas')
  } finally {
    loading.value = false
  }
})

const filteredHarvests = computed(() => {
  const list = filterCropId.value
    ? harvests.value.filter(h => h.cropId === filterCropId.value)
    : harvests.value
  return list.sort((a, b) => b.harvestDate.localeCompare(a.harvestDate))
})

const totalPages = computed(() => Math.ceil(filteredHarvests.value.length / PAGE_SIZE))

const pagedHarvests = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredHarvests.value.slice(start, start + PAGE_SIZE)
})

watch(filterCropId, () => { currentPage.value = 1 })

const plantStats = computed(() => {
  const map = {}
  harvests.value.forEach(h => {
    const key = h.plantName
    if (!map[key]) map[key] = { plantName: key, total: 0, unit: h.unit, count: 0 }
    map[key].total += h.quantity || 0
    map[key].count++
  })
  return Object.values(map).sort((a, b) => b.total - a.total)
})

const maxStat = computed(() =>
  plantStats.value.reduce((acc, s) => Math.max(acc, s.total), 0)
)

function openCreate() {
  Object.assign(form, emptyForm())
  if (crops.value.length > 0) form.cropId = crops.value[0].id
  editingId.value = null
  formSnapshot = JSON.stringify({ ...form })
  showModal.value = true
}

function openEdit(h) {
  Object.assign(form, {
    harvestDate: h.harvestDate,
    quantity: h.quantity,
    unit: h.unit,
    quality: h.quality,
    notes: h.notes,
    cropId: h.cropId
  })
  editingId.value = h.id
  formSnapshot = JSON.stringify({ ...form })
  showModal.value = true
}

function tryCloseModal() {
  if (JSON.stringify({ ...form }) !== formSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showModal.value = false
}

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      const updated = await harvestService.update(editingId.value, { ...form })
      const idx = harvests.value.findIndex(h => h.id === editingId.value)
      harvests.value.splice(idx, 1, updated)
      toast.success('Cosecha actualizada')
    } else {
      const created = await harvestService.create({ ...form })
      harvests.value.unshift(created)
      toast.success('Cosecha registrada')
    }
    showModal.value = false
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar la cosecha')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  if (!confirm('¿Eliminar este registro de cosecha?')) return
  try {
    await harvestService.delete(id)
    harvests.value = harvests.value.filter(h => h.id !== id)
    toast.success('Cosecha eliminada')
  } catch {
    toast.error('Error al eliminar la cosecha')
  }
}

const qualityClass = {
  MALA: 'mala', NORMAL: 'normal', BUENA: 'buena', EXCELENTE: 'excelente'
}
const qualityLabel = { MALA: 'Mala', NORMAL: 'Normal', BUENA: 'Buena', EXCELENTE: 'Excelente' }
const qualityStars = { MALA: '★', NORMAL: '★★', BUENA: '★★★', EXCELENTE: '★★★★' }

function barWidth(total) {
  if (!maxStat.value) return '0%'
  return `${Math.round((total / maxStat.value) * 100)}%`
}
</script>

<template>
  <div class="harvest-view">

    <!-- ── Header ── -->
    <div class="harvest-header animate-up">
      <div>
        <h1 class="harvest-title">
          <span aria-hidden="true">🧺</span> Registro de Cosechas
        </h1>
        <p class="harvest-sub">Lleva el historial completo de lo que produces</p>
      </div>
      <button
        class="btn btn-success"
        @click="openCreate"
        :disabled="crops.length === 0"
        :title="crops.length === 0 ? 'Primero añade cultivos en tus huertos' : ''"
        aria-label="Registrar nueva cosecha"
      >
        <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>
        Registrar cosecha
      </button>
    </div>

    <!-- ── Stats chart ── -->
    <div v-if="plantStats.length > 0" class="stats-panel animate-up" role="region" aria-label="Estadísticas por planta">
      <div class="stats-panel-head">
        <div class="panel-head-icon" style="background:var(--green-100);color:var(--green-600)">
          <i class="bi bi-bar-chart-fill" aria-hidden="true"></i>
        </div>
        <h2 class="panel-head-title">Producción por planta</h2>
      </div>
      <div class="bar-chart" role="list">
        <div
          v-for="(s, idx) in plantStats"
          :key="s.plantName"
          class="bar-row"
          role="listitem"
          :aria-label="`${s.plantName}: ${s.total.toFixed(1)} ${s.unit}, ${s.count} cosecha${s.count !== 1 ? 's' : ''}`"
        >
          <div class="bar-row-header">
            <span class="bar-row-label">{{ s.plantName }}</span>
            <span class="bar-row-value">
              {{ s.total.toFixed(1) }} {{ s.unit }}
              <span style="font-size:0.72rem;opacity:0.7;font-weight:400">
                · {{ s.count }} cosecha{{ s.count !== 1 ? 's' : '' }}
              </span>
            </span>
          </div>
          <div class="bar-track">
            <div
              class="bar-fill"
              :style="`--target-w: ${barWidth(s.total)}; width: ${barWidth(s.total)}; --bar-delay: ${idx * 80}ms`"
              role="progressbar"
              :aria-valuenow="s.total"
              :aria-valuemax="maxStat"
              aria-valuemin="0"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Crop filter ── -->
    <div v-if="crops.length > 0" class="filter-row animate-up" role="group" aria-label="Filtrar por cultivo">
      <span class="filter-label">Filtrar:</span>
      <div class="filter-pills">
        <button
          class="filter-pill"
          :class="{ active: filterCropId === null }"
          @click="filterCropId = null"
          :aria-pressed="filterCropId === null"
        >Todos</button>
        <button
          v-for="c in crops"
          :key="c.id"
          class="filter-pill"
          :class="{ active: filterCropId === c.id }"
          @click="filterCropId = c.id"
          :aria-pressed="filterCropId === c.id"
        >{{ c.plantName }}</button>
      </div>
    </div>

    <!-- ── Loading ── -->
    <div v-if="loading" class="text-center py-5" aria-label="Cargando cosechas" aria-busy="true">
      <div class="spinner-border text-success" role="status">
        <span class="visually-hidden">Cargando...</span>
      </div>
    </div>

    <!-- ── Empty ── -->
    <div
      v-else-if="filteredHarvests.length === 0"
      class="empty-state panel-card"
      style="min-height:220px"
      role="status"
      aria-label="No hay cosechas registradas"
    >
      <span class="empty-icon" aria-hidden="true">🌱</span>
      <p class="mb-1 fw-semibold" style="font-size:0.95rem">¡Tu huerto tiene mucho potencial!</p>
      <p class="mb-3" style="font-size:0.85rem;color:var(--text-light)">
        Registra tu primera cosecha cuando recolectes algo.
      </p>
      <button v-if="crops.length > 0" class="btn btn-success btn-sm" @click="openCreate">
        <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Registrar cosecha
      </button>
      <p v-else class="mb-0" style="font-size:0.82rem;color:var(--text-light)">
        Primero añade cultivos en tus huertos
      </p>
    </div>

    <template v-else>
      <!-- Desktop table -->
      <div class="harvest-table-wrap d-none d-md-block animate-up">
        <table class="table table-hover align-middle" aria-label="Tabla de cosechas">
          <thead class="table-light">
            <tr>
              <th scope="col">Cultivo</th>
              <th scope="col">Huerto</th>
              <th scope="col">Fecha</th>
              <th scope="col">Cantidad</th>
              <th scope="col">Calidad</th>
              <th scope="col">Notas</th>
              <th scope="col"><span class="visually-hidden">Acciones</span></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="h in pagedHarvests"
              :key="h.id"
              class="harvest-row"
            >
              <td>
                <span class="fw-semibold" style="font-size:0.9rem">{{ h.plantName }}</span>
              </td>
              <td>
                <span style="font-size:0.82rem;color:var(--text-muted)">{{ h.gardenName }}</span>
              </td>
              <td>
                <span style="font-size:0.85rem">{{ h.harvestDate }}</span>
              </td>
              <td>
                <span v-if="h.quantity != null" class="harvest-qty-cell">
                  <strong>{{ h.quantity }}</strong>
                  <span style="opacity:0.6;font-size:0.8rem">{{ h.unit }}</span>
                </span>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <span :class="`q-pill ${qualityClass[h.quality]}`">
                  {{ qualityStars[h.quality] }} {{ qualityLabel[h.quality] }}
                </span>
              </td>
              <td>
                <span style="font-size:0.82rem;color:var(--text-muted);font-style:italic">
                  {{ h.notes || '—' }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-1">
                  <button
                    class="btn btn-sm btn-outline-secondary"
                    @click="openEdit(h)"
                    :aria-label="`Editar cosecha de ${h.plantName}`"
                  >
                    <i class="bi bi-pencil" aria-hidden="true"></i>
                  </button>
                  <button
                    class="btn btn-sm btn-outline-danger"
                    @click="handleDelete(h.id)"
                    :aria-label="`Eliminar cosecha de ${h.plantName}`"
                  >
                    <i class="bi bi-trash" aria-hidden="true"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile cards -->
      <div class="d-md-none stagger">
        <div
          v-for="h in pagedHarvests"
          :key="h.id"
          class="harvest-mobile-card panel-card mb-3"
        >
          <div class="harvest-mobile-head">
            <div>
              <div class="fw-semibold" style="font-size:0.92rem">{{ h.plantName }}</div>
              <div style="font-size:0.78rem;color:var(--text-muted)">{{ h.gardenName }}</div>
            </div>
            <span :class="`q-pill ${qualityClass[h.quality]}`">
              {{ qualityStars[h.quality] }} {{ qualityLabel[h.quality] }}
            </span>
          </div>
          <div class="harvest-mobile-body">
            <span>
              <i class="bi bi-calendar3 me-1" aria-hidden="true"></i>
              {{ h.harvestDate }}
            </span>
            <span v-if="h.quantity != null">
              <i class="bi bi-box me-1" aria-hidden="true"></i>
              {{ h.quantity }} {{ h.unit }}
            </span>
          </div>
          <div v-if="h.notes" class="harvest-mobile-notes">{{ h.notes }}</div>
          <div class="harvest-mobile-actions">
            <button class="btn btn-sm btn-outline-secondary" @click="openEdit(h)" :aria-label="`Editar ${h.plantName}`">
              <i class="bi bi-pencil me-1" aria-hidden="true"></i>Editar
            </button>
            <button class="btn btn-sm btn-outline-danger" @click="handleDelete(h.id)" :aria-label="`Eliminar ${h.plantName}`">
              <i class="bi bi-trash me-1" aria-hidden="true"></i>Eliminar
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <nav
        v-if="totalPages > 1"
        class="pagination-row"
        aria-label="Paginación de cosechas"
      >
        <button
          class="btn btn-sm btn-outline-secondary"
          :disabled="currentPage === 1"
          @click="currentPage--"
          aria-label="Página anterior"
        >
          <i class="bi bi-chevron-left" aria-hidden="true"></i>
        </button>
        <span class="page-indicator" aria-live="polite">
          Página {{ currentPage }} de {{ totalPages }}
        </span>
        <button
          class="btn btn-sm btn-outline-secondary"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
          aria-label="Página siguiente"
        >
          <i class="bi bi-chevron-right" aria-hidden="true"></i>
        </button>
      </nav>
    </template>

    <!-- ── Modal ── -->
    <BaseModal
      :show="showModal"
      size="lg"
      scrollable
      @close="tryCloseModal"
    >
      <template #header>
        <h2 class="modal-title">
          <span aria-hidden="true">{{ editingId ? '✏️' : '🧺' }}</span>
          {{ editingId ? 'Editar cosecha' : 'Nueva cosecha' }}
        </h2>
      </template>
      <form @submit.prevent="handleSave">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label" for="modal-crop">Cultivo <span class="text-danger" aria-hidden="true">*</span></label>
            <select id="modal-crop" v-model.number="form.cropId" class="form-select" required>
              <option v-for="c in crops" :key="c.id" :value="c.id">{{ c.plantName }}</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label" for="modal-date">Fecha <span class="text-danger" aria-hidden="true">*</span></label>
            <input id="modal-date" v-model="form.harvestDate" type="date" class="form-control" required />
          </div>
          <div class="col-md-4">
            <label class="form-label" for="modal-qty">Cantidad</label>
            <input id="modal-qty" v-model.number="form.quantity" type="number" step="0.1" min="0" class="form-control" />
          </div>
          <div class="col-md-4">
            <label class="form-label" for="modal-unit">Unidad</label>
            <select id="modal-unit" v-model="form.unit" class="form-select">
              <option value="kg">kg</option>
              <option value="g">g</option>
              <option value="unidades">unidades</option>
              <option value="litros">litros</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label" for="modal-quality">Calidad</label>
            <select id="modal-quality" v-model="form.quality" class="form-select">
              <option value="MALA">★ Mala</option>
              <option value="NORMAL">★★ Normal</option>
              <option value="BUENA">★★★ Buena</option>
              <option value="EXCELENTE">★★★★ Excelente</option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label" for="modal-notes">Notas</label>
            <input id="modal-notes" v-model="form.notes" class="form-control" placeholder="Observaciones opcionales..." />
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="tryCloseModal">Cancelar</button>
          <button type="submit" class="btn btn-success" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
            <i v-else class="bi bi-check-lg me-1" aria-hidden="true"></i>
            Guardar
          </button>
        </div>
      </form>
    </BaseModal>

  </div>
</template>

<style scoped>
.harvest-view { max-width: 1100px; }

/* Header */
.harvest-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.harvest-title {
  font-family: var(--font-display) !important;
  font-size: 1.6rem !important;
  font-weight: 800 !important;
  color: var(--text-primary) !important;
  margin-bottom: 4px;
}
.harvest-sub {
  font-size: 0.88rem;
  color: var(--text-muted);
  margin: 0;
}

/* Stats panel */
.stats-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  margin-bottom: 20px;
}
.stats-panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

/* Filter */
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-label {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
}
.filter-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.filter-pill {
  padding: 5px 14px;
  border-radius: var(--r-full);
  font-family: var(--font-display);
  font-size: 0.82rem;
  font-weight: 600;
  border: 1.5px solid var(--stone-300);
  background: var(--bg-elevated);
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--t-base);
}
.filter-pill:hover {
  border-color: var(--green-400);
  color: var(--green-600);
  background: var(--green-50);
}
.filter-pill.active {
  background: var(--green-600);
  border-color: var(--green-600);
  color: var(--text-on-accent);
}

/* Table wrap */
.harvest-table-wrap {
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: var(--r-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  margin-bottom: 16px;
}
.harvest-row { transition: background var(--t-fast); }
.harvest-qty-cell {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-family: var(--font-display);
}

/* Mobile card */
.harvest-mobile-card { padding: 0; }
.harvest-mobile-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 16px 10px;
  gap: 10px;
}
.harvest-mobile-body {
  display: flex;
  gap: 16px;
  padding: 0 16px 8px;
  font-size: 0.82rem;
  color: var(--text-muted);
}
.harvest-mobile-notes {
  padding: 0 16px 8px;
  font-size: 0.8rem;
  color: var(--text-light);
  font-style: italic;
}
.harvest-mobile-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 8px 16px 14px;
  border-top: 1px solid var(--border-subtle);
  margin-top: 4px;
}

/* Pagination */
.pagination-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
.page-indicator {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 600;
}
</style>
