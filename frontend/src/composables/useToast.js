import { reactive } from 'vue'

const toasts = reactive([])
let nextId = 0

function add(message, type = 'info', duration = 4000) {
  const id = ++nextId
  toasts.push({ id, message, type })
  if (duration > 0) setTimeout(() => remove(id), duration)
}

function remove(id) {
  const i = toasts.findIndex(t => t.id === id)
  if (i !== -1) toasts.splice(i, 1)
}

export function useToast() {
  return {
    toasts,
    success: (msg, duration) => add(msg, 'success', duration),
    error:   (msg, duration) => add(msg, 'error',   duration),
    warning: (msg, duration) => add(msg, 'warning', duration),
    info:    (msg, duration) => add(msg, 'info',    duration),
    remove
  }
}
