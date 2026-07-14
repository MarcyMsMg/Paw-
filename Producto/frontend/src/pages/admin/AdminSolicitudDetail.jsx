import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Check, X, MapPin, Calendar, Heart, Users, FileText, ExternalLink } from "lucide-react";
import { adminApi } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";
import { validateText } from "@/lib/validators";
const STATUS_LABELS = {
    PENDING: "PENDIENTE",
    APPROVED: "APROBADA",
    REJECTED: "RECHAZADA",
};
const AdminSolicitudDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [s, setS] = useState();
    const [loading, setLoading] = useState(true);
    const [showRechazo, setShowRechazo] = useState(false);
    const [motivo, setMotivo] = useState("");
    const [submitting, setSubmitting] = useState(false);
    useEffect(() => {
        if (!id)
            return;
        adminApi
            .listNgoRequests()
            .then((all) => setS(all.find((r) => r.id === id)))
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    }, [id]);
    if (loading)
        return <p className="text-center py-20 text-muted-foreground">Cargando...</p>;
    if (!s)
        return <p className="text-center py-20 text-muted-foreground">Solicitud no encontrada.</p>;
    const abrirActa = async () => {
        if (!s.constitutionActUrl) {
            toast({ title: "Esta solicitud no tiene acta registrada", variant: "destructive" });
            return;
        }
        try {
            if (s.constitutionActUrl.startsWith("data:")) {
                const tab = window.open("", "_blank");
                if (!tab) {
                    toast({ title: "El navegador bloqueo la ventana del documento", variant: "destructive" });
                    return;
                }
                const response = await fetch(s.constitutionActUrl);
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                tab.location.href = url;
                setTimeout(() => URL.revokeObjectURL(url), 60000);
                return;
            }
            window.open(s.constitutionActUrl, "_blank", "noopener,noreferrer");
        } catch (error) {
            toast({ title: "No se pudo abrir el acta", description: error.message, variant: "destructive" });
        }
    };
    const aprobar = async () => {
        setSubmitting(true);
        try {
            await adminApi.approveNgoRequest(s.id);
            toast({ title: "Solicitud aprobada", description: "La ONG ya puede iniciar sesion." });
            navigate("/admin/solicitudes-ong");
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setSubmitting(false);
        }
    };
    const rechazar = async () => {
        const errors = validateText(motivo, { required: true, min: 5, max: 500, label: "El motivo de rechazo" });
        if (errors) {
            toast({ title: errors, variant: "destructive" });
            return;
        }
        setSubmitting(true);
        try {
            await adminApi.rejectNgoRequest(s.id, motivo.trim());
            toast({ title: "Rechazo registrado", description: "Se notificara a la ONG." });
            navigate("/admin/solicitudes-ong");
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setSubmitting(false);
        }
    };
    return (<div className="space-y-6">
      <Button asChild variant="ghost" size="sm"><Link to="/admin/solicitudes-ong"><ArrowLeft className="w-4 h-4"/> Volver</Link></Button>

      <div className="bg-card rounded-3xl border border-border shadow-card overflow-hidden">
        <div className="h-48 relative bg-gradient-to-br from-primary/30 via-primary/10 to-background">
          <div className="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent"/>
          <div className="absolute bottom-4 left-6 flex items-end gap-4">
            <div className="w-16 h-16 rounded-2xl bg-card grid place-items-center text-3xl shadow-card border-4 border-card">P+</div>
            <div className="text-primary-foreground pb-2 drop-shadow">
              <h1 className="font-display text-2xl md:text-3xl font-bold">{s.ngoName}</h1>
              <p className="text-sm opacity-90">{s.email}</p>
            </div>
          </div>
          <div className="absolute top-4 right-4"><StatusBadge value={STATUS_LABELS[s.status]}/></div>
        </div>

        <div className="p-6 md:p-8 space-y-6">
          <div className="grid sm:grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div className="flex items-center gap-2"><MapPin className="w-4 h-4 text-primary"/> {s.location ?? "-"}</div>
            <div className="flex items-center gap-2"><Calendar className="w-4 h-4 text-primary"/> Fundada {s.foundationYear ?? "-"}</div>
            <div className="flex items-center gap-2"><Heart className="w-4 h-4 text-primary"/> {s.rescuedAnimalsCount ?? 0} rescatados</div>
            <div className="flex items-center gap-2"><Users className="w-4 h-4 text-primary"/> {s.volunteersCount ?? 0} voluntarios</div>
          </div>

          {s.description && (<div>
              <h3 className="font-semibold mb-2">Descripcion</h3>
              <p className="text-muted-foreground leading-relaxed">{s.description}</p>
            </div>)}

          <div className="rounded-xl border border-border bg-secondary/30 p-4 space-y-3">
            <div className="flex items-start gap-3">
              <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary"><FileText className="h-4 w-4" /></span>
              <div>
                <h3 className="font-semibold">Acta de constituci&oacute;n</h3>
                <p className="text-sm text-muted-foreground">Documento obligatorio adjunto por la ONG para la revision administrativa.</p>
              </div>
            </div>
            <Button type="button" variant="outline" size="sm" onClick={abrirActa} disabled={!s.constitutionActUrl}>
              <FileText className="w-4 h-4" /> Revisar acta <ExternalLink className="w-3.5 h-3.5" />
            </Button>
            {!s.constitutionActUrl && <p className="text-xs text-muted-foreground">Esta solicitud fue creada antes de exigir el acta.</p>}
          </div>

          {s.rejectionReason && (<div className="p-4 rounded-xl bg-red-50 border border-red-200">
              <h3 className="font-semibold text-red-800 mb-1">Motivo de rechazo</h3>
              <p className="text-sm text-red-700">{s.rejectionReason}</p>
            </div>)}

          {s.status === "PENDING" && (<div className="pt-4 border-t border-border space-y-4">
              {!showRechazo ? (<div className="flex flex-wrap gap-3">
                  <Button variant="hero" size="lg" onClick={aprobar} disabled={submitting}><Check className="w-4 h-4"/> Aprobar solicitud</Button>
                  <Button variant="outline" size="lg" onClick={() => setShowRechazo(true)} disabled={submitting}><X className="w-4 h-4"/> Rechazar solicitud</Button>
                </div>) : (<div className="space-y-3">
                  <Textarea placeholder="Motivo de rechazo..." rows={4} minLength={5} maxLength={500} value={motivo} onChange={(e) => setMotivo(e.target.value)} className="rounded-xl"/>
                  <div className="flex gap-3">
                    <Button variant="destructive" onClick={rechazar} disabled={submitting}>Confirmar rechazo</Button>
                    <Button variant="ghost" onClick={() => setShowRechazo(false)} disabled={submitting}>Cancelar</Button>
                  </div>
                </div>)}
            </div>)}
        </div>
      </div>
    </div>);
};
export default AdminSolicitudDetail;