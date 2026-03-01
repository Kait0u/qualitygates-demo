# 📘 Software Quality Management Demo

## CI/CD Quality Gates with Spring Boot + Gradle (Kotlin DSL)

---

# 🎯 Demo Goal

Demonstrate how **automated Quality Gates in CI/CD prevent low-quality code from being merged into the master branch**, using:

* Java 21
* Spring Boot 3.5.11
* Gradle (Kotlin DSL)
* JUnit 5
* JaCoCo (coverage gate)
* Trivy + CycloneDX SBOM (security gate)
* SonarCloud (static analysis gate)
* GitHub Actions (CI enforcement)

Core platforms:

* GitHub Actions
* SonarCloud
* JaCoCo
* Trivy (Aqua Security)

---

# 🏗️ Project Overview

## Demo Application

A minimal REST API:

**Project name:** `qualitygates-demo`

Domain:

* `Task`
* `TaskService`
* `TaskController`

Endpoints:

* `POST /tasks`
* `GET /tasks`
* `GET /tasks/{id}`
* `PUT /tasks/{id}`
* `DELETE /tasks/{id}`

Small enough to understand quickly.
Complex enough to generate coverage + analysis reports.

---

# 🧱 Technical Stack

| Layer           | Tool                      |
| --------------- | ------------------------- |
| Language        | Java 21                   |
| Framework       | Spring Boot 3.5.11        |
| Build           | Gradle Kotlin DSL         |
| Tests           | JUnit 5                   |
| Test reporting  | gradle-test-logger (MOCHA)|
| Coverage        | JaCoCo                    |
| Static Analysis | SonarCloud                |
| Security        | Trivy + CycloneDX SBOM    |
| CI              | GitHub Actions             |

---

# 🚦 Quality Gates Implemented

We enforce **four independent gates** in sequence.

---

## 1️⃣ Test Execution Gate

* All tests must pass
* Rich per-test output via `com.adarshr.test-logger` (MOCHA theme)

---

## 2️⃣ JaCoCo Coverage Gate

```kotlin
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

**Gate condition:** instruction coverage ≥ 80%

Failure → build fails.

> Note: `lombok.config` includes `lombok.addLombokGeneratedAnnotation = true`
> so Lombok-generated code is excluded from coverage measurement.

---

## 3️⃣ Trivy Security Gate

Generates a **CycloneDX SBOM** from Gradle's resolved dependency graph
(`./gradlew cyclonedxBom`), then scans it with Trivy:

```bash
trivy sbom \
  --scanners vuln \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  --format table \
  build/reports/bom.json
```

**Gate condition:** zero HIGH or CRITICAL CVEs (CVSS ≥ 7.0)

> Why Trivy over OWASP Dependency-Check?
> Trivy is fully open source (Apache 2.0), requires no API key or account,
> and maintains its own vulnerability database pulled from GitHub.

---

## 4️⃣ SonarCloud Quality Gate

| Metric          | Condition |
| --------------- | --------- |
| Coverage        | ≥ 80%     |
| Bugs            | 0         |
| Vulnerabilities | 0         |
| Code Smells     | < 5       |
| Duplications    | < 3%      |

Pipeline waits for SonarCloud Quality Gate result via `-Dsonar.qualitygate.wait=true`.

If ❌ → PR cannot merge.

---

# 🔁 CI/CD Pipeline Design

### Workflow: `.github/workflows/pull-request.yml`

Triggered on:

```yaml
on:
  pull_request:
    branches: [ master ]
