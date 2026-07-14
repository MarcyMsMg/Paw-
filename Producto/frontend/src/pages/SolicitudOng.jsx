import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Building2, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";
import { authApi } from "@/services/authApi";
import {
    normalizeEmail,
    isValidEmail,
    validatePassword,
    validatePasswordConfirmation,
    validateText,
    isValidImageUrl,
} from "@/lib/validators";

const CURRENT_YEAR = new Date().getFullYear();
const MAX_ACTA_SIZE_BYTES = 3 * 1024 * 1024;
const ACTA_ACCEPTED_TYPES = ["application/pdf", "image/jpeg", "image/png", "image/webp"];

function readFileAsDataUrl(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(String(reader.result ?? ""));
        reader.onerror = () => reject(new Error("No se pudo leer el archivo."));
        reader.readAsDataURL(file);
    });
}

const SolicitudOng = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [done, setDone] = useState(false);
    const [f, setF] = useState({
        nombre: "", email: "", password: "", confirm: "",
        descripcion: "", ubicacion: "", fundada: 2020, rescatados: 0, voluntarios: 0,
        logo: "P+", banner: "", redes: "", constitutionActUrl: "", constitutionActName: "",
    });
    const set = (k, v) => setF((p) => ({ ...p, [k]: v }));

    const handleActaChange = async (event) => {
        const file = event.target.files?.[0];
        if (!file) {
            setF((p) => ({ ...p, constitutionActUrl: "", constitutionActName: "" }));
            return;
        }
        if (!ACTA_ACCEPTED_TYPES.includes(file.type)) {
            event.target.value = "";
            setF((p) => ({ ...p, constitutionActUrl: "", constitutionActName: "" }));
            toast({ title: "El acta debe ser PDF o imagen (jpg, png o webp)", variant: "destructive" });
            return;
        }
        if (file.size > MAX_ACTA_SIZE_BYTES) {
            event.target.value = "";
            setF((p) => ({ ...p, constitutionActUrl: "", constitutionActName: "" }));
            toast({ title: "El acta no puede superar los 3 MB", variant: "destructive" });
            return;
        }
        try {
            const dataUrl = await readFileAsDataUrl(file);
            setF((p) => ({ ...p, constitutionActUrl: dataUrl, constitutionActName: file.name }));
        } catch (error) {
            event.target.value = "";
            setF((p) => ({ ...p, constitutionActUrl: "", constitutionActName: "" }));
            toast({ title: "No se pudo leer el acta", description: error.message, variant: "destructive" });
        }
    };

    const submit = async (e) => {
        e.preventDefault();
        const fundada = Number(f.fundada);
        const rescatados = Number(f.rescatados);
        const voluntarios = Number(f.voluntarios);
        const errors = [
            validateText(f.nombre, { required: true, min: 3, max: 120, label: "El nombre de la ONG" }),
            !isValidEmail(f.email) ? "Ingresa un correo institucional valido" : null,
            validatePassword(f.password),
            validatePasswordConfirmation(f.password, f.confirm),
            validateText(f.descripcion, { required: true, min: 20, max: 2000, label: "La descripcion" }),
            validateText(f.ubicacion, { required: true, min: 3, max: 160, label: "La ubicacion" }),
            !f.constitutionActUrl ? "El acta de constitucion es obligatoria" : null,
            !Number.isInteger(fundada) || fundada < 1900 || fundada > CURRENT_YEAR
                ? `El anio de fundacion debe estar entre 1900 y ${CURRENT_YEAR}`
                : null,
            !(rescatados >= 0) ? "Los animales rescatados no pueden ser negativos" : null,
            !(voluntarios >= 0) ? "Los voluntarios no pueden ser negativos" : null,
            f.banner.trim() && !isValidImageUrl(f.banner) ? "El banner debe ser una URL de imagen valida (jpg, jpeg, png o webp)" : null,
        ].find(Boolean);
        if (errors) {
            toast({ title: errors, variant: "destructive" });
            return;
        }
        setLoading(true);
        try {
            await authApi.requestNgoRegistration({
                ngoName: f.nombre.trim(),
                email: normalizeEmail(f.email),
                password: f.password,
                coverImageUrl: f.banner.trim() || undefined,
                constitutionActUrl: f.constitutionActUrl,
                description: f.descripcion.trim(),
                location: f.ubicacion.trim(),
                foundationYear: fundada,
                rescuedAnimalsCount: rescatados,
                volunteersCount: voluntarios,
            });
            setDone(true);
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setLoading(false);
        }
    };
    if (done) {
        return (<section className="container max-w-lg py-20 text-center animate-fade-in-up">
        <div className="inline-grid place-items-center w-16 h-16 rounded-2xl gradient-hero text-primary-foreground shadow-glow mb-6">
          <CheckCircle2 className="w-7 h-7"/>
        </div>
        <h1 className="font-display text-3xl font-bold mb-3">Solicitud enviada</h1>
        <p className="text-muted-foreground mb-2">Tu solicitud fue enviada correctamente.</p>
        <p className="text-muted-foreground mb-8">Un administrador revisara la informacion antes de activar la cuenta.</p>
        <Button asChild variant="hero" size="lg"><Link to="/">Volver al inicio</Link></Button>
      </section>);
    }
    return (<section className="container max-w-2xl py-12 md:py-16 animate-fade-in-up">
      <div className="text-center mb-8">
        <div className="inline-grid place-items-center w-14 h-14 rounded-2xl gradient-hero text-primary-foreground shadow-glow mb-4">
          <Building2 className="w-6 h-6"/>
        </div>
        <h1 className="font-display text-3xl md:text-4xl font-bold">Solicitud de registro ONG</h1>
        <p className="mt-2 text-muted-foreground">Completa el formulario. Un administrador revisara tu solicitud.</p>
      </div>

      <form onSubmit={submit} className="bg-card border border-border shadow-card rounded-3xl p-6 md:p-8 space-y-4">
        <div className="grid sm:grid-cols-2 gap-4">
          <div className="space-y-2 sm:col-span-2"><Label>Nombre de la ONG</Label><Input required minLength={3} maxLength={120} value={f.nombre} onChange={(e) => set("nombre", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Correo institucional</Label><Input type="email" required maxLength={120} value={f.email} onChange={(e) => set("email", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Ubicacion</Label><Input required minLength={3} maxLength={160} value={f.ubicacion} onChange={(e) => set("ubicacion", e.target.value)} placeholder="Ciudad, Pais" className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Contrasena</Label><Input type="password" required minLength={8} maxLength={72} value={f.password} onChange={(e) => set("password", e.target.value)} placeholder="Minimo 8 caracteres, con mayuscula, minuscula y numero" className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Confirmar contrasena</Label><Input type="password" required value={f.confirm} onChange={(e) => set("confirm", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2 sm:col-span-2"><Label>Descripcion de la ONG</Label><Textarea required minLength={20} maxLength={2000} rows={4} value={f.descripcion} onChange={(e) => set("descripcion", e.target.value)} className="rounded-xl"/></div>
          <div className="space-y-2"><Label>Anio de fundacion</Label><Input type="number" required min={1900} max={CURRENT_YEAR} value={f.fundada} onChange={(e) => set("fundada", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Logo (texto corto)</Label><Input value={f.logo} onChange={(e) => set("logo", e.target.value)} placeholder="P+" className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Animales rescatados</Label><Input type="number" min={0} value={f.rescatados} onChange={(e) => set("rescatados", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Voluntarios</Label><Input type="number" min={0} value={f.voluntarios} onChange={(e) => set("voluntarios", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2 sm:col-span-2"><Label>Banner (URL opcional)</Label><Input type="url" value={f.banner} onChange={(e) => set("banner", e.target.value)} placeholder="https://..." className="h-11 rounded-xl"/></div>
          <div className="space-y-2 sm:col-span-2"><Label>Redes sociales (opcional)</Label><Input maxLength={200} value={f.redes} onChange={(e) => set("redes", e.target.value)} placeholder="@mi_ong" className="h-11 rounded-xl"/></div>
          <div className="space-y-2 sm:col-span-2">
            <Label>Acta de constituci&oacute;n *</Label>
            <Input type="file" required accept=".pdf,.jpg,.jpeg,.png,.webp,application/pdf,image/jpeg,image/png,image/webp" onChange={handleActaChange} className="h-11 rounded-xl cursor-pointer"/>
            <p className="text-xs text-muted-foreground">PDF o imagen, maximo 3 MB.</p>
            {f.constitutionActName && <p className="text-xs font-medium text-primary">Archivo seleccionado: {f.constitutionActName}</p>}
          </div>
        </div>
        <Button type="submit" disabled={loading} variant="hero" size="lg" className="w-full">{loading ? "Enviando..." : "Enviar solicitud"}</Button>
        <p className="text-sm text-center text-muted-foreground">Ya tienes cuenta activa? <Link to="/login" className="text-primary font-semibold hover:underline">Inicia sesion</Link></p>
      </form>
    </section>);
};
export default SolicitudOng;