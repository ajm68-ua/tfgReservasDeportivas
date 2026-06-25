<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import logoLfg from '@/assets/images/Logo LFGHub.png'

const authStore = useAuthStore()
const router = useRouter()

const menuAbierto = ref(false)

function cerrarSesion() {
  menuAbierto.value = false
  authStore.logout()
  router.push('/login')
}

const userInitials = computed(() => {
  if (!authStore.usuario) return ''
  const n = authStore.usuario.nombre ? authStore.usuario.nombre.charAt(0).toUpperCase() : ''
  const a = authStore.usuario.apellidos ? authStore.usuario.apellidos.charAt(0).toUpperCase() : ''
  return n + a
})
</script>

<template>
  <header class="flex items-center justify-between px-8 py-4 bg-white border-b border-gray-100 sticky top-0 z-50">
    <div class="w-1/4 flex items-center">
      <RouterLink to="/">
        <img :src="logoLfg" alt="LFG Hub Logo" class="h-12 object-contain cursor-pointer" />
      </RouterLink>
    </div>

    <nav class="hidden md:flex flex-1 justify-center gap-8 text-sm font-semibold text-gray-500">
      <RouterLink to="/pistas" class="hover:text-gray-900 transition-colors">Buscar Pistas</RouterLink>
      <RouterLink to="/partidas" class="hover:text-gray-900 transition-colors">Partidas Abiertas</RouterLink>
      <RouterLink to="/centros" class="hover:text-gray-900 transition-colors">Centros Deportivos</RouterLink>
      <RouterLink v-if="authStore.isLogged()" to="/mis-reservas" class="hover:text-gray-900 transition-colors">Mis Reservas</RouterLink>
    </nav>

    <div class="w-1/4 flex items-center justify-end gap-3">
      <template v-if="!authStore.isLogged()">
        <RouterLink to="/login">
          <button class="px-5 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-md hover:bg-gray-50 transition-colors">
            Iniciar Sesión
          </button>
        </RouterLink>
        <RouterLink to="/registro">
          <button class="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors">
            Registrarse
          </button>
        </RouterLink>
      </template>

      <template v-else>
        <div v-if="menuAbierto" class="fixed inset-0 z-40" @click="menuAbierto = false"></div>

        <div class="relative z-50 flex items-center gap-3">
          <span class="text-sm font-semibold text-gray-700 hidden sm:block">{{ authStore.usuario.nombre }}</span>
          
          <button @click="menuAbierto = !menuAbierto" class="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold overflow-hidden">
            <img v-if="authStore.usuario.foto" :src="authStore.usuario.foto" alt="Perfil" class="w-full h-full object-cover" />
            <span v-else>{{ userInitials }}</span>
          </button>

          <div v-if="menuAbierto" class="absolute right-0 top-12 mt-2 w-48 bg-white rounded-xl shadow-lg py-2 border border-gray-100 animate-fade-in-down">
            <RouterLink 
              :to="authStore.isAdmin() ? '/admin' : '/perfil'" 
              @click="menuAbierto = false"
              class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition"
            >
              Mi Perfil
            </RouterLink>
            <RouterLink 
              v-if="authStore.isAdmin()"
              to="/admin/pistas" 
              @click="menuAbierto = false"
              class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition"
            >
              Gestión de Pistas
            </RouterLink>
            <hr class="my-1 border-gray-100">
            <button 
              @click="cerrarSesion"
              class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition font-medium"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </template>
    </div>
  </header>
</template>
