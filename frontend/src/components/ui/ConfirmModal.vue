<script setup>
defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: 'Confirmar' },
  message: { type: String, required: true },
  type: { type: String, default: 'warning' },
  confirmText: { type: String, default: 'Confirmar' },
  cancelText: { type: String, default: 'Cancelar' }
})
defineEmits(['confirm', 'cancel'])
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200 ease-in-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200 ease-in-out"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-gray-900/50 backdrop-blur-sm" @click="$emit('cancel')"></div>
        
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
          <div class="p-6">
            <h3 class="text-xl font-bold text-gray-900 mb-2">{{ title }}</h3>
            <p class="text-gray-600 whitespace-pre-line text-sm">{{ message }}</p>
          </div>
          
          <div class="bg-gray-50 px-6 py-4 flex justify-end gap-3">
            <button @click="$emit('cancel')" class="px-4 py-2 font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50">
              {{ cancelText }}
            </button>
            <button @click="$emit('confirm')" class="px-4 py-2 font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
