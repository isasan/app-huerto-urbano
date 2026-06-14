<script setup>
defineProps({
  crops: { type: Array, default: () => [] }
})

defineEmits(['delete', 'edit'])

const statusClass = {
  PLANTED: 'planted',
  GROWING: 'growing',
  READY: 'ready',
  HARVESTED: 'harvested'
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
    <div v-if="crops.length === 0" class="empty-state py-4">
      <span class="empty-icon" aria-hidden="true">🌿</span>
      <p class="mb-0 small">No hay cultivos en esta parcela</p>
    </div>
    <div v-else class="crop-list">
      <div
        v-for="crop in crops"
        :key="crop.id"
        class="crop-item animate-in"
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
          <span :class="`crop-status ${statusClass[crop.status]}`">{{ statusLabel[crop.status] }}</span>
          <button class="btn btn-sm btn-outline-secondary" @click="$emit('edit', crop)" :aria-label="`Editar ${crop.plantName}`">
            <i class="bi bi-pencil" aria-hidden="true"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" @click="$emit('delete', crop.id)" :aria-label="`Eliminar ${crop.plantName}`">
            <i class="bi bi-trash" aria-hidden="true"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.crop-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 10px;
  border-bottom: 1px solid var(--border-subtle);
  border-radius: var(--r-xs);
  transition: background var(--t-fast);
}
.crop-item:last-child { border-bottom: none; }
.crop-item:hover { background: var(--green-50); }
.crop-status {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--r-full);
  font-size: 0.74rem;
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
}
.crop-status.planted   { background: var(--blue-100);  color: var(--blue-700); }
.crop-status.growing   { background: var(--green-100); color: var(--green-700); }
.crop-status.ready     { background: var(--amber-100); color: var(--amber-700); }
.crop-status.harvested { background: var(--bg-subtle); color: var(--stone-600); }
</style>
