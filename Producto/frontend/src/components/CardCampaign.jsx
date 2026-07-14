import { Link } from "react-router-dom";
import { Clock, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ProgressBar } from "./ProgressBar";
import { formatCurrency, percent } from "@/lib/format";
export function CardCampaign({ c, index = 0 }) {
    const pct = percent(c.recaudado, c.meta);
    const donorCount = Number(c.donantes ?? 0);
    return (<article className="group bg-card rounded-2xl border border-border overflow-hidden shadow-soft hover:shadow-card hover:-translate-y-1 transition-smooth animate-fade-in-up flex flex-col" style={{ animationDelay: `${index * 60}ms` }}>
      <div className="relative h-52 overflow-hidden">
        {c.imagen ? (<img src={c.imagen} alt={c.titulo} loading="lazy" className="w-full h-full object-cover group-hover:scale-110 transition-smooth duration-700"/>) : (<div className="w-full h-full gradient-soft"/>)}
        <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full text-xs font-semibold bg-background/90 backdrop-blur text-foreground shadow-soft">
          {c.categoria}
        </span>
        {typeof c.diasRestantes === "number" && c.diasRestantes <= 7 && (<span className="absolute top-3 right-3 px-2.5 py-1 rounded-full text-xs font-semibold bg-accent text-accent-foreground shadow-soft animate-float">
            ¡Últimos días!
          </span>)}
      </div>

      <div className="p-5 flex flex-col flex-1">
        <h3 className="font-display font-bold text-lg leading-snug line-clamp-2">{c.titulo}</h3>
        <p className="mt-2 text-sm text-muted-foreground line-clamp-2">{c.descripcionCorta}</p>

        <div className="mt-4">
          <ProgressBar value={pct}/>
          <div className="mt-2 flex items-end justify-between">
            <div>
              <p className="text-lg font-bold text-primary">{formatCurrency(c.recaudado)}</p>
              <p className="text-xs text-muted-foreground">de {formatCurrency(c.meta)}</p>
            </div>
            <p className="text-sm font-bold">{pct}%</p>
          </div>
        </div>

        <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground">
          <span className="flex items-center gap-1"><Users className="w-3.5 h-3.5"/> {donorCount} {donorCount === 1 ? "donante" : "donantes"}</span>
          {typeof c.diasRestantes === "number" && (<span className="flex items-center gap-1"><Clock className="w-3.5 h-3.5"/> {c.diasRestantes} días</span>)}
        </div>

        <Button asChild variant="hero" className="w-full mt-4">
          <Link to={`/crowdfunding/${c.id}`}>Ver campaña</Link>
        </Button>
      </div>
    </article>);
}
