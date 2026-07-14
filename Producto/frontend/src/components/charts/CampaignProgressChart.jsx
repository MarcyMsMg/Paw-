import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { ChartCard } from "@/components/charts/ChartCard";

export function CampaignProgressChart({ data = [], title = "Progreso de campanas" }) {
  const rows = data.slice(0, 6).map((item) => ({
    name: item.title?.length > 18 ? `${item.title.slice(0, 18)}...` : item.title,
    progress: Number(item.progress ?? 0),
  }));
  return (
    <ChartCard title={title}>
      {rows.length === 0 ? (
        <p className="text-sm text-muted-foreground">Sin campanas para mostrar.</p>
      ) : (
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={rows}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `${value}%`} />
              <Tooltip formatter={(value) => `${Number(value).toFixed(1)}%`} />
              <Bar dataKey="progress" fill="hsl(var(--accent))" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </ChartCard>
  );
}