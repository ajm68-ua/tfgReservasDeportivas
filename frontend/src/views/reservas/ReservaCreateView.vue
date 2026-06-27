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

const pistaId = route.query.pistaId
const pista = ref(null)
const centro = ref(null)
const cargando = ref(true)

const reservas = ref([])
const guardando = ref(false)

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
    const resPista = await api.get(`/pistas/${pistaId}`)
    pista.value = resPista.data

    const resCentro = await api.get(`/centros/${pista.value.centroId}`)
    centro.value = resCentro.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar los datos de la pista')
    router.push('/pistas')
  } finally {
    cargando.value = false
  }
})

async function cargarDisponibilidad(fecha) {
  if (!fecha || !centro.value) return
  
  try {
    const res = await api.get(`/reservas/pista/${pistaId}?fecha=${fecha}`)
    reservas.value = res.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar la disponibilidad')
  }
}

async function confirmarReserva(datosReserva) {
  guardando.value = true
  try {
    await api.post('/reservas', {
      pistaId: pista.value.id,
      organizadorId: authStore.usuario.id,
      ...datosReserva
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

    <LoadingSpinner v-if="cargando" full-screen />

    <main v-else class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      <ReservaFormulario 
        :pista="pista"
        :centro="centro"
        :reservasDelDia="reservas"
        :guardando="guardando"
        modo="CREATE"
        @fecha-changed="cargarDisponibilidad"
        @submit="confirmarReserva"
      />
    </main>
  </div>
</template>
