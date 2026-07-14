import { Link } from "react-router-dom";
import { ClipboardList, Building2, Users, UserCheck } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { StatCard } from "@/components/StatCard";
import { dashboardApi } from "@/services/dashboardApi";
import { Button } from "@/components/ui/button";
import { ApplicationStatusChart } from "@/components/charts/ApplicationStatusChart";
import { CampaignProgressChart } from "@/components/charts/CampaignProgressChart";
import { UsersByRoleChart } from "@/components/charts/UsersByRoleChart";
import { DonationsByMonthChart } from "@/components/charts/DonationsByMonthChart";
import { LatestNotifications } from "@/components/LatestNotifications";

const fallback = {
  users: { pendingNgoRequests: 0, activeNgos: 0, naturalPersons: 0, activeUsers: 0, usersByRole: {}, usersByStatus: {} },
  adoptions: { applicationsByStatus: {} },
  campaigns: { campaignProgress: [] },
  donations: { donationsByMonth: {} },
  notifications: [],
};

const AdminDashboard = () => {
  const query = useQuery({
    queryKey: ["dashboard", "admin"],
    queryFn: dashboardApi.admin,
    refetchInterval: 30000,
    retry: false,
  });
  const data = query.data ?? fallback;

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-primary">Panel de administracion</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Resumen general</h1>
        </div>
        <Button asChild variant="hero"><Link to="/admin/solicitudes-ong">Revisar solicitudes</Link></Button>
      </header>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={ClipboardList} label="Solicitudes pendientes" value={query.isLoading ? "..." : data.users.pendingNgoRequests} hint="ONGs por revisar" tone="warm" />
        <StatCard icon={Building2} label="ONGs activas" value={query.isLoading ? "..." : data.users.activeNgos} tone="primary" />
        <StatCard icon={Users} label="Personas registradas" value={query.isLoading ? "..." : data.users.naturalPersons} tone="blue" />
        <StatCard icon={UserCheck} label="Usuarios activos" value={query.isLoading ? "..." : data.users.activeUsers} tone="rose" />
      </div>

      <div className="grid lg:grid-cols-2 gap-5">
        <UsersByRoleChart data={data.users.usersByRole} />
        <UsersByRoleChart data={data.users.usersByStatus} title="Usuarios por estado" />
        <ApplicationStatusChart data={data.adoptions.applicationsByStatus} title="Adopciones por estado" />
        <CampaignProgressChart data={data.campaigns.campaignProgress} />
        <DonationsByMonthChart data={data.donations.donationsByMonth} />
        <LatestNotifications items={data.notifications} loading={query.isLoading} />
      </div>

      <div className="bg-card rounded-2xl border border-border shadow-soft p-6">
        <h2 className="font-display text-xl font-bold mb-4">Acciones rapidas</h2>
        <div className="grid sm:grid-cols-3 gap-3">
          <Button asChild variant="soft"><Link to="/admin/solicitudes-ong">Solicitudes ONG</Link></Button>
          <Button asChild variant="soft"><Link to="/admin/ongs">ONGs aprobadas</Link></Button>
          <Button asChild variant="soft"><Link to="/admin/usuarios">Usuarios</Link></Button>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;