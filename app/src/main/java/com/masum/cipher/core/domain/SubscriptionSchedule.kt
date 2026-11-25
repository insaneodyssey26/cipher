package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import java.util.concurrent.TimeUnit

object SubscriptionSchedule {
    const val MAX_REMINDERS = 3
    val REMINDER_INTERVAL_MS: Long = TimeUnit.DAYS.toMillis(3)

    fun nextDueAfter(subscription: SubscriptionEntity, now: Long): Long {
        val step = TimeUnit.DAYS.toMillis(subscription.frequencyDays.coerceAtLeast(1).toLong())
        val candidate = subscription.nextExpectedDate + step
        if (candidate > now) return candidate
        return candidate + ((now - candidate) / step + 1) * step
    }

    fun isDue(subscription: SubscriptionEntity, now: Long): Boolean =
        subscription.nextExpectedDate <= now

    fun shouldRemind(subscription: SubscriptionEntity, now: Long): Boolean {
        if (!isDue(subscription, now)) return false
        if (subscription.reminderCount >= MAX_REMINDERS) return false
        if (subscription.reminderCount == 0) return true
        return now - subscription.lastRemindedAt >= REMINDER_INTERVAL_MS
    }

    fun resolved(subscription: SubscriptionEntity, now: Long): SubscriptionEntity =
        subscription.copy(
            nextExpectedDate = nextDueAfter(subscription, now),
            lastRemindedAt = 0L,
            reminderCount = 0
        )
}
