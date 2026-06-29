<script setup>
import { RouterView } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import ChatWidget from '@/components/ChatWidget.vue'
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
