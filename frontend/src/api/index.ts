import axios from "axios";

/** 后端统一响应包装 */
interface SfResult<T> {
  code: number;
  message: string;
  data: T;
}

const http = axios.create({
  baseURL: "/api",
  timeout: 300000,
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg = error.response?.data?.message || error.message || "网络错误";
    console.error("API Error:", msg);
    return Promise.reject(error);
  }
);

/** GET 请求，自动解包 SfResult.data，code≠0 时抛异常 */
export async function get<T>(url: string): Promise<T> {
  const res = await http.get<SfResult<T>>(url);
  if (res.data.code !== 0) {
    throw new Error(res.data.message || "请求失败");
  }
  return res.data.data;
}

/** POST JSON 请求，自动解包 SfResult.data */
export async function post<T>(url: string, data?: unknown): Promise<T> {
  const res = await http.post<SfResult<T>>(url, data);
  if (res.data.code !== 0) throw new Error(res.data.message || "请求失败");
  return res.data.data;
}

/** PUT JSON 请求，自动解包 SfResult.data */
export async function put<T>(url: string, data?: unknown): Promise<T> {
  const res = await http.put<SfResult<T>>(url, data);
  if (res.data.code !== 0) throw new Error(res.data.message || "请求失败");
  return res.data.data;
}

/** 上传文件，Content-Type 为 multipart/form-data */
export async function uploadFile<T>(url: string, file: File): Promise<T> {
  const form = new FormData();
  form.append("file", file);
  const res = await http.post<SfResult<T>>(url, form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  if (res.data.code !== 0) {
    throw new Error(res.data.message || "上传失败");
  }
  return res.data.data;
}

export default http;
