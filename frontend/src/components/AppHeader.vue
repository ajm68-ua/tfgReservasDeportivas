<script setup>
import { RouterLink } from 'vue-router'
import logoLfg from '@/assets/images/Logo LFGHub.png'
</script>

<template>
  <header class="flex items-center justify-between px-8 py-4 bg-white border-b border-gray-100 sticky top-0 z-50">
    <div class="w-1/4 flex items-center">
      <RouterLink to="/">
        <img :src="logoLfg" alt="LFG Hub Logo" class="h-12 object-contain cursor-pointer" />
      </RouterLink>
    </div>

    <nav class="hidden md:flex flex-1 justify-center gap-8 text-sm font-semibold text-gray-500">
      <RouterLink to="/pistas" class="hover:text-gray-900 transition-colors">Buscar Pistas</RouterLink>
      <RouterLink to="/partidas" class="hover:text-gray-900 transition-colors">Partidas Abiertas</RouterLink>
      <RouterLink to="/centros" class="hover:text-gray-900 transition-colors">Centros Deportivos</RouterLink>
    </nav>

    <div class="w-1/4 flex items-center justify-end gap-3">
      <RouterLink to="/login">
        <button class="px-5 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-md hover:bg-gray-50 transition-colors">
          Iniciar Sesión
        </button>
      </RouterLink>
      <RouterLink to="/registro">
        <button class="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors">
          Registrarse
        </button>
      </RouterLink>
    </div>
  </header>
</template>
