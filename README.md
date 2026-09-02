# Personal Job Copilot

A small personal job-search dashboard. The Spring Boot API imports listings from RemoteOK and Adzuna, stores them in PostgreSQL, and exposes a tracker used by the React frontend.

## Run it

Create a Supabase project and copy `backend/.env.example` values into your environment. `SUPABASE_DB_URL` must be the JDBC pooler URL; Flyway creates the Phase 1 tables when the backend starts. Adzuna is optional—without its keys, RemoteOK still works.

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

In another terminal:

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`. Use **Refresh jobs** to import listings immediately; the backend also refreshes every day at 6:00 AM in the configured time zone.

The main API routes are `GET /api/jobs`, `POST /api/jobs/refresh`, `GET /api/applications`, `POST /api/applications/jobs/{jobId}`, and `PATCH /api/applications/{id}/status`.
