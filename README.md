# QuickFill

**QuickFill** is a localized "Data Vault" for Android that eliminates repetitive typing. Built with the modern Android stack, it leverages the **Autofill Framework** to inject user-defined snippets (like addresses, ID numbers, and social media boilerplate) directly into input fields across the OS.

---

## 💡 Why QuickFill?

Most productivity apps use **Accessibility Services** to "read" the screen and paste data. However, modern Android versions are increasingly restricting these services due to security and performance concerns. 

QuickFill is built "The Android Way":
- **Native Autofill API:** Uses the official system-level framework for data injection.
- **Privacy First:** No Accessibility permissions required. No cloud syncing. Everything stays in your local Room database.
- **Context Aware:** Automatically filters snippets based on the category of the app you are currently using (Social, Finance, etc.).

## 🛠 Tech Stack

- **Kotlin 2.1.0:** Utilizing the latest language features and performance.
- **Jetpack Compose:** A modern, reactive UI for managing your snippet vault.
- **Hilt (Dagger):** Dependency injection for a clean, testable architecture.
- **Room Database:** Local-first persistent storage with Coroutines/Flow support.
- **KSP (Kotlin Symbol Processing):** Optimized build times for code generation.

---

## 📱 System Requirements & Version Behavior

QuickFill leverages the Android Autofill Framework, which has evolved significantly over different Android versions. The app adapts its UI and logic based on your device's API level.

### **Minimum Requirements**
- **Android 8.0 (API 26) or higher:** Required for the base Autofill Service functionality.

### **Version-Specific Behavior**

| Android Version | Behavior & UI Features |
| :--- | :--- |
| **Android 8.0 - 9.0** (API 26-28) | Uses legacy **Dropdown UI**. Snippets appear in a system-rendered menu below the input field. |
| **Android 11 - 12L** (API 30-32) | Supports **Inline Suggestions**. Snippets appear as "chips" or "pills" inside compatible keyboards (e.g., Gboard). Falls back to dropdown if the keyboard doesn't support inline suggestions. |
| **Android 13+** (API 33+) | Uses the modern **Field & Presentation API**. Offers the most stable performance and supports high-resolution app icons in the suggestions. |

### **Limitations**
- **App Compatibility:** Some apps (e.g., certain browsers or custom views) do not provide the necessary "Autofill Hints." QuickFill may not be able to detect input fields in these apps.
- **Keyboard Support:** Inline suggestions (chips above the keyboard) require a compatible Input Method Editor (IME) like Gboard or Samsung Keyboard.
- **Package Visibility:** On Android 11+, system restrictions limit how apps "see" each other. QuickFill uses a metadata caching strategy to ensure app icons and labels are displayed correctly even with these restrictions.
- **Service Activation:** Like all password managers, you must manually enable QuickFill in **Settings > System > Languages & Input > Autofill service**.

---

## 📁 Project Structure

The project uses a feature-based clean architecture:

```text
com.byteutility.dev.quickfill/
├── data/
│   ├── entity/        # Room entities (Snippet, Category, etc.)
│   ├── dao/           # DAO interfaces for database operations
│   └── db/            # Room database configuration
│
├── di/                # Hilt modules (database, repositories, services)
│
├── service/           # AutofillService implementation
│                      # Handles FillRequest & SaveRequest logic
│
├── ui/
│   ├── list/          # Snippet list screen
│   ├── add/           # Add/Edit snippet screen
│   ├── setup/         # Autofill setup guidance screen
│   └── components/    # Reusable Compose UI components
│
└── util/              # App package/category detection
                       # ViewNode traversal & autofill field parsing
```
---
## 🚀 Getting Started
#### 1. Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK 26 (Oreo) or higher

---
#### 2. Installation

1. Clone the repository.
2. Build the project in Android Studio.
3. Install the generated APK on your device.
4. Launch the app.
5. Select **QuickFill** as your active Autofill provider.

---

#### 3. Usage

1. Open the app.
2. Create a new snippet:
   - Add a **label** (e.g., `Work Email`)
   - Add a **value** (e.g., `yourname@company.com`)
   - Assign a **category** (e.g., `WORK`)
3. Save the snippet.

Now, open any app and tap on a text field.  
Your saved snippet will appear as a suggestion (either as a keyboard suggestion pill or in a dropdown).

---
## 🚧 Roadmap

- **Advanced Filtering**  Improve package-name mapping to better classify apps currently marked as "Undefined".
- **Encryption**  Integrate a SQLCipher layer with the Room database to securely store sensitive data.
- **Search**  Add quick search functionality within the Snippet List.
- **App Specific**  Support app specific Snippet addition.

---
## 🤝 Contributing

This project started as a personal use-case and serves as a Proof of Concept (PoC).

If you have ideas to improve the Autofill logic or enhance the overall experience, feel free to open an issue or submit a pull request. Contributions are welcome!
