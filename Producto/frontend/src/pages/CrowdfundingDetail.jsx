import { useState } from "react";
import { Link, useParams, useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft, Clock, Heart, Share2, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ProgressBar } from "@/components/ProgressBar";
import { Skeleton } from "@/components/Skeleton";
import { useFetch } from "@/hooks/useFetch";
import { campaignsApi, campaignToView, ngosApi, ngoToFundacion, isImageUrl, youtubeEmbedUrl, donationsApi } from "@/services/authApi";
import { useAuth } from "@/contexts/AuthContext";
import { formatCurrency, percent } from "@/lib/format";
import { toast } from "@/hooks/use-toast";
const CrowdfundingDetail = () => {
    const { id = "" } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const { data: detail, loading } = useFetch(async () => {
        const campaign = await campaignsApi.getById(id);
        if (campaign.status !== "ACTIVE") return null;
        const ngo = await ngosApi.getById(campaign.ngoId);
        return { campaign: campaignToView(campaign), fundacion: ngoToFundacion(ngo) };
    }, [id]);
    const c = detail?.campaign;
    const f = detail?.fundacion;
    // Contador real de donantes: donantes unicos con donacion APROBADA.
    const { data: donorCount } = useFetch(
        () => c ? donationsApi.getCampaignSummary(id).then((summary) => summary?.donorCount ?? 0) : Promise.resolve(0),
        [id, c?.id]
    );
    const [amount, setAmount] = useState(50000);
    const [submitting, setSubmitting] = useState(false);
    // Las ONG y el admin no donan: solo ven el monto recaudado.
    const canDonate = !user || user.role === "PERSONA_NATURAL";
    if (loading) {
        return (<div className="container py-10 space-y-6">
        <Skeleton className="h-96 w-full"/>
        <Skeleton className="h-10 w-1/2"/>
      </div>);
    }
    if (!c)
        return <p className="container py-20 text-center">Campana no encontrada.</p>;
    const pct = percent(c.recaudado, c.meta);
    const donate = async () => {
        // Obligar a iniciar sesion para donar.
        if (!user) {
            toast({ title: "Inicia sesion para donar" });
            navigate("/login", { state: { from: location.pathname } });
            return;
        }
        if (Number(amount) < 1000) {
            toast({ title: "El monto minimo es $1.000", variant: "destructive" });
            return;
        }
        setSubmitting(true);
        try {
            const { checkoutUrl } = await donationsApi.create({
                donorId: user.id,
                campaignId: c.id,
                amount: Number(amount),
            });
            if (checkoutUrl) {
                // Redirige a la pasarela de MercadoPago.
                window.location.href = checkoutUrl;
            } else {
                toast({ title: "No se pudo iniciar el pago", variant: "destructive" });
                setSubmitting(false);
            }
        } catch (err) {
            toast({ title: "Error", description: err.message, variant: "destructive" });
            setSubmitting(false);
        }
    };
    return (<>
      <div className="relative h-80 md:h-[480px] overflow-hidden">
        {c.imagen ? (<img src={c.imagen} alt={c.titulo} className="w-full h-full object-cover"/>) : (<div className="w-full h-full gradient-soft"/>)}
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/30 to-transparent"/>
        <div className="absolute top-6 left-6">
          <Button asChild variant="outline" size="sm" className="bg-card/80 backdrop-blur">
            <Link to="/crowdfundings"><ArrowLeft className="w-4 h-4"/> Volver</Link>
          </Button>
        </div>
      </div>

      <section className="container -mt-24 relative grid lg:grid-cols-3 gap-8 pb-16">
        <div className="lg:col-span-2 bg-card rounded-3xl border border-border shadow-card p-6 md:p-10 animate-fade-in-up">
          <span className="inline-block px-3 py-1 rounded-full bg-secondary text-xs font-semibold text-secondary-foreground">
            {c.categoria}
          </span>
          <h1 className="mt-3 font-display text-3xl md:text-5xl font-bold leading-tight">{c.titulo}</h1>

          {f && (<Link to={`/fundacion/${f.id}`} className="mt-4 inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-primary transition-smooth">
              <span className="w-8 h-8 rounded-full bg-secondary grid place-items-center text-lg overflow-hidden shrink-0">
                {isImageUrl(f.logo) ? (<img src={f.logo} alt={f.nombre} className="w-full h-full object-cover"/>) : (f.logo)}
              </span>
              Por <span className="font-semibold text-foreground">{f.nombre}</span>
            </Link>)}

          {youtubeEmbedUrl(c.video) && (
            <div className="mt-6 aspect-video rounded-2xl overflow-hidden border border-border bg-secondary">
              <iframe
                src={youtubeEmbedUrl(c.video)}
                title={`Video de ${c.titulo}`}
                className="w-full h-full"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowFullScreen
              />
            </div>
          )}

          <p className="mt-6 text-muted-foreground leading-relaxed whitespace-pre-line">{c.descripcion}</p>
        </div>

        {/* Columna derecha: cuadro de stats + tarjeta de donacion */}
        <div className="lg:sticky lg:top-28 self-start space-y-6">
          <div className="bg-card rounded-3xl border border-border shadow-card p-6 animate-scale-in">
            <div className="grid grid-cols-3 gap-3">
              {[
              { Icon: Users, n: donorCount ?? 0, l: "Donantes" },
              { Icon: Clock, n: c.diasRestantes ?? "-", l: "Dias restantes" },
              { Icon: Heart, n: `${pct}%`, l: "Completado" },
          ].map(({ Icon, n, l }) => (<div key={l} className="bg-secondary/50 rounded-2xl p-4 text-center">
                  <Icon className="w-5 h-5 mx-auto text-primary mb-1"/>
                  <p className="font-display text-2xl font-bold">{n}</p>
                  <p className="text-xs text-muted-foreground">{l}</p>
                </div>))}
            </div>
          </div>

          {/* Tarjeta de donacion */}
          <aside className="bg-card rounded-3xl border border-border shadow-card p-6 md:p-8 animate-scale-in">
          <p className="text-3xl md:text-4xl font-display font-bold text-primary">{formatCurrency(c.recaudado)}</p>
          <p className="text-sm text-muted-foreground">recaudados de <span className="font-semibold text-foreground">{formatCurrency(c.meta)}</span></p>

          <div className="my-5">
            <ProgressBar value={pct}/>
            <p className="mt-2 text-sm font-semibold text-primary">{pct}% completado</p>
          </div>

          {canDonate ? (<>
            <p className="text-sm font-semibold mb-2">Elige un monto</p>
            <div className="grid grid-cols-3 gap-2 mb-3">
              {[20000, 50000, 100000].map((v) => (<button key={v} onClick={() => setAmount(v)} className={`py-2 rounded-full text-sm font-semibold border transition-smooth ${amount === v
                  ? "bg-primary text-primary-foreground border-primary"
                  : "bg-card border-border hover:border-primary"}`}>
                  {formatCurrency(v)}
                </button>))}
            </div>
            <input type="number" min={1000} value={amount} onChange={(e) => setAmount(Number(e.target.value))} className="w-full h-12 rounded-full px-5 bg-secondary border border-border focus:outline-none focus:ring-2 focus:ring-ring text-sm"/>

            <Button onClick={donate} variant="hero" size="lg" className="w-full mt-4" disabled={submitting}>
              <Heart className="w-4 h-4 fill-current"/> {submitting ? "Redirigiendo..." : `Donar ${formatCurrency(amount)}`}
            </Button>
            <Button variant="ghost" size="sm" className="w-full mt-2">
              <Share2 className="w-4 h-4"/> Compartir
            </Button>
            <p className="text-xs text-muted-foreground text-center mt-4">
              100% de tu donacion llega a la fundacion.
            </p>
          </>) : (
            <p className="text-xs text-muted-foreground text-center">
              Solo las personas pueden donar a las campanas.
            </p>
          )}
          </aside>
        </div>
      </section>
    </>);
};
export default CrowdfundingDetail;
