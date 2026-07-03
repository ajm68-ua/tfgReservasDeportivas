<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import { useAuthStore } from '@/stores/auth'
import { toast } from 'vue3-toastify'

const router = useRouter()
const authStore = useAuthStore()

const cargando = ref(false)

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
  const error = validar()
  if (error) { toast.error(error); return }

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
      toast.error('Correo o contraseña incorrectos.')
    } else {
      toast.error('Ha ocurrido un error. Inténtalo de nuevo.')
    }
  } finally {
    cargando.value = false
  }
}
</script>

<template>
  <div class="bg-white font-sans flex items-center justify-center min-h-[calc(100vh-64px)] px-6 py-10">
    <div class="w-full max-w-sm">

      <div class="mb-8">
        <h2 class="text-3xl font-bold text-gray-900 tracking-tight">Iniciar Sesión</h2>
        <p class="text-sm text-gray-500 mt-1">Introduce tus datos para iniciar sesión</p>
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
