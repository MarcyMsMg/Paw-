import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { userApi, displayName, payoutAccountsApi } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { toast } from "@/hooks/use-toast";
import { isValidRut, isValidEmail, isValidImageUrl, validateText, formatRut, normalizeEmail } from "@/lib/validators";

const CURRENT_YEAR = new Date().getFullYear();

const EMPTY_TRANSFER = {
  holderName: "",
  rut: "",
  bankName: "",
  accountType: "Cuenta Corriente",
  accountNumber: "",
  email: "",
};

// Pestaña "Datos de transferencia": los campos salen bloqueados; "Editar" los habilita.
function TransferDataTab({ ngoId }) {
  const [data, setData] = useState(EMPTY_TRANSFER);
  const [original, setOriginal] = useState(EMPTY_TRANSFER);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [exists, setExists] = useState(false);

  useEffect(() => {
    if (!ngoId) return;
    payoutAccountsApi
      .getByNgo(ngoId)
      .then((a) => {
        const loaded = {
          holderName: a.holderName ?? "",
          rut: a.rut ?? "",
          bankName: a.bankName ?? "",
          accountType: a.accountType ?? "Cuenta Corriente",
          accountNumber: a.accountNumber ?? "",
          email: a.email ?? "",
        };
        setData(loaded);
        setOriginal(loaded);
        setExists(true);
      })
      .catch((err) => {
        // 404 = la ONG aún no cargó sus datos → form vacío, habilitado para empezar.
        if (err.status === 404) setEditing(true);
        else toast({ title: "Error", description: err.message, variant: "destructive" });
      })
      .finally(() => setLoading(false));
  }, [ngoId]);

  const set = (k, v) => setData((p) => ({ ...p, [k]: v }));

  const save = async () => {
    const errors = [
      validateText(data.holderName, { required: true, min: 2, max: 160, label: "El titular de la cuenta" }),
      !isValidRut(data.rut) ? "El RUT ingresado no es válido" : null,
      validateText(data.bankName, { required: true, min: 2, max: 120, label: "El banco" }),
      !data.accountNumber.trim() ? "El N° de cuenta es obligatorio" : !/^[0-9-]+$/.test(data.accountNumber.trim()) ? "El N° de cuenta solo puede contener números" : null,
      data.email.trim() && !isValidEmail(data.email) ? "El email de contacto no es válido" : null,
    ].find(Boolean);
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    setSaving(true);
    try {
      const saved = await payoutAccountsApi.upsert(ngoId, {
        holderName: data.holderName.trim(),
        rut: formatRut(data.rut),
        bankName: data.bankName.trim(),
        accountType: data.accountType,
        accountNumber: data.accountNumber.trim(),
        email: data.email.trim() ? normalizeEmail(data.email) : undefined,
      });
      const next = {
        holderName: saved.holderName ?? "",
        rut: saved.rut ?? "",
        bankName: saved.bankName ?? "",
        accountType: saved.accountType ?? "Cuenta Corriente",
        accountNumber: saved.accountNumber ?? "",
        email: saved.email ?? "",
      };
      setData(next);
      setOriginal(next);
      setExists(true);
      setEditing(false);
      toast({ title: "Datos de transferencia guardados ✅" });
    } catch (err) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  const cancel = () => {
    setData(original);
    setEditing(false);
  };

  if (loading) return <p className="text-muted-foreground">Cargando...</p>;

  const locked = !editing;
  return (
    <div className="bg-card rounded-3xl border border-border shadow-soft p-6 md:p-8 space-y-4 max-w-2xl">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm text-muted-foreground">
          {exists
            ? "Estos son los datos donde recibirás las transferencias de tus campañas."
            : "Aún no has cargado tus datos de transferencia. Complétalos para poder recibir los fondos."}
        </p>
        {locked && <Button variant="outline" size="sm" onClick={() => setEditing(true)}>Editar</Button>}
      </div>

      <div className="grid sm:grid-cols-2 gap-4">
        <div className="space-y-2 sm:col-span-2">
          <Label>Titular de la cuenta</Label>
          <Input required value={data.holderName} disabled={locked} onChange={(e) => set("holderName", e.target.value)} className={`h-11 rounded-xl ${locked ? "bg-secondary/40" : ""}`} />
        </div>
        <div className="space-y-2">
          <Label>RUT</Label>
          <Input required value={data.rut} disabled={locked} onChange={(e) => set("rut", e.target.value)} placeholder="12.345.678-9" className={`h-11 rounded-xl ${locked ? "bg-secondary/40" : ""}`} />
        </div>
        <div className="space-y-2">
          <Label>Banco</Label>
          <Input required value={data.bankName} disabled={locked} onChange={(e) => set("bankName", e.target.value)} className={`h-11 rounded-xl ${locked ? "bg-secondary/40" : ""}`} />
        </div>
        <div className="space-y-2">
          <Label>Tipo de cuenta</Label>
          <select value={data.accountType} disabled={locked} onChange={(e) => set("accountType", e.target.value)} className={`h-11 w-full rounded-xl border border-input bg-background px-3 text-sm ${locked ? "bg-secondary/40" : ""}`}>
            <option>Cuenta Corriente</option>
            <option>Cuenta Vista</option>
            <option>Cuenta de Ahorro</option>
            <option>Cuenta RUT</option>
          </select>
        </div>
        <div className="space-y-2">
          <Label>N° de cuenta</Label>
          <Input required inputMode="numeric" value={data.accountNumber} disabled={locked} onChange={(e) => set("accountNumber", e.target.value)} className={`h-11 rounded-xl ${locked ? "bg-secondary/40" : ""}`} />
        </div>
        <div className="space-y-2 sm:col-span-2">
          <Label>Email de contacto (opcional)</Label>
          <Input type="email" value={data.email} disabled={locked} onChange={(e) => set("email", e.target.value)} className={`h-11 rounded-xl ${locked ? "bg-secondary/40" : ""}`} />
        </div>
      </div>

      {editing && (
        <div className="flex gap-3">
          <Button variant="hero" size="lg" onClick={save} disabled={saving}>{saving ? "Guardando..." : "Guardar"}</Button>
          {exists && <Button variant="ghost" size="lg" onClick={cancel} disabled={saving}>Cancelar</Button>}
        </div>
      )}
    </div>
  );
}

const OngPerfil = () => {
  const { user, refresh } = useAuth();
  const [o, setO] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!user) return;
    userApi
      .getById(user.id)
      .then(setO)
      .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
      .finally(() => setLoading(false));
  }, [user]);

  if (loading) return <p className="text-muted-foreground">Cargando...</p>;
  if (!o) return <p className="text-muted-foreground">No se pudo cargar el perfil.</p>;

  const set = (k, v) => setO({ ...o, [k]: v });

  const save = async () => {
    const description = o.description?.trim() ?? "";
    const location = o.location?.trim() ?? "";
    const profileImageUrl = o.profileImageUrl?.trim() ?? "";
    const coverImageUrl = o.coverImageUrl?.trim() ?? "";
    const errors = [
      description ? validateText(description, { min: 20, max: 2000, label: "La descripción" }) : null,
      location ? validateText(location, { min: 3, max: 160, label: "La ubicación" }) : null,
      o.foundationYear != null && (o.foundationYear < 1900 || o.foundationYear > CURRENT_YEAR)
        ? `El año de fundación debe estar entre 1900 y ${CURRENT_YEAR}`
        : null,
      o.rescuedAnimalsCount != null && o.rescuedAnimalsCount < 0 ? "Los animales rescatados no pueden ser negativos" : null,
      o.volunteersCount != null && o.volunteersCount < 0 ? "Los voluntarios no pueden ser negativos" : null,
      profileImageUrl && !isValidImageUrl(profileImageUrl) ? "El logo debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
      coverImageUrl && !isValidImageUrl(coverImageUrl) ? "El banner debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
    ].find(Boolean);
    if (errors) {
      toast({ title: errors, variant: "destructive" });
      return;
    }
    setSaving(true);
    try {
      const updated = await userApi.updateProfile(o.id, {
        profileImageUrl: profileImageUrl || undefined,
        description: description || undefined,
        coverImageUrl: coverImageUrl || undefined,
        location: location || undefined,
        foundationYear: o.foundationYear ?? undefined,
        rescuedAnimalsCount: o.rescuedAnimalsCount ?? undefined,
        volunteersCount: o.volunteersCount ?? undefined,
      });
      setO(updated);

      const session = JSON.parse(localStorage.getItem("pawplus_session") || "{}");
      localStorage.setItem(
        "pawplus_session",
        JSON.stringify({
          ...session,
          nombre: displayName(updated),
          foto: updated.profileImageUrl ?? undefined,
        })
      );
      refresh();

      toast({ title: "Perfil actualizado ✅" });
    } catch (err) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Mi ONG</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Perfil de la ONG</h1>
      </header>

      <Tabs defaultValue="perfil">
        <TabsList>
          <TabsTrigger value="perfil">Perfil</TabsTrigger>
          <TabsTrigger value="transferencia">Datos de transferencia</TabsTrigger>
        </TabsList>

        <TabsContent value="perfil">
          <div className="bg-card rounded-3xl border border-border shadow-soft p-6 md:p-8 space-y-4 max-w-2xl">
            <div className="grid sm:grid-cols-2 gap-4">
              <div className="space-y-2 sm:col-span-2">
                <Label>Nombre de la ONG</Label>
                <Input value={o.ngoName ?? ""} disabled className="h-11 rounded-xl bg-secondary/40" />
                <p className="text-xs text-muted-foreground">El nombre de la ONG no se puede cambiar.</p>
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Correo</Label>
                <Input type="email" value={o.email} disabled className="h-11 rounded-xl bg-secondary/40" />
                <p className="text-xs text-muted-foreground">El correo no se puede cambiar por ahora.</p>
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Descripción</Label>
                <Textarea maxLength={3000} rows={4} value={o.description ?? ""} onChange={(e) => set("description", e.target.value)} className="rounded-xl" placeholder="Cuéntale al mundo qué hace tu ONG..." />
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Ubicación</Label>
                <Input maxLength={150} value={o.location ?? ""} onChange={(e) => set("location", e.target.value)} className="h-11 rounded-xl" placeholder="Ciudad, País" />
              </div>

              <div className="space-y-2">
                <Label>Año de fundación</Label>
                <Input type="number" min={1900} max={CURRENT_YEAR} value={o.foundationYear ?? ""} onChange={(e) => set("foundationYear", e.target.value === "" ? null : Number(e.target.value))} className="h-11 rounded-xl" />
              </div>

              <div className="space-y-2">
                <Label>Animales rescatados</Label>
                <Input type="number" min={0} value={o.rescuedAnimalsCount ?? ""} onChange={(e) => set("rescuedAnimalsCount", e.target.value === "" ? null : Number(e.target.value))} className="h-11 rounded-xl" />
              </div>

              <div className="space-y-2">
                <Label>Voluntarios</Label>
                <Input type="number" min={0} value={o.volunteersCount ?? ""} onChange={(e) => set("volunteersCount", e.target.value === "" ? null : Number(e.target.value))} className="h-11 rounded-xl" />
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Logo / foto de perfil (URL)</Label>
                <Input value={o.profileImageUrl ?? ""} onChange={(e) => set("profileImageUrl", e.target.value)} className="h-11 rounded-xl" placeholder="https://..." />
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Banner / portada (URL)</Label>
                <Input value={o.coverImageUrl ?? ""} onChange={(e) => set("coverImageUrl", e.target.value)} className="h-11 rounded-xl" placeholder="https://..." />
              </div>
            </div>

            <Button variant="hero" size="lg" onClick={save} disabled={saving}>
              {saving ? "Guardando..." : "Guardar cambios"}
            </Button>
          </div>
        </TabsContent>

        <TabsContent value="transferencia">
          <TransferDataTab ngoId={o.id} />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default OngPerfil;
