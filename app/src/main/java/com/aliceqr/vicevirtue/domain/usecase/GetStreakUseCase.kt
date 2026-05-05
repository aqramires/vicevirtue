package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import java.util.Calendar
import java.util.SortedSet
import javax.inject.Inject

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
        val createdDay = getCalendarDay(trackable.createdAt)

        return when (trackable.type) {
            TrackableType.VICE -> calculateViceStreak(eventDays, today, createdDay)
            TrackableType.VIRTUE -> calculateVirtueStreak(eventDays, today)
        }
    }

    private fun calculateViceStreak(eventDays: SortedSet<Long>, today: Long, createdDay: Long): Int {
        if (eventDays.contains(today)) return 0
        
        // The last time the vice was committed (or the creation date if never committed)
        val lastEventDay = eventDays.lastOrNull { it < today } ?: createdDay
        
        // Streak is the number of full days between lastEventDay and today
        val diffMillis = today - lastEventDay
        val streak = (diffMillis / (1000L * 60 * 60 * 24)).toInt()
        
        return maxOf(0, streak)
    }

    private fun calculateVirtueStreak(eventDays: SortedSet<Long>, today: Long): Int {
        // Start from today or yesterday
        val startDay = if (eventDays.contains(today)) today else today - 1000L * 60 * 60 * 24
        if (!eventDays.contains(startDay)) return 0
        var streak = 0
        var currentDay = startDay
        while (eventDays.contains(currentDay)) {
            streak++
            currentDay -= 1000L * 60 * 60 * 24
        }
        return streak
    }

    private fun getCalendarDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
