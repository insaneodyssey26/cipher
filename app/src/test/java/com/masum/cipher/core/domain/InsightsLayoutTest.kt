package com.masum.cipher.core.domain

import com.masum.cipher.core.domain.model.InsightsLayout
import com.masum.cipher.core.domain.model.InsightsSection
import com.masum.cipher.core.domain.model.InsightsSection.ACTIVITY_CALENDAR
import com.masum.cipher.core.domain.model.InsightsSection.BUDGET
import com.masum.cipher.core.domain.model.InsightsSection.CASH_FLOW
import com.masum.cipher.core.domain.model.InsightsSection.CATEGORIES
import com.masum.cipher.core.domain.model.InsightsSection.NET_WORTH
import com.masum.cipher.core.domain.model.InsightsSection.OVERVIEW
import com.masum.cipher.core.domain.model.InsightsSection.PEAK_HOURS
import com.masum.cipher.core.domain.model.InsightsTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsLayoutTest {

    private val spendingDefault = listOf(OVERVIEW, BUDGET, CASH_FLOW, NET_WORTH)
    private val habitsDefault = listOf(CATEGORIES, PEAK_HOURS, ACTIVITY_CALENDAR)

    @Test
    fun noSavedOrderResolvesToTheDefaultOrderOfEachTab() {
        assertEquals(spendingDefault, InsightsLayout.resolve(InsightsTab.SPENDING, emptyList()))
        assertEquals(habitsDefault, InsightsLayout.resolve(InsightsTab.HABITS, emptyList()))
    }

    @Test
    fun aSavedOrderIsRespected() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("net_worth", "overview", "cash_flow", "budget"))
        assertEquals(listOf(NET_WORTH, OVERVIEW, CASH_FLOW, BUDGET), resolved)
    }

    @Test
    fun theOverviewCanBeMovedAnywhere() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("budget", "cash_flow", "net_worth", "overview"))
        assertEquals(listOf(BUDGET, CASH_FLOW, NET_WORTH, OVERVIEW), resolved)
    }

    @Test
    fun anOrderSavedBeforeTheOverviewWasMovableKeepsItOnTop() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("net_worth", "budget", "cash_flow"))
        assertEquals(listOf(OVERVIEW, NET_WORTH, BUDGET, CASH_FLOW), resolved)
    }

    @Test
    fun unknownIdsAreIgnored() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("removed_section", "cash_flow"))
        assertEquals(listOf(OVERVIEW, BUDGET, CASH_FLOW, NET_WORTH), resolved)
    }

    @Test
    fun duplicatedIdsAppearOnlyOnce() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("net_worth", "net_worth", "budget"))
        assertEquals(listOf(OVERVIEW, NET_WORTH, CASH_FLOW, BUDGET), resolved)
    }

    @Test
    fun idsBelongingToAnotherTabAreIgnored() {
        val resolved = InsightsLayout.resolve(InsightsTab.HABITS, listOf("budget", "peak_hours"))
        assertEquals(listOf(CATEGORIES, PEAK_HOURS, ACTIVITY_CALENDAR), resolved)
    }

    @Test
    fun sectionsMissingFromASavedOrderReturnToTheirDefaultPosition() {
        val resolved = InsightsLayout.resolve(InsightsTab.HABITS, listOf("activity_calendar"))
        assertEquals(listOf(CATEGORIES, PEAK_HOURS, ACTIVITY_CALENDAR), resolved)
    }

    @Test
    fun everyResolvedOrderContainsEachSectionOfTheTabExactlyOnce() {
        val resolved = InsightsLayout.resolve(InsightsTab.SPENDING, listOf("x", "net_worth", "net_worth", "peak_hours"))
        assertEquals(spendingDefault.toSet(), resolved.toSet())
        assertEquals(spendingDefault.size, resolved.size)
    }

    @Test
    fun movingForwardShiftsTheItemAndKeepsTheRest() {
        assertEquals(listOf(BUDGET, CASH_FLOW, NET_WORTH, OVERVIEW), InsightsLayout.move(spendingDefault, 0, 3))
    }

    @Test
    fun movingBackwardShiftsTheItemAndKeepsTheRest() {
        assertEquals(listOf(NET_WORTH, OVERVIEW, BUDGET, CASH_FLOW), InsightsLayout.move(spendingDefault, 3, 0))
    }

    @Test
    fun movingByOneSwapsNeighbours() {
        assertEquals(listOf(OVERVIEW, CASH_FLOW, BUDGET, NET_WORTH), InsightsLayout.move(spendingDefault, 1, 2))
    }

    @Test
    fun movingBeyondTheEdgesIsClamped() {
        assertEquals(listOf(BUDGET, CASH_FLOW, NET_WORTH, OVERVIEW), InsightsLayout.move(spendingDefault, 0, 99))
        assertEquals(listOf(NET_WORTH, OVERVIEW, BUDGET, CASH_FLOW), InsightsLayout.move(spendingDefault, 3, -5))
    }

    @Test
    fun movingToTheSamePositionOrFromAnInvalidIndexChangesNothing() {
        assertSame(spendingDefault, InsightsLayout.move(spendingDefault, 1, 1))
        assertSame(spendingDefault, InsightsLayout.move(spendingDefault, 7, 0))
        assertSame(spendingDefault, InsightsLayout.move(spendingDefault, -1, 0))
    }

    @Test
    fun theDefaultOrderIsDetected() {
        assertTrue(InsightsLayout.isDefault(InsightsTab.SPENDING, spendingDefault))
        assertFalse(InsightsLayout.isDefault(InsightsTab.SPENDING, InsightsLayout.move(spendingDefault, 0, 1)))
    }

    @Test
    fun storageRoundTripPreservesTheOrder() {
        val custom = listOf(PEAK_HOURS, ACTIVITY_CALENDAR, CATEGORIES)
        assertEquals(custom, InsightsLayout.resolve(InsightsTab.HABITS, InsightsLayout.toStorage(custom)))
        val spending = listOf(NET_WORTH, OVERVIEW, BUDGET, CASH_FLOW)
        assertEquals(spending, InsightsLayout.resolve(InsightsTab.SPENDING, InsightsLayout.toStorage(spending)))
    }

    @Test
    fun storageIdsAreUniqueAndStable() {
        val ids = InsightsSection.entries.map { it.storageId }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(
            listOf("overview", "budget", "cash_flow", "net_worth", "categories", "peak_hours", "activity_calendar"),
            ids
        )
    }
}
