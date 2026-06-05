<template>
  <div>
    <header>
      <h1>ScriptForge</h1>
      <p>AI 小说转剧本工具</p>
    </header>

    <main>
      <button :disabled="checking" @click="checkHealth">
        {{ checking ? "检查中..." : "检查后端连接" }}
      </button>
      <p v-if="healthResult !== null" class="result">
        {{ healthResult }}
      </p>

      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { get } from "@/api";

interface HealthResponse {
  status: string;
  timestamp: string;
}

const checking = ref(false);
const healthResult = ref<string | null>(null);

async function checkHealth() {
  checking.value = true;
  healthResult.value = null;
  try {
    const data = await get<HealthResponse>("/health");
    healthResult.value = `后端连接成功: ${data.status} (${data.timestamp})`;
  } catch (e: unknown) {
    healthResult.value = `连接失败: ${e instanceof Error ? e.message : "未知错误"}`;
  } finally {
    checking.value = false;
  }
}
</script>
