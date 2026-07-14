import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { PawPrint, Megaphone, ClipboardList, Coins } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/contexts/AuthContext";
import { adoptionsApi, animalToView, publicCampaignsToViewWithDonors } from "@/services/authApi";
import { dashboardApi } from "@/services/dashboardApi";
import { StatCard } from "@/components/StatCard";
import { Button } from "@/components/ui/button";
import { CardCampaignLite } from "@/components/CardCampaignLite";
import { CardAnimal } from "@/components/CardAnimal";
import { formatCurrency } from "@/lib/format";
import { ApplicationStatusChart } from "@/components/charts/ApplicationStatusChart";
import { DonationsByMonthChart } from "@/components/charts/DonationsByMonthChart";
import { LatestNotifications } from "@/components/LatestNotifications";

const pickRandom = (items, count) => {
  const copy = [...items];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy.slice(0, count);
};

const fallback = {
  adoptions: { totalApplications: 0, applicationsByStatus: {} },
  donations: { approvedDonations: 0, totalDonated: 0, donationsByMonth: {} },
  notifications: [],
};

const PersonaDashboard = () => {
  const { user } = useAuth();
  const [animales, setAnimales] = useState([]);
  const [camps, setCamps] = useState([]);
  const query = useQuery({
    queryKey: ["dashboard", "person", user?.id],
    queryFn: () => dashboardApi.person(user.id),
    enabled: Boolean(user?.id),
    refetchInterval: 30000,
    retry: false,
  });
  const data = query.data ?? fallback;

  useEffect(() => {
    if (!user) return;
    adoptionsApi.listAnimals({ status: "AVAILABLE" })
      .then((items) => setAnimales(pickRandom(items.map(animalToView), 3)))
      .catch(() => setAnimales([]));

    publicCampaignsToViewWithDonors()
      .then((cs) => cs.sort((a, b) => (a.diasRestantes ?? Infinity) - (b.diasRestantes ?? Infinity)).slice(0, 3))
      .then(setCamps)
      .catch(() => setCamps([]));
  }, [user]);

  return (
    <div className="space-y-8">
      <header>
        <p className="text-sm font-semibold text-primary">Tu espacio</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Hola, {user?.nombre}</h1>
        <p className="text-muted-foreground mt-1">Aqui estan las causas que te necesitan hoy.</p>
      </header>

      <div className="grid sm:grid-cols-3 gap-4">
        <StatCard icon={ClipboardList} label="Mis postulaciones" value={query.isLoading ? "..." : data.adoptions.totalApplications} tone="primary" />
        <StatCard icon={Coins} label="Mis donaciones" value={query.isLoading ? "..." : data.donations.approvedDonations} tone="warm" />
        <StatCard icon={PawPrint} label="Total donado" value={query.isLoading ? "..." : formatCurrency(Number(data.donations.totalDonated ?? 0))} tone="blue" />
      </div>

      <div className="grid lg:grid-cols-2 gap-5">
        <ApplicationStatusChart data={data.adoptions.applicationsByStatus} title="Estado de mis postulaciones" />
        <DonationsByMonthChart data={data.donations.donationsByMonth} />
        <LatestNotifications items={data.notifications} loading={query.isLoading} />
      </div>

      <section>
        <div className="flex items-end justify-between mb-4">
          <h2 className="font-display text-2xl font-bold">Animales sugeridos</h2>
          <Button asChild variant="ghost" size="sm"><Link to="/animales">Ver todos</Link></Button>
        </div>
        <div className="grid sm:grid-cols-3 gap-4">{animales.map((a) => <CardAnimal key={a.id} a={a} />)}</div>
        {animales.length === 0 && <p className="text-muted-foreground text-sm">No hay animales disponibles por ahora.</p>}
      </section>

      <section>
        <div className="flex items-end justify-between mb-4">
          <h2 className="font-display text-2xl font-bold">Campanas recomendadas</h2>
          <Button asChild variant="ghost" size="sm"><Link to="/crowdfundings">Ver todas</Link></Button>
        </div>
        <div className="grid sm:grid-cols-3 gap-4">{camps.map((c) => <CardCampaignLite key={c.id} c={c} />)}</div>
        {camps.length === 0 && <p className="text-muted-foreground text-sm">No hay campanas activas por ahora.</p>}
      </section>

      <section className="flex flex-wrap gap-3">
        <Button asChild variant="soft"><Link to="/persona/mis-postulaciones"><Megaphone className="w-4 h-4" /> Mis postulaciones</Link></Button>
        <Button asChild variant="soft"><Link to="/persona/mis-donaciones"><Coins className="w-4 h-4" /> Mis donaciones</Link></Button>
      </section>
    </div>
  );
};

export default PersonaDashboard;