Below is your **detailed demo plan** in Markdown format.
It is structured so you can use it directly as:

* 📄 Presentation notes
* 📚 A README in the repository
* 🎓 A script for your Software Quality Management class

---

# 📘 Software Quality Management Demo

## CI/CD Quality Gates with Spring Boot + Gradle (Kotlin DSL)

---

# 🎯 Demo Goal

Demonstrate how **automated Quality Gates in CI/CD prevent low-quality code from being merged into the main branch**, using:

* Java 21
* Spring Boot 3.5.11
* Gradle (Kotlin DSL)
* JUnit 5
* JaCoCo (coverage gate)
* SonarCloud (static analysis gate)
* OWASP Dependency-Check (security gate)
* GitHub Actions (CI enforcement)

Core platforms:

* GitHub Actions
* SonarCloud
* JaCoCo
* OWASP Dependency-Check

---

# 🏗️ Project Overview

## Demo Application

A minimal REST API:

**Project name:** `quality-gates-demo`

Example domain:

* `Task`
* `TaskService`
* `TaskController`

Endpoints:

* `POST /tasks`
* `GET /tasks/{id}`
* `DELETE /tasks/{id}`

Small enough to understand quickly
Complex enough to generate coverage + analysis reports

---

# 🧱 Technical Stack

| Layer           | Tool                   |
| --------------- | ---------------------- |
| Language        | Java 21                |
| Framework       | Spring Boot 3.5.11     |
| Build           | Gradle Kotlin DSL      |
| Tests           | JUnit 5                |
| Coverage        | JaCoCo                 |
| Static Analysis | SonarCloud             |
| Security        | OWASP Dependency-Check |
| CI              | GitHub Actions         |

---

# 🚦 Quality Gates to Implement

We will enforce **multiple independent gates**.

## 1️⃣ Test Execution Gate

* All tests must pass
* `gradle build` must succeed

---

## 2️⃣ JaCoCo Coverage Gate

Configure Gradle rule:

```kotlin
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

**Gate condition:**

* Line coverage ≥ 80%

Failure → build fails.

---

## 3️⃣ SonarCloud Quality Gate

Conditions:

| Metric          | Condition |
| --------------- | --------- |
| Coverage        | ≥ 80%     |
| Bugs            | 0         |
| Vulnerabilities | 0         |
| Code Smells     | < 5       |
| Duplications    | < 3%      |

Pipeline waits for SonarCloud Quality Gate result.

If ❌ → PR cannot merge.

---

## 4️⃣ Security Gate (OWASP Dependency Check)

Fail build if:

* Any dependency has CVSS ≥ 7.0

---

# 🔁 CI/CD Pipeline Design

### Workflow: `pull-request.yml`

Triggered on:

```yaml
on:
  pull_request:
    branches: [ main ]
```

### Pipeline Stages

1. Checkout
2. Setup JDK 21
3. Build
4. Run tests
5. JaCoCo coverage verification
6. OWASP scan
7. SonarCloud analysis
8. Wait for Sonar Quality Gate
9. Fail or Pass

---

# 🌿 Branch Strategy for Demo

This is the core of your class demonstration.

We create **progressive PR failures**.

---

# 🟢 Phase 0 — Baseline (main branch)

### Status: ✅ Green pipeline

Contains:

* Good tests
* ≥ 80% coverage
* No major vulnerabilities
* Clean SonarCloud analysis

This establishes:

> “This is our protected main branch.”

---

# 🔴 Phase 1 — JaCoCo Coverage Failure

## Branch:

`feature/reduce-coverage`

### Action:

* Add new method in `TaskService`
* Do NOT write tests for it

Coverage drops to ~65%

### Expected CI Result:

❌ `jacocoTestCoverageVerification` fails

PR shows:

```
Coverage is 65%, but required minimum is 80%.
BUILD FAILED
```

### Teaching Point:

> Coverage gate prevents under-tested features from entering main.

---

# 🔴 Phase 2 — Fix Coverage, Break Tests

## Branch:

`feature/fix-coverage-break-tests`

### Action:

* Add tests to increase coverage
* Intentionally break business logic
* Tests fail

### Expected CI Result:

❌ Unit tests fail

### Teaching Point:

> Even if coverage is high, correctness still matters.

---

# 🔴 Phase 3 — Disable Tests, Trigger Security Failure

## Branch:

`feature/disable-tests-add-vulnerability`

### Action:

* Disable tests:

```kotlin
tasks.test {
    enabled = false
}
```

* Add vulnerable dependency version:
  Example: outdated Apache Commons

OWASP detects vulnerability.

### Expected CI Result:

❌ OWASP Dependency-Check fails

### Teaching Point:

> Developers cannot bypass quality gates by disabling tests.

---

# 🔴 Phase 4 — SonarCloud Static Analysis Failure

## Branch:

`feature/add-code-smells`

### Action:

* Add duplicated logic
* Add long methods
* Add empty catch block
* Add unused variables

SonarCloud flags:

* Code smells
* Maintainability issues

### Expected Result:

❌ Sonar Quality Gate failed

---

# 🟢 Phase 5 — Final Clean PR

## Branch:

`feature/final-clean-implementation`

### Action:

* Proper tests
* Good coverage
* Clean code
* No vulnerabilities
* No Sonar issues

### Expected Result:

✅ All checks green
PR allowed to merge

---

# 📊 Visual Pipeline Diagram

```
Pull Request
     ↓
GitHub Actions
     ↓
Build
     ↓
Tests
     ↓
JaCoCo Gate
     ↓
OWASP Gate
     ↓
SonarCloud Gate
     ↓
Merge Allowed / Blocked
```

---

# 🧠 What You Explain During Demo

## 1️⃣ What Is a Quality Gate?

A measurable, automated decision point that determines whether code meets defined quality standards.

---

## 2️⃣ Why Multiple Gates?

Because quality is multi-dimensional:

* Correctness
* Testability
* Security
* Maintainability
* Reliability

---

## 3️⃣ Why in CI/CD?

Early detection is cheaper.

> “Shift Left Quality”

---

# 🗂️ Repository Structure

```
quality-gates-demo/
 ├── .github/workflows/
 │     └── pull-request.yml
 ├── build.gradle.kts
 ├── settings.gradle.kts
 ├── src/main/java
 ├── src/test/java
 └── README.md
```

---

# 🎤 Suggested Live Demo Script

### Step 1:

Show main branch → Green pipeline

### Step 2:

Open PR from `feature/reduce-coverage`
→ Show failed coverage gate

Explain JaCoCo.

### Step 3:

Open next PR
→ Show failed tests

Explain difference between coverage and correctness.

### Step 4:

Show security failure

Explain supply-chain risk.

### Step 5:

Final clean PR
→ All green

Conclude:

> Quality Gates act as automated guardians of production code.

---

# 🏆 Learning Outcomes for Class

Students will understand:

* CI-based enforcement
* Automated quality metrics
* DevOps quality integration
* Branch protection strategy
* Objective quality measurement

---

# ⏱ Suggested Timing (20–25 min demo)

| Part                    | Time   |
| ----------------------- | ------ |
| Theory intro            | 5 min  |
| Architecture overview   | 3 min  |
| Failing PRs walkthrough | 10 min |
| Final success           | 3 min  |
| Q&A                     | 5 min  |

---

# 🚀 Optional Advanced Additions

If you want to impress:

* Add branch protection rules
* Require PR reviews
* Add mutation testing (Pitest)
* Add Docker build stage
* Add SBOM generation

---

# 🎓 Final Message for Presentation

> “Quality Gates transform quality from subjective opinion into objective, enforceable policy embedded in the delivery pipeline.”

