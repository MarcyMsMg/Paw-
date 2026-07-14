export const formatCurrency = (n) => new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
}).format(n);
export const percent = (recaudado, meta) => Math.min(100, Math.round((recaudado / meta) * 100));
