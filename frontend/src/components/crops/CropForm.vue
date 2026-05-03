<script setup>
import { reactive } from 'vue'

const props = defineProps({
  initial: { type: Object, default: () => ({}) },
  plotId:  { type: Number, required: true },
  saving:  { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  plantName:           props.initial.plantName || '',
  variety:             props.initial.variety || '',
  plantedDate:         props.initial.plantedDate || '',
  expectedHarvestDate: props.initial.expectedHarvestDate || '',
  notes:               props.initial.notes || ''
})

function submit() {
  emit('submit', { ...form })
}
</script>

<template>
  <form @submit.prevent="submit">
    <div class="row g-3">
      <div class="col-md-6">
        <label class="form-label">Planta <span class="text-danger">*</span></label>
        <input v-model="form.plantName" class="form-control" required placeholder="Tomate, Lechuga..." />
      </div>
      <div class="col-md-6">
        <label class="form-label">Variedad</label>
        <input v-model="form.variety" class="form-control" placeholder="Cherry, Iceberg..." />
      </div>
      <div class="col-md-6">
        <label class="form-label">Fecha de siembra <span class="text-danger">*</span></label>
        <input v-model="form.plantedDate" type="date" class="form-control" required />
      </div>
      <div class="col-md-6">
        <label class="form-label">Fecha estimada de cosecha</label>
        <input v-model="form.expectedHarvestDate" type="date" class="form-control" />
      </div>
      <div class="col-12">
        <label class="form-label">Notas</label>
        <textarea v-model="form.notes" class="form-control" rows="2"></textarea>
      </div>
    </div>
    <div class="d-flex gap-2 justify-content-end mt-3">
      <button type="button" class="btn btn-outline-secondary" @click="$emit('cancel')">Cancelar</button>
      <button type="submit" class="btn btn-success" :disabled="saving">
        <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
        Guardar
      </button>
    </div>
  </form>
</template>
