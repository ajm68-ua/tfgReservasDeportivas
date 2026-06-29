<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'
import PageHeader from '@/components/ui/PageHeader.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { NIVELES_OPCIONES, MAPA_DEPORTES } from '@/utils/constants'

import { useAuthStore } from '@/stores/auth'

const obtenerIniciales = (name, surname) => {
  const n = name ? name.charAt(0).toUpperCase() : 'U'
  const a = surname ? surname.trim().split(/\s+/)[0].charAt(0).toUpperCase() : ''
  return (n + a) || 'U'
}

const route = useRoute()
const userId = route.params.id

const authStore = useAuthStore()

const cargando = ref(true)
const errorMensaje = ref('')
const usuario = ref(null)
const resenas = ref([])

const mostrarModalResena = ref(false)
const enviandoResena = ref(false)
const errorResena = ref('')
const modoEdicion = ref(false)
const resenaEditandoId = ref(null)
const formResena = ref({
  puntuacion: 5,
  comentario: ''
})

const cargarDatos = async () => {
  try {
    const [userRes, resenasRes] = await Promise.all([
      api.get(`/usuarios/${userId}`),
      api.get(`/resenas/usuario/${userId}`)
    ])
    usuario.value = userRes.data
    resenas.value = resenasRes.data
  } catch (error) {
    console.error('Error al cargar perfil público:', error)
    errorMensaje.value = 'No se pudo cargar el perfil del usuario. Es posible que no exista.'
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargarDatos()
})

const abrirModalNuevo = () => {
  modoEdicion.value = false
  resenaEditandoId.value = null
  formResena.value = { puntuacion: 5, comentario: '' }
  mostrarModalResena.value = true
}

const abrirModalEditar = (resena) => {
  modoEdicion.value = true
  resenaEditandoId.value = resena.id
  formResena.value = {
    puntuacion: resena.puntuacion || 0,
    comentario: resena.comentario
  }
  mostrarModalResena.value = true
}

const enviarResena = async () => {
  if (formResena.value.comentario.trim() === '') {
    errorResena.value = 'El comentario no puede estar vacío.'
    return
  }
  
  enviandoResena.value = true
  errorResena.value = ''
  
  try {
    if (modoEdicion.value) {
      await api.put(`/resenas/usuario/${resenaEditandoId.value}`, {
        evaluadorId: authStore.usuario.id,
        evaluadoId: userId,
        puntuacion: formResena.value.puntuacion,
        comentario: formResena.value.comentario
      })
    } else {
      await api.post('/resenas/usuario', {
        evaluadorId: authStore.usuario.id,
        evaluadoId: userId,
        puntuacion: formResena.value.puntuacion,
        comentario: formResena.value.comentario
      })
    }
    
    mostrarModalResena.value = false
    
    await cargarDatos()
  } catch (error) {
    console.error('Error al enviar reseña:', error)
    errorResena.value = 'Hubo un problema al enviar tu reseña. Inténtalo de nuevo.'
  } finally {
    enviandoResena.value = false
  }
}

const eliminarResena = async (id) => {
  if (!confirm('¿Estás seguro de que quieres eliminar esta reseña?')) return
  try {
    await api.delete(`/resenas/usuario/${id}`)
    await cargarDatos()
  } catch (error) {
    console.error('Error al eliminar reseña:', error)
    alert('No se pudo eliminar la reseña.')
  }
}

const getPuntuacion = (resena) => {
  const p = Number(resena?.puntuacion) || 0
  return p > 5 ? 5 : (p < 0 ? 0 : p)
}

const getEstrellasVacias = (resena) => {
  return 5 - getPuntuacion(resena)
}

const inicialesUsuario = computed(() => {
  if (!usuario.value) return ''
  const n = usuario.value.nombre ? usuario.value.nombre.charAt(0).toUpperCase() : ''
  const a = usuario.value.apellidos ? usuario.value.apellidos.trim().split(/\s+/)[0].charAt(0).toUpperCase() : ''
  return (n + a) || 'U'
})

const getLabelNivel = (nivelValue) => {
  if (!nivelValue) return 'No especificado'
  const found = NIVELES_OPCIONES.find(n => n.value === nivelValue)
  return found ? found.label : nivelValue
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: 'long', year: 'numeric' }).format(date)
}
</script>

