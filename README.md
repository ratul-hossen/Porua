# 📚 Porua — Learn from your mistake

![Version](https://img.shields.io/badge/Version-v2.0.0-E11D2E?style=flat&logo=android)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat)
![Architecture](https://img.shields.io/badge/Architecture-Offline--First-FF9900)

> *"Knows where you're weak, plans what to study next, works offline."*

**Porua** (a devoted learner in Bangla) is an offline-first study companion designed specifically for Bangladeshi SSC and HSC students following the NCTB curriculum. Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database (Offline-First)**, and **MVVM Architecture** for the Mobile Application Development Project (INTI International University).

🔗 **[Figma UI/UX Design Link](https://www.figma.com/design/p8xg5GtAUUhRQ8rkEjbbml/Porua-%25E2%2580%2594-UI-UX-Design--Assignment-2-?node-id=0-1&p=f&t=N3IQ6weJu9CK8XZG-0)**

---

## 👥 Team & Task Distribution (Group 8)

| Member | Role | Key Responsibilities |
| :--- | :--- | :--- |
| **MD Ratul Hossen** | UI/UX Designer & AI Lead | Figma designs, Compose Theme/Components, Onboarding, AI integrations (Screens 01-05, 18-20). |
| **Abiduzzaman Rahim** | Frontend Developer | Compose UI for main screens (06-17), Navigation Graph, ViewModels. |
| **Saniya Sachin Pendhari** | Database & Data Layer | Room DB, DAOs, SM-2 Spaced Repetition logic, mastery calculations, Repositories. |
| **Muhammad Rehan Kuaiser** | Security & Privacy Lead | SQLCipher encryption, Keystore, API key protection, AI safety filtering, Privacy policy. |
| **Sumayya Syed** | Content, QA & Docs | NCTB JSON Question Bank, QA/Bug tracking, UI Testing, Final Report. |

---

## ✨ What's New in Version 2.0.0

### 1. 🎯 Adaptive 20-Question Practice & Remedial Loop
* **Standard 20-MCQ Sessions:** Every practice session serves 20 questions with 4 options ($A, B, C, D$) and a sticky `SUBMIT` button.
* **100% Mastery Path:** Scoring 20/20 unlocks celebration feedback and a direct **`NEXT SESSION`** progression button.
* **Learn from Mistakes (ভুল থেকে শেখো):** When mistakes occur (e.g. 5 wrong), the engine identifies their specific categories and builds a targeted **20-question remedial session** (5 failed questions + 15 similar questions from those weak categories).
* **Skip Option:** Learners can choose to skip remediation and advance directly.
* **100% Offline & $0 Cost:** All adaptive pooling and SM-2 schedules run on-device via Room SQLite.

### 2. 🌐 Universal Common Section (English & ICT)
* **Auto-Included for All Streams:** Whether a learner selects **Science**, **Commerce**, or **Arts**, the Common section is automatically available.
* **ICT:** Chapter 1 to 6 (Number Systems, HTML, C Programming, DBMS, Communications, World & BD).
* **English:** 1st & 2nd Paper (Grammar: Prepositions, Right form of verbs, Modifiers, Connectors, Narration).

### 3. 📊 Full Stream Support: Science, Commerce & Arts
* **Science:** Physics, Chemistry, Biology, Higher Mathematics.
* **Commerce:** Accounting, Business Organization & Management, Finance & Banking.
* **Arts (Humanities):** Economics, Civics & Good Governance, History.

### 4. 🔑 In-App BYOK (Bring Your Own Key) for Gemini & Grok
* Settings (`MeScreen`) includes an **"AI Configuration"** section.
* Users can input their free Google Gemini API Key and Grok API Key directly in the app.
* Securely stored in `DataStore`; no keys are committed to Git or leaked.

---

## 📱 Core Features & Implemented Screens

1. **01 · Splash (`SplashScreen.kt`)** — Brand Crimson gradient (`#0A0A0A` → `#E11D2E`), offline bank init, version `v2.0.0`.
2. **02 · Welcome (`WelcomeScreen.kt`)** — Hero preview card, value props, "Sign Up" and "Login" with session management.
3. **03 · Exam Track (`ExamTrackScreen.kt`)** — NCTB tracks (SSC, HSC, Admission) and stream selection (Science, Commerce, Arts).
4. **04 · Subjects & Exam Date (`SubjectsDateScreen.kt`)** — Selectable subject cards, live countdown calculation, study goal selector.
5. **05 · Diagnostic Intro (`DiagnosticIntroScreen.kt`)** — Bullseye illustration, 100% offline indicator, on-device ML card.
6. **06 · Home (`HomeScreen.kt`)** — Greeting header, exam countdown, streak tracker, readiness %, Today's plan, AI insight card.
7. **07 · Practice Hub (`PracticeHubScreen.kt`)** — Common, Science, Commerce, and Arts subjects with Circular Mastery Rings.
8. **08 · Chapters (`ContentChaptersScreen.kt`)** — Subject chapter overview, filter chips, mastery percentage badges.
9. **09 & 10 · Quiz & Practice (`QuizScreen.kt`)** — 20-MCQ session, timer, 4 options, bottom `SUBMIT` button.
10. **11 · Results & Adaptive Feedback (`QuizResultScreen.kt`)** — Score report, 100% `NEXT SESSION`, `Learn from Mistakes` (20-MCQ remedial session) and `Skip`.
11. **18 · Ask Porua AI Chat (`AskPoruaScreen.kt`)** — Step-by-step doubt solver with NCTB context, local chat history, consent sheet.
12. **19 · AI Insights (`AiInsightsScreen.kt`)** — 7-day forgetting risk tracker, mastery forecast line chart.
13. **20 · Snap a Question (`SnapQuestionScreen.kt`)** — CameraX viewfinder with OCR text detection and solution finder.
14. **Study Plan (`PlanScreen.kt`)** — Day-by-day revision schedule with Room CRUD and validation.
15. **Profile & Settings (`MeScreen.kt`)** — User stats, BYOK Gemini & Grok API key setup with DataStore persistence.

---

## 🏗 Architecture & Design System

- **Clean Architecture & MVVM**:
  - `data/local/`: Room Database (`AppDatabase` v4), DAOs (Subject, Chapter, Question, StudyTask, UserProfile, ChatHistory, QuizAttempt).
  - `data/content/`: NCTB Offline JSON Parser & Repository for Common, Science, Commerce, and Arts subjects.
  - `data/auth/`: Secure Password Hashing & DataStore session + BYOK AI Key storage.
  - `data/repository/`: `PoruaRepository` & `AuthRepository` for clean abstraction.
  - `domain/sm2/`: Mathematical implementation of SuperMemo 2 algorithm.
  - `domain/countdown/`: Dynamic countdown and available exam year provider with comprehensive unit tests.
  - `ui/viewmodel/`: `PoruaViewModel`, `ContentViewModel`, `QuizViewModel` with `StateFlow`.
  - `ui/theme/`: Crimson Light/Dark color tokens, Plus Jakarta typography, 4-pt grid spacing, corner radii.
  - `ui/components/`: Reusable components matching Figma design library (`MasteryRing`, `TopicBar`, `BottomNav`, `TopBar`, `AiInsightCard`, `Banner`, etc.).

---

## 🚀 Building & Running

### Prerequisites
* JDK 17 or higher
* Android SDK (API 35, minSdk 26)

### Local Build:
```bash
./gradlew assembleDebug
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk`

### Automated GitHub Release:
Tagging a release triggers GitHub Actions CI/CD to automatically build and publish the APK:
```bash
git push origin main
git push origin v2.0.0
```

---

## 🔒 Security & Privacy

*   **Zero API Key Leakage:** Keys are stored exclusively on the user's local device inside Android DataStore.
*   **100% Offline Core:** Question banks, Spaced Repetition algorithms, and Weakness Tracking execute locally on SQLite without network calls.
*   **AI Safety:** Privacy consent sheet is displayed before any external AI query is transmitted.

---

## 📝 License

This project is created for **Assignment 2** and is for educational purposes.
