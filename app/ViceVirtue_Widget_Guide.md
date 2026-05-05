# ViceVirtue — Widget Feature Guide

> **Agent Instructions**: This document is an addendum to `ViceVirtue_BuildGuide.md`. The main app (Room database, domain models, use cases, Hilt DI, theme) is already fully built. Do not re-implement or duplicate anything from the main guide. All references to `TrackableRepository`, `GetStreakUseCase`, `LogEventUseCase`, `TrackableType`, `ViceRed`, `VirtueBlue`, and other existing symbols assume they are already present and compiled. Follow every section in order.

---

## Table of Contents

1. [Widget Overview](#1-widget-overview)
2. [New Dependencies](#2-new-dependencies)
3. [Project Structure Additions](#3-project-structure-additions)
4. [Widget Configuration — Picker Screen](#4-widget-configuration--picker-screen)
5. [Widget State & Data Pipeline](#5-widget-state--data-pipeline)
6. [Widget Receivers & Workers](#6-widget-receivers--workers)
7. [Glance UI — Layout & Theming](#7-glance-ui--layout--theming)
8. [Quick-Log Action Flow](#8-quick-log-action-flow)
9. [AndroidManifest Additions](#9-androidmanifest-additions)
10. [Widget Visual Design Spec](#10-widget-visual-design-spec)
11. [Business Logic Rules](#11-business-logic-rules)

---

## 1. Widget Overview

### What the widget does

Each widget instance is **bound to exactly one Vice or Virtue** chosen by the user at placement time. The widget continuously shows:

- The trackable's **name** and **type icon** (💀 skull / 🛡 shield)
- The **current streak** with its label ("days clean" for Vice, "days strong" for Virtue)
- A single **action button** that records an event with no description:
  - Vice → button label: **"I failed"** — records that the user indulged (resets streak)
  - Virtue → button label: **"I triumphed"** — records that the user acted (extends streak)

Tapping anywhere on the widget body (other than the button) deep-links into the app's **Detail screen** for that trackable.

### Scope constraints

| Constraint | Detail |
|---|---|
| One trackable per widget | Each widget instance tracks exactly one Vice or Virtue |
| Multiple instances allowed | User can place multiple widgets, each for a different trackable |
| No description on widget log | Quick-log always uses `description = ""` (consolidation rules still apply) |
| Fully local | No network, no remote config |
| Glance framework | Widgets are built with **Jetpack Glance** — NOT classic `RemoteViews` |

---

## 2. New Dependencies

Add to `app/build.gradle.kts`:

```kotlin
// Glance (Compose-based widgets)
implementation("androidx.glance:glance-appwidget:1.1.0")
implementation("androidx.glance:glance-material3:1.1.0")

// WorkManager (for periodic streak refresh)
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Hilt + WorkManager integration
implementation("androidx.hilt:hilt-work:1.2.0")
kapt("androidx.hilt:hilt-compiler:1.2.0")
```

> **Agent Note**: `glance-material3` enables `GlanceTheme` with Material3 color roles. It does NOT automatically inherit the app's `MaterialTheme` — the widget theme is configured separately in Section 7.

---

## 3. Project Structure Additions

Add the following files to the existing project. Do not modify any existing files except where explicitly instructed.

```
com.vicevirtue/
│
├── widget/
│   ├── ViceVirtueWidget.kt           ← GlanceAppWidget subclass
│   ├── ViceVirtueWidgetReceiver.kt   ← GlanceAppWidgetReceiver
│   ├── WidgetConfigActivity.kt       ← Shown when user places a widget
│   ├── WidgetConfigViewModel.kt
│   ├── WidgetStateDefinition.kt      ← Glance StateDefinition (DataStore)
│   ├── WidgetActionCallback.kt       ← Handles "I failed" / "I did it" tap
│   ├── WidgetUpdateWorker.kt         ← WorkManager worker for periodic refresh
│   └── ui/
│       ├── ViceVirtueWidgetContent.kt  ← Glance Composable, root layout
│       └── WidgetTheme.kt              ← Glance color mapping
│
└── di/
    └── WorkerModule.kt               ← Hilt binding for WidgetUpdateWorker
```

### New XML resources

```
res/
├── xml/
│   └── vice_virtue_widget_info.xml   ← AppWidgetProviderInfo
└── layout/
    └── widget_loading.xml            ← Minimal initial RemoteViews fallback
```

---

## 4. Widget Configuration — Picker Screen

When the user places a ViceVirtue widget from their launcher, Android launches `WidgetConfigActivity` before the widget appears. The user selects which trackable to bind.

### 4.1 `WidgetConfigActivity.kt`

```kotlin
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the widget ID passed by the launcher
        val appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If no valid ID, abort immediately
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Default result = CANCELED so widget is not placed if user backs out
        setResult(RESULT_CANCELED)

        setContent {
            ViceVirtueTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onTrackableSelected = { trackableId ->
                        // Persist the (appWidgetId → trackableId) binding
                        viewModel.saveWidgetBinding(appWidgetId, trackableId)

                        // Trigger initial widget render
                        val resultIntent = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultIntent)

                        // Update the widget immediately
                        lifecycleScope.launch {
                            ViceVirtueWidget().update(this@WidgetConfigActivity, appWidgetId)
                            finish()
                        }
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}
```

---

### 4.2 `WidgetConfigViewModel.kt`

```kotlin
@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val repository: TrackableRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class ConfigUiState(
        val vices: List<Trackable> = emptyList(),
        val virtues: List<Trackable> = emptyList(),
        val isLoading: Boolean = true
    )

    val uiState: StateFlow<ConfigUiState> = repository.getAllTrackables()
        .map { list ->
            ConfigUiState(
                vices    = list.filter { it.type == TrackableType.VICE },
                virtues  = list.filter { it.type == TrackableType.VIRTUE },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConfigUiState())

    // Persists the widget→trackable binding using DataStore
    fun saveWidgetBinding(appWidgetId: Int, trackableId: Long) {
        viewModelScope.launch {
            context.widgetDataStore.edit { prefs ->
                prefs[widgetTrackableKey(appWidgetId)] = trackableId
            }
        }
    }
}
```

---

### 4.3 `WidgetConfigScreen` Composable

This is a standard Compose screen (inside `WidgetConfigActivity`). It must use `ViceVirtueTheme` (already applied in the activity above).

**Layout**:
- Top app bar: "Choose a Trackable" with a close (✕) button that calls `onCancel`
- Two sections, same visual pattern as Dashboard: **Vices** (skull header, red) and **Virtues** (shield header, blue)
- Each trackable is a selectable row:
  - `TypeIconCircle` (reuse existing component) + name text
  - Tapping the row immediately calls `onTrackableSelected(trackable.id)`
- Empty state if no trackables exist: "Add a Vice or Virtue in the app first."
- **No confirmation step** — selection is immediate and final

---

### 4.4 DataStore Keys for Widget Bindings

Create `data/widget/WidgetPreferences.kt`:

```kotlin
private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_preferences"
)

fun widgetTrackableKey(appWidgetId: Int): Preferences.Key<Long> =
    longPreferencesKey("widget_trackable_$appWidgetId")

suspend fun Context.getTrackableIdForWidget(appWidgetId: Int): Long? {
    return widgetDataStore.data.first()[widgetTrackableKey(appWidgetId)]
}

suspend fun Context.removeWidgetBinding(appWidgetId: Int) {
    widgetDataStore.edit { prefs ->
        prefs.remove(widgetTrackableKey(appWidgetId))
    }
}
```

> **Agent Note**: This DataStore is separate from any existing app DataStore. Use a distinct `name = "widget_preferences"` to avoid collisions.

---

## 5. Widget State & Data Pipeline

### 5.1 `WidgetStateDefinition.kt`

Glance needs a `GlanceStateDefinition` to persist state across widget renders. Use DataStore-backed Glance state.

```kotlin
// The data class that represents everything the widget needs to render
data class WidgetState(
    val trackableName: String = "",
    val trackableType: String = "",     // "VICE" or "VIRTUE"
    val streak: Int = 0,
    val isLoading: Boolean = true,
    val isError: Boolean = false        // true if trackable was deleted
)

object WidgetStateDefinition : GlanceStateDefinition<WidgetState> {

    private val Context.widgetStateDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "glance_widget_state")

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> =
        context.widgetStateDataStore

    override fun getLocation(context: Context, fileKey: String): File =
        File(context.applicationContext.filesDir, "datastore/glance_widget_state.preferences_pb")

    // Keys
    private val KEY_NAME     = stringPreferencesKey("name")
    private val KEY_TYPE     = stringPreferencesKey("type")
    private val KEY_STREAK   = intPreferencesKey("streak")
    private val KEY_LOADING  = booleanPreferencesKey("loading")
    private val KEY_ERROR    = booleanPreferencesKey("error")

    // ... implement serialize / deserialize as DataStore Preferences → WidgetState mapping
    // Agent: implement getDefaultState(), getDataStore(), and a companion updateState()
    // helper that writes all keys atomically.
}
```

> **Agent Note**: For simplicity, implement `WidgetStateDefinition` using `PreferencesGlanceStateDefinition` and store state as individual `Preferences` keys. Each `appWidgetId` will have its own Glance-managed state file. The keys above are per-widget because Glance namespaces them by widget ID automatically.

---

### 5.2 Data Flow Summary

```
Room DB ──► TrackableRepository ──► GetStreakUseCase
                                          │
                         ┌────────────────┘
                         │
                   WidgetUpdateWorker  (or ViceVirtueWidget.provideGlance)
                         │
                         ▼
                  updateAppWidgetState(context, WidgetStateDefinition, appWidgetId) { 
                      it.copy(name = ..., streak = ..., ...) 
                  }
                         │
                         ▼
               ViceVirtueWidget.update(context, appWidgetId)
                         │
                         ▼
              ViceVirtueWidgetContent (Glance Composable reads currentState())
```

No ViewModel exists inside the widget itself. Data flows in through `currentState<WidgetState>()` and is refreshed either by `WorkManager` or by direct `widget.update()` calls triggered by events.

---

## 6. Widget Receivers & Workers

### 6.1 `ViceVirtueWidget.kt`

```kotlin
class ViceVirtueWidget : GlanceAppWidget() {

    override val stateDefinition = WidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch fresh data every time the widget is rendered
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val trackableId = context.getTrackableIdForWidget(appWidgetId)

        if (trackableId == null) {
            // Widget placed but not configured — should not happen, but guard anyway
            updateAppWidgetState(context, WidgetStateDefinition, id) { state ->
                state.copy(isError = true, isLoading = false)
            }
        } else {
            // Resolve via repository (inject via EntryPoint — see note below)
            val repository = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            ).repository()

            val trackable = repository.getTrackableById(trackableId)

            if (trackable == null) {
                // Trackable was deleted from the app
                updateAppWidgetState(context, WidgetStateDefinition, id) { state ->
                    state.copy(isError = true, isLoading = false)
                }
            } else {
                val streak = GetStreakUseCase(repository).invoke(trackable)
                updateAppWidgetState(context, WidgetStateDefinition, id) { state ->
                    state.copy(
                        trackableName  = trackable.name,
                        trackableType  = trackable.type.name,
                        streak         = streak,
                        isLoading      = false,
                        isError        = false
                    )
                }
            }
        }

        provideContent {
            ViceVirtueWidgetContent()
        }
    }
}

// Hilt EntryPoint for accessing the repository outside of Hilt-injected classes
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): TrackableRepository
}
```

> **Agent Note**: `GlanceAppWidget` is not a Hilt component and cannot use `@Inject`. Use `EntryPointAccessors.fromApplication()` as shown above to access the Hilt object graph. `GetStreakUseCase` can be instantiated directly since it has no external dependencies beyond the repository.

---

### 6.2 `ViceVirtueWidgetReceiver.kt`

```kotlin
@AndroidEntryPoint
class ViceVirtueWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ViceVirtueWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Clean up DataStore bindings for deleted widget IDs
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { id ->
                context.removeWidgetBinding(id)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Handled by GlanceAppWidgetReceiver; override only if custom logic needed
    }
}
```

---

### 6.3 `WidgetUpdateWorker.kt`

Periodic worker that refreshes all active widget instances (streak updates as days pass).

```kotlin
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(ViceVirtueWidget::class.java)
            glanceIds.forEach { glanceId ->
                ViceVirtueWidget().update(applicationContext, glanceId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "ViceVirtueWidgetRefresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = java.util.concurrent.TimeUnit.MINUTES
            )
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
```

**Schedule this worker** at app startup. Add to `ViceVirtueApp.onCreate()`:

```kotlin
override fun onCreate() {
    super.onCreate()
    WidgetUpdateWorker.schedule(this)
}
```

> **Agent Note**: WorkManager's minimum periodic interval is 15 minutes. This is sufficient — streaks are calendar-day based. The worker also fires on boot (WorkManager handles this automatically with `androidx.work:work-runtime-ktx`).

---

### 6.4 `WorkerModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    @Provides
    fun provideWorkManagerInitializer(): WorkManagerInitializer =
        WorkManagerInitializer { context, configuration ->
            WorkManager.initialize(context, configuration)
        }
}

// Also add to di/DatabaseModule.kt or a new HiltWorkerFactory binding:
// The HiltWorkerFactory must be provided to WorkManager.
// In ViceVirtueApp, override getWorkManagerConfiguration():

@HiltAndroidApp
class ViceVirtueApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

> **Agent Note**: This replaces the default WorkManager initializer. Remove the `<provider>` for `WorkManagerInitializer` from `AndroidManifest.xml` and add `tools:node="remove"` as shown in Section 9.

---

## 7. Glance UI — Layout & Theming

### 7.1 `WidgetTheme.kt`

Glance cannot use Compose `MaterialTheme` directly. Map the app's color tokens to Glance's `ColorProviders`.

```kotlin
object ViceVirtueWidgetTheme {

    // Light/dark color pairs for each semantic role
    val viceRedProvider = ColorProvider(
        day   = Color(0xFFC0392B),  // ViceRed
        night = Color(0xFFE57373)   // ViceRedLight
    )
    val virtueBlueProvider = ColorProvider(
        day   = Color(0xFF2E4FA3),  // VirtueBlue
        night = Color(0xFF6B8DD6)   // VirtueBlueLight
    )
    val surfaceProvider = ColorProvider(
        day   = Color(0xFFFFFFFF),
        night = Color(0xFF1C1C1E)   // DarkSurface
    )
    val onSurfaceProvider = ColorProvider(
        day   = Color(0xFF0E0E0E),  // NeutralBlack
        night = Color(0xFFFFFFFF)   // NeutralWhite
    )
    val subtleProvider = ColorProvider(
        day   = Color(0xFF8E8E93),  // NeutralGray
        night = Color(0xFF8E8E93)
    )
    val viceSurfaceProvider = ColorProvider(
        day   = Color(0xFFFFF0F0),  // ViceRedSurface
        night = Color(0xFF2A1515)   // DarkViceSurface
    )
    val virtueSurfaceProvider = ColorProvider(
        day   = Color(0xFFF0F4FF),  // VirtueBlueSurface
        night = Color(0xFF111D36)   // DarkVirtueSurface
    )
    val streakGoldProvider = ColorProvider(
        day   = Color(0xFFFFB800),
        night = Color(0xFFFFB800)
    )
    val streakElectricProvider = ColorProvider(
        day   = Color(0xFF00C2FF),
        night = Color(0xFF00C2FF)
    )

    // Returns the appropriate type-color provider
    fun typeColorProvider(type: TrackableType) = when (type) {
        TrackableType.VICE    -> viceRedProvider
        TrackableType.VIRTUE  -> virtueBlueProvider
    }

    fun typeSurfaceProvider(type: TrackableType) = when (type) {
        TrackableType.VICE    -> viceSurfaceProvider
        TrackableType.VIRTUE  -> virtueSurfaceProvider
    }

    fun streakColorProvider(type: TrackableType) = when (type) {
        TrackableType.VICE    -> streakGoldProvider
        TrackableType.VIRTUE  -> streakElectricProvider
    }
}
```

---

### 7.2 `ViceVirtueWidgetContent.kt`

This is the root Glance Composable. It reads `currentState<WidgetState>()` and renders one of three states.

```kotlin
@Composable
fun ViceVirtueWidgetContent() {
    val state = currentState<WidgetState>()
    val context = LocalContext.current
    val glanceId = LocalGlanceId.current
    val appWidgetId = remember {
        runBlocking { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }
    }

    GlanceTheme {
        when {
            state.isLoading -> WidgetLoadingState()
            state.isError   -> WidgetErrorState()
            else -> {
                val type = TrackableType.valueOf(state.trackableType)
                WidgetReadyState(
                    state       = state,
                    type        = type,
                    appWidgetId = appWidgetId
                )
            }
        }
    }
}
```

---

### 7.3 `WidgetReadyState` — Main Widget Layout

```kotlin
@Composable
fun WidgetReadyState(
    state: WidgetState,
    type: TrackableType,
    appWidgetId: Int
) {
    val context = LocalContext.current
    val typeColor   = ViceVirtueWidgetTheme.typeColorProvider(type)
    val typeSurface = ViceVirtueWidgetTheme.typeSurfaceProvider(type)
    val streakColor = ViceVirtueWidgetTheme.streakColorProvider(type)

    // Deep-link intent: opens Detail screen for this trackable
    val detailIntent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("deep_link_trackable_id", state.trackableId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(typeSurface)
            .cornerRadius(24)
            .clickable(actionStartActivity(detailIntent))
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            // ── Row 1: Icon + Name ────────────────────────────────────
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                // Type icon in colored circle
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .background(typeColor)
                        .cornerRadius(18),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(
                            if (type == TrackableType.VICE)
                                R.drawable.ic_widget_skull
                            else
                                R.drawable.ic_widget_shield
                        ),
                        contentDescription = type.name,
                        modifier = GlanceModifier.size(20.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White, Color.White))
                    )
                }
                Spacer(GlanceModifier.width(10.dp))
                Text(
                    text = state.trackableName,
                    style = TextStyle(
                        color     = ViceVirtueWidgetTheme.onSurfaceProvider,
                        fontSize  = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            Spacer(GlanceModifier.height(10.dp))

            // ── Row 2: Streak ─────────────────────────────────────────
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                Text(
                    text = "${state.streak}",
                    style = TextStyle(
                        color      = streakColor,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = if (type == TrackableType.VICE) "days\nclean" else "days\nstrong",
                    style = TextStyle(
                        color    = ViceVirtueWidgetTheme.subtleProvider,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(12.dp))

            // ── Row 3: Action Button ──────────────────────────────────
            val buttonLabel = if (type == TrackableType.VICE) "I failed" else "I did it"

            Button(
                text = buttonLabel,
                onClick = actionRunCallback<WidgetActionCallback>(
                    parameters = actionParametersOf(
                        WidgetActionCallback.KEY_WIDGET_ID to appWidgetId
                    )
                ),
                modifier = GlanceModifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = typeColor,
                    contentColor    = ColorProvider(Color.White, Color.White)
                )
            )
        }
    }
}
```

---

### 7.4 `WidgetLoadingState`

```kotlin
@Composable
fun WidgetLoadingState() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ViceVirtueWidgetTheme.surfaceProvider)
            .cornerRadius(24)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading…",
            style = TextStyle(
                color    = ViceVirtueWidgetTheme.subtleProvider,
                fontSize = 13.sp
            )
        )
    }
}
```

---

### 7.5 `WidgetErrorState`

Shown when the bound trackable has been deleted from the app.

```kotlin
@Composable
fun WidgetErrorState() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ViceVirtueWidgetTheme.surfaceProvider)
            .cornerRadius(24)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Text(
                text = "⚠",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = "Trackable deleted.\nRemove & re-add\nthis widget.",
                style = TextStyle(
                    color    = ViceVirtueWidgetTheme.subtleProvider,
                    fontSize = 12.sp
                )
            )
        }
    }
}
```

---

## 8. Quick-Log Action Flow

### 8.1 `WidgetActionCallback.kt`

This callback runs when the user taps "I failed" or "I did it" on the widget. It must:
1. Determine which trackable is bound to this widget
2. Call `LogEventUseCase` with `description = ""`
3. Refresh the widget state immediately after logging

```kotlin
class WidgetActionCallback : ActionCallback {

    companion object {
        val KEY_WIDGET_ID = ActionParameters.Key<Int>("widget_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = parameters[KEY_WIDGET_ID] ?: return

        // Access repository via Hilt EntryPoint
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).repository()

        val trackableId = context.getTrackableIdForWidget(appWidgetId) ?: return
        val trackable   = repository.getTrackableById(trackableId) ?: return

        // Log event with empty description (consolidation rules apply)
        LogEventUseCase(repository).invoke(
            trackableId = trackable.id,
            description = ""
        )

        // Refresh this specific widget instance
        ViceVirtueWidget().update(context, glanceId)
    }
}
```

### 8.2 Action Flow Diagram

```
User taps "I failed" / "I did it"
         │
         ▼
WidgetActionCallback.onAction()
         │
         ├─► context.getTrackableIdForWidget(appWidgetId)
         │         │
         │         └─► reads widget_preferences DataStore
         │
         ├─► repository.getTrackableById(trackableId)
         │
         ├─► LogEventUseCase.invoke(trackableId, description = "")
         │         │
         │         ├─► Consolidation check (today × empty description)
         │         └─► Insert or update EventEntity in Room
         │
         └─► ViceVirtueWidget().update(context, glanceId)
                   │
                   └─► provideGlance() re-runs → recalculates streak
                             │
                             └─► Widget UI re-renders with new streak
```

### 8.3 Deep-Link Handling in MainActivity

Add deep-link handling so tapping the widget body opens the Detail screen:

```kotlin
// In MainActivity.kt, inside setContent { ... }, before NavHost:
val intent = intent
val deepLinkId = intent.getLongExtra("deep_link_trackable_id", -1L)

// Pass deepLinkId as startDestination override if valid:
val startDestination = if (deepLinkId != -1L) {
    Screen.Detail.createRoute(deepLinkId)
} else {
    Screen.Dashboard.route
}

// Then use startDestination in NavHost(startDestination = startDestination)
```

> **Agent Note**: If the app is already running and in the back stack, `FLAG_ACTIVITY_CLEAR_TOP` ensures the existing instance is reused rather than a new one being launched. The `deepLinkId` extra must be read in `onCreate` and handled via the NavController after the NavGraph is set up.

---

## 9. AndroidManifest Additions

Add the following inside `<application>`:

```xml
<!-- Widget Receiver -->
<receiver
    android:name=".widget.ViceVirtueWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/vice_virtue_widget_info" />
</receiver>

<!-- Widget Config Activity -->
<activity
    android:name=".widget.WidgetConfigActivity"
    android:exported="true"
    android:theme="@style/Theme.ViceVirtue">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>

<!-- Remove default WorkManager initializer (required when using HiltWorkerFactory) -->
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

### `res/xml/vice_virtue_widget_info.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:configure="com.vicevirtue.widget.WidgetConfigActivity"
    android:description="@string/widget_description"
    android:initialLayout="@layout/widget_loading"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:resizeMode="horizontal|vertical"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:updatePeriodMillis="0"
    android:widgetCategory="home_screen"
    android:previewImage="@drawable/widget_preview" />
```

| Attribute | Value | Reason |
|---|---|---|
| `minWidth` / `minHeight` | 180 × 110 dp | Fits comfortably in a 2×2 cell |
| `targetCellWidth/Height` | 2 × 2 | Default placement size |
| `updatePeriodMillis` | 0 | Updates driven by WorkManager, not system polling |
| `resizeMode` | horizontal\|vertical | User can resize freely |
| `configure` | `WidgetConfigActivity` | Forces picker on placement |

### `res/layout/widget_loading.xml`

This is a minimal `RemoteViews` layout shown before Glance renders:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background_neutral">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="ViceVirtue"
        android:textColor="#8E8E93"
        android:textSize="13sp" />

</FrameLayout>
```

> Create `res/drawable/widget_background_neutral.xml` as a rounded rectangle shape drawable with `android:radius="24dp"` and fill color `#F2F2F7` (light) — this is only visible for a fraction of a second before Glance takes over.

---

## 10. Widget Visual Design Spec

### Anatomy

```
┌────────────────────────────────────────┐
│  [●]  Smoking              ←  type icon circle + name
│                                        │
│  14         days                       │
│             clean          ←  streak number + label
│                                        │
│  [      I failed       ]   ←  full-width action button
└────────────────────────────────────────┘
```

### Visual Rules

| Element | Vice | Virtue |
|---|---|---|
| Widget background | `ViceRedSurface` / `DarkViceSurface` | `VirtueBlueSurface` / `DarkVirtueSurface` |
| Icon circle fill | `ViceRed` | `VirtueBlue` |
| Icon | 💀 skull (white) | 🛡 shield (white) |
| Streak number color | `StreakGold (#FFB800)` | `StreakElectric (#00C2FF)` |
| Streak label text | `NeutralGray`, 11sp | `NeutralGray`, 11sp |
| Action button fill | `ViceRed` | `VirtueBlue` |
| Action button text | "I failed", white, bold | "I did it", white, bold |
| Corner radius | 24dp (outer widget) | 24dp |
| Padding | 16dp all sides | 16dp all sides |

### Drawable Assets Required

Create the following vector drawables in `res/drawable/`:

**`ic_widget_skull.xml`** — Simple skull silhouette, `24×24dp` viewport, single path, fill `#FFFFFF`. Use a minimal geometric skull (two circular eye sockets, rounded cranium). Do not use emoji rendering.

**`ic_widget_shield.xml`** — Simple shield silhouette, `24×24dp` viewport, single path, fill `#FFFFFF`. Classic heraldic shield shape (flat top, pointed bottom).

**`widget_preview.png`** — 2×2 cell preview image at `320×320px`. Show both variants side by side (one Vice red, one Virtue blue). Used in the launcher widget picker. Create as a static PNG.

### Size Behavior

| Widget size | Layout adaptation |
|---|---|
| 2×2 (default) | Full layout as specified above |
| 2×1 (narrow) | Hide streak label text, show only number; button becomes square icon-only |
| 3×2 (wider) | No change — layout is already compact; extra space becomes padding |
| 1×1 | Not supported — `minWidth/Height` prevents this |

> **Agent Note**: Glance handles size variants via `LocalSize.current`. Implement a `SizeMode.Responsive` with two explicit size buckets: `DpSize(180.dp, 110.dp)` (compact) and `DpSize(180.dp, 180.dp)` (standard). Add `override val sizeMode = SizeMode.Responsive(setOf(compactSize, standardSize))` to `ViceVirtueWidget`. Inside `ViceVirtueWidgetContent`, read `LocalSize.current` and render the compact variant (no streak label, icon-only button) when the height is below 150dp.

---

## 11. Business Logic Rules

| Rule | Detail |
|---|---|
| Widget logs use empty description | `description = ""` is passed to `LogEventUseCase` — consolidation with other empty-description logs on the same day applies identically to the main app |
| Streak recalculates on every widget render | `GetStreakUseCase` is called inside `provideGlance()` every time the widget updates |
| Widget refresh cadence | WorkManager fires every 15 minutes. Additionally, the widget refreshes immediately after any action tap. At midnight (day rollover), the next WorkManager cycle will update streaks automatically. |
| Deleted trackable handling | If `repository.getTrackableById()` returns null (trackable was deleted in the app), render `WidgetErrorState`. Do NOT crash or show stale data. |
| Widget binding cleanup | On `onDeleted()`, the DataStore key for that `appWidgetId` is removed. No orphan data remains. |
| No reconfigure option | Once placed, the widget's trackable binding is permanent. To change it, the user must remove and re-add the widget. |
| Boot persistence | WorkManager's `PeriodicWorkRequest` survives device reboots automatically. No `BOOT_COMPLETED` receiver is needed. |
| Multiple widgets, same trackable | Allowed. Two widgets can both point to the same trackable. Each updates independently. |
| Thread safety | All DataStore reads and Room queries inside `WidgetActionCallback` and `provideGlance()` run on coroutine dispatchers provided by Glance/WorkManager. Do not block the main thread. |

---

*End of ViceVirtue Widget Guide v1.0 — Addendum to ViceVirtue_BuildGuide.md*
