package com.masum.cipher.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.TransactionCategory
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Bot
import kotlinx.coroutines.delay

private data class DemoExpense(
    val merchant: String,
    val amount: String,
    val category: TransactionCategory
)

private val DemoExpenses = listOf(
    DemoExpense("Swiggy", "450", TransactionCategory.FOOD),
    DemoExpense("Uber", "220", TransactionCategory.TRANSPORT),
    DemoExpense("Netflix", "649", TransactionCategory.ENTERTAINMENT),
    DemoExpense("Amazon", "1,299", TransactionCategory.SHOPPING)
)

@Composable
internal fun SmartCategoriesDemo(
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var organized by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            organized = 0
            delay(1300)
            for (index in 1..DemoExpenses.size) {
                organized = index
                delay(if (index == 1) 1500L else 900L)
            }
            delay(3200)
        }
    }

    val progress by animateFloatAsState(
        targetValue = organized.toFloat() / DemoExpenses.size,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "smart_progress"
    )
    val description = stringResource(R.string.onboarding_rules_tour_subtitle)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
        ) {
            DemoExpenses.forEachIndexed { index, expense ->
                SmartExpenseRow(
                    expense = expense,
                    currencySymbol = currencySymbol,
                    isOrganized = organized > index
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 68.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedContent(
                    targetState = organized,
                    transitionSpec = {
                        (slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(200)))
                            .togetherWith(slideOutVertically(tween(140)) { -it / 2 } + fadeOut(tween(120)))
                            .using(SizeTransform(clip = true))
                    },
                    label = "smart_count"
                ) { count ->
                    Text(
                        text = stringResource(R.string.onboarding_rules_demo_organized, count, DemoExpenses.size),
                        style = Typography.labelMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = organized in 1..2,
                transitionSpec = {
                    (fadeIn(tween(240)) + slideInVertically(spring(dampingRatio = 0.8f, stiffness = 380f)) { it / 2 })
                        .togetherWith(fadeOut(tween(200)))
                        .using(SizeTransform(clip = false))
                },
                label = "smart_rule_toast"
            ) { showRule ->
                if (showRule) {
                    val first = DemoExpenses.first()
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Bot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(
                                R.string.onboarding_rules_demo_rule_saved,
                                first.merchant,
                                stringResource(first.category.titleRes)
                            ),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartExpenseRow(
    expense: DemoExpense,
    currencySymbol: String,
    isOrganized: Boolean
) {
    val category = expense.category
    val onSurface = MaterialTheme.colorScheme.onSurface
    val avatarColor by animateColorAsState(
        targetValue = if (isOrganized) category.color.copy(alpha = 0.16f) else onSurface.copy(alpha = 0.08f),
        animationSpec = tween(320),
        label = "smart_avatar"
    )
    val rowTint by animateColorAsState(
        targetValue = if (isOrganized) category.color.copy(alpha = 0.05f) else Color.Transparent,
        animationSpec = tween(320),
        label = "smart_row_tint"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowTint)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isOrganized, animationSpec = tween(260), label = "smart_avatar_content") { organized ->
                if (organized) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(19.dp)
                    )
                } else {
                    Text(
                        text = expense.merchant.take(1),
                        style = Typography.titleMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = onSurface
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.merchant,
                style = Typography.titleSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                ),
                color = onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = isOrganized,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), initialScale = 0.85f))
                        .togetherWith(fadeOut(tween(140)))
                        .using(SizeTransform(clip = false))
                },
                label = "smart_chip"
            ) { organized ->
                if (organized) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(category.color.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = stringResource(category.titleRes),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = category.color,
                            maxLines = 1
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(onSurface.copy(alpha = 0.07f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_rules_demo_uncategorized),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Text(
            text = "-$currencySymbol${expense.amount}",
            style = Typography.titleSmall.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = RoseExpense,
            maxLines = 1,
            softWrap = false
        )
    }
}
