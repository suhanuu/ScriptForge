import { uploadFile } from "./index";
import type { UploadResultDto } from "@/types";

/** 上传小说文件，返回分章结果 */
export function uploadNovel(file: File) {
  return uploadFile<UploadResultDto>("/novels/upload", file);
}
