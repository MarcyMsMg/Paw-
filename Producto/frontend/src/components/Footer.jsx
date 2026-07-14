import { Link } from "react-router-dom";
import { PawPrint, Heart, Instagram, Twitter, Facebook } from "lucide-react";
export function Footer() {
    return (<footer className="border-t border-border bg-secondary/40 mt-24">
      <div className="container py-12 grid gap-10 md:grid-cols-4">
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <span className="grid place-items-center w-9 h-9 rounded-xl gradient-hero text-primary-foreground">
              <PawPrint className="w-4 h-4"/>
            </span>
            <span className="font-display text-xl font-bold">
              Paw<span className="text-primary">+</span>
            </span>
          </div>
          <p className="text-sm text-muted-foreground max-w-xs">
            Conectamos personas con ONGs de rescate animal. Cada donación es una segunda oportunidad.
          </p>
        </div>

        <div>
          <h4 className="font-semibold mb-3 text-sm">Plataforma</h4>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li><Link to="/fundaciones" className="hover:text-foreground">Fundaciones</Link></li>
            <li><Link to="/crowdfundings" className="hover:text-foreground">Crowdfunding</Link></li>
            <li><Link to="/register" className="hover:text-foreground">Crear cuenta</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="font-semibold mb-3 text-sm">Recursos</h4>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li><a href="#" className="hover:text-foreground">Cómo funciona</a></li>
            <li><a href="#" className="hover:text-foreground">Para ONGs</a></li>
            <li><a href="#" className="hover:text-foreground">Preguntas frecuentes</a></li>
          </ul>
        </div>

        <div>
          <h4 className="font-semibold mb-3 text-sm">Síguenos</h4>
          <div className="flex gap-3">
            {[Instagram, Twitter, Facebook].map((Icon, i) => (<a key={i} href="#" className="w-9 h-9 grid place-items-center rounded-full bg-background border border-border hover:bg-primary hover:text-primary-foreground hover:border-primary transition-smooth">
                <Icon className="w-4 h-4"/>
              </a>))}
          </div>
        </div>
      </div>
      <div className="border-t border-border">
        <div className="container py-5 flex flex-col md:flex-row gap-2 items-center justify-between text-xs text-muted-foreground">
          <p>© {new Date().getFullYear()} Paw+. Todos los derechos reservados.</p>
          <p className="flex items-center gap-1">
            Hecho con <Heart className="w-3 h-3 text-primary fill-primary"/> para los peluditos.
          </p>
        </div>
      </div>
    </footer>);
}
