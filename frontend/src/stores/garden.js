import { defineStore } from 'pinia'
import { ref } from 'vue'
import { gardenService } from '@/services/gardenService.js'

export const useGardenStore = defineStore('garden', () => {
  const gardens = ref([])
  const selectedGarden = ref(null)
  const plots = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchGardens() {
    loading.value = true
    error.value = null
    try {
      gardens.value = await gardenService.getAll()
    } catch (e) {
      error.value = e.response?.data?.message || 'Error al cargar huertos'
    } finally {
      loading.value = false
    }
  }

  async function fetchGardenById(id) {
    loading.value = true
    try {
      selectedGarden.value = await gardenService.getById(id)
    } catch (e) {
      error.value = e.response?.data?.message || 'Error al cargar el huerto'
    } finally {
      loading.value = false
    }
  }

  async function createGarden(garden) {
    const created = await gardenService.create(garden)
    gardens.value.push(created)
    return created
  }

  async function updateGarden(id, garden) {
    const updated = await gardenService.update(id, garden)
    const index = gardens.value.findIndex(g => g.id === id)
    if (index !== -1) gardens.value[index] = updated
    if (selectedGarden.value?.id === id) selectedGarden.value = updated
    return updated
  }

  async function deleteGarden(id) {
    await gardenService.delete(id)
    gardens.value = gardens.value.filter(g => g.id !== id)
  }

  async function fetchPlots(gardenId) {
    try {
      plots.value = await gardenService.getPlots(gardenId)
    } catch (e) {
      error.value = e.response?.data?.message || 'Error al cargar parcelas'
    }
  }

  async function createPlot(gardenId, data) {
    const created = await gardenService.createPlot(gardenId, data)
    plots.value.push(created)
    if (selectedGarden.value?.id === gardenId) selectedGarden.value.plotCount++
    const garden = gardens.value.find(g => g.id === gardenId)
    if (garden) garden.plotCount++
    return created
  }

  async function updatePlot(gardenId, plotId, data) {
    const updated = await gardenService.updatePlot(gardenId, plotId, data)
    const index = plots.value.findIndex(p => p.id === plotId)
    if (index !== -1) plots.value[index] = updated
    return updated
  }

  async function deletePlot(gardenId, plotId) {
    await gardenService.deletePlot(gardenId, plotId)
    plots.value = plots.value.filter(p => p.id !== plotId)
    if (selectedGarden.value?.id === gardenId) selectedGarden.value.plotCount--
    const garden = gardens.value.find(g => g.id === gardenId)
    if (garden) garden.plotCount--
  }

  return {
    gardens, selectedGarden, plots, loading, error,
    fetchGardens, fetchGardenById, createGarden, updateGarden, deleteGarden,
    fetchPlots, createPlot, updatePlot, deletePlot
  }
})
