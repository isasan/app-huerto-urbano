import api from './api.js'

export const gardenService = {
  async getAll() {
    const { data } = await api.get('/gardens')
    return data
  },

  async getById(id) {
    const { data } = await api.get(`/gardens/${id}`)
    return data
  },

  async create(garden) {
    const { data } = await api.post('/gardens', garden)
    return data
  },

  async update(id, garden) {
    const { data } = await api.put(`/gardens/${id}`, garden)
    return data
  },

  async delete(id) {
    await api.delete(`/gardens/${id}`)
  },

  async getPlots(gardenId) {
    const { data } = await api.get(`/gardens/${gardenId}/plots`)
    return data
  },

  async createPlot(gardenId, plot) {
    const { data } = await api.post(`/gardens/${gardenId}/plots`, plot)
    return data
  },

  async updatePlot(gardenId, plotId, plot) {
    const { data } = await api.put(`/gardens/${gardenId}/plots/${plotId}`, plot)
    return data
  },

  async deletePlot(gardenId, plotId) {
    await api.delete(`/gardens/${gardenId}/plots/${plotId}`)
  }
}
