<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const centroId = route.params.id
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-4">Detalle del Centro</h1>
      <p class="text-gray-500">Centro ID: {{ centroId }}</p>
    </div>
  </div>
</template>
