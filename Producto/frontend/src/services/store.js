import campaign1 from "@/assets/campaign-1.jpg";
import campaign2 from "@/assets/campaign-2.jpg";
import campaign3 from "@/assets/campaign-3.jpg";
const KEY = "pawplus_db_v1";
const now = () => new Date().toISOString();
const uid = () => Math.random().toString(36).slice(2, 10);
const seed = () => ({
    admin: { email: "admin@pawplus.com", password: "admin123" },
    personas: [
        { id: "p1", nombre: "María", apellido: "García", email: "maria@mail.com", password: "1234", createdAt: now() },
        { id: "p2", nombre: "Carlos", apellido: "Pérez", email: "carlos@mail.com", password: "1234", createdAt: now() },
        { id: "p3", nombre: "Lucía", apellido: "Rojas", email: "lucia@mail.com", password: "1234", createdAt: now() },
    ],
    ongs: [
        {
            id: "o1", nombre: "Patitas Felices", email: "ong@patitas.com", password: "1234",
            descripcion: "Rescatamos perros en situación de calle desde 2015.",
            ubicacion: "Bogotá, Colombia", fundada: 2015, rescatados: 1240, voluntarios: 85,
            logo: "🐾", banner: campaign1, activa: true, createdAt: now(),
        },
        {
            id: "o2", nombre: "Gatitos Sin Hogar", email: "ong@gatitos.com", password: "1234",
            descripcion: "Refugio felino con esterilización gratuita.",
            ubicacion: "Medellín, Colombia", fundada: 2018, rescatados: 820, voluntarios: 42,
            logo: "🐱", banner: campaign2, activa: true, createdAt: now(),
        },
    ],
    solicitudes: [
        {
            id: "s1", nombre: "Refugio Esperanza", email: "contacto@esperanza.org", password: "1234",
            descripcion: "Santuario rural para animales mayores y con discapacidad.",
            ubicacion: "Cundinamarca, Colombia", fundada: 2019, rescatados: 340, voluntarios: 28,
            logo: "🌿", banner: campaign3, estado: "PENDIENTE", fechaSolicitud: now(), createdAt: now(),
        },
        {
            id: "s2", nombre: "Manada Libre", email: "info@manadalibre.org", password: "1234",
            descripcion: "Educación y rescate en zonas rurales.",
            ubicacion: "Boyacá, Colombia", fundada: 2020, rescatados: 560, voluntarios: 35,
            logo: "🐺", banner: campaign1, estado: "PENDIENTE", fechaSolicitud: now(), createdAt: now(),
        },
    ],
    animales: [
        { id: "a1", ongId: "o1", nombre: "Lola", especie: "Perro", edad: "3 años", sexo: "Hembra", tamano: "Mediano", salud: "Recuperándose de cirugía", descripcion: "Cariñosa, juguetona y muy sociable.", fotos: [campaign1], estado: "DISPONIBLE", createdAt: now() },
        { id: "a2", ongId: "o1", nombre: "Toby", especie: "Perro", edad: "5 años", sexo: "Macho", tamano: "Grande", salud: "Excelente", descripcion: "Tranquilo, ideal para familias.", fotos: [campaign3], estado: "DISPONIBLE", createdAt: now() },
        { id: "a3", ongId: "o2", nombre: "Mishi", especie: "Gato", edad: "1 año", sexo: "Hembra", tamano: "Pequeño", salud: "Esterilizada y vacunada", descripcion: "Gatita curiosa que ama dormir.", fotos: [campaign2], estado: "DISPONIBLE", createdAt: now() },
        { id: "a4", ongId: "o2", nombre: "Felipe", especie: "Gato", edad: "2 años", sexo: "Macho", tamano: "Mediano", salud: "Buena", descripcion: "Muy independiente y observador.", fotos: [campaign2], estado: "EN_PROCESO", createdAt: now() },
    ],
    campanas: [
        { id: "c1", ongId: "o1", titulo: "Salvemos a Lola: cirugía urgente", descripcion: "Lola fue atropellada y necesita una cirugía de cadera para volver a caminar.", banner: campaign1, meta: 4500000, recaudado: 3120000, donantes: 248, estado: "ACTIVA", categoria: "Emergencia médica", diasRestantes: 12, createdAt: now() },
        { id: "c2", ongId: "o2", titulo: "Esterilización masiva: 200 gatos", descripcion: "Jornada gratuita de esterilización para controlar la sobrepoblación felina.", banner: campaign2, meta: 8000000, recaudado: 2400000, donantes: 187, estado: "ACTIVA", categoria: "Esterilización", diasRestantes: 28, createdAt: now() },
        { id: "c3", ongId: "o1", titulo: "Ambulancia veterinaria 24/7", descripcion: "Necesitamos una ambulancia para responder a emergencias en toda la ciudad.", banner: campaign3, meta: 25000000, recaudado: 18500000, donantes: 612, estado: "ACTIVA", categoria: "Infraestructura", diasRestantes: 45, createdAt: now() },
    ],
    donaciones: [
        { id: "d1", campanaId: "c1", ongId: "o1", personaId: "p1", monto: 150000, metodoPago: "Tarjeta", estado: "APROBADO", fecha: now() },
        { id: "d2", campanaId: "c2", ongId: "o2", personaId: "p2", monto: 50000, metodoPago: "PSE", estado: "APROBADO", fecha: now() },
        { id: "d3", campanaId: "c1", ongId: "o1", personaId: "p3", monto: 80000, metodoPago: "Tarjeta", estado: "PENDIENTE", fecha: now() },
    ],
    postulaciones: [
        { id: "po1", animalId: "a1", ongId: "o1", personaId: "p1", nombreCompleto: "María García", email: "maria@mail.com", telefono: "3001234567", direccion: "Chapinero, Bogotá", tipoVivienda: "Apartamento con balcón", otrosAnimales: "No", motivo: "Quiero darle un hogar amoroso.", disponibilidad: "Fines de semana", estado: "PENDIENTE", fecha: now() },
    ],
});
function load() {
    if (typeof window === "undefined")
        return seed();
    try {
        const raw = localStorage.getItem(KEY);
        if (!raw) {
            const s = seed();
            localStorage.setItem(KEY, JSON.stringify(s));
            return s;
        }
        return JSON.parse(raw);
    }
    catch {
        return seed();
    }
}
function save(db) {
    localStorage.setItem(KEY, JSON.stringify(db));
}
const delay = (ms = 300) => new Promise((r) => setTimeout(r, ms));
export const db = {
    reset() { localStorage.removeItem(KEY); },
    raw: () => load(),
    // Personas
    async listPersonas() { await delay(); return load().personas; },
    async createPersona(p) {
        const d = load();
        if (d.personas.find((x) => x.email === p.email) || d.ongs.find((x) => x.email === p.email))
            throw new Error("El correo ya está registrado");
        const np = { ...p, id: uid(), createdAt: now() };
        d.personas.push(np);
        save(d);
        return np;
    },
    async updatePersona(id, patch) {
        const d = load();
        d.personas = d.personas.map((p) => (p.id === id ? { ...p, ...patch } : p));
        save(d);
        return d.personas.find((p) => p.id === id);
    },
    // ONGs
    async listOngs() { await delay(); return load().ongs; },
    async getOng(id) { await delay(); return load().ongs.find((o) => o.id === id); },
    async updateOng(id, patch) {
        const d = load();
        d.ongs = d.ongs.map((o) => (o.id === id ? { ...o, ...patch } : o));
        save(d);
        return d.ongs.find((o) => o.id === id);
    },
    // Solicitudes ONG
    async listSolicitudes() { await delay(); return load().solicitudes; },
    async getSolicitud(id) { await delay(); return load().solicitudes.find((s) => s.id === id); },
    async createSolicitud(s) {
        const d = load();
        if (d.personas.find((x) => x.email === s.email) || d.ongs.find((x) => x.email === s.email)
            || d.solicitudes.find((x) => x.email === s.email && x.estado !== "RECHAZADA"))
            throw new Error("El correo ya tiene una solicitud o cuenta");
        const ns = { ...s, id: uid(), estado: "PENDIENTE", fechaSolicitud: now(), createdAt: now() };
        d.solicitudes.push(ns);
        save(d);
        return ns;
    },
    async aprobarSolicitud(id) {
        const d = load();
        const s = d.solicitudes.find((x) => x.id === id);
        if (!s)
            throw new Error("Solicitud no encontrada");
        s.estado = "APROBADA";
        const ong = {
            id: uid(), nombre: s.nombre, email: s.email, password: s.password,
            descripcion: s.descripcion, ubicacion: s.ubicacion, fundada: s.fundada,
            rescatados: s.rescatados, voluntarios: s.voluntarios, logo: s.logo,
            banner: s.banner, redes: s.redes, documento: s.documento, activa: true, createdAt: now(),
        };
        d.ongs.push(ong);
        save(d);
        return ong;
    },
    async rechazarSolicitud(id, motivo) {
        const d = load();
        const s = d.solicitudes.find((x) => x.id === id);
        if (!s)
            throw new Error("Solicitud no encontrada");
        s.estado = "RECHAZADA";
        s.motivoRechazo = motivo;
        save(d);
        return s;
    },
    // Animales
    async listAnimales(ongId) {
        await delay();
        const a = load().animales;
        return ongId ? a.filter((x) => x.ongId === ongId) : a;
    },
    async getAnimal(id) { await delay(); return load().animales.find((a) => a.id === id); },
    async createAnimal(a) {
        const d = load();
        const na = { ...a, id: uid(), createdAt: now() };
        d.animales.push(na);
        save(d);
        return na;
    },
    async updateAnimal(id, patch) {
        const d = load();
        d.animales = d.animales.map((a) => (a.id === id ? { ...a, ...patch } : a));
        save(d);
        return d.animales.find((a) => a.id === id);
    },
    async deleteAnimal(id) {
        const d = load();
        d.animales = d.animales.filter((a) => a.id !== id);
        save(d);
    },
    // Campañas
    async listCampanas(ongId) {
        await delay();
        const c = load().campanas;
        return ongId ? c.filter((x) => x.ongId === ongId) : c;
    },
    async getCampana(id) { await delay(); return load().campanas.find((c) => c.id === id); },
    async createCampana(c) {
        const d = load();
        const nc = { ...c, id: uid(), recaudado: 0, donantes: 0, createdAt: now() };
        d.campanas.push(nc);
        save(d);
        return nc;
    },
    async updateCampana(id, patch) {
        const d = load();
        d.campanas = d.campanas.map((c) => (c.id === id ? { ...c, ...patch } : c));
        save(d);
        return d.campanas.find((c) => c.id === id);
    },
    // Donaciones
    async listDonaciones(filter) {
        await delay();
        let r = load().donaciones;
        if (filter?.ongId)
            r = r.filter((d) => d.ongId === filter.ongId);
        if (filter?.personaId)
            r = r.filter((d) => d.personaId === filter.personaId);
        return r;
    },
    async createDonacion(don) {
        const d = load();
        const nd = { ...don, id: uid(), fecha: now(), estado: "PENDIENTE" };
        d.donaciones.push(nd);
        const camp = d.campanas.find((c) => c.id === don.campanaId);
        if (camp) {
            camp.recaudado += don.monto;
            camp.donantes += 1;
        }
        save(d);
        return nd;
    },
    // Postulaciones
    async listPostulaciones(filter) {
        await delay();
        let r = load().postulaciones;
        if (filter?.ongId)
            r = r.filter((p) => p.ongId === filter.ongId);
        if (filter?.personaId)
            r = r.filter((p) => p.personaId === filter.personaId);
        return r;
    },
    async createPostulacion(p) {
        const d = load();
        const np = { ...p, id: uid(), fecha: now(), estado: "PENDIENTE" };
        d.postulaciones.push(np);
        const an = d.animales.find((a) => a.id === p.animalId);
        if (an && an.estado === "DISPONIBLE")
            an.estado = "EN_PROCESO";
        save(d);
        return np;
    },
    async updatePostulacion(id, estado) {
        const d = load();
        const p = d.postulaciones.find((x) => x.id === id);
        if (p) {
            p.estado = estado;
            if (estado === "ACEPTADA") {
                const a = d.animales.find((x) => x.id === p.animalId);
                if (a)
                    a.estado = "ADOPTADO";
            }
        }
        save(d);
        return p;
    },
    // Helpers de autenticación
    findUserByEmail(email) {
        const d = load();
        if (d.admin.email === email)
            return { kind: "admin", user: d.admin };
        const ong = d.ongs.find((o) => o.email === email);
        if (ong)
            return { kind: "ong", user: ong };
        const persona = d.personas.find((p) => p.email === email);
        if (persona)
            return { kind: "persona", user: persona };
        return null;
    },
};
