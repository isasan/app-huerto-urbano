<script setup>
import { computed, onMounted, ref } from 'vue'
import { useGardenStore } from '@/stores/garden'
import { useToast } from '@/composables/useToast'
import GardenCard from '@/components/gardens/GardenCard.vue'
import GardenForm from '@/components/gardens/GardenForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'

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
  <div class="gardens-view">
    <div class="page-header animate-up">
      <div>
        <h1 class="page-title"><span aria-hidden="true">🌳</span> Mis Huertos</h1>
        <p class="page-sub">Organiza tus espacios de cultivo</p>
      </div>
      <button class="btn btn-success" @click="openCreate">
        <i class="bi bi-plus-lg me-1"></i>Nuevo huerto
      </button>
    </div>

    <!-- Modal crear/editar huerto -->
    <BaseModal
      :show="showForm"
      :title="editingGarden ? 'Editar huerto' : 'Nuevo huerto'"
      :icon="editingGarden ? 'bi-pencil' : 'bi-tree'"
      @close="tryCloseForm(null)"
    >
      <GardenForm
        v-if="showForm"
        :initial="editingGarden || {}"
        :saving="saving"
        @submit="handleSave"
        @cancel="tryCloseForm(null)"
      />
    </BaseModal>

    <div v-if="gardenStore.loading" class="row g-3" aria-busy="true" aria-label="Cargando huertos">
      <div v-for="i in 3" :key="i" class="col-12 col-md-6 col-xl-4">
        <div class="garden-skel panel-card">
          <div class="skel skel-title"></div>
          <div class="skel skel-line"></div>
          <div class="skel skel-line short"></div>
          <div class="skel skel-btn"></div>
        </div>
      </div>
    </div>

    <div
      v-else-if="sortedGardens.length === 0"
      class="empty-state panel-card"
      style="min-height: 260px"
      role="status"
    >
      <span class="empty-icon" aria-hidden="true">🌱</span>
      <p class="mb-1 fw-semibold" style="font-size: 0.95rem">Aún no tienes huertos</p>
      <p class="mb-3" style="font-size: 0.85rem; color: var(--text-light)">
        Crea tu primer huerto para empezar a gestionar tus cultivos
      </p>
      <button class="btn btn-success" @click="openCreate">
        <i class="bi bi-plus-lg me-1"></i>Crear mi primer huerto
      </button>
    </div>

    <div v-else class="row g-3 stagger">
      <div v-for="garden in sortedGardens" :key="garden.id" class="col-12 col-md-6 col-xl-4">
        <GardenCard :garden="garden" @edit="handleEdit" @delete="handleDelete" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
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
.garden-skel { padding: 20px; gap: 12px; }
.skel-title { height: 20px; width: 55%; }
.skel-line  { height: 12px; width: 90%; }
.skel-line.short { width: 65%; }
.skel-btn   { height: 32px; width: 100%; margin-top: 6px; }
</style>
