import { Link } from "react-router-dom";
import { MapPin, Heart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { isImageUrl } from "@/services/authApi";
export function CardFundacion({ f, index = 0 }) {
    return (<article className="group relative bg-card rounded-2xl border border-border overflow-hidden shadow-soft hover:shadow-card hover:-translate-y-1 transition-smooth animate-fade-in-up" style={{ animationDelay: `${index * 60}ms` }}>
      <div className="relative h-40 bg-secondary">
        <div className="absolute inset-0 overflow-hidden">
          {f.banner ? (
            <img src={f.banner} alt={f.nombre} loading="lazy" className="w-full h-full object-cover group-hover:scale-110 transition-smooth duration-700"/>
          ) : (
            <div className="w-full h-full gradient-soft"/>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent"/>
        </div>
        <div className="absolute -bottom-7 left-5 z-20 w-14 h-14 rounded-2xl bg-card border-4 border-card overflow-hidden shadow-card">
          {isImageUrl(f.logo) ? (
            <img src={f.logo} alt={f.nombre} className="w-full h-full object-cover"/>
          ) : (
            <div className="w-full h-full grid place-items-center text-3xl">{f.logo || "Paw+"}</div>
          )}
        </div>
      </div>
      <div className="p-5 pt-10">
        <h3 className="font-display font-bold text-xl">{f.nombre}</h3>
        <p className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
          <MapPin className="w-3 h-3"/> {f.ubicacion}
        </p>
        <p className="mt-3 text-sm text-muted-foreground line-clamp-2">{f.descripcionCorta}</p>

        <div className="mt-4 flex items-center gap-4 text-xs">
          <span className="flex items-center gap-1 text-primary font-semibold">
            <Heart className="w-3.5 h-3.5 fill-primary"/> {f.rescatados.toLocaleString()} rescatados
          </span>
        </div>

        <Button asChild variant="soft" className="w-full mt-4">
          <Link to={`/fundacion/${f.id}`}>Ver perfil</Link>
        </Button>
      </div>
    </article>);
}
