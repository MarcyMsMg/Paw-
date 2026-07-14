import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { ChartCard } from "@/components/charts/ChartCard";

const COLORS = ["hsl(var(--primary))", "hsl(var(--accent))", "#60a5fa", "#fb7185", "#94a3b8"];
const labels = {
  ADMIN: "Admin",
  NGO: "ONG",
  NATURAL_PERSON: "Personas",
  ACTIVE: "Activos",
  INACTIVE: "Inactivos",
  SUSPENDED: "Suspendidos",
  PENDING: "Pendientes",
};

export function UsersByRoleChart({ data = {}, title = "Usuarios por rol" }) {
  const rows = Object.entries(data)
    .filter(([, value]) => Number(value) > 0)
    .map(([name, value]) => ({ name: labels[name] ?? name, value }));
  return (
    <ChartCard title={title}>
      {rows.length === 0 ? (
        <p className="text-sm text-muted-foreground">Sin datos para mostrar.</p>
      ) : (
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={rows} dataKey="value" nameKey="name" outerRadius={82} label>
                {rows.map((entry, index) => <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      )}
    </ChartCard>
  );
}