import { defineStore } from 'pinia'
import { ref } from 'vue'
import { weatherService } from '@/services/weatherService.js'

export const useWeatherStore = defineStore('weather', () => {
  const weather = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const lastUpdated = ref(null)

  async function fetchWeather(city) {
    loading.value = true
    error.value = null
    try {
      weather.value = city
        ? await weatherService.getWeather(city)
        : await weatherService.getMyWeather()
      lastUpdated.value = new Date()
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data?.error
      if (e.response?.status === 400 && !city) {
        // El usuario no tiene ciudad configurada: estado vacío, no error
        error.value = null
      } else {
        error.value = msg || (city ? `No se pudo obtener el clima para "${city}"` : 'No se pudo obtener el clima')
      }
      weather.value = null
    } finally {
      loading.value = false
    }
  }

  return { weather, loading, error, lastUpdated, fetchWeather }
})
