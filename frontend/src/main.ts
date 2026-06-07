import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

// Monaco Editor worker 配置
(window as any).MonacoEnvironment = {
  getWorker() {
    return new Worker(
      new URL("monaco-editor/esm/vs/editor/editor.worker.js", import.meta.url),
      { type: "module" }
    );
  },
};

createApp(App).use(router).mount("#app");
