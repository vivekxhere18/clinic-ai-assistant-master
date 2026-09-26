# Clinic AI Assistant

An educational, standalone web app for a small clinic in India. Staff or patients enter
patient info and symptoms; an AI assistant produces a **structured, non-diagnostic** summary
to help staff triage and prepare for the doctor visit. Includes a doctor dashboard to browse
and search patients.

> ⚠️ **This is a demo/educational project.** It does not diagnose, prescribe, or replace
> professional medical advice. All AI output is informational only. Sample data is fictional.

## Features

- **Patient registration form** — name, age, gender, phone, city, medical history, symptom
  checkboxes (Fever, Cough, Headache, Cold, Body Pain, Stomach Pain, Other + free text),
  duration, and additional notes. Client-side validation with inline error messages.
- **AI intake summary** — on submit, the backend generates a structured summary: plain-language
  summary, identified symptoms, duration, missing information, suggested follow-up questions,
  a triage **attention level** (Routine / Soon / Urgent), and a safety disclaimer.
- **Doctor dashboard** — searchable/filterable list of all registered patients showing name,
  age, main symptoms, duration, and a color-coded attention badge.
- **Patient detail view** — full patient info plus the AI summary, with a prominent disclaimer
  banner and a note when the AI service was unavailable and a rule-based fallback was used.
- **Safety by design** — the AI is explicitly instructed (and the offline fallback is
  hard-coded) to never diagnose, prescribe, or suggest dosages. The "Urgent" attention flag can
  be raised for red-flag symptoms without ever naming a condition.

## Tech Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.3 (Web, Data JPA, Validation), Maven |
| Database  | H2 (file-based, embedded — no external DB needed) |
| AI        | Anthropic Claude API (`claude-sonnet-4-6`), with a deterministic mock fallback |
| Frontend  | Angular 19 (standalone components), plain HTML/CSS, RxJS |

## Project Structure

```
clinic-ai-assistant/
├── backend/          Spring Boot API (port 8081)
│   └── src/main/java/com/clinic/aiassistant/
│       ├── model/         Patient JPA entity
│       ├── dto/           PatientRequest / PatientResponse / AiSummary / PatientListItem
│       ├── repository/    PatientRepository
│       ├── service/       PatientService, AiSummaryService (Claude call + mock fallback)
│       ├── controller/    PatientController (/api/patients)
│       ├── config/        CORS config
│       └── exception/     Global exception handler
└── frontend/         Angular standalone app (port 4200)
    └── src/app/
        ├── models/         patient.model.ts
        ├── services/       patient.service.ts
        ├── pages/          register, dashboard, patient-detail
        └── components/     attention-badge, disclaimer
```

## Setup & Running

### Prerequisites
- Java 17+
- Maven 3.9+
- Node 20+ / npm (Angular CLI is invoked via `npx`, no global install needed)

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on **http://localhost:8081**. H2 data file is created at `backend/data/clinicdb`.

By default, no `ANTHROPIC_API_KEY` is set, so every request uses the **deterministic mock AI
fallback** (`aiSummary.status = "FALLBACK"`) — the app works fully offline out of the box.

To use the real Claude API instead:

```bash
export ANTHROPIC_API_KEY=sk-ant-...
cd backend
mvn spring-boot:run
```

Successful real calls will return `aiSummary.status = "OK"`. Any API error automatically falls
back to the mock summary (`status = "FALLBACK"`) so the app never breaks.

### 2. Frontend

```bash
cd frontend
npm install   # first time only
npx ng serve --proxy-config proxy.conf.json
```

Runs on **http://localhost:4200** and proxies all `/api/*` calls to the backend on 8081.

### 3. Try it out

1. Open http://localhost:4200 → redirects to the Dashboard.
2. Click **+ New Patient**, fill in the form (e.g. name "Rahul Sharma", age 34, symptoms
   Fever + Headache, duration "2 days"), and submit.
3. You're taken to the patient detail page showing the AI summary, attention badge, and
   disclaimer.
