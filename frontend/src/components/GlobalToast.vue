<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

const typeConfig = {
  success: { icon: 'bi-check-circle-fill', bg: 'var(--green-600)',  label: 'Éxito' },
  error:   { icon: 'bi-x-circle-fill',     bg: 'var(--red-600)',    label: 'Error' },
  warning: { icon: 'bi-exclamation-triangle-fill', bg: 'var(--amber-600)', label: 'Aviso' },
  info:    { icon: 'bi-info-circle-fill',   bg: 'var(--blue-600)',   label: 'Info' },
}
</script>

<template>
  <div
    class="toast-stack"
    aria-live="assertive"
    aria-atomic="false"
    role="status"
  >
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast-pill"
        :style="`--toast-bg: ${typeConfig[t.type]?.bg ?? 'var(--green-600)'}`"
        role="alert"
        :aria-label="`${typeConfig[t.type]?.label ?? ''}: ${t.message}`"
      >
        <i
          :class="`bi ${typeConfig[t.type]?.icon ?? 'bi-info-circle-fill'}`"
          class="toast-icon"
          aria-hidden="true"
        ></i>
        <span class="toast-msg">{{ t.message }}</span>
        <button
          class="toast-close"
          @click="remove(t.id)"
          aria-label="Cerrar notificación"
        >
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-stack {
  position: fixed;
  bottom: 24px;
  right: 20px;
  z-index: 1100;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--toast-bg);
  color: white;
  border-radius: var(--r-full);
  padding: 10px 16px 10px 14px;
  box-shadow: 0 6px 24px rgba(0,0,0,0.18), 0 2px 8px rgba(0,0,0,0.1);
  pointer-events: all;
  max-width: 360px;
  font-family: var(--font-body);
  font-size: 0.88rem;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.toast-icon {
  font-size: 1rem;
  flex-shrink: 0;
  opacity: 0.9;
}

.toast-msg {
  flex: 1;
  line-height: 1.4;
}

.toast-close {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  font-size: 0.7rem;
  transition: background var(--t-fast);
}
.toast-close:hover {
  background: rgba(255,255,255,0.35);
}

/* TransitionGroup */
.toast-enter-active {
  animation: toastSlideIn 0.32s cubic-bezier(0.34,1.56,0.64,1) both;
}
.toast-leave-active {
  animation: toastSlideOut 0.22s ease both;
}

@keyframes toastSlideIn {
  from {
    opacity: 0;
    transform: translateX(60px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}
@keyframes toastSlideOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.85) translateX(20px);
  }
}
</style>
