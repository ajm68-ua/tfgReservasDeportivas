<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api.js'
import { toast } from 'vue3-toastify'

const router = useRouter()

const cargando = ref(false)
const exitoso = ref(false)

const form = reactive({
  nombre: '',
  ciudad: '',
  direccion: '',
  telefono: '',
  horarioApertura: '',
  horarioCierre: '',
  
  adminNombre: '',
  adminApellidos: '',
  adminEmail: '',
  adminPassword: '',
  adminConfirmarPassword: ''
})

function validar() {
  if (!form.nombre.trim()) return 'El nombre del centro es obligatorio.'
  if (!form.ciudad.trim()) return 'La ciudad es obligatoria.'
  if (!form.direccion.trim()) return 'La dirección es obligatoria.'
  if (!form.telefono.trim()) return 'El teléfono es obligatorio.'
  if (!form.horarioApertura) return 'El horario de apertura es obligatorio.'
  if (!form.horarioCierre) return 'El horario de cierre es obligatorio.'

  if (!form.adminNombre.trim()) return 'Tu nombre es obligatorio.'
  if (!form.adminApellidos.trim()) return 'Tus apellidos son obligatorios.'
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.adminEmail.trim() || !emailRegex.test(form.adminEmail)) return 'Introduce un correo electrónico válido.'
  
  if (form.adminPassword.length < 4) return 'La contraseña debe tener al menos 4 caracteres.'
  if (form.adminPassword !== form.adminConfirmarPassword) return 'Las contraseñas no coinciden.'

  const telefonoRegex = /^[0-9\s\+\-]{9,15}$/
  if (!telefonoRegex.test(form.telefono)) {
    return 'Introduce un teléfono válido.'
  }

  if (form.horarioApertura >= form.horarioCierre) {
    return 'El horario de cierre debe ser posterior al de apertura.'
  }

  return null
}

async function registrar() {
  const error = validar()
  if (error) { toast.error(error); return }

  cargando.value = true
  try {
    await api.post('/centros/registro', {
      centro: {
        nombre: form.nombre,
        ciudad: form.ciudad,
        direccion: form.direccion,
        telefono: form.telefono,
        horarioApertura: form.horarioApertura + ':00',
        horarioCierre: form.horarioCierre + ':00'
      },
      admin: {
        nombre: form.adminNombre,
        apellidos: form.adminApellidos,
        email: form.adminEmail,
        password: form.adminPassword
      }
    })
    exitoso.value = true
    setTimeout(() => router.push({ name: 'home' }), 2500)
  } catch (err) {
    if (err.response?.status === 409) {
      toast.error('El correo o el centro ya están registrados.')
    } else if (err.response?.status === 400) {
      toast.error('Revisa los campos, el formato de los datos es inválido.')
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
        <h2 class="text-2xl font-extrabold text-gray-900 mb-2">¡Centro registrado!</h2>
        <p class="text-gray-400 text-sm">Redirigiendo a la pantalla principal…</p>
      </div>

      <template v-else>
        <div class="mb-6">
          <h1 class="text-2xl font-extrabold text-gray-900 tracking-tight mb-1">Registra tu centro</h1>
          <p class="text-gray-500 text-sm">Date de alta como administrador de las instalaciones</p>
        </div>

        <h3 class="text-sm font-bold text-gray-900 mb-3 border-b border-gray-100 pb-2">1. Datos del Gerente</h3>
        
        <div class="grid grid-cols-2 gap-3 mb-3">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="admin-nombre">TU NOMBRE</label>
            <input
              id="admin-nombre"
              v-model="form.adminNombre"
              type="text"
              placeholder="Tu nombre"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="admin-apellidos">TUS APELLIDOS</label>
            <input
              id="admin-apellidos"
              v-model="form.adminApellidos"
              type="text"
              placeholder="Tus apellidos"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
        </div>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="admin-email">CORREO ELECTRÓNICO</label>
          <input
            id="admin-email"
            v-model="form.adminEmail"
            type="email"
            placeholder="tu@email.com"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="grid grid-cols-2 gap-3 mb-6">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="admin-password">CONTRASEÑA</label>
            <input
              id="admin-password"
              v-model="form.adminPassword"
              type="password"
              placeholder="Mínimo 4 caracteres"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="admin-confirm">CONFIRMAR</label>
            <input
              id="admin-confirm"
              v-model="form.adminConfirmarPassword"
              type="password"
              placeholder="Repite tu contraseña"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
        </div>

        <h3 class="text-sm font-bold text-gray-900 mb-3 border-b border-gray-100 pb-2">2. Datos de las Instalaciones</h3>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-nombre">NOMBRE DEL CENTRO</label>
          <input
            id="centro-nombre"
            v-model="form.nombre"
            type="text"
            placeholder="Ej: Padel Club Madrid"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="grid grid-cols-2 gap-3 mb-3">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-ciudad">CIUDAD</label>
            <input
              id="centro-ciudad"
              v-model="form.ciudad"
              type="text"
              placeholder="Tu ciudad"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-telefono">TELÉFONO</label>
            <input
              id="centro-telefono"
              v-model="form.telefono"
              type="tel"
              placeholder="600 000 000"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
        </div>

        <div class="mb-3">
          <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-direccion">DIRECCIÓN FÍSICA</label>
          <input
            id="centro-direccion"
            v-model="form.direccion"
            type="text"
            placeholder="Calle Mayor 1, Puerta 2"
            class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
          />
        </div>

        <div class="grid grid-cols-2 gap-3 mb-5">
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-apertura">HORARIO APERTURA</label>
            <input
              id="centro-apertura"
              v-model="form.horarioApertura"
              type="time"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
          </div>
          <div>
            <label class="block text-[11px] font-semibold text-gray-500 mb-1" for="centro-cierre">HORARIO CIERRE</label>
            <input
              id="centro-cierre"
              v-model="form.horarioCierre"
              type="time"
              class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition"
            />
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
          <span v-if="!cargando">Dar de alta centro</span>
          <span v-else>Registrando…</span>
        </button>
        
        <p class="text-center text-sm text-gray-500 mt-4">
          ¿Eres un jugador?
          <router-link to="/registro" class="text-blue-600 font-semibold hover:text-blue-500 transition ml-1">
            Regístrate aquí
          </router-link>
        </p>
      </template>

    </div>
  </div>
</template>
