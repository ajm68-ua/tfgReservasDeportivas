<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { MAPA_DEPORTES, NIVELES_OPCIONES, DEPORTES_ENUM } from '@/utils/constants'
import api from '@/services/api'

const authStore = useAuthStore()

const exitoPerfil = ref('')
const errorPerfil = ref('')
const exitoPassword = ref('')
const errorPassword = ref('')
const guardando = ref(false)
const cambiandoPassword = ref(false)

const formPerfil = reactive({
  nombre: '',
  apellidos: '',
  email: '',
  telefono: '',
  ciudad: '',
  nivel: '',
  descripcion: '',
  notificacionesPartidas: true,
  notificacionesChat: true,
  foto: ''
})

const formOriginal = reactive({})

const formPassword = reactive({
  contrasenaActual: '',
  nuevaContrasena: '',
  confirmarContrasena: ''
})

const niveles = NIVELES_OPCIONES

const inicialesUsuario = computed(() => {
  const n = formPerfil.nombre ? formPerfil.nombre.charAt(0).toUpperCase() : ''
  const a = formPerfil.apellidos ? formPerfil.apellidos.trim().split(/\s+/)[0].charAt(0).toUpperCase() : ''
  return (n + a) || 'U'
})

const isFormChanged = computed(() => {
  return JSON.stringify(formPerfil) !== JSON.stringify(formOriginal)
})

onMounted(() => {
  if (authStore.usuario) {
    cargarDatos()
  }
})

function cargarDatos() {
  const u = authStore.usuario
  formPerfil.nombre = u.nombre || ''
  formPerfil.apellidos = u.apellidos || ''
  formPerfil.email = u.email || ''
  formPerfil.telefono = u.telefono || ''
  formPerfil.ciudad = u.ciudad || ''
  formPerfil.nivel = u.nivel || ''
  formPerfil.descripcion = u.descripcion || ''
  formPerfil.notificacionesPartidas = u.notificacionesPartidas ?? true
  formPerfil.notificacionesChat = u.notificacionesChat ?? true
  formPerfil.foto = u.foto || ''

  Object.assign(formOriginal, formPerfil)
}

async function subirFoto(event) {
  errorPerfil.value = ''
  exitoPerfil.value = ''
  
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    errorPerfil.value = 'La imagen es demasiado grande. El tamaño máximo es 2MB.'
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64Str = e.target.result
    formPerfil.foto = base64Str
    try {
      const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
      authStore.login(res.data)
      Object.assign(formOriginal, formPerfil)
      exitoPerfil.value = 'Foto actualizada correctamente.'
    } catch (err) {
      errorPerfil.value = 'No se ha podido actualizar la foto.'
    }
  }
  reader.readAsDataURL(file)
}

async function eliminarFoto() {
  formPerfil.foto = ''
  try {
    const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
    authStore.login(res.data)
    Object.assign(formOriginal, formPerfil)
    exitoPerfil.value = 'Foto eliminada correctamente.'
  } catch (err) {
    errorPerfil.value = 'No se ha podido eliminar la foto.'
  }
}

async function guardarPerfil() {
  exitoPerfil.value = ''
  errorPerfil.value = ''
  guardando.value = true

  try {
    const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
    authStore.login(res.data)
    Object.assign(formOriginal, formPerfil)
    exitoPerfil.value = 'Perfil actualizado correctamente.'
  } catch (err) {
    errorPerfil.value = 'No se ha podido actualizar el perfil.'
  } finally {
    guardando.value = false
  }
}

async function alternarNotificacion(tipo) {
  if (tipo === 'partidas') {
    formPerfil.notificacionesPartidas = !formPerfil.notificacionesPartidas
  } else if (tipo === 'chat') {
    formPerfil.notificacionesChat = !formPerfil.notificacionesChat
  }
  
  try {
    const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
    authStore.login(res.data)
    Object.assign(formOriginal, formPerfil)
  } catch (err) {
    if (tipo === 'partidas') formPerfil.notificacionesPartidas = !formPerfil.notificacionesPartidas
    if (tipo === 'chat') formPerfil.notificacionesChat = !formPerfil.notificacionesChat
  }
}