```

### Pipeline Stages

1. Checkout (full depth — required for SonarCloud blame)
2. Setup JDK 21 (Temurin)
3. Cache SonarCloud packages
4. Build (compile only)
5. **Gate 1** — Run tests
6. **Gate 2** — JaCoCo coverage verification
7. Generate CycloneDX SBOM
8. **Gate 3** — Trivy vulnerability scan
9. **Gate 4** — SonarCloud analysis + quality gate wait
10. Fail or Pass

### Required GitHub Secrets & Variables

| Kind     | Name                | Description                          |
| -------- | ------------------- | ------------------------------------ |
| Secret   | `SONAR_TOKEN`       | SonarCloud user token                |
| Variable | `SONAR_PROJECT_KEY` | e.g. `Kait0u_qualitygates-demo`      |
| Variable | `SONAR_ORG`         | SonarCloud organisation slug         |

> `SONAR_PROJECT_KEY` and `SONAR_ORG` are stored as **Variables** (not Secrets)
> so the SonarCloud dashboard link is readable in CI logs.

---

# 🌿 Branch Strategy

Each branch is an independent PR from `master`, demonstrating a different failure mode.

---

# 🟢 Phase 0 — Baseline (`master`)

### Status: ✅ Green pipeline

Contains:

* `Task`, `TaskService`, `TaskController` (in-memory HashMap storage)
* Full tests — `TaskServiceTest`, `TaskControllerTest`, `DemoApplicationTests`
* ≥ 80% instruction coverage
* No vulnerabilities (jackson-bom overridden to 2.21.1)
* Clean SonarCloud analysis

> "This is our protected master branch."

---

# 🔴 Phase 1 — JaCoCo Coverage Failure

**Branch:** `feature/reduce-coverage`

### What changed:

* Added `findAll()` and `update()` to `TaskService`
* Added `GET /tasks` and `PUT /tasks/{id}` to `TaskController`
* **No tests written** for any of the new code

Coverage drops to ~60–65%.

### Expected CI result:

❌ `jacocoTestCoverageVerification` fails

### Teaching point:

> The coverage gate prevents under-tested features from entering master.
> New code without tests is automatically rejected.

---

# 🔴 Phase 2 — Fix Coverage, Break Tests

**Branch:** `feature/fix-coverage-break-tests`

### What changed:

* Added `findAll()` and `update()` with full tests (coverage stays above 80%)
* Introduced a logic bug in `deleteById`: task is removed but always returns `false`

### Expected CI result:

❌ `deleteById_returnsTrue_andRemovesTask_whenExists` fails

### Teaching point:

> High coverage does not mean correct behaviour.
> Tests must assert the right outcomes, not just execute the code.

---

# 🔴 Phase 3 — Security Failure

**Branch:** `feature/add-vulnerability`

### What changed:

* Added `commons-collections:commons-collections:3.2.1` as a dependency

Trivy detects:

| CVE            | Severity | Description                                      |
| -------------- | -------- | ------------------------------------------------ |
| CVE-2015-7501  | CRITICAL | InvokerTransformer RCE via deserialisation       |
| CVE-2015-6420  | HIGH     | Insecure deserialisation in Apache Commons       |

### Expected CI result:

❌ Trivy scan fails — HIGH and CRITICAL CVEs detected

### Teaching point:

> Vulnerable third-party dependencies are a supply-chain risk.
> The security gate catches them before they reach production,
> regardless of how good your tests are.

---

# 🔴 Phase 4 — SonarCloud Static Analysis Failure

**Branch:** `feature/add-code-smells`

### What changed:

Added `TaskExportService` with deliberate code quality issues:

* **Empty catch block** — exceptions silently swallowed in `exportToFile`
* **Unused field** — `exportVersion` declared but never read
* **Unused local variables** — `recordCount` declared in two methods
* **Duplicated blocks** — identical null-guard logic copy-pasted into `exportAsCsv` and `exportAsHtml`
* **Magic number** — `255` used inline without a named constant

All methods are tested (coverage passes), so only SonarCloud fires.

### Expected CI result:

❌ SonarCloud Quality Gate failed

### Teaching point:

> Static analysis catches structural problems that tests cannot:
> duplication, dead code, and swallowed exceptions.
> Code can be "tested" and still be unmaintainable.

---

# 🟢 Phase 5 — Final Clean PR

**Branch:** `feature/final-clean-implementation`

### What changed:

* Added `findAll()` and `update()` properly — clean implementation
* Full tests for all new methods
* No vulnerable dependencies
* No code smells

### Expected CI result:

✅ All four gates pass — PR allowed to merge

---

# 📊 Visual Pipeline Diagram

```
Pull Request
     ↓
