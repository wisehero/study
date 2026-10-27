<script setup>
import { ref, onMounted } from 'vue';
import { useSidebar } from 'vitepress/theme';
const { hasSidebar } = useSidebar();
const hidden = ref(false);

// 접힌 상태는 브라우저에 기억한다. 저장소를 못 쓰는 환경에서도 버튼은 동작한다
onMounted(() => { hidden.value = document.documentElement.classList.contains('sidebar-hidden'); });
function toggle() {
  hidden.value = !hidden.value;
  document.documentElement.classList.toggle('sidebar-hidden', hidden.value);
  try { localStorage.setItem('sidebar-hidden', hidden.value ? '1' : '0'); } catch {}
}
</script>

<template>
  <button v-if="hasSidebar" type="button" class="sidebar-toggle" :aria-expanded="!hidden"
    :aria-label="hidden ? '사이드바 펼치기' : '사이드바 접기'" :title="hidden ? '사이드바 펼치기' : '사이드바 접기'" @click="toggle">
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.8">
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="M9 4v16" />
      <path v-if="hidden" d="M13 10l2 2-2 2" />
      <path v-else d="M15 10l-2 2 2 2" />
    </svg>
  </button>
</template>
