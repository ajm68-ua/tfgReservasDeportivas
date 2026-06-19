<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import PistaCard from '@/components/PistaCard.vue'

const route = useRoute()
const router = useRouter()
const centroId = route.params.id

const centro = ref(null)
const pistasDelCentro = ref([])
const cargando = ref(true)

const imagenFallback = 'https://images.unsplash.com/photo-1593079831268-3381b0db4a77?q=80&w=1200&auto=format&fit=crop'

const mapaDeportes = {
  'PADEL': 'Pádel',
  'TENIS': 'Tenis',
  'FUTBOL': 'Fútbol',
  'BALONCESTO': 'Baloncesto',
  'SQUASH': 'Squash',
  'BADMINTON': 'Bádminton'
}

onMounted(async () => {
  try {
    const [resCentro, resPistas] = await Promise.all([
      api.get(`/centros/${centroId}`),
      api.get('/pistas')
    ])
    
    centro.value = resCentro.data
    
    pistasDelCentro.value = resPistas.data
      .filter(p => p.centroId === parseInt(centroId))
      .map(p => ({
        ...p,
        deporteFormat: mapaDeportes[p.deporte] || p.deporte
      }))

  } catch (err) {
    console.error(err)
    toast.error('Error al cargar la información del centro.')
    router.push('/centros')
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans pb-16">
    
    <div v-if="cargando" class="flex justify-center items-center h-screen">
      <svg class="animate-spin h-12 w-12 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
    </div>

    <template v-else-if="centro">
      <div class="bg-gray-900 text-white py-12 px-6 lg:px-8 shadow-inner">
        <div class="max-w-7xl mx-auto">
          <div class="flex items-center gap-2 mb-3">
            <span class="px-3 py-1 bg-blue-600 text-white rounded-md text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 shadow-sm">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              {{ centro.ciudad }}
            </span>
          </div>
          <h1 class="text-4xl md:text-5xl font-extrabold tracking-tight mb-3">{{ centro.nombre }}</h1>
          <p class="text-gray-400 text-lg flex items-center gap-2">
            <svg class="w-5 h-5 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
            {{ centro.direccion }}
          </p>
        </div>
      </div>

      <div class="max-w-7xl mx-auto px-6 lg:px-8 py-10 grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        
        <div class="lg:col-span-1 flex flex-col gap-6">
          <button @click="router.back()" class="w-full flex items-center justify-center gap-2 py-3 px-4 bg-white border border-gray-200 text-gray-700 rounded-xl font-semibold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm group">
            <svg class="w-5 h-5 group-hover:-translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
            Volver a resultados
          </button>

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div class="h-64 w-full">
              <img 
                :src="centro.foto || imagenFallback" 
                :alt="centro.nombre" 
                class="w-full h-full object-cover"
              />
            </div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col gap-6">
            <h3 class="text-xl font-bold text-gray-900 border-b border-gray-100 pb-3">Información</h3>
            
            <div class="flex items-start gap-4">
              <div class="w-10 h-10 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
              </div>
              <div>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-0.5">Contacto</p>
                <p class="text-gray-900 font-medium">{{ centro.telefono || 'No especificado' }}</p>
              </div>
            </div>

            <div class="flex items-start gap-4">
              <div class="w-10 h-10 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              </div>
              <div>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-0.5">Horario</p>
                <p class="text-gray-900 font-medium" v-if="centro.horarioApertura && centro.horarioCierre">
                  De {{ centro.horarioApertura.substring(0, 5) }} a {{ centro.horarioCierre.substring(0, 5) }}
                </p>
                <p class="text-gray-900 font-medium" v-else>
                  Horario no disponible
                </p>
              </div>
            </div>

          </div>
        </div>

        <div class="lg:col-span-2">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900">Pistas disponibles</h2>
            <span class="bg-blue-100 text-blue-800 text-xs font-bold px-3 py-1 rounded-full">{{ pistasDelCentro.length }} en total</span>
          </div>

          <div v-if="pistasDelCentro.length === 0" class="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
            <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
            <h3 class="text-xl font-bold text-gray-900 mb-2">No hay pistas publicadas</h3>
            <p class="text-gray-500">Este centro deportivo aún no tiene pistas disponibles para reservar.</p>
          </div>

          <div v-else class="space-y-4">
            <PistaCard 
              v-for="pista in pistasDelCentro" 
              :key="pista.id"
              :pista="pista"
            />
          </div>
        </div>

      </div>
    </template>
  </div>
</template>
