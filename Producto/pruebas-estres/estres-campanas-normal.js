// Prueba de carga NORMAL — Campañas (uso cotidiano, no un pico extremo).
// Complementa a estres-campanas.js: muestra que en condiciones normales el sistema
// responde holgadamente bajo el umbral de 2 s.
//
//   k6 run estres-campanas-normal.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export const options = {
  vus: 10,          // 10 usuarios concurrentes (carga normal)
  duration: '40s',
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% bajo 2 s (RNF2)
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
