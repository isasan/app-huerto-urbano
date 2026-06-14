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

const touched = reactive({ plantName: false, plantedDate: false })

function submit() {
  touched.plantName = true
  touched.plantedDate = true
  if (!form.plantName.trim() || !form.plantedDate) return
  emit('submit', { ...form })
}
</script>

<template>
  <form @submit.prevent="submit" novalidate>
    <div class="row g-3">
      <div class="col-md-6">
        <label class="form-label" for="crop-plant">Planta <span class="text-danger" aria-hidden="true">*</span></label>
        <input
          id="crop-plant"
          v-model="form.plantName"
          class="form-control"
          :class="{ 'is-invalid': touched.plantName && !form.plantName.trim() }"
          required
          placeholder="Tomate, Lechuga..."
          @blur="touched.plantName = true"
        />
        <div class="invalid-feedback">Indica qué planta vas a cultivar</div>
      </div>
      <div class="col-md-6">
        <label class="form-label" for="crop-variety">Variedad</label>
        <input id="crop-variety" v-model="form.variety" class="form-control" placeholder="Cherry, Iceberg..." />
      </div>
      <div class="col-md-6">
        <label class="form-label" for="crop-planted">Fecha de siembra <span class="text-danger" aria-hidden="true">*</span></label>
        <input
          id="crop-planted"
          v-model="form.plantedDate"
          type="date"
          class="form-control"
          :class="{ 'is-invalid': touched.plantedDate && !form.plantedDate }"
          required
          @blur="touched.plantedDate = true"
        />
        <div class="invalid-feedback">La fecha de siembra es obligatoria</div>
      </div>
      <div class="col-md-6">
        <label class="form-label" for="crop-harvest">Fecha estimada de cosecha</label>
        <input id="crop-harvest" v-model="form.expectedHarvestDate" type="date" class="form-control" />
      </div>
      <div class="col-12">
        <label class="form-label" for="crop-notes">Notas</label>
        <textarea id="crop-notes" v-model="form.notes" class="form-control" rows="2"></textarea>
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
