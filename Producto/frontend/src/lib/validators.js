// ---------------------------------------------------------------------------
// Email
// ---------------------------------------------------------------------------

export function normalizeEmail(email) {
  return String(email ?? "").trim().toLowerCase();
}

export function isValidEmail(email) {
  const value = normalizeEmail(email);
  if (!value || value.length > 120) return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

// ---------------------------------------------------------------------------
// Contraseña
// ---------------------------------------------------------------------------

// Devuelve un mensaje de error si la contraseña no cumple la política, o null si es válida.
export function validatePassword(password) {
  const value = password ?? "";
  if (value !== value.trim()) return "La contraseña no puede empezar ni terminar con espacios";
  if (!value) return "La contraseña es obligatoria";
  if (value.length < 8) return "La contraseña debe tener al menos 8 caracteres";
  if (value.length > 72) return "La contraseña no puede superar los 72 caracteres";
  if (!/[a-z]/.test(value)) return "La contraseña debe incluir al menos una minúscula";
  if (!/[A-Z]/.test(value)) return "La contraseña debe incluir al menos una mayúscula";
  if (!/[0-9]/.test(value)) return "La contraseña debe incluir al menos un número";
  return null;
}

export function validatePasswordConfirmation(password, confirm) {
  if (password !== confirm) return "Las contraseñas no coinciden";
  return null;
}

// ---------------------------------------------------------------------------
// RUT chileno
// ---------------------------------------------------------------------------

// Deja solo el cuerpo numérico + dígito verificador (sin puntos ni guion), en mayúsculas.
export function cleanRut(rut) {
  return String(rut ?? "").replace(/[^0-9kK]/g, "").toUpperCase();
}

// Formatea a "12.345.678-9" a partir de cualquier entrada con o sin puntos/guion.
export function formatRut(rut) {
  const clean = cleanRut(rut);
  if (clean.length < 2) return clean;
  const body = clean.slice(0, -1);
  const dv = clean.slice(-1);
  const withDots = body.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
  return `${withDots}-${dv}`;
}

// Valida un RUT chileno (con o sin puntos/guion) usando el algoritmo de dígito verificador módulo 11.
export function isValidRut(rut) {
  const clean = cleanRut(rut);
  if (clean.length < 2) return false;
  const body = clean.slice(0, -1);
  const dv = clean.slice(-1);
  if (!/^\d+$/.test(body)) return false;
  let sum = 0;
  let multiplier = 2;
  for (let i = body.length - 1; i >= 0; i--) {
    sum += Number(body[i]) * multiplier;
    multiplier = multiplier === 7 ? 2 : multiplier + 1;
  }
  const remainder = 11 - (sum % 11);
  const expectedDv = remainder === 11 ? "0" : remainder === 10 ? "K" : String(remainder);
  return dv === expectedDv;
}

// ---------------------------------------------------------------------------
// Teléfono chileno
// ---------------------------------------------------------------------------

// Acepta 912345678, +56912345678 o 56912345678 (celular chileno, con o sin espacios).
export function isValidChilePhone(phone) {
  const value = String(phone ?? "").replace(/\s+/g, "");
  return /^(\+?56)?9\d{8}$/.test(value);
}

// ---------------------------------------------------------------------------
// URLs
// ---------------------------------------------------------------------------

// Solo http:// o https:// (rechaza javascript:, data:, etc.).
export function isValidInternalOrHttpUrl(url) {
  const value = String(url ?? "").trim();
  if (!value) return false;
  try {
    const parsed = new URL(value);
    return parsed.protocol === "http:" || parsed.protocol === "https:";
  } catch {
    return false;
  }
}

// URL http/https que además apunte a una imagen jpg/jpeg/png/webp (no svg).
export function isValidImageUrl(url) {
  if (!isValidInternalOrHttpUrl(url)) return false;
  return /\.(jpe?g|png|webp)(\?.*)?(#.*)?$/i.test(String(url).trim());
}

// youtube.com/watch?v=, youtu.be/, youtube.com/embed/, youtube.com/shorts/
export function isValidYouTubeUrl(url) {
  const value = String(url ?? "").trim();
  if (!isValidInternalOrHttpUrl(value)) return false;
  return /^https?:\/\/(www\.)?(youtube\.com\/(watch\?v=|embed\/|shorts\/)|youtu\.be\/)[\w-]+/i.test(value);
}

// ---------------------------------------------------------------------------
// Números
// ---------------------------------------------------------------------------

export function isPositiveNumber(value) {
  const n = Number(value);
  return Number.isFinite(n) && n > 0;
}

export function isPositiveInteger(value) {
  const n = Number(value);
  return Number.isInteger(n) && n > 0;
}

// ---------------------------------------------------------------------------
// Texto genérico
// ---------------------------------------------------------------------------

// options: { required, min, max, label }. Devuelve mensaje de error o null.
export function validateText(value, options = {}) {
  const { required = false, min = 0, max = Infinity, label = "Este campo" } = options;
  const trimmed = String(value ?? "").trim();
  if (!trimmed) return required ? `${label} es obligatorio` : null;
  if (trimmed.length < min) return `${label} debe tener al menos ${min} caracteres`;
  if (trimmed.length > max) return `${label} no puede superar los ${max} caracteres`;
  return null;
}

// ---------------------------------------------------------------------------
// Fechas
// ---------------------------------------------------------------------------

// options: { requireStart, requireEnd, allowEqual }. Fechas en formato yyyy-mm-dd (comparables como string).
export function validateDateRange(startDate, endDate, options = {}) {
  const { requireStart = true, requireEnd = true, allowEqual = false } = options;
  if (requireStart && !startDate) return "La fecha de inicio es obligatoria";
  if (requireEnd && !endDate) return "La fecha de fin es obligatoria";
  if (startDate && endDate) {
    const invalid = allowEqual ? endDate < startDate : endDate <= startDate;
    if (invalid) {
      return allowEqual
        ? "La fecha de fin debe ser igual o posterior a la de inicio"
        : "La fecha de fin debe ser posterior a la de inicio";
    }
  }
  return null;
}

// ---------------------------------------------------------------------------
// Listas de imágenes
// ---------------------------------------------------------------------------

// options: { minItems }. Devuelve mensaje de error o null.
export function validateImageUrlList(urls, maxItems = 5, options = {}) {
  const { minItems = 1 } = options;
  const list = (urls || []).map((u) => String(u ?? "").trim()).filter(Boolean);
  if (list.length < minItems) return `Debes agregar al menos ${minItems} foto${minItems > 1 ? "s" : ""}`;
  if (list.length > maxItems) return `No puedes agregar más de ${maxItems} fotos`;
  const invalid = list.find((u) => !isValidImageUrl(u));
  if (invalid) return "Todas las fotos deben ser URLs válidas de imagen (jpg, jpeg, png o webp)";
  return null;
}
