import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import RegisterCentroView from '@/views/auth/RegisterCentroView.vue'

import HomeView from '@/views/home/HomeView.vue'

import CentrosListView from '@/views/centros/CentrosListView.vue'
import CentroDetailView from '@/views/centros/CentroDetailView.vue'

import PistasListView from '@/views/pistas/PistasListView.vue'

import ReservaCreateView from '@/views/reservas/ReservaCreateView.vue'
import MisReservasView from '@/views/reservas/MisReservasView.vue'
import ReservaEditView from '@/views/reservas/ReservaEditView.vue'
import ReservaPagoView from '@/views/reservas/ReservaPagoView.vue'
import PartidasAbiertasView from '@/views/partidas/PartidasAbiertasView.vue'



import PerfilView from '@/views/perfil/PerfilView.vue'

import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminPistasView from '@/views/admin/AdminPistasView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requiresAuth: false },
  },
  {
    path: '/registro',
    name: 'registro',
    component: RegisterView,
    meta: { requiresAuth: false },
  },
  {
    path: '/registro-centro',
    name: 'registro-centro',
    component: RegisterCentroView,
    meta: { requiresAuth: false },
  },

  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/centros',
    name: 'centros',
    component: CentrosListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/centros/:id',
    name: 'centro-detail',
    component: CentroDetailView,
    meta: { requiresAuth: true },
  },
  {
    path: '/pistas',
    name: 'pistas',
    component: PistasListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/nueva',
    name: 'reserva-create',
    component: ReservaCreateView,
    meta: { requiresAuth: true, rol: 'DEPORTISTA' },
  },
  {
    path: '/mis-reservas',
    name: 'mis-reservas',
    component: MisReservasView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/:id/editar',
    name: 'reserva-edit',
    component: ReservaEditView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/:id/pago',
    name: 'reserva-pago',
    component: ReservaPagoView,
    meta: { requiresAuth: true },
  },
  {
    path: '/partidas',
    name: 'partidas',
    component: PartidasAbiertasView,
    meta: { requiresAuth: true, rol: 'DEPORTISTA' },
  },

  {
    path: '/perfil',
    name: 'perfil',
    component: PerfilView,
    meta: { requiresAuth: true },
  },
  {
    path: '/usuario/:id',
    name: 'perfil-publico',
    component: () => import('@/views/perfil/PerfilPublicoView.vue'),
    meta: { requiresAuth: true },
  },

  {
    path: '/admin',
    name: 'admin-dashboard',
    component: AdminDashboardView,
    meta: { requiresAuth: true, rol: 'ADMINISTRADOR_CENTRO' },
  },
  {
    path: '/admin/pistas',
    name: 'admin-pistas',
    component: AdminPistasView,
    meta: { requiresAuth: true, rol: 'ADMINISTRADOR_CENTRO' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLogged()) {
    next({ name: 'login' })
  } else if (to.meta.rol && authStore.usuario?.rol !== to.meta.rol) {
    next({ name: 'home' })
  } else if (!to.meta.requiresAuth && authStore.isLogged() && (to.name === 'login' || to.name === 'registro')) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
