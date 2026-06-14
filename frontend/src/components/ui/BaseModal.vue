<script setup>
import { watch, onUnmounted, ref, nextTick } from 'vue'

const props = defineProps({
  show: { type: Boolean, required: true },
  title: { type: String, default: '' },
  icon: { type: String, default: '' },
  size: {
    type: String,
    default: 'md',
    validator: v => ['sm', 'md', 'lg'].includes(v)
  },
  variant: {
    type: String,
    default: 'default',
    validator: v => ['default', 'success', 'danger'].includes(v)
  },
  scrollable: { type: Boolean, default: false },
  centered: { type: Boolean, default: true }
})

const emit = defineEmits(['close'])

const contentEl = ref(null)

function onKeydown(e) {
  if (e.key === 'Escape') emit('close')
}

watch(
  () => props.show,
  async open => {
    if (open) {
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', onKeydown)
      await nextTick()
      contentEl.value?.focus()
    } else {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', onKeydown)
    }
  }
)

onUnmounted(() => {
  document.body.style.overflow = ''
  document.removeEventListener('keydown', onKeydown)
})

const sizeClass = {
  sm: 'modal-sm',
  md: '',
  lg: 'modal-lg'
}
</script>

<template>
  <Teleport to="body">
    <Transition name="hv-modal">
      <div
        v-if="show"
        class="hv-modal-backdrop"
        role="dialog"
        aria-modal="true"
        :aria-label="title || undefined"
        @click.self="emit('close')"
      >
        <div
          class="modal-dialog"
          :class="[
            sizeClass[size],
            { 'modal-dialog-centered': centered, 'modal-dialog-scrollable': scrollable }
          ]"
        >
          <div ref="contentEl" class="modal-content" tabindex="-1">
            <div class="modal-header" :class="`hv-modal-header-${variant}`">
              <slot name="header">
                <h2 class="modal-title">
                  <i v-if="icon" class="bi me-2" :class="icon" aria-hidden="true"></i>{{ title }}
                </h2>
              </slot>
              <button
                type="button"
                class="btn-close"
                :class="{ 'btn-close-white': variant !== 'default' }"
                aria-label="Cerrar"
                @click="emit('close')"
              ></button>
            </div>
            <div class="modal-body">
              <slot></slot>
            </div>
            <div v-if="$slots.footer" class="modal-footer">
              <slot name="footer"></slot>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.hv-modal-backdrop {
  /* Bootstrap define estos vars en .modal; sin ellos .modal-dialog pierde su max-width */
  --bs-modal-width: 500px;
  --bs-modal-margin: 1.75rem;
  position: fixed;
  inset: 0;
  z-index: 1055;
  background: var(--overlay-backdrop);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow-y: auto;
  padding: 1rem;
}
.hv-modal-backdrop .modal-dialog {
  width: 100%;
  margin: 0 auto;
}
.hv-modal-backdrop .modal-content:focus {
  outline: none;
}
.hv-modal-header-danger,
.hv-modal-header-success {
  border-top-left-radius: var(--r-lg);
  border-top-right-radius: var(--r-lg);
}
.hv-modal-header-danger { background: var(--red-600); }
.hv-modal-header-success { background: var(--green-600); }
.hv-modal-header-danger .modal-title,
.hv-modal-header-success .modal-title {
  color: var(--text-on-accent) !important;
}

/* Transición: overlay fade + dialog pop */
.hv-modal-enter-active,
.hv-modal-leave-active {
  transition: opacity var(--t-base);
}
.hv-modal-enter-active .modal-dialog,
.hv-modal-leave-active .modal-dialog {
  transition: transform var(--t-base), opacity var(--t-base);
}
.hv-modal-enter-from,
.hv-modal-leave-to {
  opacity: 0;
}
.hv-modal-enter-from .modal-dialog,
.hv-modal-leave-to .modal-dialog {
  transform: scale(0.94) translateY(12px);
  opacity: 0;
}
</style>
