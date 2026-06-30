<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { obtenerIniciales, formatearDinero, formatearFecha } from '@/utils/formatters'
import logoLfg from '@/assets/images/Logo LFGHub.png'
import { useNotificationStore } from '@/stores/notifications'

const authStore = useAuthStore()
const router = useRouter()
const notificationStore = useNotificationStore()

const menuAbierto = ref(false)
const notificacionesAbiertas = ref(false)
const pestanaNotificaciones = ref('NO_LEIDAS')

const manejarClickNotificacion = (notif) => {
  notificationStore.marcarComoLeida(notif.id)
  notificacionesAbiertas.value = false
  
  if (notif.tipo === 'CHAT') {
    window.dispatchEvent(new CustomEvent('abrir-chat', { detail: { reservaId: notif.referenciaId } }))
  } else if (notif.tipo === 'NUEVA_PARTIDA') {
    router.push('/partidas')
  }
}

onMounted(() => {
  if (authStore.isLogged()) {
    notificationStore.fetchNotificaciones()
    notificationStore.conectarWebSocket()
  }
})

onUnmounted(() => {
  notificationStore.desconectarWebSocket()
})

function cerrarSesion() {
  menuAbierto.value = false
  authStore.logout()
  router.push('/login')
}

const inicialesUsuario = computed(() => {
  if (!authStore.usuario) return ''
  return obtenerIniciales(authStore.usuario.nombre, authStore.usuario.apellidos)
})
</script>

