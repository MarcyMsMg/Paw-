import { useEffect, useState } from "react";
import { Search } from "lucide-react";
import { CardFeedPost } from "@/components/CardFeedPost";
import { CardSkeleton } from "@/components/Skeleton";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FEED_TYPE_LABELS, feedApi } from "@/services/feedApi";
import { toast } from "@/hooks/use-toast";

const Feed = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [type, setType] = useState("");
  const [search, setSearch] = useState("");
  const [submittedSearch, setSubmittedSearch] = useState("");

  useEffect(() => {
    setLoading(true);
    feedApi.listFeedPosts({ type, search: submittedSearch })
      .then(setPosts)
      .catch((error) => toast({ title: "No fue posible cargar el feed", description: error.message, variant: "destructive" }))
      .finally(() => setLoading(false));
  }, [type, submittedSearch]);

  const submitSearch = (event) => {
    event.preventDefault();
    setSubmittedSearch(search.trim());
  };

  return (
    <div className="container py-10 md:py-16 space-y-8">
      <header className="max-w-3xl">
        <p className="text-sm font-semibold text-primary mb-2">Actualizaciones</p>
        <h1 className="font-display text-4xl md:text-6xl font-bold">Historias y avances de las ONGs</h1>
        <p className="text-muted-foreground mt-3">Rescates, jornadas, urgencias, comunicados y finales felices publicados por fundaciones verificadas.</p>
      </header>

      <div className="bg-card rounded-2xl border border-border shadow-soft p-4 flex flex-col md:flex-row gap-3">
        <select value={type} onChange={(event) => setType(event.target.value)} className="h-11 rounded-xl border border-input bg-background px-3 text-sm md:w-64">
          <option value="">Todos los tipos</option>
          {Object.entries(FEED_TYPE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
        <form onSubmit={submitSearch} className="flex flex-1 gap-2">
          <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar actualizaciones..." className="h-11 rounded-xl" />
          <Button type="submit" variant="hero" className="h-11"><Search className="w-4 h-4" /> Buscar</Button>
        </form>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading
          ? Array.from({ length: 3 }).map((_, index) => <CardSkeleton key={index} />)
          : posts.map((post, index) => <CardFeedPost key={post.id} post={post} index={index} />)}
      </div>
      {!loading && posts.length === 0 && (
        <div className="border border-dashed border-border rounded-2xl py-16 text-center">
          <p className="font-semibold">No hay publicaciones para mostrar.</p>
          <p className="text-sm text-muted-foreground mt-1">Prueba quitando filtros o vuelve mas tarde.</p>
        </div>
      )}
    </div>
  );
};

export default Feed;
