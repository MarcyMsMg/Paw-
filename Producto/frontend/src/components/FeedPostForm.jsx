import { useEffect, useState } from "react";
import { Save } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { FEED_TYPE_LABELS } from "@/services/feedApi";

const empty = {
  title: "",
  summary: "",
  content: "",
  type: "GENERAL",
  imageUrlsText: "",
  videoUrl: "",
  relatedAnimalId: "",
  relatedCampaignId: "",
  publishNow: false,
};

const animalLabel = (animal) => {
  const name = animal.nombre ?? animal.name ?? "Animal sin nombre";
  const species = animal.especie ?? animal.species;
  const status = animal.estado ?? animal.status;
  return [name, species, status].filter(Boolean).join(" - ");
};

const campaignLabel = (campaign) => {
  const title = campaign.titulo ?? campaign.title ?? "Campana sin titulo";
  const status = campaign.estado ?? campaign.status;
  return [title, status].filter(Boolean).join(" - ");
};

export function FeedPostForm({
  initial,
  mode = "create",
  saving,
  relatedAnimals = [],
  relatedCampaigns = [],
  loadingRelated = false,
  onSubmit,
  onCancel,
}) {
  const [form, setForm] = useState(empty);

  useEffect(() => {
    if (!initial) {
      setForm(empty);
      return;
    }
    setForm({
      title: initial.title ?? "",
      summary: initial.summary ?? "",
      content: initial.content ?? "",
      type: initial.type ?? "GENERAL",
      imageUrlsText: (initial.imageUrls ?? []).join("\n"),
      videoUrl: initial.videoUrl ?? "",
      relatedAnimalId: initial.relatedAnimalId ?? "",
      relatedCampaignId: initial.relatedCampaignId ?? "",
      publishNow: false,
    });
  }, [initial]);

  const set = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const submit = (event) => {
    event.preventDefault();
    onSubmit({
      title: form.title.trim(),
      summary: form.summary.trim() || null,
      content: form.content.trim(),
      type: form.type,
      imageUrls: form.imageUrlsText.split("\n").map((url) => url.trim()).filter(Boolean),
      videoUrl: form.videoUrl.trim() || null,
      relatedAnimalId: form.relatedAnimalId || null,
      relatedCampaignId: form.relatedCampaignId || null,
      ...(mode === "create" ? { publishNow: form.publishNow } : {}),
    });
  };

  const selectedAnimalMissing = form.relatedAnimalId && !relatedAnimals.some((animal) => animal.id === form.relatedAnimalId);
  const selectedCampaignMissing = form.relatedCampaignId && !relatedCampaigns.some((campaign) => campaign.id === form.relatedCampaignId);

  return (
    <form onSubmit={submit} className="bg-card rounded-2xl border border-border shadow-soft p-6 md:p-8 space-y-5">
      <div className="grid md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label>Titulo</Label>
          <Input required maxLength={180} value={form.title} onChange={(event) => set("title", event.target.value)} />
        </div>
        <div className="space-y-2">
          <Label>Tipo</Label>
          <select value={form.type} onChange={(event) => set("type", event.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm">
            {Object.entries(FEED_TYPE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>
        <div className="space-y-2 md:col-span-2">
          <Label>Resumen</Label>
          <Input maxLength={600} value={form.summary} onChange={(event) => set("summary", event.target.value)} placeholder="Breve bajada para tarjetas y listados" />
        </div>
        <div className="space-y-2 md:col-span-2">
          <Label>Contenido</Label>
          <Textarea required rows={8} maxLength={10000} value={form.content} onChange={(event) => set("content", event.target.value)} />
        </div>
        <div className="space-y-2 md:col-span-2">
          <Label>Imagenes</Label>
          <Textarea rows={4} value={form.imageUrlsText} onChange={(event) => set("imageUrlsText", event.target.value)} placeholder={"https://imagen-1.jpg\nhttps://imagen-2.jpg"} />
          <p className="text-xs text-muted-foreground">Agrega una URL por linea. La primera imagen se usara como portada.</p>
        </div>
        <div className="space-y-2">
          <Label>Video</Label>
          <Input value={form.videoUrl} onChange={(event) => set("videoUrl", event.target.value)} placeholder="https://..." />
        </div>
        <div className="space-y-2">
          <Label>Animal relacionado</Label>
          <select value={form.relatedAnimalId} onChange={(event) => set("relatedAnimalId", event.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm" disabled={loadingRelated}>
            <option value="">Sin animal relacionado</option>
            {selectedAnimalMissing && <option value={form.relatedAnimalId}>Animal seleccionado anteriormente</option>}
            {relatedAnimals.map((animal) => <option key={animal.id} value={animal.id}>{animalLabel(animal)}</option>)}
          </select>
        </div>
        <div className="space-y-2">
          <Label>Campana relacionada</Label>
          <select value={form.relatedCampaignId} onChange={(event) => set("relatedCampaignId", event.target.value)} className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm" disabled={loadingRelated}>
            <option value="">Sin campana relacionada</option>
            {selectedCampaignMissing && <option value={form.relatedCampaignId}>Campana seleccionada anteriormente</option>}
            {relatedCampaigns.map((campaign) => <option key={campaign.id} value={campaign.id}>{campaignLabel(campaign)}</option>)}
          </select>
        </div>
        {loadingRelated && <p className="text-xs text-muted-foreground md:col-span-2">Cargando animales y campanas asociadas...</p>}
        {mode === "create" && (
          <label className="flex items-center gap-3 pt-7 text-sm">
            <input type="checkbox" checked={form.publishNow} onChange={(event) => set("publishNow", event.target.checked)} />
            Publicar inmediatamente
          </label>
        )}
      </div>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" variant="hero" size="lg" disabled={saving}>
          <Save className="w-4 h-4" /> {saving ? "Guardando..." : "Guardar"}
        </Button>
        <Button type="button" variant="outline" size="lg" onClick={onCancel} disabled={saving}>Cancelar</Button>
      </div>
    </form>
  );
}