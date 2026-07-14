import { useEffect, useState } from "react";
import { adoptionsApi, applicationToView } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { StatusBadge } from "@/components/StatusBadge";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";

const OngSolicitudesAdopcion = () => {
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(null);
  const [decision, setDecision] = useState(null);
  const [ngoResponse, setNgoResponse] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const reload = () => adoptionsApi
    .listApplications()
    .then((applications) => setItems(applications.map(applicationToView)));

  useEffect(() => { reload().catch(showError); }, []);

  const openDecision = (application, status) => {
    setDecision({ application, status });
    setNgoResponse("");
  };

  const closeDecision = () => {
    if (submitting) return;
    setDecision(null);
    setNgoResponse("");
  };

  const decide = async () => {
    if (!decision) return;
    if (decision.status === "INFO_REQUESTED" && !ngoResponse.trim()) return;

    setSubmitting(true);
    try {
      await adoptionsApi.decideApplication(decision.application.id, {
        status: decision.status,
        ngoResponse: ngoResponse.trim() || null,
      });
      toast({ title: decisionToast(decision.status) });
      setDecision(null);
      setNgoResponse("");
      await reload();
    } catch (error) {
      showError(error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Mi ONG</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Solicitudes de adopcion</h1>
      </header>

      <div className="space-y-3">
        {items.map((application) => (
          <article key={application.id} className="bg-card rounded-lg border border-border overflow-hidden">
            <div className="p-5 flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-3 flex-1 min-w-[220px]">
                <div className="w-12 h-12 rounded-md bg-secondary overflow-hidden shrink-0">
                  {application.animalPhotoUrl && <img src={application.animalPhotoUrl} alt={application.animalName} className="w-full h-full object-cover" />}
                </div>
                <div>
                  <p className="font-semibold">{application.nombreCompleto}</p>
                  <p className="text-xs text-muted-foreground">Animal: <strong>{application.animalName}</strong> - {new Date(application.fecha).toLocaleDateString()}</p>
                </div>
              </div>
              <StatusBadge value={application.estado} />
              <div className="flex flex-wrap gap-2">
                <Button size="sm" variant="soft" onClick={() => setOpen(open === application.id ? null : application.id)}>
                  {open === application.id ? "Ocultar" : "Ver detalle"}
                </Button>
                {["PENDIENTE", "INFORMACION_SOLICITADA"].includes(application.estado) && (
                  <>
                    <Button size="sm" variant="outline" onClick={() => openDecision(application, "INFO_REQUESTED")}>Pedir informacion</Button>
                    <Button size="sm" variant="hero" onClick={() => openDecision(application, "ACCEPTED")}>Aceptar</Button>
                    <Button size="sm" variant="outline" onClick={() => openDecision(application, "REJECTED")}>Rechazar</Button>
                  </>
                )}
              </div>
            </div>

            {open === application.id && (
              <div className="px-5 pb-5 border-t border-border pt-4 space-y-5">
                <div className="grid sm:grid-cols-2 gap-3 text-sm">
                  <p><strong>Email:</strong> {application.email}</p>
                  <p><strong>Telefono:</strong> {application.telefono}</p>
                  <p><strong>Direccion:</strong> {application.direccion}</p>
                  <p><strong>Vivienda:</strong> {application.tipoVivienda}</p>
                  <p><strong>Otros animales:</strong> {application.otrosAnimales}</p>
                  <p><strong>Disponibilidad:</strong> {application.disponibilidad || "No indicada"}</p>
                  <p className="sm:col-span-2"><strong>Motivacion:</strong> {application.motivo}</p>
                  <p className="sm:col-span-2"><strong>Experiencia previa:</strong> {application.experienciaPrevia || "No indicada"}</p>
                </div>
                {application.respuestas.length > 0 && (
                  <div>
                    <h3 className="font-semibold mb-3">Respuestas personalizadas</h3>
                    <dl className="grid sm:grid-cols-2 gap-3">
                      {application.respuestas.map((answer) => (
                        <div key={answer.key} className="bg-secondary/50 rounded-md p-3">
                          <dt className="text-xs font-semibold text-muted-foreground">{answer.label}</dt>
                          <dd className="text-sm mt-1 whitespace-pre-line">{answer.value}</dd>
                        </div>
                      ))}
                    </dl>
                  </div>
                )}
                {application.ngoResponse && (
                  <div className="bg-primary/5 border border-primary/20 rounded-md p-3 text-sm">
                    <strong>Respuesta de la ONG:</strong> {application.ngoResponse}
                  </div>
                )}
              </div>
            )}
          </article>
        ))}
        {items.length === 0 && <p className="text-muted-foreground">Aun no recibes postulaciones.</p>}
      </div>

      <Dialog open={Boolean(decision)} onOpenChange={(isOpen) => !isOpen && closeDecision()}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{decisionTitle(decision?.status)}</DialogTitle>
            <DialogDescription>
              {decision?.application ? `Postulacion de ${decision.application.nombreCompleto} para ${decision.application.animalName}.` : ""}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <label className="text-sm font-medium">{decisionLabel(decision?.status)}</label>
            <Textarea
              rows={4}
              value={ngoResponse}
              onChange={(event) => setNgoResponse(event.target.value)}
              placeholder={decisionPlaceholder(decision?.status)}
            />
            {decision?.status === "INFO_REQUESTED" && (
              <p className="text-xs text-muted-foreground">Este mensaje es obligatorio para solicitar mas informacion.</p>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={closeDecision} disabled={submitting}>Cancelar</Button>
            <Button
              type="button"
              variant={decision?.status === "REJECTED" ? "outline" : "hero"}
              onClick={decide}
              disabled={submitting || (decision?.status === "INFO_REQUESTED" && !ngoResponse.trim())}
            >
              {submitting ? "Guardando..." : decisionAction(decision?.status)}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

function decisionTitle(status) {
  if (status === "ACCEPTED") return "Aceptar postulacion";
  if (status === "REJECTED") return "Rechazar postulacion";
  return "Solicitar informacion";
}

function decisionLabel(status) {
  if (status === "ACCEPTED") return "Mensaje para la persona";
  if (status === "REJECTED") return "Motivo del rechazo";
  return "Informacion requerida";
}

function decisionPlaceholder(status) {
  if (status === "ACCEPTED") return "Ej. Nos pondremos en contacto para coordinar los proximos pasos.";
  if (status === "REJECTED") return "Ej. Motivo opcional para orientar a la persona.";
  return "Ej. Necesitamos confirmar horarios, espacio disponible o experiencia previa.";
}

function decisionAction(status) {
  if (status === "ACCEPTED") return "Aceptar";
  if (status === "REJECTED") return "Rechazar";
  return "Enviar solicitud";
}

function decisionToast(status) {
  if (status === "ACCEPTED") return "Postulacion aceptada";
  if (status === "REJECTED") return "Postulacion rechazada";
  return "Informacion solicitada";
}

function showError(error) {
  toast({ title: "No fue posible completar la operacion", description: error?.message, variant: "destructive" });
}

export default OngSolicitudesAdopcion;
