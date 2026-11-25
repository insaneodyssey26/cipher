package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SubscriptionScheduleTest {

    private val day = TimeUnit.DAYS.toMillis(1)
    private val now = 1_800_000_000_000L

    private fun sub(
        nextExpectedDate: Long = now - 1,
        frequencyDays: Int = 30,
        lastRemindedAt: Long = 0L,
        reminderCount: Int = 0
    ) = SubscriptionEntity(
        id = 1L,
        merchant = "NETFLIX",
        amount = 649.0,
        category = "ENTERTAINMENT",
        frequencyDays = frequencyDays,
        nextExpectedDate = nextExpectedDate,
        lastRemindedAt = lastRemindedAt,
        reminderCount = reminderCount
    )

    @Test
    fun nextDueAfterAddsExactlyOneIntervalWhenOnlyOneCycleIsMissed() {
        val due = now - day
        assertEquals(due + 30 * day, SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = due), now))
    }

    @Test
    fun nextDueAfterSkipsEveryMissedCycle() {
        val due = now - 95 * day
        val result = SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = due), now)
        assertEquals(due + 120 * day, result)
        assertTrue(result > now)
        assertTrue(result - now <= 30 * day)
    }

    @Test
    fun nextDueAfterIsStrictlyInTheFutureWhenDueExactlyNow() {
        val result = SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = now), now)
        assertEquals(now + 30 * day, result)
    }

    @Test
    fun nextDueAfterLandsOnTheBoundaryWhenCandidateEqualsNow() {
        val result = SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = now - 30 * day), now)
        assertEquals(now + 30 * day, result)
    }

    @Test
    fun nextDueAfterDoesNotMoveBackwardsForAFutureDueDate() {
        val due = now + 10 * day
        assertEquals(due + 30 * day, SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = due), now))
    }

    @Test
    fun nextDueAfterSurvivesAZeroOrNegativeFrequency() {
        val zero = SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = now - 3 * day, frequencyDays = 0), now)
        val negative = SubscriptionSchedule.nextDueAfter(sub(nextExpectedDate = now - 3 * day, frequencyDays = -5), now)
        assertTrue(zero > now && zero - now <= day)
        assertTrue(negative > now && negative - now <= day)
    }

    @Test
    fun aSubscriptionIsOnlyDueOnceItsDateHasPassed() {
        assertTrue(SubscriptionSchedule.isDue(sub(nextExpectedDate = now), now))
        assertFalse(SubscriptionSchedule.isDue(sub(nextExpectedDate = now + 1), now))
    }

    @Test
    fun remindsImmediatelyTheFirstTimeItBecomesDue() {
        assertTrue(SubscriptionSchedule.shouldRemind(sub(), now))
    }

    @Test
    fun neverRemindsBeforeTheDueDate() {
        assertFalse(SubscriptionSchedule.shouldRemind(sub(nextExpectedDate = now + day), now))
    }

    @Test
    fun doesNotRemindAgainWithinTheReminderInterval() {
        val reminded = now - TimeUnit.DAYS.toMillis(1)
        assertFalse(SubscriptionSchedule.shouldRemind(sub(lastRemindedAt = reminded, reminderCount = 1), now))
        val almost = now - SubscriptionSchedule.REMINDER_INTERVAL_MS + 1
        assertFalse(SubscriptionSchedule.shouldRemind(sub(lastRemindedAt = almost, reminderCount = 1), now))
    }

    @Test
    fun remindsAgainOnceTheReminderIntervalHasElapsed() {
        val reminded = now - SubscriptionSchedule.REMINDER_INTERVAL_MS
        assertTrue(SubscriptionSchedule.shouldRemind(sub(lastRemindedAt = reminded, reminderCount = 1), now))
        assertTrue(SubscriptionSchedule.shouldRemind(sub(lastRemindedAt = reminded, reminderCount = 2), now))
    }

    @Test
    fun stopsRemindingAfterTheMaximumNumberOfReminders() {
        val longAgo = now - 30 * day
        val exhausted = sub(lastRemindedAt = longAgo, reminderCount = SubscriptionSchedule.MAX_REMINDERS)
        assertFalse(SubscriptionSchedule.shouldRemind(exhausted, now))
    }

    @Test
    fun resolvedAdvancesTheDateAndClearsReminderState() {
        val due = now - 40 * day
        val resolved = SubscriptionSchedule.resolved(
            sub(nextExpectedDate = due, lastRemindedAt = now - day, reminderCount = 3),
            now
        )
        assertEquals(due + 60 * day, resolved.nextExpectedDate)
        assertEquals(0L, resolved.lastRemindedAt)
        assertEquals(0, resolved.reminderCount)
        assertFalse(SubscriptionSchedule.shouldRemind(resolved, now))
    }

    @Test
    fun fullReminderLifecycleSendsThreeRemindersThenStops() {
        var current = sub()
        var clock = now
        var sent = 0
        repeat(30) {
            if (SubscriptionSchedule.shouldRemind(current, clock)) {
                sent++
                current = current.copy(lastRemindedAt = clock, reminderCount = current.reminderCount + 1)
            }
            clock += day
        }
        assertEquals(SubscriptionSchedule.MAX_REMINDERS, sent)
    }
}
