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
    <h3 class="text-xl font-bold text-gray-900 mb-2">{{ titulo }}</h3>
    <p class="text-gray-500 mb-6 max-w-sm mx-auto">{{ descripcion }}</p>
    <button @click="emit('action')" class="px-6 py-2.5 bg-gray-900 text-white rounded-full font-semibold text-sm hover:bg-gray-800 transition shadow-md hover:shadow-lg">
      {{ textoBoton }}
    </button>
  </div>
</template>
