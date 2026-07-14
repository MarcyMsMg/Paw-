import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Edit, Trash2 } from "lucide-react";
import { adoptionsApi, animalToView } from "@/services/authApi";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { StatusBadge } from "@/components/StatusBadge";
import { toast } from "@/hooks/use-toast";

const OngAnimales = () => {
    const [items, setItems] = useState([]);
    const reload = () => adoptionsApi.listOwnedAnimals().then((animals) => setItems(animals.map(animalToView)));

    useEffect(() => { reload().catch(showError); }, []);

    const remove = async (id) => {
        try {
            await adoptionsApi.retireAnimal(id);
            toast({ title: "Animal retirado" });
            reload();
        }
        catch (error) {
            showError(error);
        }
    };

    return (
        <div className="space-y-6">
            <header className="flex flex-wrap items-end justify-between gap-4">
                <div>
                    <p className="text-sm font-semibold text-primary">Mi ONG</p>
                    <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Animales en adopcion</h1>
                </div>
                <Button asChild variant="hero"><Link to="/ong/animales/nuevo"><Plus className="w-4 h-4"/> Crear animal</Link></Button>
            </header>

            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
                {items.map((a) => (
                    <article key={a.id} className="bg-card rounded-2xl border border-border shadow-soft overflow-hidden hover:shadow-card transition-smooth">
                        <div className="h-40 bg-secondary overflow-hidden">
                            {a.fotos[0] && <img src={a.fotos[0]} alt={a.nombre} className="w-full h-full object-cover"/>}
                        </div>
                        <div className="p-4 space-y-2">
                            <div className="flex items-center justify-between">
                                <h3 className="font-display font-bold text-lg">{a.nombre}</h3>
                                <StatusBadge value={a.estado}/>
                            </div>
                            <p className="text-xs text-muted-foreground">{a.especie} - {a.edad} - {a.sexo} - {a.tamano}</p>
                            <p className="text-sm text-muted-foreground line-clamp-2">{a.descripcion}</p>
                            <div className="flex gap-2 pt-2">
                                <Button asChild size="sm" variant="soft" className="flex-1">
                                    <Link to={`/ong/animales/editar/${a.id}`}><Edit className="w-3.5 h-3.5"/> Editar</Link>
                                </Button>
                                <AlertDialog>
                                    <AlertDialogTrigger asChild>
                                        <Button size="sm" variant="outline"><Trash2 className="w-3.5 h-3.5"/></Button>
                                    </AlertDialogTrigger>
                                    <AlertDialogContent>
                                        <AlertDialogHeader>
                                            <AlertDialogTitle>Retirar animal</AlertDialogTitle>
                                            <AlertDialogDescription>
                                                {`"${a.nombre}" dejara de aparecer como disponible para adopcion.`}
                                            </AlertDialogDescription>
                                        </AlertDialogHeader>
                                        <AlertDialogFooter>
                                            <AlertDialogCancel>Cancelar</AlertDialogCancel>
                                            <AlertDialogAction onClick={() => remove(a.id)}>Retirar</AlertDialogAction>
                                        </AlertDialogFooter>
                                    </AlertDialogContent>
                                </AlertDialog>
                            </div>
                        </div>
                    </article>
                ))}
                {items.length === 0 && <p className="text-muted-foreground col-span-full">Aun no has publicado animales.</p>}
            </div>
        </div>
    );
};

function showError(error) {
    toast({ title: "No fue posible cargar los animales", description: error?.message, variant: "destructive" });
}

export default OngAnimales;
