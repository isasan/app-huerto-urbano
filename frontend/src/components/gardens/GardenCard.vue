<script setup>
defineProps({
  garden: { type: Object, required: true }
})

defineEmits(['edit', 'delete'])
</script>

<template>
  <div class="card h-100 border-0 shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-start">
        <h6 class="card-title mb-1">
          <i class="bi bi-tree-fill text-success me-2"></i>{{ garden.name }}
        </h6>
        <div class="dropdown">
          <button class="btn btn-sm btn-outline-secondary" data-bs-toggle="dropdown">
            <i class="bi bi-three-dots-vertical"></i>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <RouterLink :to="`/gardens/${garden.id}`" class="dropdown-item">
                <i class="bi bi-eye me-2"></i>Ver detalle
              </RouterLink>
            </li>
            <li>
              <button class="dropdown-item" @click="$emit('edit', garden)">
                <i class="bi bi-pencil me-2"></i>Editar
              </button>
            </li>
            <li><hr class="dropdown-divider" /></li>
            <li>
              <button class="dropdown-item text-danger" @click="$emit('delete', garden.id)">
                <i class="bi bi-trash me-2"></i>Eliminar
              </button>
            </li>
          </ul>
        </div>
      </div>
      <p class="text-muted small mb-2">{{ garden.description || 'Sin descripción' }}</p>
      <div class="d-flex gap-3 text-muted small">
        <span><i class="bi bi-geo-alt me-1"></i>{{ garden.location || 'Sin ubicación' }}</span>
        <span><i class="bi bi-rulers me-1"></i>{{ garden.sizeM2 || 0 }} m²</span>
        <span><i class="bi bi-grid me-1"></i>{{ garden.plotCount }} parcelas</span>
      </div>
    </div>
    <div class="card-footer bg-transparent border-0 pt-0">
      <RouterLink :to="`/gardens/${garden.id}`" class="btn btn-sm btn-outline-success w-100">
        Ver huerto
      </RouterLink>
    </div>
  </div>
</template>
