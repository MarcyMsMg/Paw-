# Levantamiento Docker - PAW+

## 1. Copiar variables

```bash
cp .env.example .env
```

Edita `.env` y cambia las claves.

## 2. Construir y levantar todo

```bash
docker compose up --build
```

## 3. Accesos locales

- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Users: http://localhost:8081
- Campaigns: http://localhost:8082
- Donations: http://localhost:8083
- Adoptions: http://localhost:8084
- Feed: http://localhost:8085
- Notifications: http://localhost:8086
- PostgreSQL: localhost:5432

## 4. Apagar sin borrar datos

```bash
docker compose down
```

## 5. Apagar y borrar la base local

```bash
docker compose down -v
```

## Nota importante

Dentro de Docker los microservicios no se llaman por `localhost`. Se comunican por el nombre del servicio:

- `http://users:8081`
- `http://campaigns:8082`
- `http://donations:8083`
- `http://adoptions:8084`
- `http://feed:8085`
- `http://notifications:8086`

Desde el navegador sí se usa `localhost`, por eso el frontend consume:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```
