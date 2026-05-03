<script setup>
import TaskItem from './TaskItem.vue'

const props = defineProps({
  tasks: { type: Array, default: () => [] }
})

defineEmits(['toggle', 'edit', 'delete'])

const typeGroups = [
  { key: 'RIEGO',      label: 'Riego',       emoji: '💧' },
  { key: 'FERTILIZAR', label: 'Fertilizar',  emoji: '🌱' },
  { key: 'PODAR',      label: 'Podar',       emoji: '✂️' },
  { key: 'TRATAR',     label: 'Tratar',      emoji: '🛡️' },
  { key: 'OTRO',       label: 'Otro',        emoji: '📋' }
]

function tasksByType(type) {
  return props.tasks.filter(t => t.type === type)
}

function pendingCount(type) {
  return props.tasks.filter(t => t.type === type && !t.completed).length
}
</script>

<template>
  <div class="row g-3">
    <div
      v-for="group in typeGroups"
      :key="group.key"
      class="col-md-6 col-xl-4"
    >
      <div v-if="tasksByType(group.key).length > 0" class="card border-0 shadow-sm h-100">
        <div class="card-header bg-transparent d-flex align-items-center gap-2">
          <span class="fs-5">{{ group.emoji }}</span>
          <span class="fw-semibold">{{ group.label }}</span>
          <span v-if="pendingCount(group.key) > 0" class="badge bg-warning text-dark ms-auto">
            {{ pendingCount(group.key) }} pendiente{{ pendingCount(group.key) > 1 ? 's' : '' }}
          </span>
          <span v-else class="badge bg-success ms-auto">Todo hecho</span>
        </div>
        <div class="list-group list-group-flush">
          <TaskItem
            v-for="task in tasksByType(group.key)"
            :key="task.id"
            :task="task"
            @toggle="$emit('toggle', $event)"
            @edit="$emit('edit', $event)"
            @delete="$emit('delete', $event)"
          />
        </div>
      </div>
    </div>

    <div v-if="tasks.length === 0" class="col-12 text-center text-muted py-5">
      <i class="bi bi-check2-all fs-1 d-block mb-3"></i>
      <p>No hay tareas que mostrar</p>
    </div>
  </div>
</template>
