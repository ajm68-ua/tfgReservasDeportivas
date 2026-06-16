<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import PistaCard from '@/components/PistaCard.vue'

const route = useRoute()
const router = useRouter()

const pistas = ref([])
const centros = ref([])
const cargando = ref(true)

const deportes = ['Pádel', 'Tenis', 'Fútbol', 'Baloncesto', 'Squash', 'Bádminton']
const mapaDeportes = {
  'PADEL': 'Pádel',
  'TENIS': 'Tenis',
  'FUTBOL': 'Fútbol',
  'BALONCESTO': 'Baloncesto',
  'SQUASH': 'Squash',
  'BADMINTON': 'Bádminton'
}

const filtroDeporte = ref(route.query.deporte || '')
const filtroCiudad = ref(route.query.ciudad || '')
const filtroBusqueda = ref('')

const ordenSeleccionado = ref('defecto')
const opcionesOrden = [
  { value: 'defecto', label: 'Relevancia' },
  { value: 'precioAsc', label: 'Precio: Menor a Mayor' },
  { value: 'precioDesc', label: 'Precio: Mayor a Menor' },
  { value: 'nombreAsc', label: 'Nombre: A - Z' }
]

const ciudadesUnicas = computed(() => {
  const ciudades = centros.value.map(c => c.ciudad).filter(Boolean)
  return [...new Set(ciudades)].sort()
})

const pistasFiltradasYOrdenadas = computed(() => {
  let result = pistas.value

  if (filtroDeporte.value) {
    result = result.filter(p => mapaDeportes[p.deporte] === filtroDeporte.value)
  }

  if (filtroCiudad.value) {
    result = result.filter(p => p.centroCiudad === filtroCiudad.value)
  }

  if (filtroBusqueda.value) {
    const q = filtroBusqueda.value.toLowerCase()
    result = result.filter(p => 
      p.nombre?.toLowerCase().includes(q) || 
      p.centroNombre?.toLowerCase().includes(q)
    )
  }

  result = [...result].sort((a, b) => {
    if (ordenSeleccionado.value === 'precioAsc') {
      return a.precioPorHora - b.precioPorHora
    } else if (ordenSeleccionado.value === 'precioDesc') {
      return b.precioPorHora - a.precioPorHora
    } else if (ordenSeleccionado.value === 'nombreAsc') {
      return a.nombre.localeCompare(b.nombre)
    }
    return 0
  })

  return result
})

function limpiarFiltros() {
  filtroDeporte.value = ''
  filtroCiudad.value = ''
  filtroBusqueda.value = ''
  ordenSeleccionado.value = 'defecto'
  
  router.replace({ query: {} })
}

onMounted(async () => {
  try {
    const [resPistas, resCentros] = await Promise.all([
      api.get('/pistas'),
      api.get('/centros')
    ])
    pistas.value = resPistas.data
    centros.value = resCentros.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar la información.')
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <div class="bg-gray-900 text-white py-12 px-6 lg:px-8 shadow-inner">
      <div class="max-w-7xl mx-auto">
        <h1 class="text-4xl md:text-5xl font-extrabold tracking-tight mb-3">Encuentra tu pista</h1>
        <p class="text-gray-400 text-lg">Reserva las mejores instalaciones en tu ciudad al mejor precio.</p>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex flex-col md:flex-row gap-4 items-end">
          
          <div class="w-full md:flex-1">
            <label class="block text-xs font-semibold text-gray-600 mb-2 uppercase tracking-wide">Buscar</label>
            <div class="relative">
              <input v-model="filtroBusqueda" type="text" placeholder="Nombre de pista o centro..." class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              <svg class="w-4 h-4 text-gray-400 absolute left-3 top-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            </div>
          </div>

          <div class="w-full md:w-48">
            <label class="block text-xs font-semibold text-gray-600 mb-2 uppercase tracking-wide">Deporte</label>
            <select v-model="filtroDeporte" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition cursor-pointer">
              <option value="">Cualquier deporte</option>
              <option v-for="dep in deportes" :key="dep" :value="dep">{{ dep }}</option>
            </select>
          </div>

          <div class="w-full md:w-48">
            <label class="block text-xs font-semibold text-gray-600 mb-2 uppercase tracking-wide">Ciudad</label>
            <select v-model="filtroCiudad" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition cursor-pointer">
              <option value="">Cualquier ciudad</option>
              <option v-for="ciudad in ciudadesUnicas" :key="ciudad" :value="ciudad">{{ ciudad }}</option>
            </select>
          </div>
          
          <div class="w-full md:w-56">
            <label class="block text-xs font-semibold text-gray-600 mb-2 uppercase tracking-wide">Ordenar por</label>
            <select v-model="ordenSeleccionado" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition cursor-pointer">
              <option v-for="op in opcionesOrden" :key="op.value" :value="op.value">{{ op.label }}</option>
            </select>
          </div>

          <div class="w-full md:w-auto flex justify-end pb-1.5 pl-2">
            <button v-if="filtroDeporte || filtroCiudad || filtroBusqueda" @click="limpiarFiltros" class="text-xs font-semibold text-blue-600 hover:text-blue-800 transition whitespace-nowrap">
              Limpiar filtros
            </button>
          </div>

        </div>
      </div>

      <main class="w-full">
        
        <div v-if="cargando" class="flex justify-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>

        <div v-else-if="pistasFiltradasYOrdenadas.length === 0" class="bg-white rounded-2xl border border-gray-100 p-16 text-center shadow-sm">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-2">No hemos encontrado resultados</h3>
          <p class="text-gray-500 mb-6 max-w-sm mx-auto">No hay pistas que coincidan con los filtros seleccionados. Intenta ampliar tu búsqueda.</p>
          <button @click="limpiarFiltros" class="px-6 py-2.5 bg-gray-900 text-white rounded-full font-semibold text-sm hover:bg-gray-800 transition shadow-md hover:shadow-lg">Limpiar todos los filtros</button>
        </div>

        <div v-else class="space-y-5">
          <p class="text-sm text-gray-500 font-medium pb-2 border-b border-gray-100">Mostrando {{ pistasFiltradasYOrdenadas.length }} pista{{ pistasFiltradasYOrdenadas.length > 1 ? 's' : '' }}</p>
          
          <PistaCard 
            v-for="pista in pistasFiltradasYOrdenadas" 
            :key="pista.id"
            :pista="pista"
          />

        </div>
      </main>

    </div>
  </div>
</template>
