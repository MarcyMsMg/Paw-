import { useEffect, useState } from "react";
import { ArrowDown, ArrowUp, FileText, Plus, Save, Trash2, X } from "lucide-react";
import { adoptionsApi } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";
import { validateText } from "@/lib/validators";

const MAX_FIELDS = 15;
const MAX_OPTIONS = 20;
const baseFields = [
  "Nombre completo", "Correo", "Teléfono", "Dirección o comuna",
  "Tipo de vivienda", "Otros animales", "Motivación",
  "Disponibilidad para entrevista", "Experiencia previa",
];
const fieldTypes = [
  ["SHORT_TEXT", "Texto corto"],
  ["LONG_TEXT", "Texto largo"],
  ["EMAIL", "Correo"],
  ["PHONE", "Teléfono"],
  ["NUMBER", "Número"],
  ["BOOLEAN", "Sí / No"],
  ["SINGLE_CHOICE", "Selección única"],
  ["MULTIPLE_CHOICE", "Selección múltiple"],
];
const blankField = () => ({ label: "", type: "SHORT_TEXT", required: false, placeholder: "", options: [] });
const emptyTemplate = { name: "", description: "", active: true, fields: [] };

const OngFormTemplates = () => {
  const [templates, setTemplates] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyTemplate);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);

  const reload = () => adoptionsApi.listTemplates().then(setTemplates);
  useEffect(() => { reload().catch(showError); }, []);

  const startNew = () => {
    setEditingId(null);
    setForm({ ...emptyTemplate, fields: [] });
    setEditing(true);
  };
  const startEdit = (template) => {
    setEditingId(template.id);
    setForm({
      name: template.name,
      description: template.description ?? "",
      active: template.active,
      fields: template.fields.map((field) => ({
        label: field.label,
        type: field.type,
        required: field.required,
        placeholder: field.placeholder ?? "",
        options: field.options ?? [],
      })),
    });
    setEditing(true);
  };
  const updateField = (index, patch) => setForm((current) => ({
    ...current,
    fields: current.fields.map((field, position) => position === index ? { ...field, ...patch } : field),
  }));
  const removeField = (index) => setForm((current) => ({
    ...current,
    fields: current.fields.filter((_, position) => position !== index),
  }));
  const moveField = (index, direction) => setForm((current) => {
    const target = index + direction;
    if (target < 0 || target >= current.fields.length) return current;
    const fields = [...current.fields];
    [fields[index], fields[target]] = [fields[target], fields[index]];
    return { ...current, fields };
  });
  const save = async (event) => {
    event.preventDefault();
    const nameError = validateText(form.name, { required: true, min: 3, max: 120, label: "El nombre de la plantilla" });
    const descriptionError = validateText(form.description, { max: 500, label: "La descripción" });
    let fieldError = null;
    for (const [index, field] of form.fields.entries()) {
      const position = `Pregunta ${index + 1}`;
      fieldError = validateText(field.label, { required: true, min: 3, max: 160, label: position });
      if (fieldError) break;
      if (["SINGLE_CHOICE", "MULTIPLE_CHOICE"].includes(field.type)) {
        const options = field.options.map((option) => option.trim()).filter(Boolean);
        const uniqueOptions = new Set(options);
        if (options.length < 2) {
          fieldError = `${position}: agrega al menos 2 opciones`;
        } else if (options.length > MAX_OPTIONS) {
          fieldError = `${position}: no puede tener más de ${MAX_OPTIONS} opciones`;
        } else if (uniqueOptions.size !== options.length) {
          fieldError = `${position}: no puede tener opciones duplicadas`;
        } else if (options.some((option) => option.length > 160)) {
          fieldError = `${position}: cada opción puede tener máximo 160 caracteres`;
        }
      }
      if (fieldError) break;
    }
    const errors = nameError || descriptionError || fieldError;
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    setSaving(true);
    try {
      const payload = {
        ...form,
        name: form.name.trim(),
        description: form.description.trim(),
        fields: form.fields.map((field) => ({
          ...field,
          label: field.label.trim(),
          placeholder: field.placeholder.trim(),
          options: ["SINGLE_CHOICE", "MULTIPLE_CHOICE"].includes(field.type)
            ? field.options.map((option) => option.trim()).filter(Boolean)
            : [],
        })),
      };
      if (editingId) await adoptionsApi.updateTemplate(editingId, payload);
      else await adoptionsApi.createTemplate(payload);
      toast({ title: editingId ? "Plantilla actualizada" : "Plantilla creada" });
      setEditing(false);
      await reload();
    } catch (error) {
      showError(error);
    } finally {
      setSaving(false);
    }
  };
  const deactivate = async (template) => {
    if (!confirm(`¿Desactivar la plantilla "${template.name}"?`)) return;
    try {
      await adoptionsApi.deactivateTemplate(template.id);
      toast({ title: "Plantilla desactivada" });
      await reload();
    } catch (error) {
      showError(error);
    }
  };

  return <div className="space-y-7">
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <p className="text-sm font-semibold text-primary">Mi ONG</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Formularios de adopción</h1>
        <p className="text-muted-foreground mt-2">Crea plantillas y asígnalas a los animales que compartan necesidades.</p>
      </div>
      {!editing && <Button variant="hero" onClick={startNew}><Plus className="w-4 h-4" /> Nueva plantilla</Button>}
    </header>

    {editing ? <form onSubmit={save} className="bg-card border border-border rounded-lg p-5 md:p-7 space-y-7">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="font-display text-2xl font-bold">{editingId ? "Editar plantilla" : "Nueva plantilla"}</h2>
          <p className="text-sm text-muted-foreground">Los campos base se incluyen siempre y no pueden eliminarse.</p>
        </div>
        <Button type="button" size="icon" variant="ghost" title="Cerrar" onClick={() => setEditing(false)}><X className="w-4 h-4" /></Button>
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <div className="space-y-2"><Label>Nombre de la plantilla</Label><Input required minLength={3} maxLength={120} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Ej. Perros grandes" /></div>
        <div className="flex items-center gap-3 pt-7"><Switch checked={form.active} onCheckedChange={(active) => setForm({ ...form, active })} /><Label>Plantilla activa</Label></div>
        <div className="space-y-2 md:col-span-2"><Label>Descripción</Label><Textarea maxLength={500} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Cuándo debe utilizarse esta plantilla" /></div>
      </div>

      <section className="space-y-3">
        <div><h3 className="font-semibold">Campos base</h3><p className="text-sm text-muted-foreground">Se muestran en todos los formularios.</p></div>
        <div className="flex flex-wrap gap-2">{baseFields.map((field) => <span key={field} className="px-3 py-1.5 rounded-md bg-secondary text-xs font-medium">{field}</span>)}</div>
      </section>

      <section className="space-y-4">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div><h3 className="font-semibold">Campos personalizados</h3><p className="text-sm text-muted-foreground">{form.fields.length} de {MAX_FIELDS} campos utilizados.</p></div>
          <Button type="button" variant="outline" disabled={form.fields.length >= MAX_FIELDS} onClick={() => setForm({ ...form, fields: [...form.fields, blankField()] })}><Plus className="w-4 h-4" /> Agregar campo</Button>
        </div>
        {form.fields.map((field, index) => <div key={index} className="border border-border rounded-lg p-4 space-y-4">
          <div className="flex items-center justify-between gap-2"><p className="font-semibold text-sm">Pregunta {index + 1}</p><div className="flex gap-1">
            <Button type="button" size="icon" variant="ghost" title="Subir" disabled={index === 0} onClick={() => moveField(index, -1)}><ArrowUp className="w-4 h-4" /></Button>
            <Button type="button" size="icon" variant="ghost" title="Bajar" disabled={index === form.fields.length - 1} onClick={() => moveField(index, 1)}><ArrowDown className="w-4 h-4" /></Button>
            <Button type="button" size="icon" variant="ghost" title="Eliminar" onClick={() => removeField(index)}><Trash2 className="w-4 h-4 text-destructive" /></Button>
          </div></div>
          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2"><Label>Pregunta</Label><Input required maxLength={160} value={field.label} onChange={(e) => updateField(index, { label: e.target.value })} /></div>
            <div className="space-y-2"><Label>Tipo de respuesta</Label><select value={field.type} onChange={(e) => updateField(index, { type: e.target.value, options: [] })} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm">{fieldTypes.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
            <div className="space-y-2"><Label>Texto de ayuda</Label><Input maxLength={200} value={field.placeholder} onChange={(e) => updateField(index, { placeholder: e.target.value })} placeholder="Opcional" /></div>
            <div className="flex items-center gap-3 pt-7"><Switch checked={field.required} onCheckedChange={(required) => updateField(index, { required })} /><Label>Respuesta obligatoria</Label></div>
            {["SINGLE_CHOICE", "MULTIPLE_CHOICE"].includes(field.type) && <div className="space-y-2 md:col-span-2"><Label>Opciones, una por línea</Label><Textarea required value={field.options.join("\n")} onChange={(e) => updateField(index, { options: e.target.value.split("\n") })} placeholder={"Sí\nNo"} /></div>}
          </div>
        </div>)}
        {form.fields.length === 0 && <div className="border border-dashed border-border rounded-lg py-10 text-center text-sm text-muted-foreground">Esta plantilla usará solamente los campos base.</div>}
      </section>

      <div className="flex gap-3"><Button type="submit" variant="hero" disabled={saving}><Save className="w-4 h-4" /> {saving ? "Guardando..." : "Guardar plantilla"}</Button><Button type="button" variant="outline" onClick={() => setEditing(false)}>Cancelar</Button></div>
    </form> : <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
      {templates.map((template) => <article key={template.id} className="bg-card border border-border rounded-lg p-5 space-y-4">
        <div className="flex items-start justify-between gap-3"><div className="w-10 h-10 rounded-md bg-primary/10 grid place-items-center"><FileText className="w-5 h-5 text-primary" /></div><span className={`text-xs font-semibold px-2 py-1 rounded-md ${template.active ? "bg-emerald-100 text-emerald-800" : "bg-secondary text-muted-foreground"}`}>{template.active ? "Activa" : "Inactiva"}</span></div>
        <div><h2 className="font-display text-xl font-bold">{template.name}</h2><p className="text-sm text-muted-foreground mt-1 line-clamp-2">{template.description || "Sin descripción"}</p></div>
        <div className="text-xs text-muted-foreground flex gap-4"><span>{template.fields.length} personalizados</span><span>{template.assignedAnimals} animales</span><span>v{template.revision}</span></div>
        <div className="flex gap-2"><Button variant="soft" size="sm" className="flex-1" onClick={() => startEdit(template)}>Editar</Button>{template.active && <Button variant="outline" size="sm" onClick={() => deactivate(template)}>Desactivar</Button>}</div>
      </article>)}
      {templates.length === 0 && <div className="md:col-span-2 xl:col-span-3 border border-dashed border-border rounded-lg py-14 text-center"><FileText className="w-8 h-8 text-muted-foreground mx-auto mb-3" /><p className="font-semibold">Aún no tienes plantillas</p><p className="text-sm text-muted-foreground mt-1">Puedes seguir usando el formulario estándar o crear una plantilla.</p></div>}
    </div>}
  </div>;
};

function showError(error) {
  toast({ title: "No fue posible completar la operación", description: error?.message, variant: "destructive" });
}

export default OngFormTemplates;
