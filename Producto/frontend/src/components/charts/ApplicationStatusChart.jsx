import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { ChartCard } from "@/components/charts/ChartCard";

const labels = {
  PENDING: "Pendientes",
  INFO_REQUESTED: "Info",
  ACCEPTED: "Aceptadas",
  REJECTED: "Rechazadas",
};

export function ApplicationStatusChart({ data = {}, title = "Postulaciones por estado" }) {
  const rows = Object.entries(data).map(([name, value]) => ({ name: labels[name] ?? name, value }));
  return (
    <ChartCard title={title}>
      {rows.length === 0 ? (
        <p className="text-sm text-muted-foreground">Sin datos para mostrar.</p>
      ) : (
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={rows}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="value" fill="hsl(var(--primary))" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </ChartCard>
  );
}