async function cambiarPassword() {
  exitoPassword.value = ''
  errorPassword.value = ''

  if (!formPassword.contrasenaActual || !formPassword.nuevaContrasena || !formPassword.confirmarContrasena) {
    errorPassword.value = 'Rellena todos los campos de contraseña.'
    return
  }
  if (formPassword.nuevaContrasena !== formPassword.confirmarContrasena) {
    errorPassword.value = 'Las contraseñas nuevas no coinciden.'
    return
  }
  if (formPassword.nuevaContrasena.length < 4) {
    errorPassword.value = 'La nueva contraseña debe tener al menos 4 caracteres.'
    return
  }

  cambiandoPassword.value = true
  try {
    await api.put(`/usuarios/${authStore.usuario.id}/password`, {
      contrasenaActual: formPassword.contrasenaActual,
      nuevaContrasena: formPassword.nuevaContrasena
    })
    exitoPassword.value = 'Contraseña cambiada con éxito.'
    formPassword.contrasenaActual = ''
    formPassword.nuevaContrasena = ''
    formPassword.confirmarContrasena = ''
  } catch (err) {
    if (err.response?.status === 401) {
      errorPassword.value = 'La contraseña actual no es correcta.'
    } else {
      errorPassword.value = 'Ha ocurrido un error al cambiar la contraseña.'
    }
  } finally {
    cambiandoPassword.value = false
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-64px)] bg-gray-50 p-4 md:p-8 font-sans">
    <div class="max-w-5xl mx-auto flex flex-col lg:flex-row gap-6">
      
      <div class="w-full lg:w-1/3 xl:w-1/4">
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center">
          
          <div class="relative mb-4 group cursor-pointer">
            <div class="w-32 h-32 rounded-full bg-blue-600 text-white flex items-center justify-center text-4xl font-bold border-4 border-white shadow-md overflow-hidden relative">
              <img v-if="authStore.usuario?.foto" :src="authStore.usuario.foto" alt="Foto perfil" class="w-full h-full object-cover" />
              <span v-else>{{ inicialesUsuario }}</span>
            </div>
          </div>
          
          <h2 class="text-xl font-bold text-gray-900 mb-1">{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellidos }}</h2>
          <p class="text-sm text-gray-500 mb-6">Deportista</p>
          
          <div class="w-full bg-yellow-50 border border-yellow-100 rounded-xl p-3 flex items-center justify-center gap-2 mb-6">
            <svg class="w-5 h-5 text-yellow-500 fill-current" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
            <div class="text-sm">
              <span class="font-bold text-gray-800">{{ authStore.usuario?.valoracionMedia || '0.0' }}/5</span>
              <span class="text-xs text-gray-500 ml-1">(Basado en 0 valoraciones)</span>
            </div>
          </div>

          <input type="file" ref="fotoInput" class="hidden" accept="image/*" @change="subirFoto" />
          <button @click="$refs.fotoInput.click()" class="w-full bg-white border border-blue-600 text-blue-600 font-semibold py-2 rounded-lg hover:bg-blue-50 transition text-sm">
            Editar Foto
          </button>
          
          <button v-if="authStore.usuario?.foto" @click="eliminarFoto" class="w-full bg-white border border-red-500 text-red-500 font-semibold py-2 rounded-lg hover:bg-red-50 transition text-sm mt-3">
            Eliminar Foto
          </button>
        </div>
      </div>

      <div class="w-full lg:w-2/3 xl:w-3/4 flex flex-col gap-6">
        
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Ajustes de la Cuenta</h3>
            <p class="text-sm text-gray-500">Actualiza tu información personal</p>
          </div>

          <div v-if="exitoPerfil" class="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-lg text-sm">{{ exitoPerfil }}</div>
          <div v-if="errorPerfil" class="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">{{ errorPerfil }}</div>

          <form @submit.prevent="guardarPerfil" class="space-y-5">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Nombre <span class="text-red-500">*</span></label>
                <input v-model="formPerfil.nombre" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" required />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Apellidos <span class="text-red-500">*</span></label>
                <input v-model="formPerfil.apellidos" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" required />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Correo Electrónico <span class="text-red-500">*</span></label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                </div>
                <input v-model="formPerfil.email" type="email" disabled class="w-full bg-gray-100 border border-gray-200 text-gray-500 rounded-lg pl-10 pr-3 py-2 text-sm cursor-not-allowed" />
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Teléfono</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
                  </div>
                  <input v-model="formPerfil.telefono" type="text" placeholder="+34 600 000 000" class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" />
                </div>
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Ciudad</label>
                <select v-model="formPerfil.ciudad" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition appearance-none">
                  <option value="" disabled>Selecciona tu ciudad</option>
                  <option value="Madrid">Madrid</option>
                  <option value="Barcelona">Barcelona</option>
                  <option value="Valencia">Valencia</option>
                  <option value="Elche">Elche</option>
                  <option value="Sevilla">Sevilla</option>
                </select>
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-2">Nivel de juego</label>
              <div class="flex flex-wrap gap-2">
                <button 
                  v-for="n in niveles" 
                  :key="n.value" 
                  type="button"
                  @click="formPerfil.nivel = n.value"
                  :class="[
                    'px-4 py-2 text-sm font-medium rounded-lg border transition',
                    formPerfil.nivel === n.value 
                      ? 'bg-blue-50 border-blue-500 text-blue-700' 
                      : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                  ]"
                >
                  {{ n.label }}
                </button>
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Descripción</label>
              <textarea v-model="formPerfil.descripcion" rows="3" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition resize-none"></textarea>
            </div>

            <div class="flex items-center justify-between pt-4 border-t border-gray-100">
              <p class="text-xs text-gray-400"><span class="text-red-500">*</span> Campos obligatorios</p>
              <div class="flex gap-3">
                <button v-if="isFormChanged" type="button" @click="cargarDatos" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition">
                  Cancelar
                </button>
                <button type="submit" :disabled="guardando || !isFormChanged" class="px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-70 transition">
                  {{ guardando ? 'Guardando...' : 'GUARDAR CAMBIOS' }}
                </button>
              </div>
            </div>
          </form>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Seguridad</h3>
            <p class="text-sm text-gray-500">Gestiona tu contraseña y acceso</p>
          </div>

          <div v-if="exitoPassword" class="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-lg text-sm">{{ exitoPassword }}</div>
          <div v-if="errorPassword" class="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">{{ errorPassword }}</div>

          <form @submit.prevent="cambiarPassword" class="space-y-5">
            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Contraseña actual</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                </div>
                <input v-model="formPassword.contrasenaActual" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" />
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Nueva contraseña</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                  </div>
                  <input v-model="formPassword.nuevaContrasena" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" />
                </div>
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Confirmar contraseña</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                  </div>
                  <input v-model="formPassword.confirmarContrasena" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg pl-10 pr-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition" />
                </div>
              </div>
            </div>

            <div class="flex justify-end pt-4">
              <button type="submit" :disabled="cambiandoPassword" class="px-5 py-2 text-sm font-semibold text-blue-600 bg-white border border-blue-600 rounded-lg hover:bg-blue-50 disabled:opacity-70 transition">
                {{ cambiandoPassword ? 'Cambiando...' : 'Cambiar Contraseña' }}
              </button>
            </div>
          </form>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Notificaciones</h3>
            <p class="text-sm text-gray-500">Configura cómo quieres recibir avisos</p>
          </div>

          <div class="space-y-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="font-bold text-gray-800 text-sm">Nuevas partidas abiertas</p>
                <p class="text-xs text-gray-500">Notificaciones de partidas en tu zona</p>
              </div>
              <button 
                @click="alternarNotificacion('partidas')"
                :class="formPerfil.notificacionesPartidas ? 'bg-blue-600' : 'bg-gray-200'"
                class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                <span :class="formPerfil.notificacionesPartidas ? 'translate-x-5' : 'translate-x-0'" class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"></span>
              </button>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <p class="font-bold text-gray-800 text-sm">Mensajes de chat</p>
                <p class="text-xs text-gray-500">Avisos de nuevos mensajes</p>
              </div>
              <button 
                @click="alternarNotificacion('chat')"
                :class="formPerfil.notificacionesChat ? 'bg-blue-600' : 'bg-gray-200'"
                class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                <span :class="formPerfil.notificacionesChat ? 'translate-x-5' : 'translate-x-0'" class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"></span>
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
