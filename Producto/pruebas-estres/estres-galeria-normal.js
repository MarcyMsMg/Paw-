// Prueba de carga NORMAL — Galería de adopciones (uso cotidiano, no un pico extremo).
// Complementa a estres-galeria.js: muestra el rendimiento en condiciones normales.
//
//   k6 run estres-galeria-normal.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8084';

export const options = {
  vus: 10,          // 10 usuarios concurrentes (carga normal)
  duration: '40s',
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% bajo 2 s (RNF2)
    http_req_failed: ['rate<0.01'],    // menos del 1% de errores
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
