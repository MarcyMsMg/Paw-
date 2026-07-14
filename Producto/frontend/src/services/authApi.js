// Cada microservicio vive en su propio puerto. Cuando exista el API Gateway,
// estas dos URLs apuntarÃ¡n al mismo origen y el gateway harÃ¡ el ruteo.
const API_BASE_URL = trimTrailingSlash(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api");
const USERS_BASE_URL = API_BASE_URL;
// CampaÃ±as tambiÃ©n pasa por el gateway (antes iba directo a :8082).
const CAMPAIGNS_BASE_URL = API_BASE_URL;
const ADOPTIONS_BASE_URL = `${API_BASE_URL}/adoptions`;
const TOKEN_KEY = "pawplus_token";
const SESSION_KEY = "pawplus_session";
function trimTrailingSlash(value) {
    return value.replace(/\/+$/, "");
}
function clearAuthSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
    window.dispatchEvent(new Event("pawplus:auth-expired"));
}
export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.status = status;
        this.name = "ApiError";
    }
}
function friendlyError(status, backendMessage, path) {
    if (status === 401 && path === "/auth/login")
        return "Email o contraseÃ±a incorrectos";
    if (status === 401)
        return backendMessage || "Debes iniciar sesiÃ³n para continuar.";
    if (status === 403)
        return "Tu cuenta aÃºn no estÃ¡ activada. Espera la aprobaciÃ³n del administrador.";
    if (status === 404)
        return "Recurso no encontrado";
    if (status === 409)
        return "El email ya estÃ¡ registrado";
    if (status === 400)
        return "Datos invÃ¡lidos. Revisa el formulario.";
    if (status >= 500)
        return "Error del servidor. IntÃ©ntalo mÃ¡s tarde.";
    return backendMessage || "OcurriÃ³ un error inesperado";
}
async function request(path, init = {}, baseUrl = USERS_BASE_URL) {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = {
        "Content-Type": "application/json",
        ...init.headers,
    };
    if (token)
        headers.Authorization = `Bearer ${token}`;
    let res;
    try {
        res = await fetch(`${baseUrl}${path}`, { ...init, headers });
    }
    catch {
        throw new ApiError("No se pudo conectar con el servidor. Â¿EstÃ¡ corriendo el backend?", 0);
    }
    let body = null;
    try {
        body = (await res.json());
    }
    catch {
        // respuesta vacÃ­a o no-JSON
    }
    if (!res.ok || (body && body.success === false)) {
        const message = friendlyError(res.status, body?.message ?? "", path);
        if (res.status === 401 && path !== "/auth/login") {
            clearAuthSession();
        }
        throw new ApiError(message, res.status);
    }
    return body?.data;
}
export function mapBackendRole(role) {
    if (role === "ADMIN")
        return "ADMINISTRADOR";
    if (role === "NGO")
        return "ONG";
    return "PERSONA_NATURAL";
}
export function displayName(user) {
    if (user.role === "NGO" && user.ngoName)
        return user.ngoName;
    if (user.role === "ADMIN")
        return "Administrador";
    const first = user.firstName ?? "";
    const last = user.lastName ?? "";
    return `${first} ${last}`.trim() || user.email;
}
export function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}
export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}
export const authApi = {
    login: (email, password) => request("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
    }),
    registerNaturalPerson: (payload) => request("/auth/register/natural-person", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
    requestNgoRegistration: (payload) => request("/auth/register/ngo-request", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
};
export const userApi = {
    getById: (userId) => request(`/users/${userId}`, { method: "GET" }),
    updateProfile: (userId, payload) => request(`/users/${userId}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
    }),
};
// Endpoints pÃºblicos del directorio de ONGs (no requieren login).
export const ngosApi = {
    list: () => request("/ngos", { method: "GET" }),
    getById: (id) => request(`/ngos/${id}`, { method: "GET" }),
};
// Adaptador: convierte el BackendUser de una ONG al shape "Fundacion" que esperan
// los componentes existentes (CardFundacion, FundacionDetail) sin tener que reescribirlos.
// `logo` puede ser una URL (foto subida por la ONG) o un emoji; los componentes detectan
// el tipo y renderizan <img> o texto segÃºn corresponda.
export function ngoToFundacion(ngo) {
    return {
        id: ngo.id,
        nombre: ngo.ngoName ?? "ONG sin nombre",
        descripcion: ngo.description ?? "Esta ONG aÃºn no tiene una descripciÃ³n.",
        descripcionCorta: ngo.description ?? "",
        ubicacion: ngo.location ?? "Sin ubicaciÃ³n",
        fundada: ngo.foundationYear ?? new Date().getFullYear(),
        rescatados: ngo.rescuedAnimalsCount ?? 0,
        voluntarios: ngo.volunteersCount ?? 0,
        logo: ngo.profileImageUrl || "ðŸ¾",
        banner: ngo.coverImageUrl || "",
    };
}
// Helper para distinguir URLs de strings normales (emoji, texto, etc.).
export function isImageUrl(value) {
    return typeof value === "string" && /^https?:\/\//i.test(value.trim());
}
// Convierte un link de YouTube (en cualquiera de sus formatos) a la URL de embed.
// Devuelve null si no es un enlace de YouTube reconocible.
export function youtubeEmbedUrl(url) {
    if (!url) return null;
    const patterns = [
        /youtu\.be\/([\w-]{11})/,
        /youtube\.com\/watch\?v=([\w-]{11})/,
        /youtube\.com\/embed\/([\w-]{11})/,
        /youtube\.com\/shorts\/([\w-]{11})/,
    ];
    for (const re of patterns) {
        const m = url.match(re);
        if (m) return `https://www.youtube.com/embed/${m[1]}`;
    }
    return null;
}
// â”€â”€ Microservicio de CampaÃ±as (puerto 8082) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Le pasamos CAMPAIGNS_BASE_URL como tercer argumento de request().
export const campaignsApi = {
    list: (ngoId) => {
        const qs = ngoId ? `?ngoId=${ngoId}` : "";
        return request(`/campaigns${qs}`, { method: "GET" }, CAMPAIGNS_BASE_URL);
    },
    getById: (id) => request(`/campaigns/${id}`, { method: "GET" }, CAMPAIGNS_BASE_URL),
    create: (payload) => request("/campaigns", {
        method: "POST",
        body: JSON.stringify(payload),
    }, CAMPAIGNS_BASE_URL),
    update: (id, payload) => request(`/campaigns/${id}`, {
        method: "PATCH",
        body: JSON.stringify(payload),
    }, CAMPAIGNS_BASE_URL),
    finish: (id) => request(`/campaigns/${id}/finish`, { method: "PATCH" }, CAMPAIGNS_BASE_URL),
};

function buildQuery(params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "")
            query.set(key, value);
    });
    const result = query.toString();
    return result ? `?${result}` : "";
}

