import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { donationsApi, donationToView, campaignsApi } from "@/services/authApi";
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionContent,
} from "@/components/ui/accordion";
import { formatCurrency } from "@/lib/format";
import { toast } from "@/hooks/use-toast";

const STATUS_STYLES = {
  "Pagada": "bg-emerald-100 text-emerald-800 border-emerald-200",
  "Pendiente de transacción": "bg-amber-100 text-amber-800 border-amber-200",
  "En progreso": "bg-sky-100 text-sky-800 border-sky-200",
};

function CampaignBadge({ label }) {
  return (
    <span className={`inline-flex shrink-0 items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${STATUS_STYLES[label] || "bg-secondary text-secondary-foreground border-border"}`}>
      {label}
    </span>
  );
}

// Estado de la campaña visto por la ONG. Solo las COMPLETED liberan fondos, así que la
// distinción Pagada/Pendiente aplica a esas; el resto está "En progreso".
function statusLabel(campaignStatus, coverage) {
  if (campaignStatus === "COMPLETED") {
    return coverage === "PAGADO" ? "Pagada" : "Pendiente de transacción";
  }
  return "En progreso";
}

const OngDonaciones = () => {
  const { user } = useAuth();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        const ds = await donationsApi.listByNgo(user.id);
        // Solo las donaciones aprobadas (las efectivamente recibidas).
        const approved = ds.map(donationToView).filter((d) => d.status === "APPROVED");

        // Agrupamos por campaña.
        const byCampaign = {};
        for (const d of approved) {
          if (!d.campanaId) continue;
          (byCampaign[d.campanaId] ||= []).push(d);
        }
        const campIds = Object.keys(byCampaign);

        const campEntries = await Promise.all(
          campIds.map((cid) =>
            campaignsApi.getById(cid).then((c) => [cid, c]).catch(() => [cid, null])
          )
        );
        const campMeta = Object.fromEntries(campEntries);

        const built = campIds
          .map((cid) => {
            const dons = byCampaign[cid].sort(
              (a, b) => new Date(b.fecha ?? 0) - new Date(a.fecha ?? 0)
            );
            const camp = campMeta[cid];
            return {
              campaignId: cid,
              titulo: camp?.title ?? "—",
              estado: statusLabel(camp?.status, null),
              donaciones: dons,
              total: dons.reduce((s, d) => s + Number(d.monto || 0), 0),
              ultima: dons[0]?.fecha,
            };
          })
          .sort((a, b) => new Date(b.ultima ?? 0) - new Date(a.ultima ?? 0));

        setGroups(built);
      } catch (err) {
        toast({ title: "Error", description: err.message, variant: "destructive" });
      } finally {
        setLoading(false);
      }
    })();
  }, [user]);

  return (
    <div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Mi ONG</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Donaciones recibidas</h1>
        <p className="text-muted-foreground mt-1 text-sm">
          Agrupadas por campaña. Despliega una campaña para ver sus donaciones aprobadas.
        </p>
      </header>

      {loading ? (
        <p className="text-muted-foreground">Cargando...</p>
      ) : groups.length === 0 ? (
        <div className="bg-card rounded-2xl border border-border shadow-soft p-10 text-center text-muted-foreground">
          Aún no has recibido donaciones.
        </div>
      ) : (
        <div className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
          <Accordion type="multiple">
            {groups.map((g) => (
              <AccordionItem key={g.campaignId} value={g.campaignId} className="px-4 last:border-b-0">
                <AccordionTrigger className="hover:no-underline">
                  <div className="flex flex-1 items-center justify-between gap-3 pr-3">
                    <div className="flex items-center gap-2 min-w-0">
                      <span className="truncate font-semibold text-left">{g.titulo}</span>
                      <CampaignBadge label={g.estado} />
                    </div>
                    <span className="font-bold text-primary shrink-0">{formatCurrency(g.total)}</span>
                  </div>
                </AccordionTrigger>
                <AccordionContent>
                  <div className="rounded-xl bg-secondary/40 border border-border divide-y divide-border mb-2">
                    {g.donaciones.map((d) => (
                      <div key={d.id} className="flex items-center justify-between gap-3 px-3 py-2 text-sm">
                        <span className="text-muted-foreground">
                          {d.fecha ? new Date(d.fecha).toLocaleString() : "—"}
                        </span>
                        <span className="font-medium">{formatCurrency(d.monto)}</span>
                      </div>
                    ))}
                  </div>
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </div>
      )}
    </div>
  );
};

export default OngDonaciones;
