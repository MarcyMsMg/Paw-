import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, CheckCheck, Trash2 } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { notificationsApi } from "@/services/notificationsApi";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleString("es-CL", { dateStyle: "medium", timeStyle: "short" });
}

export default function Notificaciones() {
  const [filter, setFilter] = useState("all");
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const filters = useMemo(() => (filter === "unread" ? { unread: true } : {}), [filter]);

  const query = useQuery({
    queryKey: ["notifications", "page", filter],
    queryFn: () => notificationsApi.listMyNotifications(filters),
    refetchInterval: 30000,
    retry: false,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["notifications"] });
  const markRead = useMutation({ mutationFn: notificationsApi.markAsRead, onSuccess: invalidate });
  const markAll = useMutation({ mutationFn: notificationsApi.markAllAsRead, onSuccess: invalidate });
  const remove = useMutation({ mutationFn: notificationsApi.deleteNotification, onSuccess: invalidate });

  const openNotification = async (notification) => {
    if (!notification.readAt) await markRead.mutateAsync(notification.id);
    if (notification.redirectUrl) navigate(notification.redirectUrl);
  };

  const notifications = query.data ?? [];

  return (
    <main className="container py-28 md:py-32 space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-primary">Centro de actividad</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Notificaciones</h1>
        </div>
        <Button variant="soft" onClick={() => markAll.mutate()} disabled={markAll.isPending || notifications.length === 0}>
          <CheckCheck className="w-4 h-4" /> Marcar todas como leidas
        </Button>
      </header>

      <div className="flex gap-2">
        <Button variant={filter === "all" ? "default" : "outline"} size="sm" onClick={() => setFilter("all")}>Todas</Button>
        <Button variant={filter === "unread" ? "default" : "outline"} size="sm" onClick={() => setFilter("unread")}>No leidas</Button>
      </div>

      <section className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
        {query.isLoading ? (
          <p className="p-6 text-sm text-muted-foreground">Cargando notificaciones...</p>
        ) : query.isError ? (
          <p className="p-6 text-sm text-destructive">No se pudieron cargar las notificaciones.</p>
        ) : notifications.length === 0 ? (
          <div className="p-10 text-center text-muted-foreground">
            <Bell className="w-10 h-10 mx-auto mb-3" />
            <p>No tienes notificaciones para este filtro.</p>
          </div>
        ) : (
          <ul className="divide-y divide-border">
            {notifications.map((notification) => (
              <li key={notification.id} className={cn("p-4 md:p-5 flex gap-4", !notification.readAt && "bg-secondary/40")}>
                <button type="button" onClick={() => openNotification(notification)} className="flex-1 text-left min-w-0">
                  <div className="flex items-center gap-2">
                    {!notification.readAt && <span className="w-2 h-2 rounded-full bg-primary shrink-0" />}
                    <h2 className="font-semibold truncate">{notification.title}</h2>
                  </div>
                  <p className="text-sm text-muted-foreground mt-1">{notification.message}</p>
                  <p className="text-xs text-muted-foreground mt-2">{formatDate(notification.createdAt)}</p>
                </button>
                <div className="flex items-start gap-2">
                  {!notification.readAt && (
                    <Button variant="outline" size="sm" onClick={() => markRead.mutate(notification.id)}>Leer</Button>
                  )}
                  <Button variant="ghost" size="icon" onClick={() => remove.mutate(notification.id)} aria-label="Eliminar">
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}