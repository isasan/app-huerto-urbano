import api from './api.js'

export const authService = {
  async login(credentials) {
    const { data } = await api.post('/auth/login', credentials)
    return data
  },

  async register(userData) {
    const { data } = await api.post('/auth/register', userData)
    return data
  }
}
