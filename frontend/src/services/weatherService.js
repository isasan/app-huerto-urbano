import api from './api.js'

export const weatherService = {
  async getWeather(city) {
    const { data } = await api.get('/weather', { params: { city } })
    return data
  },

  async getMyWeather() {
    const { data } = await api.get('/weather')
    return data
  }
}
