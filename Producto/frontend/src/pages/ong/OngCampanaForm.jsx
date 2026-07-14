import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { campaignsApi } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
} from "@/components/ui/alert-dialog";
import { toast } from "@/hooks/use-toast";
import { formatCurrency } from "@/lib/format";
import { validateText, validateDateRange, isPositiveNumber, isValidImageUrl, isValidYouTubeUrl, isValidInternalOrHttpUrl } from "@/lib/validators";

// Comisión referencial de MercadoPago (aproximada) para estimar el monto líquido a
// recibir. Es solo una estimación informativa; el descuento real lo aplica MercadoPago.
const MP_COMMISSION_RATE = 0.0349;

const empty = {
  titulo: "",
  descripcion: "",
  banner: "",
  meta: 1000000,
  categoria: "Emergencia médica",
  video: "",
  fechaInicio: "",
  fechaFin: "",
};

const OngCampanaForm = () => {
  const { user } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();
  const [c, setC] = useState(empty);
  const [saving, setSaving] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  useEffect(() => {
    if (id)
      campaignsApi.getById(id).then((r) =>
        setC({
          titulo: r.title,
          descripcion: r.description,
          banner: r.bannerUrl ?? "",
          meta: Number(r.goalAmount),
          categoria: r.category ?? "",
          video: r.videoUrl ?? "",
          fechaInicio: r.startDate ?? "",
          fechaFin: r.endDate ?? "",
        })
      );
  }, [id]);

  const set = (k, v) => setC((p) => ({ ...p, [k]: v }));

  // Valida y decide: al editar guarda directo; al crear abre la confirmación
  // (porque el monto no se podrá cambiar luego).
  const save = (e) => {
    e.preventDefault();
    if (!user) return;
    const errors = [
      validateText(c.titulo, { required: true, min: 3, max: 120, label: "El título" }),
      validateText(c.descripcion, { required: true, min: 20, max: 2000, label: "La descripción" }),
      validateText(c.categoria, { max: 80, label: "La categoría" }),
      !id && !isPositiveNumber(c.meta) ? "La meta económica debe ser mayor a 0" : null,
      !id && isPositiveNumber(c.meta) && Number(c.meta) < 1000 ? "La meta económica mínima es $1.000" : null,
      validateDateRange(c.fechaInicio, c.fechaFin),
      c.banner.trim() && !isValidImageUrl(c.banner) ? "El banner debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
      c.video.trim() && !(isValidYouTubeUrl(c.video) || isValidInternalOrHttpUrl(c.video)) ? "El video debe ser un link de YouTube o una URL http/https válida" : null,
    ].find(Boolean);
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    if (id) {
      doSubmit();
    } else {
      setConfirmOpen(true);
    }
  };

  const doSubmit = async () => {
    setSaving(true);
    try {
      const payload = {
        title: c.titulo.trim(),
        description: c.descripcion.trim(),
        bannerUrl: c.banner || undefined,
        videoUrl: c.video || undefined,
        category: c.categoria || undefined,
        startDate: c.fechaInicio,
        endDate: c.fechaFin,
      };
      if (id) {
        // En edición NO se envía goalAmount: el monto es inmutable.
        await campaignsApi.update(id, payload);
      } else {
        await campaignsApi.create({ ...payload, goalAmount: Number(c.meta), ngoId: user.id });
      }
      toast({ title: id ? "Campaña actualizada" : "Campaña creada 🐾" });
      navigate("/ong/campanas");
    } catch (err) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    } finally {
      setSaving(false);
      setConfirmOpen(false);
    }
  };

  return (
    <div className="space-y-6">
      <Button asChild variant="ghost" size="sm"><Link to="/ong/campanas"><ArrowLeft className="w-4 h-4" /> Volver</Link></Button>
      <h1 className="font-display text-3xl font-bold">{id ? "Editar campaña" : "Nueva campaña"}</h1>

      <form onSubmit={save} className="bg-card rounded-3xl border border-border shadow-soft p-6 md:p-8 space-y-4">
        <div className="space-y-2"><Label>Título</Label><Input required minLength={3} maxLength={120} value={c.titulo} onChange={(e) => set("titulo", e.target.value)} className="h-11 rounded-xl" /></div>
        <div className="space-y-2"><Label>Descripción del caso</Label><Textarea rows={5} required minLength={20} maxLength={2000} value={c.descripcion} onChange={(e) => set("descripcion", e.target.value)} className="rounded-xl" /></div>
        <div className="grid sm:grid-cols-2 gap-4">
          <div className="space-y-2"><Label>Banner promocional (URL)</Label><Input value={c.banner} onChange={(e) => set("banner", e.target.value)} placeholder="https://..." className="h-11 rounded-xl" /></div>
          <div className="space-y-2"><Label>Categoría</Label><Input maxLength={80} value={c.categoria} onChange={(e) => set("categoria", e.target.value)} className="h-11 rounded-xl" /></div>
          <div className="space-y-2 sm:col-span-2">
            <Label>Video de presentación (link de YouTube o URL, opcional)</Label>
            <Input value={c.video} onChange={(e) => set("video", e.target.value)} placeholder="https://www.youtube.com/watch?v=..." className="h-11 rounded-xl" />
            {c.video.trim() && !(isValidYouTubeUrl(c.video) || isValidInternalOrHttpUrl(c.video)) && (
              <p className="text-xs text-destructive">No parece un link válido.</p>
            )}
          </div>
          <div className="space-y-2 sm:col-span-2">
            <Label>Meta económica</Label>
            <Input
              type="number"
              required={!id}
              min={1000}
              value={c.meta}
              onChange={(e) => set("meta", e.target.value)}
              disabled={!!id}
              className={`h-11 rounded-xl ${id ? "bg-secondary/40" : ""}`}
            />
            <p className="text-xs text-muted-foreground">
              {id
                ? "El monto a recaudar no se puede modificar."
                : "Una vez creada la campaña, este monto no podrá cambiarse."}
            </p>
            {Number(c.meta) > 0 && (
              <div className="rounded-xl bg-secondary/40 border border-border p-3 text-xs space-y-1">
                <div className="flex justify-between gap-3">
                  <span className="text-muted-foreground">Comisión estimada de MercadoPago (~{(MP_COMMISSION_RATE * 100).toFixed(2)}%)</span>
                  <span className="text-destructive">−{formatCurrency(Math.round(Number(c.meta) * MP_COMMISSION_RATE))}</span>
                </div>
                <div className="flex justify-between gap-3 font-semibold pt-1 border-t border-border">
                  <span>Monto total a recibir (aproximado)</span>
                  <span className="text-primary">{formatCurrency(Math.round(Number(c.meta) * (1 - MP_COMMISSION_RATE)))}</span>
                </div>
              </div>
            )}
          </div>
          <div className="space-y-2"><Label>Fecha de inicio</Label><Input type="date" required value={c.fechaInicio} onChange={(e) => set("fechaInicio", e.target.value)} className="h-11 rounded-xl" /></div>
          <div className="space-y-2"><Label>Fecha de finalización</Label><Input type="date" required value={c.fechaFin} onChange={(e) => set("fechaFin", e.target.value)} className="h-11 rounded-xl" /></div>
        </div>
        <p className="text-xs text-muted-foreground">La campaña solo será visible públicamente entre la fecha de inicio y la de finalización.</p>
        <div className="flex gap-3">
          <Button type="submit" variant="hero" size="lg" disabled={saving}>{saving ? "Guardando..." : id ? "Guardar" : "Crear campaña"}</Button>
          <Button type="button" variant="outline" size="lg" onClick={() => navigate("/ong/campanas")}>Cancelar</Button>
        </div>
      </form>

      <AlertDialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Crear esta campaña?</AlertDialogTitle>
            <AlertDialogDescription>
              El monto a recaudar (<strong>{formatCurrency(Number(c.meta))}</strong>) <strong>no se podrá modificar</strong> después
              de crear la campaña. El resto de los datos sí podrás editarlos más adelante.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={saving}>Cancelar</AlertDialogCancel>
            <Button variant="hero" onClick={doSubmit} disabled={saving}>
              {saving ? "Creando..." : "Confirmar y crear"}
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default OngCampanaForm;
