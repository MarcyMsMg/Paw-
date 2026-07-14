import { Link } from "react-router-dom";
import { Coins, Megaphone, PawPrint, ClipboardList } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/contexts/AuthContext";
import { dashboardApi } from "@/services/dashboardApi";
import { StatCard } from "@/components/StatCard";
import { Button } from "@/components/ui/button";
import { formatCurrency } from "@/lib/format";
import { ApplicationStatusChart } from "@/components/charts/ApplicationStatusChart";
import { CampaignProgressChart } from "@/components/charts/CampaignProgressChart";
import { DonationsByMonthChart } from "@/components/charts/DonationsByMonthChart";
import { LatestNotifications } from "@/components/LatestNotifications";

const fallback = {
  adoptions: { pendingApplications: 0, animalsAvailable: 0, applicationsByStatus: {} },
  campaigns: { activeCampaigns: 0, campaignProgress: [] },
  donations: { totalRaised: 0, donationsByMonth: {} },
  notifications: [],
};

const OngDashboard = () => {
  const { user } = useAuth();
  const query = useQuery({
    queryKey: ["dashboard", "ngo", user?.id],
    queryFn: () => dashboardApi.ngo(user.id),
    enabled: Boolean(user?.id),
    refetchInterval: 30000,
    retry: false,
  });
  const data = query.data ?? fallback;

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-primary">Panel ONG</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Hola, {user?.nombre}</h1>
        </div>
        <Button asChild variant="hero"><Link to="/ong/campanas/nueva">Nueva campana</Link></Button>
      </header>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={Coins} label="Total recaudado" value={query.isLoading ? "..." : formatCurrency(Number(data.donations.totalRaised ?? 0))} tone="warm" />
        <StatCard icon={Megaphone} label="Campanas activas" value={query.isLoading ? "..." : data.campaigns.activeCampaigns} tone="primary" />
        <StatCard icon={PawPrint} label="Animales disponibles" value={query.isLoading ? "..." : data.adoptions.animalsAvailable} tone="blue" />
        <StatCard icon={ClipboardList} label="Adopciones pendientes" value={query.isLoading ? "..." : data.adoptions.pendingApplications} tone="rose" />
      </div>

      <div className="grid lg:grid-cols-2 gap-5">
        <ApplicationStatusChart data={data.adoptions.applicationsByStatus} />
        <CampaignProgressChart data={data.campaigns.campaignProgress} />
        <DonationsByMonthChart data={data.donations.donationsByMonth} />
        <LatestNotifications items={data.notifications} loading={query.isLoading} />
      </div>
    </div>
  );
};

export default OngDashboard;