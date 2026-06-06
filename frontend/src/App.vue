<script setup>
import { RouterView } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
</script>

<template>
  <AppHeader />
  <RouterView />
</template>

<style>
@tailwind base;
@tailwind components;
@tailwind utilities;
</style>
