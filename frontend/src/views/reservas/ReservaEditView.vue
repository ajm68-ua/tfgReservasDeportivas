<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ReservaFormulario from '@/components/ReservaFormulario.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const reservaId = route.params.id
const reservaActual = ref(null)

const pista = ref(null)
const centro = ref(null)
const cargando = ref(true)

const reservas = ref([])
const guardando = ref(false)

const initialData = ref({})

onMounted(async () => {
  if (!authStore.isLogged()) {
    toast.error('Debes iniciar sesión para modificar una reserva')
    router.push('/login')
    return
  }

  try {
    const resReserva = await api.get(`/reservas/${reservaId}`)
    const r = resReserva.data
    
    if (!r) throw new Error("Reserva no encontrada")
    if (r.estadoPago === 'CANCELADO') throw new Error("No se puede editar una reserva cancelada")
    
    reservaActual.value = r

    const horaInicioInt = parseInt(r.horaInicio.split(':')[0])
    const horaFinInt = parseInt(r.horaFin.split(':')[0])
    const prevSeleccionados = []
    for (let i = horaInicioInt; i < horaFinInt; i++) {
      const startStr = `${i.toString().padStart(2, '0')}:00:00`
      const endStr = `${(i + 1).toString().padStart(2, '0')}:00:00`
      prevSeleccionados.push({
        start: startStr,
        end: endStr,
        label: `${i.toString().padStart(2, '0')}:00 - ${(i + 1).toString().padStart(2, '0')}:00`,
        ocupado: false
      })
    }

    initialData.value = {
      fecha: r.fecha,
      nivel: r.nivel,
      esAbierta: r.esAbierta,
      bloquesSeleccionados: prevSeleccionados
    }

    const resPista = await api.get(`/pistas/${r.pistaId}`)
    pista.value = resPista.data

    const resCentro = await api.get(`/centros/${pista.value.centroId}`)
    centro.value = resCentro.data

  } catch (err) {
    console.error(err)
    toast.error(err.message || 'Error al cargar los datos')
    router.push('/mis-reservas')
  } finally {
    cargando.value = false
  }
})

async function cargarDisponibilidad(fecha) {
  if (!fecha || !centro.value || !pista.value) return
  
  try {
    const res = await api.get(`/reservas/pista/${pista.value.id}?fecha=${fecha}`)
    reservas.value = res.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar la disponibilidad')
  }
}

async function guardarCambios(datosReserva) {
  guardando.value = true
  try {
    await api.put(`/reservas/${reservaId}`, {
      pistaId: pista.value.id,
      organizadorId: authStore.usuario.id,
      ...datosReserva
    })
    
    toast.success('Reserva modificada con éxito')
    router.push('/mis-reservas')
  } catch (err) {
    console.error(err)
    if (err.response?.data) {
      toast.error(err.response.data.message || 'Error al modificar la reserva')
    } else {
      toast.error('Error al modificar la reserva')
    }
  } finally {
    guardando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <PageHeader 
      title="Modificar Reserva" 
      :subtitle="'Modifica tu reserva'" 
    />

    <LoadingSpinner v-if="cargando" full-screen />

    <main v-else class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      <ReservaFormulario 
        :pista="pista"
        :centro="centro"
        :reservasDelDia="reservas"
        :guardando="guardando"
        :initialData="initialData"
        :reservaIgnoradaId="reservaActual.id"
        modo="EDIT"
        @fecha-changed="cargarDisponibilidad"
        @submit="guardarCambios"
        @cancelar-edicion="router.push('/mis-reservas')"
      />
    </main>
  </div>
</template>
