const API_BASE_URL = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api");
const FEED_BASE_URL = `${API_BASE_URL}/feed`;
const TOKEN_KEY = "pawplus_token";

function trimTrailingSlash(value) {
  return value.replace(/\/+$/, "");
}

function buildQuery(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, value);
  });
  const result = query.toString();
  return result ? `?${result}` : "";
}

async function request(path, init = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = {
    "Content-Type": "application/json",
    ...init.headers,
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${FEED_BASE_URL}${path}`, { ...init, headers });
  } catch {
    throw new Error("No se pudo conectar con el servicio de publicaciones.");
  }

  let body = null;
  try {
    body = await response.json();
  } catch {
    // Empty body.
  }

  if (!response.ok || body?.success === false) {
    throw new Error(body?.message || "No fue posible completar la operacion.");
  }

  return body?.data;
}

export const feedApi = {
  listFeedPosts: (filters) => request(`/posts${buildQuery(filters)}`, { method: "GET" }),
  getFeedPostById: (id) => request(`/posts/${id}`, { method: "GET" }),
  listMyFeedPosts: () => request("/ngo/posts", { method: "GET" }),
  createFeedPost: (data) => request("/ngo/posts", {
    method: "POST",
    body: JSON.stringify(data),
  }),
  updateFeedPost: (id, data) => request(`/ngo/posts/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }),
  publishFeedPost: (id) => request(`/ngo/posts/${id}/publish`, { method: "PATCH" }),
  archiveFeedPost: (id) => request(`/ngo/posts/${id}/archive`, { method: "PATCH" }),
};

export const FEED_TYPE_LABELS = {
  RESCUE: "Rescate",
  ADOPTION_DAY: "Jornada de adopcion",
  CAMPAIGN_UPDATE: "Avance de campana",
  SUCCESS_STORY: "Historia feliz",
  URGENT: "Urgencia",
  GENERAL: "Comunicado",
};

export const FEED_STATUS_LABELS = {
  DRAFT: "BORRADOR",
  PUBLISHED: "PUBLICADO",
  ARCHIVED: "ARCHIVADO",
  HIDDEN: "OCULTO",
};

export function feedPostToView(post) {
  return {
    ...post,
    tipo: FEED_TYPE_LABELS[post.type] ?? post.type,
    estado: FEED_STATUS_LABELS[post.status] ?? post.status,
    fecha: post.publishedAt ?? post.createdAt,
    imagen: post.imageUrls?.[0] ?? "",
    resumen: post.summary || post.content,
    ongNombre: post.ngoName || "ONG Paw+",
    ongLogo: post.ngoLogoUrl || "",
  };
}
