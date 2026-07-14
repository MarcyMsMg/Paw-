import { useEffect, useState } from "react";
import { adminApi } from "@/services/authApi";
import { toast } from "@/hooks/use-toast";
const AdminUsuarios = () => {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        adminApi
            .listUsers("NATURAL_PERSON")
            .then(setItems)
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    }, []);
    return (<div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Administración</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Personas registradas</h1>
      </header>

      <div className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-secondary/50">
            <tr className="text-left">
              <th className="px-4 py-3 font-semibold">Nombre</th>
              <th className="px-4 py-3 font-semibold">Correo</th>
              <th className="px-4 py-3 font-semibold">Registrado</th>
            </tr>
          </thead>
          <tbody>
            {items.map((p) => (<tr key={p.id} className="border-t border-border hover:bg-secondary/30">
                <td className="px-4 py-3 font-semibold">{[p.firstName, p.lastName].filter(Boolean).join(" ") || "—"}</td>
                <td className="px-4 py-3 text-muted-foreground">{p.email}</td>
                <td className="px-4 py-3 text-muted-foreground">{new Date(p.createdAt).toLocaleDateString()}</td>
              </tr>))}
            {!loading && items.length === 0 && <tr><td colSpan={3} className="px-4 py-10 text-center text-muted-foreground">Sin usuarios.</td></tr>}
            {loading && <tr><td colSpan={3} className="px-4 py-10 text-center text-muted-foreground">Cargando...</td></tr>}
          </tbody>
        </table>
      </div>
    </div>);
};
export default AdminUsuarios;