export const adoptionsApi = {
    listAnimals: (params) => request(`/animals${buildQuery(params)}`, { method: "GET" }, ADOPTIONS_BASE_URL),
    getAnimal: (id) => request(`/animals/${id}`, { method: "GET" }, ADOPTIONS_BASE_URL),
    getAnimalForm: (id) => request(`/animals/${id}/form`, { method: "GET" }, ADOPTIONS_BASE_URL),
    listOwnedAnimals: () => request("/ngo/animals", { method: "GET" }, ADOPTIONS_BASE_URL),
    createAnimal: (payload) => request("/ngo/animals", {
        method: "POST",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
    updateAnimal: (id, payload) => request(`/ngo/animals/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
    retireAnimal: (id) => request(`/ngo/animals/${id}`, { method: "DELETE" }, ADOPTIONS_BASE_URL),
    listTemplates: () => request("/ngo/form-templates", { method: "GET" }, ADOPTIONS_BASE_URL),
    getTemplate: (id) => request(`/ngo/form-templates/${id}`, { method: "GET" }, ADOPTIONS_BASE_URL),
    createTemplate: (payload) => request("/ngo/form-templates", {
        method: "POST",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
    updateTemplate: (id, payload) => request(`/ngo/form-templates/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
    deactivateTemplate: (id) => request(`/ngo/form-templates/${id}`, { method: "DELETE" }, ADOPTIONS_BASE_URL),
    submitApplication: (animalId, payload) => request(`/animals/${animalId}/applications`, {
        method: "POST",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
    listApplications: () => request("/applications", { method: "GET" }, ADOPTIONS_BASE_URL),
    decideApplication: (id, payload) => request(`/applications/${id}/decision`, {
        method: "PATCH",
        body: JSON.stringify(payload),
    }, ADOPTIONS_BASE_URL),
};

const ANIMAL_STATUS_ES = {
    AVAILABLE: "DISPONIBLE",
    IN_PROCESS: "EN_PROCESO",
    ADOPTED: "ADOPTADO",
    RETIRED: "RETIRADO",
};

export function animalToView(animal) {
    return {
        ...animal,
        ongId: animal.ngoId,
        nombre: animal.name,
        especie: animal.species,
        edad: animal.age,
        sexo: animal.sex,
        tamano: animal.size,
        ubicacion: animal.location ?? "",
        salud: animal.healthStatus ?? "",
        descripcion: animal.description,
        requisitos: animal.adoptionRequirements ?? "",
        fotos: animal.photoUrls ?? [],
        estado: ANIMAL_STATUS_ES[animal.status] ?? animal.status,
        formTemplateId: animal.formTemplateId ?? null,
    };
}

const APPLICATION_STATUS_ES = {
    PENDING: "PENDIENTE",
    INFO_REQUESTED: "INFORMACION_SOLICITADA",
    ACCEPTED: "ACEPTADA",
    REJECTED: "RECHAZADA",
};

export function applicationToView(application) {
    return {
        ...application,
        nombreCompleto: application.fullName,
        telefono: application.phone,
        direccion: application.address,
        tipoVivienda: application.housingType,
        otrosAnimales: application.otherAnimals,
        motivo: application.motivation,
        disponibilidad: application.availability,
        experienciaPrevia: application.previousExperience,
        estado: APPLICATION_STATUS_ES[application.status] ?? application.status,
        fecha: application.createdAt,
        respuestas: application.customAnswers ?? [],
    };
}
const CAMPAIGN_STATUS_ES = { ACTIVE: "ACTIVA", COMPLETED: "COMPLETADA", PAUSED: "PAUSADA" };
// DÃ­as restantes hasta la fecha de fin (0 si ya pasÃ³). null si no hay fecha.
function diasHasta(isoDate) {
    if (!isoDate) return null;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const fin = new Date(`${isoDate}T00:00:00`);
    const diff = Math.ceil((fin - hoy) / 86400000);
    return diff > 0 ? diff : 0;
}
// Adaptador: convierte la campaÃ±a del backend al shape que esperan los componentes
// existentes (CardCampaign, CrowdfundingDetail, OngCampanas). Incluye alias (`imagen`
// + `banner`) y el estado en espaÃ±ol para no reescribir esos componentes.
// `donantes` es derivado (vendrÃ¡ de Donaciones); `diasRestantes` se calcula desde endDate.
export function campaignToView(c) {
    return {
        id: c.id,
        ngoId: c.ngoId,
        fundacionId: c.ngoId,
        titulo: c.title,
        descripcion: c.description ?? "",
        descripcionCorta: c.description ?? "",
        categoria: c.category ?? "General",
        imagen: c.bannerUrl || "",
        banner: c.bannerUrl || "",
        video: c.videoUrl || "",
        meta: Number(c.goalAmount ?? 0),
        recaudado: Number(c.raisedAmount ?? 0),
        donantes: Number(c.donorCount ?? c.donorsCount ?? c.donantes ?? 0),
        diasRestantes: diasHasta(c.endDate),
        fechaInicio: c.startDate ?? null,
        fechaFin: c.endDate ?? null,
        estado: CAMPAIGN_STATUS_ES[c.status] ?? c.status,
    };
}
export function countApprovedDonors(donations = []) {
    return new Set(
        donations
            .filter((d) => String(d.status ?? "").toUpperCase() === "APPROVED")
            .map((d) => d.donorId)
            .filter(Boolean)
    ).size;
}
export async function campaignToViewWithDonors(campaign) {
    const view = campaignToView(campaign);
    try {
        const summary = await donationsApi.getCampaignSummary(view.id);
        return { ...view, donantes: summary?.donorCount ?? 0 };
    } catch {
        return view;
    }
}
export async function campaignsToViewWithDonors(campaigns = []) {
    return Promise.all(campaigns.map(campaignToViewWithDonors));
}

function isCampaignCurrentlyVisible(campaign) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const start = campaign.startDate ? new Date(`${campaign.startDate}T00:00:00`) : null;
    const end = campaign.endDate ? new Date(`${campaign.endDate}T00:00:00`) : null;
    return campaign.status === "ACTIVE" && (!start || start <= today) && (!end || end >= today);
}

export async function publicCampaignsToViewWithDonors(ngoId) {
    const [campaigns, activeNgos] = await Promise.all([
        campaignsApi.list(ngoId),
        ngosApi.list(),
    ]);
    const activeNgoIds = new Set((activeNgos ?? []).map((ngo) => ngo.id));
    const visibleCampaigns = (campaigns ?? []).filter((campaign) =>
        activeNgoIds.has(campaign.ngoId) && isCampaignCurrentlyVisible(campaign)
    );
    return campaignsToViewWithDonors(visibleCampaigns);
}
export const adminApi = {
    listNgoRequests: () => request("/admin/ngo-requests", { method: "GET" }),
    listPendingNgoRequests: () => request("/admin/ngo-requests/pending", { method: "GET" }),
    approveNgoRequest: (id) => request(`/admin/ngo-requests/${id}/approve`, { method: "PATCH" }),
    rejectNgoRequest: (id, reason) => request(`/admin/ngo-requests/${id}/reject`, {
        method: "PATCH",
        body: JSON.stringify({ reason }),
    }),
    listUsers: (role) => {
        const qs = role ? `?role=${role}` : "";
        return request(`/admin/users${qs}`, { method: "GET" });
    },
    updateUserStatus: (userId, status) => request(`/admin/users/${userId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    }),
    listAdoptionAnimals: () => request("/admin/animals", { method: "GET" }, ADOPTIONS_BASE_URL),
    listAdoptionApplications: () => request("/applications", { method: "GET" }, ADOPTIONS_BASE_URL),
};
// â”€â”€ Microservicio de Donaciones (a travÃ©s del gateway: /api/donations) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const donationsApi = {
    // Crea la donaciÃ³n y devuelve { donationId, status, checkoutUrl } para redirigir a MercadoPago.
    create: (payload) => request("/donations", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
    listByDonor: (donorId) => request(`/donations?donorId=${donorId}`, { method: "GET" }),
    getCampaignSummary: (campaignId) => request(`/donations/campaigns/${campaignId}/summary`, { method: "GET" }),
    listByCampaign: (campaignId) => request(`/donations?campaignId=${campaignId}`, { method: "GET" }),
    listByNgo: (ngoId) => request(`/donations?ngoId=${ngoId}`, { method: "GET" }),
    getById: (id) => request(`/donations/${id}`, { method: "GET" }),
    getReceipt: (id) => request(`/donations/${id}/receipt`, { method: "GET" }),
    // Confirma un pago al volver de MercadoPago (la back_url trae el payment_id).
    syncPayment: (paymentId) => request(`/donations/sync-payment?paymentId=${paymentId}`, { method: "POST" }),
};
const DONATION_STATUS_ES = {
    PENDING: "PENDIENTE",
    APPROVED: "APROBADO",
    REJECTED: "RECHAZADO",
    REFUNDED: "DEVUELTO",
    CHARGED_BACK: "CONTRACARGO",
    EXPIRED: "EXPIRADO",
};
// Datos de transferencia (bancarios) de una ONG â€” a travÃ©s del gateway.
export const payoutAccountsApi = {
    getByNgo: (ngoId) => request(`/payout-accounts/${ngoId}`, { method: "GET" }),
    upsert: (ngoId, payload) => request(`/payout-accounts/${ngoId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    }),
};
// Panel admin: liquidaciÃ³n de fondos a las ONGs â€” vÃ­a gateway (/api/admin/payouts â†’ Donaciones).
export const adminPayoutsApi = {
    // Saldo pendiente por ONG (con desglose de campaÃ±as finalizadas).
    getBalances: () => request("/admin/payouts/balances", { method: "GET" }),
    getBalance: (ngoId) => request(`/admin/payouts/balances/${ngoId}`, { method: "GET" }),
    // Historial de transferencias ya registradas de una ONG.
    history: (ngoId) => request(`/admin/payouts?ngoId=${ngoId}`, { method: "GET" }),
    // Registrar una transferencia realizada a una ONG.
    createPayout: (payload) => request("/admin/payouts", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
};
// Adaptador: convierte la donaciÃ³n del backend al shape que esperan las vistas.
export function donationToView(d) {
    return {
        id: d.id,
        campanaId: d.campaignId,
        ngoId: d.ngoId,
        donorId: d.donorId,
        monto: Number(d.paidAmount ?? d.amount ?? 0),
        metodoPago: d.paymentMethod ?? "â€”",
        receiptNumber: d.receiptNumber ?? null,
        fecha: d.paidAt ?? d.createdAt,
        estado: DONATION_STATUS_ES[d.status] ?? d.status,
        status: d.status,
    };
}
