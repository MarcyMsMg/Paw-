import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { ChartCard } from "@/components/charts/ChartCard";
import { formatCurrency } from "@/lib/format";

export function DonationsByMonthChart({ data = {}, title = "Donaciones por mes" }) {
  const rows = Object.entries(data).map(([month, amount]) => ({ month, amount: Number(amount ?? 0) }));
  return (
    <ChartCard title={title}>
      {rows.length === 0 ? (
        <p className="text-sm text-muted-foreground">Sin donaciones aprobadas aun.</p>
      ) : (
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={rows}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `$${value}`} />
              <Tooltip formatter={(value) => formatCurrency(value)} />
              <Line type="monotone" dataKey="amount" stroke="hsl(var(--primary))" strokeWidth={3} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </ChartCard>
  );
}