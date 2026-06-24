<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="bg-gray-900 text-white py-12 px-6 lg:px-8 shadow-inner">
    <div class="max-w-7xl mx-auto">
      <slot name="title">
        <h1 v-if="title" class="text-4xl md:text-5xl font-extrabold tracking-tight mb-3">{{ title }}</h1>
      </slot>
      
      <slot name="subtitle">
        <p v-if="subtitle" class="text-gray-400 text-lg">{{ subtitle }}</p>
      </slot>
    </div>
  </div>
</template>
