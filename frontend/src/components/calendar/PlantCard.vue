<script setup>
defineProps({
  plant: { type: Object, required: true }
})

defineEmits(['click-detail'])

const difficultyClass = {
  FACIL: 'facil',
  MEDIA: 'media',
  DIFICIL: 'dificil'
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
        <span class="plant-emoji" aria-hidden="true">{{ plant.emoji }}</span>
      </div>

      <div class="d-flex justify-content-between align-items-start mb-1">
        <h6 class="card-title mb-0 fw-bold">{{ plant.name }}</h6>
        <span :class="`diff-pill ${difficultyClass[plant.difficulty]}`">
          {{ difficultyLabel[plant.difficulty] }}
        </span>
      </div>

      <p class="text-muted small mb-3">
        <i class="bi bi-clock me-1" aria-hidden="true"></i>{{ plant.daysToHarvest }} días hasta cosecha
      </p>

      <div class="mt-auto">
        <div class="d-flex flex-wrap gap-1 mb-2">
          <span v-if="plant.canSow" class="act-pill sembrar">🌱 Sembrar</span>
          <span v-if="plant.canTransplant" class="act-pill trasplantar">🌿 Trasplantar</span>
          <span v-if="plant.canHarvest" class="act-pill cosechar">🌾 Cosechar</span>
          <span v-if="!plant.canSow && !plant.canTransplant && !plant.canHarvest" class="text-muted small fst-italic">
            Sin actividad este mes
          </span>
        </div>

        <button
          class="btn btn-outline-success btn-sm w-100"
          @click="$emit('click-detail', plant)"
        >
          <i class="bi bi-journal-text me-1" aria-hidden="true"></i>Ver ficha
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.plant-card {
  transition: transform var(--t-base), box-shadow var(--t-base);
}
.plant-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover) !important;
}
.plant-emoji {
  font-size: 3rem;
  line-height: 1;
  display: inline-block;
  transition: transform var(--t-base);
}
.plant-card:hover .plant-emoji {
  transform: scale(1.12) rotate(-3deg);
}
.diff-pill, .act-pill {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 9px;
  border-radius: var(--r-full);
  font-size: 0.68rem;
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
}
.diff-pill.facil   { background: var(--green-100); color: var(--green-700); }
.diff-pill.media   { background: var(--amber-100); color: var(--amber-700); }
.diff-pill.dificil { background: var(--red-100);   color: var(--red-700); }
.act-pill { font-size: 0.74rem; }
.act-pill.sembrar     { background: var(--green-100);   color: var(--green-700); }
.act-pill.trasplantar { background: var(--amber-100);   color: var(--amber-700); }
.act-pill.cosechar    { background: var(--harvest-100); color: var(--harvest-600); }
</style>