GitHub Actions
     ↓
Build (compile)
     ↓
Gate 1 — Tests
     ↓
Gate 2 — JaCoCo ≥ 80%
     ↓
Generate SBOM (CycloneDX)
     ↓
Gate 3 — Trivy (no HIGH/CRITICAL CVEs)
     ↓
Gate 4 — SonarCloud Quality Gate
     ↓
Merge Allowed / Blocked
```

---

# 🧠 What You Explain During Demo

## 1️⃣ What Is a Quality Gate?

A measurable, automated decision point that determines whether code meets defined quality standards before it can be merged.

---

## 2️⃣ Why Multiple Gates?

Because quality is multi-dimensional:

* **Correctness** — tests
* **Testability** — coverage
* **Security** — dependency scanning
* **Maintainability** — static analysis

Each gate catches a different class of problem.

---

## 3️⃣ Why in CI/CD?

Early detection is cheaper.

> "Shift Left Quality" — find it in the PR, not in production.

---

# 🗂️ Repository Structure

```
qualitygates-demo/
 ├── .github/workflows/
 │     └── pull-request.yml
 ├── src/
 │   ├── main/java/com/example/demo/
 │   │     ├── controller/TaskController.java
 │   │     ├── model/Task.java
 │   │     └── service/TaskService.java
 │   └── test/java/com/example/demo/
 │         ├── controller/TaskControllerTest.java
 │         └── service/TaskServiceTest.java
 ├── build.gradle.kts
 ├── lombok.config
 └── settings.gradle.kts
```

---

# 🎤 Suggested Live Demo Script

### Step 1 — Show the green baseline

Open the `master` branch pipeline on GitHub Actions.
All four gates pass. Establish:

> "This is our protected branch. Nothing merges unless all gates are green."

### Step 2 — Coverage failure

Open PR from `feature/reduce-coverage`.
Point to the JaCoCo gate failure in the CI log.

Explain: coverage measures what percentage of code is exercised by tests.
New untested code silently lowered it.

### Step 3 — Test failure

Open PR from `feature/fix-coverage-break-tests`.
Coverage is fine. But the test execution gate fails.

Explain: coverage and correctness are different things.
A test that runs but asserts nothing proves nothing.

### Step 4 — Security failure

Open PR from `feature/add-vulnerability`.
Point to the Trivy table — CRITICAL and HIGH CVEs listed with CVE IDs and fix versions.

Explain: this is a supply-chain attack vector.
One vulnerable library can compromise the entire application.

### Step 5 — Static analysis failure

Open PR from `feature/add-code-smells`.
All tests pass, coverage is fine — but SonarCloud blocks the merge.

Explain: static analysis finds structural rot that tests cannot catch.
An empty catch block is a silent failure waiting to happen.

### Step 6 — All green

Open PR from `feature/final-clean-implementation`.
Every gate passes. Show the green checkmarks.

Conclude:

> "Quality Gates transform quality from a subjective code review opinion
> into an objective, enforceable policy embedded in the delivery pipeline."

---

# 🏆 Learning Outcomes

Students will understand:

* CI-based quality enforcement
* The difference between coverage, correctness, security, and maintainability
* How to configure JaCoCo, Trivy, and SonarCloud in a real Gradle project
* Branch protection and gated merges
* Why multiple independent gates are necessary

---

# ⏱ Suggested Timing (20–25 min)

| Part                    | Time   |
| ----------------------- | ------ |
| Theory intro            | 5 min  |
| Architecture overview   | 3 min  |
| Failing PRs walkthrough | 10 min |
| Final success           | 3 min  |
| Q&A                     | 5 min  |

---

# 🚀 Optional Advanced Additions

* Add branch protection rules on GitHub (require status checks to pass)
* Require PR reviews before merge
* Add mutation testing (Pitest) as a fifth gate
* Add Docker build stage
* Publish the CycloneDX SBOM as a release artefact

---

# 🎓 Final Message

> "Quality Gates transform quality from subjective opinion into objective,
> enforceable policy embedded in the delivery pipeline."
