import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import { LayoutDashboard, ClipboardList, Building2, Users, PawPrint, Megaphone, Coins, User, ListChecks, FileText, Banknote, Newspaper } from "lucide-react";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { AuthProvider } from "@/contexts/AuthContext";
import { Layout } from "@/components/Layout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { DashboardLayout } from "@/components/DashboardLayout";
import Home from "./pages/Home";
import Fundaciones from "./pages/Fundaciones";
import FundacionDetail from "./pages/FundacionDetail";
import Crowdfundings from "./pages/Crowdfundings";
import CrowdfundingDetail from "./pages/CrowdfundingDetail";
import Animales from "./pages/Animales";
import AnimalDetail from "./pages/AnimalDetail";
import Feed from "./pages/Feed";
import FeedPostDetail from "./pages/FeedPostDetail";
import Login from "./pages/Login";
import RegistroPersona from "./pages/RegistroPersona";
import SolicitudOng from "./pages/SolicitudOng";
import Notificaciones from "./pages/Notificaciones";
import NotFound from "./pages/NotFound.jsx";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminSolicitudes from "./pages/admin/AdminSolicitudes";
import AdminSolicitudDetail from "./pages/admin/AdminSolicitudDetail";
import AdminOngs from "./pages/admin/AdminOngs";
import AdminUsuarios from "./pages/admin/AdminUsuarios";
import AdminAdoptions from "./pages/admin/AdminAdoptions";
import AdminPayouts from "./pages/admin/AdminPayouts";
import OngDashboard from "./pages/ong/OngDashboard";
import OngPerfil from "./pages/ong/OngPerfil";
import OngAnimales from "./pages/ong/OngAnimales";
import OngAnimalForm from "./pages/ong/OngAnimalForm";
import OngCampanas from "./pages/ong/OngCampanas";
import OngCampanaForm from "./pages/ong/OngCampanaForm";
import OngSolicitudesAdopcion from "./pages/ong/OngSolicitudesAdopcion";
import OngDonaciones from "./pages/ong/OngDonaciones";
import OngFormTemplates from "./pages/ong/OngFormTemplates";
import OngPublicaciones from "./pages/ong/OngPublicaciones";
import OngPublicacionForm from "./pages/ong/OngPublicacionForm";
import PersonaDashboard from "./pages/persona/PersonaDashboard";
import PersonaPerfil from "./pages/persona/PersonaPerfil";
import MisPostulaciones from "./pages/persona/MisPostulaciones";
import MisDonaciones from "./pages/persona/MisDonaciones";
const queryClient = new QueryClient();
const adminItems = [
    { to: "/admin/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/admin/solicitudes-ong", label: "Solicitudes ONG", icon: ClipboardList },
    { to: "/admin/ongs", label: "ONGs aprobadas", icon: Building2 },
    { to: "/admin/usuarios", label: "Usuarios", icon: Users },
    { to: "/admin/adopciones", label: "Adopciones", icon: PawPrint },
    { to: "/admin/transferencias", label: "Transferencias", icon: Banknote },
];
const ongItems = [
    { to: "/ong/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/ong/perfil", label: "Perfil", icon: Building2 },
    { to: "/ong/animales", label: "Animales", icon: PawPrint },
    { to: "/ong/formularios", label: "Formularios", icon: FileText },
    { to: "/ong/publicaciones", label: "Publicaciones", icon: Newspaper },
    { to: "/ong/campanas", label: "Campañas", icon: Megaphone },
    { to: "/ong/solicitudes-adopcion", label: "Adopciones", icon: ListChecks },
    { to: "/ong/donaciones", label: "Donaciones", icon: Coins },
];
const personaItems = [
    { to: "/persona/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/persona/perfil", label: "Perfil", icon: User },
    { to: "/persona/mis-postulaciones", label: "Mis postulaciones", icon: ListChecks },
    { to: "/persona/mis-donaciones", label: "Mis donaciones", icon: Coins },
];
const App = () => (<QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* Públicas */}
            <Route element={<Layout />}>
              <Route path="/" element={<Home />}/>
              <Route path="/fundaciones" element={<Fundaciones />}/>
              <Route path="/fundacion/:id" element={<FundacionDetail />}/>
              <Route path="/crowdfundings" element={<Crowdfundings />}/>
              <Route path="/crowdfunding/:id" element={<CrowdfundingDetail />}/>
              <Route path="/animales" element={<Animales />}/>
              <Route path="/animales/:id" element={<AnimalDetail />}/>
              <Route path="/feed" element={<Feed />}/>
              <Route path="/feed/:id" element={<FeedPostDetail />}/>
              <Route path="/login" element={<Login />}/>
              <Route path="/registro-persona" element={<RegistroPersona />}/>
              <Route path="/register" element={<Navigate to="/registro-persona" replace/>}/>
              <Route path="/solicitud-ong" element={<SolicitudOng />}/>
            </Route>


            {/* Notificaciones */}
            <Route element={<ProtectedRoute roles={["ADMINISTRADOR", "ONG", "PERSONA_NATURAL"]}/>}> 
              <Route element={<Layout />}>
                <Route path="/notificaciones" element={<Notificaciones />}/>
              </Route>
            </Route>
            {/* Admin */}
            <Route element={<ProtectedRoute roles={["ADMINISTRADOR"]}/>}>
              <Route element={<DashboardLayout items={adminItems} title="Administrador"/>}>
                <Route path="/admin/dashboard" element={<AdminDashboard />}/>
                <Route path="/admin/solicitudes-ong" element={<AdminSolicitudes />}/>
                <Route path="/admin/solicitudes-ong/:id" element={<AdminSolicitudDetail />}/>
                <Route path="/admin/ongs" element={<AdminOngs />}/>
                <Route path="/admin/usuarios" element={<AdminUsuarios />}/>
                <Route path="/admin/adopciones" element={<AdminAdoptions />}/>
                <Route path="/admin/transferencias" element={<AdminPayouts />}/>
              </Route>
            </Route>

            {/* ONG */}
            <Route element={<ProtectedRoute roles={["ONG"]}/>}>
              <Route element={<DashboardLayout items={ongItems} title="ONG"/>}>
                <Route path="/ong/dashboard" element={<OngDashboard />}/>
                <Route path="/ong/perfil" element={<OngPerfil />}/>
                <Route path="/ong/animales" element={<OngAnimales />}/>
                <Route path="/ong/animales/nuevo" element={<OngAnimalForm />}/>
                <Route path="/ong/animales/editar/:id" element={<OngAnimalForm />}/>
                <Route path="/ong/formularios" element={<OngFormTemplates />}/>
                <Route path="/ong/publicaciones" element={<OngPublicaciones />}/>
                <Route path="/ong/publicaciones/nueva" element={<OngPublicacionForm />}/>
                <Route path="/ong/publicaciones/editar/:id" element={<OngPublicacionForm />}/>
                <Route path="/ong/campanas" element={<OngCampanas />}/>
                <Route path="/ong/campanas/nueva" element={<OngCampanaForm />}/>
                <Route path="/ong/campanas/editar/:id" element={<OngCampanaForm />}/>
                <Route path="/ong/solicitudes-adopcion" element={<OngSolicitudesAdopcion />}/>
                <Route path="/ong/donaciones" element={<OngDonaciones />}/>
              </Route>
            </Route>

            {/* Persona */}
            <Route element={<ProtectedRoute roles={["PERSONA_NATURAL"]}/>}>
              <Route element={<DashboardLayout items={personaItems} title="Persona Natural"/>}>
                <Route path="/persona/dashboard" element={<PersonaDashboard />}/>
                <Route path="/persona/perfil" element={<PersonaPerfil />}/>
                <Route path="/persona/mis-postulaciones" element={<MisPostulaciones />}/>
                <Route path="/persona/mis-donaciones" element={<MisDonaciones />}/>
              </Route>
            </Route>

            <Route path="*" element={<NotFound />}/>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>);
export default App;
