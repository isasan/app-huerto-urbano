<script setup>
import { ref, watch } from 'vue'
import PlantCard from './PlantCard.vue'
import PlantDetailModal from './PlantDetailModal.vue'
import { calendarService } from '@/services/calendarService.js'
import { plantDetailService } from '@/services/plantDetailService.js'

const props = defineProps({
  hemisphere: { type: String, default: 'NORTE' },
  month: { type: Number, required: true }
})

const plants = ref([])
const detailsMap = ref({})   // { "Tomate": PlantDetailResponse, ... }
const loading = ref(false)
const error = ref(null)

const showModal = ref(false)
const selectedPlant = ref(null)   // { name, emoji } from PlantCard
const selectedDetail = ref(null)  // PlantDetailResponse | null

async function loadPlants() {
  loading.value = true
  error.value = null
  try {
    const [calendarData, details] = await Promise.all([
      calendarService.getPlants(props.hemisphere, props.month),
      plantDetailService.getAll()
    ])
    plants.value = calendarData
    detailsMap.value = Object.fromEntries(details.map(d => [d.plantName, d]))
  } catch {
    error.value = 'No se pudo cargar el calendario. Inténtalo de nuevo.'
  } finally {
    loading.value = false
  }
}

watch([() => props.hemisphere, () => props.month], loadPlants, { immediate: true })

function openDetail(plant) {
  selectedPlant.value = plant
  // Static plants use detailsMap; DB plants carry detail fields inline
  const fromMap = detailsMap.value[plant.name]
  if (fromMap) {
    selectedDetail.value = fromMap
  } else if (plant.latinName || plant.wateringFrequency || plant.pests || plant.wikipediaUrl) {
    selectedDetail.value = {
      plantName: plant.name,
      latinName: plant.latinName ?? null,
      wateringFrequency: plant.wateringFrequency ?? null,
      pests: plant.pests ?? null,
      wikipediaUrl: plant.wikipediaUrl ?? null
    }
  } else {
    selectedDetail.value = null
  }
  showModal.value = true
}
</script>

<template>
  <div>
    <div v-if="loading" class="row g-3" aria-busy="true" aria-label="Cargando plantas">
      <div v-for="i in 8" :key="i" class="col-12 col-sm-6 col-md-4 col-xl-3">
        <div class="plant-skel panel-card">
          <div class="skel skel-emoji"></div>
          <div class="skel skel-name"></div>
          <div class="skel skel-info"></div>
        </div>
      </div>
    </div>

    <div v-else-if="error" class="empty-state panel-card" style="min-height: 200px" role="alert">
      <i class="bi bi-cloud-slash empty-icon" aria-hidden="true"></i>
      <p class="mb-3">{{ error }}</p>
      <button class="btn btn-outline-success btn-sm" @click="loadPlants">
        <i class="bi bi-arrow-clockwise me-1" aria-hidden="true"></i>Reintentar
      </button>
    </div>

    <div v-else-if="plants.length === 0" class="empty-state py-5" role="status">
      <span class="empty-icon" aria-hidden="true">🌾</span>
      <p class="text-muted mb-1">No hay actividad recomendada para este mes y hemisferio.</p>
      <p class="text-muted small mb-0">Prueba con otro mes o cambia el hemisferio.</p>
    </div>

    <div v-else class="row g-3 stagger">
      <div
        v-for="plant in plants"
        :key="plant.name"
        class="col-12 col-sm-6 col-md-4 col-xl-3"
      >
        <PlantCard :plant="plant" @click-detail="openDetail" />
      </div>
    </div>

    <PlantDetailModal
      :show="showModal"
      :plant="selectedPlant"
      :detail="selectedDetail"
      @close="showModal = false"
    />
  </div>
</template>

<style scoped>
.plant-skel {
  padding: 20px;
  gap: 10px;
  align-items: center;
}
.skel-emoji { height: 48px; width: 48px; border-radius: 50%; }
.skel-name  { height: 16px; width: 60%; }
.skel-info  { height: 12px; width: 80%; }
</style>
