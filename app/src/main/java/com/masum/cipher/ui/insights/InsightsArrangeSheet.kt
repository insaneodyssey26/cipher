package com.masum.cipher.ui.insights

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.InsightsLayout
import com.masum.cipher.core.domain.model.InsightsSection
import com.masum.cipher.core.domain.model.InsightsTab
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.CipherSheet
import com.masum.cipher.ui.components.FormPrimaryButton
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.ChevronUp
import compose.icons.lucideicons.GripVertical
import compose.icons.lucideicons.RotateCcw
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val RowHeight = 64.dp
private val RowGap = 8.dp
private val CompactWidth = 340.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InsightsArrangeSheet(
    tab: InsightsTab,
    savedOrder: List<InsightsSection>,
    isHapticsEnabled: Boolean,
    onOrderChange: (List<InsightsSection>) -> Unit,
    onDismiss: () -> Unit
) {
    val orderState = remember(tab) { mutableStateOf(savedOrder) }
    var heldSection by remember { mutableStateOf<InsightsSection?>(null) }
    val view = LocalView.current
    val currentOnOrderChange by rememberUpdatedState(onOrderChange)
    val isDefault = InsightsLayout.isDefault(tab, orderState.value)

    val applyOrder: (List<InsightsSection>) -> Unit = { newOrder ->
        if (newOrder != orderState.value) {
            orderState.value = newOrder
            currentOnOrderChange(newOrder)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = heldSection == null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState(), enabled = heldSection == null)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.insights_arrange_title),
                        style = Typography.titleLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.insights_arrange_subtitle),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ResetButton(
                    enabled = !isDefault,
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        applyOrder(InsightsSection.defaultOrder(tab))
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            ArrangeList(
                orderState = orderState,
                heldSection = heldSection,
                onHeldChange = { heldSection = it },
                onOrderChange = applyOrder,
                onTick = { view.performVibrate(isHapticsEnabled, isLongPress = false) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            FormPrimaryButton(
                text = stringResource(R.string.action_done),
                enabled = true,
                accent = MaterialTheme.colorScheme.primary,
                onClick = onDismiss,
                background = Color.Transparent
            )
        }
    }
}

@Composable
private fun ResetButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "arrange_reset_alpha"
    )
    val label = stringResource(R.string.insights_arrange_reset)
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LucideIcons.RotateCcw,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ArrangeList(
    orderState: State<List<InsightsSection>>,
    heldSection: InsightsSection?,
    onHeldChange: (InsightsSection?) -> Unit,
    onOrderChange: (List<InsightsSection>) -> Unit,
    onTick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val stridePx = with(LocalDensity.current) { (RowHeight + RowGap).toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var settlingSection by remember { mutableStateOf<InsightsSection?>(null) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val currentOnHeldChange by rememberUpdatedState(onHeldChange)
    val currentOnOrderChange by rememberUpdatedState(onOrderChange)
    val currentOnTick by rememberUpdatedState(onTick)

    val count = orderState.value.size
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight * count + RowGap * (count - 1).coerceAtLeast(0))
    ) {
        val compact = maxWidth < CompactWidth

        orderState.value.forEachIndexed { index, section ->
            key(section) {
                val isHeld = heldSection == section
                val isActive = isHeld || settlingSection == section
                val slot = remember { Animatable(index * stridePx) }
                val lift by animateFloatAsState(
                    targetValue = if (isHeld) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                    label = "arrange_row_lift"
                )

                LaunchedEffect(index, isActive) {
                    val target = index * stridePx
                    if (isActive) {
                        slot.snapTo(target)
                    } else {
                        slot.animateTo(target, spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                    }
                }

                ArrangeRow(
                    section = section,
                    compact = compact,
                    canMoveUp = index > 0,
                    canMoveDown = index < count - 1,
                    lift = lift,
                    modifier = Modifier
                        .offset {
                            val liveIndex = orderState.value.indexOf(section)
                            val y = if (isActive) liveIndex * stridePx + dragOffset else slot.value
                            IntOffset(0, y.roundToInt())
                        }
                        .zIndex(if (isActive) 1f else 0f),
                    onMoveUp = {
                        val current = orderState.value
                        val from = current.indexOf(section)
                        currentOnOrderChange(InsightsLayout.move(current, from, from - 1))
                        currentOnTick()
                    },
                    onMoveDown = {
                        val current = orderState.value
                        val from = current.indexOf(section)
                        currentOnOrderChange(InsightsLayout.move(current, from, from + 1))
                        currentOnTick()
                    },
                    dragModifier = Modifier.pointerInput(section) {
                        detectDragGestures(
                            onDragStart = {
                                settleJob?.cancel()
                                settlingSection = null
                                dragOffset = 0f
                                currentOnHeldChange(section)
                                currentOnTick()
                            },
                            onDragEnd = {
                                settlingSection = section
                                currentOnHeldChange(null)
                                settleJob = scope.launch {
                                    animate(
                                        initialValue = dragOffset,
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                    ) { value, _ -> dragOffset = value }
                                    settlingSection = null
                                }
                            },
                            onDragCancel = {
                                settlingSection = section
                                currentOnHeldChange(null)
                                settleJob = scope.launch {
                                    animate(
                                        initialValue = dragOffset,
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                    ) { value, _ -> dragOffset = value }
                                    settlingSection = null
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val current = orderState.value
                                val from = current.indexOf(section)
                                val lastIndex = current.lastIndex
                                dragOffset = (dragOffset + dragAmount.y)
                                    .coerceIn(-from * stridePx, (lastIndex - from) * stridePx)
                                val to = (from + (dragOffset / stridePx).roundToInt()).coerceIn(0, lastIndex)
                                if (to != from) {
                                    currentOnOrderChange(InsightsLayout.move(current, from, to))
                                    dragOffset -= (to - from) * stridePx
                                    currentOnTick()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ArrangeRow(
    section: InsightsSection,
    compact: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    lift: Float,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    dragModifier: Modifier,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(18.dp)
    val title = stringResource(section.titleRes).lowercase().replaceFirstChar { it.uppercase() }
    val idleBorder = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .graphicsLayer {
                val scale = 1f + 0.03f * lift
                scaleX = scale
                scaleY = scale
                shadowElevation = 14.dp.toPx() * lift
                this.shape = shape
                clip = false
            }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, if (lift > 0.01f) primary.copy(alpha = 0.55f) else idleBorder, shape)
            .padding(start = 2.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .then(dragModifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LucideIcons.GripVertical,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (!compact) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = title,
            style = Typography.titleSmall.copy(
                fontFamily = DMSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = if (compact) 6.dp else 12.dp)
        )

        ArrangeArrow(
            icon = LucideIcons.ChevronUp,
            description = stringResource(R.string.insights_arrange_move_up, title),
            enabled = canMoveUp,
            onClick = onMoveUp
        )
        ArrangeArrow(
            icon = LucideIcons.ChevronDown,
            description = stringResource(R.string.insights_arrange_move_down, title),
            enabled = canMoveDown,
            onClick = onMoveDown
        )
    }
}

@Composable
private fun ArrangeArrow(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.25f,
        label = "arrange_arrow_alpha"
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
    }
}
