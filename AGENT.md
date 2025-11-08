**I. Core Architectural Principles (The "Lawnchair Way")**

* **Pragmatic Modernization:** The primary architectural goal is to incrementally refactor the
  legacy AOSP codebase towards a modern, testable, and maintainable structure. We do not rewrite for
  the sake of rewriting.
* **Decoupling & Abstraction:** New features and refactors must prioritize decoupling from the core
  `Launcher3` (`src`) module wherever possible. The preferred pattern is to create clean Kotlin
  interfaces (`lawnchair` package) that act as a bridge to legacy systems.
* **Unidirectional Data Flow (UDF) for UI:** All new, complex UI screens (especially in Settings)
  are to be built using a ViewModel-centric UDF architecture. State should be exposed via
  `StateFlow` and consumed by "dumb" Jetpack Compose UI.
* **Embrace Kotlin & Coroutines:** All new asynchronous code must use Kotlin Coroutines, preferably
  `Flow`. Legacy callbacks and `AsyncTask`-style patterns are to be eliminated during refactoring.
* **Respect for Constraints:** The architecture must work within the project's real-world
  constraints:
* **DI is a Service Locator:** We use a `MainThreadInitializedObject` singleton pattern, not
  Hilt/Dagger. Note that this pattern is slowly being migrated to use Hilt/Dagger (without the
  Android Gradle Plugin) by Google, so expect inconsistency.