<template>
  <header class="flex items-center justify-between px-8 py-4 bg-white border-b border-gray-100 sticky top-0 z-50">
    <div class="w-1/4 flex items-center">
      <RouterLink to="/">
        <img :src="logoLfg" alt="LFG Hub Logo" class="h-12 object-contain cursor-pointer" />
      </RouterLink>
    </div>

    <nav class="hidden md:flex flex-1 justify-center gap-8 text-sm font-semibold text-gray-500">
      <RouterLink to="/pistas" class="hover:text-gray-900 transition-colors">Buscar Pistas</RouterLink>
      <RouterLink to="/partidas" class="hover:text-gray-900 transition-colors">Partidas Abiertas</RouterLink>
      <RouterLink to="/centros" class="hover:text-gray-900 transition-colors">Centros Deportivos</RouterLink>
      <RouterLink v-if="authStore.isLogged()" to="/mis-reservas" class="hover:text-gray-900 transition-colors">Mis Reservas</RouterLink>
    </nav>

    <div class="w-1/4 flex items-center justify-end gap-3">
      <template v-if="!authStore.isLogged()">
        <RouterLink to="/login">
          <button class="px-5 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-md hover:bg-gray-50 transition-colors">
            Iniciar Sesión
          </button>
        </RouterLink>
        <RouterLink to="/registro">
          <button class="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors">
            Registrarse
          </button>
        </RouterLink>
      </template>

      <template v-else>
        <div v-if="menuAbierto || notificacionesAbiertas" class="fixed inset-0 z-40" @click="menuAbierto = false; notificacionesAbiertas = false"></div>

        <div class="relative z-50 flex items-center gap-3">
          
          <div class="relative">
            <button @click="notificacionesAbiertas = !notificacionesAbiertas; menuAbierto = false" class="relative p-2 text-gray-500 hover:text-gray-700 transition">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
              <span v-if="notificationStore.noLeidasCount > 0" class="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold flex items-center justify-center rounded-full shadow-sm">
                {{ notificationStore.noLeidasCount > 9 ? '9+' : notificationStore.noLeidasCount }}
              </span>
            </button>

            <div v-if="notificacionesAbiertas" class="absolute right-0 top-12 mt-2 w-80 bg-white rounded-xl shadow-lg py-2 border border-gray-100 animate-fade-in-down z-50">
              <div class="px-4 py-3 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                <h3 class="text-sm font-bold text-gray-900">Notificaciones</h3>
                <button 
                  v-if="notificationStore.noLeidasCount > 0"
                  @click="notificationStore.marcarTodasComoLeidas()" 
                  class="text-xs text-blue-600 hover:text-blue-800 font-medium"
                >
                  Marcar todas leídas
                </button>
              </div>

              <div class="flex border-b border-gray-100">
                <button 
                  @click="pestanaNotificaciones = 'NO_LEIDAS'"
                  class="flex-1 py-2 text-xs font-semibold border-b-2 transition-colors"
                  :class="pestanaNotificaciones === 'NO_LEIDAS' ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-500 hover:text-gray-700'"
                >
                  No leídas ({{ notificationStore.noLeidasCount }})
                </button>
                <button 
                  @click="pestanaNotificaciones = 'LEIDAS'"
                  class="flex-1 py-2 text-xs font-semibold border-b-2 transition-colors"
                  :class="pestanaNotificaciones === 'LEIDAS' ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-500 hover:text-gray-700'"
                >
                  Leídas ({{ notificationStore.notificaciones.length - notificationStore.noLeidasCount }})
                </button>
              </div>
              
              <div class="max-h-80 overflow-y-auto">
                <div v-if="notificationStore.notificacionesOrdenadas.filter(n => (pestanaNotificaciones === 'LEIDAS' ? n.leido : !n.leido)).length === 0" class="p-4 text-center text-gray-500 text-sm">
                  No tienes notificaciones
                </div>
                
                <div 
                  v-for="notif in notificationStore.notificacionesOrdenadas.filter(n => (pestanaNotificaciones === 'LEIDAS' ? n.leido : !n.leido))" 
                  :key="notif.id"
                  class="p-4 border-b border-gray-50 hover:bg-gray-50 transition flex flex-col gap-1 last:border-b-0 relative group"
                  :class="{'bg-blue-50/30': !notif.leido}"
                >
                  <div 
                    class="cursor-pointer"
                    @click="manejarClickNotificacion(notif)"
                  >
                    <div class="flex justify-between items-start gap-2">
                      <p class="text-sm font-bold text-gray-900 pr-4" :class="{'text-blue-700': !notif.leido}">{{ notif.titulo }}</p>
                      <span v-if="!notif.leido" class="w-2 h-2 rounded-full bg-blue-600 flex-shrink-0 mt-1"></span>
                    </div>
                    <p class="text-xs text-gray-600 line-clamp-2 pr-4">{{ notif.mensaje }}</p>
                    <p class="text-[10px] text-gray-400 mt-1">{{ formatearFecha(notif.fechaCreacion, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) }}</p>
                  </div>
                  
                  <button 
                    @click.stop="notificationStore.borrarNotificacion(notif.id)"
                    class="absolute top-4 right-4 text-gray-300 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity p-1"
                    title="Eliminar notificación"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <span class="text-sm font-semibold text-gray-700 hidden sm:block">{{ authStore.usuario.nombre }}</span>
          
          <button @click="menuAbierto = !menuAbierto; notificacionesAbiertas = false" class="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold overflow-hidden">
            <img v-if="authStore.usuario.foto" :src="authStore.usuario.foto" alt="Perfil" class="w-full h-full object-cover" />
            <span v-else>{{ inicialesUsuario }}</span>
          </button>

          <div v-if="menuAbierto" class="absolute right-0 top-12 mt-2 w-56 bg-white rounded-xl shadow-lg py-2 border border-gray-100 animate-fade-in-down">
            <div class="px-4 py-3 border-b border-gray-100 mb-2 bg-gray-50/50">
              <p class="text-xs text-gray-500 font-medium uppercase tracking-wider mb-0.5">Saldo disponible</p>
              <p class="text-lg font-bold text-gray-900">{{ formatearDinero(authStore.usuario?.saldo) }}</p>
            </div>
            
            <RouterLink 
              :to="authStore.isAdmin() ? '/admin' : '/perfil'" 
              @click="menuAbierto = false"
              class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition"
            >
              Mi Perfil
            </RouterLink>
            <RouterLink 
              v-if="authStore.isAdmin()"
              to="/admin/pistas" 
              @click="menuAbierto = false"
              class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition"
            >
              Gestión de Pistas
            </RouterLink>
            <hr class="my-1 border-gray-100">
            <button 
              @click="cerrarSesion"
              class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition font-medium"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </template>
    </div>
  </header>
</template>
