<script setup>
import { reactive } from 'vue'

const props = defineProps({
  initial: { type: Object, default: () => ({}) },
  saving:  { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  name: props.initial.name || '',
  description: props.initial.description || '',
  location: props.initial.location || '',
  sizeM2: props.initial.sizeM2 || null
})

const touched = reactive({ name: false })

function submit() {
  touched.name = true
  if (!form.name.trim()) return
  emit('submit', { ...form })
}
</script>

<template>
  <form @submit.prevent="submit" novalidate>
    <div class="mb-3">
      <label class="form-label" for="garden-name">Nombre <span class="text-danger" aria-hidden="true">*</span></label>
      <input
        id="garden-name"
        v-model="form.name"
        type="text"
        class="form-control"
        :class="{ 'is-invalid': touched.name && !form.name.trim() }"
        required
        placeholder="Mi huerto"
        @blur="touched.name = true"
      />
      <div class="invalid-feedback">El nombre es obligatorio</div>
    </div>
    <div class="mb-3">
      <label class="form-label" for="garden-description">Descripción</label>
      <textarea id="garden-description" v-model="form.description" class="form-control" rows="2" placeholder="Descripción del huerto"></textarea>
    </div>
    <div class="mb-3">
      <label class="form-label" for="garden-location">Ubicación</label>
      <input id="garden-location" v-model="form.location" type="text" class="form-control" placeholder="Balcón, jardín, terraza..." />
    </div>
    <div class="mb-3">
      <label class="form-label" for="garden-size">Tamaño (m²)</label>
      <input id="garden-size" v-model.number="form.sizeM2" type="number" step="0.1" min="0" class="form-control" />
    </div>
    <div class="d-flex gap-2 justify-content-end">
      <button type="button" class="btn btn-outline-secondary" @click="$emit('cancel')">Cancelar</button>
      <button type="submit" class="btn btn-success" :disabled="saving">
        <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
        Guardar
      </button>
    </div>
  </form>
</template>
