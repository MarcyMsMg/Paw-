import { useEffect, useMemo, useState } from "react";
import { CardAnimal } from "@/components/CardAnimal";
import { adoptionsApi, animalToView, ngosApi, ngoToFundacion } from "@/services/authApi";
import { toast } from "@/hooks/use-toast";

const Animales = () => {
  const [items, setItems] = useState([]);
  const [ongs, setOngs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [esp, setEsp] = useState("TODAS");
  const [tam, setTam] = useState("TODOS");
  const [est, setEst] = useState("TODOS");
  const [ong, setOng] = useState("TODAS");

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError("");

    adoptionsApi.listAnimals()
      .then((animals) => {
        if (!alive) return;
        setItems((animals ?? []).map(animalToView));
      })
      .catch((err) => {
        if (!alive) return;
        setError(err?.message || "No se pudieron cargar los animales.");
        setItems([]);
      })
      .finally(() => alive && setLoading(false));

    ngosApi.list()
      .then((result) => alive && setOngs((result ?? []).map(ngoToFundacion)))
      .catch((err) => {
        if (!alive) return;
        setOngs([]);
        toast({ title: "No se pudieron cargar las ONG", description: err?.message, variant: "destructive" });
      });

    return () => { alive = false; };
  }, []);

  const filtered = useMemo(
    () =>
      items.filter(
        (a) =>
          (esp === "TODAS" || a.especie === esp) &&
          (tam === "TODOS" || a.tamano === tam) &&
          (est === "TODOS" || a.estado === est) &&
          (ong === "TODAS" || a.ongId === ong)
      ),
    [items, esp, tam, est, ong]
  );
  const especies = Array.from(new Set(items.map((i) => i.especie)));

  return (
    <section className="container py-12 md:py-16">
      <header className="max-w-2xl mb-10 animate-fade-in-up">
        <p className="text-sm font-semibold text-primary mb-2">Adopta</p>
        <h1 className="font-display text-4xl md:text-6xl font-bold">Animales en adopcion</h1>
        <p className="mt-3 text-muted-foreground">
          Conoce a los animales que esperan un hogar y encuentra a tu companero ideal.
        </p>
      </header>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-3 mb-8">
        <select value={esp} onChange={(e) => setEsp(e.target.value)} className="h-11 rounded-xl border border-input bg-background px-3 text-sm">
          <option value="TODAS">Todas las especies</option>
          {especies.map((s) => <option key={s}>{s}</option>)}
        </select>
        <select value={tam} onChange={(e) => setTam(e.target.value)} className="h-11 rounded-xl border border-input bg-background px-3 text-sm">
          <option value="TODOS">Todos los tamanos</option><option>Pequeno</option><option>Mediano</option><option>Grande</option>
        </select>
        <select value={est} onChange={(e) => setEst(e.target.value)} className="h-11 rounded-xl border border-input bg-background px-3 text-sm">
          <option value="TODOS">Todos los estados</option><option value="DISPONIBLE">Disponible</option><option value="EN_PROCESO">En proceso</option><option value="ADOPTADO">Adoptado</option>
        </select>
        <select value={ong} onChange={(e) => setOng(e.target.value)} className="h-11 rounded-xl border border-input bg-background px-3 text-sm">
          <option value="TODAS">Todas las ONG</option>
          {ongs.map((o) => <option key={o.id} value={o.id}>{o.nombre}</option>)}
        </select>
      </div>

      {loading && <p className="text-muted-foreground text-center py-16">Cargando animales...</p>}
      {!loading && error && <p className="text-destructive text-center py-16">{error}</p>}
      {!loading && !error && (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map((a) => <CardAnimal key={a.id} a={a} />)}
          {filtered.length === 0 && (
            <p className="text-muted-foreground col-span-full text-center py-16">Sin resultados con estos filtros.</p>
          )}
        </div>
      )}
    </section>
  );
};

export default Animales;