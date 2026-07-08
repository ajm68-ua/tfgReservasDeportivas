<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import { useAuthStore } from '@/stores/auth'
import CentroDeportivoCard from '@/components/CentroDeportivoCard.vue'
import PartidaAbiertaCard from '@/components/PartidaAbiertaCard.vue'
import { DEPORTES_ARRAY } from '@/utils/constants'
import { toast } from 'vue3-toastify'

const router = useRouter()
const authStore = useAuthStore()
const deportes = DEPORTES_ARRAY
const deporteSeleccionado = ref('')
const ciudadSeleccionada = ref('')

const centros = ref([])
const cargando = ref(true)
const error = ref(null)

const partidasAbiertas = ref([])
const cargandoPartidas = ref(true)

const paginaActual = ref(0)
const centrosPorPagina = 3

const ciudadUsuario = computed(() => authStore.usuario?.ciudad || '')

const centrosFiltrados = computed(() => {
  if (!ciudadUsuario.value) return centros.value
  return centros.value.filter(c => c.ciudad === ciudadUsuario.value)
})

const centrosMostrados = computed(() => {
  const inicio = paginaActual.value * centrosPorPagina
  return centrosFiltrados.value.slice(inicio, inicio + centrosPorPagina)
})

const ciudadesUnicas = computed(() => {
  const ciudades = centros.value.map(c => c.ciudad).filter(Boolean)
  return [...new Set(ciudades)].sort()
})

const hayVariasPaginas = computed(() => centrosFiltrados.value.length > centrosPorPagina)

const tituloCentros = computed(() => {
  if (ciudadUsuario.value) {
    return `Centros deportivos en ${ciudadUsuario.value}`
  }
  return 'Centros deportivos destacados'
})

const partidasDestacadas = computed(() => {
  return partidasAbiertas.value
    .filter(p => {
      const ocupadas = p.participantesIds ? p.participantesIds.length : 0
      return ocupadas < p.capacidadMaxima
    })
    .sort((a, b) => {
      const fechaA = new Date(a.fecha + 'T' + a.horaInicio)
      const fechaB = new Date(b.fecha + 'T' + b.horaInicio)
      return fechaA - fechaB
    })
    .slice(0, 3)
})

function paginaAnterior() {
  if (paginaActual.value > 0) {
    paginaActual.value--
  } else {
    paginaActual.value = Math.max(0, Math.ceil(centrosFiltrados.value.length / centrosPorPagina) - 1)
  }
}
function paginaSiguiente() {
  const maxPagina = Math.max(0, Math.ceil(centrosFiltrados.value.length / centrosPorPagina) - 1)
  if (paginaActual.value < maxPagina) {
    paginaActual.value++
  } else {
    paginaActual.value = 0
  }
}

function buscarPistas() {
  router.push({
    name: 'pistas',
    query: {
      deporte: deporteSeleccionado.value || undefined,
      ciudad: ciudadSeleccionada.value || undefined
    }
  })
}

const joiningId = ref(null)

async function cargarPartidas() {
  try {
    const res = await api.get('/reservas/abiertas')
    partidasAbiertas.value = res.data
  } catch (err) {
    console.error(err)
  }
}

async function unirsePartida(partidaId) {
  if (joiningId.value) return

  const partida = partidasAbiertas.value.find(p => p.id === partidaId)
  let confirmMessage = '¿Quieres unirte a esta partida? Se te cobrará la parte proporcional de la pista de tu saldo.'

  if (partida && authStore.usuario && partida.nivel !== authStore.usuario.nivel) {
    confirmMessage = '¿Estás seguro que quieres unirte? El nivel de juego solicitado no corresponde a tu nivel.\n\nSe te cobrará la parte proporcional de la pista de tu saldo.'
  }

  const confirmacion = confirm(confirmMessage)
  if (!confirmacion) return

  joiningId.value = partidaId
  try {
    await api.post(`/reservas/${partidaId}/unirse?usuarioId=${authStore.usuario.id}`)
    await authStore.refreshUser(api)
    toast.success('¡Te has unido a la partida con éxito!')
    await cargarPartidas()
    window.dispatchEvent(new CustomEvent('reserva-actualizada'))
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al unirse a la partida')
  } finally {
    joiningId.value = null
  }
}

