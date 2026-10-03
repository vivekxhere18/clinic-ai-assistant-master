
<div align="center">

# 🏥 Clinic AI Assistant

**AI-assisted patient intake and triage summaries for small clinics — informational only, never diagnostic.**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)
![H2](https://img.shields.io/badge/Database-H2-1021FF)
![Claude](https://img.shields.io/badge/AI-Anthropic%20Claude-D97757)
![Status](https://img.shields.io/badge/Status-Demo%20Project-blue)

</div>

> [!WARNING]
> This is an **educational/demo project**. It does **not** diagnose, prescribe, or replace
> professional medical advice. All AI output is informational only, and all sample patient
> data is **fictional**.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Problem Statement](#-problem-statement)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [AI Integration](#-ai-integration)
- [API Reference](#-api-reference)
- [Getting Started](#-getting-started)
- [Test Cases](#-test-cases)
- [Safety & Responsible AI](#-safety--responsible-ai)
- [Learnings](#-learnings)
- [Future Improvements](#-future-improvements)
- [Internship Details](#-internship-details)

---

## 📌 Overview

**Clinic AI Assistant** is a full-stack web application for a small-clinic scenario in India.
Patients or front-desk staff enter patient details and symptoms through a registration form.
The backend then generates a **structured, non-diagnostic AI summary** that helps clinic staff
triage patients and prepare for the doctor's visit. A doctor-facing dashboard lets doctors
browse, search, and review all registered patients and their summaries.

It was built end-to-end (frontend, backend, persistence, and AI integration) during a
2-week internship.

## ❓ Problem Statement

Small clinics often lack a structured way to capture patient-reported symptoms before a
consultation. This leads to:

- Unstructured, inconsistent intake notes
- No early triage signal for urgent cases
- Doctors spending consultation time on basic clarifying questions

This project digitizes intake, uses an AI assistant to summarize and flag urgency (never to
diagnose), and gives doctors a searchable dashboard of patients before they walk in.

## ✨ Key Features

| Feature | Description |
|---|---|
| **Patient registration form** | Captures name, age, gender, phone, city, medical history, symptom checkboxes (Fever, Cough, Headache, Cold, Body Pain, Stomach Pain, Other + free text), duration, and notes, with client-side validation and inline errors |
| **AI-generated intake summary** | Plain-language summary, identified symptoms and duration, missing information, suggested follow-up questions, a triage level (`Routine` / `Soon` / `Urgent`), and a mandatory disclaimer |
| **Doctor dashboard** | Searchable, filterable patient list with name, age, main symptoms, duration, and a color-coded attention badge |
| **Patient detail view** | Full intake data and AI summary, disclaimer banner, and a visible note whenever the offline fallback was used |
| **Safety by design** | The AI must never diagnose, prescribe, or suggest dosages, enforced in both the Claude system prompt and the offline fallback logic |

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.3 (Web, Data JPA, Validation), Maven |
| **Database** | H2 (file-based, embedded, no external setup) |
| **AI** | Anthropic Claude API, with a deterministic rule-based fallback |
| **Frontend** | Angular 19 (standalone components), HTML/CSS, RxJS |

## 🏗️ Architecture

```text
clinic-ai-assistant/
├── backend/                     Spring Boot REST API (port 8081)
│   └── src/main/java/com/clinic/aiassistant/
│       ├── model/               Patient JPA entity
│       ├── dto/                 PatientRequest / PatientResponse / AiSummary / PatientListItem
│       ├── repository/          PatientRepository (Spring Data JPA)
│       ├── service/             PatientService, AiSummaryService (Claude call + fallback)
│       ├── controller/          PatientController → /api/patients
│       ├── config/              CORS configuration
│       └── exception/           Global exception handler
│
└── frontend/                    Angular standalone app (port 4200)
    └── src/app/
        ├── models/              patient.model.ts
        ├── services/            patient.service.ts
        ├── pages/               register, dashboard, patient-detail
        └── components/          attention-badge, disclaimer
```

**Request flow**

```text
Angular form ─▶ POST /api/patients ─▶ PatientService (persist via JPA/H2)
                                          │
                                          ▼
                              AiSummaryService ─▶ Claude API ──(on failure)──▶ rule-based fallback
                                          │
                                          ▼
              structured AiSummary stored & returned ─▶ dashboard badge ─▶ patient detail view
```

## 🤖 AI Integration

- **Prompt design:** A system prompt instructs Claude that it is a *clinical intake assistant*.
  It must never diagnose, prescribe, or give dosages. It must respond with **only** valid JSON
  matching a fixed schema: `patientSummary`, `symptomsIdentified`, `duration`,
  `missingInformation`, `suggestedQuestions`, `attentionLevel`, `disclaimer`.
- **Response parsing:** The first JSON object in the raw output is extracted, which tolerates
  stray prose or code fences. It is then validated: required fields must be present, and
  `attentionLevel` must be one of the three allowed values.
- **Reliability fallback:** If no API key is configured, or the API call fails (network error,
  invalid key, malformed response), the service switches to a **deterministic rule-based
  generator**. It scans symptoms and notes for red-flag keywords (e.g. chest pain,
  breathlessness, severe bleeding, unconsciousness) to choose between `Routine` and `Urgent`,
  and always attaches the disclaimer. The API contract stays the same, with
  `status: "OK"` vs `status: "FALLBACK"`.

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/patients` | Create a patient and generate the AI summary |
| `GET` | `/api/patients?q=` | List patients, optionally filtered by name, city, or symptom |
| `GET` | `/api/patients/{id}` | Fetch full patient detail and AI summary |

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Node 20+ and npm

### 1. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API runs at **http://localhost:8081**. With no `ANTHROPIC_API_KEY` set, it automatically
uses the offline fallback, so no external dependency is needed to demo the app.

<details>
<summary><b>Enable live Claude calls (optional)</b></summary>

```bash
export ANTHROPIC_API_KEY=sk-ant-...
cd backend
mvn spring-boot:run
```

</details>

### 2. Run the frontend

```bash
cd frontend
npm install
npx ng serve --proxy-config proxy.conf.json
```

The app runs at **http://localhost:4200** and proxies `/api/*` to the backend.

### 3. Try it out

1. Open http://localhost:4200, which redirects to the **Dashboard**.
2. Click **+ New Patient**, fill in the form, and submit.
3. View the AI summary, attention badge, and disclaimer on the patient detail page.
4. Return to the Dashboard to see the new patient. Search by name, city, or symptom.

## ✅ Test Cases

Manually verified:

| # | Scenario | Expected Result |
|---|---|---|
| 1 | Normal input (Fever + Headache, 2 days) | `attentionLevel: "Routine"`, summary echoes symptoms and duration, disclaimer shown |
| 2 | Red-flag input ("severe chest pain and breathless") | `attentionLevel: "Urgent"` without naming any condition |
| 3 | Missing required fields / invalid age | `400 Bad Request` with field-level errors, also blocked client-side |
| 4 | AI unavailable (no or invalid API key) | Request still succeeds with `status: "FALLBACK"`, UI shows an offline-summary notice |
| 5 | Search / filter patients | `GET /api/patients?q=Mumbai` and dashboard search filter by name, city, or symptom |
| 6 | Unknown patient ID | `GET /api/patients/999` returns `404 Not Found` |

## 🛡️ Safety & Responsible AI

- The system never generates a diagnosis, prescription, or dosage. This is enforced in both
  the live AI prompt and the hard-coded fallback.
- A disclaimer appears on every AI summary, in both the UI and the API response.
- `attentionLevel` is strictly a **triage urgency signal**, never a named medical condition.
- All sample patient data used in testing is fictional.
- The Anthropic API key is read only from a server-side environment variable. It is never
  exposed to the frontend or committed to source control.

## 📚 Learnings

- Designing a safety-constrained AI prompt and schema, and validating structured LLM output
  server-side
- Building a resilient fallback so a third-party AI dependency never breaks the core app
- End-to-end full-stack development: Angular standalone components, Spring Boot REST API,
  and JPA persistence
- Practical experience with CORS configuration, DTO-based API contracts, and global
  exception handling in Spring Boot

## 🔮 Future Improvements

- [ ] Authentication and authorization for the doctor-only dashboard
- [ ] Multi-language patient intake (Hindi and regional languages)
- [ ] Conversation-style follow-ups instead of a single static summary
- [ ] Automated unit and integration tests (currently manually verified)

## 🎓 Internship Details

| Field | Detail |
|---|---|
| **Intern Name** | Vivek Bharade |
| **Organization** | Premature Solution |
| **Role** | Software Development Intern |
| **Duration** | 2 Weeks |
| **Project Title** | Clinic AI Assistant |

---

<div align="center">
<sub>Educational demo project. Not a medical device. Not for clinical use.</sub>
</div>
