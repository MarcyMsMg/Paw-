import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Calendar, Heart, MapPin, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CardCampaign } from "@/components/CardCampaign";
import { CardFeedPost } from "@/components/CardFeedPost";
import { CardSkeleton, Skeleton } from "@/components/Skeleton";
import { useFetch } from "@/hooks/useFetch";
import { ngosApi, ngoToFundacion, isImageUrl, publicCampaignsToViewWithDonors } from "@/services/authApi";
import { feedApi } from "@/services/feedApi";
const FundacionDetail = () => {
    const { id = "" } = useParams();
    const { data: f, loading } = useFetch(
        async () => ngoToFundacion(await ngosApi.getById(id)),
        [id]
    );
    const { data: camps, loading: lc } = useFetch(() => publicCampaignsToViewWithDonors(id), [id]);
    const { data: posts, loading: lp } = useFetch(() => feedApi.listFeedPosts({ ngoId: id }), [id]);
    if (loading) {
        return (<div className="container py-10 space-y-6">
        <Skeleton className="h-72 w-full"/>
        <Skeleton className="h-10 w-1/2"/>
        <Skeleton className="h-32 w-full"/>
      </div>);
    }
    if (!f)
        return <p className="container py-20 text-center">Fundacion no encontrada.</p>;
    return (<>
      <div className="relative h-72 md:h-96 overflow-hidden">
        {f.banner ? (
          <img src={f.banner} alt={f.nombre} className="w-full h-full object-cover"/>
        ) : (
          <div className="w-full h-full gradient-soft"/>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/40 to-transparent"/>
        <div className="absolute top-6 left-6">
          <Button asChild variant="outline" size="sm" className="bg-card/80 backdrop-blur">
            <Link to="/fundaciones"><ArrowLeft className="w-4 h-4"/> Volver</Link>
          </Button>
        </div>
      </div>

      <section className="container -mt-20 relative">
        <div className="bg-card rounded-3xl border border-border shadow-card p-6 md:p-10 animate-fade-in-up">
          <div className="flex flex-col md:flex-row md:items-center gap-6">
            <div className="w-20 h-20 rounded-2xl gradient-soft border border-border overflow-hidden shadow-soft">
              {isImageUrl(f.logo) ? (
                <img src={f.logo} alt={f.nombre} className="w-full h-full object-cover"/>
              ) : (
                <div className="w-full h-full grid place-items-center text-5xl">{f.logo || "P+"}</div>
              )}
            </div>
            <div className="flex-1">
              <h1 className="font-display text-3xl md:text-5xl font-bold">{f.nombre}</h1>
              <p className="mt-2 flex items-center gap-1 text-sm text-muted-foreground">
                <MapPin className="w-4 h-4"/> {f.ubicacion} - Desde {f.fundada}
              </p>
            </div>
            <Button variant="hero" size="lg">
              <Heart className="w-4 h-4"/> Seguir
            </Button>
          </div>

          <div className="grid grid-cols-3 gap-4 mt-8 pt-8 border-t border-border">
            {[
            { Icon: Heart, n: f.rescatados.toLocaleString(), l: "Rescatados" },
            { Icon: Users, n: f.voluntarios.toString(), l: "Voluntarios" },
            { Icon: Calendar, n: (new Date().getFullYear() - f.fundada).toString(), l: "Anios activos" },
        ].map(({ Icon, n, l }) => (<div key={l} className="text-center">
                <Icon className="w-5 h-5 mx-auto text-primary mb-1"/>
                <p className="font-display text-2xl md:text-3xl font-bold">{n}</p>
                <p className="text-xs text-muted-foreground">{l}</p>
              </div>))}
          </div>

          <p className="mt-8 text-muted-foreground leading-relaxed">{f.descripcion}</p>
        </div>
      </section>

      <section className="container py-16">
        <h2 className="font-display text-3xl md:text-4xl font-bold mb-8">Campanas activas</h2>
        {lc ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 2 }).map((_, i) => <CardSkeleton key={i}/>)}
          </div>) : camps && camps.length > 0 ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {camps.map((c, i) => <CardCampaign key={c.id} c={c} index={i}/>)}
          </div>) : (<p className="text-muted-foreground">Esta fundacion no tiene campanas activas.</p>)}
      </section>

      <section className="container pb-16">
        <h2 className="font-display text-3xl md:text-4xl font-bold mb-8">Actualizaciones de la ONG</h2>
        {lp ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 2 }).map((_, i) => <CardSkeleton key={i}/>) }
          </div>) : posts && posts.length > 0 ? (<div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {posts.slice(0, 3).map((post, i) => <CardFeedPost key={post.id} post={post} index={i}/>) }
          </div>) : (<p className="text-muted-foreground">Esta fundacion aun no tiene actualizaciones publicadas.</p>)}
      </section>
    </>);
};
export default FundacionDetail;
