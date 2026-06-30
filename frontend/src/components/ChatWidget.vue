<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import SockJS from 'sockjs-client/dist/sockjs'
import { Client } from '@stomp/stompjs'
import { obtenerIniciales, formatearFecha, formatearHora } from '@/utils/formatters'

const authStore = useAuthStore()

const estaAbierto = ref(false)
const cargando = ref(false)
const partidasActivas = ref([])
const partidaSeleccionada = ref(null)

const mensajes = ref([])
const nuevoMensaje = ref('')
const contenedorMensajes = ref(null)
const mensajesNoLeidos = ref({})

let stompClient = null
let currentSubscription = null

const alternarWidget = () => {
  estaAbierto.value = !estaAbierto.value
  if (estaAbierto.value && partidasActivas.value.length === 0) {
    cargarPartidas()
  }
}

const cargarPartidas = async () => {
  cargando.value = true
  try {
    const { data } = await api.get(`/reservas/usuario/${authStore.usuario.id}`)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    partidasActivas.value = data.filter(r => 
      r.esAbierta === true && 
      r.estadoPago !== 'CANCELADO' &&
      new Date(r.fecha) >= today
    )
  } catch (err) {
    console.error('Error al cargar partidas para el chat', err)
  } finally {
    cargando.value = false
  }
}

const seleccionarPartida = async (match) => {
  partidaSeleccionada.value = match
  mensajesNoLeidos.value[match.id] = 0
  await cargarMensajes(match.id)
  conectarWebSocket(match.id)
}

const volverALaLista = () => {
  if (currentSubscription) {
    currentSubscription.unsubscribe()
    currentSubscription = null
  }
  partidaSeleccionada.value = null
  mensajes.value = []
}

const cargarMensajes = async (matchId) => {
  try {
    const { data } = await api.get(`/chat/reserva/${matchId}?usuarioId=${authStore.usuario.id}`)
    mensajes.value = data
    desplazarAbajo()
  } catch (err) {
    console.error('Error al cargar historial de chat', err)
  }
}

const conectarWebSocket = (matchId) => {
  if (stompClient && stompClient.connected) {
    suscribirseAPartida(matchId)
    return
  }

  const socket = new SockJS('http://localhost:8080/ws-chat')
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      suscribirseAPartida(matchId)
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
    }
  })

  stompClient.activate()
}

const suscribirseAPartida = (matchId) => {
  if (currentSubscription) {
    currentSubscription.unsubscribe()
  }
  
  currentSubscription = stompClient.subscribe(`/topic/chat/${matchId}`, (message) => {
    const parsedMessage = JSON.parse(message.body)
    mensajes.value.push(parsedMessage)
    desplazarAbajo()
    
    if (!estaAbierto.value || partidaSeleccionada.value?.id !== matchId) {
      mensajesNoLeidos.value[matchId] = (mensajesNoLeidos.value[matchId] || 0) + 1
    }
  })
}

const enviarMensaje = () => {
  if (!nuevoMensaje.value.trim() || !stompClient || !stompClient.connected) return

  const messageDto = {
    reservaId: partidaSeleccionada.value.id,
    usuarioId: authStore.usuario.id,
    mensaje: nuevoMensaje.value.trim()
  }

  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify(messageDto)
  })

  nuevoMensaje.value = ''
}

const desplazarAbajo = async () => {
  await nextTick()
  if (contenedorMensajes.value) {
    contenedorMensajes.value.scrollTop = contenedorMensajes.value.scrollHeight
  }
}

const formatearFechaLocal = (dateString) => {
  return formatearFecha(dateString, { hour: '2-digit', minute: '2-digit' })
}

const totalNoLeidos = computed(() => {
  return Object.values(mensajesNoLeidos.value).reduce((a, b) => a + b, 0)
})

const manejarReservaActualizada = async () => {
  if (authStore.isLogged()) {
    await cargarPartidas()
    if (partidaSeleccionada.value) {
      const stillActive = partidasActivas.value.some(m => m.id === partidaSeleccionada.value.id)
      if (!stillActive) {
        volverALaLista()
      }
    }
  }
}

onMounted(async () => {
  if (authStore.isLogged()) {
    await cargarPartidas()
  }
  window.addEventListener('reserva-actualizada', manejarReservaActualizada)
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
  }
  window.removeEventListener('reserva-actualizada', manejarReservaActualizada)
})

</script>

