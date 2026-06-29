<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { obtenerIniciales } from '@/utils/formatters'

const router = useRouter()
const authStore = useAuthStore()

const cargando = ref(true)
const guardandoPerfil = ref(false)
const guardandoCentro = ref(false)
const cambiandoPassword = ref(false)
const asignandoAdmin = ref(false)
const eliminandoCentro = ref(false)

const formPerfil = reactive({})
const formOriginalPerfil = reactive({})

const formPassword = reactive({
  contrasenaActual: '',
  nuevaContrasena: '',
  confirmarContrasena: ''
})

const formCentro = reactive({})
const formOriginalCentro = reactive({})

const administradores = ref([])
const nuevoAdminEmail = ref('')

const isPerfilChanged = computed(() => JSON.stringify(formPerfil) !== JSON.stringify(formOriginalPerfil))
const isCentroChanged = computed(() => JSON.stringify(formCentro) !== JSON.stringify(formOriginalCentro))
const soyUnicoAdmin = computed(() => administradores.value.length === 1)

async function cambiarPassword() {

  if (!formPassword.contrasenaActual || !formPassword.nuevaContrasena || !formPassword.confirmarContrasena) {
    toast.error('Rellena todos los campos de contraseña.')
    return
  }
  if (formPassword.nuevaContrasena !== formPassword.confirmarContrasena) {
    toast.error('Las contraseñas nuevas no coinciden.')
    return
  }
  if (formPassword.nuevaContrasena.length < 4) {
    toast.error('La nueva contraseña debe tener al menos 4 caracteres.')
    return
  }

  cambiandoPassword.value = true
  try {
    await api.put(`/usuarios/${authStore.usuario.id}/password`, {
      contrasenaActual: formPassword.contrasenaActual,
      nuevaContrasena: formPassword.nuevaContrasena
    })
    toast.success('Contraseña cambiada con éxito.')
    formPassword.contrasenaActual = ''
    formPassword.nuevaContrasena = ''
    formPassword.confirmarContrasena = ''
  } catch (err) {
    if (err.response?.status === 401) {
      toast.error('La contraseña actual no es correcta.')
    } else {
      toast.error('Ha ocurrido un error al cambiar la contraseña.')
    }
  } finally {
    cambiandoPassword.value = false
  }
}

onMounted(async () => {
  if (!authStore.usuario || authStore.usuario.rol !== 'ADMINISTRADOR_CENTRO' || !authStore.usuario.centroId) {
    router.push('/')
    return
  }
  
  await cargarTodo()
})

async function cargarTodo() {
  cargando.value = true
  try {
    const u = authStore.usuario
    Object.assign(formPerfil, {
      nombre: u.nombre || '',
      apellidos: u.apellidos || '',
      email: u.email || '',
      telefono: u.telefono || '',
      ciudad: u.ciudad || ''
    })
    Object.assign(formOriginalPerfil, formPerfil)

    const resCentro = await api.get(`/centros/${u.centroId}`)
    Object.assign(formCentro, resCentro.data)
    Object.assign(formOriginalCentro, formCentro)

    await cargarAdmins()

  } catch (err) {
    console.error(err)
  } finally {
    cargando.value = false
  }
}

async function cargarAdmins() {
  try {
    const resAdmins = await api.get(`/usuarios/admin-centro/${authStore.usuario.centroId}/administradores`)
    administradores.value = resAdmins.data
  } catch (err) {
    console.error('Error cargando administradores', err)
  }
}

async function guardarPerfil() {
  guardandoPerfil.value = true
  try {
    const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
    authStore.login(res.data)
    Object.assign(formOriginalPerfil, formPerfil)
    toast.success('Datos personales actualizados.')
  } catch (err) {
    toast.error('Error al actualizar el perfil.')
  } finally {
    guardandoPerfil.value = false
  }
}

function cancelarPerfil() {
  Object.assign(formPerfil, formOriginalPerfil)
}

async function subirFotoPerfil(event) {
  
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    toast.error('La imagen es demasiado grande. El tamaño máximo es 2MB.')
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    formPerfil.foto = e.target.result
    try {
      const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
      authStore.login(res.data)
      Object.assign(formOriginalPerfil, formPerfil)
      toast.success('Foto de perfil actualizada correctamente.')
    } catch (err) {
      toast.error('No se ha podido actualizar la foto.')
    }
  }
  reader.readAsDataURL(file)
}

async function eliminarFotoPerfil() {
  formPerfil.foto = ''
  try {
    const res = await api.put(`/usuarios/${authStore.usuario.id}`, formPerfil)
    authStore.login(res.data)
    Object.assign(formOriginalPerfil, formPerfil)
    toast.success('Foto de perfil eliminada correctamente.')
  } catch (err) {
    toast.error('No se ha podido eliminar la foto.')
  }
}

