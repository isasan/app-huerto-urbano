<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useWeatherStore } from '@/stores/weather'
import { useAuthStore } from '@/stores/auth'

const weatherStore = useWeatherStore()
const authStore = useAuthStore()

const REFRESH_MS = 30 * 60 * 1000
let timer = null

function load() {
  weatherStore.fetchWeather(authStore.user?.city || undefined)
}

onMounted(() => {
  load()
  timer = setInterval(load, REFRESH_MS)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="weather-widget" role="region" aria-label="Clima actual">
    <!-- Loading -->
    <div v-if="weatherStore.loading" aria-busy="true" aria-label="Cargando clima">
      <div class="w-skel-row">
        <div class="skel" style="width:44px;height:44px;border-radius:10px"></div>
        <div style="flex:1">
          <div class="skel" style="width:60%;height:22px;margin-bottom:6px"></div>
          <div class="skel" style="width:80%;height:14px"></div>
        </div>
      </div>
      <div class="skel" style="width:70%;height:13px;margin-top:8px"></div>
    </div>

    <!-- Error -->
    <div v-else-if="weatherStore.error" class="weather-error" role="alert">
      <i class="bi bi-exclamation-triangle-fill" aria-hidden="true"></i>
      <span>{{ weatherStore.error }}</span>
    </div>

    <!-- Data -->
    <div v-else-if="weatherStore.weather">
      <div class="weather-main">
        <div class="weather-icon-wrap" aria-hidden="true">
          <i :class="`bi ${weatherStore.weather.icon}`"></i>
        </div>
        <div class="weather-temps">
          <div class="weather-temp" aria-label="Temperatura">
            {{ Math.round(weatherStore.weather.temperature) }}<span class="temp-deg">°C</span>
          </div>
          <div class="weather-desc" aria-label="Descripción">
            {{ weatherStore.weather.description }}
          </div>
        </div>
      </div>

      <div class="weather-location">
        <i class="bi bi-geo-alt-fill" aria-hidden="true"></i>
        <span>{{ weatherStore.weather.city }}</span>
      </div>

      <div class="weather-details" role="list">
        <div class="weather-detail" role="listitem" :aria-label="`Sensación térmica ${Math.round(weatherStore.weather.feelsLike)} grados`">
          <i class="bi bi-thermometer-half" aria-hidden="true"></i>
          <span>Sensación {{ Math.round(weatherStore.weather.feelsLike) }}°C</span>
        </div>
        <div class="weather-detail" role="listitem" :aria-label="`Humedad ${weatherStore.weather.humidity} por ciento`">
          <i class="bi bi-droplet-half" aria-hidden="true"></i>
          <span>{{ weatherStore.weather.humidity }}%</span>
        </div>
        <div class="weather-detail" role="listitem" :aria-label="`Viento ${weatherStore.weather.windSpeed} kilómetros por hora`">
          <i class="bi bi-wind" aria-hidden="true"></i>
          <span>{{ weatherStore.weather.windSpeed }} km/h</span>
        </div>
      </div>
    </div>

    <!-- No city configured -->
    <div v-else class="weather-empty">
      <div class="weather-empty-icon" aria-hidden="true">🌍</div>
      <p>
        Configura tu ciudad en
        <RouterLink to="/profile" class="weather-link" aria-label="Ir a tu perfil para configurar la ciudad">
          tu perfil
        </RouterLink>
        para ver el clima
      </p>
    </div>
  </div>
</template>

<style scoped>
.weather-widget {
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: var(--r-md);
  padding: 16px 18px;
  box-shadow: var(--shadow-sm);
  min-width: 180px;
  animation: fadeInUp var(--t-slow) both;
}

.w-skel-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Main weather row */
.weather-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.weather-icon-wrap {
  width: 48px;
  height: 48px;
  background: var(--amber-50);
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.7rem;
  color: var(--amber-600);
  flex-shrink: 0;
}
.weather-temp {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.7rem;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -0.03em;
}
.temp-deg {
  font-size: 1rem;
  font-weight: 600;
  opacity: 0.7;
}
.weather-desc {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin-top: 3px;
  text-transform: capitalize;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.8rem;
  color: var(--text-light);
  margin-bottom: 8px;
  font-weight: 600;
}
.weather-location i { color: var(--green-500); }

.weather-details {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.weather-detail {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.76rem;
  color: var(--text-light);
  background: var(--stone-100);
  border-radius: var(--r-full);
  padding: 3px 9px;
}
.weather-detail i { opacity: 0.7; }

.weather-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  color: var(--red-600);
}

.weather-empty {
  text-align: center;
}
.weather-empty-icon {
  font-size: 2rem;
  opacity: 0.3;
  margin-bottom: 6px;
}
.weather-empty p {
  font-size: 0.82rem;
  color: var(--text-muted);
  margin: 0;
}
.weather-link {
  color: var(--green-600);
  font-weight: 700;
  text-decoration: none;
}
.weather-link:hover {
  text-decoration: underline;
}
</style>
