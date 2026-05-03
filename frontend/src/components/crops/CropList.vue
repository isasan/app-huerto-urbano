<script setup>
defineProps({
  crops: { type: Array, default: () => [] }
})

defineEmits(['delete', 'edit'])

const statusBadge = {
  PLANTED: 'bg-info text-dark',
  GROWING: 'bg-primary',
  READY: 'bg-success',
  HARVESTED: 'bg-secondary'
}

const statusLabel = {
  PLANTED: 'Plantado',
  GROWING: 'Creciendo',
  READY: 'Listo para cosechar',
  HARVESTED: 'Cosechado'
}
</script>

<template>
  <div>
    <div v-if="crops.length === 0" class="text-center text-muted py-4">
      <i class="bi bi-seedling fs-2 d-block mb-2"></i>
      No hay cultivos en esta parcela
    </div>
    <div v-else class="list-group list-group-flush">
      <div
        v-for="crop in crops"
        :key="crop.id"
        class="list-group-item d-flex justify-content-between align-items-center px-0"
      >
        <div>
          <div class="fw-semibold">{{ crop.plantName }}
            <span v-if="crop.variety" class="text-muted fw-normal"> — {{ crop.variety }}</span>
          </div>
          <div class="text-muted small">
            Plantado: {{ crop.plantedDate }}
            <span v-if="crop.expectedHarvestDate"> | Cosecha estimada: {{ crop.expectedHarvestDate }}</span>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span :class="`badge ${statusBadge[crop.status]}`">{{ statusLabel[crop.status] }}</span>
          <button class="btn btn-sm btn-outline-primary" @click="$emit('edit', crop)">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="$emit('delete', crop.id)">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
