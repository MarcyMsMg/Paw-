import { cn } from "@/lib/utils";
const tones = {
    primary: "from-primary/15 to-primary/5 text-primary",
    warm: "from-accent/20 to-accent/5 text-accent-foreground",
    blue: "from-blue-500/15 to-blue-500/5 text-blue-600",
    rose: "from-rose-500/15 to-rose-500/5 text-rose-600",
};
export function StatCard({ icon: Icon, label, value, hint, tone = "primary", className }) {
    return (<div className={cn("relative bg-card rounded-2xl border border-border shadow-soft p-5 overflow-hidden hover:shadow-card transition-smooth animate-fade-in-up", className)}>
      <div className={cn("absolute -top-8 -right-8 w-28 h-28 rounded-full bg-gradient-to-br opacity-60 blur-2xl", tones[tone])}/>
      <div className="relative flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">{label}</p>
          <p className="font-display text-3xl font-bold mt-2">{value}</p>
          {hint && <p className="text-xs text-muted-foreground mt-1">{hint}</p>}
        </div>
        <div className={cn("w-11 h-11 rounded-xl grid place-items-center bg-gradient-to-br", tones[tone])}>
          <Icon className="w-5 h-5"/>
        </div>
      </div>
    </div>);
}
