# 📚 Porua — Learn from your mistake

![Porua App](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat)
![Architecture](https://img.shields.io/badge/Architecture-Offline--First-FF9900)

**Porua** (a devoted learner in Bangla) is an offline-first, AI-powered study companion designed specifically for Bangladeshi SSC and HSC students. Existing educational apps deliver abundant content but fail to tell students where they are weak, do not plan revisions around exam dates, and often fail offline. Porua solves this by diagnosing weak topics, generating personalized spaced-repetition plans, and working reliably without an active internet connection.

🔗 **[Figma UI/UX Design Link](https://www.figma.com/design/p8xg5GtAUUhRQ8rkEjbbml/Porua-%25E2%2580%2594-UI-UX-Design--Assignment-2-?node-id=0-1&p=f&t=N3IQ6weJu9CK8XZG-0)**

---

## ✨ Key Features (MVP)

*   **🔍 Weak-Topic Detection:** Analyzes quiz/practice results and generates a visible, drillable mastery map per subject and topic.
*   **📅 Personalized Revision Plan:** Reads the user's exam date and subject list to auto-generate a day-by-day study plan (Revise / Practise / Learn).
*   **🧠 Spaced-Repetition Engine (SM-2):** An on-device scheduler that resurfaces weak and older topics at optimal intervals to prevent forgetting.
*   **📊 Progress Dashboard:** Visualizes mastery and improvement per subject over weeks, complete with an exam countdown.
*   **✈️ Offline-First Practice:** Features a local NCTB-aligned question bank. All performance data is stored securely on-device with optional cloud sync.
*   **🤖 On-Device AI Integration:** 
    *   **Ask Porua:** AI chat for context-aware, step-by-step doubt solving (Bangla/English).
    *   **AI Insights:** Generates actionable study patterns based on past attempt data.
    *   **Snap a Question:** CameraX capture with on-device text recognition (ML Kit) for instant solutions.

---

## 🛠️ Technology Stack

*   **Platform:** Native Android (Kotlin)
*   **UI Framework:** Jetpack Compose & Material Design 3
*   **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
*   **Local Database:** Room (Encrypted with SQLCipher)
*   **Asynchronous Programming:** Coroutines & Kotlin Flow
*   **Dependency Injection:** Dagger Hilt
*   **Background Tasks:** WorkManager (for spaced-repetition reminders)
*   **Preferences:** Jetpack DataStore
*   **AI & ML:** Gemini Nano (via ML Kit GenAI for on-device processing) & Gemini API (Cloud fallback)
*   **Security:** Android Keystore for key management, strict Network Security Config.

---

## 👥 Team & Task Distribution (Group 8)

Every screen, file, and decision has a specific owner to ensure seamless collaboration without merge conflicts.

| Member | Role | Key Responsibilities |
| :--- | :--- | :--- |
| **MD Ratul Hossen** | UI/UX Designer & AI Lead | Figma designs, Compose Theme/Components, Onboarding, AI integrations (Screens 01-05, 18-20). |
| **Abiduzzaman Rahim** | Frontend Developer | Compose UI for main screens (06-17), Navigation Graph, ViewModels. |
| **Saniya Sachin Pendhari** | Database & Data Layer | Room DB, DAOs, SM-2 Spaced Repetition logic, mastery calculations, Repositories. |
| **Muhammad Rehan Kuaiser** | Security & Privacy Lead | SQLCipher encryption, Keystore, API key protection, AI safety filtering, Privacy policy. |
| **Sumayya Syed** | Content, QA & Docs | NCTB JSON Question Bank (300+ MCQs), QA/Bug tracking, UI Testing, Final Report. |

---

## 🚀 Getting Started

### Prerequisites
*   [Android Studio](https://developer.android.com/studio) (Latest version recommended)
*   JDK 17 or higher
*   An Android device or emulator running API level 24+ (For Gemini Nano features, a compatible modern device is required).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-org/porua.git
    ```
2.  **Open the project:**
    Open Android Studio and select `File > Open`, then navigate to the cloned `porua` directory.
3.  **Setup Environment Variables:**
    *   Create a `local.properties` file in the root directory if it doesn't exist.
    *   Add your Gemini API key (for cloud fallback testing) to `local.properties`:
        ```properties
        GEMINI_API_KEY=your_api_key_here
        ```
4.  **Build and Run:**
    Sync the Gradle project and click the Run button (`Shift + F10`) to deploy the app to your device or emulator.

---

## 🔒 Security & Privacy

Since Porua targets young learners, privacy is a primary focus. 
*   **Data Residency:** Student performance data never leaves the device unless explicitly requested. 
*   **Encryption:** The local Room database is heavily encrypted using SQLCipher.
*   **AI Safety:** Prompts are strict, heavily filtered for safety, and block personal data from being transmitted.

---

## 📝 License

This project is created for **Assignment 2** and is for educational purposes.
