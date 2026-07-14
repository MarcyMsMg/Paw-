import { ApiError, getToken } from "@/services/authApi";

const API_BASE_URL = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api");

function trimTrailingSlash(value) {
  return value.replace(/\/+$/, "");
}

async function request(path, init = {}) {
  const token = getToken();
  const headers = { "Content-Type": "application/json", ...init.headers };
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  } catch {
    throw new ApiError("No se pudo cargar estadisticas.", 0);
  }

  let body = null;
  try { body = await res.json(); } catch {}
  if (!res.ok || body?.success === false) {
    throw new ApiError(body?.message || "No se pudo cargar estadisticas.", res.status);
  }
  return body?.data ?? body;
}

export const statsApi = {
  getAdoptionStatsNgo: () => request("/adoptions/stats/ngo"),
  getAdoptionStatsPerson: () => request("/adoptions/stats/person"),
  getAdoptionStatsAdmin: () => request("/adoptions/stats/admin"),
  getCampaignStatsNgo: (ngoId) => request(`/campaigns/stats/ngo?ngoId=${ngoId}`),
  getCampaignStatsAdmin: () => request("/campaigns/stats/admin"),
  getDonationStatsPerson: (donorId) => request(`/donations/stats/person?donorId=${donorId}`),
  getDonationStatsNgo: (ngoId) => request(`/donations/stats/ngo?ngoId=${ngoId}`),
  getDonationStatsAdmin: () => request("/donations/stats/admin"),
  getAdminUserStats: () => request("/admin/stats"),
};

export async function safeStat(loader, fallback) {
  try {
    return await loader();
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn("Dashboard stat failed:", error);
    }
    return fallback;
  }
}