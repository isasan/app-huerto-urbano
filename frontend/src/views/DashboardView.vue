<script setup>
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboardService.js'
import WeatherWidget from '@/components/weather/WeatherWidget.vue'

const route = useRoute()
const accessDenied = ref(route.query.accessDenied === '1')

const authStore = useAuthStore()
const dashboard = ref(null)
const loading = ref(false)

/* Animated display values */
const anim = reactive({ gardens: 0, crops: 0, tasks: 0 })

function animateCount(target, key, delay = 0) {
  setTimeout(() => {
    const duration = 900
    const start = Date.now()
    const from = anim[key]
    const step = () => {
      const t = Math.min((Date.now() - start) / duration, 1)
      const ease = 1 - Math.pow(1 - t, 3)
      anim[key] = Math.round(from + (target - from) * ease)
      if (t < 1) requestAnimationFrame(step)
    }
    requestAnimationFrame(step)
  }, delay)
}

onMounted(async () => {
  loading.value = true
  try {
    dashboard.value = await dashboardService.get()
  } finally {
    loading.value = false
  }
})

watch(
  () => dashboard.value,
  (val) => {
    if (!val) return
    animateCount(val.totalGardens, 'gardens', 80)
    animateCount(val.totalCrops,   'crops',   160)
    animateCount(val.pendingTasks, 'tasks',   240)
  }
)

const nextHarvestDate = computed(() => {
  if (!dashboard.value?.cropsReadyToHarvest?.length) return null
  const dates = dashboard.value.cropsReadyToHarvest
    .map(c => c.expectedHarvestDate)
    .filter(Boolean)
    .sort()
  return dates[0] ?? null
})

const harvestDisplay = computed(() => {
  if (!dashboard.value) return '—'
  if (nextHarvestDate.value) return nextHarvestDate.value
  if (dashboard.value.cropsReadyToHarvest.length > 0) return '¡Ya!'
  return '—'
})

const greetingTime = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return 'Buenos días'
  if (h < 19) return 'Buenas tardes'
  return 'Buenas noches'
})

const qualityBadge = {
  MALA: 'mala',
  NORMAL: 'normal',
  BUENA: 'buena',
  EXCELENTE: 'excelente'
}
const qualityLabel = { MALA: 'Mala', NORMAL: 'Normal', BUENA: 'Buena', EXCELENTE: 'Excelente' }
const qualityStars = { MALA: '★', NORMAL: '★★', BUENA: '★★★', EXCELENTE: '★★★★' }

const typeConfig = {
  RIEGO:      { icon: 'bi-droplet-fill',  color: 'blue',  bg: '#DBEAFE', fg: '#1D4ED8', emoji: '💧' },
  FERTILIZAR: { icon: 'bi-flower2',       color: 'green', bg: '#D1FAE5', fg: '#059669', emoji: '🌱' },
  PODAR:      { icon: 'bi-scissors',      color: 'amber', bg: '#FEF3C7', fg: '#B45309', emoji: '✂️' },
  TRATAR:     { icon: 'bi-shield-fill',   color: 'red',   bg: '#FEE2E2', fg: '#B91C1C', emoji: '🛡️' },
  OTRO:       { icon: 'bi-three-dots',    color: 'stone', bg: '#F5F5F4', fg: '#78716C', emoji: '📋' },
}

function getTypeConfig(type) {
  return typeConfig[type] || typeConfig.OTRO
}
</script>

