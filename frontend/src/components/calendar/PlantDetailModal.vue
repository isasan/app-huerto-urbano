<script setup>
import BaseModal from '@/components/ui/BaseModal.vue'

defineProps({
  show: { type: Boolean, default: false },
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
  <BaseModal :show="show" @close="$emit('close')">
    <template #header>
      <h2 class="modal-title d-flex align-items-center gap-2">
        <span class="plant-emoji" aria-hidden="true">{{ plant?.emoji }}</span>
        <span>
          <span class="d-block">{{ plant?.name }}</span>
          <span v-if="detail?.latinName" class="plant-latin">{{ detail.latinName }}</span>
        </span>
      </h2>
    </template>

    <!-- Sin ficha todavía -->
    <div v-if="!detail" class="empty-state py-3">
      <span class="empty-icon" aria-hidden="true">📋</span>
      <p class="mb-0">El administrador aún no ha añadido información para esta planta.</p>
    </div>

    <template v-else>
      <!-- Riego -->
      <div class="detail-row">
        <span class="detail-row-icon" aria-hidden="true">💧</span>
        <div>
          <div class="detail-row-label water">Riego</div>
          <div>{{ detail.wateringFrequency || 'Sin especificar' }}</div>
        </div>
      </div>

      <!-- Plagas -->
      <div class="detail-row">
        <span class="detail-row-icon" aria-hidden="true">🐛</span>
        <div>
          <div class="detail-row-label pests">Plagas a vigilar</div>
          <div v-if="pestList(detail.pests).length" class="d-flex flex-wrap gap-1 mt-1">
            <span
              v-for="pest in pestList(detail.pests)"
              :key="pest"
              class="pest-pill"
            >{{ pest }}</span>
          </div>
          <span v-else class="text-muted small">Sin especificar</span>
        </div>
      </div>

      <!-- Wikipedia -->
      <div v-if="detail.wikipediaUrl" class="detail-row mb-0">
        <span class="detail-row-icon" aria-hidden="true">🔗</span>
        <div>
          <div class="detail-row-label">Más información</div>
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

    <template #footer>
      <button type="button" class="btn btn-outline-secondary btn-sm" @click="$emit('close')">Cerrar</button>
    </template>
  </BaseModal>
</template>

<style scoped>
.plant-emoji { font-size: 1.6rem; }
.plant-latin {
  display: block;
  font-family: var(--font-body);
  font-weight: 400;
  font-style: italic;
  font-size: 0.82rem;
  color: var(--text-muted);
}
.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 16px;
}
.detail-row-icon { font-size: 1.3rem; }
.detail-row-label {
  font-size: 0.74rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-muted);
}
.detail-row-label.water { color: var(--blue-600); }
.detail-row-label.pests { color: var(--red-600); }
.pest-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--r-full);
  font-size: 0.78rem;
  font-weight: 600;
  font-family: var(--font-display);
  background: var(--red-100);
  color: var(--red-700);
}
</style>
