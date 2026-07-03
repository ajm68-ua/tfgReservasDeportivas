<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const cargando = ref(false)
const errorMensaje = ref('')

const form = reactive({
  email: '',
  password: ''
})

function validar() {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.email.trim() || !emailRegex.test(form.email)) return 'Introduce un correo electrónico válido.'
  if (!form.password.trim()) return 'La contraseña es obligatoria.'
  
  return null
}

async function iniciarSesion() {
  errorMensaje.value = ''
  const error = validar()
  if (error) { errorMensaje.value = error; return }

  cargando.value = true
  try {
    const respuesta = await api.post('/usuarios/login', {
      email: form.email,
      password: form.password
    })
    
    authStore.login(respuesta.data.usuario, respuesta.data.token)

    if (authStore.isAdmin()) {
      router.push('/admin')
    } else {
      router.push('/')
    }
    
  } catch (err) {
    if (err.response?.status === 401) {
      errorMensaje.value = 'Correo o contraseña incorrectos.'
    } else {
      errorMensaje.value = 'Ha ocurrido un error. Inténtalo de nuevo.'
    }
  } finally {
    cargando.value = false
  }
}
</script>

<template>
  <div class="bg-white font-sans flex items-center justify-center min-h-[calc(100vh-64px)] px-6 py-10">
    <div class="w-full max-w-sm">

      <h1 class="text-2xl font-extrabold text-gray-900 tracking-tight mb-1 text-center">¡Hola de nuevo!</h1>
      <p class="text-gray-500 text-sm mb-6 text-center">Inicia sesión con tu cuenta</p>

      <div v-if="errorMensaje" class="flex items-center gap-2 bg-red-50 border border-red-200 text-red-500 rounded-xl px-3 py-2.5 text-sm mb-5">
        <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10" stroke-width="2"/>
          <line x1="12" y1="8" x2="12" y2="12" stroke-width="2"/>
          <line x1="12" y1="16" x2="12.01" y2="16" stroke-width="2"/>
        </svg>
        {{ errorMensaje }}
      </div>

      <div class="mb-4">
        <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="login-email">CORREO ELECTRÓNICO</label>
        <input
          id="login-email"
          v-model="form.email"
          type="email"
          placeholder="tu@email.com"
          @keyup.enter="iniciarSesion"
          class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
        />
      </div>

      <div class="mb-6">
        <div class="flex justify-between items-center mb-1">
          <label class="block text-[11px] font-semibold text-gray-500" for="login-password">CONTRASEÑA</label>
        </div>
        <input
          id="login-password"
          v-model="form.password"
          type="password"
          placeholder="Tu contraseña"
          @keyup.enter="iniciarSesion"
          class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
        />
      </div>

      <button
        @click="iniciarSesion"
        :disabled="cargando"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold text-sm py-2.5 px-6 rounded-full transition flex items-center justify-center gap-2 shadow-md hover:shadow-lg hover:-translate-y-0.5"
      >
        <svg v-if="cargando" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
        </svg>
        <span v-if="!cargando">Entrar</span>
        <span v-else>Entrando…</span>
      </button>

      <p class="text-center text-sm text-gray-500 mt-6">
        ¿Aún no tienes cuenta?
      </p>
      <div class="flex flex-col gap-2 mt-2">
        <router-link to="/registro" class="block w-full text-center bg-gray-50 hover:bg-gray-100 text-gray-700 font-semibold text-sm py-2 px-6 rounded-full transition border border-gray-200">
          Registrarse como Deportista
        </router-link>
        <router-link to="/registro-centro" class="block w-full text-center bg-gray-50 hover:bg-gray-100 text-gray-700 font-semibold text-sm py-2 px-6 rounded-full transition border border-gray-200">
          Soy un Centro Deportivo
        </router-link>
      </div>

    </div>
  </div>
</template>
