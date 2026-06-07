<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  centro: {
    type: Object,
    required: true
  }
})

//Imagen por defecto
const imagenFallback = 'https://images.unsplash.com/photo-1593079831268-3381b0db4a77?q=80&w=800&auto=format&fit=crop'

function getImagen(foto) {
  return foto && foto.trim() !== '' ? foto : imagenFallback
}
</script>

<template>
  <div class="bg-white rounded-[1rem] shadow-sm overflow-hidden flex flex-col border border-gray-100 hover:shadow-lg transition-shadow duration-300">
    <div class="h-48 w-full overflow-hidden bg-gray-200">
      <img
        :src="getImagen(centro.foto)"
        :alt="centro.nombre"
        class="w-full h-full object-cover transition-transform duration-500 hover:scale-105"
        loading="lazy"
      />
    </div>

    <div class="p-4 flex flex-col flex-grow">
      <h4 class="font-extrabold text-base text-black mb-1">{{ centro.nombre }}</h4>

      <div class="flex items-center gap-1 text-gray-400 text-xs mb-3">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        <span>{{ centro.ciudad }}</span>
      </div>

      <div class="flex items-center gap-1 text-gray-400 text-xs mb-4">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span v-if="centro.horarioApertura && centro.horarioCierre">
          {{ centro.horarioApertura.substring(0, 5) }} – {{ centro.horarioCierre.substring(0, 5) }}
        </span>
        <span v-else>Horario no disponible</span>
      </div>

      <RouterLink :to="`/centros/${centro.id}`" class="mt-auto">
        <button class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-lg transition-colors text-sm">
          Ver disponibilidad
        </button>
      </RouterLink>
    </div>
  </div>
</template>
