import { Link, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";
import { PawPrint } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { useAuth, homeFor } from "@/contexts/AuthContext";
import { isValidEmail } from "@/lib/validators";
const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    const [loading, setLoading] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const submit = async (e) => {
        e.preventDefault();
        if (!isValidEmail(email)) {
            toast({ title: "Ingresa un email válido", variant: "destructive" });
            return;
        }
        if (!password) {
            toast({ title: "Ingresa tu contraseña", variant: "destructive" });
            return;
        }
        setLoading(true);
        try {
            const u = await login(email, password);
            toast({ title: `Bienvenido ${u.nombre} 🐾` });
            const from = location.state?.from;
            navigate(from || homeFor(u.role));
        }
        catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
        }
        finally {
            setLoading(false);
        }
    };
    return (<section className="container max-w-5xl py-12 md:py-20 grid md:grid-cols-2 gap-10 items-center animate-fade-in-up">
      <div className="space-y-6">
        <div className="inline-grid place-items-center w-14 h-14 rounded-2xl gradient-hero text-primary-foreground shadow-glow">
          <PawPrint className="w-6 h-6"/>
        </div>
        <h1 className="font-display text-4xl md:text-5xl font-bold leading-tight">Bienvenido de vuelta a <span className="text-gradient">Paw+</span></h1>
        <p className="text-muted-foreground">Inicia sesión y continúa cambiando vidas.</p>
      </div>

      <form onSubmit={submit} className="bg-card border border-border shadow-card rounded-3xl p-8 space-y-5">
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="tu@email.com" className="h-12 rounded-xl"/>
        </div>
        <div className="space-y-2">
          <Label htmlFor="password">Contraseña</Label>
          <Input id="password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" className="h-12 rounded-xl"/>
        </div>
        <Button type="submit" disabled={loading} variant="hero" size="lg" className="w-full">
          {loading ? "Ingresando..." : "Iniciar sesión"}
        </Button>
        <div className="text-sm text-center text-muted-foreground space-y-1">
          <p>¿No tienes cuenta? <Link to="/registro-persona" className="text-primary font-semibold hover:underline">Regístrate como Persona</Link></p>
          <p>¿Eres una ONG? <Link to="/solicitud-ong" className="text-primary font-semibold hover:underline">Envía tu solicitud</Link></p>
        </div>
      </form>
    </section>);
};
export default Login;
