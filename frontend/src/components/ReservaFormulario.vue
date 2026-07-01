<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { toast } from 'vue3-toastify'
import { MAPA_DEPORTES } from '@/utils/constants'
import { formatearHora, formatearDinero } from '@/utils/formatters'

const props = defineProps({
  pista: { type: Object, required: true },
  centro: { type: Object, required: true },
  reservasDelDia: { type: Array, default: () => [] },
  reservaIgnoradaId: { type: Number, default: null },
  guardando: { type: Boolean, default: false },
  modo: { type: String, default: 'CREATE' },
  modoAdmin: { type: Boolean, default: false },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['fecha-changed', 'submit', 'cancelar-edicion'])

function getFechaLocal() {
  const hoy = new Date()
  const anio = hoy.getFullYear()
  const mes = String(hoy.getMonth() + 1).padStart(2, '0')
  const dia = String(hoy.getDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

const fecha = ref(props.initialData.fecha || getFechaLocal())
const nivel = ref(props.initialData.nivel || 'INTERMEDIO')
const esAbierta = ref(props.initialData.esAbierta || false)
const bloquesSeleccionados = ref(props.initialData.bloquesSeleccionados ? [...props.initialData.bloquesSeleccionados] : [])

const bloquesHorarios = ref([])

import { NIVELES_OPCIONES } from '@/utils/constants'

const niveles = NIVELES_OPCIONES

const fechaMinima = computed(() => getFechaLocal())

watch(fecha, (newVal, oldVal) => {
  if (oldVal !== undefined) {
    bloquesSeleccionados.value = []
  }
  emit('fecha-changed', newVal)
})

watch(() => props.reservasDelDia, () => {
  generarBloques()
}, { deep: true, immediate: true })

onMounted(() => {
  if (!props.initialData.fecha) {
    emit('fecha-changed', fecha.value)
  }
})

function generarBloques() {
  if (!props.centro || !props.centro.horarioApertura) return

  const apertura = props.centro.horarioApertura
  const cierre = props.centro.horarioCierre

  const horaInicio = parseInt(apertura.split(':')[0])
  const horaFin = parseInt(cierre.split(':')[0])

  const bloques = []
  for (let i = horaInicio; i < horaFin; i++) {
    const startStr = `${i.toString().padStart(2, '0')}:00:00`
    const endStr = `${(i + 1).toString().padStart(2, '0')}:00:00`
    
    const ocupado = props.reservasDelDia.some(r => {
      if (props.reservaIgnoradaId && r.id === props.reservaIgnoradaId) return false
      if (r.estadoPago === 'CANCELADO') return false
      return startStr < r.horaFin && endStr > r.horaInicio
    })

    const hoyStr = getFechaLocal()
    const esHoy = fecha.value === hoyStr
    const esPasado = fecha.value < hoyStr
    const horaActualTime = new Date().getHours()
    const yaPaso = esPasado || (esHoy && i <= horaActualTime)

    bloques.push({
      start: startStr,
      end: endStr,
      label: `${i.toString().padStart(2, '0')}:00 - ${(i + 1).toString().padStart(2, '0')}:00`,
      ocupado: ocupado || yaPaso,
      reservado: ocupado,
      pasado: yaPaso
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
  if (bloque.ocupado || props.modoAdmin) return;

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
  if (!props.pista || bloquesSeleccionados.value.length === 0) return 0
  return props.pista.precioPorHora * bloquesSeleccionados.value.length
})

function submitForm() {
  if (bloquesSeleccionados.value.length === 0) {
    toast.warning('Por favor, selecciona al menos una hora')
    return
  }

  const horaInicioReserva = bloquesSeleccionados.value[0].start
  const horaFinReserva = bloquesSeleccionados.value[bloquesSeleccionados.value.length - 1].end

  emit('submit', {
    fecha: fecha.value,
    horaInicio: horaInicioReserva,
    horaFin: horaFinReserva,
    nivel: nivel.value,
    esAbierta: esAbierta.value
  })
}
</script>

<template>
  <div v-if="!modoAdmin" class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
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
      <p class="text-3xl font-extrabold text-black">{{ formatearDinero(pista.precioPorHora) }}<span class="text-sm text-gray-500 font-bold"> / HORA</span></p>
    </div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div :class="modoAdmin ? 'lg:col-span-3 space-y-8' : 'lg:col-span-2 space-y-8'">
      
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-4">
        <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
          {{ modoAdmin ? 'Día de Ocupación' : '1. Selecciona el Día' }}
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
          {{ modoAdmin ? 'Horarios' : '2. Elige la Hora' }}
        </h3>
        
        <div v-if="bloquesHorarios.length > 0" class="grid grid-cols-2 sm:grid-cols-3 gap-3">
          <button
            v-for="bloque in bloquesHorarios"
            :key="bloque.start"
            @click="seleccionarBloque(bloque)"
            :disabled="bloque.ocupado"
            class="py-3 px-2 rounded-xl text-sm font-semibold border-2 transition-all duration-200 text-center flex flex-col items-center justify-center gap-1"
            :class="[
              bloque.reservado && modoAdmin 
                ? 'bg-red-50 border-red-200 text-red-700 cursor-default' 
                : bloque.ocupado 
                  ? 'bg-gray-100 border-gray-100 text-gray-400 cursor-not-allowed opacity-60' 
                  : bloquesSeleccionados.some(b => b.start === bloque.start)
                    ? 'bg-blue-50 border-blue-500 text-blue-700 shadow-sm cursor-pointer'
                    : modoAdmin 
                      ? 'bg-green-50 border-green-200 text-green-700 cursor-default'
                      : 'bg-white border-green-200 text-gray-700 hover:border-green-400 hover:shadow-sm cursor-pointer'
            ]"
          >
            <span>{{ bloque.label }}</span>
            <span v-if="bloque.reservado && modoAdmin" class="text-[10px] uppercase tracking-wider font-bold">Reservado</span>
            <span v-else-if="bloque.ocupado" class="text-[10px] uppercase tracking-wider font-bold opacity-70">Ocupado</span>
            <span v-else class="text-[10px] uppercase tracking-wider font-bold" :class="{'text-blue-600': bloquesSeleccionados.some(b => b.start === bloque.start), 'text-green-600': !modoAdmin}">Disponible</span>
          </button>
        </div>
        <div v-else class="text-center py-6 text-gray-500">
          No hay horarios disponibles para este día.
        </div>
      </div>

    </div>

    <div v-if="!modoAdmin" class="space-y-8">
      
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
            <span class="text-gray-900">{{ bloquesSeleccionados.length > 0 ? `${formatearHora(bloquesSeleccionados[0].start)} - ${formatearHora(bloquesSeleccionados[bloquesSeleccionados.length-1].end)}` : '--:--' }}</span>
          </div>
          <div class="flex justify-between">
            <span>Pista</span>
            <span class="text-gray-900">{{ pista.nombre }}</span>
          </div>
          <div class="h-px bg-gray-200 my-4"></div>
          <div class="flex justify-between items-end">
            <span class="text-gray-500">Total a pagar</span>
            <span class="text-3xl font-extrabold text-black">{{ formatearDinero(precioTotal) }}</span>
          </div>
        </div>

        <button 
          @click="submitForm" 
          :disabled="bloquesSeleccionados.length === 0 || guardando"
          class="w-full py-3.5 bg-blue-600 text-white rounded-xl font-bold shadow-md hover:bg-blue-500 hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <span v-if="guardando" class="flex items-center gap-2">
            <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            {{ modo === 'EDIT' ? 'Guardando...' : 'Procesando...' }}
          </span>
          <span v-else>{{ modo === 'EDIT' ? 'Guardar Cambios' : 'Confirmar y Pagar' }}</span>
        </button>

        <button 
          v-if="modo === 'EDIT'"
          @click="emit('cancelar-edicion')"
          class="w-full py-3 mt-3 bg-white text-gray-600 hover:text-gray-900 font-bold rounded-xl transition-colors"
        >
          Cancelar Edición
        </button>
      </div>

    </div>
  </div>
</template>
