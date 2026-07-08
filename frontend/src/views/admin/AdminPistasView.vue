<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import AdminPistaCard from '@/components/AdminPistaCard.vue'
import { DEPORTES_ARRAY, MAPA_DEPORTES } from '@/utils/constants'

const router = useRouter()
const authStore = useAuthStore()

const cargando = ref(true)
const pistas = ref([])
const centro = ref(null)
const reservasHoy = ref([])
let stompClient = null

const mostrarModal = ref(false)
const modoEdicion = ref(false)
const guardando = ref(false)

const DEPORTES_KEYS = Object.keys(MAPA_DEPORTES)

const form = reactive({
  id: null,
  nombre: '',
  deporte: DEPORTES_KEYS[0],
  precioPorHora: 10.0,
  capacidadMaxima: 4,
  disponible: true,
  foto: ''
})

onMounted(async () => {
  if (!authStore.usuario || !authStore.isAdmin() || !authStore.usuario.centroId) {
    router.push('/')
    return
  }
  try {
    await Promise.all([cargarCentro(), cargarPistas(), cargarReservasHoy()])
    conectarWebSocket()
  } finally {
    cargando.value = false
  }
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

async function cargarCentro() {
  try {
    const res = await api.get(`/centros/${authStore.usuario.centroId}`)
    centro.value = res.data
  } catch (err) {
    console.error(err)
  }
}

async function cargarReservasHoy() {
  try {
    const res = await api.get(`/reservas/centro/${authStore.usuario.centroId}`)
    reservasHoy.value = res.data
  } catch (err) {
    console.error(err)
  }
}

function conectarWebSocket() {
  const socket = new SockJS('/ws-chat')
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe(`/topic/centro/${authStore.usuario.centroId}/reservas`, (msg) => {
        if (msg.body === 'REFRESH') {
          cargarReservasHoy()
        }
      })
    }
  })
  stompClient.activate()
}

async function cargarPistas() {
  try {
    const res = await api.get(`/pistas/admin/centro/${authStore.usuario.centroId}`)
    pistas.value = res.data
  } catch (err) {
    toast.error('Error al cargar las pistas del centro.')
  }
}

function abrirModalNueva() {
  modoEdicion.value = false
  form.id = null
  form.nombre = ''
  form.deporte = DEPORTES_KEYS[0]
  form.precioPorHora = 10.0
  form.capacidadMaxima = 4
  form.disponible = true
  form.foto = ''
  mostrarModal.value = true
}

function abrirModalEditar(pista) {
  modoEdicion.value = true
  form.id = pista.id
  form.nombre = pista.nombre
  form.deporte = pista.deporte
  form.precioPorHora = pista.precioPorHora
  form.capacidadMaxima = pista.capacidadMaxima
  form.disponible = pista.disponible
  form.foto = pista.foto || ''
  mostrarModal.value = true
}

function cerrarModal() {
  mostrarModal.value = false
}

async function guardarPista() {
  if (!form.nombre || form.precioPorHora <= 0) {
    toast.error('Completa los campos correctamente.')
    return
  }

  guardando.value = true
  try {
    if (modoEdicion.value) {
      await api.put(`/pistas/${form.id}/centro/${authStore.usuario.centroId}`, form)
      toast.success('Pista actualizada correctamente.')
    } else {
      await api.post(`/pistas/centro/${authStore.usuario.centroId}`, form)
      toast.success('Pista creada correctamente.')
    }
    cerrarModal()
    await cargarPistas()
  } catch (err) {
    toast.error('Error al guardar la pista.')
  } finally {
    guardando.value = false
  }
}

function procesarFotoPista(event) {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 5 * 1024 * 1024) {
    toast.error('La imagen es demasiado grande. El tamaño máximo es 5MB.')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    form.foto = e.target.result
  }
  reader.readAsDataURL(file)
}

async function eliminarPista(id) {
  if (!confirm('¿Estás seguro de que quieres eliminar esta pista permanentemente? Esta acción no se puede deshacer.')) return
  
  try {
    await api.delete(`/pistas/${id}/centro/${authStore.usuario.centroId}`)
    toast.success('Pista eliminada correctamente.')
    await cargarPistas()
  } catch (err) {
    toast.error('Error al eliminar la pista.')
  }
}

