import { useEffect, useState } from "react";
import { useParams, Link, useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft, Heart, Building2, ClipboardCheck } from "lucide-react";
import { adoptionsApi, animalToView, ngosApi, ngoToFundacion, isImageUrl } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { StatusBadge } from "@/components/StatusBadge";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "@/hooks/use-toast";
import { isValidChilePhone, isValidEmail, validateText } from "@/lib/validators";

const fallbackForm = { name: "Formulario de adopcion", fields: [] };

const AnimalDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [animal, setAnimal] = useState();
  const [ngo, setNgo] = useState();
  const [formDefinition, setFormDefinition] = useState(fallbackForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [checkingApplication, setCheckingApplication] = useState(false);
  const [existingApplication, setExistingApplication] = useState(null);
  const [customAnswers, setCustomAnswers] = useState({});
  const [form, setForm] = useState({
    fullName: user?.nombre || "",
    email: user?.email || "",
    phone: "",
    address: "",
    housingType: "Casa",
    otherAnimals: "No",
    motivation: "",
    availability: "",
    previousExperience: "",
  });

  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  useEffect(() => {
    if (!id) return;
    let alive = true;
    setLoading(true);
    setError("");

    adoptionsApi.getAnimal(id)
      .then(async (backendAnimal) => {
        if (!alive) return;
        const view = animalToView(backendAnimal);
        setAnimal(view);
        setNgo({ id: view.ongId, nombre: "ONG responsable", logo: "" });

        adoptionsApi.getAnimalForm(id)
          .then((definition) => alive && setFormDefinition(definition ?? fallbackForm))
          .catch((err) => {
            if (!alive) return;
            setFormDefinition(fallbackForm);
            toast({ title: "Formulario no disponible", description: err?.message, variant: "destructive" });
          });

        ngosApi.getById(view.ongId)
          .then((backendNgo) => alive && setNgo(ngoToFundacion(backendNgo)))
          .catch(() => alive && setNgo({ id: view.ongId, nombre: "ONG responsable", logo: "" }));
      })
      .catch((err) => {
        if (!alive) return;
        setError(err?.message || "No se pudo cargar el animal.");
      })
      .finally(() => alive && setLoading(false));

    return () => { alive = false; };
  }, [id]);

  useEffect(() => {
    if (!id || user?.role !== "PERSONA_NATURAL") {
      setExistingApplication(null);
      setCheckingApplication(false);
      return;
    }

    let alive = true;
    setCheckingApplication(true);

    adoptionsApi.listApplications()
      .then((applications = []) => {
        if (!alive) return;
        const application = applications.find((item) => item.animalId === id) ?? null;
        setExistingApplication(application);
      })
      .catch(() => alive && setExistingApplication(null))
      .finally(() => alive && setCheckingApplication(false));

    return () => { alive = false; };
  }, [id, user?.role]);

  if (loading) {
    return <p className="container py-20 text-center text-muted-foreground">Cargando animal...</p>;
  }

  if (error || !animal) {
    return <p className="container py-20 text-center text-destructive">{error || "Animal no encontrado."}</p>;
  }

  const handleApply = () => {
    if (!user) {
      navigate("/login", { state: { from: location.pathname } });
      return;
    }
    if (user.role !== "PERSONA_NATURAL") {
      toast({ title: "Solo personas naturales pueden postular", variant: "destructive" });
      return;
    }
    if (existingApplication) {
      toast({ title: "Ya postulaste a este animal", description: "Puedes revisar el estado desde Mis postulaciones." });
      return;
    }
    setShowForm(true);
  };

  const customFields = (formDefinition?.fields ?? []).filter((field) => !field.system);

  const submit = async (event) => {
    event.preventDefault();
    const errors = [
      validateText(form.fullName, { required: true, min: 3, max: 160, label: "El nombre completo" }),
      !isValidEmail(form.email) ? "Ingresa un email valido" : null,
      !isValidChilePhone(form.phone) ? "Ingresa un telefono chileno valido (ej: 912345678 o +56912345678)" : null,
      validateText(form.address, { required: true, min: 5, max: 220, label: "La direccion" }),
      validateText(form.motivation, { required: true, min: 20, max: 2000, label: "La motivacion" }),
      validateText(form.availability, { max: 500, label: "La disponibilidad" }),
      validateText(form.previousExperience, { max: 1000, label: "La experiencia previa" }),
    ].find(Boolean);
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    const missing = customFields.find((field) => field.required && !customAnswers[field.key]?.trim());
    if (missing) {
      toast({ title: "Falta una respuesta", description: missing.label, variant: "destructive" });
      return;
    }

    setSubmitting(true);
    try {
      await adoptionsApi.submitApplication(animal.id, { ...form, customAnswers });
      toast({ title: "Postulacion enviada", description: "La ONG revisara tu solicitud pronto." });
      navigate("/persona/mis-postulaciones");
    } catch (err) {
      toast({ title: "No fue posible enviar la postulacion", description: err?.message, variant: "destructive" });
    } finally {
      setSubmitting(false);
    }
  };

  const canApply = !user || user.role === "PERSONA_NATURAL";
  const acceptingApplications = ["DISPONIBLE", "EN_PROCESO"].includes(animal.estado);
  const safeNgo = ngo ?? { id: animal.ongId, nombre: "ONG responsable", logo: "" };

  return <section className="container py-12 md:py-16 space-y-7">
    <Button asChild variant="ghost" size="sm"><Link to="/animales"><ArrowLeft className="w-4 h-4" /> Volver</Link></Button>
    <div className="grid md:grid-cols-2 gap-7">
      <div className="rounded-lg overflow-hidden shadow-card border border-border">
        {animal.fotos[0] && <img src={animal.fotos[0]} alt={animal.nombre} className="w-full h-[420px] object-cover" />}
      </div>
      <div className="space-y-4">
        <div className="flex items-center gap-3"><h1 className="font-display text-4xl font-bold">{animal.nombre}</h1><StatusBadge value={animal.estado} /></div>
        <p className="text-muted-foreground">{[animal.especie, animal.edad, animal.sexo, animal.tamano].filter(Boolean).join(" - ")}</p>
        <p className="leading-relaxed">{animal.descripcion}</p>
        <div className="bg-secondary/50 rounded-lg p-4"><p className="text-sm font-semibold mb-1">Estado de salud</p><p className="text-sm text-muted-foreground">{animal.salud || "Sin informacion"}</p></div>
        {animal.requisitos && <div className="border border-border rounded-lg p-4"><p className="text-sm font-semibold mb-1">Requisitos de adopcion</p><p className="text-sm text-muted-foreground">{animal.requisitos}</p></div>}
        <Link to={`/fundacion/${safeNgo.id}`} className="flex items-center gap-3 p-4 bg-card border border-border rounded-lg hover:border-primary transition-smooth">
          <span className="w-10 h-10 grid place-items-center rounded-md bg-secondary text-sm font-semibold overflow-hidden shrink-0">
            {isImageUrl(safeNgo.logo) ? <img src={safeNgo.logo} alt={safeNgo.nombre} className="w-full h-full object-cover" /> : "P+"}
          </span>
          <div className="flex-1 min-w-0">
            <p className="text-xs text-muted-foreground">ONG responsable</p>
            <p className="font-semibold truncate">{safeNgo.nombre}</p>
          </div>
          <Building2 className="w-4 h-4 text-muted-foreground shrink-0" />
        </Link>
        {acceptingApplications && !showForm && canApply && (existingApplication ? <div className="rounded-lg border border-primary/20 bg-primary/5 p-4 space-y-3"><div className="flex items-start gap-3"><span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary"><ClipboardCheck className="h-4 w-4" /></span><div><p className="font-semibold">Ya postulaste a este animal</p><p className="text-sm text-muted-foreground">Tu solicitud esta registrada. Puedes revisar su estado cuando quieras.</p></div></div><Button asChild variant="outline" size="sm"><Link to="/persona/mis-postulaciones">Ver mis postulaciones</Link></Button></div> : <Button variant="hero" size="lg" className="w-full" onClick={handleApply} disabled={checkingApplication}><Heart className="w-4 h-4" /> {checkingApplication ? "Revisando postulaciones..." : user ? "Postular a adopcion" : "Inicia sesion para postular"}</Button>)}
      </div>
    </div>

    {showForm && <form onSubmit={submit} className="bg-card rounded-lg border border-border p-6 md:p-8 space-y-7">
      <div><p className="text-sm font-semibold text-primary">{formDefinition.name}</p><h2 className="font-display text-2xl font-bold mt-1">Formulario de adopcion</h2><p className="text-sm text-muted-foreground mt-1">Los campos marcados con * son obligatorios.</p></div>
      <div className="grid sm:grid-cols-2 gap-4">
        <Field label="Nombre completo *"><Input required minLength={3} maxLength={160} value={form.fullName} onChange={(e) => set("fullName", e.target.value)} /></Field>
        <Field label="Correo *"><Input type="email" required maxLength={160} value={form.email} onChange={(e) => set("email", e.target.value)} /></Field>
        <Field label="Telefono *"><Input type="tel" required maxLength={20} placeholder="912345678" value={form.phone} onChange={(e) => set("phone", e.target.value)} /></Field>
        <Field label="Direccion o comuna *"><Input required minLength={5} maxLength={220} value={form.address} onChange={(e) => set("address", e.target.value)} /></Field>
        <Field label="Tipo de vivienda *"><select required value={form.housingType} onChange={(e) => set("housingType", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option>Casa</option><option>Departamento</option><option>Casa con patio</option><option>Departamento con balcon</option></select></Field>
        <Field label="Tiene otros animales? *"><select required value={form.otherAnimals} onChange={(e) => set("otherAnimals", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option>No</option><option>Si, perros</option><option>Si, gatos</option><option>Si, otros</option></select></Field>
        <Field label="Por que quiere adoptar? *" wide><Textarea required minLength={20} maxLength={2000} rows={4} value={form.motivation} onChange={(e) => set("motivation", e.target.value)} /></Field>
        <Field label="Disponibilidad para entrevista"><Input maxLength={500} value={form.availability} onChange={(e) => set("availability", e.target.value)} /></Field>
        <Field label="Experiencia previa"><Input maxLength={1000} value={form.previousExperience} onChange={(e) => set("previousExperience", e.target.value)} /></Field>
      </div>

      {customFields.length > 0 && <section className="space-y-4 border-t border-border pt-6"><div><h3 className="font-display text-xl font-bold">Preguntas de {safeNgo.nombre}</h3><p className="text-sm text-muted-foreground">Estas preguntas consideran las necesidades particulares de {animal.nombre}.</p></div><div className="grid sm:grid-cols-2 gap-4">{customFields.map((field) => <DynamicField key={field.key} field={field} value={customAnswers[field.key] ?? ""} onChange={(value) => setCustomAnswers((current) => ({ ...current, [field.key]: value }))} />)}</div></section>}

      <div className="flex gap-3"><Button type="submit" variant="hero" size="lg" disabled={submitting}>{submitting ? "Enviando..." : "Enviar postulacion"}</Button><Button type="button" variant="ghost" size="lg" onClick={() => setShowForm(false)}>Cancelar</Button></div>
    </form>}
  </section>;
};

function Field({ label, wide, children }) {
  return <div className={`space-y-2 ${wide ? "sm:col-span-2" : ""}`}><Label>{label}</Label>{children}</div>;
}

function DynamicField({ field, value, onChange }) {
  const label = `${field.label}${field.required ? " *" : ""}`;
  if (field.type === "LONG_TEXT") return <Field label={label} wide><Textarea required={field.required} maxLength={4000} value={value} onChange={(e) => onChange(e.target.value)} placeholder={field.placeholder ?? ""} /></Field>;
  if (field.type === "BOOLEAN") return <Field label={label}><select required={field.required} value={value} onChange={(e) => onChange(e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="">Selecciona</option><option value="true">Si</option><option value="false">No</option></select></Field>;
  if (field.type === "SINGLE_CHOICE") return <Field label={label}><select required={field.required} value={value} onChange={(e) => onChange(e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="">Selecciona</option>{field.options.map((option) => <option key={option}>{option}</option>)}</select></Field>;
  if (field.type === "MULTIPLE_CHOICE") {
    const selected = value ? value.split("\n") : [];
    return <Field label={label} wide><div className="grid sm:grid-cols-2 gap-2 border border-input rounded-md p-3">{field.options.map((option) => <label key={option} className="flex items-center gap-2 text-sm"><input type="checkbox" checked={selected.includes(option)} onChange={(e) => onChange(e.target.checked ? [...selected, option].join("\n") : selected.filter((item) => item !== option).join("\n"))} />{option}</label>)}</div></Field>;
  }
  const type = field.type === "EMAIL" ? "email" : field.type === "NUMBER" ? "number" : field.type === "PHONE" ? "tel" : "text";
  return <Field label={label}><Input type={type} required={field.required} maxLength={4000} value={value} onChange={(e) => onChange(e.target.value)} placeholder={field.placeholder ?? ""} /></Field>;
}

export default AnimalDetail;