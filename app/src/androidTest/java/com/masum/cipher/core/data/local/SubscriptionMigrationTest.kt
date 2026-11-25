package com.masum.cipher.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubscriptionMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migratingFourteenToFifteenKeepsSubscriptionsAndZeroesReminderState() {
        helper.createDatabase(DATABASE_NAME, 14).apply {
            execSQL(
                "INSERT INTO subscriptions (id, merchant, amount, category, frequencyDays, nextExpectedDate) " +
                    "VALUES (1, 'Netflix', 649.0, 'ENTERTAINMENT', 30, 1750000000000)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(DATABASE_NAME, 15, true, AppDatabase.MIGRATION_14_15)

        migrated.query("SELECT merchant, amount, frequencyDays, nextExpectedDate, lastRemindedAt, reminderCount FROM subscriptions WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Netflix", cursor.getString(0))
            assertEquals(649.0, cursor.getDouble(1), 0.001)
            assertEquals(30, cursor.getInt(2))
            assertEquals(1750000000000L, cursor.getLong(3))
            assertEquals(0L, cursor.getLong(4))
            assertEquals(0, cursor.getInt(5))
        }
    }

    private companion object {
        const val DATABASE_NAME = "subscription-migration-test"
    }
}
