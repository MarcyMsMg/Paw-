import { ApiError, getToken } from "@/services/authApi";

const API_BASE_URL = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api");

function trimTrailingSlash(value) {
  return value.replace(/\/+$/, "");
}

function buildQuery(filters = {}) {
  const query = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, value);
  });
  const qs = query.toString();
  return qs ? `?${qs}` : "";
}

async function request(path, init = {}) {
  const token = getToken();
  const headers = {
    "Content-Type": "application/json",
    ...init.headers,
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  } catch {
    throw new ApiError("No se pudo conectar con notificaciones.", 0);
  }

  let body = null;
  try {
    body = await res.json();
  } catch {
    // Empty response.
  }

  if (!res.ok || body?.success === false) {
    throw new ApiError(body?.message || "No se pudo completar la accion.", res.status);
  }
  return body?.data ?? body;
}

export const notificationsApi = {
  listMyNotifications: (filters = {}) => request(`/notifications/me${buildQuery(filters)}`),
  getUnreadCount: () => request("/notifications/me/unread-count"),
  markAsRead: (id) => request(`/notifications/${id}/read`, { method: "PATCH" }),
  markAllAsRead: () => request("/notifications/read-all", { method: "PATCH" }),
  deleteNotification: (id) => request(`/notifications/${id}`, { method: "DELETE" }),
};