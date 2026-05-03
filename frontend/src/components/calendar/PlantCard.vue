<script setup>
defineProps({
  plant: { type: Object, required: true }
})

defineEmits(['click-detail'])

const difficultyClass = {
  FACIL: 'bg-success',
  MEDIA: 'bg-warning text-dark',
  DIFICIL: 'bg-danger'
}

const difficultyLabel = {
  FACIL: 'Fácil',
  MEDIA: 'Media',
  DIFICIL: 'Difícil'
}
</script>

<template>
  <div class="card h-100 border-0 shadow-sm plant-card">
    <div class="card-body d-flex flex-column">
      <div class="text-center mb-3">
        <span style="font-size: 3rem; line-height: 1;">{{ plant.emoji }}</span>
      </div>

      <div class="d-flex justify-content-between align-items-start mb-1">
        <h6 class="card-title mb-0 fw-bold">{{ plant.name }}</h6>
        <span :class="`badge ${difficultyClass[plant.difficulty]}`" style="font-size: 0.65rem; white-space: nowrap;">
          {{ difficultyLabel[plant.difficulty] }}
        </span>
      </div>

      <p class="text-muted small mb-3">
        <i class="bi bi-clock me-1"></i>{{ plant.daysToHarvest }} días hasta cosecha
      </p>

      <div class="mt-auto">
        <div class="d-flex flex-wrap gap-1 mb-2">
          <span v-if="plant.canSow" class="badge bg-success">🌱 Sembrar</span>
          <span v-if="plant.canTransplant" class="badge bg-warning text-dark">🌿 Trasplantar</span>
          <span v-if="plant.canHarvest" class="badge text-white" style="background-color: #fd7e14;">🌾 Cosechar</span>
          <span v-if="!plant.canSow && !plant.canTransplant && !plant.canHarvest" class="text-muted small fst-italic">
            Sin actividad este mes
          </span>
        </div>

        <button
          class="btn btn-outline-success btn-sm w-100"
          @click="$emit('click-detail', plant)"
        >
          <i class="bi bi-journal-text me-1"></i>Ver ficha
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.plant-card {
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.plant-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.12) !important;
}
</style>
