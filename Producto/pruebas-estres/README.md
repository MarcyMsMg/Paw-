# Pruebas de estrés — PAW+ (k6)

Scripts de carga para los endpoints públicos, alineados con el plan de pruebas
(casos PE-xx). Sirven como evidencia de las pruebas de estrés de la Evaluación 3.

## 1. Instalar k6 (una sola vez)

- **Windows (winget):** `winget install k6.k6` (o con Chocolatey: `choco install k6`)
- **Descarga directa:** https://k6.io/docs/get-started/installation/

Verificar: `k6 version`

## 2. Levantar el/los servicios a probar

Cada script apunta por defecto al servicio directo:

| Script | Servicio | Puerto por defecto | Endpoint |
|---|---|---|---|
| `estres-galeria.js` | Adopciones | 8084 | `GET /api/adoptions/animals` |
| `estres-campanas.js` | Campañas | 8082 | `GET /api/campaigns` |

Levanta el servicio correspondiente antes de correr el script (ej. `./mvnw spring-boot:run`
en el repo, o apunta al despliegue con `-e BASE_URL=...`).

## 3. Ejecutar

```bash
k6 run estres-galeria.js
k6 run estres-campanas.js
```

Contra otra URL (gateway o despliegue en Render):

```bash
k6 run -e BASE_URL=http://localhost:8080 estres-galeria.js
k6 run -e BASE_URL=https://tu-backend.onrender.com estres-campanas.js
```

## 4. Qué mide

Cada script sube gradualmente hasta 200–300 usuarios virtuales, mantiene la carga
1 minuto y baja. Los **umbrales (thresholds)** son los criterios de aceptación:

- `http_req_duration p(95) < 2000` → el 95% de las respuestas bajo **2 segundos** (RNF2).
- `http_req_failed rate < 0.01` → menos del **1%** de peticiones fallidas.

Si se cumplen, k6 marca la corrida como **PASS** (los thresholds salen en verde ✓).

## 5. Evidencia para el informe

Al terminar, k6 imprime un resumen con `http_req_duration` (avg, p95, max),
`http_req_failed` y el total de peticiones. **Una captura de ese resumen** es la
evidencia de la prueba de estrés que pide la rúbrica.

Para guardar el resumen en un archivo además de la consola:

```bash
k6 run --summary-export=resumen-galeria.json estres-galeria.js
```
