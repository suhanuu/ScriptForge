<template>
  <div class="viewer-page">
    <div class="toolbar">
      <router-link to="/" class="back-link">&larr; 返回</router-link>
      <h2>{{ novelTitle }}</h2>
      <a v-if="scriptId" :href="`/api/scripts/${scriptId}/yaml`" class="btn btn-primary" download>下载 YAML</a>
    </div>

    <div v-if="loading" class="loading"><div class="spinner" /><p>加载中...</p></div>

    <div v-else class="split-view">
      <!-- 左栏：原文 -->
      <div class="panel panel-left">
        <h3>小说原文</h3>
        <div class="chapter-content">
          <div v-for="ch in chapters" :key="ch.index" class="chapter-section">
            <h4>第{{ ch.index }}章 {{ ch.title }}</h4>
            <pre class="content-text">{{ ch.content }}</pre>
          </div>
        </div>
      </div>

      <!-- 右栏：剧本 YAML -->
      <div class="panel panel-right">
        <h3>剧本</h3>
        <pre class="yaml-text"><code>{{ yamlContent || '暂无剧本内容' }}</code></pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { get } from "@/api";
import { getScriptResult } from "@/api/script";
import type { ChapterDto, ConvertResult } from "@/types";

interface NovelData { id: number; uuid: string; fileName: string; rawContent: string; totalChars: number; }

const route = useRoute();
const scriptId = computed(() => Number(route.params.scriptId));
const novelUuid = computed(() => route.params.novelUuid as string);

const loading = ref(true);
const novelTitle = ref("");
const yamlContent = ref("");
const chapters = ref<ChapterDto[]>([]);

onMounted(async () => {
  try {
    const [novelRes, scriptRes] = await Promise.all([
      get<NovelData>(`/novels/by-uuid/${novelUuid.value}`),
      getScriptResult(scriptId.value).catch(() => null),
    ]);
    novelTitle.value = novelRes.fileName || "未命名";
    // 后端返回 rawContent，简单分行展示
    const raw = novelRes.rawContent || "";
    const chSections = raw.split(/(?=第[0-9零一二三四五六七八九十百千]+[章节回卷])/);
    chapters.value = chSections
      .filter((s: string) => s.trim())
      .map((s: string, i: number) => ({
        index: i + 1, title: s.split("\n")[0]?.replace(/^#+\s*/, "").trim() || `第${i + 1}章`,
        content: s.trim(), charCount: s.length,
      }));

    if (scriptRes) {
      const result = scriptRes as unknown as ConvertResult;
      yamlContent.value = result.yamlContent || "";
    }
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.viewer-page { height: calc(100vh - 60px); display: flex; flex-direction: column; }
.toolbar { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid #e5e7eb; margin-bottom: 16px; }
.toolbar h2 { flex: 1; font-size: 1.1rem; }
.back-link { color: #6366f1; text-decoration: none; }
.btn { padding: 8px 20px; border: none; border-radius: 8px; cursor: pointer; text-decoration: none; display: inline-block; }
.btn-primary { background: #6366f1; color: #fff; }

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
