import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Eye } from "lucide-react";
import { adminApi } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { toast } from "@/hooks/use-toast";
const STATUS_LABELS = {
    PENDING: "PENDIENTE",
    APPROVED: "APROBADA",
    REJECTED: "RECHAZADA",
};
const AdminSolicitudes = () => {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState("TODAS");
    useEffect(() => {
        adminApi
            .listNgoRequests()
            .then(setItems)
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    }, []);
    const filtered = items.filter((s) => filter === "TODAS" || s.status === filter);
    return (<div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Administración</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Solicitudes de registro ONG</h1>
      </header>

      <div className="flex flex-wrap gap-2">
        {["TODAS", "PENDING", "APPROVED", "REJECTED"].map((f) => (<button key={f} onClick={() => setFilter(f)} className={`px-4 py-2 rounded-full text-xs font-semibold border transition-smooth ${filter === f ? "bg-primary text-primary-foreground border-primary" : "bg-card border-border hover:border-primary"}`}>
            {f === "TODAS" ? "TODAS" : STATUS_LABELS[f]}
          </button>))}
      </div>

      <div className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-secondary/50">
              <tr className="text-left">
                <th className="px-4 py-3 font-semibold">ONG</th>
                <th className="px-4 py-3 font-semibold">Correo</th>
                <th className="px-4 py-3 font-semibold">Ubicación</th>
                <th className="px-4 py-3 font-semibold">Fecha</th>
                <th className="px-4 py-3 font-semibold">Estado</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s) => (<tr key={s.id} className="border-t border-border hover:bg-secondary/30">
                  <td className="px-4 py-3 font-semibold flex items-center gap-2"><span className="text-xl">🐾</span>{s.ngoName}</td>
                  <td className="px-4 py-3 text-muted-foreground">{s.email}</td>
                  <td className="px-4 py-3 text-muted-foreground">{s.location ?? "—"}</td>
                  <td className="px-4 py-3 text-muted-foreground">{new Date(s.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3"><StatusBadge value={STATUS_LABELS[s.status]}/></td>
                  <td className="px-4 py-3 text-right">
                    <Button asChild size="sm" variant="soft"><Link to={`/admin/solicitudes-ong/${s.id}`}><Eye className="w-3.5 h-3.5"/> Ver detalle</Link></Button>
                  </td>
                </tr>))}
              {!loading && filtered.length === 0 && (<tr><td colSpan={6} className="px-4 py-10 text-center text-muted-foreground">No hay solicitudes para este filtro.</td></tr>)}
              {loading && (<tr><td colSpan={6} className="px-4 py-10 text-center text-muted-foreground">Cargando...</td></tr>)}
            </tbody>
          </table>
        </div>
      </div>
    </div>);
};
export default AdminSolicitudes;