<template>
  <div v-if="authStore.isLogged()" class="fixed bottom-6 right-6 z-50 font-sans flex flex-col items-end">
    
    <div 
      v-if="estaAbierto" 
      class="bg-white rounded-2xl shadow-2xl border border-gray-200 w-80 sm:w-96 mb-4 flex flex-col overflow-hidden transition-all duration-300 transform origin-bottom-right h-[32rem]"
    >
      <div class="bg-gray-900 text-white p-4 flex items-center justify-between shadow-sm">
        <div class="flex items-center gap-3">
          <button v-if="partidaSeleccionada" @click="volverALaLista" class="hover:bg-gray-700 p-2 rounded-full transition bg-gray-800 border border-gray-700 flex items-center justify-center w-8 h-8" title="Volver a la lista">
            <i class="fas fa-arrow-left text-sm"></i>
          </button>
          <div class="flex flex-col cursor-pointer" @click="partidaSeleccionada ? volverALaLista() : null" title="Volver a la lista">
            <span class="font-bold text-sm sm:text-base">
              {{ partidaSeleccionada ? partidaSeleccionada.nombrePista : 'Chat Partidas Abiertas' }}
            </span>
            <span v-if="partidaSeleccionada" class="text-xs text-gray-300">{{ partidaSeleccionada.nombreCentro }}</span>
          </div>
        </div>
        <button @click="alternarWidget" class="hover:bg-gray-800 p-1 w-8 h-8 rounded-full transition flex items-center justify-center text-gray-300 hover:text-white">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div v-if="!partidaSeleccionada" class="flex-1 overflow-y-auto bg-gray-50 p-2">
        <div v-if="cargando" class="flex justify-center items-center h-full text-gray-400">
          <i class="fas fa-spinner fa-spin text-2xl"></i>
        </div>
        <div v-else-if="partidasActivas.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400 p-6 text-center">
          <i class="fas fa-comments text-4xl mb-3 text-gray-300"></i>
          <p class="text-sm">No estás participando en ninguna partida abierta próximamente.</p>
        </div>
        <div v-else class="flex flex-col gap-2">
          <div 
            v-for="match in partidasActivas" 
            :key="match.id"
            @click="seleccionarPartida(match)"
            class="bg-white p-4 rounded-xl border border-gray-100 shadow-sm cursor-pointer hover:border-blue-300 hover:shadow-md transition flex items-center justify-between gap-3 relative"
          >
            <div class="flex-1 min-w-0">
              <h4 class="font-bold text-gray-800 truncate text-lg">{{ match.nombrePista }}</h4>
              <p class="text-sm text-gray-500 truncate">- {{ match.fecha }} a las {{ formatearHora(match.horaInicio) }}</p>
            </div>
            
            <div v-if="mensajesNoLeidos[match.id]" class="bg-red-500 text-white text-xs font-bold w-6 h-6 flex items-center justify-center rounded-full shrink-0 shadow-sm">
              {{ mensajesNoLeidos[match.id] }}
            </div>
          </div>
        </div>
      </div>

      <div v-else class="flex-1 flex flex-col bg-gray-50 min-h-0">
        <div class="flex-1 p-4 overflow-y-auto flex flex-col gap-3" ref="contenedorMensajes">
          <div v-if="mensajes.length === 0" class="text-center text-gray-400 text-sm mt-10">
            No hay mensajes aún. ¡Escribe el primero!
          </div>
          
          <div 
            v-for="msg in mensajes" 
            :key="msg.id"
            class="flex items-end gap-2 w-full"
            :class="msg.usuarioId === authStore.usuario.id ? 'justify-end' : 'justify-start'"
          >
            <div v-if="msg.usuarioId !== authStore.usuario.id" class="flex-shrink-0">
              <img v-if="msg.fotoUsuario" :src="msg.fotoUsuario" alt="Perfil" class="w-8 h-8 rounded-full object-cover border border-gray-200 shadow-sm" />
              <div v-else class="w-8 h-8 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-xs font-bold border border-gray-300 shadow-sm">
                {{ obtenerIniciales(msg.nombreUsuario, msg.apellidosUsuario) }}
              </div>
            </div>

            <div 
              class="flex flex-col max-w-[75%]"
              :class="msg.usuarioId === authStore.usuario.id ? 'items-end' : 'items-start'"
            >
              <span class="text-[10px] text-gray-500 mb-1 px-1">{{ msg.nombreUsuario }}</span>
              <div 
                class="px-3 py-2 rounded-2xl text-sm shadow-sm"
                :class="msg.usuarioId === authStore.usuario.id ? 'bg-blue-600 text-white rounded-br-none' : 'bg-white text-gray-800 border border-gray-100 rounded-bl-none'"
              >
                {{ msg.mensaje }}
              </div>
              <span class="text-[9px] text-gray-400 mt-1 px-1">{{ formatearFechaLocal(msg.fechaEnvio) }}</span>
            </div>

            <div v-if="msg.usuarioId === authStore.usuario.id" class="flex-shrink-0">
              <img v-if="msg.fotoUsuario" :src="msg.fotoUsuario" alt="Perfil" class="w-8 h-8 rounded-full object-cover border border-gray-200 shadow-sm" />
              <div v-else class="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold border border-blue-200 shadow-sm">
                {{ obtenerIniciales(msg.nombreUsuario, msg.apellidosUsuario) }}
              </div>
            </div>
          </div>
        </div>

        <div class="p-3 bg-white border-t border-gray-200">
          <form @submit.prevent="enviarMensaje" class="flex items-center gap-2">
            <input 
              v-model="nuevoMensaje" 
              type="text" 
              placeholder="Escribe un mensaje..."
              class="flex-1 border border-gray-300 rounded-full px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            />
            <button 
              type="submit"
              :disabled="!nuevoMensaje.trim()"
              class="w-10 h-10 bg-blue-600 text-white rounded-full flex items-center justify-center hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex-shrink-0"
            >
              <i class="fas fa-paper-plane"></i>
            </button>
          </form>
        </div>
      </div>
    </div>

    <button 
      @click="alternarWidget"
      class="w-14 h-14 bg-gray-900 text-white rounded-full shadow-xl flex items-center justify-center text-2xl hover:bg-gray-800 hover:scale-105 transition-all focus:outline-none focus:ring-4 focus:ring-gray-300 relative"
    >
      <i :class="estaAbierto ? 'fas fa-times' : 'fas fa-comment-dots'"></i>
      <span 
        v-if="!estaAbierto && totalNoLeidos > 0" 
        class="absolute -top-1 -right-1 bg-red-500 border-2 border-white text-white text-xs font-bold w-6 h-6 flex items-center justify-center rounded-full"
      >
        {{ totalNoLeidos }}
      </span>
    </button>
  </div>
</template>
