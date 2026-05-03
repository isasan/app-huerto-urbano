import api from './api.js'

export const taskService = {
  async getAll(params = {}) {
    const { data } = await api.get('/tasks', { params })
    return data
  },

  async create(task) {
    const { data } = await api.post('/tasks', task)
    return data
  },

  async update(id, task) {
    const { data } = await api.put(`/tasks/${id}`, task)
    return data
  },

  async toggleComplete(id) {
    const { data } = await api.patch(`/tasks/${id}/complete`)
    return data
  },

  async delete(id) {
    await api.delete(`/tasks/${id}`)
  }
}
