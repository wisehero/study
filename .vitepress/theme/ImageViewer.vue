<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
const dialog = ref();
const image = ref();

function open(event) {
  const target = event.target.closest('.study-image')?.querySelector('img');
  if (!target) return;
  image.value = { src: target.src, alt: target.alt };
  dialog.value.showModal();
}
onMounted(() => document.addEventListener('click', open));
onUnmounted(() => document.removeEventListener('click', open));
</script>

<template>
  <dialog ref="dialog" class="image-viewer" aria-label="그림 크게 보기" @click="dialog.close()">
    <img v-if="image" :src="image.src" :alt="image.alt">
    <button type="button">닫기</button>
  </dialog>
</template>
