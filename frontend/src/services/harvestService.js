import api from './api.js'

export const harvestService = {
  async getAll(params = {}) {
    const { data } = await api.get('/harvests', { params })
    return data
  },

  async create(harvest) {
    const { data } = await api.post('/harvests', harvest)
    return data
  },

  async update(id, harvest) {
    const { data } = await api.put(`/harvests/${id}`, harvest)
    return data
  },

  async delete(id) {
    await api.delete(`/harvests/${id}`)
  }
}
