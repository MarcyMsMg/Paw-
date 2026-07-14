import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Menu, X, PawPrint, LogOut, User } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { NotificationBell } from "@/components/NotificationBell";
import { cn } from "@/lib/utils";
import { isImageUrl } from "@/services/authApi";

export function DashboardLayout({ items, title }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const handleLogout = () => { logout(); navigate("/"); };

  const SidebarContent = (
    <div className="flex h-full w-full min-w-0 flex-col overflow-hidden">
      <Link to="/" className="flex shrink-0 items-center gap-2 border-b border-border px-6 py-5">
        <span className="grid place-items-center w-9 h-9 rounded-xl gradient-hero text-primary-foreground shadow-glow">
          <PawPrint className="w-4 h-4" />
        </span>
        <span className="font-display text-xl font-bold">Paw<span className="text-primary">+</span></span>
      </Link>
      <div className="shrink-0 px-6 py-4">
        <p className="text-xs uppercase tracking-wider text-muted-foreground font-semibold">{title}</p>
      </div>
      <nav className="min-h-0 flex-1 space-y-1 overflow-y-auto px-3">
        {items.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to} end onClick={() => setOpen(false)} className={({ isActive }) => cn("flex w-full min-w-0 items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-smooth", isActive ? "bg-primary text-primary-foreground shadow-soft" : "text-foreground/70 hover:bg-secondary")}>
            <Icon className="h-4 w-4 shrink-0" />
            <span className="truncate">{label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="shrink-0 space-y-3 border-t border-border p-4">
        <div className="flex items-center gap-3 px-2">
          <div className="w-10 h-10 rounded-full bg-secondary overflow-hidden shrink-0 grid place-items-center">
            {isImageUrl(user?.foto) ? (
              <img src={user.foto} alt={user?.nombre ?? ""} className="w-full h-full object-cover" />
            ) : (
              <User className="w-5 h-5 text-muted-foreground" />
            )}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold truncate">{user?.nombre}</p>
            <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
          </div>
        </div>
        <Button variant="outline" size="sm" className="w-full" onClick={handleLogout}>
          <LogOut className="w-4 h-4" /> Cerrar sesion
        </Button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-secondary/30">
      <header className="md:hidden sticky top-0 z-40 flex items-center justify-between px-4 h-14 bg-card border-b border-border">
        <Link to="/" className="flex items-center gap-2">
          <span className="grid place-items-center w-8 h-8 rounded-lg gradient-hero text-primary-foreground"><PawPrint className="w-4 h-4" /></span>
          <span className="font-display font-bold">Paw<span className="text-primary">+</span></span>
        </Link>
        <div className="flex items-center gap-1">
          <NotificationBell />
          <button onClick={() => setOpen(true)} className="p-2 rounded-lg hover:bg-secondary"><Menu className="w-5 h-5" /></button>
        </div>
      </header>

      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 overflow-hidden border-r border-border bg-card md:flex">
        {SidebarContent}
      </aside>

      {open && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div className="absolute inset-0 bg-black/40" onClick={() => setOpen(false)} />
          <div className="relative w-72 max-w-[80%] bg-card animate-fade-in">
            <button onClick={() => setOpen(false)} className="absolute top-3 right-3 p-2 rounded-lg hover:bg-secondary"><X className="w-5 h-5" /></button>
            {SidebarContent}
          </div>
        </div>
      )}

      <main className="md:pl-64">
        <div className="hidden md:flex justify-end px-8 pt-5">
          <NotificationBell />
        </div>
        <div className="container max-w-6xl py-6 md:py-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}