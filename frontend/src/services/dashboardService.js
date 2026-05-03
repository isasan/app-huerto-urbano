import api from './api.js'

export const dashboardService = {
  async get() {
    const { data } = await api.get('/dashboard')
    return data
  }
}
