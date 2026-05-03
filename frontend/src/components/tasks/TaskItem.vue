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
  RIEGO: 'text-primary',
  FERTILIZAR: 'text-success',
  PODAR: 'text-warning',
  TRATAR: 'text-danger',
  OTRO: 'text-secondary'
}

const today = new Date().toISOString().split('T')[0]
const isOverdue = computed(() =>
  !props.task.completed && props.task.dueDate && props.task.dueDate < today
)
</script>

<template>
  <div class="list-group-item d-flex align-items-center gap-3 px-3 py-2"
       :class="{
         'bg-light': task.completed,
         'border-danger border-start border-3': isOverdue
       }">
    <input
      type="checkbox"
      class="form-check-input mt-0 flex-shrink-0"
      :checked="task.completed"
      @change="$emit('toggle', task.id)"
    />
    <i :class="`bi ${typeIcons[task.type]} ${typeColors[task.type]} fs-5 flex-shrink-0`"></i>
    <div class="flex-grow-1 min-width-0">
      <div :class="{ 'text-decoration-line-through text-muted': task.completed }">
        {{ task.title }}
      </div>
      <div class="text-muted small">
        <span v-if="task.dueDate" :class="{ 'text-danger fw-semibold': isOverdue }">
          <i class="bi bi-calendar-event me-1"></i>{{ task.dueDate }}
          <span v-if="isOverdue" class="ms-1">(vencida)</span>
        </span>
        <span v-if="task.gardenName" class="ms-2">
          <i class="bi bi-tree me-1"></i>{{ task.gardenName }}
        </span>
      </div>
    </div>
    <span class="badge bg-secondary-subtle text-secondary flex-shrink-0">{{ task.type }}</span>
    <button class="btn btn-sm btn-outline-primary flex-shrink-0" @click="$emit('edit', task)">
      <i class="bi bi-pencil"></i>
    </button>
    <button class="btn btn-sm btn-outline-danger flex-shrink-0" @click="$emit('delete', task.id)">
      <i class="bi bi-trash"></i>
    </button>
  </div>
</template>
