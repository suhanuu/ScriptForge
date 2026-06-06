/** 分章结果中的单个章节 */
export interface ChapterDto {
  index: number;
  title: string;
  content: string;
  charCount: number;
}

/** 上传成功后返回的结果 */
export interface UploadResultDto {
  novelId: string;
  fileName: string;
  totalChars: number;
  chapters: ChapterDto[];
}

/** 转换请求 */
export interface ConvertRequestDto {
  novelUuid: string;
  chapterNumbers?: number[];
}

/** 章节转换进度 */
export interface ChapterProgress {
  chapterNumber: number;
  chapterTitle: string;
  status: string; // CONVERTING | DONE | ERROR
  errorMessage?: string;
}

/** 转换结果 */
export interface ConvertResult {
  scriptId: number;
  status: string; // CONVERTING | READY | PARTIAL_ERROR | ERROR
  yamlContent: string;
  chapterProgress: ChapterProgress[];
}
