<script setup>
import { MAPA_DEPORTES } from '@/utils/constants'
import { formatearDinero } from '@/utils/formatters'

defineProps({
  pista: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['toggle-disponibilidad', 'editar', 'eliminar'])
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
</template>
