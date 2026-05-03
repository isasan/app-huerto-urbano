import api from '@/services/api'

export const plantDetailService = {
  async getAll() {
    const { data } = await api.get('/plant-details')
    return data
  },

  async getByName(plantName) {
    const { data } = await api.get(`/plant-details/${encodeURIComponent(plantName)}`)
    return data
  },

  async update(id, payload) {
    const { data } = await api.put(`/admin/plant-details/${id}`, payload)
    return data
  },

  async create(payload) {
    const { data } = await api.post('/admin/plant-details', payload)
    return data
  },

  async delete(id) {
    await api.delete(`/admin/plant-details/${id}`)
  }
}
