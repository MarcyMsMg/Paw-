# Levantamiento local con Docker - PAW+

## 1. Requisitos previos

Tener instalado y abierto:

- Docker Desktop
- Git
- Navegador web

## 2. Ubicarse en la raíz del proyecto

Abrir una terminal en la carpeta raíz del proyecto, donde se encuentra el archivo `docker-compose.yml`.

Ejemplo en Windows:

```powershell
cd "C:\Users\fenix\OneDrive\Escritorio\Producto"
```

## 3. Crear archivo `.env`

En la raíz del proyecto debe existir un archivo llamado `.env`.

Si existe un archivo `.env.example`, copiarlo como `.env`:

```powershell
copy .env.example .env
```

Luego completar el `.env` con las credenciales correspondientes:

```env
JWT_SECRET=
INTERNAL_API_KEY=
PAWPLUS_ADMIN_PASSWORD=

USERS_DATASOURCE_URL=
USERS_DATASOURCE_USERNAME=
USERS_DATASOURCE_PASSWORD=

CAMPAIGNS_DATASOURCE_URL=
CAMPAIGNS_DATASOURCE_USERNAME=
CAMPAIGNS_DATASOURCE_PASSWORD=

DONATIONS_DATASOURCE_URL=
DONATIONS_DATASOURCE_USERNAME=
DONATIONS_DATASOURCE_PASSWORD=

ADOPTIONS_DATASOURCE_URL=
ADOPTIONS_DATASOURCE_USERNAME=
ADOPTIONS_DATASOURCE_PASSWORD=

FEED_DATASOURCE_URL=
FEED_DATASOURCE_USERNAME=
FEED_DATASOURCE_PASSWORD=

NOTIFICATIONS_DATASOURCE_URL=
NOTIFICATIONS_DATASOURCE_USERNAME=
NOTIFICATIONS_DATASOURCE_PASSWORD=

MERCADOPAGO_ACCESS_TOKEN=
MERCADOPAGO_WEBHOOK_SECRET=
MERCADOPAGO_NOTIFICATION_URL=

VITE_API_BASE_URL=http://localhost:8080/api
```

## 4. Levantar el proyecto

Desde la raíz del proyecto ejecutar:

```powershell
docker compose up --build
```

Para levantarlo en segundo plano:

```powershell
docker compose up --build -d
```

## 5. Verificar contenedores activos

```powershell
docker ps
```

Deben aparecer los servicios:

```text
frontend
api-gateway
users
campaigns
donations
adoptions
feed
notifications
```

## 6. Acceder al sistema

Abrir en el navegador:

```text
http://localhost:5173
```

El API Gateway queda disponible en:

```text
http://localhost:8080
```

## 7. Ver logs

Ver logs de todos los servicios:

```powershell
docker compose logs -f
```

Ver logs de un servicio específico:

```powershell
docker compose logs -f users
docker compose logs -f api-gateway
docker compose logs -f notifications
```

## 8. Detener el proyecto

```powershell
docker compose down
```

## 9. Reconstruir desde cero

Si se modifican archivos del backend, frontend, Dockerfile o `application.properties`, ejecutar:

```powershell
docker compose down
docker compose up --build
```

Para reconstruir sin caché:

```powershell
docker compose down
docker compose build --no-cache
docker compose up
```

## 10. Liberar puerto del frontend

Si el puerto `5173` está ocupado, buscar el proceso:

```powershell
netstat -ano | findstr :5173
```

Finalizar el proceso usando el PID obtenido:

```powershell
taskkill /PID NUMERO_PID /F
```

Luego volver a ejecutar:

```powershell
docker compose up --build
```
