import api from './api.js'

export const adminService = {
  async getStats() {
    const { data } = await api.get('/admin/stats')
    return data
  },

  async getAllUsers() {
    const { data } = await api.get('/admin/users')
    return data
  },

  async getUserById(id) {
    const { data } = await api.get(`/admin/users/${id}`)
    return data
  },

  async changeRole(id, role) {
    const { data } = await api.put(`/admin/users/${id}/role`, { role })
    return data
  },

  async deleteUser(id) {
    await api.delete(`/admin/users/${id}`)
  },

  async createUser(payload) {
    const { data } = await api.post('/admin/users', payload)
    return data
  }
}
