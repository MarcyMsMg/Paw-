import { createContext, useContext, useEffect, useState } from "react";
import { authApi, clearToken, displayName, getToken, mapBackendRole, saveToken } from "@/services/authApi";
import { normalizeEmail } from "@/lib/validators";
const Ctx = createContext(null);
const SK = "pawplus_session";
export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        const token = getToken();
        const raw = localStorage.getItem(SK);
        if (!token) {
            localStorage.removeItem(SK);
            setLoading(false);
            return;
        }
        if (raw)
            try {
                setUser(JSON.parse(raw));
            }
            catch { }
        setLoading(false);
    }, []);
    useEffect(() => {
        const handleAuthExpired = () => setUser(null);
        window.addEventListener("pawplus:auth-expired", handleAuthExpired);
        return () => window.removeEventListener("pawplus:auth-expired", handleAuthExpired);
    }, []);
    const login = async (email, password) => {
        const { token, user: backendUser } = await authApi.login(normalizeEmail(email), password);
        const session = {
            id: backendUser.id,
            email: backendUser.email,
            nombre: displayName(backendUser),
            role: mapBackendRole(backendUser.role),
            foto: backendUser.profileImageUrl ?? undefined,
        };
        saveToken(token);
        localStorage.setItem(SK, JSON.stringify(session));
        setUser(session);
        return session;
    };
    const logout = () => {
        clearToken();
        localStorage.removeItem(SK);
        setUser(null);
    };
    const refresh = () => {
        const raw = localStorage.getItem(SK);
        if (raw)
            try {
                setUser(JSON.parse(raw));
            }
            catch { }
    };
    return <Ctx.Provider value={{ user, loading, login, logout, refresh }}>{children}</Ctx.Provider>;
}
export function useAuth() {
    const c = useContext(Ctx);
    if (!c)
        throw new Error("useAuth must be used inside AuthProvider");
    return c;
}
export function homeFor(role) {
    if (role === "ADMINISTRADOR")
        return "/admin/dashboard";
    if (role === "ONG")
        return "/ong/dashboard";
    return "/persona/dashboard";
}
