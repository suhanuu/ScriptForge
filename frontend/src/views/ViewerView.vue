<template>
  <div class="viewer-page">
    <div class="toolbar">
      <router-link to="/" class="back-link">&larr; 返回</router-link>
      <h2>{{ novelTitle }}</h2>
      <button class="btn btn-ghost" @click="editMode = !editMode">{{ editMode ? '预览' : '编辑' }}</button>
      <button v-if="editMode && scriptId" class="btn btn-ghost" @click="validate">校验</button>
      <button v-if="editMode && scriptId" class="btn btn-primary" @click="saveYaml">保存</button>
      <a v-if="scriptId" :href="downloadUrl" class="btn btn-secondary" download>下载 YAML</a>
    </div>

    <div v-if="loading" class="loading"><div class="spinner" /><p>加载中...</p></div>

    <div v-else class="split-view">
      <div class="panel panel-left">
        <h3>小说原文</h3>
        <div class="chapter-content">
          <div v-for="ch in chapters" :key="ch.index" class="chapter-section">
            <h4>第{{ ch.index }}章 {{ ch.title }}</h4>
            <pre class="content-text">{{ ch.content }}</pre>
          </div>
        </div>
      </div>

      <div class="panel panel-right">
        <h3>剧本 {{ editMode ? '(编辑)' : '' }}</h3>
        <YamlEditor ref="yamlEditor" v-if="editMode" v-model="yamlContent" />
        <pre v-else class="yaml-text"><code>{{ yamlContent || '暂无剧本内容' }}</code></pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { get } from "@/api";
import { getScriptResult, saveScriptYaml, validateYaml } from "@/api/script";
import YamlEditor from "@/components/Script/YamlEditor.vue";
import type { ChapterDto, ConvertResult } from "@/types";

interface ChapterData { chapterNumber: number; title: string; content: string; wordCount: number; }
interface NovelData { fileName: string; }

const route = useRoute();
const scriptId = computed(() => Number(route.params.scriptId));
const novelUuid = computed(() => route.params.novelUuid as string);
const downloadUrl = computed(() => `/api/scripts/${scriptId.value}/yaml`);
const selectedChapters = computed(() => {
  const s = route.query.chapters as string;
  return s ? new Set(s.split(",").map(Number).filter(n => !isNaN(n))) : null;
});

const loading = ref(true);
const editMode = ref(false);
const novelTitle = ref("");
const yamlContent = ref("");
const chapters = ref<ChapterDto[]>([]);

onMounted(async () => {
  try {
    const [novelRes, chaptersRes, scriptRes] = await Promise.all([
      get<NovelData>(`/novels/by-uuid/${novelUuid.value}`),
      get<ChapterData[]>(`/novels/by-uuid/${novelUuid.value}/chapters`),
      getScriptResult(scriptId.value).catch(() => null),
    ]);
    novelTitle.value = novelRes.fileName || "未命名";
    chapters.value = chaptersRes
      .filter(ch => !selectedChapters.value || selectedChapters.value.has(ch.chapterNumber))
      .map(ch => ({ index: ch.chapterNumber, title: ch.title, content: ch.content, charCount: ch.wordCount }));

    if (scriptRes) {
      const result = scriptRes as unknown as ConvertResult;
      yamlContent.value = result.yamlContent || "";
    }
  } finally { loading.value = false; }
});

const yamlEditor = ref<InstanceType<typeof YamlEditor> | null>(null);

async function validate() {
  try {
    const errors = await validateYaml(yamlContent.value);
    if (errors.length === 0) {
      alert("校验通过，无错误");
      yamlEditor.value?.setMarkers([]);
    } else {
      yamlEditor.value?.setMarkers(errors);
    }
  } catch (e: unknown) {
    alert("校验失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
}

async function saveYaml() {
  if (!scriptId.value) return;
  try {
    await saveScriptYaml(scriptId.value, yamlContent.value);
    alert("保存成功");
  } catch (e: unknown) {
    alert("保存失败: " + (e instanceof Error ? e.message : "未知错误"));
  }
}
</script>

<style scoped>
.viewer-page { height: calc(100vh - 60px); display: flex; flex-direction: column; }
.toolbar { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid #e5e7eb; margin-bottom: 16px; }
.toolbar h2 { flex: 1; font-size: 1.1rem; }
.back-link { color: #6366f1; text-decoration: none; }
.btn { padding: 8px 20px; border: none; border-radius: 8px; cursor: pointer; text-decoration: none; display: inline-block; }
.btn-primary { background: #6366f1; color: #fff; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #d1d5db; }
.btn-ghost:hover { background: #f3f4f6; }

.loading { text-align: center; padding: 64px 0; }
.spinner { width: 36px; height: 36px; margin: 0 auto 16px; border: 3px solid #e5e7eb; border-top-color: #6366f1; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.split-view { flex: 1; display: flex; gap: 16px; overflow: hidden; }
.panel { flex: 1; overflow-y: auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.panel h3 { margin-bottom: 12px; font-size: .95rem; color: #6b7280; }

.chapter-section { margin-bottom: 24px; }
.chapter-section h4 { font-size: 1rem; margin-bottom: 8px; color: #6366f1; }
.content-text { white-space: pre-wrap; font-size: .9rem; line-height: 1.8; color: #374151; }

.yaml-text { background: #1e1e2e; color: #cdd6f4; padding: 16px; border-radius: 6px; font-size: .85rem; line-height: 1.6; white-space: pre-wrap; overflow-x: auto; height: 100%; }
</style>