<template>
  <div class="dashboard-view">

    <!-- Access denied alert -->
    <div v-if="accessDenied" class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
      <i class="bi bi-shield-x me-2" aria-hidden="true"></i>
      Acceso denegado. No tienes permisos de administrador.
      <button type="button" class="btn-close" @click="accessDenied = false" aria-label="Cerrar alerta"></button>
    </div>

    <!-- ── Hero Header ── -->
    <div class="dash-hero animate-up">
      <div class="hero-text">
        <p class="hero-greeting">{{ greetingTime }},</p>
        <h1 class="hero-name">{{ authStore.user?.username }} <span class="hero-leaf" aria-hidden="true">🌿</span></h1>
        <p class="hero-sub">Aquí tienes el estado de tu huerto hoy</p>
      </div>
      <div class="hero-weather">
        <WeatherWidget />
      </div>
    </div>

    <!-- ── Loading skeleton ── -->
    <div v-if="loading" aria-label="Cargando datos del huerto" aria-busy="true">
      <div class="row g-3 mb-4">
        <div v-for="i in 4" :key="i" class="col-6 col-md-3">
          <div class="stat-skel">
            <div class="skel" style="width:48px;height:48px;border-radius:10px;margin-bottom:12px"></div>
            <div class="skel" style="width:60%;height:32px;margin-bottom:8px"></div>
            <div class="skel" style="width:80%;height:14px"></div>
          </div>
        </div>
      </div>
      <div class="row g-3">
        <div v-for="i in 3" :key="i" class="col-md-4">
          <div class="stat-skel" style="height:220px">
            <div class="skel" style="width:70%;height:18px;margin-bottom:12px"></div>
            <div class="skel" style="width:100%;height:14px;margin-bottom:8px"></div>
            <div class="skel" style="width:90%;height:14px;margin-bottom:8px"></div>
            <div class="skel" style="width:80%;height:14px"></div>
          </div>
        </div>
      </div>
    </div>

    <template v-else-if="dashboard">
      <!-- ── Stats grid ── -->
      <div class="row g-3 mb-4 stagger">

        <!-- Mis Huertos -->
        <div class="col-6 col-md-3">
          <RouterLink to="/gardens" class="stat-card c-green" aria-label="Ver mis huertos">
            <div class="stat-card-inner">
              <div class="stat-icon" aria-hidden="true">
                <i class="bi bi-tree-fill"></i>
              </div>
              <div class="stat-data">
                <div class="stat-value c-green" aria-live="polite">{{ anim.gardens }}</div>
                <div class="stat-label">Mis Huertos</div>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- Cultivos -->
        <div class="col-6 col-md-3">
          <RouterLink to="/gardens" class="stat-card c-blue" aria-label="Ver cultivos activos">
            <div class="stat-card-inner">
              <div class="stat-icon" aria-hidden="true">
                <i class="bi bi-flower1"></i>
              </div>
              <div class="stat-data">
                <div class="stat-value c-blue" aria-live="polite">{{ anim.crops }}</div>
                <div class="stat-label">Cultivos Activos</div>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- Tareas -->
        <div class="col-6 col-md-3">
          <RouterLink to="/tasks" class="stat-card c-amber" aria-label="Ver tareas pendientes">
            <div class="stat-card-inner">
              <div class="stat-icon" aria-hidden="true">
                <i class="bi bi-check2-circle"></i>
              </div>
              <div class="stat-data">
                <div class="stat-value-wrap">
                  <span class="stat-value c-amber" aria-live="polite">{{ anim.tasks }}</span>
                  <span
                    v-if="dashboard.overdueTasks > 0"
                    class="overdue-chip"
                    :aria-label="`${dashboard.overdueTasks} tareas vencidas`"
                  >
                    {{ dashboard.overdueTasks }} vencida{{ dashboard.overdueTasks !== 1 ? 's' : '' }}
                  </span>
                </div>
                <div class="stat-label">Tareas Pendientes</div>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- Próxima cosecha -->
        <div class="col-6 col-md-3">
          <RouterLink to="/harvests" class="stat-card c-red" aria-label="Ver próxima cosecha">
            <div class="stat-card-inner">
              <div class="stat-icon" aria-hidden="true">
                <i class="bi bi-basket2-fill"></i>
              </div>
              <div class="stat-data">
                <div class="stat-value c-red harvest-val">{{ harvestDisplay }}</div>
                <div class="stat-label">Próxima Cosecha</div>
              </div>
            </div>
          </RouterLink>
        </div>

      </div>

      <!-- ── Three panels ── -->
      <div class="row g-3 stagger">

        <!-- Tareas de hoy (timeline) -->
        <div class="col-md-4">
          <div class="panel-card h-100" role="region" aria-label="Tareas para hoy">
            <div class="panel-head">
              <div class="panel-head-icon" style="background:var(--blue-100);color:var(--blue-600)">
                <i class="bi bi-calendar-check" aria-hidden="true"></i>
              </div>
              <h2 class="panel-head-title">Tareas para hoy</h2>
            </div>
            <div class="panel-body">
              <div
                v-if="dashboard.tasksDueToday.length === 0"
                class="empty-state"
                aria-label="No hay tareas para hoy"
              >
                <span class="empty-icon" aria-hidden="true">☀️</span>
                <p class="mb-0 fw-semibold" style="font-size:0.88rem">¡Sin tareas para hoy!</p>
                <p class="mb-0" style="font-size:0.8rem;color:var(--text-light)">Disfruta el día en el huerto.</p>
              </div>

              <ul v-else class="timeline-list" role="list">
                <li
                  v-for="(t, idx) in dashboard.tasksDueToday"
                  :key="t.id"
                  class="timeline-item"
                  :style="`animation-delay:${idx * 60}ms`"
                >
                  <div
                    class="timeline-dot"
                    :style="`background:${getTypeConfig(t.type).bg};color:${getTypeConfig(t.type).fg}`"
                    :aria-label="t.type"
                  >
                    <span aria-hidden="true">{{ getTypeConfig(t.type).emoji }}</span>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-title">{{ t.title }}</div>
                    <div class="timeline-sub">{{ t.gardenName }}</div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Listos para cosechar -->
        <div class="col-md-4">
          <div class="panel-card h-100" role="region" aria-label="Cultivos listos para cosechar">
            <div class="panel-head">
              <div class="panel-head-icon" style="background:var(--green-100);color:var(--green-600)">
                <i class="bi bi-scissors" aria-hidden="true"></i>
              </div>
              <h2 class="panel-head-title">Listos para cosechar</h2>
            </div>
            <div class="panel-body">
              <div
                v-if="dashboard.cropsReadyToHarvest.length === 0"
                class="empty-state"
                aria-label="No hay cultivos listos para cosechar"
              >
                <span class="empty-icon" aria-hidden="true">⏳</span>
                <p class="mb-0 fw-semibold" style="font-size:0.88rem">Cultivos en crecimiento</p>
                <p class="mb-0" style="font-size:0.8rem;color:var(--text-light)">¡La cosecha llegará pronto!</p>
              </div>

              <ul v-else class="harvest-ready-list" role="list">
                <li
                  v-for="(c, idx) in dashboard.cropsReadyToHarvest"
                  :key="c.id"
                  class="harvest-ready-item"
                  :style="`animation-delay:${idx * 60}ms`"
                >
                  <div class="harvest-ready-icon" aria-hidden="true">🌾</div>
                  <div class="harvest-ready-text">
                    <div class="timeline-title">{{ c.plantName }}</div>
                    <div class="timeline-sub">{{ c.plotName }}</div>
                  </div>
                  <span class="ready-badge" aria-label="Listo para cosechar">
                    <i class="bi bi-check-lg me-1" aria-hidden="true"></i>Listo
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Últimas cosechas -->
        <div class="col-md-4">
          <div class="panel-card h-100" role="region" aria-label="Últimas cosechas">
            <div class="panel-head">
              <div class="panel-head-icon" style="background:var(--amber-100);color:var(--amber-600)">
                <i class="bi bi-basket" aria-hidden="true"></i>
              </div>
              <h2 class="panel-head-title">Últimas cosechas</h2>
            </div>
            <div class="panel-body">
              <div
                v-if="dashboard.recentHarvests.length === 0"
                class="empty-state"
                aria-label="No hay cosechas recientes"
              >
                <span class="empty-icon" aria-hidden="true">🧺</span>
                <p class="mb-0 fw-semibold" style="font-size:0.88rem">¡Tu primera cosecha se acerca!</p>
                <p class="mb-0" style="font-size:0.8rem;color:var(--text-light)">Registra tus cosechas aquí.</p>
              </div>

              <ul v-else class="harvest-list" role="list">
                <li
                  v-for="(h, idx) in dashboard.recentHarvests"
                  :key="h.id"
                  class="harvest-list-item"
                  :style="`animation-delay:${idx * 60}ms`"
                >
                  <div class="harvest-list-main">
                    <div class="timeline-title">{{ h.plantName }}</div>
                    <div class="timeline-sub">
                      <i class="bi bi-calendar3 me-1" aria-hidden="true"></i>{{ h.harvestDate }}
                    </div>
                  </div>
                  <div class="harvest-list-side">
                    <div class="harvest-qty" v-if="h.quantity != null">
                      {{ h.quantity }} <span style="opacity:.7">{{ h.unit }}</span>
                    </div>
                    <span :class="`q-pill ${qualityBadge[h.quality]}`" :aria-label="`Calidad: ${qualityLabel[h.quality]}`">
                      {{ qualityStars[h.quality] }} {{ qualityLabel[h.quality] }}
                    </span>
                  </div>
                </li>
              </ul>
            </div>
            <div v-if="dashboard.recentHarvests.length > 0" class="panel-foot">
              <RouterLink to="/harvests" class="see-all-link" aria-label="Ver todas las cosechas">
                Ver todas las cosechas
                <i class="bi bi-arrow-right ms-1" aria-hidden="true"></i>
              </RouterLink>
            </div>
          </div>
        </div>

      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard-view {
  max-width: 1200px;
}

