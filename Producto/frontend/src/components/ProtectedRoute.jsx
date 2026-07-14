import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
export function ProtectedRoute({ roles }) {
    const { user, loading } = useAuth();
    const loc = useLocation();
    if (loading)
        return <div className="container py-20 text-center text-muted-foreground">Cargando...</div>;
    if (!user)
        return <Navigate to="/login" state={{ from: loc.pathname }} replace/>;
    if (!roles.includes(user.role))
        return <Navigate to="/" replace/>;
    return <Outlet />;
}
