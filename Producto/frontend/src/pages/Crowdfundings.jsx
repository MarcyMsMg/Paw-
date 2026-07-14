import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { CardCampaign } from "@/components/CardCampaign";
import { CardSkeleton } from "@/components/Skeleton";
import { Input } from "@/components/ui/input";
import { useFetch } from "@/hooks/useFetch";
import { publicCampaignsToViewWithDonors } from "@/services/authApi";
import { cn } from "@/lib/utils";
const Crowdfundings = () => {
    const { data, loading } = useFetch(
        async () => publicCampaignsToViewWithDonors(),
        []
    );
    const [q, setQ] = useState("");
    const [cat, setCat] = useState(null);
    const categorias = useMemo(() => Array.from(new Set(data?.map((c) => c.categoria) ?? [])), [data]);
    const filtered = useMemo(() => data?.filter((c) => {
        const matchQ = c.titulo.toLowerCase().includes(q.toLowerCase()) ||
            c.descripcionCorta.toLowerCase().includes(q.toLowerCase());
        const matchCat = !cat || c.categoria === cat;
        return matchQ && matchCat;
    }) ?? [], [data, q, cat]);
    return (<section className="container py-12 md:py-16">
      <header className="max-w-2xl mb-10 animate-fade-in-up">
        <p className="text-sm font-semibold text-primary mb-2">Campañas activas</p>
        <h1 className="font-display text-4xl md:text-6xl font-bold">Crowdfunding por los peluditos</h1>
        <p className="mt-3 text-muted-foreground">
          Apoya causas reales y sigue el impacto de cada donación.
        </p>
      </header>

      <div className="flex flex-col md:flex-row gap-4 mb-8">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground"/>
          <Input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Buscar campañas..." className="pl-11 h-12 rounded-full bg-card"/>
        </div>
      </div>

      <div className="flex gap-2 mb-8 overflow-x-auto pb-2">
        <button onClick={() => setCat(null)} className={cn("px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-smooth", !cat ? "bg-primary text-primary-foreground" : "bg-secondary hover:bg-secondary/70")}>
          Todas
        </button>
        {categorias.map((c) => (<button key={c} onClick={() => setCat(c)} className={cn("px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-smooth", cat === c ? "bg-primary text-primary-foreground" : "bg-secondary hover:bg-secondary/70")}>
            {c}
          </button>))}
      </div>

      {loading ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {Array.from({ length: 6 }).map((_, i) => <CardSkeleton key={i}/>)}
        </div>) : filtered.length === 0 ? (<p className="text-muted-foreground text-center py-16">Sin campañas para esos filtros.</p>) : (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((c, i) => <CardCampaign key={c.id} c={c} index={i}/>)}
        </div>)}
    </section>);
};
export default Crowdfundings;
