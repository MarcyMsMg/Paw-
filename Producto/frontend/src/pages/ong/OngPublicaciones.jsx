import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Edit, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/StatusBadge";
import { feedApi, feedPostToView } from "@/services/feedApi";
import { toast } from "@/hooks/use-toast";

const OngPublicaciones = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const reload = () => feedApi.listMyFeedPosts().then((posts) => setItems(posts.map(feedPostToView)));

  useEffect(() => {
    reload()
      .catch(showError)
      .finally(() => setLoading(false));
  }, []);

  const publish = async (post) => {
    try {
      await feedApi.publishFeedPost(post.id);
      toast({ title: "Publicacion publicada" });
      await reload();
    } catch (error) {
      showError(error);
    }
  };

  const archive = async (post) => {
    try {
      await feedApi.archiveFeedPost(post.id);
      toast({ title: "Publicacion archivada" });
      await reload();
    } catch (error) {
      showError(error);
    }
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-primary">Mi ONG</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Publicaciones</h1>
        </div>
        <Button asChild variant="hero"><Link to="/ong/publicaciones/nueva"><Plus className="w-4 h-4" /> Nueva publicacion</Link></Button>
      </header>

      <div className="space-y-3">
        {items.map((post) => (
          <article key={post.id} className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden">
            <div className="p-5 flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-4 min-w-[240px] flex-1">
                <div className="w-20 h-16 rounded-xl bg-secondary overflow-hidden shrink-0">
                  {post.imagen && <img src={post.imagen} alt={post.title} className="w-full h-full object-cover" />}
                </div>
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-display font-bold text-lg">{post.title}</h2>
                    <StatusBadge value={post.estado} />
                  </div>
                  <p className="text-sm text-muted-foreground line-clamp-1">{post.tipo} · {formatDate(post.fecha)}</p>
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button asChild size="sm" variant="soft"><Link to={`/ong/publicaciones/editar/${post.id}`}><Edit className="w-3.5 h-3.5" /> Editar</Link></Button>
                {post.status === "DRAFT" && <Button size="sm" variant="hero" onClick={() => publish(post)}>Publicar</Button>}
                {post.status === "PUBLISHED" && <Button size="sm" variant="outline" onClick={() => archive(post)}>Archivar</Button>}
              </div>
            </div>
          </article>
        ))}
        {!loading && items.length === 0 && (
          <div className="border border-dashed border-border rounded-2xl py-14 text-center">
            <p className="font-semibold">Aun no tienes publicaciones.</p>
            <p className="text-sm text-muted-foreground mt-1">Crea rescates, avances o comunicados para tu comunidad.</p>
          </div>
        )}
        {loading && <p className="text-muted-foreground">Cargando publicaciones...</p>}
      </div>
    </div>
  );
};

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString() : "Sin fecha";
}

function showError(error) {
  toast({ title: "No fue posible completar la operacion", description: error?.message, variant: "destructive" });
}

export default OngPublicaciones;
