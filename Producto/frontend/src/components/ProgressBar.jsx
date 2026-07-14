import { cn } from "@/lib/utils";
export function ProgressBar({ value, className, showLabel }) {
    const pct = Math.max(0, Math.min(100, value));
    return (<div className={cn("w-full", className)}>
      <div className="relative h-2.5 w-full overflow-hidden rounded-full bg-secondary">
        <div className="h-full rounded-full gradient-hero transition-[width] duration-700 ease-out relative overflow-hidden" style={{ width: `${pct}%` }}>
          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shimmer bg-[length:200%_100%]"/>
        </div>
      </div>
      {showLabel && (<div className="mt-1.5 flex justify-between text-xs font-medium text-muted-foreground">
          <span className="text-primary font-semibold">{pct}% recaudado</span>
        </div>)}
    </div>);
}