4. Back on the Dashboard, the new patient appears in the list; use the search box to filter by
   name, city, or symptom.

## AI Prompt & JSON Schema

`AiSummaryService` sends a system + user prompt to Claude and expects **only** a JSON object
back, matching this schema:

```json
{
  "patientSummary": "string",
  "symptomsIdentified": ["string"],
  "duration": "string",
  "missingInformation": ["string"],
  "suggestedQuestions": ["string"],
  "attentionLevel": "Routine | Soon | Urgent",
  "disclaimer": "string"
}
```

**System prompt (summarized):** You are a clinical intake assistant for a small clinic in
India. You MUST NOT diagnose, prescribe, or suggest dosages. Your output is informational only.
`attentionLevel` reflects triage urgency only (never a diagnosis) — mark `Urgent` for red-flag
symptoms (chest pain, difficulty breathing, severe bleeding, loss of consciousness, etc.)
without naming any condition. Respond with valid JSON only, no markdown or prose.

**Response handling:** the raw model text is scanned for the first `{...}` JSON object (in case
of stray prose/code fences), parsed, and validated (required fields present, `attentionLevel` is
one of the three allowed values). Any parsing/validation/HTTP failure triggers the mock
fallback below, so the API contract to the frontend never changes.

**Mock fallback (`buildMockSummary`)** — used whenever no API key is set or the real call fails:
- Echoes back the selected symptoms and duration into a plain-language summary.
- Scans symptom text/notes for red-flag keywords (chest pain, breathless, severe, blood,
  bleeding, unconscious, seizure, etc.) → sets `attentionLevel = "Urgent"` if found, else
  `"Routine"`.
- Lists generic missing information (medications/allergies, severity, trend) and generic
  follow-up questions.
- Always attaches the safety disclaimer and sets `status = "FALLBACK"`.

## API Reference

| Method | Path                  | Description |
|--------|-----------------------|-------------|
| POST   | `/api/patients`       | Create a patient + generate AI summary. Body: see `PatientRequest`. |
| GET    | `/api/patients?q=`    | List patients (optional search by name/city/symptom). |
| GET    | `/api/patients/{id}`  | Full patient detail + AI summary. |

## Test Cases (manually verified)

1. **Normal input (Routine)** — Rahul Sharma, 34, Fever + Headache, 2 days →
   `attentionLevel: "Routine"`, mock summary echoes symptoms/duration, disclaimer present.
2. **Red-flag input (Urgent)** — Priya Verma, 45, "Other: severe chest pain and breathless since
   morning", 3 hours → `attentionLevel: "Urgent"` without naming any condition.
3. **Missing required fields** — omitting name/symptoms/duration or an out-of-range age (e.g.
   200) → `400 Bad Request` with a `fieldErrors` map; the Angular form blocks submission
   client-side with inline messages before it even reaches the API.
4. **AI service unavailable** — no `ANTHROPIC_API_KEY` (or an invalid one / network failure) →
   request still succeeds with `status: "FALLBACK"`; the patient-detail page shows a note that
   an offline summary was used instead of erroring out.
5. **Search / filter** — `GET /api/patients?q=Mumbai` and the dashboard search box both filter
   by name, city, or symptom text (case-insensitive, debounced on the frontend).
6. **Unknown patient** — `GET /api/patients/999` → `404 Not Found`.

## Safety Notes

- No diagnosis, prescriptions, or dosages are ever generated — enforced both in the Claude
  system prompt and hard-coded in the offline mock fallback.
- The disclaimer banner is shown on every AI summary, in the UI (`DisclaimerComponent`) and via
  the API response (`disclaimer` field).
- The `attentionLevel` field is a triage urgency hint only, never a named condition.
- All patient data used in examples is fictional (Rahul, Priya, Amit) — no real PII.
- The Anthropic API key is only ever read server-side from an environment variable; it is never
  exposed to the frontend or committed to source control (`.gitignore` excludes `.env`, `data/`,
  `target/`, `node_modules/`).

## Notes

- Git/GitHub setup for this project is intentionally deferred until the code is reviewed.