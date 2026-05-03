<script setup>
defineProps({
  plant: { type: Object, default: null },   // { name, emoji } from calendar
  detail: { type: Object, default: null }   // PlantDetailResponse | null
})

defineEmits(['close'])

function pestList(pests) {
  if (!pests) return []
  return pests.split(',').map(p => p.trim()).filter(Boolean)
}
</script>

<template>
  <div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,.45);" @click.self="$emit('close')">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">

        <!-- Cabecera -->
        <div class="modal-header bg-success text-white">
          <h5 class="modal-title d-flex align-items-center gap-2">
            <span class="fs-4">{{ plant?.emoji }}</span>
            <div>
              <div>{{ plant?.name }}</div>
              <div v-if="detail?.latinName" class="fw-normal fst-italic" style="font-size:.85rem">
                {{ detail.latinName }}
              </div>
            </div>
          </h5>
          <button type="button" class="btn-close btn-close-white" @click="$emit('close')"></button>
        </div>

        <!-- Cuerpo -->
        <div class="modal-body">

          <!-- Sin ficha todavía -->
          <div v-if="!detail" class="text-center text-muted py-3">
            <div class="fs-2 mb-2">📋</div>
            <p class="mb-0">El administrador aún no ha añadido información para esta planta.</p>
          </div>

          <template v-else>
            <!-- Riego -->
            <div class="d-flex align-items-start gap-3 mb-3">
              <span class="fs-4">💧</span>
              <div>
                <div class="fw-semibold text-primary small text-uppercase tracking-wide">Riego</div>
                <div>{{ detail.wateringFrequency || 'Sin especificar' }}</div>
              </div>
            </div>

            <!-- Plagas -->
            <div class="d-flex align-items-start gap-3 mb-3">
              <span class="fs-4">🐛</span>
              <div>
                <div class="fw-semibold text-danger small text-uppercase">Plagas a vigilar</div>
                <div v-if="pestList(detail.pests).length" class="d-flex flex-wrap gap-1 mt-1">
                  <span
                    v-for="pest in pestList(detail.pests)"
                    :key="pest"
                    class="badge bg-danger-subtle text-danger border border-danger-subtle"
                  >{{ pest }}</span>
                </div>
                <span v-else class="text-muted small">Sin especificar</span>
              </div>
            </div>

            <!-- Wikipedia -->
            <div v-if="detail.wikipediaUrl" class="d-flex align-items-start gap-3">
              <span class="fs-4">🔗</span>
              <div>
                <div class="fw-semibold text-secondary small text-uppercase">Más información</div>
                <a
                  :href="detail.wikipediaUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-success small"
                >
                  Ver en Wikipedia <i class="bi bi-box-arrow-up-right ms-1"></i>
                </a>
              </div>
            </div>
          </template>
        </div>

        <div class="modal-footer border-0 pt-0">
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="$emit('close')">Cerrar</button>
        </div>
      </div>
    </div>
  </div>
</template>