async function abandonarPartida(partidaId) {
  if (joiningId.value) return

  const confirmacion = confirm('¿Quieres abandonar esta partida? Se te devolverá la parte proporcional de la pista a tu saldo.')
  if (!confirmacion) return

  joiningId.value = partidaId
  try {
    await api.post(`/reservas/${partidaId}/abandonar?usuarioId=${authStore.usuario.id}`)
    await authStore.refreshUser(api)
    toast.success('Has abandonado la partida con éxito')
    await cargarPartidas()
    window.dispatchEvent(new CustomEvent('reserva-actualizada'))
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al abandonar la partida')
  } finally {
    joiningId.value = null
  }
}

onMounted(async () => {
  try {
    const [resCentros, resPartidas] = await Promise.all([
      api.get('/centros'),
      api.get('/reservas/abiertas')
    ])
    centros.value = resCentros.data
    partidasAbiertas.value = resPartidas.data
  } catch (err) {
    error.value = 'No se pudieron cargar los datos.'
    console.error(err)
  } finally {
    cargando.value = false
    cargandoPartidas.value = false
  }
})
</script>



<template>
  <div class="min-h-screen bg-white font-sans flex flex-col">
    <section class="bg-gray-900 text-white py-24 px-8">
      <div class="max-w-6xl mx-auto w-full">
        <h1 class="text-5xl md:text-[4rem] font-extrabold tracking-tight mb-2 leading-tight">
          Tu pista lista en un clic.
        </h1>
        <h2 class="text-5xl md:text-[4rem] font-extrabold tracking-tight text-blue-500 mb-6 leading-tight">
          Juega cuando quieras.
        </h2>
        <p class="text-gray-400 text-lg max-w-xl mb-12 leading-relaxed">
          Encuentra pistas deportivas en los mejores centros de tu ciudad. Organiza partidas abiertas o reserva en privado.
        </p>

        <div class="bg-white p-2 rounded-full shadow-lg max-w-3xl flex items-center mb-6">
          <div class="flex-1 flex items-center px-6 border-r border-gray-200 relative">
            <select v-model="deporteSeleccionado" class="w-full bg-transparent text-gray-600 text-sm focus:outline-none cursor-pointer appearance-none py-2">
              <option value="" disabled selected>Selecciona deporte</option>
              <option v-for="deporte in deportes" :key="deporte" :value="deporte">{{ deporte }}</option>
            </select>
            <svg class="w-4 h-4 text-gray-500 absolute right-6 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </div>

          <div class="flex-1 flex items-center px-6 relative">
            <svg class="w-5 h-5 text-gray-400 mr-3 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <select v-model="ciudadSeleccionada" class="w-full bg-transparent text-gray-600 text-sm focus:outline-none cursor-pointer appearance-none py-2">
              <option value="" disabled selected>Selecciona ciudad</option>
              <option v-for="ciudad in ciudadesUnicas" :key="ciudad" :value="ciudad">{{ ciudad }}</option>
            </select>
            <svg class="w-4 h-4 text-gray-500 absolute right-6 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </div>

          <button @click="buscarPistas" class="bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm py-3 px-8 rounded-full transition flex items-center gap-2 whitespace-nowrap">
            Buscar Pistas
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
            </svg>
          </button>
        </div>
      </div>
    </section>

    <section class="py-16 px-8 bg-white">
      <div class="max-w-6xl mx-auto w-full">
        <div class="bg-gray-100 rounded-3xl py-10 px-12 md:px-20 relative">
          <h3 class="text-2xl md:text-3xl font-extrabold text-gray-900 mb-8">
            {{ tituloCentros }}
          </h3>

          <div v-if="cargando" class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div
              v-for="n in 3"
              :key="n"
              class="bg-white rounded-[1rem] h-72 animate-pulse border border-gray-100"
            ></div>
          </div>

          <div v-else-if="error" class="text-center py-12 text-gray-500">
            <svg class="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <p>{{ error }}</p>
          </div>
          <div v-else-if="centrosFiltrados.length === 0" class="text-center py-12 text-gray-500">
            <svg class="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
            <p class="font-semibold text-gray-700 mb-1">No hay centros en tu zona</p>
            <p class="text-sm">Configura tu ciudad en tu perfil o explora todos los centros disponibles.</p>
            <router-link to="/centros" class="inline-block mt-4 text-sm font-semibold text-blue-600 hover:text-blue-800 transition">
              Ver todos los centros
            </router-link>
          </div>

          <div v-else class="relative flex items-center">
            <button
              v-if="hayVariasPaginas"
              @click="paginaAnterior"
              class="absolute -left-8 md:-left-12 w-12 h-12 bg-white rounded-full shadow-md flex items-center justify-center text-black font-extrabold text-xl hover:bg-gray-50 transition z-10"
            >
              &lt;
            </button>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 w-full">
              <CentroDeportivoCard
                v-for="centro in centrosMostrados"
                :key="centro.id"
                :centro="centro"
              />
            </div>

            <button
              v-if="hayVariasPaginas"
              @click="paginaSiguiente"
              class="absolute -right-8 md:-right-12 w-12 h-12 bg-white rounded-full shadow-md flex items-center justify-center text-black font-extrabold text-xl hover:bg-gray-50 transition z-10"
            >
              &gt;
            </button>
          </div>

          <div v-if="centrosFiltrados.length > 0" class="text-center mt-8">
            <router-link to="/centros" class="text-sm font-semibold text-blue-600 hover:text-blue-800 transition">
              Ver todos los centros →
            </router-link>
          </div>
        </div>
      </div>
    </section>
    <section v-if="!cargandoPartidas && partidasDestacadas.length > 0" class="py-16 px-8 bg-gray-50">
      <div class="max-w-6xl mx-auto w-full">
        <div class="flex items-end justify-between mb-8">
          <div>
            <h3 class="text-2xl md:text-3xl font-extrabold text-gray-900 mb-2">
              Partidas que buscan jugadores
            </h3>
            <p class="text-gray-500 text-sm">
              Hay hueco en estas partidas. Únete y completa el grupo.
            </p>
          </div>
          <router-link to="/partidas" class="hidden md:block text-sm font-semibold text-blue-600 hover:text-blue-800 transition whitespace-nowrap">
            Ver todas →
          </router-link>
        </div>

        <div class="grid grid-cols-1 gap-6">
          <PartidaAbiertaCard
            v-for="partida in partidasDestacadas"
            :key="partida.id"
            :partida="partida"
            :isJoining="joiningId === partida.id"
            @unirse="unirsePartida"
            @abandonar="abandonarPartida"
          />
        </div>

        <div class="text-center mt-6 md:hidden">
          <router-link to="/partidas" class="text-sm font-semibold text-blue-600 hover:text-blue-800 transition">
            Ver todas las partidas →
          </router-link>
        </div>
      </div>
    </section>

    <section class="py-20 px-8 bg-white">
      <div class="max-w-5xl mx-auto w-full">
        <h3 class="text-2xl md:text-3xl font-extrabold text-gray-900 text-center mb-4">
          Reserva en tres pasos
        </h3>
        <p class="text-gray-500 text-center text-sm mb-14 max-w-lg mx-auto">
          Sin complicaciones. Elige, reserva y juega.
        </p>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div class="text-center">
            <div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <svg class="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h4 class="font-bold text-gray-900 mb-2">Busca tu pista</h4>
            <p class="text-sm text-gray-500 leading-relaxed">
              Filtra por deporte, ciudad o centro deportivo y encuentra la pista que mejor se adapte a ti.
            </p>
          </div>

          <div class="text-center">
            <div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <svg class="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <h4 class="font-bold text-gray-900 mb-2">Reserva al instante</h4>
            <p class="text-sm text-gray-500 leading-relaxed">
              Elige el día y la hora que más te convenga. La reserva se confirma en el momento.
            </p>
          </div>

          <div class="text-center">
            <div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <svg class="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <h4 class="font-bold text-gray-900 mb-2">Juega con quien quieras</h4>
            <p class="text-sm text-gray-500 leading-relaxed">
              Crea una partida privada con tus amigos o únete a una abierta y conoce gente nueva.
            </p>
          </div>
        </div>
      </div>
    </section>

    <section class="py-20 px-8 bg-gray-900">
      <div class="max-w-3xl mx-auto text-center">
        <h3 class="text-3xl md:text-4xl font-extrabold text-white mb-4">
          ¿Listo para tu próxima partida?
        </h3>
        <p class="text-gray-400 text-base mb-8 max-w-md mx-auto leading-relaxed">
          Explora las pistas disponibles en tu zona y empieza a jugar hoy mismo.
        </p>
        <router-link to="/pistas">
          <button class="bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm py-3.5 px-10 rounded-full transition">
            Buscar pistas
          </button>
        </router-link>
      </div>
    </section>

  </div>
</template>