async function guardarCentro() {
  guardandoCentro.value = true
  try {
    const res = await api.put(`/centros/${authStore.usuario.centroId}`, formCentro)
    Object.assign(formCentro, res.data)
    Object.assign(formOriginalCentro, formCentro)
    toast.success('Centro deportivo actualizado.')
  } catch (err) {
    toast.error('Error al actualizar el centro.')
  } finally {
    guardandoCentro.value = false
  }
}

function cancelarCentro() {
  Object.assign(formCentro, formOriginalCentro)
}

async function subirFotoCentro(event) {
  
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    toast.error('La imagen es demasiado grande. El tamaño máximo es 2MB.')
    return
  }

  const reader = new FileReader()
  reader.onload = async (e) => {
    formCentro.foto = e.target.result
    try {
      const res = await api.put(`/centros/${authStore.usuario.centroId}`, formCentro)
      Object.assign(formCentro, res.data)
      Object.assign(formOriginalCentro, formCentro)
      toast.success('Foto del centro actualizada correctamente.')
    } catch (err) {
      toast.error('No se ha podido actualizar la foto del centro.')
    }
  }
  reader.readAsDataURL(file)
}

async function eliminarFotoCentro() {
  formCentro.foto = ''
  try {
    const res = await api.put(`/centros/${authStore.usuario.centroId}`, formCentro)
    Object.assign(formCentro, res.data)
    Object.assign(formOriginalCentro, formCentro)
    toast.success('Foto del centro eliminada correctamente.')
  } catch (err) {
    toast.error('No se ha podido eliminar la foto del centro.')
  }
}

async function eliminarCentro() {
  if (!soyUnicoAdmin.value) {
    toast.error('Debes ser el único administrador para eliminar el centro. Revoca los permisos de los demás primero.')
    return
  }

  if (!confirm('¿Estás SEGURO de que deseas eliminar este centro deportivo? Esta acción borrará todas sus pistas asociadas y no se puede deshacer.')) return

  eliminandoCentro.value = true
  try {
    await api.delete(`/centros/${authStore.usuario.centroId}`)
    authStore.logout()
    router.push('/')
  } catch (err) {
    if (err.response?.status === 409) {
      toast.error(err.response.data)
    } else {
      toast.error('Ha ocurrido un error al eliminar el centro.')
    }
  } finally {
    eliminandoCentro.value = false
  }
}

async function asignarAdmin() {
  if (!nuevoAdminEmail.value) return

  asignandoAdmin.value = true
  try {
    await api.post(`/usuarios/admin-centro/${authStore.usuario.centroId}/asignar`, { email: nuevoAdminEmail.value })
    nuevoAdminEmail.value = ''
    toast.success('Administrador asignado correctamente.')
    await cargarAdmins()
  } catch (err) {
    if (err.response?.status === 404) {
      toast.error('No se ha encontrado un usuario con ese email.')
    } else if (err.response?.status === 409 || err.response?.status === 400) {
      toast.error(err.response.data)
    } else {
      toast.error('Error al asignar administrador.')
    }
  } finally {
    asignandoAdmin.value = false
  }
}

async function revocarAdmin(emailAdmin) {
  if (emailAdmin === authStore.usuario.email) return

  if (!confirm(`¿Estás seguro de que quieres revocar los permisos de administración a ${emailAdmin}?`)) return
  
  try {
    await api.delete(`/usuarios/admin-centro/${authStore.usuario.centroId}/revocar/${emailAdmin}`)
    toast.success('Permisos revocados correctamente.')
    await cargarAdmins()
  } catch (err) {
    if (err.response?.status === 409) {
      toast.error(err.response.data)
    } else {
      toast.error('Error al revocar permisos.')
    }
  }
}
</script>

