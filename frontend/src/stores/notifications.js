import { defineStore } from 'pinia'
import api from '@/services/api'
import SockJS from 'sockjs-client/dist/sockjs'
import { Client } from '@stomp/stompjs'
import { useAuthStore } from './auth'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notificaciones: [],
    stompClient: null
  }),

  getters: {
    noLeidasCount: (state) => state.notificaciones.filter(n => !n.leido).length,
    notificacionesOrdenadas: (state) => {
      return [...state.notificaciones].sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion))
    }
  },

  actions: {
    async fetchNotificaciones() {
      const authStore = useAuthStore()
      if (!authStore.usuario) return

      try {
        const { data } = await api.get(`/notificaciones/usuario/${authStore.usuario.id}`)
        this.notificaciones = data
      } catch (err) {
        console.error('Error al cargar notificaciones', err)
      }
    },

    async marcarComoLeida(id) {
      try {
        await api.put(`/notificaciones/${id}/leer`)
        const notif = this.notificaciones.find(n => n.id === id)
        if (notif) notif.leido = true
      } catch (err) {
        console.error('Error al marcar notificacion como leída', err)
      }
    },

    async borrarNotificacion(id) {
      try {
        await api.delete(`/notificaciones/${id}`)
        this.notificaciones = this.notificaciones.filter(n => n.id !== id)
      } catch (err) {
        console.error('Error al borrar notificación', err)
      }
    },

    async marcarTodasComoLeidas() {
      const authStore = useAuthStore()
      if (!authStore.usuario) return

      try {
        await api.put(`/notificaciones/usuario/${authStore.usuario.id}/leer-todas`)
        this.notificaciones.forEach(n => n.leido = true)
      } catch (err) {
        console.error('Error al marcar todas las notificaciones como leídas', err)
      }
    },

    conectarWebSocket() {
      const authStore = useAuthStore()
      if (!authStore.usuario) return

      if (this.stompClient && this.stompClient.connected) {
        return
      }

      const socket = new SockJS('http://localhost:8080/ws-chat')
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      this.stompClient.onConnect = () => {
        this.stompClient.subscribe(`/topic/notificaciones/${authStore.usuario.id}`, (message) => {
          if (message.body) {
            const nuevaNotificacion = JSON.parse(message.body)
            this.notificaciones.unshift(nuevaNotificacion)
          }
        })
      }

      this.stompClient.onStompError = (frame) => {
        console.error('Error en STOMP de notificaciones:', frame.headers['message'])
      }

      this.stompClient.activate()
    },

    desconectarWebSocket() {
      if (this.stompClient) {
        this.stompClient.deactivate()
        this.stompClient = null
      }
    }
  }
})
