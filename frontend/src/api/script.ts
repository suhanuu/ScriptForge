import { post, get } from "./index";
import type { ConvertRequestDto, ConvertResult } from "@/types";

/** 发起剧本转换 */
export function convertScript(data: ConvertRequestDto) {
  return post<ConvertResult>("/scripts/convert", data);
}

/** 获取转换结果 */
export function getScriptResult(id: number) {
  return get<ConvertResult>(`/scripts/${id}`);
}

/** 下载 YAML 的 URL */
export function getDownloadUrl(id: number) {
  return `/api/scripts/${id}/yaml`;
}
