<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import CentroDeportivoCard from '@/components/CentroDeportivoCard.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()

const centros = ref([])
const cargando = ref(true)

const filtroCiudad = ref('')
const filtroBusqueda = ref('')

const ordenSeleccionado = ref('defecto')
const opcionesOrden = [
  { value: 'defecto', label: 'Relevancia' },
  { value: 'nombreAsc', label: 'Nombre: A - Z' },
  { value: 'nombreDesc', label: 'Nombre: Z - A' }
]

const ciudadesUnicas = computed(() => {
  const ciudades = centros.value.map(c => c.ciudad).filter(Boolean)
  return [...new Set(ciudades)].sort()
})

const centrosFiltradosYOrdenados = computed(() => {
  let result = centros.value

  if (filtroCiudad.value) {
    result = result.filter(c => c.ciudad === filtroCiudad.value)
  }

  if (filtroBusqueda.value) {
    const q = filtroBusqueda.value.toLowerCase()
    result = result.filter(c => 
      c.nombre?.toLowerCase().includes(q) || 
      c.direccion?.toLowerCase().includes(q)
    )
  }

  result = [...result].sort((a, b) => {
    if (ordenSeleccionado.value === 'nombreAsc') {
      return a.nombre.localeCompare(b.nombre)
    } else if (ordenSeleccionado.value === 'nombreDesc') {
      return b.nombre.localeCompare(a.nombre)
    }
    return 0
  })

  return result
})

function limpiarFiltros() {
  filtroCiudad.value = ''
  filtroBusqueda.value = ''
  ordenSeleccionado.value = 'defecto'
}

onMounted(async () => {
  try {
    const res = await api.get('/centros')
    centros.value = res.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar los centros deportivos.')
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <PageHeader 
      title="Encuentra tu centro" 
      subtitle="Descubre las mejores instalaciones cerca de ti y comienza a entrenar." 
    />

    <div class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <div class="flex flex-col md:flex-row gap-4 items-end">
          
          <div class="w-full md:flex-1">
            <label class="block text-xs font-semibold text-gray-600 mb-2 uppercase tracking-wide">Buscar</label>
            <div class="relative">
              <input v-model="filtroBusqueda" type="text" placeholder="Nombre del centro o dirección..." class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              <svg class="w-4 h-4 text-gray-400 absolute left-3 top-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            </div>
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
              <option v-for="opcion in opcionesOrden" :key="opcion.value" :value="opcion.value">{{ opcion.label }}</option>
            </select>
          </div>
        </div>
      </div>

      <main class="w-full">
        <LoadingSpinner v-if="cargando" />

        <EmptyState 
          v-else-if="centrosFiltradosYOrdenados.length === 0"
          descripcion="No hay centros que coincidan con los filtros seleccionados. Intenta ampliar tu búsqueda."
          @action="limpiarFiltros"
        />

        <div v-else class="space-y-5">
          <p class="text-sm text-gray-500 font-medium pb-2 border-b border-gray-100">Mostrando {{ centrosFiltradosYOrdenados.length }} centro{{ centrosFiltradosYOrdenados.length > 1 ? 's' : '' }}</p>
          
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <CentroDeportivoCard 
              v-for="centro in centrosFiltradosYOrdenados" 
              :key="centro.id"
              :centro="centro"
            />
          </div>

        </div>
      </main>

    </div>
  </div>
</template>
