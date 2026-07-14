import { Link } from "react-router-dom";
import { Bell } from "lucide-react";

function formatDate(value) {
  if (!value) return "";
  return new Date(value).toLocaleString("es-CL", { dateStyle: "short", timeStyle: "short" });
}

export function LatestNotifications({ items = [], loading = false }) {
  return (
    <section className="bg-card rounded-2xl border border-border shadow-soft p-5">
      <div className="flex items-center justify-between gap-3 mb-4">
        <h2 className="font-display text-xl font-bold">Ultimas notificaciones</h2>
        <Link to="/notificaciones" className="text-sm font-semibold text-primary hover:underline">Ver todas</Link>
      </div>
      {loading ? (
        <p className="text-sm text-muted-foreground">Cargando notificaciones...</p>
      ) : items.length === 0 ? (
        <div className="flex items-center gap-3 text-sm text-muted-foreground">
          <Bell className="w-4 h-4" /> No hay notificaciones recientes.
        </div>
      ) : (
        <ul className="divide-y divide-border">
          {items.map((item) => (
            <li key={item.id} className="py-3">
              <Link to={item.redirectUrl || "/notificaciones"} className="block hover:text-primary transition-smooth">
                <p className="text-sm font-semibold">{item.title}</p>
                <p className="text-xs text-muted-foreground line-clamp-2 mt-0.5">{item.message}</p>
                <p className="text-[11px] text-muted-foreground mt-1">{formatDate(item.createdAt)}</p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}