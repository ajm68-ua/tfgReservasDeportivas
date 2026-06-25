<script setup>
import { useRouter, useRoute } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()
const route = useRoute()
const reservaId = route.params.id

</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    <PageHeader 
      title="Paga tu reserva" 
      :subtitle="'Paga de forma segura y rápida tu reserva'" 
    />
    
    <div class="max-w-2xl mx-auto px-6 lg:px-8 py-20 w-full flex-grow flex flex-col items-center justify-center text-center">
      
    </div>
  </div>
</template>
