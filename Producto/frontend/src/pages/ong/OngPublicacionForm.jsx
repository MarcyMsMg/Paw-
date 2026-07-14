import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { FeedPostForm } from "@/components/FeedPostForm";
import { adoptionsApi, animalToView, campaignsApi, campaignToView } from "@/services/authApi";
import { feedApi } from "@/services/feedApi";
import { toast } from "@/hooks/use-toast";

const OngPublicacionForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [initial, setInitial] = useState(null);
  const [loading, setLoading] = useState(Boolean(id));
  const [saving, setSaving] = useState(false);
  const [relatedAnimals, setRelatedAnimals] = useState([]);
  const [relatedCampaigns, setRelatedCampaigns] = useState([]);
  const [loadingRelated, setLoadingRelated] = useState(false);

  useEffect(() => {
    if (!id) return;
    feedApi.listMyFeedPosts()
      .then((posts) => setInitial(posts.find((post) => post.id === id) ?? null))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!user) return;
    setLoadingRelated(true);

    Promise.allSettled([
      adoptionsApi.listOwnedAnimals(),
      campaignsApi.list(user.id),
    ])
      .then(([animalsResult, campaignsResult]) => {
        if (animalsResult.status === "fulfilled") {
          setRelatedAnimals(animalsResult.value.map(animalToView));
        } else {
          setRelatedAnimals([]);
        }

        if (campaignsResult.status === "fulfilled") {
          setRelatedCampaigns(campaignsResult.value.map(campaignToView));
        } else {
          setRelatedCampaigns([]);
        }

        if (animalsResult.status === "rejected" || campaignsResult.status === "rejected") {
          toast({
            title: "No se cargaron todas las opciones",
            description: "Puedes guardar la publicacion sin relacionarla, o intentar nuevamente cuando los servicios esten disponibles.",
            variant: "destructive",
          });
        }
      })
      .finally(() => setLoadingRelated(false));
  }, [user]);

  const submit = async (payload) => {
    setSaving(true);
    try {
      if (id) await feedApi.updateFeedPost(id, payload);
      else await feedApi.createFeedPost(payload);
      toast({ title: id ? "Publicacion actualizada" : "Publicacion creada" });
      navigate("/ong/publicaciones");
    } catch (error) {
      showError(error);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p className="text-muted-foreground">Cargando publicacion...</p>;
  if (id && !initial) return <p className="text-muted-foreground">Publicacion no encontrada.</p>;

  return (
    <div className="space-y-6">
      <Button asChild variant="ghost" size="sm"><Link to="/ong/publicaciones"><ArrowLeft className="w-4 h-4" /> Volver</Link></Button>
      <header>
        <p className="text-sm font-semibold text-primary">Mi ONG</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">{id ? "Editar publicacion" : "Nueva publicacion"}</h1>
      </header>
      <FeedPostForm
        initial={initial}
        mode={id ? "edit" : "create"}
        saving={saving}
        relatedAnimals={relatedAnimals}
        relatedCampaigns={relatedCampaigns}
        loadingRelated={loadingRelated}
        onSubmit={submit}
        onCancel={() => navigate("/ong/publicaciones")}
      />
    </div>
  );
};

function showError(error) {
  toast({ title: "No fue posible guardar", description: error?.message, variant: "destructive" });
}

export default OngPublicacionForm;