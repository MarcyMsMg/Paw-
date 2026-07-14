// Prueba de estrés — Galería de adopciones (caso PE-02 del plan de pruebas).
// Simula muchos usuarios consultando el listado público de animales al mismo tiempo.
//
// Ejecutar (con el servicio de Adopciones levantado en 8084):
//   k6 run estres-galeria.js
// Contra otra URL (ej. el gateway o el despliegue en Render):
//   k6 run -e BASE_URL=http://localhost:8080 estres-galeria.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8084';

export const options = {
  stages: [
    { duration: '30s', target: 200 }, // sube gradualmente a 200 usuarios virtuales
    { duration: '1m', target: 200 },  // mantiene la carga 1 minuto
    { duration: '30s', target: 0 },   // baja gradualmente
  ],
  thresholds: {
    // Criterio de aceptación (RNF2): el 95% de las respuestas bajo 2 segundos.
    http_req_duration: ['p(95)<2000'],
    // Menos del 1% de peticiones fallidas.
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/api/adoptions/animals`);
  check(res, {
    'status 200': (r) => r.status === 200,
    'responde en menos de 2s': (r) => r.timings.duration < 2000,
  });
  sleep(1);
}
