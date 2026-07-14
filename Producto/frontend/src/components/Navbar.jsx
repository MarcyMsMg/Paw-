import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Menu, X, PawPrint, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { NotificationBell } from "@/components/NotificationBell";
import { cn } from "@/lib/utils";
import { useAuth, homeFor } from "@/contexts/AuthContext";
const links = [
    { to: "/", label: "Inicio" },
    { to: "/animales", label: "Animales" },
    { to: "/fundaciones", label: "Fundaciones" },
    { to: "/crowdfundings", label: "Crowdfunding" },
    { to: "/feed", label: "Actualizaciones" },
];
export function Navbar() {
    const [scrolled, setScrolled] = useState(false);
    const [open, setOpen] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const handleLogout = () => { logout(); navigate("/"); };
    useEffect(() => {
        const onScroll = () => setScrolled(window.scrollY > 8);
        onScroll();
        window.addEventListener("scroll", onScroll);
        return () => window.removeEventListener("scroll", onScroll);
    }, []);
    useEffect(() => setOpen(false), [location.pathname]);
    return (<header className={cn("fixed top-0 inset-x-0 z-50 transition-smooth", scrolled
            ? "bg-background/80 backdrop-blur-xl border-b border-border shadow-soft"
            : "bg-transparent")}>
      <nav className="container flex items-center justify-between h-16 md:h-20">
        <Link to="/" className="flex items-center gap-2 group">
          <span className="grid place-items-center w-10 h-10 rounded-2xl gradient-hero text-primary-foreground shadow-glow group-hover:scale-110 transition-smooth">
            <PawPrint className="w-5 h-5"/>
          </span>
          <span className="font-display text-2xl font-bold">
            Paw<span className="text-primary">+</span>
          </span>
        </Link>

        <ul className="hidden md:flex items-center gap-1">
          {links.map((l) => (<li key={l.to}>
              <NavLink to={l.to} end={l.to === "/"} className={({ isActive }) => cn("px-4 py-2 rounded-full text-sm font-medium transition-smooth", isActive
                ? "bg-secondary text-secondary-foreground"
                : "text-foreground/70 hover:text-foreground hover:bg-secondary/60")}>
                {l.label}
              </NavLink>
            </li>))}
        </ul>

        <div className="hidden md:flex items-center gap-2">
          {user ? (<>
              <NotificationBell />
              <Button asChild variant="soft" size="sm"><Link to={homeFor(user.role)}>Mi panel</Link></Button>
              <Button variant="ghost" size="sm" onClick={handleLogout}><LogOut className="w-4 h-4"/> Salir</Button>
            </>) : (<>
              <Button asChild variant="ghost" size="sm"><Link to="/login">Iniciar sesión</Link></Button>
              <Button asChild variant="hero" size="sm"><Link to="/registro-persona">Registrarse</Link></Button>
              <Button asChild variant="outline" size="sm"><Link to="/solicitud-ong">Soy una ONG</Link></Button>
            </>)}
        </div>

        <button aria-label="Abrir menú" className="md:hidden p-2 rounded-lg hover:bg-secondary" onClick={() => setOpen((o) => !o)}>
          {open ? <X className="w-5 h-5"/> : <Menu className="w-5 h-5"/>}
        </button>
      </nav>

      {open && (<div className="md:hidden border-t border-border bg-background/95 backdrop-blur-xl animate-fade-in">
          <ul className="container py-4 flex flex-col gap-1">
            {links.map((l) => (<li key={l.to}>
                <NavLink to={l.to} end={l.to === "/"} className={({ isActive }) => cn("block px-4 py-3 rounded-xl text-sm font-medium", isActive ? "bg-secondary" : "hover:bg-secondary/60")}>
                  {l.label}
                </NavLink>
              </li>))}
            {user ? (<li className="flex flex-col gap-2 pt-2">
                <div className="flex justify-center"><NotificationBell /></div>
                <Button asChild variant="hero" className="w-full"><Link to={homeFor(user.role)}>Mi panel</Link></Button>
                <Button variant="outline" className="w-full" onClick={handleLogout}>Cerrar sesión</Button>
              </li>) : (<li className="flex flex-col gap-2 pt-2">
                <div className="flex gap-2">
                  <Button asChild variant="outline" className="flex-1"><Link to="/login">Iniciar sesión</Link></Button>
                  <Button asChild variant="hero" className="flex-1"><Link to="/registro-persona">Registrarse</Link></Button>
                </div>
                <Button asChild variant="soft" className="w-full"><Link to="/solicitud-ong">Soy una ONG</Link></Button>
              </li>)}
          </ul>
        </div>)}
    </header>);
}
