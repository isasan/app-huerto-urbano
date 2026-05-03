import api from './api.js'

export const userService = {
  async getProfile() {
    const { data } = await api.get('/users/me')
    return data
  },

  async updateProfile(payload) {
    const { data } = await api.put('/users/me', payload)
    return data
  },

  async changePassword(payload) {
    await api.put('/users/me/password', payload)
  }
}
