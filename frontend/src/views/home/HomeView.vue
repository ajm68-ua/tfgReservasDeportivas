<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import CentroDeportivoCard from '@/components/CentroDeportivoCard.vue'

const router = useRouter()
const deportes = ['Pádel', 'Tenis', 'Fútbol', 'Baloncesto', 'Squash', 'Bádminton']
const deporteSeleccionado = ref('')
const fechaSeleccionada = ref('')
const hoy = new Date().toISOString().split('T')[0]

const centros = ref([])
const cargando = ref(true)
const error = ref(null)

const paginaActual = ref(0)
const centrosPorPagina = 3

const centrosMostrados = computed(() => {
  const inicio = paginaActual.value * centrosPorPagina
  return centros.value.slice(inicio, inicio + centrosPorPagina)
})

const hayVariasPaginas = computed(() => centros.value.length > centrosPorPagina)

function paginaAnterior() {
  if (paginaActual.value > 0) {
    paginaActual.value--
  } else {
    paginaActual.value = Math.max(0, Math.ceil(centros.value.length / centrosPorPagina) - 1)
  }
}
function paginaSiguiente() {
  const maxPagina = Math.max(0, Math.ceil(centros.value.length / centrosPorPagina) - 1)
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
      fecha: fechaSeleccionada.value || undefined
    }
  })
}

onMounted(async () => {
  try {
    const response = await api.get('/centros')
    centros.value = response.data
  } catch (err) {
    error.value = 'No se pudieron cargar los centros deportivos.'
    console.error(err)
  } finally {
    cargando.value = false
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

          <div class="flex-1 flex items-center px-6 text-gray-400">
            <svg class="w-5 h-5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <input
              type="date"
              v-model="fechaSeleccionada"
              :min="hoy"
              class="bg-transparent text-gray-600 text-sm focus:outline-none cursor-pointer w-full"
            />
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

    <section class="py-16 px-8 flex-grow bg-white relative">
      <div class="max-w-6xl mx-auto w-full">
        <div class="bg-gray-100 rounded-3xl py-10 px-12 md:px-20 relative">
          <h3 class="text-2xl md:text-3xl font-extrabold text-gray-900 mb-8">
            Descubre los centros deportivos de tu ciudad
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
        </div>
      </div>
    </section>

  </div>
</template>
