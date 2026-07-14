import { useEffect, useState } from "react";
import { adminApi, animalToView, applicationToView, ngosApi } from "@/services/authApi";
import { StatusBadge } from "@/components/StatusBadge";
import { toast } from "@/hooks/use-toast";

const AdminAdoptions = () => {
    const [animals, setAnimals] = useState([]);
    const [applications, setApplications] = useState([]);
    const [ngoNames, setNgoNames] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            adminApi.listAdoptionAnimals(),
            adminApi.listAdoptionApplications(),
            ngosApi.list(),
        ])
            .then(([animalItems, applicationItems, ngoItems]) => {
                setAnimals(animalItems.map(animalToView));
                setApplications(applicationItems.map(applicationToView));
                setNgoNames(Object.fromEntries((ngoItems ?? []).map((ngo) => [ngo.id, ngo.ngoName ?? ngo.email ?? "ONG sin nombre"])));
            })
            .catch((err) => toast({ title: "Error", description: err.message, variant: "destructive" }))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="space-y-6">
            <header>
                <p className="text-sm font-semibold text-primary">Administracion</p>
                <h1 className="font-display text-3xl md:text-4xl font-bold mt-1">Adopciones</h1>
            </header>

            <section className="grid sm:grid-cols-3 gap-4">
                <div className="bg-card rounded-lg border border-border p-4">
                    <p className="text-sm text-muted-foreground">Animales publicados</p>
                    <p className="text-3xl font-bold mt-1">{loading ? "..." : animals.length}</p>
                </div>
                <div className="bg-card rounded-lg border border-border p-4">
                    <p className="text-sm text-muted-foreground">Postulaciones</p>
                    <p className="text-3xl font-bold mt-1">{loading ? "..." : applications.length}</p>
                </div>
                <div className="bg-card rounded-lg border border-border p-4">
                    <p className="text-sm text-muted-foreground">En proceso</p>
                    <p className="text-3xl font-bold mt-1">
                        {loading ? "..." : animals.filter((animal) => animal.estado === "EN_PROCESO").length}
                    </p>
                </div>
            </section>

            <section className="bg-card rounded-lg border border-border overflow-hidden">
                <div className="px-4 py-3 border-b border-border">
                    <h2 className="font-display text-xl font-bold">Animales</h2>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead className="bg-secondary/50">
                            <tr className="text-left">
                                <th className="px-4 py-3 font-semibold">Animal</th>
                                <th className="px-4 py-3 font-semibold">ONG</th>
                                <th className="px-4 py-3 font-semibold">Ubicacion</th>
                                <th className="px-4 py-3 font-semibold">Estado</th>
                                <th className="px-4 py-3 font-semibold">Publicado</th>
                            </tr>
                        </thead>
                        <tbody>
                            {animals.map((animal) => (
                                <tr key={animal.id} className="border-t border-border hover:bg-secondary/30">
                                    <td className="px-4 py-3">
                                        <p className="font-semibold">{animal.nombre}</p>
                                        <p className="text-xs text-muted-foreground">{animal.especie} - {animal.edad || "Edad no indicada"}</p>
                                    </td>
                                    <td className="px-4 py-3 text-muted-foreground">{ngoNames[animal.ongId] ?? "ONG no encontrada"}</td>
                                    <td className="px-4 py-3 text-muted-foreground">{animal.ubicacion || "Sin ubicacion"}</td>
                                    <td className="px-4 py-3"><StatusBadge value={animal.estado} /></td>
                                    <td className="px-4 py-3 text-muted-foreground">{formatDate(animal.createdAt)}</td>
                                </tr>
                            ))}
                            {!loading && animals.length === 0 && (
                                <tr><td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">Sin animales registrados.</td></tr>
                            )}
                            {loading && (
                                <tr><td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">Cargando...</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            <section className="bg-card rounded-lg border border-border overflow-hidden">
                <div className="px-4 py-3 border-b border-border">
                    <h2 className="font-display text-xl font-bold">Postulaciones</h2>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead className="bg-secondary/50">
                            <tr className="text-left">
                                <th className="px-4 py-3 font-semibold">Persona</th>
                                <th className="px-4 py-3 font-semibold">Animal</th>
                                <th className="px-4 py-3 font-semibold">Contacto</th>
                                <th className="px-4 py-3 font-semibold">Estado</th>
                                <th className="px-4 py-3 font-semibold">Fecha</th>
                            </tr>
                        </thead>
                        <tbody>
                            {applications.map((application) => (
                                <tr key={application.id} className="border-t border-border hover:bg-secondary/30">
                                    <td className="px-4 py-3 font-semibold">{application.nombreCompleto}</td>
                                    <td className="px-4 py-3 text-muted-foreground">{application.animalName}</td>
                                    <td className="px-4 py-3 text-muted-foreground">
                                        <p>{application.email}</p>
                                        <p className="text-xs">{application.telefono}</p>
                                    </td>
                                    <td className="px-4 py-3"><StatusBadge value={application.estado} /></td>
                                    <td className="px-4 py-3 text-muted-foreground">{formatDate(application.fecha)}</td>
                                </tr>
                            ))}
                            {!loading && applications.length === 0 && (
                                <tr><td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">Sin postulaciones registradas.</td></tr>
                            )}
                            {loading && (
                                <tr><td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">Cargando...</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
};

function formatDate(value) {
    return value ? new Date(value).toLocaleDateString() : "Sin fecha";
}

export default AdminAdoptions;
