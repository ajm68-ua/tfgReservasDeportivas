<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const reservaId = route.params.id
const reserva = ref(null)
const loading = ref(true)
const paying = ref(false)
const error = ref(null)

const cargarReserva = async () => {
  try {
    const response = await api.get(`/reservas/${reservaId}`)
    reserva.value = response.data
  } catch (e) {
    error.value = "No se pudo cargar la reserva."
    toast.error("Error al cargar la reserva")
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  cargarReserva()
})

const importeAPagar = computed(() => {
  if (!reserva.value) return 0
  if (reserva.value.esAbierta) {
    const capacidad = reserva.value.capacidadMaxima || 4
    return reserva.value.precioTotal / capacidad
  }
  return reserva.value.precioTotal
})

const procesarPago = async () => {
  if (paying.value) return;
  paying.value = true
  try {
    await api.put(`/reservas/${reservaId}/pagar`)
    
    if (authStore.usuario && authStore.usuario.saldo !== undefined) {
      authStore.usuario.saldo -= importeAPagar.value
      localStorage.setItem('usuario', JSON.stringify(authStore.usuario))
    }

    toast.success("Pago procesado correctamente")
    
    setTimeout(() => {
      router.push('/mis-reservas')
    }, 1500)
    
  } catch (e) {
    let msg = "Error al procesar el pago"
    if (e.response && e.response.data && typeof e.response.data === 'string') {
        msg = e.response.data
    } else if (e.response && e.response.data && e.response.data.message) {
        msg = e.response.data.message
    } else if (e.response && e.response.status === 400) {
        msg = "Saldo insuficiente o error de validación."
    }
    toast.error(msg)
    paying.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    <PageHeader 
      title="Pago de Reserva" 
      subtitle="Revisa los datos y confirma tu pago" 
    />
    
    <main class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col">
      
      <div v-if="loading" class="flex flex-col items-center justify-center py-20 flex-grow">
        <div class="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
        <p class="mt-4 text-gray-500 font-medium">Cargando detalles de tu reserva...</p>
      </div>
      
      <div v-else-if="error" class="w-full bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-2xl shadow-sm text-center">
        <p>{{ error }}</p>
        <button @click="router.push('/mis-reservas')" class="mt-4 text-sm font-semibold hover:underline">
          Volver a mis reservas
        </button>
      </div>

      <div v-else-if="reserva" class="flex-grow">
        
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
          <div>
            <span class="px-2.5 py-1 bg-blue-50 text-blue-700 rounded-md text-[10px] font-bold uppercase tracking-wider mb-2 inline-block">
              Pista Reservada
            </span>
            <h2 class="text-2xl font-bold text-gray-900 leading-tight">{{ reserva.nombrePista }}</h2>
            <p class="text-gray-500 font-medium mt-1 flex items-center gap-1.5">
              {{ reserva.nombreCentro }}
            </p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-500 font-bold uppercase tracking-wider mb-1">Precio Total</p>
            <p class="text-3xl font-extrabold text-black">{{ reserva.precioTotal.toFixed(2) }}€</p>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          <div class="lg:col-span-2 space-y-8">
            
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 class="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                1. Detalles del Partido
              </h3>
              
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div>
                  <p class="text-sm text-gray-500 font-medium mb-1">Fecha</p>
                  <p class="text-lg font-semibold text-gray-900">
                    {{ new Date(reserva.fecha).toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}
                  </p>
                </div>
                <div>
                  <p class="text-sm text-gray-500 font-medium mb-1">Horario</p>
                  <p class="text-lg font-semibold text-gray-900">
                    {{ reserva.horaInicio.substring(0,5) }} - {{ reserva.horaFin.substring(0,5) }}
                  </p>
                </div>
                <div>
                  <p class="text-sm text-gray-500 font-medium mb-1">Nivel</p>
                  <p class="text-lg font-semibold text-gray-900 capitalize">{{ reserva.nivel.toLowerCase() }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500 font-medium mb-1">Capacidad de Pista</p>
                  <p class="text-lg font-semibold text-gray-900">{{ reserva.capacidadMaxima || 4 }} personas</p>
                </div>
              </div>
            </div>

          </div>

          <div class="space-y-8">
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 class="text-lg font-bold text-gray-900 mb-6">2. Resumen y Confirmación</h3>
              
              <div class="space-y-3 text-sm text-gray-600 font-medium mb-6">
                <div class="flex justify-between">
                  <span>Subtotal Pista</span>
                  <span class="text-gray-900">{{ reserva.precioTotal.toFixed(2) }}€</span>
                </div>
                <div v-if="reserva.esAbierta" class="flex justify-between text-blue-600">
                  <span>Descuento (Pago dividido)</span>
                  <span>-{{ (reserva.precioTotal - importeAPagar).toFixed(2) }}€</span>
                </div>
                <div class="h-px bg-gray-200 my-4"></div>
                <div class="flex justify-between items-end">
                  <span class="text-gray-500">Total a deducir</span>
                  <span class="text-3xl font-extrabold text-black">{{ importeAPagar.toFixed(2) }}€</span>
                </div>
              </div>

              <div class="bg-gray-50 rounded-xl p-4 mb-6 border border-gray-200">
                <p class="text-xs text-gray-500 uppercase tracking-wider font-bold mb-1">Tu Saldo</p>
                <p class="text-xl font-bold text-gray-900">{{ authStore.usuario?.saldo?.toFixed(2) || '0.00' }}€</p>
                
                <p v-if="(authStore.usuario?.saldo || 0) < importeAPagar" class="text-red-600 text-sm mt-2 font-semibold flex items-center bg-red-50 p-2 rounded-md">
                  <svg class="w-4 h-4 mr-1 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                  No tienes saldo suficiente.
                </p>
              </div>

              <button 
                @click="procesarPago" 
                :disabled="paying || (authStore.usuario?.saldo || 0) < importeAPagar"
                class="w-full py-3.5 bg-blue-600 text-white rounded-xl font-bold shadow-md hover:bg-blue-500 hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                <span v-if="paying" class="flex items-center gap-2">
                  <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                  Procesando...
                </span>
                <span v-else>Confirmar y Pagar</span>
              </button>

              <button 
                @click="router.push('/mis-reservas')"
                class="w-full py-3 mt-3 bg-white text-gray-600 hover:text-gray-900 font-bold rounded-xl transition-colors border border-gray-200"
              >
                Cancelar
              </button>
            </div>
          </div>
          
        </div>
      </div>
      
    </main>
  </div>
</template>
