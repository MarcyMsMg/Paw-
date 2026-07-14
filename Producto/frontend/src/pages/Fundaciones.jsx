import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { CardFundacion } from "@/components/CardFundacion";
import { CardSkeleton } from "@/components/Skeleton";
import { Input } from "@/components/ui/input";
import { useFetch } from "@/hooks/useFetch";
import { ngosApi, ngoToFundacion } from "@/services/authApi";
const Fundaciones = () => {
    const { data, loading } = useFetch(
        async () => (await ngosApi.list()).map(ngoToFundacion),
        []
    );
    const [q, setQ] = useState("");
    const filtered = useMemo(() => {
        const needle = q.trim().toLowerCase();
        if (!needle) return data ?? [];
        return data?.filter((f) =>
            f.nombre.toLowerCase().includes(needle) ||
            (f.descripcionCorta ?? "").toLowerCase().includes(needle) ||
            (f.descripcion ?? "").toLowerCase().includes(needle) ||
            (f.ubicacion ?? "").toLowerCase().includes(needle)
        ) ?? [];
    }, [data, q]);
    return (<section className="container py-12 md:py-16">
      <header className="max-w-2xl mb-10 animate-fade-in-up">
        <p className="text-sm font-semibold text-primary mb-2">Directorio</p>
        <h1 className="font-display text-4xl md:text-6xl font-bold">Fundaciones aliadas</h1>
        <p className="mt-3 text-muted-foreground">
          Conoce a las ONGs de rescate animal verificadas en nuestra plataforma.
        </p>
      </header>

      <div className="relative max-w-md mb-8">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground"/>
        <Input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Buscar por nombre, ubicación..." className="pl-11 h-12 rounded-full bg-card"/>
      </div>

      {loading ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {Array.from({ length: 6 }).map((_, i) => <CardSkeleton key={i}/>)}
        </div>) : filtered.length === 0 ? (<p className="text-muted-foreground text-center py-16">No encontramos fundaciones con esa búsqueda.</p>) : (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((f, i) => <CardFundacion key={f.id} f={f} index={i}/>)}
        </div>)}
    </section>);
};
export default Fundaciones;
