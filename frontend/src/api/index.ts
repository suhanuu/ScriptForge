import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 300000,
});

interface SfResult<T> {
  code: number;
  message: string;
  data: T;
}

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg = error.response?.data?.message || error.message || "网络错误";
    console.error("API Error:", msg);
    return Promise.reject(error);
  }
);

export async function get<T>(url: string): Promise<T> {
  const res = await http.get<SfResult<T>>(url);
  if (res.data.code !== 0) {
    throw new Error(res.data.message || "请求失败");
  }
  return res.data.data;
}

export default http;
