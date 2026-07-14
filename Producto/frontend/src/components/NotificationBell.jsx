import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Bell } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { notificationsApi } from "@/services/notificationsApi";
import { useAuth } from "@/contexts/AuthContext";
import { cn } from "@/lib/utils";

function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleString("es-CL", { dateStyle: "short", timeStyle: "short" });
}

export function NotificationBell({ className }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  const enabled = Boolean(user);
  const unread = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: notificationsApi.getUnreadCount,
    enabled,
    refetchInterval: 30000,
    retry: false,
  });
  const latest = useQuery({
    queryKey: ["notifications", "latest"],
    queryFn: () => notificationsApi.listMyNotifications({ limit: 5 }),
    enabled,
    refetchInterval: 30000,
    retry: false,
  });

  const markRead = useMutation({
    mutationFn: notificationsApi.markAsRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["notifications"] }),
  });

  useEffect(() => {
    const onClick = (event) => {
      if (ref.current && !ref.current.contains(event.target)) setOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  if (!user) return null;

  const count = unread.data?.count ?? 0;
  const items = latest.data ?? [];

  const openNotification = async (notification) => {
    if (!notification.readAt) await markRead.mutateAsync(notification.id);
    setOpen(false);
    if (notification.redirectUrl) navigate(notification.redirectUrl);
  };

  return (
    <div ref={ref} className={cn("relative", className)}>
      <button
        type="button"
        aria-label="Notificaciones"
        className="relative grid place-items-center w-10 h-10 rounded-full hover:bg-secondary transition-smooth"
        onClick={() => setOpen((value) => !value)}
      >
        <Bell className="w-5 h-5" />
        {count > 0 && (
          <span className="absolute -top-1 -right-1 min-w-5 h-5 px-1 rounded-full bg-primary text-primary-foreground text-[11px] font-bold grid place-items-center">
            {count > 9 ? "9+" : count}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 max-w-[calc(100vw-2rem)] rounded-2xl border border-border bg-card shadow-card overflow-hidden z-50">
          <div className="flex items-center justify-between px-4 py-3 border-b border-border">
            <p className="font-semibold">Notificaciones</p>
            <Link to="/notificaciones" onClick={() => setOpen(false)} className="text-xs font-semibold text-primary hover:underline">Ver todas</Link>
          </div>
          {latest.isError || unread.isError ? (
              <div className="px-4 py-6 space-y-1">
                <p className="text-sm font-medium text-destructive">No se pudieron cargar las notificaciones.</p>
                <p className="text-xs text-muted-foreground">
                  Revisa que el servicio notifications esté activo y que el token sea válido.
                </p>
              </div>
            ) : items.length === 0 ? (
              <p className="px-4 py-6 text-sm text-muted-foreground">No tienes notificaciones por ahora.</p>
            ) : (
            <ul className="max-h-96 overflow-y-auto divide-y divide-border">
              {items.map((notification) => (
                <li key={notification.id}>
                  <button type="button" onClick={() => openNotification(notification)} className="w-full text-left px-4 py-3 hover:bg-secondary/60 transition-smooth">
                    <div className="flex gap-2">
                      {!notification.readAt && <span className="mt-1.5 w-2 h-2 rounded-full bg-primary shrink-0" />}
                      <div className="min-w-0">
                        <p className="text-sm font-semibold line-clamp-1">{notification.title}</p>
                        <p className="text-xs text-muted-foreground line-clamp-2 mt-0.5">{notification.message}</p>
                        <p className="text-[11px] text-muted-foreground mt-1">{formatDate(notification.createdAt)}</p>
                      </div>
                    </div>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}