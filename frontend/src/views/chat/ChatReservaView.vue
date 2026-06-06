<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const reservaId = route.params.id
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-4">Chat de la Partida</h1>
      <p class="text-gray-500">Reserva ID: {{ reservaId }}</p>
    </div>
  </div>
</template>
