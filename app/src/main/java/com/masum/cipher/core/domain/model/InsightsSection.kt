package com.masum.cipher.core.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.masum.cipher.R
import compose.icons.LucideIcons
import compose.icons.lucideicons.CalendarDays
import compose.icons.lucideicons.ChartPie
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Landmark
import compose.icons.lucideicons.LayoutDashboard
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Wallet

enum class InsightsTab { SPENDING, HABITS }

enum class InsightsSection(
    val tab: InsightsTab,
    val storageId: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    OVERVIEW(InsightsTab.SPENDING, "overview", R.string.overview, LucideIcons.LayoutDashboard),
    BUDGET(InsightsTab.SPENDING, "budget", R.string.dashboard_monthly_budget, LucideIcons.Wallet),
    CASH_FLOW(InsightsTab.SPENDING, "cash_flow", R.string.cash_flow_trend, LucideIcons.TrendingUp),
    NET_WORTH(InsightsTab.SPENDING, "net_worth", R.string.insights_net_worth_title, LucideIcons.Landmark),
    CATEGORIES(InsightsTab.HABITS, "categories", R.string.spending_by_category, LucideIcons.ChartPie),
    PEAK_HOURS(InsightsTab.HABITS, "peak_hours", R.string.peak_spending_hours, LucideIcons.Clock),
    ACTIVITY_CALENDAR(InsightsTab.HABITS, "activity_calendar", R.string.activity_calendar, LucideIcons.CalendarDays);

    companion object {
        fun defaultOrder(tab: InsightsTab): List<InsightsSection> = entries.filter { it.tab == tab }
    }
}

object InsightsLayout {

    fun resolve(tab: InsightsTab, savedIds: List<String>): List<InsightsSection> {
        val defaults = InsightsSection.defaultOrder(tab)
        val result = savedIds
            .mapNotNull { id -> defaults.firstOrNull { it.storageId == id } }
            .distinct()
            .toMutableList()
        defaults.forEachIndexed { defaultIndex, section ->
            if (section !in result) {
                result.add(defaultIndex.coerceAtMost(result.size), section)
            }
        }
        return result
    }

    fun move(order: List<InsightsSection>, from: Int, to: Int): List<InsightsSection> {
        if (from !in order.indices) return order
        val target = to.coerceIn(0, order.lastIndex)
        if (target == from) return order
        val mutable = order.toMutableList()
        mutable.add(target, mutable.removeAt(from))
        return mutable
    }

    fun toStorage(order: List<InsightsSection>): List<String> = order.map { it.storageId }

    fun isDefault(tab: InsightsTab, order: List<InsightsSection>): Boolean =
        order == InsightsSection.defaultOrder(tab)
}
