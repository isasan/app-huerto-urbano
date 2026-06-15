import { ref, computed, readonly } from 'vue'

const STORAGE_KEY = 'huerto-theme'
const media = window.matchMedia('(prefers-color-scheme: dark)')

function resolveInitial() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'light' || stored === 'dark') return stored
  return media.matches ? 'dark' : 'light'
}

const theme = ref(resolveInitial())

function apply(t) {
  document.documentElement.dataset.theme = t
  document.documentElement.setAttribute('data-bs-theme', t)
}

function toggle() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem(STORAGE_KEY, theme.value)
  apply(theme.value)
}

// Seguir el tema del sistema solo mientras el usuario no haya elegido uno
media.addEventListener('change', e => {
  if (localStorage.getItem(STORAGE_KEY)) return
  theme.value = e.matches ? 'dark' : 'light'
  apply(theme.value)
})

apply(theme.value)

export function useTheme() {
  return {
    theme: readonly(theme),
    isDark: computed(() => theme.value === 'dark'),
    toggle
  }
}
