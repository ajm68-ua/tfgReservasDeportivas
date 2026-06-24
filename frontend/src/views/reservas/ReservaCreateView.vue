<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { MAPA_DEPORTES } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const pistaId = route.query.pistaId
const pista = ref(null)
const centro = ref(null)
const cargando = ref(true)

function getFechaLocal() {
  const hoy = new Date()
  const anio = hoy.getFullYear()
  const mes = String(hoy.getMonth() + 1).padStart(2, '0')
  const dia = String(hoy.getDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

const fecha = ref(getFechaLocal())
const bloquesSeleccionados = ref([])
const nivel = ref('INTERMEDIO')
const esAbierta = ref(false)
const guardando = ref(false)

const reservas = ref([])
const bloquesHorarios = ref([])

const niveles = [
  { value: 'PRINCIPIANTE', label: 'Principiante' },
  { value: 'INTERMEDIO', label: 'Intermedio' },
  { value: 'AVANZADO', label: 'Avanzado' },
  { value: 'PROFESIONAL', label: 'Profesional' }
]

onMounted(async () => {
  if (!pistaId) {
    toast.error('No se ha especificado ninguna pista')
    router.push('/pistas')
    return
  }
  
  if (!authStore.isLogged()) {
    toast.error('Debes iniciar sesión para reservar')
    router.push('/login')
    return
  }

  try {
    const resPista = await api.get(`/pistas`)
    const p = resPista.data.find(x => x.id === parseInt(pistaId))
    if (!p) throw new Error("Pista no encontrada")
    pista.value = p

    const resCentro = await api.get(`/centros/${p.centroId}`)
    centro.value = resCentro.data

    await cargarDisponibilidad()
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar los datos de la pista')
    router.push('/pistas')
  } finally {
    cargando.value = false
  }
})

watch(fecha, () => {
  bloquesSeleccionados.value = []
  cargarDisponibilidad()
})

async function cargarDisponibilidad() {
  if (!fecha.value || !centro.value) return
  
  try {
    const res = await api.get(`/reservas/pista/${pistaId}?fecha=${fecha.value}`)
    reservas.value = res.data
    generarBloques()
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar la disponibilidad')
  }
}

function generarBloques() {
  const apertura = centro.value.horarioApertura
  const cierre = centro.value.horarioCierre

  const horaInicio = parseInt(apertura.split(':')[0])
  const horaFin = parseInt(cierre.split(':')[0])

  const bloques = []
  for (let i = horaInicio; i < horaFin; i++) {
    const startStr = `${i.toString().padStart(2, '0')}:00:00`
    const endStr = `${(i + 1).toString().padStart(2, '0')}:00:00`
    
    const ocupado = reservas.value.some(r => {
      return startStr < r.horaFin && endStr > r.horaInicio
    })

    const esHoy = fecha.value === getFechaLocal()
    const horaActual = new Date().getHours()
    const yaPaso = esHoy && i <= horaActual

    bloques.push({
      start: startStr,
      end: endStr,
      label: `${i.toString().padStart(2, '0')}:00 - ${(i + 1).toString().padStart(2, '0')}:00`,
      ocupado: ocupado || yaPaso
    })
  }
  bloquesHorarios.value = bloques
}

function verificarContinuidad(array) {
  if (array.length <= 1) return true
  for(let i=0; i < array.length - 1; i++) {
    if(array[i].end !== array[i+1].start) return false
  }
  return true
}

function seleccionarBloque(bloque) {
  if (bloque.ocupado) return

  const idx = bloquesSeleccionados.value.findIndex(b => b.start === bloque.start)
  if (idx >= 0) {
    bloquesSeleccionados.value.splice(idx, 1)
    if (!verificarContinuidad(bloquesSeleccionados.value)) {
      bloquesSeleccionados.value = []
    }
  } else {
    bloquesSeleccionados.value.push(bloque)
    bloquesSeleccionados.value.sort((a, b) => a.start.localeCompare(b.start))
    if (!verificarContinuidad(bloquesSeleccionados.value)) {
      bloquesSeleccionados.value = [bloque]
    }
  }
}

const precioTotal = computed(() => {
  if (!pista.value || bloquesSeleccionados.value.length === 0) return 0
  return pista.value.precioPorHora * bloquesSeleccionados.value.length
})

const fechaMinima = computed(() => {
  return getFechaLocal()
})

async function confirmarReserva() {
  if (bloquesSeleccionados.value.length === 0) {
    toast.warning('Por favor, selecciona al menos una hora')
    return
  }

  const horaInicioReserva = bloquesSeleccionados.value[0].start
  const horaFinReserva = bloquesSeleccionados.value[bloquesSeleccionados.value.length - 1].end

  guardando.value = true
  try {
    await api.post('/reservas', {
      pistaId: pista.value.id,
      organizadorId: authStore.usuario.id,
      fecha: fecha.value,
      horaInicio: horaInicioReserva,
      horaFin: horaFinReserva,
      nivel: nivel.value,
      esAbierta: esAbierta.value
    })
    
    toast.success('Reserva confirmada con éxito')
    router.push('/mis-reservas')
  } catch (err) {
    console.error(err)
    if (err.response?.data) {
      toast.error(err.response.data.message || 'Error al confirmar la reserva')
    } else {
      toast.error('Error al confirmar la reserva')
    }
  } finally {
    guardando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <PageHeader 
      title="Nueva Reserva" 
      subtitle="Configura los detalles de tu partido" 
    />

    <LoadingSpinner v-if="cargando" />

    <div v-else class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <span class="px-2.5 py-1 bg-blue-50 text-blue-700 rounded-md text-[10px] font-bold uppercase tracking-wider mb-2 inline-block">
            {{ MAPA_DEPORTES[pista.deporte] || pista.deporte }}
          </span>
          <h2 class="text-2xl font-bold text-gray-900 leading-tight">{{ pista.nombre }}</h2>
          <p class="text-gray-500 font-medium mt-1 flex items-center gap-1.5">
            {{ centro.nombre }} ({{ centro.ciudad }})
          </p>
        </div>
        <div class="text-right">
          <p class="text-3xl font-extrabold text-black">{{ pista.precioPorHora }}€<span class="text-sm text-gray-500 font-bold"> / HORA</span></p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <div class="lg:col-span-2 space-y-8">
          
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              1. Selecciona el Día
            </h3>
            <input 
              v-model="fecha" 
              type="date" 
              :min="fechaMinima"
              class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none font-medium text-gray-700"
            />
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              2. Elige la Hora
            </h3>
            
            <div v-if="bloquesHorarios.length > 0" class="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <button
                v-for="bloque in bloquesHorarios"
                :key="bloque.start"
                @click="seleccionarBloque(bloque)"
                :disabled="bloque.ocupado"
                class="py-3 px-2 rounded-xl text-sm font-semibold border-2 transition-all duration-200 text-center flex flex-col items-center justify-center gap-1"
                :class="[
                  bloque.ocupado 
                    ? 'bg-gray-100 border-gray-100 text-gray-400 cursor-not-allowed opacity-60' 
                    : bloquesSeleccionados.some(b => b.start === bloque.start)
                      ? 'bg-blue-50 border-blue-500 text-blue-700 shadow-sm'
                      : 'bg-white border-green-200 text-gray-700 hover:border-green-400 hover:shadow-sm cursor-pointer'
                ]"
              >
                <span>{{ bloque.label }}</span>
                <span v-if="bloque.ocupado" class="text-[10px] uppercase tracking-wider font-bold opacity-70">Ocupado</span>
                <span v-else class="text-[10px] uppercase tracking-wider font-bold text-green-600" :class="{'text-blue-600': bloquesSeleccionados.some(b => b.start === bloque.start)}">Disponible</span>
              </button>
            </div>
            <div v-else class="text-center py-6 text-gray-500">
              No hay horarios disponibles para este día.
            </div>
          </div>

        </div>

        <div class="space-y-8">
          
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-6">
            <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
              3. Opciones
            </h3>
            
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Nivel del partido</label>
              <select v-model="nivel" class="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition outline-none font-medium">
                <option v-for="n in niveles" :key="n.value" :value="n.value">{{ n.label }}</option>
              </select>
            </div>

            <div class="pt-2">
              <label class="flex items-start gap-3 cursor-pointer group">
                <div class="relative flex items-center mt-0.5">
                  <input type="checkbox" v-model="esAbierta" class="sr-only peer">
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </div>
                <div class="flex-1">
                  <span class="block text-sm font-bold text-gray-900 group-hover:text-blue-600 transition-colors">Pista Abierta</span>
                  <span class="block text-xs text-gray-500 mt-0.5">Permite que otros usuarios puedan unirse a tu reserva.</span>
                </div>
              </label>
            </div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 class="text-lg font-bold text-gray-900 mb-6">4. Resumen</h3>
            
            <div class="space-y-3 text-sm text-gray-600 font-medium mb-6">
              <div class="flex justify-between">
                <span>Fecha</span>
                <span class="text-gray-900">{{ fecha.split('-').reverse().join('/') }}</span>
              </div>
              <div class="flex justify-between">
                <span>Hora</span>
                <span class="text-gray-900">{{ bloquesSeleccionados.length > 0 ? `${bloquesSeleccionados[0].start.substring(0,5)} - ${bloquesSeleccionados[bloquesSeleccionados.length-1].end.substring(0,5)}` : '--:--' }}</span>
              </div>
              <div class="flex justify-between">
                <span>Pista</span>
                <span class="text-gray-900">{{ pista.nombre }}</span>
              </div>
              <div class="h-px bg-gray-200 my-4"></div>
              <div class="flex justify-between items-end">
                <span class="text-gray-500">Total a pagar</span>
                <span class="text-3xl font-extrabold text-black">{{ precioTotal }}€</span>
              </div>
            </div>

            <button 
              @click="confirmarReserva" 
              :disabled="bloquesSeleccionados.length === 0 || guardando"
              class="w-full py-3.5 bg-blue-600 text-white rounded-xl font-bold shadow-md hover:bg-blue-500 hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <span v-if="guardando" class="flex items-center gap-2">
                <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                Procesando...
              </span>
              <span v-else>Confirmar y Pagar</span>
            </button>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>
