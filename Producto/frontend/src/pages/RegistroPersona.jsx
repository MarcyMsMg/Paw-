import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { PawPrint } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { authApi } from "@/services/authApi";
import { normalizeEmail, isValidEmail, validatePassword, validatePasswordConfirmation, validateText, isValidImageUrl } from "@/lib/validators";
const RegistroPersona = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState({ nombre: "", apellido: "", email: "", password: "", confirm: "", foto: "" });
    const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
    const submit = async (e) => {
        e.preventDefault();
        const errors = [
            validateText(form.nombre, { required: true, min: 2, max: 80, label: "El nombre" }),
            validateText(form.apellido, { required: true, min: 2, max: 80, label: "El apellido" }),
            !isValidEmail(form.email) ? "Ingresa un email válido" : null,
            validatePassword(form.password),
            validatePasswordConfirmation(form.password, form.confirm),
            form.foto.trim() && !isValidImageUrl(form.foto) ? "La foto de perfil debe ser una URL de imagen válida (jpg, jpeg, png o webp)" : null,
        ].find(Boolean);
        if (errors) {
            toast({ title: errors, variant: "destructive" });
            return;
        }
        setLoading(true);
        try {
            await authApi.registerNaturalPerson({
                firstName: form.nombre.trim(),
                lastName: form.apellido.trim(),
                email: normalizeEmail(form.email),
                password: form.password,
                profileImageUrl: form.foto.trim() || undefined,
            });
            toast({ title: "¡Cuenta creada! 🐾", description: "Ya puedes iniciar sesión." });
            navigate("/login");
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setLoading(false);
        }
    };
    return (<section className="container max-w-md py-16 md:py-24 animate-fade-in-up">
      <div className="text-center mb-8">
        <div className="inline-grid place-items-center w-14 h-14 rounded-2xl gradient-hero text-primary-foreground shadow-glow mb-4">
          <PawPrint className="w-6 h-6"/>
        </div>
        <h1 className="font-display text-3xl md:text-4xl font-bold">Únete a Paw+</h1>
        <p className="mt-2 text-muted-foreground">Crea tu cuenta como Persona Natural.</p>
      </div>

      <form onSubmit={submit} className="bg-card border border-border shadow-card rounded-3xl p-8 space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-2"><Label>Nombre</Label><Input required minLength={2} maxLength={80} value={form.nombre} onChange={(e) => set("nombre", e.target.value)} className="h-11 rounded-xl"/></div>
          <div className="space-y-2"><Label>Apellido</Label><Input required minLength={2} maxLength={80} value={form.apellido} onChange={(e) => set("apellido", e.target.value)} className="h-11 rounded-xl"/></div>
        </div>
        <div className="space-y-2"><Label>Email</Label><Input type="email" required maxLength={120} value={form.email} onChange={(e) => set("email", e.target.value)} className="h-11 rounded-xl"/></div>
        <div className="space-y-2"><Label>Contraseña</Label><Input type="password" required minLength={8} maxLength={72} value={form.password} onChange={(e) => set("password", e.target.value)} placeholder="Mínimo 8 caracteres, con mayúscula, minúscula y número" className="h-11 rounded-xl"/></div>
        <div className="space-y-2"><Label>Confirmar contraseña</Label><Input type="password" required value={form.confirm} onChange={(e) => set("confirm", e.target.value)} className="h-11 rounded-xl"/></div>
        <div className="space-y-2"><Label>Foto de perfil (URL opcional)</Label><Input type="url" value={form.foto} onChange={(e) => set("foto", e.target.value)} placeholder="https://..." className="h-11 rounded-xl"/></div>
        <Button type="submit" disabled={loading} variant="hero" size="lg" className="w-full">{loading ? "Creando..." : "Crear cuenta"}</Button>
        <p className="text-sm text-center text-muted-foreground">¿Ya tienes cuenta? <Link to="/login" className="text-primary font-semibold hover:underline">Inicia sesión</Link></p>
        <p className="text-sm text-center text-muted-foreground">¿Eres una ONG? <Link to="/solicitud-ong" className="text-primary font-semibold hover:underline">Solicitar registro</Link></p>
      </form>
    </section>);
};
export default RegistroPersona;
