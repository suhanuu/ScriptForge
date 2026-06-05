/** API 通信层的基础 HTTP 工具类型 */

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
