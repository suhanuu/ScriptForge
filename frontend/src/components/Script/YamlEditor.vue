<template>
  <div ref="editorContainer" class="editor-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from "vue";
import * as monaco from "monaco-editor";

const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{ "update:modelValue": [value: string] }>();

const editorContainer = ref<HTMLElement | null>(null);
let editor: monaco.editor.IStandaloneCodeEditor | null = null;

onMounted(() => {
  if (!editorContainer.value) return;
  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: "yaml",
    theme: "vs-dark",
    fontSize: 14,
    lineNumbers: "on",
    minimap: { enabled: false },
    automaticLayout: true,
  });
  editor.onDidChangeModelContent(() => {
    emit("update:modelValue", editor!.getValue());
  });
});

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getValue()) {
    editor.setValue(val);
  }
});

onBeforeUnmount(() => {
  editor?.dispose();
});
</script>

<style scoped>
.editor-container { height: 100%; min-height: 400px; }
</style>
