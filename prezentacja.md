---
marp: true
theme: default
paginate: true
---

# Quality Gates

### Automatyczne bramki jakości w CI/CD

<br/>

**Jakub Jaworski (70203)**
Zarządzanie Jakością Oprogramowania

---

# Co to jest CI/CD?

**Continuous Integration / Continuous Delivery**

- Każda zmiana kodu jest automatycznie budowana i testowana
- Błędy są wykrywane natychmiast, nie dopiero przygotowując wdrożenie
- Cel: skrócić pętlę informacji zwrotnej

<br/>

> *„Im wcześniej wykryjesz błąd, tym taniej go naprawisz."*

---

# Czym jest Quality Gate?

**Automatyczny punkt decyzyjny** — kod przechodzi dalej tylko wtedy, gdy spełnia zdefiniowane standardy jakości.

<br/>

- Mierzalne kryteria, nie subiektywna ocena
- Wymuszane przez pipeline — nie można ich ominąć
- Blokuje merge do głównej gałęzi, jeśli warunek bramki nie jest spełniony

---

# Dlaczego wiele bramek?

Jakość ma wiele wymiarów — każda bramka pilnuje innego aspektu.

| Bramka              | Co sprawdza?                        |
| ------------------- | ----------------------------------- |
| Testy jednostkowe   | Poprawność logiki biznesowej        |
| JaCoCo              | Pokrycie kodu testami (≥ 80%)       |
| Trivy               | Podatności w zależnościach (CVE)    |
| SonarCloud          | Code smells, duplikacje, błędy      |

<br/>

> Zaliczenie jednej bramki **nie zastępuje** pozostałych.

---

# Nasze bramki — stos technologiczny

- **Java 21** + **Spring Boot 3.5.11** + **Gradle (Kotlin DSL)**
- **JUnit 5** — testy jednostkowe i integracyjne
- **JaCoCo** — raport i weryfikacja pokrycia (min. 80%)
- **Trivy** + **CycloneDX SBOM** — skanowanie podatności
- **SonarCloud** — statyczna analiza i Quality Gate
- **GitHub Actions** — egzekucja całego pipeline'u

---

# Przepływ pipeline'u

```
Pull Request
     ↓
Budowanie projektu
     ↓
Bramka 1 — Testy (wszystkie muszą przejść)
     ↓
Bramka 2 — JaCoCo (pokrycie ≥ 80%)
     ↓
Bramka 3 — Trivy (brak HIGH/CRITICAL CVE)
     ↓
Bramka 4 — SonarCloud Quality Gate
     ↓
✅ Merge dozwolony  /  ❌ Merge zablokowany
```

---

# 🖥️ Demo



> *„Quality Gates zamieniają jakość z subiektywnej opinii*
> *w obiektywną, egzekwowalną politykę wbudowaną w pipeline."*

---

# Scenariusze demo

| Gałąź                              | Która bramka pada?     |
| ---------------------------------- | ---------------------- |
| `feature/reduce-coverage`          | ❌ JaCoCo < 80%        |
| `feature/fix-coverage-break-tests` | ❌ Testy jednostkowe   |
| `feature/add-vulnerability`        | ❌ Trivy (CVE CRITICAL)|
| `feature/add-code-smells`          | ❌ SonarCloud          |
| `feature/final-clean-implementation` | ✅ Wszystkie zielone |

---
<center><h1>Dziękuję za uwagę! :)</h1></center>