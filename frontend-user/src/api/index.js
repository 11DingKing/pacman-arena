import axios from "axios";
import { ElMessage } from "element-plus";

const api = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

// 防止重复提示
let lastErrorTime = 0;
let lastErrorMsg = "";

function showError(msg) {
  const now = Date.now();
  // 相同错误消息在1秒内不重复显示
  if (msg === lastErrorMsg && now - lastErrorTime < 1000) {
    return;
  }
  lastErrorTime = now;
  lastErrorMsg = msg;
  ElMessage.error(msg);
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      const skip =
        response.config?.skipShowError ||
        /\/payment\/(create|confirm|order-status|mode)/.test(
          response.config?.url || "",
        );
      if (!skip) showError(res.message || "请求失败");
      if (res.code === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.href = "/login";
      }
      return Promise.reject(res);
    }
    return res;
  },
  (error) => {
    const skip =
      error.config?.skipShowError ||
      /\/payment\/(create|confirm|order-status|mode)/.test(
        error.config?.url || "",
      );
    if (!skip)
      showError(error.response?.data?.message || error.message || "网络错误");
    return Promise.reject(error);
  },
);

// Auth API
export const authApi = {
  login: (data) => api.post("/auth/login", data),
  register: (data) => api.post("/auth/register", data),
  getInfo: () => api.get("/auth/info"),
};

// Game API
export const gameApi = {
  submitScore: (data) => api.post("/game/submit", data),
  getRanking: (limit = 50) => api.get("/game/ranking", { params: { limit } }),
  getMyRecords: (limit = 20) =>
    api.get("/game/my-records", { params: { limit } }),
  getMyBest: () => api.get("/game/my-best"),
};

// Item API
export const itemApi = {
  getList: () => api.get("/item/list"),
  getMyItems: () => api.get("/item/my-items"),
  useItem: (itemId) => api.post("/item/use", { itemId }),
};

// Payment API（显式 skipShowError，仅弹窗内展示错误，避免重复 toast）
export const paymentApi = {
  mode: () => api.get("/payment/mode", { skipShowError: true }),
  createOrder: (data) =>
    api.post("/payment/create", data, { skipShowError: true }),
  confirmOrder: (orderNo) =>
    api.post("/payment/confirm", { orderNo }, { skipShowError: true }),
  orderStatus: (orderNo) =>
    api.get("/payment/order-status", {
      params: { orderNo },
      skipShowError: true,
    }),
  getOrders: (pageNum = 1, pageSize = 10) =>
    api.get("/payment/orders", { params: { pageNum, pageSize } }),
};

export default api;
