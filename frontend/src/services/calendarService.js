import api from './api.js'

export const calendarService = {
  async getPlants(hemisphere, month) {
    const params = { hemisphere }
    if (month != null) params.month = month
    const { data } = await api.get('/calendar', { params })
    return data
  }
}