<template>
  <div class="min-h-[calc(100vh-64px)] bg-gray-50 p-4 md:p-8 font-sans">
    
    <LoadingSpinner v-if="cargando" text="Cargando perfil..." full-screen />

    <EmptyState 
      v-else-if="errorMensaje"
      icon="fas fa-user-slash"
      title="Usuario no encontrado"
      :description="errorMensaje"
    />

    <div v-else-if="usuario" class="max-w-5xl mx-auto flex flex-col lg:flex-row gap-6">
      
      <div class="w-full lg:w-1/3 xl:w-1/4 flex flex-col gap-6">
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col items-center text-center">
          
          <div class="relative mb-4 group">
            <div class="w-32 h-32 rounded-full bg-blue-600 text-white flex items-center justify-center text-4xl font-bold border-4 border-white shadow-md overflow-hidden relative">
              <img v-if="usuario.foto" :src="usuario.foto" alt="Foto perfil" class="w-full h-full object-cover" />
              <span v-else>{{ inicialesUsuario }}</span>
            </div>
          </div>
          
          <h2 class="text-xl font-bold text-gray-900 mb-1">{{ usuario.nombre }} {{ usuario.apellidos }}</h2>
          <p class="text-sm text-gray-500 mb-6 flex items-center gap-1 justify-center">
            <i class="fas fa-map-marker-alt"></i> {{ usuario.ciudad || 'Ciudad no especificada' }}
          </p>
          
          <div class="w-full bg-yellow-50 border border-yellow-100 rounded-xl p-3 flex items-center justify-center gap-2 mb-2">
            <svg class="w-5 h-5 text-yellow-500 fill-current" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
            <div class="text-sm">
              <span class="font-bold text-gray-800">{{ usuario.valoracionMedia ? usuario.valoracionMedia.toFixed(1) : '0.0' }}/5</span>
              <span class="text-xs text-gray-500 ml-1">({{ resenas.length }} reseñas)</span>
            </div>
          </div>
          
          <div class="text-xs text-gray-400 w-full text-center mt-2 border-t border-gray-100 pt-4">
            Miembro desde {{ formatDate(usuario.fechaRegistro) }}
          </div>
        </div>
      </div>

      <div class="w-full lg:w-2/3 xl:w-3/4 flex flex-col gap-6">
        
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6 pb-4 border-b border-gray-100">
            <h3 class="text-xl font-bold text-gray-900">Información del Jugador</h3>
          </div>

          <div class="space-y-6">
            <div>
              <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Nivel de Juego</label>
              <div class="inline-block bg-blue-50 border border-blue-200 text-blue-700 px-4 py-2 rounded-lg font-semibold text-sm">
                {{ getLabelNivel(usuario.nivel) }}
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Descripción</label>
              <div class="bg-gray-50 border border-gray-100 rounded-xl p-4 text-gray-700 min-h-[5rem]">
                <p v-if="usuario.descripcion" class="italic whitespace-pre-wrap">"{{ usuario.descripcion }}"</p>
                <p v-else class="italic text-gray-400">Este usuario no ha escrito ninguna descripción.</p>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:p-8">
          <div class="mb-6 pb-4 border-b border-gray-100 flex justify-between items-center">
            <div>
              <h3 class="text-xl font-bold text-gray-900">Reseñas y Opiniones</h3>
              <span class="bg-gray-100 text-gray-600 text-xs font-bold px-3 py-1 rounded-full mt-2 inline-block">{{ resenas.length }} reseñas</span>
            </div>
            
            <button 
              v-if="authStore.usuario && authStore.usuario.id != userId && !resenas.some(r => r.evaluadorId === authStore.usuario.id)"
              @click="abrirModalNuevo"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition hover:shadow-md"
            >
              <i class="fas fa-pen mr-2"></i>Dejar una reseña
            </button>
          </div>

          <div v-if="resenas.length > 0" class="flex flex-col gap-4">
            <div 
              v-for="resena in resenas" 
              :key="resena.id" 
              class="bg-gray-50 rounded-xl p-5 border border-gray-100 relative"
            >
              <div class="absolute top-4 right-4 flex items-center gap-2">
                <span 
                  v-if="resena.reservaId" 
                  class="bg-green-100 text-green-700 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide flex items-center gap-1"
                >
                  <i class="fas fa-check-circle"></i> Verificada
                </span>
                <span 
                  v-else 
                  class="bg-gray-200 text-gray-600 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide"
                >
                  No verificada
                </span>
              </div>
              
              <div class="flex items-center gap-3 mb-2">
                <div class="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm flex-shrink-0">
                  {{ obtenerIniciales(resena.nombreEvaluador, resena.apellidosEvaluador) }}
                </div>
                <div>
                  <p class="font-bold text-gray-900 text-sm">{{ resena.nombreEvaluador }} {{ resena.apellidosEvaluador || '' }}</p>
                  <p class="text-xs text-gray-500">{{ formatDate(resena.fechaCreacion) }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2 mb-3 pl-13">
                <svg class="w-5 h-5 text-yellow-500 fill-current" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
                <span class="text-gray-700 font-bold text-sm">{{ getPuntuacion(resena) }}/5</span>
              </div>
              <p class="text-gray-700 text-sm pl-0 sm:pl-13 mt-2 sm:mt-0">{{ resena.comentario }}</p>

              <div v-if="authStore.usuario && authStore.usuario.id === resena.evaluadorId" class="flex gap-3 justify-end mt-4 pt-3 border-t border-gray-100">
                <button @click="abrirModalEditar(resena)" class="text-blue-600 hover:text-blue-800 text-sm font-semibold transition flex items-center gap-1">
                  <i class="fas fa-edit"></i> Editar
                </button>
                <button @click="eliminarResena(resena.id)" class="text-red-600 hover:text-red-800 text-sm font-semibold transition flex items-center gap-1">
                  <i class="fas fa-trash-alt"></i> Eliminar
                </button>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-10">
            <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-400 text-2xl border border-gray-100">
              <i class="fas fa-comment-slash"></i>
            </div>
            <h3 class="text-lg font-semibold text-gray-700">Sin reseñas</h3>
            <p class="text-sm text-gray-500 mt-1 max-w-sm mx-auto">Este usuario aún no ha recibido reseñas de otros jugadores.</p>
          </div>
        </div>

      </div>
    </div>

    <div v-if="mostrarModalResena" class="fixed inset-0 bg-gray-900 bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
          <h3 class="text-xl font-bold text-gray-900">Dejar una reseña</h3>
          <button @click="mostrarModalResena = false" class="text-gray-400 hover:text-gray-600 transition">
            <i class="fas fa-times text-xl"></i>
          </button>
        </div>
        
        <div class="p-6 flex flex-col gap-6">
          <div v-if="errorResena" class="bg-red-50 text-red-600 p-3 rounded-lg text-sm font-medium">
            {{ errorResena }}
          </div>
          
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">Puntuación: <span class="text-blue-600 text-lg ml-1">{{ formResena.puntuacion }} / 5</span></label>
            <div class="flex items-center gap-4">
              <span class="text-gray-500 font-bold">0</span>
              <input 
                v-model.number="formResena.puntuacion" 
                type="range" 
                min="0" 
                max="5" 
                step="1"
                class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
              <span class="text-gray-500 font-bold">5</span>
            </div>
            <div class="flex text-yellow-400 text-xl gap-1 mt-3 justify-center">
              <i 
                v-for="i in 5" :key="'modal-star-'+i"
                :class="i <= formResena.puntuacion ? 'fas fa-star' : 'far fa-star text-gray-200'"
              ></i>
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">Comentario</label>
            <textarea 
              v-model="formResena.comentario" 
              rows="4" 
              class="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-sm text-gray-800 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition resize-none"
              placeholder="¿Qué te ha parecido jugar con este usuario?"
            ></textarea>
          </div>
        </div>
        
        <div class="p-6 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
          <button 
            @click="mostrarModalResena = false" 
            class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-100 transition"
          >
            Cancelar
          </button>
          <button 
            @click="enviarResena" 
            :disabled="enviandoResena"
            class="px-5 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
          >
            {{ enviandoResena ? 'Enviando...' : 'Publicar Reseña' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
