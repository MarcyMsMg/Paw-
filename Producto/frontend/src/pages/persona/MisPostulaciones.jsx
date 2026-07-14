import { useEffect, useState } from "react";
import { adoptionsApi, applicationToView } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { toast } from "@/hooks/use-toast";

const MisPostulaciones = () => {
  const [items, setItems] = useState([]);
  useEffect(() => {
    adoptionsApi.listApplications()
      .then((applications) => setItems(applications.map(applicationToView)))
      .catch((error) => toast({ title: "No fue posible cargar tus postulaciones", description: error?.message, variant: "destructive" }));
  }, []);

  return <div className="space-y-6">
    <header><p className="text-sm font-semibold text-primary">Mi cuenta</p><h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Mis postulaciones</h1></header>
    <div className="grid sm:grid-cols-2 gap-4">
      {items.map((application) => <article key={application.id} className="bg-card rounded-lg border border-border p-4 flex gap-4">
        <div className="w-20 h-20 rounded-md bg-secondary overflow-hidden shrink-0">{application.animalPhotoUrl && <img src={application.animalPhotoUrl} alt={application.animalName} className="w-full h-full object-cover" />}</div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between gap-2"><p className="font-semibold">{application.animalName || "Animal"}</p><StatusBadge value={application.estado} /></div>
          <p className="text-xs text-muted-foreground">{new Date(application.fecha).toLocaleString()}</p>
          <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{application.motivo}</p>
          {application.ngoResponse && <p className="text-sm mt-2 border-t border-border pt-2"><strong>ONG:</strong> {application.ngoResponse}</p>}
        </div>
      </article>)}
      {items.length === 0 && <p className="text-muted-foreground col-span-full">Aún no tienes postulaciones.</p>}
    </div>
  </div>;
};

export default MisPostulaciones;
