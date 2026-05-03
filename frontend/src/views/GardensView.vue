<script setup>
import { computed, onMounted, ref } from 'vue'
import { useGardenStore } from '@/stores/garden'
import { useToast } from '@/composables/useToast'
import GardenCard from '@/components/gardens/GardenCard.vue'
import GardenForm from '@/components/gardens/GardenForm.vue'

const gardenStore = useGardenStore()
const toast = useToast()

const showForm = ref(false)
const editingGarden = ref(null)
const saving = ref(false)

let initialFormSnapshot = ''

const sortedGardens = computed(() =>
  [...gardenStore.gardens].sort((a, b) => a.name.localeCompare(b.name, 'es'))
)

onMounted(() => gardenStore.fetchGardens())

function openCreate() {
  editingGarden.value = null
  initialFormSnapshot = JSON.stringify({})
  showForm.value = true
}

function handleEdit(garden) {
  editingGarden.value = garden
  initialFormSnapshot = JSON.stringify({
    name: garden.name, description: garden.description,
    location: garden.location, sizeM2: garden.sizeM2
  })
  showForm.value = true
}

function tryCloseForm(currentData) {
  if (currentData && JSON.stringify(currentData) !== initialFormSnapshot) {
    if (!confirm('¿Cerrar sin guardar los cambios?')) return
  }
  showForm.value = false
  editingGarden.value = null
}

async function handleSave(data) {
  saving.value = true
  try {
    if (editingGarden.value) {
      await gardenStore.updateGarden(editingGarden.value.id, data)
      toast.success('Huerto actualizado correctamente')
    } else {
      await gardenStore.createGarden(data)
      toast.success('Huerto creado correctamente')
    }
    showForm.value = false
    editingGarden.value = null
  } catch (e) {
    toast.error(e.response?.data?.message || 'Error al guardar el huerto')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  if (!confirm('¿Eliminar este huerto y todos sus datos?')) return
  try {
    await gardenStore.deleteGarden(id)
    toast.success('Huerto eliminado')
  } catch {
    toast.error('Error al eliminar el huerto')
  }
}
</script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="mb-0"><i class="bi bi-tree me-2 text-success"></i>Mis Huertos</h4>
      <button class="btn btn-success" @click="openCreate">
        <i class="bi bi-plus-lg me-1"></i>Nuevo huerto
      </button>
    </div>

    <!-- Modal crear/editar huerto -->
    <div v-if="showForm" class="modal d-block" style="background: rgba(0,0,0,0.4);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editingGarden ? 'Editar huerto' : 'Nuevo huerto' }}</h5>
            <button type="button" class="btn-close" @click="tryCloseForm(null)"></button>
          </div>
          <div class="modal-body">
            <GardenForm
              :initial="editingGarden || {}"
              :saving="saving"
              @submit="handleSave"
              @cancel="tryCloseForm(null)"
            />
          </div>
        </div>
      </div>
    </div>

    <div v-if="gardenStore.loading" class="text-center py-5">
      <div class="spinner-border text-success"></div>
    </div>

    <div v-else-if="sortedGardens.length === 0" class="text-center py-5 text-muted">
      <div class="fs-1 mb-3">🌱</div>
      <h5 class="fw-semibold">Aún no tienes huertos</h5>
      <p class="mb-3">Crea tu primer huerto para empezar a gestionar tus cultivos</p>
      <button class="btn btn-success" @click="openCreate">
        <i class="bi bi-plus-lg me-1"></i>Crear mi primer huerto
      </button>
    </div>

    <div v-else class="row g-3">
      <div v-for="garden in sortedGardens" :key="garden.id" class="col-12 col-md-6 col-xl-4">
        <GardenCard :garden="garden" @edit="handleEdit" @delete="handleDelete" />
      </div>
    </div>
  </div>
</template>