/* Hero */
.dash-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.hero-text { flex: 1; min-width: 200px; }
.hero-greeting {
  font-size: 0.9rem;
  color: var(--text-light);
  font-weight: 600;
  margin: 0;
  letter-spacing: 0.02em;
}
.hero-name {
  font-family: var(--font-display) !important;
  font-size: 2rem !important;
  font-weight: 800 !important;
  color: var(--green-600) !important;
  margin: 2px 0 6px;
  letter-spacing: -0.03em;
}
.hero-leaf {
  font-size: 1.6rem;
  animation: leafFloat 4s ease-in-out infinite;
  display: inline-block;
  vertical-align: middle;
  margin-left: 4px;
}
.hero-sub {
  font-size: 0.9rem;
  color: var(--text-muted);
  margin: 0;
}
.hero-weather { flex-shrink: 0; }

/* Stat cards inner layout */
.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
}
.stat-data { flex: 1; min-width: 0; }
.stat-value-wrap { display: flex; align-items: baseline; gap: 8px; flex-wrap: wrap; }

.harvest-val {
  font-size: 1.2rem !important;
  word-break: break-word;
}

.overdue-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: var(--red-100);
  color: var(--red-700);
  border-radius: var(--r-full);
  font-size: 0.7rem;
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
}

/* Skeleton */
.stat-skel {
  background: var(--bg-card);
  border-radius: var(--r-md);
  padding: 20px;
  border: 1px solid var(--border-card);
  box-shadow: var(--shadow-xs);
}

/* Ready to harvest list */
.harvest-ready-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.harvest-ready-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--border-subtle);
  animation: fadeInUp var(--t-slow) both;
}
.harvest-ready-item:last-child { border-bottom: none; }
.harvest-ready-icon {
  font-size: 1.3rem;
  width: 34px;
  text-align: center;
}
.harvest-ready-text { flex: 1; min-width: 0; }

.ready-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  background: var(--green-100);
  color: var(--green-700);
  border-radius: var(--r-full);
  font-size: 0.74rem;
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
  flex-shrink: 0;
}

/* Recent harvests */
.harvest-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.harvest-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 9px 4px;
  border-bottom: 1px solid var(--border-subtle);
  animation: fadeInUp var(--t-slow) both;
}
.harvest-list-item:last-child { border-bottom: none; }
.harvest-list-main { flex: 1; min-width: 0; }
.harvest-list-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.harvest-qty {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--text-primary);
}

.see-all-link {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.82rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  transition: color var(--t-fast);
}
.see-all-link:hover {
  color: var(--green-600);
}
</style>
