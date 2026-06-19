<script setup>
defineProps({
  titulo: {
    type: String,
    default: 'No hemos encontrado resultados'
  },
  descripcion: {
    type: String,
    required: true
  },
  textoBoton: {
    type: String,
    default: 'Limpiar todos los filtros'
  }
})

const emit = defineEmits(['action'])
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-100 p-16 text-center shadow-sm">
    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
      <slot name="icon">
        <svg class="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
      </slot>
    </div>
    <h3 class="text-xl font-bold text-gray-900 mb-2">{{ titulo }}</h3>
    <p class="text-gray-500 mb-6 max-w-sm mx-auto">{{ descripcion }}</p>
    <button @click="emit('action')" class="px-6 py-2.5 bg-gray-900 text-white rounded-full font-semibold text-sm hover:bg-gray-800 transition shadow-md hover:shadow-lg">
      {{ textoBoton }}
    </button>
  </div>
</template>
