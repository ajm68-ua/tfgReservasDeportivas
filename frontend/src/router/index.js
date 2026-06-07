import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'

import HomeView from '@/views/home/HomeView.vue'

import CentrosListView from '@/views/centros/CentrosListView.vue'
import CentroDetailView from '@/views/centros/CentroDetailView.vue'

import PistasListView from '@/views/pistas/PistasListView.vue'
import PistaDetailView from '@/views/pistas/PistaDetailView.vue'

import ReservaCreateView from '@/views/reservas/ReservaCreateView.vue'
import ReservasListView from '@/views/reservas/ReservasListView.vue'
import ReservaDetailView from '@/views/reservas/ReservaDetailView.vue'

import PartidasAbiertasView from '@/views/partidas/PartidasAbiertasView.vue'

import ChatReservaView from '@/views/chat/ChatReservaView.vue'

import ResenasView from '@/views/resenas/ResenasView.vue'

import NotificacionesView from '@/views/notificaciones/NotificacionesView.vue'

import PerfilView from '@/views/perfil/PerfilView.vue'

import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminPistasView from '@/views/admin/AdminPistasView.vue'
import AdminReservasView from '@/views/admin/AdminReservasView.vue'

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
    path: '/pistas/:id',
    name: 'pista-detail',
    component: PistaDetailView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/nueva',
    name: 'reserva-create',
    component: ReservaCreateView,
    meta: { requiresAuth: true, rol: 'DEPORTISTA' },
  },
  {
    path: '/reservas',
    name: 'reservas',
    component: ReservasListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/:id',
    name: 'reserva-detail',
    component: ReservaDetailView,
    meta: { requiresAuth: true },
  },
  {
    path: '/partidas',
    name: 'partidas',
    component: PartidasAbiertasView,
    meta: { requiresAuth: true, rol: 'DEPORTISTA' },
  },
  {
    path: '/reservas/:id/chat',
    name: 'chat-reserva',
    component: ChatReservaView,
    meta: { requiresAuth: true },
  },
  {
    path: '/reservas/:id/resenas',
    name: 'resenas',
    component: ResenasView,
    meta: { requiresAuth: true },
  },
  {
    path: '/notificaciones',
    name: 'notificaciones',
    component: NotificacionesView,
    meta: { requiresAuth: true },
  },
  {
    path: '/perfil',
    name: 'perfil',
    component: PerfilView,
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
  {
    path: '/admin/reservas',
    name: 'admin-reservas',
    component: AdminReservasView,
    meta: { requiresAuth: true, rol: 'ADMINISTRADOR_CENTRO' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
