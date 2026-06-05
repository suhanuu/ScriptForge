<template>
  <div class="home">
    <h1>ScriptForge</h1>
    <p class="subtitle">AI 小说转剧本工具</p>

    <!-- 步骤 1：上传 -->
    <UploadPanel
      v-if="!novelId"
      @confirm="onConfirm"
    />

    <!-- 步骤 2：分章已确认，等待下一步 -->
    <div v-else class="next-step">
      <p>分章结果已确认，novelId: {{ novelId }}</p>
      <p class="placeholder">转换功能将在后续 PR 中实现</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import UploadPanel from "@/components/Novel/UploadPanel.vue";

const novelId = ref<string | null>(null);

function onConfirm(id: string) {
  novelId.value = id;
}
</script>

<style scoped>
.home {
  max-width: 720px;
  margin: 0 auto;
  padding-top: 48px;
  text-align: center;
}
.subtitle {
  color: #6b7280;
  margin-bottom: 32px;
}
.next-step {
  margin-top: 32px;
  padding: 24px;
  background: #f0fdf4;
  border-radius: 8px;
}
.placeholder {
  color: #9ca3af;
  font-size: .9rem;
  margin-top: 8px;
}
</style>
