<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import PageHeader from '@/components/ui/PageHeader.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PartidaAbiertaCard from '@/components/PartidaAbiertaCard.vue'
import { DEPORTES_ENUM, NIVELES_ENUM } from '@/utils/constants'

const router = useRouter()
const authStore = useAuthStore()

const partidas = ref([])
const cargando = ref(true)
const joiningId = ref(null)

const filtroDeporte = ref('')
const filtroNivel = ref('')
const ocultarLlenas = ref(false)

const deportes = DEPORTES_ENUM
const niveles = NIVELES_ENUM

onMounted(async () => {
  if (!authStore.isLogged()) {
    toast.error('Debes iniciar sesión para ver las partidas abiertas')
    router.push('/login')
    return
  }
  await cargarPartidas()
})

async function cargarPartidas() {
  cargando.value = true
  try {
    const res = await api.get('/reservas/abiertas')
    partidas.value = res.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar las partidas abiertas')
  } finally {
    cargando.value = false
  }
}

async function unirsePartida(partidaId) {
  if (joiningId.value) return
  
  const partida = partidas.value.find(p => p.id === partidaId)
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
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al abandonar la partida')
  } finally {
    joiningId.value = null
  }
}

const partidasFiltradas = computed(() => {
  return partidas.value.filter(p => {
    const pasaDeporte = !filtroDeporte.value || p.deporte === filtroDeporte.value
    const pasaNivel = !filtroNivel.value || p.nivel === filtroNivel.value
    
    const estaLlena = p.participantesIds && p.participantesIds.length >= p.capacidadMaxima
    const pasaOcultar = !ocultarLlenas.value || !estaLlena

    return pasaDeporte && pasaNivel && pasaOcultar
  })
})

function limpiarFiltros() {
  filtroDeporte.value = ''
  filtroNivel.value = ''
  ocultarLlenas.value = false
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <PageHeader 
        title="Partidas Abiertas" 
        description="Encuentra partidos a los que unirte y conoce nuevos compañeros de juego."
      />

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8 mt-6">
        <div class="flex flex-col md:flex-row gap-4 items-end">
          <div class="flex-1 w-full">
            <label class="block text-sm font-medium text-gray-700 mb-2">Deporte</label>
            <select v-model="filtroDeporte" class="w-full rounded-xl border-gray-300 shadow-sm focus:border-primary focus:ring-primary">
              <option value="">Todos los deportes</option>
              <option v-for="d in deportes" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>
          <div class="flex-1 w-full">
            <label class="block text-sm font-medium text-gray-700 mb-2">Nivel</label>
            <select v-model="filtroNivel" class="w-full rounded-xl border-gray-300 shadow-sm focus:border-primary focus:ring-primary">
              <option value="">Todos los niveles</option>
              <option v-for="n in niveles" :key="n" :value="n">{{ n }}</option>
            </select>
          </div>
          <div class="flex items-center h-10 w-full md:w-auto px-2">
            <label class="flex items-center cursor-pointer">
              <input type="checkbox" v-model="ocultarLlenas" class="rounded border-gray-300 text-primary focus:ring-primary w-5 h-5">
              <span class="ml-2 text-sm font-medium text-gray-700 select-none">Ocultar llenas</span>
            </label>
          </div>
          <button @click="limpiarFiltros" class="w-full md:w-auto px-6 py-2.5 bg-gray-100 text-gray-700 font-medium rounded-xl hover:bg-gray-200 transition">
            Limpiar
          </button>
        </div>
      </div>

      <LoadingSpinner v-if="cargando" text="Buscando partidas disponibles..." />

      <template v-else>
        <div v-if="partidasFiltradas.length > 0" class="grid grid-cols-1 gap-6">
          <PartidaAbiertaCard 
            v-for="partida in partidasFiltradas" 
            :key="partida.id" 
            :partida="partida"
            :isJoining="joiningId === partida.id"
            @unirse="unirsePartida"
            @abandonar="abandonarPartida"
          />
        </div>

        <EmptyState 
          v-else 
          icon="fas fa-search"
          title="No hay partidas"
          description="No hemos encontrado ninguna partida abierta con esos filtros. ¡Anímate a organizar tú una!"
        />
      </template>

    </div>
  </div>
</template>
