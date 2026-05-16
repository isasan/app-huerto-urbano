<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { useGardenStore } from '@/stores/garden'
import { useToast } from '@/composables/useToast'
import { cropService } from '@/services/cropService.js'
import CropList from '@/components/crops/CropList.vue'
import CropForm from '@/components/crops/CropForm.vue'

const route = useRoute()
const gardenStore = useGardenStore()
const toast = useToast()
const gardenId = Number(route.params.id)

const selectedPlot = ref(null)
const crops = ref([])
const showCropForm = ref(false)
const editingCrop = ref(null)

const showPlotForm = ref(false)
const editingPlot = ref(null)
const plotForm = reactive({ name: '', sizeM2: null, soilType: '' })
const savingPlot = ref(false)
const savingCrop = ref(false)

let plotFormSnapshot = ''
let cropFormSnapshot = ''

onMounted(async () => {
  await gardenStore.fetchGardenById(gardenId)
  await gardenStore.fetchPlots(gardenId)
  if (gardenStore.plots.length > 0) {
    await selectPlot(gardenStore.plots[0])
  }
})

async function selectPlot(plot) {
  selectedPlot.value = plot
  crops.value = await cropService.getByPlot(plot.id)
}

function openCreatePlot() {
  editingPlot.value = null
  Object.assign(plotForm, { name: '', sizeM2: null, soilType: '' })
  plotFormSnapshot = JSON.stringify({ name: '', sizeM2: null, soilType: '' })
  showPlotForm.value = true
}

function openEditPlot(plot) {
  editingPlot.value = plot
  Object.assign(plotForm, { name: plot.name, sizeM2: plot.sizeM2, soilType: plot.soilType || '' })
  plotFormSnapshot = JSON.stringify({ name: plot.name, sizeM2: plot.sizeM2, soilType: plot.soilType || '' })
  showPlotForm.value = true
}