<template>
  <div v-if="cargando" class="min-h-[calc(100vh-64px)] bg-gray-50 flex items-center justify-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
  </div>

  <div v-else class="min-h-[calc(100vh-64px)] bg-gray-50 p-4 md:p-8 font-sans">
    <div class="max-w-6xl mx-auto flex flex-col lg:flex-row gap-6">
      
      <div class="w-full lg:w-1/3 xl:w-1/4 flex flex-col gap-6">
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center text-center">
          <div class="w-24 h-24 rounded-full bg-blue-600 text-white flex items-center justify-center text-3xl font-bold border-4 border-white shadow-md mb-4 overflow-hidden">
            <img v-if="authStore.usuario?.foto" :src="authStore.usuario.foto" alt="Foto perfil" class="w-full h-full object-cover" />
            <span v-else>{{ obtenerIniciales(formPerfil.nombre, formPerfil.apellidos) }}</span>
          </div>
          <h2 class="text-xl font-bold text-gray-900 mb-1">{{ formPerfil.nombre }} {{ formPerfil.apellidos }}</h2>
          <p class="text-sm font-semibold text-blue-600 mb-4">Administrador</p>
          <p class="text-xs text-gray-500 break-all mb-4">{{ formPerfil.email }}</p>

          <input type="file" ref="fotoPerfilInput" class="hidden" accept="image/*" @change="subirFotoPerfil" />
          <button @click="$refs.fotoPerfilInput.click()" class="w-full bg-white border border-blue-600 text-blue-600 font-semibold py-2 rounded-lg hover:bg-blue-50 transition text-sm">
            Editar Foto Perfil
          </button>
          
          <button v-if="authStore.usuario?.foto" @click="eliminarFotoPerfil" class="w-full bg-white border border-red-500 text-red-500 font-semibold py-2 rounded-lg hover:bg-red-50 transition text-sm mt-3">
            Eliminar Foto Perfil
          </button>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center text-center">
          <div class="w-24 h-24 rounded-2xl bg-gray-100 flex items-center justify-center mb-4 overflow-hidden shadow-inner">
            <img v-if="formCentro.foto" :src="formCentro.foto" alt="Centro" class="w-full h-full object-cover"/>
            <svg v-else class="w-10 h-10 text-gray-300" fill="currentColor" viewBox="0 0 20 20"><path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"></path></svg>
          </div>
          <h2 class="text-xl font-bold text-gray-900 mb-1">{{ formCentro.nombre }}</h2>
          <p class="text-sm text-gray-500 mb-4"><i class="fas fa-map-marker-alt mr-1"></i> {{ formCentro.ciudad }}</p>
          
          <div class="w-full flex justify-between text-sm px-2 text-gray-600 bg-gray-50 p-2 rounded mb-4">
            <span>Apertura: <b>{{ formCentro.horarioApertura }}</b></span>
            <span>Cierre: <b>{{ formCentro.horarioCierre }}</b></span>
          </div>

          <input type="file" ref="fotoCentroInput" class="hidden" accept="image/*" @change="subirFotoCentro" />
          <button @click="$refs.fotoCentroInput.click()" class="w-full bg-white border border-blue-600 text-blue-600 font-semibold py-2 rounded-lg hover:bg-blue-50 transition text-sm">
            Editar Foto Centro
          </button>
          
          <button v-if="formCentro.foto" @click="eliminarFotoCentro" class="w-full bg-white border border-red-500 text-red-500 font-semibold py-2 rounded-lg hover:bg-red-50 transition text-sm mt-3">
            Eliminar Foto Centro
          </button>
        </div>
      </div>

      <div class="w-full lg:w-2/3 xl:w-3/4 flex flex-col gap-6">
        
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Mis Datos Personales</h3>
            <p class="text-sm text-gray-500">Actualiza tu información personal</p>
          </div>
          <form @submit.prevent="guardarPerfil" class="space-y-5">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Nombre</label>
                <input v-model="formPerfil.nombre" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Apellidos</label>
                <input v-model="formPerfil.apellidos" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Correo Electrónico (No editable)</label>
              <input v-model="formPerfil.email" type="email" disabled class="w-full bg-gray-100 border border-gray-200 text-gray-500 rounded-lg px-3 py-2 text-sm cursor-not-allowed" />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Teléfono</label>
                <input v-model="formPerfil.telefono" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Ciudad</label>
                <input v-model="formPerfil.ciudad" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              </div>
            </div>

            <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
              <button v-if="isPerfilChanged" type="button" @click="cancelarPerfil" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition">Cancelar</button>
              <button type="submit" :disabled="guardandoPerfil || !isPerfilChanged" class="px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-70 transition">
                {{ guardandoPerfil ? 'Guardando...' : 'Guardar Cambios' }}
              </button>
            </div>
          </form>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Cambiar Contraseña</h3>
          </div>
          <form @submit.prevent="cambiarPassword" class="space-y-5">
            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Contraseña Actual</label>
              <input v-model="formPassword.contrasenaActual" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Nueva Contraseña</label>
                <input v-model="formPassword.nuevaContrasena" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Confirmar Nueva Contraseña</label>
                <input v-model="formPassword.confirmarContrasena" type="password" placeholder="••••••••" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" />
              </div>
            </div>

            <div class="flex justify-end pt-4 border-t border-gray-100">
              <button type="submit" :disabled="cambiandoPassword" class="px-5 py-2 text-sm font-semibold text-blue-600 bg-white border border-blue-600 rounded-lg hover:bg-blue-50 disabled:opacity-70 transition">
                {{ cambiandoPassword ? 'Cambiando...' : 'Actualizar Contraseña' }}
              </button>
            </div>
          </form>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6 flex justify-between items-start">
            <div>
              <h3 class="text-xl font-bold text-gray-900">Configuración del Centro</h3>
              <p class="text-sm text-gray-500">Gestiona los detalles públicos de tus instalaciones</p>
            </div>
          </div>
          <form @submit.prevent="guardarCentro" class="space-y-5">
            <div>
              <label class="block text-xs font-semibold text-gray-600 mb-1">Nombre del Centro</label>
              <input v-model="formCentro.nombre" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Dirección</label>
                <input v-model="formCentro.direccion" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Ciudad</label>
                <input v-model="formCentro.ciudad" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Teléfono</label>
                <input v-model="formCentro.telefono" type="text" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Horario Apertura</label>
                <input v-model="formCentro.horarioApertura" type="time" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-600 mb-1">Horario Cierre</label>
                <input v-model="formCentro.horarioCierre" type="time" class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:border-blue-500 transition" required />
              </div>
            </div>

            <div class="flex items-center justify-between pt-4 border-t border-gray-100">
              <button type="button" @click="eliminarCentro" :disabled="eliminandoCentro || !soyUnicoAdmin" 
                class="px-4 py-2 text-sm font-semibold text-red-600 bg-white border border-red-200 hover:bg-red-50 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed">
                {{ eliminandoCentro ? 'Eliminando...' : 'Eliminar Centro' }}
              </button>
              
              <div class="flex gap-3">
                <button v-if="isCentroChanged" type="button" @click="cancelarCentro" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition">Cancelar</button>
                <button type="submit" :disabled="guardandoCentro || !isCentroChanged" class="px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-70 transition">
                  {{ guardandoCentro ? 'Guardando...' : 'Guardar Cambios' }}
                </button>
              </div>
            </div>
            
            <p v-if="!soyUnicoAdmin" class="text-xs text-red-500 mt-2">
              * Para eliminar el centro, debes revocar los permisos de los demás administradores primero.
            </p>
          </form>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6">
            <h3 class="text-xl font-bold text-gray-900">Equipo de Administradores</h3>
            <p class="text-sm text-gray-500">Asigna o revoca permisos a otros usuarios</p>
          </div>
          <form @submit.prevent="asignarAdmin" class="flex gap-3 mb-6 bg-gray-50 p-4 rounded-xl border border-gray-100 items-end">
            <div class="flex-grow">
              <label class="block text-xs font-semibold text-gray-600 mb-1">Email del usuario a añadir</label>
              <input v-model="nuevoAdminEmail" type="email" placeholder="usuario@ejemplo.com" class="w-full bg-white border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500 transition" required />
            </div>
            <button type="submit" :disabled="asignandoAdmin" class="px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-70 transition whitespace-nowrap h-[38px]">
              {{ asignandoAdmin ? 'Asignando...' : 'Añadir Admin' }}
            </button>
          </form>

          <div class="border border-gray-100 rounded-xl overflow-hidden">
            <table class="w-full text-sm text-left text-gray-500">
              <thead class="bg-gray-50 text-xs text-gray-700 uppercase border-b border-gray-100">
                <tr>
                  <th scope="col" class="px-6 py-3">Nombre</th>
                  <th scope="col" class="px-6 py-3">Email</th>
                  <th scope="col" class="px-6 py-3 text-right">Acción</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="admin in administradores" :key="admin.id" class="bg-white border-b last:border-b-0 hover:bg-gray-50 transition">
                  <td class="px-6 py-4 font-medium text-gray-900 whitespace-nowrap flex items-center gap-3">
                    <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold">
                      {{ obtenerIniciales(admin.nombre, admin.apellidos) }}
                    </div>
                    {{ admin.nombre }} {{ admin.apellidos }}
                  </td>
                  <td class="px-6 py-4">{{ admin.email }}</td>
                  <td class="px-6 py-4 text-right">
                    <button 
                      v-if="admin.email !== authStore.usuario.email"
                      @click="revocarAdmin(admin.email)" 
                      class="text-red-500 hover:text-red-700 font-semibold px-2 py-1 hover:bg-red-50 rounded transition"
                    >
                      Revocar
                    </button>
                    <span v-else class="text-xs text-gray-400 font-semibold uppercase bg-gray-100 px-2 py-1 rounded">Tú</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>
