import api from './api.js'

export const calendarPlantService = {
  async getAll() {
    const { data } = await api.get('/admin/calendar-plants')
    return data
  },

  async create(payload) {
    const { data } = await api.post('/admin/calendar-plants', payload)
    return data
  },

  async update(id, payload) {
    const { data } = await api.put(`/admin/calendar-plants/${id}`, payload)
    return data
  },

  async delete(id) {
    await api.delete(`/admin/calendar-plants/${id}`)
  }
}