function tryClosePlotForm() {
  const current = JSON.stringify({ ...plotForm })
  if (current !== plotFormSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showPlotForm.value = false
}

async function handlePlotSave() {
  savingPlot.value = true
  try {
    if (editingPlot.value) {
      const updated = await gardenStore.updatePlot(gardenId, editingPlot.value.id, { ...plotForm })
      if (selectedPlot.value?.id === updated.id) selectedPlot.value = updated
      toast.success('Parcela actualizada')
    } else {
      const created = await gardenStore.createPlot(gardenId, { ...plotForm })
      if (!selectedPlot.value) await selectPlot(created)
      toast.success('Parcela creada')
    }
    showPlotForm.value = false
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar la parcela')
  } finally {
    savingPlot.value = false
  }
}

async function handleDeletePlot(plot) {
  if (!confirm(`¿Eliminar la parcela "${plot.name}" y todos sus cultivos?`)) return
  try {
    await gardenStore.deletePlot(gardenId, plot.id)
    if (selectedPlot.value?.id === plot.id) {
      selectedPlot.value = null
      crops.value = []
      if (gardenStore.plots.length > 0) await selectPlot(gardenStore.plots[0])
    }
    toast.success('Parcela eliminada')
  } catch {
    toast.error('Error al eliminar la parcela')
  }
}

function openCreateCrop() {
  editingCrop.value = null
  cropFormSnapshot = JSON.stringify({})
  showCropForm.value = true
}

function handleEditCrop(crop) {
  editingCrop.value = crop
  cropFormSnapshot = JSON.stringify(crop)
  showCropForm.value = true
}

function tryCloseCropForm() {
  if (cropFormSnapshot !== JSON.stringify({})) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showCropForm.value = false
  editingCrop.value = null
}

async function handleCropSubmit(data) {
  savingCrop.value = true
  try {
    if (editingCrop.value) {
      await cropService.update(selectedPlot.value.id, editingCrop.value.id, data)
      toast.success('Cultivo actualizado')
    } else {
      await cropService.create(selectedPlot.value.id, data)
      toast.success('Cultivo añadido')
    }
    crops.value = await cropService.getByPlot(selectedPlot.value.id)
    showCropForm.value = false
    editingCrop.value = null
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar el cultivo')
  } finally {
    savingCrop.value = false
  }
}

async function handleDeleteCrop(cropId) {
  if (!confirm('¿Eliminar este cultivo?')) return
  try {
    await cropService.delete(selectedPlot.value.id, cropId)
    crops.value = crops.value.filter(c => c.id !== cropId)
    toast.success('Cultivo eliminado')
  } catch {
    toast.error('Error al eliminar el cultivo')
  }
}
</script>

<template>
  <div>
    <div v-if="gardenStore.loading" class="text-center py-5">
      <div class="spinner-border text-success"></div>
    </div>

    <div v-else-if="gardenStore.selectedGarden">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-1">
        <ol class="breadcrumb mb-0">
          <li class="breadcrumb-item">
            <RouterLink to="/gardens" class="text-success text-decoration-none">
              <i class="bi bi-tree me-1"></i>Mis Huertos
            </RouterLink>
          </li>
          <li class="breadcrumb-item active" aria-current="page">
            {{ gardenStore.selectedGarden.name }}
          </li>
        </ol>
      </nav>

      <!-- Cabecera -->
      <div class="d-flex align-items-center gap-2 mb-4">
        <h4 class="mb-0">
          <i class="bi bi-tree-fill text-success me-2"></i>{{ gardenStore.selectedGarden.name }}
        </h4>
        <span class="text-muted small ms-2">
          <i class="bi bi-geo-alt me-1"></i>{{ gardenStore.selectedGarden.location || 'Sin ubicación' }}
          &nbsp;·&nbsp;
          <i class="bi bi-rulers me-1"></i>{{ gardenStore.selectedGarden.sizeM2 || 0 }} m²
        </span>
      </div>

      <div class="row g-4">
        <!-- Parcelas -->
        <div class="col-md-3">
          <div class="card border-0 shadow-sm">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h6 class="card-title mb-0">Parcelas</h6>
                <button class="btn btn-sm btn-outline-success" @click="openCreatePlot">
                  <i class="bi bi-plus-lg"></i>
                </button>
              </div>

              <div v-if="gardenStore.plots.length === 0" class="text-center text-muted py-3">
                <div class="mb-1">🟫</div>
                <p class="small mb-1 fw-semibold">Sin parcelas</p>
                <p class="small mb-2">Añade una parcela para organizar tus cultivos</p>
                <button class="btn btn-sm btn-outline-success" @click="openCreatePlot">
                  <i class="bi bi-plus-lg me-1"></i>Nueva parcela
                </button>
              </div>

              <div class="list-group list-group-flush">
                <div
                  v-for="plot in gardenStore.plots"
                  :key="plot.id"
                  :class="['list-group-item list-group-item-action border-0 rounded px-2',
                           selectedPlot?.id === plot.id ? 'active' : '']"
                  style="cursor: pointer;"
                  role="button"
                  tabindex="0"
                  @click="selectPlot(plot)"
                  @keydown.enter="selectPlot(plot)"
                >
                  <div class="d-flex justify-content-between align-items-center">
                    <span>
                      <i class="bi bi-grid me-1"></i>{{ plot.name }}
                      <span class="text-muted small ms-1" :class="selectedPlot?.id === plot.id ? 'text-white-50' : ''">({{ plot.sizeM2 }}m²)</span>
                    </span>
                    <div class="dropdown" @click.stop>
                      <button class="btn btn-sm border-0 p-0 ms-1" data-bs-toggle="dropdown">
                        <i class="bi bi-three-dots-vertical"></i>
                      </button>
                      <ul class="dropdown-menu dropdown-menu-end">
                        <li>
                          <button class="dropdown-item" @click="openEditPlot(plot)">
                            <i class="bi bi-pencil me-2"></i>Editar
                          </button>
                        </li>
                        <li>
                          <button class="dropdown-item text-danger" @click="handleDeletePlot(plot)">
                            <i class="bi bi-trash me-2"></i>Eliminar
                          </button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Cultivos -->
        <div class="col-md-9">
          <div class="card border-0 shadow-sm">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="card-title mb-0">
                  Cultivos en {{ selectedPlot?.name || 'parcela' }}
                </h6>
                <button
                  v-if="selectedPlot"
                  class="btn btn-sm btn-outline-success"
                  @click="showCropForm ? tryCloseCropForm() : openCreateCrop()"
                >
                  <i class="bi bi-plus-lg me-1"></i>Añadir cultivo
                </button>
              </div>

              <div v-if="!selectedPlot" class="text-center text-muted py-4">
                <div class="mb-2">🟫</div>
                Selecciona una parcela para ver sus cultivos
              </div>

              <template v-else>
                <div v-if="showCropForm" class="border rounded p-3 mb-3">
                  <CropForm
                    :initial="editingCrop || {}"
                    :plot-id="selectedPlot.id"
                    :saving="savingCrop"
                    @submit="handleCropSubmit"
                    @cancel="tryCloseCropForm"
                  />
                </div>

                <CropList
                  :crops="crops"
                  @edit="handleEditCrop"
                  @delete="handleDeleteCrop"
                />

                <div v-if="crops.length === 0 && !showCropForm" class="text-center text-muted py-4">
                  <div class="mb-2 fs-2">🌿</div>
                  <p class="fw-semibold mb-1">Esta parcela está vacía</p>
                  <p class="small mb-2">Añade tu primer cultivo para empezar a hacer seguimiento</p>
                  <button class="btn btn-sm btn-outline-success" @click="openCreateCrop">
                    <i class="bi bi-plus-lg me-1"></i>Añadir cultivo
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal crear/editar parcela -->
    <div v-if="showPlotForm" class="modal d-block" style="background: rgba(0,0,0,0.4);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editingPlot ? 'Editar parcela' : 'Nueva parcela' }}</h5>
            <button type="button" class="btn-close" @click="tryClosePlotForm"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handlePlotSave">
              <div class="mb-3">
                <label for="plot-name" class="form-label">Nombre <span class="text-danger">*</span></label>
                <input id="plot-name" v-model="plotForm.name" type="text" class="form-control" required placeholder="Parcela A" />
              </div>
              <div class="mb-3">
                <label for="plot-size" class="form-label">Tamaño (m²)</label>
                <input id="plot-size" v-model.number="plotForm.sizeM2" type="number" step="0.1" min="0" class="form-control" />
              </div>
              <div class="mb-3">
                <label for="plot-soil" class="form-label">Tipo de suelo</label>
                <input id="plot-soil" v-model="plotForm.soilType" type="text" class="form-control" placeholder="Arcilloso, arenoso, franco..." />
              </div>
              <div class="d-flex gap-2 justify-content-end">
                <button type="button" class="btn btn-outline-secondary" @click="tryClosePlotForm">Cancelar</button>
                <button type="submit" class="btn btn-success" :disabled="savingPlot">
                  <span v-if="savingPlot" class="spinner-border spinner-border-sm me-1"></span>
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
