<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { obtenerIniciales, formatearFecha, formatearHora, formatearDinero } from '@/utils/formatters'

const props = defineProps({
  partida: {
    type: Object,
    required: true
  },
  isJoining: {
    type: Boolean,
    default: false
  }
})

const authStore = useAuthStore()

const emit = defineEmits(['unirse', 'abandonar'])


const isJoined = computed(() => {
  if (!authStore.usuario || !props.partida.participantesIds) return false
  return props.partida.participantesIds.includes(authStore.usuario.id)
})

const estaLlena = computed(() => {
  return props.partida.participantesIds && props.partida.participantesIds.length >= props.partida.capacidadMaxima
})

const plazasOcupadas = computed(() => {
  return props.partida.participantesIds ? props.partida.participantesIds.length : 0
})

const porcentajeLlenado = computed(() => {
  if (!props.partida.capacidadMaxima) return 0
  return Math.min(100, Math.round((plazasOcupadas.value / props.partida.capacidadMaxima) * 100))
})

const barraColor = computed(() => {
  if (estaLlena.value) return 'bg-red-500'
  if (porcentajeLlenado.value >= 75) return 'bg-orange-500'
  return 'bg-green-500'
})

</script>

<template>
  <div class="flex flex-col sm:flex-row bg-white rounded-2xl shadow-sm hover:shadow-md transition-shadow overflow-hidden border border-gray-100 group" :class="{'opacity-60': estaLlena}">
    
    <div class="w-full sm:w-56 h-48 sm:h-auto flex-shrink-0 relative overflow-hidden bg-gray-100">
      <img 
        v-if="partida.pistaFoto || partida.centroFoto" 
        :src="partida.pistaFoto || partida.centroFoto" 
        class="w-full h-full object-cover" 
        alt="Foto Centro" 
      />
      <div v-else class="w-full h-full flex flex-col items-center justify-center text-gray-400">
        <svg class="w-10 h-10 mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
        <span class="text-xs font-semibold">Sin imagen</span>
      </div>
    </div>

    <div class="p-6 flex-grow flex flex-col justify-between">
      <div>
        <div class="flex items-center gap-2 mb-3">
          <span class="px-2.5 py-1 bg-gray-100 text-gray-700 rounded-md text-[10px] font-bold uppercase tracking-wider">
            Nivel: {{ partida.nivel }}
          </span>
          <span 
            class="px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider ml-auto"
            :class="estaLlena ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'"
          >
            {{ estaLlena ? 'COMPLETA' : 'ABIERTA' }}
          </span>
        </div>
        
        <h3 class="text-xl font-bold text-gray-900 mb-1 leading-tight">{{ partida.nombreCentro }}</h3>
        <p class="text-sm font-semibold text-gray-600 mb-4 flex items-center gap-1.5">
          {{ partida.nombrePista }}
        </p>
        
        <div class="grid grid-cols-2 gap-4 text-sm text-gray-600 mb-4">
          <div class="flex items-center gap-2">
            <i class="far fa-calendar text-gray-400 w-4 text-center"></i>
            <span>Fecha: <span class="font-bold text-gray-900">{{ formatearFecha(partida.fecha) }}</span></span>
          </div>
          <div class="flex items-center gap-2">
            <i class="far fa-clock text-gray-400 w-4 text-center"></i>
            <span>Hora: <span class="font-bold text-gray-900">{{ formatearHora(partida.horaInicio) }} - {{ formatearHora(partida.horaFin) }}</span></span>
          </div>
          <div class="flex items-center gap-2 col-span-2">
            <i class="far fa-user text-gray-400 w-4 text-center"></i>
            <span>Organiza: <span class="font-bold text-gray-900">{{ partida.nombreOrganizador }}</span></span>
          </div>
        </div>

        <div class="mt-4 pt-4 border-t border-gray-100">
          <div class="flex justify-between items-center mb-1">
            <span class="text-xs font-medium text-gray-500 uppercase tracking-wider">Jugadores ({{ plazasOcupadas }} / {{ partida.capacidadMaxima }})</span>
          </div>
          <div v-if="partida.jugadoresDetalle && partida.jugadoresDetalle.length > 0" class="flex -space-x-2 mb-2">
            <template v-for="jugador in partida.jugadoresDetalle" :key="jugador.id">
              <router-link :to="'/usuario/' + jugador.id" class="relative hover:-translate-y-1 hover:z-10 hover:scale-110 transition-transform duration-200">
                <img v-if="jugador.foto" :src="jugador.foto" class="w-8 h-8 rounded-full border-2 border-white object-cover shadow-sm" :title="jugador.nombre" />
                <div v-else class="w-8 h-8 rounded-full border-2 border-white shadow-sm bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold" :title="jugador.nombre + ' ' + (jugador.apellidos || '')">{{ obtenerIniciales(jugador.nombre, jugador.apellidos) }}</div>
              </router-link>
            </template>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-1.5">
            <div class="h-1.5 rounded-full transition-all duration-500" :class="barraColor" :style="`width: ${porcentajeLlenado}%`"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="p-6 bg-gray-50 sm:w-56 flex flex-col items-center justify-center border-t sm:border-t-0 sm:border-l border-gray-100 text-center">
      <div class="mb-4">
        <span class="text-3xl font-extrabold text-gray-900">{{ formatearDinero(partida.precioTotal / partida.capacidadMaxima) }}</span>
        <span class="block text-[10px] text-gray-500 uppercase font-bold tracking-widest mt-0.5">/ persona</span>
      </div>
      
      <div class="flex flex-col gap-2 w-full mt-auto">
        <button 
          v-if="isJoined"
          @click="emit('abandonar', partida.id)"
          :disabled="isJoining"
          class="w-full px-5 py-2.5 bg-red-600 text-white rounded-lg text-sm font-semibold hover:bg-red-700 transition shadow-sm hover:shadow-md block disabled:opacity-50 flex justify-center items-center gap-2"
        >
          <span v-if="isJoining" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          <i v-else class="fas fa-sign-out-alt"></i>
          Abandonar
        </button>
        <button 
          v-else-if="!estaLlena"
          @click="emit('unirse', partida.id)"
          :disabled="isJoining"
          class="w-full px-5 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition shadow-sm hover:shadow-md block disabled:opacity-50 flex justify-center items-center gap-2"
        >
          <span v-if="isJoining" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          <i v-else class="fas fa-plus"></i>
          Unirse
        </button>
        <button 
          v-else
          disabled
          class="w-full px-5 py-2.5 bg-gray-200 text-gray-500 rounded-lg text-sm font-semibold flex justify-center items-center gap-2 cursor-not-allowed border border-gray-300"
        >
          <i class="fas fa-lock"></i>
          Completa
        </button>
      </div>
    </div>
    
  </div>
</template>
