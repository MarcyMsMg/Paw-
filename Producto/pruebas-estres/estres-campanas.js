// Prueba de estrés — Campañas de crowdfunding (caso PE-03 "campaña viral" del plan).
// Simula muchos usuarios consultando el listado público de campañas al mismo tiempo.
//
// Ejecutar (con el servicio de Campañas levantado en 8082):
//   k6 run estres-campanas.js
// Contra otra URL (ej. el gateway o el despliegue):
//   k6 run -e BASE_URL=http://localhost:8080 estres-campanas.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export const options = {
  stages: [
    { duration: '30s', target: 300 }, // sube gradualmente a 300 usuarios virtuales
    { duration: '1m', target: 300 },  // mantiene la carga 1 minuto
    { duration: '30s', target: 0 },   // baja gradualmente
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% de respuestas bajo 2 s (RNF2)
    http_req_failed: ['rate<0.01'],    // menos del 1% de errores
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/api/campaigns`);
  check(res, {
    'status 200': (r) => r.status === 200,
    'responde en menos de 2s': (r) => r.timings.duration < 2000,
  });
  sleep(1);
}
