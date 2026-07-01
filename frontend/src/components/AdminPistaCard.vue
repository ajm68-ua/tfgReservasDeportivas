<script setup>
import { ref, watch } from 'vue'
import { MAPA_DEPORTES } from '@/utils/constants'
import { formatearDinero } from '@/utils/formatters'
import api from '@/services/api'
import ReservaFormulario from '@/components/ReservaFormulario.vue'

const props = defineProps({
  pista: {
    type: Object,
    required: true
  },
  centro: {
    type: Object,
    required: true
  },
  reservasHoy: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['toggle-disponibilidad', 'editar', 'eliminar'])

const mostrarCalendario = ref(false)
const reservasPista = ref([...props.reservasHoy])

let fechaSeleccionada = new Date().toISOString().split('T')[0]

watch(() => props.reservasHoy, (newVal) => {
  const hoy = new Date().toISOString().split('T')[0]
  if (fechaSeleccionada === hoy) {
    reservasPista.value = [...newVal]
  }
}, { deep: true })

async function cargarReservasPorFecha(nuevaFecha) {
  fechaSeleccionada = nuevaFecha
  try {
    const res = await api.get(`/reservas/pista/${props.pista.id}?fecha=${nuevaFecha}`)
    reservasPista.value = res.data
  } catch (err) {
    console.error('Error al cargar reservas de la pista:', err)
  }
}
</script>

<template>
  <div 
    class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col transition hover:shadow-md"
    :class="{'opacity-75 grayscale-[20%]': !pista.disponible}"
  >
    <div class="p-6 flex-1 flex flex-col">
      <div class="flex justify-between items-start mb-4">
        <h3 class="text-xl font-bold text-gray-900 truncate pr-4">{{ pista.nombre }}</h3>
        <span 
          class="px-2.5 py-1 rounded-md text-xs font-bold uppercase tracking-wider whitespace-nowrap"
          :class="pista.disponible ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
        >
          {{ pista.disponible ? 'Disponible' : 'No Disponible' }}
        </span>
      </div>
      
      <div class="space-y-2 mb-6 flex-1">
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Deporte:</span>
          <span>{{ MAPA_DEPORTES[pista.deporte] || pista.deporte }}</span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Precio/hora:</span>
          <span class="font-bold text-blue-600">{{ formatearDinero(pista.precioPorHora) }}</span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Capacidad:</span>
          <span>{{ pista.capacidadMaxima }} personas</span>
        </p>
      </div>

      <div class="mb-4">
        <button 
          @click="mostrarCalendario = true"
          class="w-full py-2 bg-blue-50 text-blue-600 border border-blue-200 rounded-lg text-sm font-semibold hover:bg-blue-100 transition flex items-center justify-center gap-2"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
          Ver Ocupación
        </button>
      </div>

      <div class="pt-4 border-t border-gray-100 flex flex-col gap-2.5">
        <button 
          @click="emit('toggle-disponibilidad', pista)"
          class="w-full py-2.5 rounded-xl text-sm font-semibold transition shadow-sm border"
          :class="pista.disponible ? 'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100' : 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100'"
        >
          {{ pista.disponible ? 'Marcar como No Disponible' : 'Marcar como Disponible' }}
        </button>
        <div class="flex gap-2.5">
          <button 
            @click="emit('editar', pista)"
            class="flex-1 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-semibold hover:bg-gray-800 transition shadow-md hover:shadow-lg"
          >
            Editar Pista
          </button>
          <button 
            @click="emit('eliminar', pista.id)"
            class="flex-1 py-2.5 bg-red-600 text-white rounded-xl text-sm font-semibold hover:bg-red-700 transition shadow-md hover:shadow-lg"
          >
            Eliminar
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="mostrarCalendario" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="fixed inset-0 bg-gray-900/50 backdrop-blur-sm" @click="mostrarCalendario = false"></div>
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] z-10 flex flex-col animate-fade-in-down overflow-hidden">
      <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
        <h3 class="text-xl font-bold text-gray-900 flex items-center gap-2">
          <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
          Ocupación: {{ pista.nombre }}
        </h3>
        <button @click="mostrarCalendario = false" class="text-gray-400 hover:text-gray-600 transition bg-white rounded-full p-1 shadow-sm border border-gray-100">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>
      <div class="p-6 overflow-y-auto flex-1">
        <ReservaFormulario 
          :pista="pista" 
          :centro="centro" 
          :reservasDelDia="reservasPista" 
          @fecha-changed="cargarReservasPorFecha"
          modoAdmin 
        />
      </div>
    </div>
  </div>
</template>
