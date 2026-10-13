# Career Skill Roadmap Backend

Spring Boot REST API backend for the existing React application. The backend is a separate Gradle project in this folder; the frontend remains in `../frontend`.

## Requirements

- JDK 21
- MySQL 8 or a compatible MySQL server

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Configure MySQL

The application uses these environment variables:

| Variable | Required | Default |
| --- | --- | --- |
| `DB_URL` | No | `jdbc:mysql://localhost:3306/career_skill_roadmap?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | Yes | None |
| `DB_PASSWORD` | Yes | None |

Create a MySQL account with permission to create/use the `career_skill_roadmap` database, then set the credentials in PowerShell:

```powershell
$env:DB_USERNAME = "your_mysql_user"
$env:DB_PASSWORD = "your_mysql_password"
```

To use a different database or host, set `DB_URL` as well:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/career_skill_roadmap?createDatabaseIfNotExist=true"
```

Credentials are not stored in the project. The database itself must be running before starting the application. Hibernate schema updates are enabled for local development. The career catalog is seeded idempotently at startup; missing starter careers are added without overwriting existing rows.

## Run

From this `backend` directory:

```powershell
.\gradlew.bat bootRun
```

The API listens on port **8080**. Verify it with:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

The API allows browser `GET`, `POST`, and `PUT` requests from the Vite development origin `http://localhost:5173` under `/api/**`. Restart the backend after changing CORS configuration for the running application to pick it up.

## Test

```powershell
.\gradlew.bat test
```

## Career API

The career catalog is stored in MySQL. On startup, the application inserts any missing starter career paths and their skills; it does not overwrite careers already in the database.

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/api/careers` | List available careers and their ordered skills |
| `GET` | `/api/careers/{id}` | Get one career and its ordered skills by database ID |

The starter catalog covers ECE, Embedded Systems, IoT, VLSI, Robotics, Electrical, Software Development, AI/ML, Cybersecurity, Cloud/Networking, Data Science, and UI/UX. A missing career returns `404 Not Found` with a Problem Details response. A non-numeric career ID returns `400 Bad Request`.

Example:

```powershell
Invoke-RestMethod http://localhost:8080/api/careers
Invoke-RestMethod http://localhost:8080/api/careers/1
```

Each career response contains `id`, `title`, `domain`, `description`, and a `skills` array. Each skill contains `id`, `title`, `description`, and its ordered `position`.

## Career assessment API

Assessment answers, recommendation scores and explanations, and the selected roadmap career are stored in MySQL. The roadmap skills are read from the selected career's ordered catalog skills. These routes are not tied to user accounts; signup and login are not implemented.

| Method | Route | Description |
| --- | --- | --- |
| `POST` | `/api/assessments` | Validate and save an assessment; return matching catalog careers and explanations |
| `GET` | `/api/assessments/{id}` | Retrieve the saved assessment, recommendations, and selected ordered roadmap |
| `PUT` | `/api/assessments/{id}/roadmap` | Save a recommended career as the assessment's roadmap |

The assessment request accepts domain IDs `ece`, `embedded`, `iot`, `vlsi`, `robotics`, `electrical`, `software`, `ai`, `cybersecurity`, `cloud`, `data`, `product`, and `custom`; non-empty free-text `interests` plus optional `customInterest`; `skillLevel` (`Beginner`, `Intermediate`, or `Advanced`); and one or more `goals` (`projects`, `hands-on`, `research`, `design`, `analysis`, `career-ready`). Recommendation scores use a transparent rule: 50 base points, 30 for a catalog-domain match, 5 for each matching interest term (up to 20), and 5 for each goal aligned to catalog terms (up to 15), capped at 100. Only careers matching a selected domain or interest term are returned. The selected skill level is persisted with the result.

Example:

```powershell
$assessment = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/assessments `
  -ContentType "application/json" `
  -Body '{"domains":["embedded"],"interests":"firmware, sensors","customInterest":"","skillLevel":"Beginner","goals":["hands-on","projects"]}'
$assessment.recommendations
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/assessments/$($assessment.id)/roadmap" `
  -ContentType "application/json" -Body "{`"careerId`":$($assessment.recommendations[0].careerId)}"
```

Validation failures return `400 Bad Request`; unknown assessment IDs return `404 Not Found`. A roadmap can only be selected from careers recommended for that assessment.

## Authentication

Signup and login endpoints are not part of this API yet.
