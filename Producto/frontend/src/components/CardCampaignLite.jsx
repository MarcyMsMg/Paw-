import { Link } from "react-router-dom";
import { ProgressBar } from "./ProgressBar";
import { formatCurrency, percent } from "@/lib/format";
export function CardCampaignLite({ c }) {
    const pct = percent(c.recaudado, c.meta);
    return (<Link to={`/crowdfunding/${c.id}`} className="group bg-card rounded-2xl border border-border overflow-hidden shadow-soft hover:shadow-card hover:-translate-y-1 transition-smooth flex flex-col">
      <div className="h-32 overflow-hidden bg-secondary">{c.banner ? (<img src={c.banner} alt={c.titulo} className="w-full h-full object-cover group-hover:scale-110 transition-smooth duration-700"/>) : (<div className="w-full h-full gradient-soft"/>)}</div>
      <div className="p-4 flex-1 flex flex-col">
        <h3 className="font-display font-bold leading-snug line-clamp-2">{c.titulo}</h3>
        <div className="mt-auto pt-3">
          <ProgressBar value={pct}/>
          <div className="mt-1 flex justify-between text-xs">
            <span className="font-bold text-primary">{formatCurrency(c.recaudado)}</span>
            <span className="text-muted-foreground">{pct}%</span>
          </div>
        </div>
      </div>
    </Link>);
}
