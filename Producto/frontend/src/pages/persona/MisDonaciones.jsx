import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { donationsApi, donationToView, campaignsApi } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { formatCurrency } from "@/lib/format";
import { toast } from "@/hooks/use-toast";

const MisDonaciones = () => {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [items, setItems] = useState([]);
  const [camps, setCamps] = useState({});
  const [loading, setLoading] = useState(true);
  const [receipt, setReceipt] = useState(null);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!user) return;

    const loadDonations = async () => {
      const ds = await donationsApi.listByDonor(user.id);
      // Más reciente arriba: ordenamos por fecha (pago o creación) descendente.
      const views = ds
        .map(donationToView)
        .sort((a, b) => new Date(b.fecha ?? 0) - new Date(a.fecha ?? 0));
      setItems(views);
      const uniqueIds = [...new Set(views.map((d) => d.campanaId).filter(Boolean))];
      const pairs = await Promise.all(
        uniqueIds.map((cid) =>
          campaignsApi.getById(cid).then((c) => [cid, c.title]).catch(() => [cid, "—"])
        )
      );
      setCamps(Object.fromEntries(pairs));
    };

    const run = async () => {
      // Al volver de MercadoPago, la back_url trae el payment_id: confirmamos el pago
      // automáticamente (sin depender de que el webhook llegue al backend).
      const paymentId = searchParams.get("payment_id") || searchParams.get("collection_id");
      const status = searchParams.get("status") || searchParams.get("collection_status");
      if (paymentId) {
        try {
          await donationsApi.syncPayment(paymentId);
        } catch {
          // el pago puede estar pendiente o no consultable aún; igual cargamos la lista
        }
        if (status === "approved") {
          toast({ title: "¡Gracias por tu donación! 🐾", description: "Tu pago fue confirmado." });
        } else if (status === "pending" || status === "in_process") {
          toast({ title: "Pago pendiente", description: "Tu donación quedó pendiente de confirmación." });
        }
        setSearchParams({}, { replace: true }); // limpiar la URL para no re-disparar
      }

      try {
        await loadDonations();
      } catch (err) {
        toast({ title: "Error", description: err.message, variant: "destructive" });
      } finally {
        setLoading(false);
      }
    };

    run();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const verComprobante = async (donationId) => {
    try {
      const r = await donationsApi.getReceipt(donationId);
      setReceipt(r);
      setOpen(true);
    } catch (err) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Mi cuenta</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Mis donaciones</h1>
      </header>

      <div className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-secondary/50"><tr className="text-left">
            <th className="px-4 py-3 font-semibold">Monto</th>
            <th className="px-4 py-3 font-semibold">Campaña</th>
            <th className="px-4 py-3 font-semibold">Método</th>
            <th className="px-4 py-3 font-semibold">Fecha</th>
            <th className="px-4 py-3 font-semibold">Estado</th>
            <th className="px-4 py-3 font-semibold">Comprobante</th>
          </tr></thead>
          <tbody>
            {items.map((d) => (
              <tr key={d.id} className="border-t border-border hover:bg-secondary/30">
                <td className="px-4 py-3 font-bold text-primary">{formatCurrency(d.monto)}</td>
                <td className="px-4 py-3">{camps[d.campanaId] || "—"}</td>
                <td className="px-4 py-3 text-muted-foreground">{d.metodoPago}</td>
                <td className="px-4 py-3 text-muted-foreground">{d.fecha ? new Date(d.fecha).toLocaleDateString() : "—"}</td>
                <td className="px-4 py-3"><StatusBadge value={d.estado} /></td>
                <td className="px-4 py-3">
                  {d.status === "APPROVED" ? (
                    <Button variant="soft" size="sm" onClick={() => verComprobante(d.id)}>Ver</Button>
                  ) : (
                    <span className="text-muted-foreground">—</span>
                  )}
                </td>
              </tr>
            ))}
            {!loading && items.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-10 text-center text-muted-foreground">Aún no has donado.</td></tr>
            )}
            {loading && (
              <tr><td colSpan={6} className="px-4 py-10 text-center text-muted-foreground">Cargando...</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Comprobante de donación</DialogTitle>
            <DialogDescription>{receipt?.receiptNumber}</DialogDescription>
          </DialogHeader>
          {receipt && (
            <div className="space-y-2 text-sm">
              <Row label="Donante" value={receipt.donorName || receipt.donorEmail || "—"} />
              <Row label="Campaña" value={receipt.campaignTitle || "—"} />
              <Row label="ONG" value={receipt.ngoName || "—"} />
              <Row label="Monto" value={formatCurrency(Number(receipt.amount ?? 0))} />
              <Row label="Método de pago" value={receipt.paymentMethod || "—"} />
              <Row label="Fecha" value={receipt.paidAt ? new Date(receipt.paidAt).toLocaleString() : "—"} />
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

function Row({ label, value }) {
  return (
    <div className="flex justify-between gap-4 border-b border-border py-2 last:border-0">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium text-right">{value}</span>
    </div>
  );
}

export default MisDonaciones;
