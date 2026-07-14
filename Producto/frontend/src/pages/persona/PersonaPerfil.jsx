import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { userApi, displayName } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { validateText, isValidImageUrl } from "@/lib/validators";
const PersonaPerfil = () => {
    const { user, refresh } = useAuth();
    const [p, setP] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    useEffect(() => {
        if (!user)
            return;
        userApi
            .getById(user.id)
            .then(setP)
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    }, [user]);
    if (loading)
        return <p className="text-muted-foreground">Cargando...</p>;
    if (!p)
        return <p className="text-muted-foreground">No se pudo cargar el perfil.</p>;
    const set = (k, v) => setP({ ...p, [k]: v });
    const save = async () => {
        const foto = p.profileImageUrl?.trim() ?? "";
        const errors = [
            validateText(p.firstName, { required: true, min: 2, max: 80, label: "El nombre" }),
            validateText(p.lastName, { required: true, min: 2, max: 80, label: "El apellido" }),
            foto && !isValidImageUrl(foto) ? "La foto de perfil debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
        ].find(Boolean);
        if (errors) {
            toast({ title: errors, variant: "destructive" });
            return;
        }
        setSaving(true);
        try {
            const updated = await userApi.updateProfile(p.id, {
                firstName: p.firstName.trim(),
                lastName: p.lastName.trim(),
                profileImageUrl: foto || undefined,
            });
            setP(updated);
            const session = JSON.parse(localStorage.getItem("pawplus_session") || "{}");
            localStorage.setItem("pawplus_session", JSON.stringify({
                ...session,
                nombre: displayName(updated),
                foto: updated.profileImageUrl ?? undefined,
            }));
            refresh();
            toast({ title: "Perfil actualizado ✅" });
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setSaving(false);
        }
    };
    return (<div className="space-y-6">
      <header>
        <p className="text-sm font-semibold text-primary">Mi cuenta</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Mi perfil</h1>
      </header>

      <div className="bg-card rounded-3xl border border-border shadow-soft p-6 md:p-8 space-y-4 max-w-xl">
        <div className="grid sm:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Nombre</Label>
            <Input required minLength={2} maxLength={80} value={p.firstName ?? ""} onChange={(e) => set("firstName", e.target.value)} className="h-11 rounded-xl"/>
          </div>
          <div className="space-y-2">
            <Label>Apellido</Label>
            <Input required minLength={2} maxLength={80} value={p.lastName ?? ""} onChange={(e) => set("lastName", e.target.value)} className="h-11 rounded-xl"/>
          </div>
          <div className="space-y-2 sm:col-span-2">
            <Label>Correo</Label>
            <Input type="email" value={p.email} disabled className="h-11 rounded-xl bg-secondary/40"/>
            <p className="text-xs text-muted-foreground">El correo no se puede cambiar por ahora.</p>
          </div>
          <div className="space-y-2 sm:col-span-2">
            <Label>Foto de perfil (URL)</Label>
            <Input value={p.profileImageUrl ?? ""} onChange={(e) => set("profileImageUrl", e.target.value)} className="h-11 rounded-xl"/>
          </div>
        </div>
        <Button variant="hero" size="lg" onClick={save} disabled={saving}>
          {saving ? "Guardando..." : "Guardar cambios"}
        </Button>
      </div>
    </div>);
};
export default PersonaPerfil;
