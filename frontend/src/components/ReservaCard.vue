<script setup>
defineProps({
  reserva: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['pagar', 'editar', 'cancelar', 're-reservar', 'eliminar'])

function formatearFecha(fechaStr) {
  const [year, month, day] = fechaStr.split('-')
  return `${day}/${month}/${year}`
}

function formatearHora(horaStr) {
  return horaStr.substring(0, 5)
}

function etiquetaEstado(estado) {
  switch (estado) {
    case 'PENDIENTE': return 'bg-yellow-100 text-yellow-800'
    case 'PAGADO': return 'bg-green-100 text-green-700'
    case 'CANCELADO': return 'bg-red-100 text-red-700'
    default: return 'bg-gray-100 text-gray-800'
  }
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col transition hover:shadow-md">
    <div class="p-6 flex-1 flex flex-col">
      <div class="flex justify-between items-start mb-4">
        <div>
          <h3 class="text-xl font-bold text-gray-900 truncate pr-4">{{ reserva.nombrePista }}</h3>
          <p class="text-gray-500 text-sm font-medium mt-1">{{ reserva.nombreCentro }}</p>
        </div>
        <span 
          class="px-2.5 py-1 rounded-md text-xs font-bold uppercase tracking-wider whitespace-nowrap"
          :class="etiquetaEstado(reserva.estadoPago)"
        >
          {{ reserva.estadoPago }}
        </span>
      </div>
      
      <div class="space-y-2 mb-6 flex-1">
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Nivel:</span>
          <span class="font-bold text-gray-900">{{ reserva.nivel }}</span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Tipo:</span>
          <span class="font-bold text-gray-900">
            {{ reserva.esAbierta ? 'Partida Abierta' : 'Partida Privada' }}
          </span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Fecha:</span>
          <span class="font-bold text-gray-900">{{ formatearFecha(reserva.fecha) }}</span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between">
          <span class="font-medium">Horario:</span>
          <span class="font-bold text-gray-900">{{ formatearHora(reserva.horaInicio) }} - {{ formatearHora(reserva.horaFin) }}</span>
        </p>
        <p class="text-sm text-gray-600 flex justify-between mt-2 pt-2 border-t border-gray-100">
          <span class="font-medium">Total:</span>
          <span class="font-bold text-gray-900 text-base">{{ reserva.precioTotal }}€</span>
        </p>
      </div>

      <div class="pt-4 border-t border-gray-100 flex flex-col gap-2.5">
        <button 
          v-if="reserva.estadoPago === 'PENDIENTE'"
          @click="emit('pagar', reserva.id)"
          class="w-full py-2.5 bg-green-600 text-white rounded-xl text-sm font-semibold hover:bg-green-700 transition shadow-md hover:shadow-lg"
        >
          Pagar Ahora
        </button>
        
        <div v-if="reserva.estadoPago !== 'CANCELADO'" class="flex gap-2.5">
          <button 
            @click="emit('editar', reserva.id)"
            class="flex-1 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-semibold hover:bg-gray-800 transition shadow-md hover:shadow-lg"
          >
            Modificar
          </button>
          <button 
            @click="emit('cancelar', reserva.id)"
            class="flex-1 py-2.5 bg-red-600 text-white rounded-xl text-sm font-semibold hover:bg-red-700 transition shadow-md hover:shadow-lg"
          >
            Cancelar
          </button>
        </div>

        <div v-if="reserva.estadoPago === 'CANCELADO'" class="flex gap-2.5">
          <button 
            @click="emit('re-reservar', reserva)"
            class="flex-1 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 transition shadow-md hover:shadow-lg"
          >
            Volver a reservar
          </button>
          <button 
            @click="emit('eliminar', reserva.id)"
            class="flex-1 py-2.5 bg-red-600 text-white rounded-xl text-sm font-semibold hover:bg-red-700 transition shadow-md hover:shadow-lg"
          >
            Eliminar 
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
