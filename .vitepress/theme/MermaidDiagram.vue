<script setup>
import { ref, onMounted, watch, useId } from 'vue';
import { useData } from 'vitepress';
const props = defineProps({ code: { type: String, required: true } });
const { isDark } = useData();
const svg = ref('');
const error = ref('');
const id = 'diagram-' + useId().replaceAll(':', '-');

onMounted(async () => {
  const { default: mermaid } = await import('mermaid');
  let version = 0;
  watch([() => props.code, isDark], async () => {
    const current = ++version;
    mermaid.initialize({ startOnLoad: false, securityLevel: 'strict',
      theme: isDark.value ? 'dark' : 'default', flowchart: { useMaxWidth: false } });
    try {
      const result = await mermaid.render(`${id}-${current}`, props.code);
      if (current === version) { svg.value = result.svg; error.value = ''; }
    } catch {
      if (current === version) error.value = '그림을 표시하지 못했습니다.';
    }
  }, { immediate: true });
});
</script>

<template>
  <figure class="mermaid-diagram" aria-label="학습 흐름도">
    <div v-if="svg && !error" v-html="svg" />
    <template v-else>
      <p v-if="error" role="alert">{{ error }}</p>
      <pre>{{ code }}</pre>
    </template>
  </figure>
</template>
