import { Link } from "react-router-dom";
import { CalendarDays } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { feedPostToView } from "@/services/feedApi";

export function CardFeedPost({ post, index = 0 }) {
  const view = feedPostToView(post);

  return (
    <article
      className="group bg-card rounded-2xl border border-border overflow-hidden shadow-soft hover:shadow-card hover:-translate-y-1 transition-smooth animate-fade-in-up flex flex-col"
      style={{ animationDelay: `${index * 60}ms` }}
    >
      <div className="relative h-48 bg-secondary overflow-hidden">
        {view.imagen ? (
          <img src={view.imagen} alt={view.title} loading="lazy" className="w-full h-full object-cover group-hover:scale-110 transition-smooth duration-700" />
        ) : (
          <div className="w-full h-full gradient-soft" />
        )}
        <Badge className="absolute top-3 left-3 bg-background/90 text-foreground hover:bg-background/90">{view.tipo}</Badge>
      </div>
      <div className="p-5 flex flex-col flex-1">
        <p className="text-xs text-muted-foreground flex items-center gap-1">
          <CalendarDays className="w-3.5 h-3.5" />
          {formatDate(view.fecha)} · {view.ongNombre}
        </p>
        <h3 className="font-display font-bold text-xl leading-snug line-clamp-2 mt-2">{view.title}</h3>
        <p className="mt-2 text-sm text-muted-foreground line-clamp-3">{view.resumen}</p>
        <Button asChild variant="hero" className="w-full mt-5">
          <Link to={`/feed/${view.id}`}>Leer actualizacion</Link>
        </Button>
      </div>
    </article>
  );
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString() : "Sin fecha";
}
