<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import SeedingCalendar from '@/components/calendar/SeedingCalendar.vue'

const authStore = useAuthStore()

const currentMonth = new Date().getMonth() + 1
const selectedMonth = ref(currentMonth)
const hemisphere = ref(
  authStore.isAuthenticated && authStore.user?.hemisphere
    ? authStore.user.hemisphere
    : 'NORTE'
)

const months = [
  { num: 1, name: 'Ene', full: 'Enero' },
  { num: 2, name: 'Feb', full: 'Febrero' },
  { num: 3, name: 'Mar', full: 'Marzo' },
  { num: 4, name: 'Abr', full: 'Abril' },
  { num: 5, name: 'May', full: 'Mayo' },
  { num: 6, name: 'Jun', full: 'Junio' },
  { num: 7, name: 'Jul', full: 'Julio' },
  { num: 8, name: 'Ago', full: 'Agosto' },
  { num: 9, name: 'Sep', full: 'Septiembre' },
  { num: 10, name: 'Oct', full: 'Octubre' },
  { num: 11, name: 'Nov', full: 'Noviembre' },
  { num: 12, name: 'Dic', full: 'Diciembre' },
]

function scrollToCalendar() {
  document.getElementById('calendario')?.scrollIntoView({ behavior: 'smooth' })
}

const features = [
  { icon: '📅', label: 'Calendario inteligente', desc: 'Por hemisferio y mes' },
  { icon: '🌡️', label: 'Clima en tiempo real',   desc: 'Para tu ciudad' },
  { icon: '📊', label: 'Registro de cosechas',    desc: 'Historial completo' },
]
</script>

<template>
  <div class="home-view">

    <!-- ── Hero ── -->
    <section class="hero-section animate-up" aria-label="Presentación de HuertoApp">
      <div class="hero-bg-layer" aria-hidden="true">
        <div class="hero-orb hero-orb-1"></div>
        <div class="hero-orb hero-orb-2"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge" aria-hidden="true">
          <span>🌱</span>
          <span>Tu huerto urbano inteligente</span>
        </div>

        <h1 class="hero-title">
          Cuida tu huerto<br>
          <span class="hero-title-accent">con pasión</span>
        </h1>

        <p class="hero-lead">
          Planta, cuida y cosecha con el calendario perfecto para tu zona.<br class="d-none d-md-block">
          Diseñado para aficionados y principiantes.
        </p>

        <p class="hero-micro" aria-hidden="true">Gratuito · Sin instalación · Hemisferio Norte y Sur</p>

        <div class="hero-ctas" role="group" aria-label="Acciones principales">
          <template v-if="!authStore.isAuthenticated">
            <RouterLink to="/register" class="cta-primary">
              🚀 Empezar gratis
            </RouterLink>
            <RouterLink to="/login" class="cta-secondary">
              Iniciar sesión
            </RouterLink>
          </template>
          <template v-else>
            <RouterLink to="/dashboard" class="cta-primary">
              📊 Ir al dashboard
            </RouterLink>
            <button class="cta-secondary" @click="scrollToCalendar">
              👁️ Ver Demo
            </button>
          </template>
        </div>
      </div>
    </section>

    <!-- ── Feature strip ── -->
    <div class="features-strip stagger" role="list" aria-label="Características principales">
      <div
        v-for="f in features"
        :key="f.label"
        class="feature-item"
        role="listitem"
      >
        <span class="feature-icon" aria-hidden="true">{{ f.icon }}</span>
        <div>
          <div class="feature-label">{{ f.label }}</div>
          <div class="feature-desc">{{ f.desc }}</div>
        </div>
      </div>
    </div>

    <!-- ── Calendar section ── -->
    <section id="calendario" class="calendar-section animate-up" aria-label="Calendario de siembra">

      <div class="cal-header">
        <div>
          <h2 class="cal-title">Calendario de Siembra</h2>
          <p class="cal-sub">Selecciona tu hemisferio y mes para saber qué hacer en tu huerto</p>
        </div>

        <!-- Hemisphere toggle -->
        <div class="hemi-toggle" role="group" aria-label="Selección de hemisferio">
          <button
            class="hemi-btn"
            :class="{ active: hemisphere === 'NORTE' }"
            @click="hemisphere = 'NORTE'"
            :aria-pressed="hemisphere === 'NORTE'"
          >
            <span aria-hidden="true">🌍</span> Norte
          </button>
          <button
            class="hemi-btn"
            :class="{ active: hemisphere === 'SUR' }"
            @click="hemisphere = 'SUR'"
            :aria-pressed="hemisphere === 'SUR'"
          >
            <span aria-hidden="true">🌏</span> Sur
          </button>
        </div>
      </div>

      <!-- Month pills -->
      <div class="month-grid" role="group" aria-label="Selección de mes">
        <button
          v-for="m in months"
          :key="m.num"
          class="month-pill"
          :class="{ active: selectedMonth === m.num }"
          @click="selectedMonth = m.num"
          :aria-pressed="selectedMonth === m.num"
          :aria-label="`${m.full}${m.num === currentMonth ? ' (mes actual)' : ''}`"
        >
          {{ m.name }}
          <span v-if="m.num === currentMonth" class="today-dot" aria-hidden="true"></span>
        </button>
      </div>

      <!-- Current selection label -->
      <div class="cal-current-label" aria-live="polite">
        <span class="cal-current-dot" aria-hidden="true"></span>
        Mostrando: <strong>{{ months[selectedMonth - 1].full }}</strong> ·
        Hemisferio <strong>{{ hemisphere === 'NORTE' ? 'Norte 🌍' : 'Sur 🌏' }}</strong>
      </div>

      <!-- Plant grid from component -->
      <SeedingCalendar :hemisphere="hemisphere" :month="selectedMonth" />

      <!-- Legend -->
      <div class="cal-legend" role="note" aria-label="Leyenda del calendario">
        <span class="legend-label">Leyenda:</span>
        <span class="legend-badge sembrar">🌱 Sembrar</span>
        <span class="legend-badge trasplantar">🌿 Trasplantar</span>
        <span class="legend-badge cosechar">🌾 Cosechar</span>
      </div>

    </section>
  </div>
