import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const storedUser = localStorage.getItem('usuario')
  const usuario = ref(storedUser ? JSON.parse(storedUser) : null)

  const isLogged = () => usuario.value !== null
  const isAdmin = () => usuario.value?.rol === 'ADMINISTRADOR_CENTRO'
  const isDeportista = () => usuario.value?.rol === 'DEPORTISTA'

  function login(userData) {
    usuario.value = userData
    localStorage.setItem('usuario', JSON.stringify(userData))
  }

  function logout() {
    usuario.value = null
    localStorage.removeItem('usuario')
  }

  async function refreshUser(api) {
    if (!usuario.value) return
    try {
      const res = await api.get(`/usuarios/${usuario.value.id}`)
      usuario.value = res.data
      localStorage.setItem('usuario', JSON.stringify(res.data))
    } catch (e) {
      console.error('Error actualizando perfil desde el servidor:', e)
    }
  }

  return {
    usuario,
    isLogged,
    isAdmin,
    isDeportista,
    login,
    logout,
    refreshUser
  }
})
