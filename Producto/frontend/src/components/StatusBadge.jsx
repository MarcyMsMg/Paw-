import { cn } from "@/lib/utils";
const styles = {
    PENDIENTE: "bg-amber-100 text-amber-800 border-amber-200",
    APROBADA: "bg-emerald-100 text-emerald-800 border-emerald-200",
    APROBADO: "bg-emerald-100 text-emerald-800 border-emerald-200",
    PAGADO: "bg-emerald-100 text-emerald-800 border-emerald-200",
    ACEPTADA: "bg-emerald-100 text-emerald-800 border-emerald-200",
    RECHAZADA: "bg-red-100 text-red-800 border-red-200",
    RECHAZADO: "bg-red-100 text-red-800 border-red-200",
    ACTIVA: "bg-emerald-100 text-emerald-800 border-emerald-200",
    COMPLETADA: "bg-blue-100 text-blue-800 border-blue-200",
    PAUSADA: "bg-zinc-100 text-zinc-700 border-zinc-200",
    DISPONIBLE: "bg-emerald-100 text-emerald-800 border-emerald-200",
    EN_PROCESO: "bg-amber-100 text-amber-800 border-amber-200",
    ADOPTADO: "bg-blue-100 text-blue-800 border-blue-200",
    RETIRADO: "bg-zinc-100 text-zinc-700 border-zinc-200",
    INFORMACION_SOLICITADA: "bg-sky-100 text-sky-800 border-sky-200",
    BORRADOR: "bg-zinc-100 text-zinc-700 border-zinc-200",
    PUBLICADO: "bg-emerald-100 text-emerald-800 border-emerald-200",
    ARCHIVADO: "bg-blue-100 text-blue-800 border-blue-200",
    OCULTO: "bg-red-100 text-red-800 border-red-200",
};
export function StatusBadge({ value, className }) {
    return (<span className={cn("inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border", styles[value] || "bg-secondary text-secondary-foreground border-border", className)}>
      {value.replace("_", " ")}
    </span>);
}
