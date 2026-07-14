import { useEffect, useState } from "react";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { toast } from "@/hooks/use-toast";
import { adminApi } from "@/services/authApi";
const AdminOngs = () => {
    const [ongs, setOngs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updatingId, setUpdatingId] = useState(null);
    const load = () => {
        setLoading(true);
        adminApi
            .listUsers("NGO")
            .then((list) => setOngs(list.filter((u) => u.status === "ACTIVE" || u.status === "DISABLED")))
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    };
    useEffect(() => { load(); }, []);
    const toggle = async (ong) => {
        const nextStatus = ong.status === "ACTIVE" ? "DISABLED" : "ACTIVE";
        setUpdatingId(ong.id);
        try {
            await adminApi.updateUserStatus(ong.id, nextStatus);
            toast({ title: nextStatus === "ACTIVE" ? "ONG activada" : "ONG desactivada" });
            load();
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setUpdatingId(null);
        }
    };
    return (<div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Administración</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">ONGs aprobadas</h1>
      </header>

      <div className="grid md:grid-cols-2 gap-4">
        {ongs.map((o) => {
            const active = o.status === "ACTIVE";
            return (<article key={o.id} className="bg-card rounded-2xl border border-border shadow-soft p-5 flex gap-4 hover:shadow-card transition-smooth">
              <div className="w-14 h-14 rounded-2xl bg-secondary grid place-items-center text-2xl">🐾</div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <h3 className="font-display font-bold text-lg truncate">{o.ngoName ?? o.email}</h3>
                  <StatusBadge value={active ? "ACTIVA" : "PAUSADA"}/>
                </div>
                <p className="text-xs text-muted-foreground">{o.email}</p>
                <p className="text-xs text-muted-foreground">Registrada: {new Date(o.createdAt).toLocaleDateString()}</p>
                <div className="mt-3">
                  <Button size="sm" variant="outline" onClick={() => toggle(o)} disabled={updatingId === o.id}>
                    {updatingId === o.id ? "..." : active ? "Desactivar" : "Activar"}
                  </Button>
                </div>
              </div>
            </article>);
        })}
        {!loading && ongs.length === 0 && <p className="text-muted-foreground">Sin ONGs aprobadas.</p>}
        {loading && <p className="text-muted-foreground">Cargando...</p>}
      </div>
    </div>);
};
export default AdminOngs;
