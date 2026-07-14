import { Link, useParams } from "react-router-dom";
import { ArrowLeft, CalendarDays, Megaphone, PawPrint } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/Skeleton";
import { useFetch } from "@/hooks/useFetch";
import { FEED_TYPE_LABELS, feedApi, feedPostToView } from "@/services/feedApi";

const FeedPostDetail = () => {
  const { id = "" } = useParams();
  const { data, loading, error } = useFetch(() => feedApi.getFeedPostById(id), [id]);

  if (loading) {
    return (
      <div className="container py-10 space-y-6">
        <Skeleton className="h-72 w-full" />
        <Skeleton className="h-10 w-2/3" />
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  if (error || !data) {
    return <p className="container py-20 text-center text-muted-foreground">Publicacion no encontrada.</p>;
  }

  const post = feedPostToView(data);

  return (
    <article className="container py-10 md:py-16 space-y-8">
      <Button asChild variant="ghost" size="sm"><Link to="/feed"><ArrowLeft className="w-4 h-4" /> Volver al feed</Link></Button>

      <header className="max-w-4xl space-y-4">
        <Badge>{FEED_TYPE_LABELS[post.type] ?? post.type}</Badge>
        <h1 className="font-display text-4xl md:text-6xl font-bold leading-tight">{post.title}</h1>
        <p className="text-sm text-muted-foreground flex flex-wrap items-center gap-2">
          <CalendarDays className="w-4 h-4" /> {formatDate(post.fecha)}
          <span>·</span>
          <span>{post.ongNombre}</span>
        </p>
        {post.summary && <p className="text-lg text-muted-foreground">{post.summary}</p>}
      </header>

      {post.imageUrls?.length > 0 && (
        <section className="grid md:grid-cols-2 gap-4">
          {post.imageUrls.map((url, index) => (
            <div key={url} className={index === 0 ? "md:col-span-2 rounded-2xl overflow-hidden bg-secondary" : "rounded-2xl overflow-hidden bg-secondary"}>
              <img src={url} alt={`${post.title} ${index + 1}`} className={index === 0 ? "w-full h-[420px] object-cover" : "w-full h-56 object-cover"} />
            </div>
          ))}
        </section>
      )}

      <section className="max-w-4xl">
        <div className="prose prose-neutral max-w-none text-foreground">
          {post.content.split("\n").map((paragraph, index) => (
            <p key={index} className="text-muted-foreground leading-relaxed whitespace-pre-line">{paragraph}</p>
          ))}
        </div>
      </section>

      {post.videoUrl && (
        <section className="max-w-4xl rounded-2xl border border-border bg-card p-5">
          <h2 className="font-display text-2xl font-bold mb-2">Video</h2>
          <a href={post.videoUrl} target="_blank" rel="noreferrer" className="text-primary font-semibold break-all">{post.videoUrl}</a>
        </section>
      )}

      {(post.relatedAnimalId || post.relatedCampaignId) && (
        <section className="flex flex-wrap gap-3">
          {post.relatedAnimalId && <Button asChild variant="soft"><Link to={`/animales/${post.relatedAnimalId}`}><PawPrint className="w-4 h-4" /> Ver animal relacionado</Link></Button>}
          {post.relatedCampaignId && <Button asChild variant="soft"><Link to={`/crowdfunding/${post.relatedCampaignId}`}><Megaphone className="w-4 h-4" /> Ver campana relacionada</Link></Button>}
        </section>
      )}
    </article>
  );
};

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString() : "Sin fecha";
}

export default FeedPostDetail;
