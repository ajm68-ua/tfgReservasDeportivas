<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import ChatWidget from '@/components/ChatWidget.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isLogged()) {
    authStore.refreshUser(api)
  }
})
</script>

<template>
  <AppHeader />
  <RouterView />
  <ChatWidget />
</template>

<style>
@tailwind base;
@tailwind components;
@tailwind utilities;
</style>
