import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Edit } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { campaignsApi, campaignToView } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { ProgressBar } from "@/components/ProgressBar";
import { StatusBadge } from "@/components/StatusBadge";
import { formatCurrency, percent } from "@/lib/format";
import { toast } from "@/hooks/use-toast";
const OngCampanas = () => {
    const { user } = useAuth();
    const [items, setItems] = useState([]);
    const reload = () => user && campaignsApi.list(user.id).then((cs) => setItems(cs.map(campaignToView)));
    useEffect(() => { reload(); }, [user]);
    const finalizar = async (c) => {
        try {
            await campaignsApi.finish(c.id);
            toast({ title: "Campaña finalizada" });
            reload();
        } catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
    };
    return (<div className="space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-primary">Mi ONG</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Campañas de crowdfunding</h1>
        </div>
        <Button asChild variant="hero"><Link to="/ong/campanas/nueva"><Plus className="w-4 h-4"/> Nueva campaña</Link></Button>
      </header>

      <div className="grid md:grid-cols-2 gap-5">
        {items.map((c) => {
            const pct = percent(c.recaudado, c.meta);
            return (<article key={c.id} className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden hover:shadow-card transition-smooth">
              <div className="h-32 bg-secondary">{c.banner ? (<img src={c.banner} alt={c.titulo} className="w-full h-full object-cover"/>) : (<div className="w-full h-full gradient-soft"/>)}</div>
              <div className="p-5 space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-display font-bold text-lg">{c.titulo}</h3>
                  <StatusBadge value={c.estado}/>
                </div>
                <ProgressBar value={pct}/>
                <div className="flex justify-between text-sm">
                  <span className="font-bold text-primary">{formatCurrency(c.recaudado)}</span>
                  <span className="text-muted-foreground">de {formatCurrency(c.meta)}</span>
                </div>
                <div className="flex gap-2 pt-2">
                  <Button asChild size="sm" variant="soft" className="flex-1"><Link to={`/ong/campanas/editar/${c.id}`}><Edit className="w-3.5 h-3.5"/> Editar</Link></Button>
                  {c.estado === "ACTIVA" && <Button size="sm" variant="outline" onClick={() => finalizar(c)}>Finalizar</Button>}
                </div>
              </div>
            </article>);
        })}
        {items.length === 0 && <p className="text-muted-foreground col-span-full">Aún no tienes campañas.</p>}
      </div>
    </div>);
};
export default OngCampanas;
