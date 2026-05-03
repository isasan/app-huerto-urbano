import api from './api.js'

export const cropService = {
  async getAll() {
    const { data } = await api.get('/crops/user')
    return data
  },

  async getByPlot(plotId) {
    const { data } = await api.get(`/plots/${plotId}/crops`)
    return data
  },

  async create(plotId, crop) {
    const { data } = await api.post(`/plots/${plotId}/crops`, crop)
    return data
  },

  async update(plotId, cropId, crop) {
    const { data } = await api.put(`/plots/${plotId}/crops/${cropId}`, crop)
    return data
  },

  async delete(plotId, cropId) {
    await api.delete(`/plots/${plotId}/crops/${cropId}`)
  }
}
