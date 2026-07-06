<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import { toast } from 'vue3-toastify'

const router = useRouter()

import { NIVELES_OPCIONES } from '@/utils/constants'

const niveles = NIVELES_OPCIONES

const cargando = ref(false)
const exitoso = ref(false)

const form = reactive({
  nombre: '',
  apellidos: '',
  email: '',
  password: '',
  confirmarPassword: '',
  telefono: '',
  ciudad: '',
  nivel: '',
})

function validar() {
  if (!form.nombre.trim()) return 'El nombre es obligatorio.'
  if (!form.apellidos.trim()) return 'Los apellidos son obligatorios.'
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.email.trim() || !emailRegex.test(form.email)) return 'Introduce un correo electrónico válido.'
  
  if (form.password.length < 4) return 'La contraseña debe tener al menos 4 caracteres.'
  if (form.password !== form.confirmarPassword) return 'Las contraseñas no coinciden.'
  
  if (form.telefono && form.telefono.trim()) {
    const telefonoRegex = /^[0-9\s\+\-]{9,15}$/
    if (!telefonoRegex.test(form.telefono)) {
      return 'Introduce un teléfono válido.'
    }
  }

  if (form.ciudad && form.ciudad.trim()) {
    const ciudadRegex = /^[a-zA-ZÀ-ÿ\s\-\']+$/
    if (form.ciudad.trim().length < 2 || !ciudadRegex.test(form.ciudad)) {
      return 'Introduce una ciudad válida (solo letras y espacios).'
    }
  }

  if (!form.nivel) return 'Selecciona tu nivel de juego.'
  return null
}

async function registrar() {
  const error = validar()
  if (error) { toast.error(error); return }

  cargando.value = true
  try {
    await api.post('/usuarios/registro', {
      nombre: form.nombre,
      apellidos: form.apellidos,
      email: form.email,
      password: form.password,
      telefono: form.telefono || null,
      ciudad: form.ciudad || null,
      nivel: form.nivel || null,
    })
    exitoso.value = true
    setTimeout(() => router.push({ name: 'login' }), 2500)
  } catch (err) {
    if (err.response?.status === 409) {
      toast.error('Ese correo ya está registrado.')
    } else {
      toast.error('Ha ocurrido un error. Inténtalo de nuevo.')
    }
  } finally {
    cargando.value = false
  }
}
</script>

<template>
  <div class="bg-white font-sans flex justify-center px-6 pt-10 pb-10">
    <div class="w-full max-w-md">

      <div v-if="exitoso" class="text-center py-10">
        <div class="w-20 h-20 rounded-full bg-green-100 border-2 border-green-300 text-green-500 text-3xl flex items-center justify-center mx-auto mb-6">
          ✓
        </div>
        <h2 class="text-2xl font-extrabold text-gray-900 mb-2">¡Registro completado!</h2>
        <p class="text-gray-400 text-sm">Redirigiendo al inicio de sesión…</p>
      </div>

      <template v-else>
        <div class="mb-6">
          <h1 class="text-2xl font-extrabold text-gray-900 tracking-tight mb-1">Crea tu cuenta</h1>
          <p class="text-gray-500 text-sm">Introduce tus datos para empezar a reservar</p>
        </div>

        <div class="grid grid-cols-2 gap-3 mb-3">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-nombre">NOMBRE</label>
            <input
              id="reg-nombre"
              v-model="form.nombre"
              type="text"
              placeholder="Tu nombre"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-apellidos">APELLIDOS</label>
            <input
              id="reg-apellidos"
              v-model="form.apellidos"
              type="text"
              placeholder="Tus apellidos"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
        </div>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-email">CORREO ELECTRÓNICO</label>
          <input
            id="reg-email"
            v-model="form.email"
            type="email"
            placeholder="tu@email.com"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-password">CONTRASEÑA</label>
          <input
            id="reg-password"
            v-model="form.password"
            type="password"
            placeholder="Mínimo 4 caracteres"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-confirm">CONFIRMAR CONTRASEÑA</label>
          <input
            id="reg-confirm"
            v-model="form.confirmarPassword"
            type="password"
            placeholder="Repite tu contraseña"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="grid grid-cols-2 gap-3 mb-3">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-telefono">
              TELEFONO <span class="text-gray-300 font-normal normal-case tracking-normal">Opcional</span>
            </label>
            <input
              id="reg-telefono"
              v-model="form.telefono"
              type="tel"
              placeholder="600 000 000"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="reg-ciudad">
              CIUDAD <span class="text-gray-300 font-normal normal-case tracking-normal">Opcional</span>
            </label>
            <input
              id="reg-ciudad"
              v-model="form.ciudad"
              type="text"
              placeholder="Tu ciudad"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
        </div>

        <div class="mb-5">
          <label class="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            NIVEL DE JUEGO
          </label>
          <div class="flex flex-wrap gap-1.5 sm:grid sm:grid-cols-4 sm:gap-2">
            <button
              v-for="opcion in niveles"
              :key="opcion.value"
              type="button"
              @click="form.nivel = opcion.value"
              class="py-2 px-2 rounded-lg text-[13px] font-medium transition-all duration-200 outline-none flex-1 sm:flex-none text-center"
              :class="form.nivel === opcion.value
                ? 'border-2 border-blue-600 bg-blue-50 text-blue-600'
                : 'border-2 border-gray-200 bg-white text-gray-500 hover:border-gray-300 hover:bg-gray-50'"
            >
              {{ opcion.label }}
            </button>
          </div>
        </div>

        <button
          @click="registrar"
          :disabled="cargando"
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold text-sm py-2.5 px-6 rounded-full transition flex items-center justify-center gap-2 shadow-md hover:shadow-lg hover:-translate-y-0.5"
        >
          <svg v-if="cargando" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
          </svg>
          <span v-if="!cargando">Crear cuenta</span>
          <span v-else>Creando…</span>
        </button>

        <p class="text-center text-sm text-gray-500 mt-4">
          ¿Ya tienes cuenta?
          <router-link to="/login" class="text-blue-600 font-semibold hover:text-blue-500 transition ml-1">
            Inicia sesión
          </router-link>
        </p>
        <p class="text-center text-sm text-gray-500 mt-1">
          ¿Eres un centro deportivo?
          <router-link to="/registro-centro" class="text-blue-600 font-semibold hover:text-blue-500 transition ml-1">
            ¡Haz click aquí!
          </router-link>
        </p>
      </template>

    </div>
  </div>
</template>
