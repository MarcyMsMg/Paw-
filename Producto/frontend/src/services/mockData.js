import campaign1 from "@/assets/campaign-1.jpg";
import campaign2 from "@/assets/campaign-2.jpg";
import campaign3 from "@/assets/campaign-3.jpg";
export const fundaciones = [
    {
        id: "patitas-felices",
        nombre: "Patitas Felices",
        logo: "🐾",
        descripcionCorta: "Rescatamos perros en situación de calle desde 2015.",
        descripcion: "Patitas Felices es una ONG dedicada al rescate, rehabilitación y adopción responsable de perros en situación de calle. Trabajamos con un equipo de veterinarios voluntarios y familias temporales que cuidan a cada peludito hasta encontrar su hogar definitivo.",
        ubicacion: "Bogotá, Colombia",
        fundada: 2015,
        rescatados: 1240,
        voluntarios: 85,
        campañas: ["c1", "c3"],
        banner: campaign1,
    },
    {
        id: "gatitos-sin-hogar",
        nombre: "Gatitos Sin Hogar",
        logo: "🐱",
        descripcionCorta: "Refugio felino con esterilización gratuita.",
        descripcion: "Somos un refugio especializado en gatos rescatados, brindando atención veterinaria, esterilización y un programa de adopción consciente. Más de 800 gatos han encontrado familia gracias a tu apoyo.",
        ubicacion: "Medellín, Colombia",
        fundada: 2018,
        rescatados: 820,
        voluntarios: 42,
        campañas: ["c2"],
        banner: campaign2,
    },
    {
        id: "huellitas-de-amor",
        nombre: "Huellitas de Amor",
        logo: "❤️",
        descripcionCorta: "Atención veterinaria de emergencia 24/7.",
        descripcion: "Operamos una clínica veterinaria de emergencia para animales rescatados sin recursos. Cada donación nos permite salvar una vida en peligro inmediato.",
        ubicacion: "Cali, Colombia",
        fundada: 2012,
        rescatados: 2100,
        voluntarios: 120,
        campañas: ["c3"],
        banner: campaign3,
    },
    {
        id: "refugio-esperanza",
        nombre: "Refugio Esperanza",
        logo: "🌿",
        descripcionCorta: "Santuario rural para animales mayores.",
        descripcion: "Cuidamos animales senior y con discapacidad que difícilmente serían adoptados. Les damos un hogar definitivo en nuestro santuario rural de 3 hectáreas.",
        ubicacion: "Cundinamarca, Colombia",
        fundada: 2019,
        rescatados: 340,
        voluntarios: 28,
        campañas: ["c1"],
        banner: campaign1,
    },
    {
        id: "manada-libre",
        nombre: "Manada Libre",
        logo: "🐺",
        descripcionCorta: "Educación y rescate en zonas rurales.",
        descripcion: "Llevamos jornadas de esterilización y educación sobre tenencia responsable a comunidades rurales sin acceso a servicios veterinarios.",
        ubicacion: "Boyacá, Colombia",
        fundada: 2020,
        rescatados: 560,
        voluntarios: 35,
        campañas: ["c2"],
        banner: campaign2,
    },
    {
        id: "alas-y-patas",
        nombre: "Alas y Patas",
        logo: "🦜",
        descripcionCorta: "Rehabilitación de fauna silvestre.",
        descripcion: "Rehabilitamos aves y pequeños mamíferos silvestres víctimas del tráfico ilegal para devolverlos a su hábitat natural.",
        ubicacion: "Santa Marta, Colombia",
        fundada: 2017,
        rescatados: 980,
        voluntarios: 50,
        campañas: ["c3"],
        banner: campaign3,
    },
];
export const campaigns = [
    {
        id: "c1",
        fundacionId: "patitas-felices",
        titulo: "Salvemos a Lola: cirugía urgente",
        descripcionCorta: "Lola fue atropellada y necesita una cirugía de cadera para volver a caminar.",
        descripcion: "Lola es una mestiza de 3 años que rescatamos hace dos semanas tras ser atropellada. Los veterinarios confirmaron que necesita una cirugía de reconstrucción de cadera valorada en $4.500.000. Cada peso cuenta para devolverle la posibilidad de correr.",
        imagen: campaign1,
        meta: 4500000,
        recaudado: 3120000,
        donantes: 248,
        diasRestantes: 12,
        categoria: "Emergencia médica",
    },
    {
        id: "c2",
        fundacionId: "gatitos-sin-hogar",
        titulo: "Esterilización masiva: 200 gatos",
        descripcionCorta: "Jornada gratuita de esterilización para controlar la sobrepoblación felina.",
        descripcion: "Organizamos una jornada de esterilización para 200 gatos comunitarios. Necesitamos cubrir insumos médicos, anestesia y honorarios del equipo veterinario voluntario.",
        imagen: campaign2,
        meta: 8000000,
        recaudado: 2400000,
        donantes: 187,
        diasRestantes: 28,
        categoria: "Esterilización",
    },
    {
        id: "c3",
        fundacionId: "huellitas-de-amor",
        titulo: "Ambulancia veterinaria 24/7",
        descripcionCorta: "Necesitamos una ambulancia para responder a emergencias en toda la ciudad.",
        descripcion: "Una ambulancia equipada nos permitirá atender emergencias a domicilio y trasladar animales heridos en cualquier momento. Es la inversión que multiplicará vidas salvadas.",
        imagen: campaign3,
        meta: 25000000,
        recaudado: 18500000,
        donantes: 612,
        diasRestantes: 45,
        categoria: "Infraestructura",
    },
    {
        id: "c4",
        fundacionId: "refugio-esperanza",
        titulo: "Alimento para 80 abuelitos",
        descripcionCorta: "Comida especializada para nuestros animales senior durante 3 meses.",
        descripcion: "Nuestros 80 residentes mayores requieren dieta especial. Tu donación cubre alimento premium y suplementos por tres meses.",
        imagen: campaign1,
        meta: 6000000,
        recaudado: 4800000,
        donantes: 320,
        diasRestantes: 18,
        categoria: "Alimentación",
    },
    {
        id: "c5",
        fundacionId: "manada-libre",
        titulo: "Brigada rural en Boyacá",
        descripcionCorta: "Llevamos atención veterinaria a 5 veredas sin acceso médico.",
        descripcion: "Una semana de brigada en zonas rurales: vacunación, desparasitación y educación a 300 familias.",
        imagen: campaign2,
        meta: 3500000,
        recaudado: 1200000,
        donantes: 95,
        diasRestantes: 22,
        categoria: "Brigadas",
    },
    {
        id: "c6",
        fundacionId: "alas-y-patas",
        titulo: "Liberación de 30 loros",
        descripcionCorta: "Última fase de rehabilitación antes de devolverlos a la selva.",
        descripcion: "30 loros rescatados del tráfico ilegal están listos para volver a volar. Necesitamos cubrir el traslado y monitoreo post-liberación.",
        imagen: campaign3,
        meta: 5500000,
        recaudado: 5100000,
        donantes: 410,
        diasRestantes: 6,
        categoria: "Fauna silvestre",
    },
];
// Carga asíncrona simulada
const delay = (ms) => new Promise((r) => setTimeout(r, ms));
export async function fetchFundaciones() {
    await delay(600);
    return fundaciones;
}
export async function fetchFundacion(id) {
    await delay(400);
    return fundaciones.find((f) => f.id === id);
}
export async function fetchCampaigns() {
    await delay(600);
    return campaigns;
}
export async function fetchCampaign(id) {
    await delay(400);
    return campaigns.find((c) => c.id === id);
}
export async function fetchCampaignsByFundacion(fundacionId) {
    await delay(300);
    return campaigns.filter((c) => c.fundacionId === fundacionId);
}
