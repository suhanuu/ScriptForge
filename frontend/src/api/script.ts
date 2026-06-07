import { post, get, put } from "./index";
import type { ConvertRequestDto, ConvertResult } from "@/types";

/** 发起剧本转换 */
export function convertScript(data: ConvertRequestDto) {
  return post<ConvertResult>("/scripts/convert", data);
}

/** 获取转换结果 */
export function getScriptResult(id: number) {
  return get<ConvertResult>(`/scripts/${id}`);
}

/** Schema 校验 YAML */
export function validateYaml(yaml: string) {
  return post<string[]>("/scripts/validate", { yaml });
}

/** 保存编辑后的 YAML */
export function saveScriptYaml(id: number, yamlContent: string) {
  return put(`/scripts/${id}/yaml`, { yamlContent });
}

/** 下载 YAML 的 URL */
export function getDownloadUrl(id: number) {
  return `/api/scripts/${id}/yaml`;
}
