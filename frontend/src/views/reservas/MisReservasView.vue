<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { toast } from 'vue3-toastify'
import PageHeader from '@/components/ui/PageHeader.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ReservaCard from '@/components/ReservaCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const reservas = ref([])
const cargando = ref(true)
const isProcessing = ref(false)

onMounted(async () => {
  if (!authStore.isLogged()) {
    toast.error('Debes iniciar sesión para ver tus reservas')
    router.push('/login')
    return
  }
  await cargarReservas()
})

async function cargarReservas() {
  cargando.value = true
  try {
    const res = await api.get(`/reservas/usuario/${authStore.usuario.id}`)
    reservas.value = res.data
  } catch (err) {
    console.error(err)
    toast.error('Error al cargar tus reservas')
  } finally {
    cargando.value = false
  }
}

async function cancelarReserva(id) {
  if (isProcessing.value) return
  if (!confirm('¿Estás seguro de que quieres cancelar esta reserva?')) return

  isProcessing.value = true
  try {
    await api.put(`/reservas/${id}/cancelar`)
    
    await authStore.refreshUser(api)

    toast.success('Reserva cancelada correctamente')
    await cargarReservas()
    window.dispatchEvent(new CustomEvent('reserva-actualizada'))
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al cancelar la reserva')
  } finally {
    isProcessing.value = false
  }
}

async function abandonarPartida(id) {
  if (isProcessing.value) return
  if (!confirm('¿Quieres abandonar esta partida? Se te devolverá la parte proporcional de la pista a tu saldo.')) return

  isProcessing.value = true
  try {
    await api.post(`/reservas/${id}/abandonar?usuarioId=${authStore.usuario.id}`)
    await authStore.refreshUser(api)

    toast.success('Has abandonado la partida con éxito')
    await cargarReservas()
    window.dispatchEvent(new CustomEvent('reserva-actualizada'))
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al abandonar la partida')
  } finally {
    isProcessing.value = false
  }
}

async function eliminarDefinitivamente(id) {
  if (isProcessing.value) return
  if (!confirm('¿Estás seguro de que quieres eliminar esta reserva del historial? Esta acción no se puede deshacer.')) return

  isProcessing.value = true
  try {
    await api.delete(`/reservas/${id}`)
    toast.success('Reserva eliminada del historial')
    await cargarReservas()
    window.dispatchEvent(new CustomEvent('reserva-actualizada'))
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al eliminar la reserva')
  } finally {
    isProcessing.value = false
  }
}

async function reReservar(reserva) {
  if (isProcessing.value) return
  isProcessing.value = true
  try {
    await api.put(`/reservas/${reserva.id}/reactivar`)
    toast.success('Reserva reactivada con éxito. Vuelve a estar pendiente de pago.')
    await cargarReservas()
  } catch (err) {
    console.error(err)
    toast.error(err.response?.data?.message || 'Error al reactivar la reserva')
  } finally {
    isProcessing.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    
    <PageHeader 
      title="Mis Reservas" 
      subtitle="Aquí puedes gestionar todas tus reservas" 
    />

    <main class="max-w-7xl mx-auto px-6 lg:px-8 py-10 w-full flex-grow flex flex-col gap-8">
      <LoadingSpinner v-if="cargando" full-screen />

      <EmptyState 
        v-else-if="reservas.length === 0"
        descripcion="Todavía no tienes ninguna reserva. ¡Anímate a reservar una pista!"
        texto-boton="Reservar Pista"
        @action="router.push('/pistas')"
      />

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <ReservaCard 
          v-for="reserva in reservas" 
          :key="reserva.id"
          :reserva="reserva"
          @pagar="id => router.push(`/reservas/${id}/pago`)"
          @editar="id => router.push(`/reservas/${id}/editar`)"
          @cancelar="cancelarReserva"
          @abandonar="abandonarPartida"
          @re-reservar="reReservar"
          @eliminar="eliminarDefinitivamente"
        />
      </div>
    </main>
  </div>
</template>
