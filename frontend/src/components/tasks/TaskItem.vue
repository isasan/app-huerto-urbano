<script setup>
import { computed } from 'vue'

const props = defineProps({
  task: { type: Object, required: true }
})

defineEmits(['toggle', 'edit', 'delete'])

const typeIcons = {
  RIEGO: 'bi-droplet',
  FERTILIZAR: 'bi-flower2',
  PODAR: 'bi-scissors',
  TRATAR: 'bi-shield',
  OTRO: 'bi-three-dots'
}

const typeColors = {
  RIEGO: 'var(--blue-600)',
  FERTILIZAR: 'var(--green-600)',
  PODAR: 'var(--amber-600)',
  TRATAR: 'var(--red-600)',
  OTRO: 'var(--stone-500)'
}

const today = new Date().toISOString().split('T')[0]
const isOverdue = computed(() =>
  !props.task.completed && props.task.dueDate && props.task.dueDate < today
)
</script>

<template>
  <div class="task-item" :class="{ completed: task.completed, overdue: isOverdue }">
    <label class="task-check">
      <input
        type="checkbox"
        :checked="task.completed"
        :aria-label="`Marcar ${task.title} como ${task.completed ? 'pendiente' : 'completada'}`"
        @change="$emit('toggle', task.id)"
      />
      <span class="task-check-box" aria-hidden="true">
        <i class="bi bi-check-lg"></i>
      </span>
    </label>
    <i class="bi fs-5 flex-shrink-0" :class="typeIcons[task.type]" :style="{ color: typeColors[task.type] }" aria-hidden="true"></i>
    <div class="flex-grow-1 min-width-0">
      <div class="task-title">{{ task.title }}</div>
      <div class="text-muted small">
        <span v-if="task.dueDate" :class="{ 'task-overdue-date': isOverdue }">
          <i class="bi bi-calendar-event me-1" aria-hidden="true"></i>{{ task.dueDate }}
          <span v-if="isOverdue" class="ms-1">(vencida)</span>
        </span>
        <span v-if="task.gardenName" class="ms-2">
          <i class="bi bi-tree me-1" aria-hidden="true"></i>{{ task.gardenName }}
        </span>
      </div>
    </div>
    <span class="type-pill flex-shrink-0">{{ task.type }}</span>
    <button class="btn btn-sm btn-outline-secondary flex-shrink-0" @click="$emit('edit', task)" :aria-label="`Editar ${task.title}`">
      <i class="bi bi-pencil" aria-hidden="true"></i>
    </button>
    <button class="btn btn-sm btn-outline-danger flex-shrink-0" @click="$emit('delete', task.id)" :aria-label="`Eliminar ${task.title}`">
      <i class="bi bi-trash" aria-hidden="true"></i>
    </button>
  </div>
</template>

<style scoped>
.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--border-subtle);
  transition: background var(--t-fast), opacity var(--t-base);
}
.task-item:last-child { border-bottom: none; }
.task-item:hover { background: var(--green-50); }
.task-item.completed {
  background: var(--bg-subtle);
  opacity: 0.72;
}
.task-item.overdue {
  border-left: 3px solid var(--red-600);
  padding-left: 13px;
}

/* Checkbox custom */
.task-check {
  position: relative;
  display: inline-flex;
  margin: 0;
  cursor: pointer;
  flex-shrink: 0;
}
.task-check input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.task-check-box {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  border: 2px solid var(--stone-300);
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--t-base);
}
.task-check-box i {
  font-size: 0.85rem;
  color: var(--text-on-accent);
  transform: scale(0);
  transition: transform var(--t-base);
}
.task-check input:checked + .task-check-box {
  background: var(--green-600);
  border-color: var(--green-600);
}
.task-check input:checked + .task-check-box i {
  transform: scale(1);
}
.task-check input:focus-visible + .task-check-box {
  outline: 2.5px solid var(--green-500);
  outline-offset: 2px;
}
.task-check:hover .task-check-box {
  border-color: var(--green-500);
}

.task-title {
  font-size: 0.92rem;
  color: var(--text-primary);
  transition: color var(--t-base), opacity var(--t-base);
}
.task-item.completed .task-title {
  text-decoration: line-through;
  color: var(--text-muted);
}
.task-overdue-date {
  color: var(--red-600);
  font-weight: 700;
}
.type-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 9px;
  border-radius: var(--r-full);
  font-size: 0.68rem;
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: 0.03em;
  background: var(--bg-subtle);
  color: var(--text-muted);
}
</style>
