# AUDIT HANDOFF DOCUMENT — KBZHU Racion (КБЖУ Рацион)

**App Version:** `1.18.3` (`versionCode = 51`)  
**Project Name:** KBZHU Racion  
**Date of Handoff:** October 17, 2026  

---

## 1. Environment & Build Configuration

- **Kotlin Version:** `2.0.21` (with Compose Compiler plugin `2.0.21`)
- **Android Gradle Plugin (AGP):** `8.7.2`
- **Gradle Version:** `8.9` (Wrapper)
- **Compile SDK:** `36` (Android 15 / Baklava Preview)
- **Target SDK:** `36`
- **Min SDK:** `26` (Android 8.0 Oreo)
- **Java Compatibility:** Java 17

---

## 2. Project Modules & Architecture

The project is structured as a native Android Single-Module Application (`:app`) following modern **Android Architecture Recommendations**:
- **UI Layer:** 100% Jetpack Compose with Material 3 design system, dark/light theme support, custom animated navigation bar (`AppTab`: Dashboard, Diary, Catalog, Profile, Settings), and responsive layouts.
- **State Management:** `ViewModel` (`MainViewModel`) exposing read-only `StateFlow` / `SharedFlow` state and events (`collectAsStateWithLifecycle`).
- **Data Layer:** `KbzhuRepository` coordinating local SQLite persistence (`AppDatabase` via Room) and remote API services.
- **Local Persistence:** Room Database v6 with SQLite open helper migrations (v1 → v6).
- **Background Operations:** WorkManager (`InactivityReminderWorker`) for inactivity reminders and Health Connect manager for step tracking.

---

## 3. Libraries & Dependencies

| Library / Component | Version | Purpose |
| :--- | :--- | :--- |
| `androidx.compose.bom` | `2024.10.01` | Jetpack Compose BOM |
| `androidx.compose.material3` | BOM | Material 3 UI components & styling |
| `androidx.room` | `2.6.1` | Local SQLite ORM with KSP code generation |
| `androidx.work:work-runtime-ktx` | `2.9.1` | Background WorkManager for reminders |
| `androidx.health.connect:connect-client` | `1.1.0-alpha11` | Health Connect step tracking integration |
| `com.squareup.okhttp3:okhttp` | `4.12.0` | HTTP client for Open Food Facts & Gemini AI |
| `com.squareup.moshi:moshi-kotlin` | `1.15.1` | JSON serialization for backups & AI parsing |
| `org.robolectric:robolectric` | `4.13` | JVM-based Android framework unit testing |
| `junit:junit` | `4.13.2` | Unit test framework |

---

## 4. Database & Room Migrations

- **Database Name:** `kbzhu_database.db`
- **Current Room Version:** `6` (`exportSchema = true`, schemas exported in `app/schemas/`)
- **Entities:** `Product`, `MealEntry`, `UserProfile`, `WaterEntry`, `WeightEntry`, `SavedMeal`

### Explicit Migration Path (1 → 6)
All migrations are defined in `AppDatabase.kt` and registered in `addMigrations(...)`:
- `MIGRATION_1_2` (v1 → v2): Creates `weight_entries` table (`dateString` PRIMARY KEY, `weightKg`, `note`).
- `MIGRATION_2_3` (v2 → v3): Creates `saved_meals` table (`id` AUTOINCREMENT, `name`, `itemsJson`, `timestamp`).
- `MIGRATION_3_4` (v3 → v4): Alters `products` table, adding `barcode TEXT` and `isCustom INTEGER NOT NULL DEFAULT 0`.
- `MIGRATION_4_5` (v4 → v5): Alters `user_profile` table, adding `targetWeightKg REAL NOT NULL DEFAULT 68.0`.
- `MIGRATION_5_6` (v5 → v6): Alters `products` table, adding `isFavorite INTEGER NOT NULL DEFAULT 0`, and creates `saved_meals` if not exists.

> **Note:** Destructive migrations (`fallbackToDestructiveMigration`) are explicitly disabled. Full migration unit tests covering 1→2, 2→3, 3→4, 4→5, 5→6 exist in `MigrationTest.kt`.

---

## 5. Gemini AI Integration & Key Resolution

