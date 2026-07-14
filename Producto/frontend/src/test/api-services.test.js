import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { feedApi, feedPostToView } from "../services/feedApi";
import { notificationsApi } from "../services/notificationsApi";
import { safeStat, statsApi } from "../services/statsApi";

const TOKEN_KEY = "pawplus_token";

afterEach(() => {
  vi.restoreAllMocks();
});

function jsonResponse(body, init = {}) {
  return new Response(JSON.stringify(body), {
    status: init.status ?? 200,
    headers: { "Content-Type": "application/json" },
  });
}

describe("feedApi", () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = vi.fn();
  });

  it("listFeedPosts builds query params and sends bearer token", async () => {
    // Arrange
    localStorage.setItem(TOKEN_KEY, "token-123");
    fetch.mockResolvedValueOnce(jsonResponse({ success: true, data: [{ id: "p1" }] }));

    // Act
    const result = await feedApi.listFeedPosts({ type: "RESCUE", search: "luna", empty: "" });

    // Assert
    expect(result).toEqual([{ id: "p1" }]);
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/feed/posts?type=RESCUE&search=luna",
      expect.objectContaining({
        method: "GET",
        headers: expect.objectContaining({ Authorization: "Bearer token-123" }),
      })
    );
  });

  it("createFeedPost serializes body", async () => {
    // Arrange
    fetch.mockResolvedValueOnce(jsonResponse({ success: true, data: { id: "p2" } }));
    const payload = { title: "Rescate", content: "Contenido", type: "RESCUE" };

    // Act
    const result = await feedApi.createFeedPost(payload);

    // Assert
    expect(result).toEqual({ id: "p2" });
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/feed/ngo/posts",
      expect.objectContaining({ method: "POST", body: JSON.stringify(payload) })
    );
  });

  it("throws backend message when response is not successful", async () => {
    // Arrange
    fetch.mockResolvedValueOnce(jsonResponse({ success: false, message: "No autorizado" }, { status: 403 }));

    // Act + Assert
    await expect(feedApi.listMyFeedPosts()).rejects.toThrow("No autorizado");
  });

  it("maps feed posts to view model with fallbacks", () => {
    // Arrange
    const post = {
      type: "SUCCESS_STORY",
      status: "PUBLISHED",
      imageUrls: ["https://img.test/a.jpg"],
      summary: "Resumen",
      content: "Contenido",
      publishedAt: "2026-07-02T10:00:00Z",
    };

    // Act
    const view = feedPostToView(post);

    // Assert
    expect(view.tipo).toBe("Historia feliz");
    expect(view.estado).toBe("PUBLICADO");
    expect(view.imagen).toBe("https://img.test/a.jpg");
    expect(view.ongNombre).toBe("ONG Paw+");
  });
});

describe("notificationsApi", () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = vi.fn();
  });

  it("listMyNotifications sends filters and token", async () => {
    // Arrange
    localStorage.setItem(TOKEN_KEY, "token-456");
    fetch.mockResolvedValueOnce(jsonResponse({ success: true, data: [{ id: "n1" }] }));

    // Act
    const result = await notificationsApi.listMyNotifications({ unread: true, type: "SYSTEM_ALERT" });

    // Assert
    expect(result).toEqual([{ id: "n1" }]);
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/notifications/me?unread=true&type=SYSTEM_ALERT",
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: "Bearer token-456" }) })
    );
  });

  it("markAsRead uses PATCH endpoint", async () => {
    // Arrange
    fetch.mockResolvedValueOnce(jsonResponse({ success: true, data: { id: "n1", readAt: "now" } }));

    // Act
    const result = await notificationsApi.markAsRead("n1");

    // Assert
    expect(result.readAt).toBe("now");
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/notifications/n1/read",
      expect.objectContaining({ method: "PATCH" })
    );
  });

  it("throws ApiError on network failure", async () => {
    // Arrange
    fetch.mockRejectedValueOnce(new Error("offline"));

    // Act + Assert
    await expect(notificationsApi.getUnreadCount()).rejects.toMatchObject({ status: 0 });
  });
});

describe("statsApi", () => {
  beforeEach(() => {
    localStorage.clear();
    global.fetch = vi.fn();
  });

  it("loads donation stats for person", async () => {
    // Arrange
    localStorage.setItem(TOKEN_KEY, "token-789");
    fetch.mockResolvedValueOnce(jsonResponse({ success: true, data: { totalDonated: 1000 } }));

    // Act
    const result = await statsApi.getDonationStatsPerson("person-1");

    // Assert
    expect(result.totalDonated).toBe(1000);
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/donations/stats/person?donorId=person-1",
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: "Bearer token-789" }) })
    );
  });

  it("safeStat returns loader value when successful", async () => {
    // Arrange
    const loader = vi.fn().mockResolvedValue({ count: 2 });

    // Act
    const result = await safeStat(loader, { count: 0 });

    // Assert
    expect(result).toEqual({ count: 2 });
  });

  it("safeStat returns fallback when loader fails", async () => {
    // Arrange
    const loader = vi.fn().mockRejectedValue(new Error("boom"));
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});

    // Act
    const result = await safeStat(loader, { count: 0 });

    // Assert
    expect(result).toEqual({ count: 0 });
    expect(warn).toHaveBeenCalledWith("Dashboard stat failed:", expect.any(Error));
  });
});
