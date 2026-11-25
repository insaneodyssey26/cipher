package com.masum.cipher.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import com.masum.cipher.R
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import kotlinx.coroutines.delay
import java.util.Locale

private const val DEMO_AMOUNT = 1299f

@Composable
internal fun CaptureFlowDemo(
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            step = 0
            delay(900)
            step = 1
            delay(1400)
            step = 2
            delay(1600)
            step = 3
            delay(3800)
        }
    }

    val messageProgress by animateFloatAsState(
        targetValue = if (step >= 1) 1f else 0f,
        animationSpec = if (step >= 1) spring(dampingRatio = 0.8f, stiffness = 380f) else tween(260),
        label = "capture_message"
    )
    val highlight by animateFloatAsState(
        targetValue = if (step >= 2) 1f else 0f,
        animationSpec = tween(if (step >= 2) 520 else 200),
        label = "capture_highlight"
    )
    val connectorProgress by animateFloatAsState(
        targetValue = if (step >= 2) 1f else 0f,
        animationSpec = tween(if (step >= 2) 1100 else 200, easing = FastOutSlowInEasing),
        label = "capture_connector"
    )
    val ledgerProgress by animateFloatAsState(
        targetValue = if (step >= 3) 1f else 0f,
        animationSpec = tween(if (step >= 3) 420 else 220),
        label = "capture_ledger"
    )
    val amount = remember { Animatable(0f) }
    LaunchedEffect(step >= 3) {
        if (step >= 3) {
            amount.animateTo(DEMO_AMOUNT, tween(900, easing = FastOutSlowInEasing))
        } else {
            amount.snapTo(0f)
        }
    }

    val description = stringResource(R.string.onboarding_capture_tour_subtitle)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CaptureMessageCard(
            currencySymbol = currencySymbol,
            progress = { messageProgress },
            highlight = { highlight }
        )
        CaptureConnector(
            progress = { connectorProgress },
            isVisible = step >= 2
        )
        CaptureLedgerCard(
            currencySymbol = currencySymbol,
            reveal = { ledgerProgress },
            amount = { amount.value },
            isLogged = step >= 3
        )
    }
}

@Composable
private fun captureCardModifier(borderColor: Color): Modifier =
    Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        .border(1.dp, borderColor, RoundedCornerShape(20.dp))

@Composable
private fun CaptureMessageCard(
    currencySymbol: String,
    progress: () -> Float,
    highlight: () -> Float
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val justNow = stringResource(R.string.onboarding_capture_demo_just_now)
    val emphasis = highlight()
    val emphasisStyle = SpanStyle(
        background = primary.copy(alpha = 0.2f * emphasis),
        color = lerp(onSurface, primary, emphasis),
        fontWeight = FontWeight.Bold
    )
    val message = buildAnnotatedString {
        append("Paid ")
        withStyle(emphasisStyle) { append("$currencySymbol 1,299") }
        append(" at ")
        withStyle(emphasisStyle) { append("Zara India") }
        append(" on Card 4821")
    }

    Column(
        modifier = captureCardModifier(MaterialTheme.colorScheme.outlineVariant)
            .graphicsLayer {
                val p = progress()
                alpha = p
                translationY = (1f - p) * -24.dp.toPx()
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "H",
                    style = Typography.labelLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = primary
                )
            }
            Text(
                text = "HDFC Bank",
                style = Typography.titleSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = justNow,
                style = Typography.labelSmall.copy(fontFamily = Lato, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            style = Typography.bodyMedium.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 21.sp
            ),
            color = onSurface.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun CaptureConnector(
    progress: () -> Float,
    isVisible: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.outlineVariant
    val background = MaterialTheme.colorScheme.background
    val chipAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(260),
        label = "capture_chip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                val x = size.width / 2f
                val stroke = 2.dp.toPx()
                drawLine(track, Offset(x, 0f), Offset(x, size.height), stroke, StrokeCap.Round)
                val p = progress()
                if (p > 0f) {
                    val y = size.height * p
                    drawLine(primary, Offset(x, 0f), Offset(x, y), stroke, StrokeCap.Round)
                    if (p < 0.98f) {
                        drawCircle(primary.copy(alpha = 0.25f), 7.dp.toPx(), Offset(x, y))
                        drawCircle(primary, 3.5.dp.toPx(), Offset(x, y))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { alpha = chipAlpha }
                .clip(RoundedCornerShape(20.dp))
                .background(background)
                .background(primary.copy(alpha = 0.12f))
                .border(1.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_capture_demo_understood),
                style = Typography.labelSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp
                ),
                color = primary
            )
        }
    }
}

@Composable
private fun CaptureLedgerCard(
    currencySymbol: String,
    reveal: () -> Float,
    amount: () -> Float,
    isLogged: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val borderColor by animateColorAsState(
        targetValue = if (isLogged) EmeraldIncome.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(360),
        label = "capture_ledger_border"
    )
    val pulse by rememberInfiniteTransition(label = "capture_skeleton").animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "capture_skeleton_pulse"
    )
    val skeletonColor = onSurface.copy(alpha = 0.09f)

    Box(modifier = captureCardModifier(borderColor).padding(16.dp)) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = (1f - reveal()) * pulse },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(skeletonColor)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(skeletonColor)
                )
                Box(
                    modifier = Modifier
                        .size(width = 76.dp, height = 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(skeletonColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = reveal() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(onSurface.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Z",
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = onSurface
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Zara India",
                    style = Typography.titleSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary.copy(alpha = 0.14f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cat_shopping),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = primary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "•• 4821",
                        style = Typography.labelSmall.copy(fontFamily = Lato, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-$currencySymbol${String.format(Locale.US, "%,.2f", amount())}",
                    style = Typography.titleSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = RoseExpense,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedVisibility(
                    visible = isLogged,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(200))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = null,
                            tint = EmeraldIncome,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = stringResource(R.string.onboarding_capture_demo_logged),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = EmeraldIncome,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
