# ViceVirtue — Android App Build Guide

> **Agent Instructions**: This is a complete, self-contained build guide. Follow each section in order. Do not skip sections. All architectural decisions are final unless explicitly noted as configurable.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack & Dependencies](#2-tech-stack--dependencies)
3. [Project Structure](#3-project-structure)
4. [Data Layer](#4-data-layer)
5. [Domain Layer](#5-domain-layer)
6. [UI Layer — Screens & Navigation](#6-ui-layer--screens--navigation)
7. [Style System](#7-style-system)
8. [Feature Specifications](#8-feature-specifications)
9. [Business Logic Rules](#9-business-logic-rules)
10. [Build & Run Instructions](#10-build--run-instructions)

---

## 1. Project Overview

**App Name**: ViceVirtue  
**Platform**: Android (minSdk 26, targetSdk 35)  
**Architecture**: MVVM + Clean Architecture (single-module)  
**Storage**: Fully local — Room database, no network calls  
**Language**: Kotlin  
**UI Framework**: Jetpack Compose  

### Core Concept

Users register personal **Vices** (bad habits) and **Virtues** (good habits). They then log **events** whenever they indulge in a Vice or act on a Virtue. The app tracks streaks, surfaces history, and allows reflection through optional descriptions.

| Concept | Symbol | Color | Streak Logic |
|---|---|---|---|
| Vice | 💀 Skull | Red (`#C0392B`) | Streak counts **days without** indulging |
| Virtue | 🛡 Shield | Blue (`#2E4FA3`) | Streak counts **consecutive days** practiced |

---

## 2. Tech Stack & Dependencies

### `build.gradle.kts` (app-level) — key dependencies

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.06.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Lifecycle / ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

// Hilt (DI)
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// DataStore (for preferences)
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

### Plugins required
- `kotlin-android`
- `kotlin-kapt`
- `com.google.dagger.hilt.android`
- `androidx.navigation.safeargs.kotlin` (optional, using Compose Nav so type-safe routes)

---

## 3. Project Structure

```
com.viceVirtue/
├── MainActivity.kt
├── ViceVirtueApp.kt                  ← Application class (Hilt)
│
├── data/
│   ├── db/
│   │   ├── ViceVirtueDatabase.kt
│   │   ├── dao/
│   │   │   ├── TrackableDao.kt       ← CRUD for Vice/Virtue entities
│   │   │   └── EventDao.kt           ← CRUD for log events
│   │   └── entity/
│   │       ├── TrackableEntity.kt    ← Represents a Vice or Virtue
│   │       └── EventEntity.kt        ← Represents a logged event
│   └── repository/
│       └── TrackableRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Trackable.kt              ← Domain model (Vice | Virtue)
│   │   ├── TrackableType.kt          ← Enum: VICE, VIRTUE
│   │   └── TrackableEvent.kt         ← Domain model for a log event
│   ├── repository/
│   │   └── TrackableRepository.kt    ← Interface
│   └── usecase/
│       ├── AddTrackableUseCase.kt
│       ├── LogEventUseCase.kt
│       ├── GetStreakUseCase.kt
│       ├── GetEventsUseCase.kt
│       └── DeleteTrackableUseCase.kt
│
├── ui/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt                 ← Sealed class of routes
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   └── Theme.kt
│   └── screens/
│       ├── dashboard/
│       │   ├── DashboardScreen.kt
│       │   └── DashboardViewModel.kt
│       ├── addtrackable/
│       │   ├── AddTrackableScreen.kt
│       │   └── AddTrackableViewModel.kt
│       ├── logevent/
│       │   ├── LogEventScreen.kt
│       │   └── LogEventViewModel.kt
│       ├── history/
│       │   ├── HistoryScreen.kt
│       │   └── HistoryViewModel.kt
│       └── detail/
│           ├── DetailScreen.kt
│           └── DetailViewModel.kt
│
└── di/
    ├── DatabaseModule.kt
    └── RepositoryModule.kt
```

---

## 4. Data Layer

### 4.1 Entities

#### `TrackableEntity.kt`

```kotlin
@Entity(tableName = "trackables")
data class TrackableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,           // "VICE" or "VIRTUE"
    val createdAt: Long = System.currentTimeMillis()
)
```

#### `EventEntity.kt`

```kotlin
@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = TrackableEntity::class,
        parentColumns = ["id"],
        childColumns = ["trackableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trackableId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackableId: Long,
    val description: String,    // Empty string if no reason given
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### 4.2 DAOs

#### `TrackableDao.kt`

```kotlin
@Dao
interface TrackableDao {
    @Query("SELECT * FROM trackables ORDER BY name ASC")
    fun getAllTrackables(): Flow<List<TrackableEntity>>

    @Query("SELECT * FROM trackables WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): Flow<List<TrackableEntity>>

    @Query("SELECT * FROM trackables WHERE id = :id")
    suspend fun getById(id: Long): TrackableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trackable: TrackableEntity): Long

    @Delete
    suspend fun delete(trackable: TrackableEntity)
}
```

#### `EventDao.kt`

```kotlin
@Dao
interface EventDao {
    // All events, newest first
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    // Events for a specific trackable
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp DESC")
    fun getEventsForTrackable(id: Long): Flow<List<EventEntity>>

    // Events filtered by date range
    @Query("""
        SELECT * FROM events
        WHERE timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    fun getEventsBetween(from: Long, to: Long): Flow<List<EventEntity>>

    // Events for a specific trackable within date range
    @Query("""
        SELECT * FROM events
        WHERE trackableId = :id AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    fun getEventsForTrackableBetween(id: Long, from: Long, to: Long): Flow<List<EventEntity>>

    // Latest event for a trackable (for streak calculation)
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEventForTrackable(id: Long): EventEntity?

    // All events for a trackable ordered ASC (for streak chain)
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp ASC")
    suspend fun getAllEventsForTrackableAsc(id: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Delete
    suspend fun delete(event: EventEntity)
}
```

---

### 4.3 Database

#### `ViceVirtueDatabase.kt`

```kotlin
@Database(
    entities = [TrackableEntity::class, EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ViceVirtueDatabase : RoomDatabase() {
    abstract fun trackableDao(): TrackableDao
    abstract fun eventDao(): EventDao
}
```

---

### 4.4 Repository Implementation

#### `TrackableRepositoryImpl.kt`

```kotlin
class TrackableRepositoryImpl @Inject constructor(
    private val trackableDao: TrackableDao,
    private val eventDao: EventDao
) : TrackableRepository {

    override fun getAllTrackables(): Flow<List<Trackable>> =
        trackableDao.getAllTrackables().map { list ->
            list.map { it.toDomain() }
        }

    override fun getTrackablesByType(type: TrackableType): Flow<List<Trackable>> =
        trackableDao.getByType(type.name).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addTrackable(trackable: Trackable): Long =
        trackableDao.insert(trackable.toEntity())

    override suspend fun deleteTrackable(trackable: Trackable) =
        trackableDao.delete(trackable.toEntity())

    override fun getAllEvents(): Flow<List<TrackableEvent>> =
        eventDao.getAllEvents().map { it.map { e -> e.toDomain() } }

    override fun getEventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>> =
        eventDao.getEventsForTrackable(trackableId).map { it.map { e -> e.toDomain() } }

    override fun getEventsBetween(from: Long, to: Long): Flow<List<TrackableEvent>> =
        eventDao.getEventsBetween(from, to).map { it.map { e -> e.toDomain() } }

    override fun getEventsForTrackableBetween(
        trackableId: Long, from: Long, to: Long
    ): Flow<List<TrackableEvent>> =
        eventDao.getEventsForTrackableBetween(trackableId, from, to)
            .map { it.map { e -> e.toDomain() } }

    override suspend fun logEvent(event: TrackableEvent): Long =
        eventDao.insert(event.toEntity())

    override suspend fun getLatestEventForTrackable(trackableId: Long): TrackableEvent? =
        eventDao.getLatestEventForTrackable(trackableId)?.toDomain()

    override suspend fun getAllEventsForTrackableAsc(trackableId: Long): List<TrackableEvent> =
        eventDao.getAllEventsForTrackableAsc(trackableId).map { it.toDomain() }
}
```

> **Agent Note**: Implement `toDomain()` and `toEntity()` extension functions in a `Mapper.kt` file within the `data/` package.

---

## 5. Domain Layer

### 5.1 Models

#### `TrackableType.kt`
```kotlin
enum class TrackableType { VICE, VIRTUE }
```

#### `Trackable.kt`
```kotlin
data class Trackable(
    val id: Long = 0,
    val name: String,
    val type: TrackableType,
    val createdAt: Long = System.currentTimeMillis(),
    val streak: Int = 0         // Computed, not stored
)
```

#### `TrackableEvent.kt`
```kotlin
data class TrackableEvent(
    val id: Long = 0,
    val trackableId: Long,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### 5.2 Repository Interface

#### `TrackableRepository.kt`
```kotlin
interface TrackableRepository {
    fun getAllTrackables(): Flow<List<Trackable>>
    fun getTrackablesByType(type: TrackableType): Flow<List<Trackable>>
    suspend fun addTrackable(trackable: Trackable): Long
    suspend fun deleteTrackable(trackable: Trackable)

    fun getAllEvents(): Flow<List<TrackableEvent>>
    fun getEventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>>
    fun getEventsBetween(from: Long, to: Long): Flow<List<TrackableEvent>>
    fun getEventsForTrackableBetween(trackableId: Long, from: Long, to: Long): Flow<List<TrackableEvent>>

    suspend fun logEvent(event: TrackableEvent): Long
    suspend fun getLatestEventForTrackable(trackableId: Long): TrackableEvent?
    suspend fun getAllEventsForTrackableAsc(trackableId: Long): List<TrackableEvent>
}
```

---

### 5.3 Use Cases

#### `AddTrackableUseCase.kt`
```kotlin
class AddTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    suspend operator fun invoke(name: String, type: TrackableType): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be blank"))
        val trackable = Trackable(name = name.trim(), type = type)
        return Result.success(repository.addTrackable(trackable))
    }
}
```

#### `LogEventUseCase.kt`

```kotlin
class LogEventUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    /**
     * Logs an event for a given trackable.
     * Consolidation rule: events with the same description (including empty)
     * on the SAME CALENDAR DAY are merged — only one event is stored per
     * (trackableId, description, calendarDay) triple.
     * If a matching event already exists for today, no duplicate is inserted;
     * instead the existing event's timestamp is updated to now.
     */
    suspend operator fun invoke(
        trackableId: Long,
        description: String
    ): Result<Long> {
        val normalizedDesc = description.trim()
        val startOfDay = getStartOfDay(System.currentTimeMillis())
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1

        val todayEvents = repository.getEventsForTrackableBetweenOnce(
            trackableId, startOfDay, endOfDay
        )
        val existing = todayEvents.firstOrNull { it.description.trim() == normalizedDesc }

        return if (existing != null) {
            // Update timestamp to now (consolidate)
            val updated = existing.copy(timestamp = System.currentTimeMillis())
            Result.success(repository.updateEvent(updated))
        } else {
            val event = TrackableEvent(
                trackableId = trackableId,
                description = normalizedDesc,
                timestamp = System.currentTimeMillis()
            )
            Result.success(repository.logEvent(event))
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
```

> **Agent Note**: Add `getEventsForTrackableBetweenOnce` (suspend, returns `List`) and `updateEvent` to the repository interface and implementation.

#### `GetStreakUseCase.kt`

```kotlin
class GetStreakUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    /**
     * VICE streak: Number of consecutive days WITHOUT an event, counting back from today.
     *   - If the user indulged today, streak = 0.
     *   - Each day with no event increments the streak.
     *
     * VIRTUE streak: Number of consecutive days WITH at least one event, counting back from today.
     *   - If the user did not act today, check if they acted yesterday to continue the chain.
     *   - A gap of any full calendar day breaks the streak.
     */
    suspend operator fun invoke(trackable: Trackable): Int {
        val events = repository.getAllEventsForTrackableAsc(trackable.id)
        val eventDays = events
            .map { getCalendarDay(it.timestamp) }
            .toSortedSet()

        val today = getCalendarDay(System.currentTimeMillis())

        return when (trackable.type) {
            TrackableType.VICE -> calculateViceStreak(eventDays, today)
            TrackableType.VIRTUE -> calculateVirtueStreak(eventDays, today)
        }
    }

    private fun calculateViceStreak(eventDays: SortedSet<Long>, today: Long): Int {
        if (eventDays.contains(today)) return 0
        var streak = 0
        var day = today - 1
        while (!eventDays.contains(day)) {
            // Only count days after the trackable was created (eventDays may be empty)
            streak++
            day--
            if (streak > 3650) break // Safety cap at 10 years
        }
        return streak
    }

    private fun calculateVirtueStreak(eventDays: SortedSet<Long>, today: Long): Int {
        // Start from today or yesterday
        val startDay = if (eventDays.contains(today)) today else today - 1
        if (!eventDays.contains(startDay)) return 0
        var streak = 0
        var day = startDay
        while (eventDays.contains(day)) {
            streak++
            day--
        }
        return streak
    }

    private fun getCalendarDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
```

#### `GetEventsUseCase.kt`

```kotlin
class GetEventsUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    // All events, newest first
    fun allEvents(): Flow<List<TrackableEvent>> = repository.getAllEvents()

    // Events filtered by type (requires joining with trackable table)
    fun eventsByType(type: TrackableType, allTrackables: List<Trackable>): Flow<List<TrackableEvent>> {
        val ids = allTrackables.filter { it.type == type }.map { it.id }.toSet()
        return repository.getAllEvents().map { events -> events.filter { it.trackableId in ids } }
    }

    // Events for a specific trackable
    fun eventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>> =
        repository.getEventsForTrackable(trackableId)

    // Events in a date range
    fun eventsInRange(from: Long, to: Long): Flow<List<TrackableEvent>> =
        repository.getEventsBetween(from, to)

    // Events for a specific trackable in a date range
    fun eventsForTrackableInRange(trackableId: Long, from: Long, to: Long): Flow<List<TrackableEvent>> =
        repository.getEventsForTrackableBetween(trackableId, from, to)
}
```

#### `DeleteTrackableUseCase.kt`

```kotlin
class DeleteTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    suspend operator fun invoke(trackable: Trackable) =
        repository.deleteTrackable(trackable)
    // Note: CASCADE foreign key ensures all events for this trackable are deleted automatically
}
```

---

## 6. UI Layer — Screens & Navigation

### 6.1 Navigation Routes

#### `Screen.kt`
```kotlin
sealed class Screen(val route: String) {
    object Dashboard  : Screen("dashboard")
    object AddTrackable : Screen("add_trackable?type={type}") {
        fun createRoute(type: String) = "add_trackable?type=$type"
    }
    object LogEvent : Screen("log_event/{trackableId}") {
        fun createRoute(id: Long) = "log_event/$id"
    }
    object History : Screen("history?trackableId={trackableId}") {
        fun createRoute(trackableId: Long? = null) =
            if (trackableId != null) "history?trackableId=$trackableId" else "history"
    }
    object Detail : Screen("detail/{trackableId}") {
        fun createRoute(id: Long) = "detail/$id"
    }
}
```

#### `NavGraph.kt`
```kotlin
@Composable
fun ViceVirtueNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(
            Screen.AddTrackable.route,
            arguments = listOf(navArgument("type") { defaultValue = "VICE" })
        ) { backStack ->
            val type = backStack.arguments?.getString("type") ?: "VICE"
            AddTrackableScreen(navController, initialType = TrackableType.valueOf(type))
        }
        composable(
            Screen.LogEvent.route,
            arguments = listOf(navArgument("trackableId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("trackableId") ?: return@composable
            LogEventScreen(navController, trackableId = id)
        }
        composable(Screen.History.route,
            arguments = listOf(navArgument("trackableId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { backStack ->
            val id = backStack.arguments?.getLong("trackableId")?.takeIf { it != -1L }
            HistoryScreen(navController, filterTrackableId = id)
        }
        composable(
            Screen.Detail.route,
            arguments = listOf(navArgument("trackableId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("trackableId") ?: return@composable
            DetailScreen(navController, trackableId = id)
        }
    }
}
```

---

### 6.2 Screen Specifications

#### Screen 1: Dashboard

**Purpose**: Home screen showing all Vices and Virtues with their current streaks.

**Layout**:
- Top app bar: "ViceVirtue" title + `+` FAB
- Two sections separated by a subtle divider:
  - **Vices** (skull icon header, red accent)
  - **Virtues** (shield icon header, blue accent)
- Each trackable is a `TrackableCard` (see component spec below)
- If a section is empty, show an empty-state message with a ghost icon

**TrackableCard component**:
- Left: type icon (💀 or 🛡) in a colored circle (red/blue)
- Center: trackable name + streak chip ("🔥 12 days" for Vice, "⚡ 7 days" for Virtue)
- Right: "Log" button (tapping opens LogEvent screen)
- Tap the card body: navigates to Detail screen
- Long press: shows delete confirmation dialog

**FAB behavior**: Opens a bottom sheet asking "Add Vice or Virtue?" with two buttons.

**ViewModel state**:
```kotlin
data class DashboardUiState(
    val vices: List<TrackableWithStreak> = emptyList(),
    val virtues: List<TrackableWithStreak> = emptyList(),
    val isLoading: Boolean = true
)
data class TrackableWithStreak(val trackable: Trackable, val streak: Int)
```

---

#### Screen 2: AddTrackable

**Purpose**: Form to register a new Vice or Virtue.

**Layout**:
- Top app bar: "New Vice" or "New Virtue" depending on type, with back navigation
- Toggle selector at top: `[💀 Vice]  [🛡 Virtue]` — pill-style toggle, colored to match
- Single `OutlinedTextField`: "Name" (e.g. "Smoking", "Morning Run")
- Primary button: "Add Vice" / "Add Virtue"
- Name must be non-empty; show inline error if submitted blank

**ViewModel**:
```kotlin
data class AddTrackableUiState(
    val name: String = "",
    val type: TrackableType = TrackableType.VICE,
    val nameError: String? = null,
    val isSuccess: Boolean = false
)
```

---

#### Screen 3: LogEvent

**Purpose**: Record an instance of indulging in a Vice or acting on a Virtue.

**Layout**:
- Top app bar: trackable name + type icon, back navigation
- Header banner: colored (red=Vice, blue=Virtue) with the trackable name and current streak
- `OutlinedTextField` (multiline, optional): "Why? (optional)"  
  Placeholder text: "Add a note about this moment..."
- Primary button: "Record [Vice/Virtue]"
- Small helper text beneath field: "Entries with the same note today will be consolidated."

**ViewModel**:
```kotlin
data class LogEventUiState(
    val trackable: Trackable? = null,
    val description: String = "",
    val isSuccess: Boolean = false,
    val wasConsolidated: Boolean = false  // Show snackbar if true
)
```

**Post-log behavior**: Navigate back to Dashboard. Show Snackbar on Dashboard if `wasConsolidated = true`: "Entry consolidated with an existing note."

---

#### Screen 4: History

**Purpose**: Browse all logged events with filtering.

**Layout**:
- Top app bar: "History" + optional filter icon button
- **Filter bar** (persistent, horizontally scrollable chips):
  - `All` (default)
  - `Vices` (skull chip, red)
  - `Virtues` (shield chip, blue)
  - Individual trackable name chips (one per trackable)
- **Date range picker**: collapsible row below chip bar. Two `OutlinedTextField`s (From / To) that open `DatePickerDialog` on tap. "Clear" button to reset.
- **Event list**: `LazyColumn`, grouped by date header ("Today", "Yesterday", "May 3", etc.)
- Each event row:
  - Left: colored type icon circle
  - Center: trackable name (bold) + description (if non-empty, italic below)
  - Right: time string ("2:34 PM")

**Filter logic**:
- Chip filters and date range compose (AND logic)
- If a specific trackable chip is selected, the Vice/Virtue chip becomes irrelevant — hide it or auto-select appropriately
- "All" chip deselects others

**ViewModel**:
```kotlin
data class HistoryUiState(
    val events: List<TrackableEvent> = emptyList(),
    val trackables: List<Trackable> = emptyList(),     // for name lookups + chips
    val selectedTrackableId: Long? = null,
    val selectedType: TrackableType? = null,           // null = All
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val isLoading: Boolean = true
)
```

---

#### Screen 5: Detail

**Purpose**: View a single Vice or Virtue in depth, with its full event history.

**Layout**:
- Top app bar: trackable name + type icon, back navigation. Delete (trash) icon in top right.
- **Hero section** (colored background):
  - Large type icon (64dp)
  - Trackable name (large type)
  - Streak display: "🔥 12 days clean" (Vice) or "⚡ 7 days strong" (Virtue)
  - Created date: "Tracking since May 1, 2025"
- **Section header**: "History"
- `LazyColumn` of all events for this trackable, same row format as History screen
- Empty state: "No events recorded yet. Tap Log to begin."
- Tapping delete shows confirmation dialog: "Delete [name]? All history will be lost."

---

## 7. Style System

> **File**: `ui/theme/` — implement all four files below.

### `Color.kt`

```kotlin
package com.vicevirtue.ui.theme

import androidx.compose.ui.graphics.Color

// === Core Brand Colors ===
val ViceRed         = Color(0xFFC0392B)   // Deep crimson — Vice primary
val ViceRedDark     = Color(0xFF96261F)   // Pressed/dark state
val ViceRedLight    = Color(0xFFE57373)   // Tint, backgrounds
val ViceRedSurface  = Color(0xFFFFF0F0)   // Light mode card background for Vice

val VirtueBlue      = Color(0xFF2E4FA3)   // Deep cobalt — Virtue primary
val VirtueBlueDark  = Color(0xFF1E3577)   // Pressed/dark state
val VirtueBlueLight = Color(0xFF6B8DD6)   // Tint
val VirtueBlueSurface = Color(0xFFF0F4FF) // Light mode card background for Virtue

// === Neutral Palette ===
val NeutralBlack    = Color(0xFF0E0E0E)
val NeutralDark     = Color(0xFF1C1C1E)
val NeutralMid      = Color(0xFF3A3A3C)
val NeutralGray     = Color(0xFF8E8E93)
val NeutralLight    = Color(0xFFD1D1D6)
val NeutralSurface  = Color(0xFFF2F2F7)
val NeutralWhite    = Color(0xFFFFFFFF)

// === Semantic ===
val StreakGold      = Color(0xFFFFB800)   // Streak fire color
val StreakElectric  = Color(0xFF00C2FF)   // Virtue streak lightning

// === Dark Theme Surfaces ===
val DarkBackground  = Color(0xFF0E0E0E)
val DarkSurface     = Color(0xFF1C1C1E)
val DarkSurfaceAlt  = Color(0xFF2C2C2E)
val DarkViceSurface = Color(0xFF2A1515)
val DarkVirtueSurface = Color(0xFF111D36)
```

---

### `Type.kt`

```kotlin
package com.vicevirtue.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Use Google Fonts via downloadable fonts or bundled:
// Display font: "Playfair Display" — editorial, weighty, moral gravitas
// Body font: "DM Sans" — clean, modern, legible

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
    Font(R.font.playfair_display_bold, FontWeight.Bold),
)

val DMSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

val ViceVirtueTypography = Typography(
    // App title, screen heroes
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    // Card titles, trackable names in hero
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    // Section headers
    titleLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Body text, descriptions
    bodyLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Buttons, chips, labels
    labelLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)
```

> **Agent Note**: Download Playfair Display and DM Sans `.ttf` files and place in `res/font/`. File names must match exactly: `playfair_display_regular.ttf`, `playfair_display_semibold.ttf`, `playfair_display_bold.ttf`, `dm_sans_regular.ttf`, `dm_sans_medium.ttf`, `dm_sans_bold.ttf`.

---

### `Shape.kt`

```kotlin
package com.vicevirtue.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ViceVirtueShapes = Shapes(
    // Chips, small badges
    extraSmall = RoundedCornerShape(4.dp),
    // Input fields, small cards
    small = RoundedCornerShape(8.dp),
    // Standard cards, buttons
    medium = RoundedCornerShape(16.dp),
    // Hero sections, bottom sheets
    large = RoundedCornerShape(24.dp),
    // Full-bleed modals
    extraLarge = RoundedCornerShape(32.dp)
)
```

---

### `Theme.kt`

```kotlin
package com.vicevirtue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary          = VirtueBlue,
    onPrimary        = NeutralWhite,
    primaryContainer = VirtueBlueSurface,
    onPrimaryContainer = VirtueBlueDark,

    secondary        = ViceRed,
    onSecondary      = NeutralWhite,
    secondaryContainer = ViceRedSurface,
    onSecondaryContainer = ViceRedDark,

    background       = NeutralWhite,
    onBackground     = NeutralBlack,
    surface          = NeutralSurface,
    onSurface        = NeutralBlack,
    surfaceVariant   = NeutralLight,
    onSurfaceVariant = NeutralMid,

    outline          = NeutralLight,
    error            = ViceRed,
    onError          = NeutralWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary          = VirtueBlueLight,
    onPrimary        = NeutralBlack,
    primaryContainer = DarkVirtueSurface,
    onPrimaryContainer = VirtueBlueLight,

    secondary        = ViceRedLight,
    onSecondary      = NeutralBlack,
    secondaryContainer = DarkViceSurface,
    onSecondaryContainer = ViceRedLight,

    background       = DarkBackground,
    onBackground     = NeutralWhite,
    surface          = DarkSurface,
    onSurface        = NeutralWhite,
    surfaceVariant   = DarkSurfaceAlt,
    onSurfaceVariant = NeutralGray,

    outline          = NeutralMid,
    error            = ViceRedLight,
    onError          = NeutralBlack,
)

@Composable
fun ViceVirtueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ViceVirtueTypography,
        shapes      = ViceVirtueShapes,
        content     = content
    )
}

// === Design Tokens (use directly in Composables) ===

object ViceVirtueTokens {
    // Spacing
    val SpaceXS  = 4
    val SpaceS   = 8
    val SpaceM   = 16
    val SpaceL   = 24
    val SpaceXL  = 32
    val SpaceXXL = 48

    // Elevation
    val ElevationCard   = 2
    val ElevationModal  = 8
    val ElevationFAB    = 6

    // Icon sizes
    val IconSm   = 20
    val IconMd   = 24
    val IconLg   = 40
    val IconHero = 64

    // Animation durations (ms)
    val AnimFast   = 150
    val AnimNormal = 300
    val AnimSlow   = 500
}
```

---

### Visual Design Rules (for all screens)

| Rule | Implementation |
|---|---|
| Vice elements always use `ViceRed` | Apply to icons, borders, buttons, hero backgrounds |
| Virtue elements always use `VirtueBlue` | Apply to icons, borders, buttons, hero backgrounds |
| Type icon circles | 40dp circle, filled with type color at 15% alpha, icon in full type color |
| Streak chips | Rounded pill, `StreakGold` text for vices, `StreakElectric` for virtues, dark background |
| Cards | `medium` shape (16dp radius), subtle shadow `ElevationCard`, surface color background |
| Section headers | `titleSmall` style, `NeutralGray` color, ALL CAPS, 1sp letter spacing |
| Empty states | Centered, `NeutralGray` icon (48dp), `bodyMedium` text |
| Buttons (primary) | Filled, `medium` shape, colored to match context (red or blue) |
| Bottom sheets | `extraLarge` top corners only, drag handle at top |
| Date headers in lists | `labelMedium`, `NeutralGray`, with horizontal rules either side |

---

## 8. Feature Specifications

### 8.1 Consolidation Logic

When the user logs an event:
1. Normalize the description: `description.trim()`
2. Query all events for that trackable on **today's calendar day** (midnight to midnight)
3. Check if any existing event has the same normalized description (including empty string)
4. **If match found**: Update the existing event's `timestamp` to `now`. Do NOT insert a new row.
5. **If no match**: Insert a new event row.
6. Surface a `wasConsolidated = true` flag to the UI if step 4 occurred.

---

### 8.2 Streak Calculation

**Vice Streak** ("days clean"):
- Count how many consecutive calendar days **have no events** going backward from yesterday.
- If there is an event **today**, streak = 0 immediately.
- Streak starts counting from the day after the most recent event.
- Example: last event was 5 days ago → streak = 5.

**Virtue Streak** ("days strong"):
- Count how many consecutive calendar days **have at least one event** going backward from today (or yesterday if today has no events).
- If there is no event today AND no event yesterday, streak = 0.
- Example: events on today, yesterday, and 2 days ago but not 3 days ago → streak = 3.

**Streak is computed at runtime** — never stored in the database.

---

### 8.3 History Filtering

Filters apply in this priority/AND logic:

```
effective_events = ALL events
  IF selectedTrackableId != null:
      effective_events = filter by trackableId
  ELSE IF selectedType != null:
      effective_events = filter by trackable type
  IF fromDate != null:
      effective_events = filter where timestamp >= fromDate (start of that day)
  IF toDate != null:
      effective_events = filter where timestamp <= toDate (end of that day)
```

Date grouping in the list:
- "Today"
- "Yesterday"  
- Day of week if within 7 days: "Wednesday"
- Full date otherwise: "Mar 14, 2025"

---

### 8.4 Deletion

- Deleting a Trackable cascades to delete all its events (enforced by Room FK constraint).
- Always show a confirmation dialog before deletion.
- Dialog copy: "Delete '[name]'? This will permanently remove all [X] logged events."
- Query event count before showing dialog so the count is accurate.

---

## 9. Business Logic Rules

| Rule | Detail |
|---|---|
| Trackable names are not unique | Two Vices can share the same name — they are separate entities |
| Descriptions are trimmed | Leading/trailing whitespace is stripped before storage and comparison |
| Empty description is valid | Logging with no note is allowed and consolidates with other no-note logs on the same day |
| No future events | The log timestamp is always `System.currentTimeMillis()` — no backdating |
| Streaks recalculate on open | Call `GetStreakUseCase` each time the Dashboard or Detail screen is shown |
| No sync, no backup | App is 100% local; no export/import in v1 |
| Dark mode | Follows system setting automatically via `isSystemInDarkTheme()` |

---

## 10. Build & Run Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Gradle 8.x
- Android SDK 35

### Steps

```bash
# 1. Clone / initialize the project
# Use Android Studio: File → New → New Project → Empty Activity (Compose)
# Package: com.vicevirtue
# Min SDK: 26

# 2. Add all dependencies to build.gradle.kts (see Section 2)

# 3. Apply plugins in build.gradle.kts:
#    id("kotlin-kapt")
#    id("com.google.dagger.hilt.android")

# 4. Add to top-level build.gradle.kts:
#    id("com.google.dagger.hilt.android") version "2.51.1" apply false

# 5. Create font resources
#    Download Playfair Display & DM Sans from fonts.google.com
#    Place .ttf files in app/src/main/res/font/

# 6. Create the ViceVirtueApp.kt Application class:
@HiltAndroidApp
class ViceVirtueApp : Application()

# 7. Register in AndroidManifest.xml:
android:name=".ViceVirtueApp"

# 8. Annotate MainActivity with @AndroidEntryPoint
# 9. Set content to ViceVirtueTheme { ViceVirtueNavGraph(...) }

# 10. Build and run:
./gradlew assembleDebug
```

### Room Schema Validation

Disable schema export in `ViceVirtueDatabase.kt` (`exportSchema = false`) for development. For production, enable and commit the schema JSON to version control.

### Hilt DI Modules

```kotlin
// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ViceVirtueDatabase =
        Room.databaseBuilder(ctx, ViceVirtueDatabase::class.java, "vicevirtue.db").build()

    @Provides fun provideTrackableDao(db: ViceVirtueDatabase) = db.trackableDao()
    @Provides fun provideEventDao(db: ViceVirtueDatabase) = db.eventDao()
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindTrackableRepository(
        impl: TrackableRepositoryImpl
    ): TrackableRepository
}
```

---

## Appendix: Component Quick Reference

| Component | File | Props |
|---|---|---|
| `TrackableCard` | `ui/components/TrackableCard.kt` | `trackable`, `streak`, `onLog`, `onClick`, `onLongPress` |
| `TypeIconCircle` | `ui/components/TypeIconCircle.kt` | `type: TrackableType`, `size: Dp` |
| `StreakChip` | `ui/components/StreakChip.kt` | `streak: Int`, `type: TrackableType` |
| `TypeToggle` | `ui/components/TypeToggle.kt` | `selected: TrackableType`, `onSelect` |
| `DateHeader` | `ui/components/DateHeader.kt` | `timestamp: Long` |
| `EventRow` | `ui/components/EventRow.kt` | `event`, `trackable`, `showTrackableName: Boolean` |
| `DeleteConfirmDialog` | `ui/components/DeleteConfirmDialog.kt` | `name`, `eventCount`, `onConfirm`, `onDismiss` |
| `EmptyState` | `ui/components/EmptyState.kt` | `message: String`, `icon: ImageVector` |

---

*End of ViceVirtue Build Guide v1.0*