- **Service Class:** `GeminiFoodService` (`app/src/main/java/com/example/data/service/GeminiFoodService.kt`).
- **Key Resolution Order:**
  1. User custom key entered in Settings screen (`custom_gemini_api_key` in `SharedPreferences`).
  2. `BuildConfig.GEMINI_API_KEY` passed via `local.properties` or environment variable.
  3. If no key is provided, Gemini AI features fail safely with `AiError.NotConfigured` without crashing or making network calls.
- **Model Fallback & Safety:**
  - Candidate models: `gemini-3.5-flash-lite`, `gemini-2.5-flash`, `gemini-2.5-flash-lite`, `gemini-2.0-flash`.
  - On HTTP `401`/`403` (Unauthorized/NotConfigured), request execution stops immediately and returns the exact error. On HTTP `429` (QuotaExceeded), candidate models are retried sequentially before reporting quota limits.
  - Secrets and API keys are stored via `EncryptedSharedPreferences` and never printed in logcat.

---

## 6. Key Fixes & Improvements Implemented

1. **Food Tier System (S–D TierMaker Style):**
   - Implemented in `FoodTierSystem.kt`.
   - Exact word-boundary matching for English & Russian food names.
   - Strict macro validation for S-Tier lean protein (`protein >= 18g`, `fat <= 4.2g`, `carbs <= 3g`, `calories <= 170 kcal`).
   - Calibrated thresholds so S-Tier items represent `<= 15%` of the predefined food database.
2. **Open Food Facts API Validation (`OpenFoodFactsService.kt`):**
   - Distinguishes absent JSON nutrients from actual `0.0` values.
   - Validates kcal (`0.1..900.0`), protein/fat/carbs (`0..100g`), and total macro sum (`<= 100.5g`). Returns `Incomplete` if incomplete.
3. **Gemini AI Parser Refactoring (`GeminiFoodService.kt`):**
   - Removed all fictitious default values (`100 kcal`, `5g protein`, etc.).
   - Mandatory missing or unplausible fields throw `AiException(AiError.BadResponse)`.
4. **Safe Backup Import & Export (`MainViewModel.kt`, `KbzhuRepository.kt`):**
   - Export embeds `format = "kbzhu-backup"`.
   - Import checks file size, format signature, non-empty data, and version scheme (`5..6`). Rejects empty JSON `{}` or corrupted files without touching the DB.
   - Atomic database restore wrapped in Room `database.withTransaction`.
5. **Pedometer & Health Connect (`HealthConnectManager.kt`):**
   - `TYPE_STEP_COUNTER` registration requires `ACTIVITY_RECOGNITION` permission on Android 10+ (API 29+).
   - Sensor steps, manual offsets, and Health Connect steps are stored in distinct SharedPreferences keys.
6. **Full Bilingual Localization (100% RU/EN):**
   - Seamless language toggle in Settings (`_appLanguage`).
   - Bi-directional category matching in Catalog (`"Мясо и птица"` <-> `"Meat & Poultry"`).

---

## 7. Automated Test Suite Results

All **60 unit and Robolectric tests** pass with 0 failures:

```
Task :app:testDebugUnitTest
Test Summary: 60 passed, 0 skipped, 0 failed
```

### Key Test Classes:
- `FoodTierSystemTest.kt`: S-Tier percentage <= 15%, baked fish, liver, sugar/honey non-S validation, RU/EN name equivalence.
- `GeminiFoodServiceTest.kt`: Robolectric test for Gemini JSON parsing and error throwing on missing fields.
- `BackupImportExportTest.kt`: Tests JSON export formatting and `{}` rejection.
- `MigrationTest.kt`: Robolectric tests for Room migrations v1 → v6.
- `KbzhuCalculatorTest.kt`: Mifflin-St Jeor formula edge case validation.
- `SmartFoodSearchTest.kt`: Smart search and sorting tests.

---

## 8. Known Warnings & Verification Status

- **Build Status:** `assembleDebug` builds cleanly (0 errors).
- **Unit Test Status:** `testDebugUnitTest` passes 100% (53/53 tests passed).
- **ProGuard / R8:** Keep rules defined in `proguard-rules.pro` for Moshi JSON adapters and Room Entities.
- **APK Artifacts:** Generated debug APKs `kbzhu-debug.apk` and `app-debug.apk` available on Desktop.
