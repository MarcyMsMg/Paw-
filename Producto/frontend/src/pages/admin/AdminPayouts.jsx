import { useEffect, useState } from "react";
import { adminPayoutsApi, payoutAccountsApi } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { withPaymentStatus } from "@/lib/payoutStatus";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { formatCurrency } from "@/lib/format";
import { toast } from "@/hooks/use-toast";

const nombre = (b) => b?.ngoName || `ONG ${String(b?.ngoId ?? "").slice(0, 8)}`;

const AdminPayouts = () => {
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);

  // Diálogo "datos bancarios"
  const [bankOpen, setBankOpen] = useState(false);
  const [bankData, setBankData] = useState(null);
  const [bankLoading, setBankLoading] = useState(false);
  const [bankNgoName, setBankNgoName] = useState("");

  // Diálogo "registrar transferencia"
  const [payOpen, setPayOpen] = useState(false);
  const [payNgo, setPayNgo] = useState(null);
  const [amount, setAmount] = useState("");
  const [reference, setReference] = useState("");
  const [note, setNote] = useState("");
  const [saving, setSaving] = useState(false);

  const load = () => {
    setLoading(true);
    adminPayoutsApi
      .getBalances()
      .then((list) => setBalances((list ?? []).filter((b) => Number(b.balanceOwed) > 0)))
      .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const verBanco = async (b) => {
    setBankNgoName(nombre(b));
    setBankData(null);
    setBankLoading(true);
    setBankOpen(true);
    try {
      setBankData(await payoutAccountsApi.getByNgo(b.ngoId));
    } catch {
      setBankData(null); // 404 = la ONG aún no cargó sus datos
    } finally {
      setBankLoading(false);
    }
  };

  const abrirTransferencia = (b) => {
    setPayNgo(b);
    setAmount(String(b.balanceOwed));
    setReference("");
    setNote("");
    setPayOpen(true);
  };

  const registrar = async () => {
    const monto = Number(amount);
    if (!(monto > 0)) {
      toast({ title: "Ingresa un monto válido", variant: "destructive" });
      return;
    }
    if (monto > Number(payNgo.balanceOwed)) {
      toast({ title: "El monto supera el saldo pendiente de la ONG", variant: "destructive" });
      return;
    }
    setSaving(true);
    try {
      await adminPayoutsApi.createPayout({
        ngoId: payNgo.ngoId,
        amount: monto,
        reference: reference || undefined,
        note: note || undefined,
      });
      toast({ title: "Transferencia registrada 🐾" });
      setPayOpen(false);
      load();
    } catch (err) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Administración</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Transferencias a ONGs</h1>
        <p className="text-muted-foreground mt-1 text-sm">
          Fondos liberados de campañas finalizadas que todavía no se transfieren a su ONG.
          El estado por campaña se estima según el total ya transferido a la ONG.
        </p>
      </header>

      <div className="space-y-4">
        {balances.map((b) => (
          <article key={b.ngoId} className="bg-card rounded-2xl border border-border shadow-soft p-5">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="min-w-0">
                <h3 className="font-display font-bold text-lg truncate">{nombre(b)}</h3>
                <p className="text-xs text-muted-foreground mt-1">
                  Recaudado liberable: {formatCurrency(Number(b.totalDonated))} · Ya transferido: {formatCurrency(Number(b.totalPaidOut))}
                </p>
              </div>
              <div className="text-right">
                <p className="text-xs text-muted-foreground">Pendiente de transferir</p>
                <p className="font-display text-2xl font-bold text-primary">{formatCurrency(Number(b.balanceOwed))}</p>
              </div>
            </div>

            {b.campaigns?.length > 0 && (
              <div className="mt-4 rounded-xl bg-secondary/40 border border-border divide-y divide-border">
                {withPaymentStatus(b.campaigns, b.totalPaidOut).map((c) => (
                  <div key={c.campaignId} className="flex items-center justify-between gap-3 px-3 py-2 text-sm">
                    <span className="truncate text-muted-foreground">{c.campaignTitle || "Campaña"}</span>
                    <div className="flex items-center gap-3 shrink-0">
                      <StatusBadge value={c.status} />
                      <span className="font-medium">{formatCurrency(Number(c.amount))}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="mt-4 flex flex-wrap gap-3">
              <Button variant="hero" size="sm" onClick={() => abrirTransferencia(b)}>Registrar transferencia</Button>
              <Button variant="outline" size="sm" onClick={() => verBanco(b)}>Ver datos bancarios</Button>
            </div>
          </article>
        ))}

        {!loading && balances.length === 0 && (
          <div className="bg-card rounded-2xl border border-border shadow-soft p-10 text-center text-muted-foreground">
            No hay transferencias pendientes 🎉
          </div>
        )}
        {loading && <p className="text-muted-foreground">Cargando...</p>}
      </div>

      {/* Datos bancarios de la ONG */}
      <Dialog open={bankOpen} onOpenChange={setBankOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Datos de transferencia</DialogTitle>
            <DialogDescription>{bankNgoName}</DialogDescription>
          </DialogHeader>
          {bankLoading ? (
            <p className="text-sm text-muted-foreground">Cargando...</p>
          ) : bankData ? (
            <div className="space-y-2 text-sm">
              <Row label="Titular" value={bankData.holderName} />
              <Row label="RUT" value={bankData.rut} />
              <Row label="Banco" value={bankData.bankName} />
              <Row label="Tipo de cuenta" value={bankData.accountType} />
              <Row label="N° de cuenta" value={bankData.accountNumber} />
              <Row label="Email" value={bankData.email} />
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">Esta ONG todavía no cargó sus datos de transferencia.</p>
          )}
        </DialogContent>
      </Dialog>

      {/* Registrar una transferencia ya realizada */}
      <Dialog open={payOpen} onOpenChange={setPayOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Registrar transferencia</DialogTitle>
            <DialogDescription>
              {payNgo && `${nombre(payNgo)} · Pendiente: ${formatCurrency(Number(payNgo.balanceOwed))}`}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1">
              <Label>Monto transferido</Label>
              <Input type="number" min={1} value={amount} onChange={(e) => setAmount(e.target.value)} className="h-11 rounded-xl" />
            </div>
            <div className="space-y-1">
              <Label>Referencia / N° de comprobante (opcional)</Label>
              <Input value={reference} onChange={(e) => setReference(e.target.value)} className="h-11 rounded-xl" />
            </div>
            <div className="space-y-1">
              <Label>Nota (opcional)</Label>
              <Textarea rows={3} value={note} onChange={(e) => setNote(e.target.value)} className="rounded-xl" />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setPayOpen(false)} disabled={saving}>Cancelar</Button>
            <Button variant="hero" onClick={registrar} disabled={saving}>{saving ? "Registrando..." : "Registrar"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

function Row({ label, value }) {
  return (
    <div className="flex justify-between gap-4 border-b border-border py-2 last:border-0">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium text-right">{value || "—"}</span>
    </div>
  );
}

export default AdminPayouts;