</template>

<style scoped>
.home-view {
  max-width: 1100px;
  margin: 0 auto;
}

/* ── Hero ── */
.hero-section {
  position: relative;
  background: linear-gradient(135deg, #065F46 0%, #059669 50%, #10B981 100%);
  border-radius: var(--r-xl);
  padding: 60px 40px;
  text-align: center;
  margin-bottom: 24px;
  overflow: hidden;
  color: white;
}
.hero-bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.hero-orb {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.hero-orb-1 {
  width: 360px; height: 360px;
  top: -120px; right: -80px;
}
.hero-orb-2 {
  width: 200px; height: 200px;
  bottom: -60px; left: -30px;
}
.hero-content { position: relative; z-index: 1; max-width: 620px; margin: 0 auto; }

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: var(--r-full);
  padding: 6px 16px;
  font-size: 0.82rem;
  font-weight: 600;
  margin-bottom: 20px;
  backdrop-filter: blur(4px);
}
.hero-title {
  font-family: var(--font-display) !important;
  font-size: clamp(2rem, 5vw, 3.2rem) !important;
  font-weight: 800 !important;
  line-height: 1.1 !important;
  color: white !important;
  margin-bottom: 16px;
  letter-spacing: -0.03em;
}
.hero-title-accent {
  color: var(--amber-400);
  text-shadow: 0 2px 20px rgba(251,191,36,0.4);
}
.hero-lead {
  font-size: 1.05rem;
  opacity: 0.88;
  line-height: 1.6;
  margin-bottom: 8px;
}
.hero-micro {
  font-size: 0.8rem;
  opacity: 0.6;
  margin-bottom: 28px;
}
.hero-ctas {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
.cta-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: white;
  color: var(--green-700) !important;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.95rem;
  border-radius: var(--r-full);
  text-decoration: none;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  transition: all var(--t-base);
}
.cta-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.2);
}
.cta-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: rgba(255,255,255,0.15);
  color: white !important;
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 0.95rem;
  border-radius: var(--r-full);
  text-decoration: none;
  border: 1.5px solid rgba(255,255,255,0.35);
  cursor: pointer;
  backdrop-filter: blur(4px);
  transition: all var(--t-base);
}
.cta-secondary:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-1px);
}

@media (max-width: 600px) {
  .hero-section { padding: 40px 24px; border-radius: var(--r-lg); }
}

/* ── Feature strip ── */
.features-strip {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.feature-item {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: var(--r-md);
  padding: 16px 18px;
  box-shadow: var(--shadow-xs);
}
.feature-icon {
  font-size: 1.8rem;
  flex-shrink: 0;
}
.feature-label {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.88rem;
  color: var(--text-primary);
}
.feature-desc {
  font-size: 0.78rem;
  color: var(--text-light);
  margin-top: 1px;
}

/* ── Calendar section ── */
.calendar-section {
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: var(--r-xl);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}
.cal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.cal-title {
  font-family: var(--font-display) !important;
  font-size: 1.5rem !important;
  font-weight: 800 !important;
  color: var(--text-primary) !important;
  margin-bottom: 4px;
}
.cal-sub {
  font-size: 0.88rem;
  color: var(--text-muted);
  margin: 0;
}

/* Month grid */
.month-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.cal-current-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.84rem;
  color: var(--text-muted);
  margin-bottom: 24px;
  padding: 8px 14px;
  background: var(--green-50);
  border-radius: var(--r-sm);
  border: 1px solid var(--green-100);
}
.cal-current-dot {
  width: 8px;
  height: 8px;
  background: var(--green-500);
  border-radius: 50%;
  flex-shrink: 0;
  animation: pulse 2s ease-in-out infinite;
}

/* Legend */
.cal-legend {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 28px;
  padding: 14px 18px;
  background: var(--stone-50);
  border-radius: var(--r-md);
  border: 1px solid var(--border-subtle);
}
.legend-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.legend-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border-radius: var(--r-full);
  font-size: 0.84rem;
  font-weight: 600;
  font-family: var(--font-display);
}
.legend-badge.sembrar      { background: var(--green-100); color: var(--green-700); }
.legend-badge.trasplantar  { background: var(--amber-100); color: var(--amber-700); }
.legend-badge.cosechar     { background: #FFF0E0; color: #C05621; }

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.3); opacity: 0.7; }
}

@media (max-width: 600px) {
  .calendar-section { padding: 20px 16px; }
  .cal-header { gap: 12px; }
}
</style>
