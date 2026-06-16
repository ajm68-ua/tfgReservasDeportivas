<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  pista: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div class="flex flex-col sm:flex-row bg-white rounded-2xl shadow-sm hover:shadow-md transition-shadow overflow-hidden border border-gray-100 group">
    <div class="w-full sm:w-56 h-48 sm:h-auto flex-shrink-0 relative overflow-hidden bg-gray-100">
      <img 
        v-if="pista.centroFoto" 
        :src="pista.centroFoto" 
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
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
          <span class="px-2.5 py-1 bg-blue-50 text-blue-700 rounded-md text-[10px] font-bold uppercase tracking-wider">{{ pista.deporte }}</span>
          <span v-if="pista.disponible" class="px-2.5 py-1 bg-green-50 text-green-700 rounded-md text-[10px] font-bold uppercase tracking-wider flex items-center gap-1">
            <span class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
            Disponible
          </span>
          <span v-else class="px-2.5 py-1 bg-red-50 text-red-700 rounded-md text-[10px] font-bold uppercase tracking-wider">No Disponible</span>
        </div>
        
        <h3 class="text-xl font-bold text-gray-900 mb-1 leading-tight">{{ pista.nombre }}</h3>
        <p class="text-sm font-semibold text-gray-600 mb-4 flex items-center gap-1.5">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
          {{ pista.centroNombre }}
        </p>
        
        <p class="text-sm text-gray-500 flex items-center gap-1.5">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          {{ pista.centroCiudad }}
        </p>
      </div>
    </div>

    <div class="p-6 bg-gray-50 sm:w-52 flex flex-col items-center justify-center border-t sm:border-t-0 sm:border-l border-gray-100 text-center">
      <div class="mb-4">
        <span class="text-3xl font-extrabold text-gray-900">{{ pista.precioPorHora }}€</span>
        <span class="block text-[10px] text-gray-500 uppercase font-bold tracking-widest mt-0.5">/ hora</span>
      </div>
      <div class="flex flex-col gap-2 w-full">
        <router-link :to="`/reservas/nueva?pistaId=${pista.id}`" class="w-full px-5 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition shadow-sm hover:shadow-md block">
          Reservar
        </router-link>
        <router-link :to="`/centros/${pista.centroId}`" class="w-full px-5 py-2.5 bg-blue-50 text-blue-700 rounded-lg text-sm font-semibold hover:bg-blue-100 transition block border border-blue-100">
          Ver Centro
        </router-link>
      </div>
    </div>
  </div>
</template>
