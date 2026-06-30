<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

const props = defineProps<{
    code: string;
}>();

const container = ref<HTMLDivElement | null>(null);
const diagramId = `mermaid-${Math.random().toString(36).slice(2)}`;

const code = computed(() => decodeURIComponent(props.code));

onMounted(async () => {
    const { default: mermaid } = await import('mermaid');
    mermaid.initialize({
        startOnLoad: false
    });

    const { svg } = await mermaid.render(diagramId, code.value);
    if (container.value) {
        container.value.innerHTML = svg;
    }
});
</script>

<template>
    <div ref="container" class="mermaid-wrapper" />
</template>

<style scoped lang="scss">
.mermaid-wrapper {
    margin: 18px 0 28px;
    overflow-x: auto;
    text-align: center;

    :deep(svg) {
        width: auto !important;
        max-width: min(100%, 860px);
        height: auto;
    }
}
</style>