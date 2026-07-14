export function ChartCard({ title, description, children }) {
  return (
    <section className="bg-card rounded-2xl border border-border shadow-soft p-5 min-h-[260px]">
      <div className="mb-4">
        <h2 className="font-display text-xl font-bold">{title}</h2>
        {description && <p className="text-sm text-muted-foreground mt-1">{description}</p>}
      </div>
      {children}
    </section>
  );
}