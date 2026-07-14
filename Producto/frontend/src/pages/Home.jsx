import { Link } from "react-router-dom";
import { ArrowRight, Heart, Shield, Sparkles, PawPrint, TrendingUp, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CardFundacion } from "@/components/CardFundacion";
import { CardCampaign } from "@/components/CardCampaign";
import { CardFeedPost } from "@/components/CardFeedPost";
import { CardSkeleton } from "@/components/Skeleton";
import { useFetch } from "@/hooks/useFetch";
import { ngosApi, ngoToFundacion, publicCampaignsToViewWithDonors } from "@/services/authApi";
import { feedApi } from "@/services/feedApi";
import heroImg from "@/assets/hero-pets.jpg";
const Home = () => {
    const { data: fundaciones, loading: lf } = useFetch(
        async () => (await ngosApi.list()).map(ngoToFundacion),
        []
    );
    const { data: campaigns, loading: lc } = useFetch(
        async () => publicCampaignsToViewWithDonors(),
        []
    );
    const { data: feedPosts, loading: lfeed } = useFetch(
        async () => (await feedApi.listFeedPosts()).slice(0, 3),
        []
    );
    return (<>
      {/* Portada */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 gradient-soft -z-10"/>
        <div className="absolute -top-32 -right-32 w-[500px] h-[500px] rounded-full bg-primary/10 blur-3xl -z-10"/>
        <div className="absolute -bottom-40 -left-32 w-[400px] h-[400px] rounded-full bg-accent/15 blur-3xl -z-10"/>

        <div className="container grid md:grid-cols-2 gap-12 items-center py-16 md:py-24">
          <div className="space-y-6 animate-fade-in-up">
            <span className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-card border border-border text-xs font-semibold shadow-soft">
              <Sparkles className="w-3.5 h-3.5 text-primary"/>
              Plataforma #1 de rescate animal en LATAM
            </span>
            <h1 className="font-display text-5xl md:text-7xl font-bold leading-[1.05] tracking-tight">
              Cada donación es una <span className="text-gradient">segunda oportunidad</span>.
            </h1>
            <p className="text-lg text-muted-foreground max-w-lg">
              Conectamos personas con ONGs de rescate animal verificadas. Apoya campañas reales, sigue su impacto y cambia historias para siempre.
            </p>
            <div className="flex flex-col sm:flex-row gap-3">
              <Button asChild size="xl" variant="hero">
                <Link to="/animales">Ver animales en adopción <ArrowRight className="w-4 h-4"/></Link>
              </Button>
              <Button asChild size="xl" variant="warm">
                <Link to="/crowdfundings">Donar <Heart className="w-4 h-4 fill-current"/></Link>
              </Button>
              <Button asChild size="xl" variant="outline">
                <Link to="/solicitud-ong">Soy una ONG</Link>
              </Button>
            </div>
            <div className="flex gap-8 pt-4">
              {[
            { n: "6.000+", l: "Animales rescatados" },
            { n: "120+", l: "ONGs aliadas" },
            { n: "$420M", l: "Recaudados" },
        ].map((s) => (<div key={s.l}>
                  <p className="font-display text-2xl md:text-3xl font-bold text-primary">{s.n}</p>
                  <p className="text-xs text-muted-foreground">{s.l}</p>
                </div>))}
            </div>
          </div>

          <div className="relative animate-scale-in">
            <div className="absolute -inset-4 gradient-hero opacity-20 blur-3xl rounded-full"/>
            <div className="relative rounded-3xl overflow-hidden shadow-glow border-8 border-card">
              <img src={heroImg} alt="Mascotas rescatadas felices" width={1600} height={1200} className="w-full h-[480px] object-cover"/>
            </div>
            <div className="absolute -bottom-6 -left-6 bg-card rounded-2xl p-4 shadow-card border border-border animate-float">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full grid place-items-center gradient-warm">
                  <Heart className="w-5 h-5 text-accent-foreground fill-accent-foreground"/>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Donación reciente</p>
                  <p className="text-sm font-bold">María donó $150.000</p>
                </div>
              </div>
            </div>
            <div className="absolute -top-4 -right-4 bg-card rounded-2xl p-4 shadow-card border border-border animate-float" style={{ animationDelay: "1s" }}>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full grid place-items-center bg-primary/10">
                  <TrendingUp className="w-5 h-5 text-primary"/>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Esta semana</p>
                  <p className="text-sm font-bold">+1.240 donantes</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CAMPANAS DESTACADAS */}
      <section className="container py-16 md:py-24">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-10">
          <div>
            <p className="text-sm font-semibold text-primary mb-2">Campañas destacadas</p>
            <h2 className="font-display text-3xl md:text-5xl font-bold">Historias que necesitan tu ayuda hoy</h2>
          </div>
          <Button asChild variant="ghost">
            <Link to="/crowdfundings">Ver todas <ArrowRight className="w-4 h-4"/></Link>
          </Button>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {lc
            ? Array.from({ length: 3 }).map((_, i) => <CardSkeleton key={i}/>)
            : campaigns?.slice(0, 3).map((c, i) => <CardCampaign key={c.id} c={c} index={i}/>)}
        </div>
      </section>


      {/* ACTUALIZACIONES */}
      <section className="container py-16 md:py-20">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-10">
          <div>
            <p className="text-sm font-semibold text-primary mb-2">Actualizaciones</p>
            <h2 className="font-display text-3xl md:text-5xl font-bold">Lo ultimo desde las ONGs</h2>
          </div>
          <Button asChild variant="ghost">
            <Link to="/feed">Ver feed <ArrowRight className="w-4 h-4"/></Link>
          </Button>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {lfeed
            ? Array.from({ length: 3 }).map((_, i) => <CardSkeleton key={i}/>)
            : feedPosts?.map((post, i) => <CardFeedPost key={post.id} post={post} index={i}/>)}
        </div>
      </section>
      {/* FUNDACIONES */}
      <section className="container py-16 md:py-20">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-10">
          <div>
            <p className="text-sm font-semibold text-primary mb-2">ONGs verificadas</p>
            <h2 className="font-display text-3xl md:text-5xl font-bold">Fundaciones que cambian vidas</h2>
          </div>
          <Button asChild variant="ghost">
            <Link to="/fundaciones">Ver todas <ArrowRight className="w-4 h-4"/></Link>
          </Button>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {lf
            ? Array.from({ length: 3 }).map((_, i) => <CardSkeleton key={i}/>)
            : fundaciones?.slice(0, 3).map((f, i) => <CardFundacion key={f.id} f={f} index={i}/>)}
        </div>
      </section>

      {/* QUE ES PAW+ */}
      <section className="container py-16 md:py-24">
        <div className="bg-card rounded-3xl border border-border shadow-card p-8 md:p-16 grid md:grid-cols-2 gap-12 items-center relative overflow-hidden">
          <div className="absolute -top-20 -right-20 w-80 h-80 rounded-full gradient-hero opacity-10 blur-3xl"/>
          <div className="space-y-6 relative">
            <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-secondary text-xs font-semibold">
              <PawPrint className="w-3.5 h-3.5 text-primary"/> Qué es Paw+
            </span>
            <h2 className="font-display text-3xl md:text-5xl font-bold">
              Transparencia, impacto y comunidad.
            </h2>
            <p className="text-muted-foreground">
              Paw+ es la plataforma donde ONGs de rescate animal publican sus campañas y reciben donaciones de forma segura. Verificamos cada fundación, mostramos métricas reales y te conectamos con causas que importan.
            </p>
            <Button asChild variant="hero" size="lg">
              <Link to="/register">Unete ahora</Link>
            </Button>
          </div>

          <div className="grid sm:grid-cols-2 gap-4 relative">
            {[
            { Icon: Shield, t: "ONGs verificadas", d: "Cada fundación pasa por un proceso de validación." },
            { Icon: TrendingUp, t: "Impacto medible", d: "Sigue cómo se usa cada peso donado." },
            { Icon: Users, t: "Comunidad activa", d: "Miles de donantes apoyando causas." },
            { Icon: Heart, t: "100% para los peluditos", d: "Sin comisiones ocultas en tu donación." },
        ].map(({ Icon, t, d }) => (<div key={t} className="bg-secondary/50 rounded-2xl p-5 hover:bg-secondary hover:-translate-y-1 transition-smooth">
                <div className="w-10 h-10 rounded-xl gradient-hero grid place-items-center text-primary-foreground mb-3">
                  <Icon className="w-5 h-5"/>
                </div>
                <h3 className="font-bold mb-1">{t}</h3>
                <p className="text-sm text-muted-foreground">{d}</p>
              </div>))}
          </div>
        </div>
      </section>

      {/* Llamada a la acción final */}
      <section className="container py-16">
        <div className="rounded-3xl gradient-hero p-10 md:p-16 text-center text-primary-foreground relative overflow-hidden">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.2),transparent_50%)]"/>
          <div className="relative space-y-5 max-w-2xl mx-auto">
            <h2 className="font-display text-4xl md:text-5xl font-bold">¿Listo para cambiar una vida?</h2>
            <p className="opacity-90">Una donación, un voluntariado, una difusión. Cada acción cuenta.</p>
            <Button asChild size="xl" variant="warm">
              <Link to="/crowdfundings">Donar ahora <Heart className="w-4 h-4 fill-current"/></Link>
            </Button>
          </div>
        </div>
      </section>
    </>);
};
export default Home;
