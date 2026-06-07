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
    value: props.modelValue, language: "yaml", theme: "vs-dark",
    fontSize: 14, lineNumbers: "on", minimap: { enabled: false }, automaticLayout: true,
  });
  editor.onDidChangeModelContent(() => emit("update:modelValue", editor!.getValue()));
});

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getValue()) editor.setValue(val);
});

/** 在编辑器中显示校验错误标记 */
function setMarkers(errors: string[]) {
  if (!editor) return;
  const model = editor.getModel();
  if (!model) return;
  const markers: monaco.editor.IMarkerData[] = [];
  for (const err of errors) {
    markers.push({
      severity: monaco.MarkerSeverity.Error,
      message: err,
      startLineNumber: 1, startColumn: 1,
      endLineNumber: 1, endColumn: 80,
    });
  }
  monaco.editor.setModelMarkers(model, "validate", markers);
}

onBeforeUnmount(() => editor?.dispose());

defineExpose({ setMarkers });
</script>

<style scoped>
.editor-container { height: 100%; min-height: 400px; }
</style>
