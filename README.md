# Beacon

A small personal job-search dashboard. The Spring Boot API imports listings from RemoteOK and Adzuna, stores them in PostgreSQL, and exposes a tracker used by the React frontend. On first use, Beacon saves your search preferences and parses one PDF resume through Groq.

## Run it

Create a Supabase project and copy `backend/.env.example` values into your environment. `SUPABASE_DB_URL` must be the JDBC pooler URL. Add a server-side `GROQ_API_KEY`; it is required for resume parsing and is never sent to the frontend. Flyway creates the tables when the backend starts. Adzuna is optional—without its keys, RemoteOK still works.

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

The onboarding routes are `GET /api/profile`, `POST /api/profile`, and `POST /api/profile/resume`. The job tracker routes remain under `/api/jobs` and `/api/applications`.

Default model is `openai/gpt-oss-120b` (Groq moved `llama-3.3-70b-versatile` to enterprise-only). Override with `GROQ_MODEL` if you want something else.
