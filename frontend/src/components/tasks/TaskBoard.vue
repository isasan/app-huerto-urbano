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
  <div class="row g-3 stagger">
    <div
      v-for="group in typeGroups"
      :key="group.key"
      class="col-md-6 col-xl-4"
    >
      <div v-if="tasksByType(group.key).length > 0" class="panel-card h-100">
        <div class="panel-head">
          <span class="fs-5" aria-hidden="true">{{ group.emoji }}</span>
          <h2 class="panel-head-title">{{ group.label }}</h2>
          <span v-if="pendingCount(group.key) > 0" class="count-pill pending ms-auto">
            {{ pendingCount(group.key) }} pendiente{{ pendingCount(group.key) > 1 ? 's' : '' }}
          </span>
          <span v-else class="count-pill done ms-auto">Todo hecho</span>
        </div>
        <div class="task-list">
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

    <div v-if="tasks.length === 0" class="col-12">
      <div class="empty-state py-5">
        <span class="empty-icon" aria-hidden="true">✨</span>
        <p class="mb-0">No hay tareas que mostrar</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.count-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--r-full);
  font-size: 0.72rem;
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
}
.count-pill.pending { background: var(--amber-100); color: var(--amber-700); }
.count-pill.done    { background: var(--green-100); color: var(--green-700); }
.task-list { padding: 4px 0; }
</style>