async function toggleDisponibilidad(pista) {
  try {
    await api.put(`/pistas/${pista.id}/centro/${authStore.usuario.centroId}`, {
      ...pista,
      disponible: !pista.disponible
    })
    pista.disponible = !pista.disponible
    toast.success(pista.disponible ? 'Pista marcada como Disponible' : 'Pista marcada como No Disponible')
  } catch (err) {
    toast.error('Error al cambiar la disponibilidad.')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <div class="bg-gray-900 text-white py-12 px-6 lg:px-8 shadow-inner">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-4xl md:text-5xl font-extrabold tracking-tight mb-3 text-white">Gestión de Pistas</h1>
          <p class="text-gray-400 text-lg">Administra las pistas de tu centro deportivo</p>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      
      <div class="flex justify-end">
        <button 
          @click="abrirModalNueva" 
          class="bg-blue-600 text-white px-5 py-2.5 rounded-xl font-semibold shadow-md hover:shadow-lg hover:bg-blue-700 transition flex items-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
          Nueva Pista
        </button>
      </div>

      <LoadingSpinner v-if="cargando" />

      <div v-else>
        <EmptyState 
          v-if="pistas.length === 0" 
          titulo="No hay pistas registradas"
          descripcion="Empieza añadiendo las instalaciones deportivas disponibles en tu centro."
          textoBoton="Añadir mi primera pista"
          @action="abrirModalNueva"
        >
          <template #icon>
            <svg class="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
          </template>
        </EmptyState>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <AdminPistaCard
            v-for="pista in pistas"
            :key="pista.id"
            :pista="pista"
            :centro="centro"
            :reservasHoy="reservasHoy.filter(r => r.pistaId === pista.id)"
            @toggle-disponibilidad="toggleDisponibilidad"
            @editar="abrirModalEditar"
            @eliminar="eliminarPista"
          />
        </div>
      </div>
    </div>

    <div v-if="mostrarModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="fixed inset-0 bg-gray-900/50 backdrop-blur-sm" @click="cerrarModal"></div>
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md z-10 overflow-hidden animate-fade-in-down">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-gray-900">{{ modoEdicion ? 'Editar Pista' : 'Nueva Pista' }}</h3>
          <button @click="cerrarModal" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        
        <form @submit.prevent="guardarPista" class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nombre de la pista</label>
            <input v-model="form.nombre" type="text" required placeholder="Ej: Pista de Pádel" class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Deporte</label>
            <select v-model="form.deporte" class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none">
              <option v-for="key in DEPORTES_KEYS" :key="key" :value="key">{{ MAPA_DEPORTES[key] }}</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Foto de la Pista</label>
            <div class="flex items-center gap-4">
              <div v-if="form.foto" class="relative w-20 h-20 rounded-lg overflow-hidden border border-gray-200">
                <img :src="form.foto" class="w-full h-full object-cover" />
                <button type="button" @click="form.foto = ''" class="absolute top-1 right-1 bg-white rounded-full p-0.5 shadow hover:bg-gray-100">
                  <svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
              </div>
              <div class="flex-1">
                <input type="file" accept="image/*" @change="procesarFotoPista" class="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 transition" />
                <p class="text-xs text-gray-500 mt-2">Opcional. Si no seleccionas ninguna, se usará la del centro.</p>
              </div>
            </div>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Precio / hora (€)</label>
              <input v-model.number="form.precioPorHora" type="number" step="0.1" min="1" required class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Capacidad Max</label>
              <input v-model.number="form.capacidadMaxima" type="number" min="1" required class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none" />
            </div>
          </div>

          <div class="flex items-center pt-2">
            <input v-model="form.disponible" type="checkbox" id="disponible" class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500">
            <label for="disponible" class="ml-2 text-sm font-medium text-gray-900">Pista disponible para reservas</label>
          </div>

          <div class="pt-4 flex gap-3">
            <button type="button" @click="cerrarModal" class="flex-1 py-2.5 bg-gray-100 text-gray-700 rounded-xl font-semibold hover:bg-gray-200 transition">Cancelar</button>
            <button type="submit" :disabled="guardando" class="flex-1 py-2.5 bg-blue-600 text-white rounded-xl font-semibold hover:bg-blue-700 transition disabled:opacity-70 disabled:cursor-not-allowed">
              <span v-if="guardando" class="flex items-center justify-center gap-2">
                <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                Guardando...
              </span>
              <span v-else>Guardar Pista</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
