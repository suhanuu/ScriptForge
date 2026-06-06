<template>
  <div class="convert-page">
    <router-link to="/" class="back-link">&larr; 返回首页</router-link>

    <h2 v-if="novelUuid">开始转换</h2>

    <!-- 章节选择 -->
    <section v-if="!converting && !result" class="chapter-select">
      <p class="hint" v-if="!novelUuid">请先从首页上传小说</p>
      <div v-else class="actions">
        <button class="btn btn-primary" :disabled="converting" @click="startConversion">
          开始转换
        </button>
      </div>
    </section>

    <!-- 转换中 -->
    <section v-if="converting" class="progress-section">
      <div class="spinner" />
      <h3>正在转换中...</h3>
      <p>AI 正在逐章将小说转换为剧本，请耐心等待</p>
    </section>

    <!-- 结果 -->
    <section v-if="result" class="result-section">
      <div class="result-header">
        <h3>转换完成</h3>
        <span class="status-badge" :class="result.status">{{ statusLabel }}</span>
      </div>

      <!-- 章节进度 -->
      <div v-if="result.chapterProgress?.length" class="chapter-progress">
        <div v-for="cp in result.chapterProgress" :key="cp.chapterNumber" class="cp-item" :class="cp.status">
          <span>第{{ cp.chapterNumber }}章 {{ cp.chapterTitle }}</span>
          <span>{{ cp.status === 'DONE' ? '完成' : cp.status === 'ERROR' ? '失败' : '转换中' }}</span>
          <span v-if="cp.errorMessage" class="cp-error">{{ cp.errorMessage }}</span>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="result-actions">
        <a v-if="result.scriptId" :href="getDownloadUrl(result.scriptId)" class="btn btn-primary" download>
          下载 YAML
        </a>
        <button class="btn btn-ghost" @click="reset">重新转换</button>
      </div>

      <!-- YAML 预览 -->
      <div v-if="result.yamlContent" class="yaml-preview">
        <h3>剧本预览</h3>
        <pre><code>{{ result.yamlContent }}</code></pre>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import { convertScript, getDownloadUrl } from "@/api/script";
import type { ConvertResult } from "@/types";

const route = useRoute();
const novelUuid = ref((route.params.novelUuid as string) || "");

const converting = ref(false);
const result = ref<ConvertResult | null>(null);

const statusLabel = computed(() => {
  const map: Record<string, string> = { READY: "全部成功", PARTIAL_ERROR: "部分成功", ERROR: "失败" };
  return map[result.value?.status || ""] || result.value?.status || "";
});

async function startConversion() {
  if (!novelUuid.value) return;
  converting.value = true;
  result.value = null;
  try {
    result.value = await convertScript({ novelUuid: novelUuid.value });
  } catch (e) {
    result.value = { scriptId: 0, status: "ERROR", yamlContent: "", chapterProgress: [] };
  } finally {
    converting.value = false;
  }
}

function reset() { result.value = null; }
</script>

<style scoped>
.convert-page { max-width: 800px; margin: 0 auto; padding-top: 24px; }
.back-link { color: #6366f1; text-decoration: none; }
.actions { margin-top: 32px; text-align: center; }
.hint { text-align: center; color: #9ca3af; margin-top: 48px; }

.btn { padding: 10px 32px; border: none; border-radius: 8px; cursor: pointer; text-decoration: none; display: inline-block; }
.btn:disabled { opacity: .5; cursor: not-allowed; }
.btn-primary { background: #6366f1; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #4f46e5; }
.btn-ghost { background: transparent; color: #6b7280; }

.progress-section { text-align: center; padding: 64px 0; }
.spinner { width: 40px; height: 40px; margin: 0 auto 24px; border: 4px solid #e5e7eb; border-top-color: #6366f1; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.result-section { margin-top: 24px; }
.result-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.status-badge { font-size: .85rem; padding: 2px 10px; border-radius: 12px; background: #d1fae5; color: #10b981; }
.status-badge.ERROR, .status-badge.PARTIAL_ERROR { background: #fee2e2; color: #ef4444; }

.chapter-progress { display: flex; flex-direction: column; gap: 4px; margin-bottom: 20px; }
.cp-item { display: flex; gap: 16px; padding: 8px 12px; border-radius: 6px; font-size: .9rem; }
.cp-item.DONE { background: #f0fdf4; }
.cp-item.ERROR { background: #fef2f2; }
.cp-error { color: #ef4444; font-size: .8rem; }

.result-actions { display: flex; gap: 12px; margin-bottom: 24px; }

.yaml-preview { background: #1e1e2e; color: #cdd6f4; padding: 20px; border-radius: 8px; overflow-x: auto; }
.yaml-preview h3 { color: #a6adc8; margin-bottom: 12px; }
.yaml-preview pre { font-family: monospace; font-size: .85rem; line-height: 1.5; white-space: pre-wrap; }
</style>
