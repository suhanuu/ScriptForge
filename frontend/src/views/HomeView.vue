<template>
  <div class="home">
    <h1>ScriptForge</h1>
    <p class="subtitle">AI 小说转剧本工具</p>

    <UploadPanel @confirm="onConfirm" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import UploadPanel from "@/components/Novel/UploadPanel.vue";

const router = useRouter();

function onConfirm(novelUuid: string) {
  router.push({ name: "convert", params: { novelUuid } });
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
</style>
