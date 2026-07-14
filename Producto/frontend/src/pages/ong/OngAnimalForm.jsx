import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { adoptionsApi, animalToView } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";
import { validateText, isValidImageUrl } from "@/lib/validators";
import campaign1 from "@/assets/campaign-1.jpg";

const empty = {
  nombre: "", especie: "Perro", edad: "", sexo: "Macho", tamano: "Mediano",
  ubicacion: "", salud: "", descripcion: "", requisitos: "", fotos: [campaign1],
  estado: "DISPONIBLE", published: true, formTemplateId: "",
};
const statusToApi = { DISPONIBLE: "AVAILABLE", EN_PROCESO: "IN_PROCESS" };

const OngAnimalForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [animal, setAnimal] = useState(empty);
  const [photo, setPhoto] = useState("");
  const [templates, setTemplates] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    adoptionsApi.listTemplates()
      .then((items) => setTemplates(items.filter((item) => item.active)))
      .catch(showError);
    if (id) {
      adoptionsApi.listOwnedAnimals().then((items) => {
        const found = items.find((item) => item.id === id);
        if (!found) return;
        const view = animalToView(found);
        setAnimal(view);
        setPhoto(view.fotos[0] || "");
      }).catch(showError);
    }
  }, [id]);

  const set = (key, value) => setAnimal((current) => ({ ...current, [key]: value }));
  const save = async (event) => {
    event.preventDefault();
    const errors = [
      validateText(animal.nombre, { required: true, min: 2, max: 120, label: "El nombre" }),
      validateText(animal.especie, { required: true, min: 2, max: 80, label: "La especie" }),
      validateText(animal.edad, { required: true, min: 1, max: 80, label: "La edad" }),
      validateText(animal.ubicacion, { max: 160, label: "La ubicación" }),
      validateText(animal.salud, { max: 500, label: "El estado de salud" }),
      validateText(animal.descripcion, { required: true, min: 20, max: 2000, label: "La descripción" }),
      validateText(animal.requisitos, { max: 1000, label: "Los requisitos de adopción" }),
      photo.trim() && !isValidImageUrl(photo) ? "La foto debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
    ].find(Boolean);
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    setSaving(true);
    const payload = {
      name: animal.nombre.trim(),
      species: animal.especie.trim(),
      age: animal.edad.trim(),
      sex: animal.sexo,
      size: animal.tamano,
      location: animal.ubicacion.trim() || null,
      healthStatus: animal.salud.trim() || null,
      description: animal.descripcion.trim(),
      adoptionRequirements: animal.requisitos.trim() || null,
      photoUrls: [photo.trim() || campaign1],
      status: statusToApi[animal.estado] ?? null,
      formTemplateId: animal.formTemplateId || null,
      ...(id ? { published: animal.published } : {}),
    };
    try {
      if (id) await adoptionsApi.updateAnimal(id, payload);
      else await adoptionsApi.createAnimal(payload);
      toast({ title: id ? "Animal actualizado" : "Animal creado" });
      navigate("/ong/animales");
    } catch (error) {
      showError(error);
    } finally {
      setSaving(false);
    }
  };

  return <div className="space-y-6">
    <Button asChild variant="ghost" size="sm"><Link to="/ong/animales"><ArrowLeft className="w-4 h-4" /> Volver</Link></Button>
    <div><h1 className="font-display text-3xl font-bold">{id ? "Editar animal" : "Nuevo animal"}</h1><p className="text-muted-foreground mt-1">Define su información y el formulario que deberán responder los postulantes.</p></div>

    <form onSubmit={save} className="bg-card rounded-lg border border-border p-6 md:p-8 space-y-5">
      <div className="grid sm:grid-cols-2 gap-4">
        <div className="space-y-2"><Label>Nombre</Label><Input required minLength={2} maxLength={120} value={animal.nombre} onChange={(e) => set("nombre", e.target.value)} /></div>
        <div className="space-y-2"><Label>Especie</Label><Input required minLength={2} maxLength={80} value={animal.especie} onChange={(e) => set("especie", e.target.value)} /></div>
        <div className="space-y-2"><Label>Edad aproximada</Label><Input required maxLength={80} value={animal.edad} onChange={(e) => set("edad", e.target.value)} placeholder="3 años" /></div>
        <div className="space-y-2"><Label>Sexo</Label><select value={animal.sexo} onChange={(e) => set("sexo", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option>Macho</option><option>Hembra</option></select></div>
        <div className="space-y-2"><Label>Tamaño</Label><select value={animal.tamano} onChange={(e) => set("tamano", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option>Pequeño</option><option>Mediano</option><option>Grande</option></select></div>
        <div className="space-y-2"><Label>Estado</Label><select value={["DISPONIBLE", "EN_PROCESO"].includes(animal.estado) ? animal.estado : ""} disabled={!statusToApi[animal.estado]} onChange={(e) => set("estado", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="DISPONIBLE">Disponible</option><option value="EN_PROCESO">En proceso</option>{!statusToApi[animal.estado] && <option value="">{animal.estado}</option>}</select></div>
        <div className="space-y-2"><Label>Ubicación</Label><Input maxLength={160} value={animal.ubicacion} onChange={(e) => set("ubicacion", e.target.value)} placeholder="Comuna o ciudad" /></div>
        <div className="space-y-2"><Label>Formulario de adopción</Label><select value={animal.formTemplateId || ""} onChange={(e) => set("formTemplateId", e.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm"><option value="">Formulario estándar</option>{templates.map((template) => <option key={template.id} value={template.id}>{template.name} ({template.fields.length} personalizados)</option>)}</select><p className="text-xs text-muted-foreground">Puedes administrar las plantillas desde la sección Formularios.</p></div>
        <div className="space-y-2 sm:col-span-2"><Label>Estado de salud</Label><Input maxLength={500} value={animal.salud} onChange={(e) => set("salud", e.target.value)} /></div>
        <div className="space-y-2 sm:col-span-2"><Label>Descripción</Label><Textarea required minLength={20} maxLength={2000} rows={4} value={animal.descripcion} onChange={(e) => set("descripcion", e.target.value)} /></div>
        <div className="space-y-2 sm:col-span-2"><Label>Requisitos de adopción</Label><Textarea maxLength={1000} rows={3} value={animal.requisitos} onChange={(e) => set("requisitos", e.target.value)} placeholder="Condiciones generales que debe cumplir la familia" /></div>
        <div className="space-y-2 sm:col-span-2"><Label>Foto principal (URL)</Label><Input value={photo} onChange={(e) => setPhoto(e.target.value)} placeholder="https://..." /></div>
      </div>
      <div className="flex gap-3"><Button type="submit" variant="hero" size="lg" disabled={saving}>{saving ? "Guardando..." : "Guardar"}</Button><Button type="button" variant="outline" size="lg" onClick={() => navigate("/ong/animales")}>Cancelar</Button></div>
    </form>
  </div>;
};

function showError(error) {
  toast({ title: "No fue posible guardar", description: error?.message, variant: "destructive" });
}

export default OngAnimalForm;
