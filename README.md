# 📚 Porua — Learn from your mistake

> *"Knows where you're weak, plans what to study next, works offline."*

Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database (Offline-First)**, and **MVVM Architecture** for the Android App Development Assignment.

---

## 📱 Features & Implemented Screens

1. **01 · Splash (`SplashScreen.kt`)**
   - Brand Crimson-to-Dark gradient background (`#0A0A0A` → `#E11D2E`).
   - `P✦` logo and offline question bank preparation progress indicator.
   - Version & group footer (`v1.0 • Group 8 • INTI`).

2. **02 · Welcome (`WelcomeScreen.kt`)**
   - Hero preview illustration card (64% Chemistry Mastery gauge, "Weak: Alcohols", "Works offline" badge).
   - Core value proposition and 3 key benefit points.
   - Action buttons: "Get started" and "Restore from backup".

3. **03 · Exam Track (`ExamTrackScreen.kt` - Step 1 of 4)**
   - Segmented 4-step progress indicator.
   - Radio selection for NCTB tracks: SSC, HSC, University admission.
   - Group selection chips: Science, Business Studies, Humanities.

4. **04 · Subjects & Exam Date (`SubjectsDateScreen.kt` - Step 2 of 4)**
   - 2-column selectable subject cards with icons (Physics, Chemistry, Higher Math, English, Biology, ICT).
   - Exam date card with live countdown calculation (`HSC • 12 Apr 2027 • 186 days`).
   - Daily study time goal selector (30 min, 1 hour, 2 hours, 3+ hours).

5. **05 · Diagnostic Intro (`DiagnosticIntroScreen.kt` - Step 4 of 4)**
   - Bullseye target illustration.
   - 3 stat cards: 20 questions, ~12 minutes, 100% offline.
   - On-device ML explanation card ("Nothing leaves your device").

6. **06 · Home (`HomeScreen.kt`)**
   - User greeting header ("Good evening, Ratul • HSC 2027 • Science • 🔥 12-day streak").
   - Hero exam readiness gradient card (`186 days to HSC • 58% exam-ready`).
   - Today's plan interactive checklist (tappable completion checkmarks).
   - "PORUA AI INSIGHT" On-device ML prediction card.
   - Weak areas chips (`Alcohols 34%`, `Electrolysis 31%`) and pill button `✦ Ask Porua`.
   - 5-destination bottom navigation bar.

7. **07 · Practice Hub (`PracticeHubScreen.kt`)**
   - "You're offline — practice, plan & ML insights still work" warning/status banner.
   - "Practice with Porua AI" hero interactive card.
   - Smart mix & Review due cards with SM-2 schedule count.
   - Subject list with circular Mastery Rings (Chemistry 62%, Physics 71%, Higher Math 48%).

8. **08 · Chapters (`ChaptersScreen.kt`)**
   - Subject chapter overview with 62% chapter mastery ring.
   - Filter chips: All, Weak first, Not started, Bookmarked.
   - Chapter cards with color-coded progress bars and percentage badges.

9. **09 & 10 · Question & Feedback (`QuestionScreen.kt`)**
   - Quiz session with timer (`0:42`), progress bar, and topic chips.
   - 4-option selection with radio circle states (A, B, C, D).
   - Instant feedback with correct/incorrect highlight and textbook explanation.
   - "✦ Explain with AI" button.
   - **SuperMemo-2 (SM-2) Spaced Repetition engine rating controls**:
     - Again (< 1 min)
     - Hard (6 min)
     - Good (1 day)
     - Easy (4 days)

10. **18 & 18c · Ask Porua AI Chat & Privacy Consent (`AskPoruaScreen.kt`)**
    - Step-by-step problem-solving tutor with NCTB syllabus references.
    - Cloud AI consent bottom sheet with 4-point privacy checklist.
    - Quick prompt suggestions and bottom input bar with camera & mic shortcuts.

11. **19 · AI Insights (`AiInsightsScreen.kt`)**
    - Forgetting risk tracker for next 7 days (Electrolysis 82%, Alcohols 74%, etc.).
    - Mastery forecast line chart (Actual vs on-device ML prediction).
    - Study habit insights: Best time (6–8 AM) & Rushing numericals alerts.

12. **20 & 20b · Snap a Question & OCR Results (`SnapQuestionScreen.kt`)**
    - Camera viewfinder with ML Kit text detection boundary and scan line.
    - OCR recognized text card with 97% confidence tag.
    - "✦ Solve with AI" button + instant matching questions from local offline bank.

13. **Mastery Map (`ProgressScreen.kt`)**
    - Per-subject and per-chapter longitudinal analytics.
    - Weak topics attention list.

14. **Study Plan & Task Manager (`PlanScreen.kt`)**
    - Day-by-day revision schedule with Room database CRUD.
    - Dialog to add custom revision tasks with input validation (TextField error state, DropdownMenu, Slider, RadioButtons) and Snackbar feedback.

15. **Profile & Settings (`MeScreen.kt`)**
    - User stats, exam target date settings, and offline mode / cloud AI toggles.

---

## 🏗 Architecture & Design System

- **Clean Architecture & MVVM**:
  - `data/local/`: Room Database (`AppDatabase`), Entities, DAOs.
  - `data/repository/`: `PoruaRepository` for clean abstraction.
  - `domain/sm2/`: Mathematical implementation of SuperMemo 2 algorithm.
  - `ui/viewmodel/`: `PoruaViewModel` with `StateFlow`.
  - `ui/theme/`: Crimson Light/Dark color tokens, Plus Jakarta typography, 4-pt grid spacing, corner radii.
  - `ui/components/`: Reusable components matching Figma design library (`MasteryRing`, `TopicBar`, `BottomNav`, `TopBar`, `AiInsightCard`, `Banner`, etc.).
