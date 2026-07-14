import { Link } from "react-router-dom";
import { StatusBadge } from "./StatusBadge";
export function CardAnimal({ a }) {
    return (<Link to={`/animales/${a.id}`} className="group bg-card rounded-2xl border border-border overflow-hidden shadow-soft hover:shadow-card hover:-translate-y-1 transition-smooth flex flex-col animate-fade-in-up">
      <div className="h-44 bg-secondary overflow-hidden relative">
        {a.fotos[0] && <img src={a.fotos[0]} alt={a.nombre} className="w-full h-full object-cover group-hover:scale-110 transition-smooth duration-700"/>}
        <div className="absolute top-2 right-2"><StatusBadge value={a.estado}/></div>
      </div>
      <div className="p-4 space-y-1">
        <h3 className="font-display font-bold text-lg">{a.nombre}</h3>
        <p className="text-xs text-muted-foreground">{[a.especie, a.edad, a.sexo, a.tamano].filter(Boolean).join(" - ")}</p>
        <p className="text-sm text-muted-foreground line-clamp-2 pt-1">{a.descripcion}</p>
      </div>
    </Link>);
}
