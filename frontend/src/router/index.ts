import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: () => import("@/views/HomeView.vue") },
    { path: "/convert/:novelUuid?", name: "convert", component: () => import("@/views/ConvertView.vue") },
  ],
});

export default router;
