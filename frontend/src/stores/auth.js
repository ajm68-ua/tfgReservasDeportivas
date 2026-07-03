import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const storedUser = localStorage.getItem('usuario')
  const storedToken = localStorage.getItem('token')
  const usuario = ref(storedUser ? JSON.parse(storedUser) : null)
  const token = ref(storedToken || null)

  const isLogged = () => usuario.value !== null && token.value !== null
  const isAdmin = () => usuario.value?.rol === 'ADMINISTRADOR_CENTRO'
  const isDeportista = () => usuario.value?.rol === 'DEPORTISTA'

  function login(userData, jwtToken) {
    usuario.value = userData

    const { foto, ...userWithoutFoto } = userData
    localStorage.setItem('usuario', JSON.stringify(userWithoutFoto))

    if (jwtToken) {
      token.value = jwtToken
      localStorage.setItem('token', jwtToken)
    }
  }

  function logout() {
    usuario.value = null
    token.value = null
    localStorage.removeItem('usuario')
    localStorage.removeItem('token')
  }

  async function refreshUser(api) {
    if (!usuario.value) return
    try {
      const res = await api.get(`/usuarios/${usuario.value.id}`)
      usuario.value = res.data
      const { foto, ...userWithoutFoto } = res.data
      localStorage.setItem('usuario', JSON.stringify(userWithoutFoto))
    } catch (e) {
      console.error('Error actualizando perfil desde el servidor:', e)
    }
  }

  return {
    usuario,
    token,
    isLogged,
    isAdmin,
    isDeportista,
    login,
    logout,
    refreshUser
  }
})
