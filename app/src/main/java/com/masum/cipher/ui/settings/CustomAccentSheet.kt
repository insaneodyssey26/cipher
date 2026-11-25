package com.masum.cipher.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.AccentPalette
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.CipherSheet
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.MidnightDeep
import com.masum.cipher.ui.theme.Slate50
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.bestContrastingColor
import com.masum.cipher.ui.theme.contrastRatio
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.TriangleAlert
import compose.icons.lucideicons.Wallet
import kotlin.math.roundToInt

private const val LowContrastThreshold = 3f
private val ThumbSize = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomAccentSheet(
    initialColor: Long,
    isHapticsEnabled: Boolean,
    onApply: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHsv = remember { AccentPalette.argbToHsv(initialColor) }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2].coerceAtLeast(AccentPalette.MIN_BRIGHTNESS)) }
    var hexText by remember { mutableStateOf(AccentPalette.toHex(AccentPalette.hsvToArgb(hue, saturation, brightness))) }

    val argb = AccentPalette.hsvToArgb(hue, saturation, brightness)
    val color = Color(argb)
    val onColor = bestContrastingColor(color, Slate50, MidnightDeep)
    val isLowContrast = contrastRatio(color, MaterialTheme.colorScheme.background) < LowContrastThreshold
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun syncHex() {
        hexText = AccentPalette.toHex(AccentPalette.hsvToArgb(hue, saturation, brightness))
    }

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.accent_custom_title),
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
                text = stringResource(R.string.accent_custom_sheet_subtitle),
                style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            AccentPreview(color = color, onColor = onColor)

            Spacer(modifier = Modifier.height(20.dp))

            ChannelSlider(
                label = stringResource(R.string.accent_custom_hue),
                value = hue,
                valueRange = 0f..360f,
                brush = Brush.horizontalGradient(
                    (0..6).map { Color(AccentPalette.hsvToArgb(it * 60f, 1f, 1f)) }
                ),
                thumbColor = Color(AccentPalette.hsvToArgb(hue, 1f, 1f)),
                onValueChange = { hue = it; syncHex() }
            )
            ChannelSlider(
                label = stringResource(R.string.accent_custom_saturation),
                value = saturation,
                valueRange = 0f..1f,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(AccentPalette.hsvToArgb(hue, 0f, brightness)),
                        Color(AccentPalette.hsvToArgb(hue, 1f, brightness))
                    )
                ),
                thumbColor = color,
                onValueChange = { saturation = it; syncHex() }
            )
            ChannelSlider(
                label = stringResource(R.string.accent_custom_brightness),
                value = brightness,
                valueRange = AccentPalette.MIN_BRIGHTNESS..1f,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(AccentPalette.hsvToArgb(hue, saturation, AccentPalette.MIN_BRIGHTNESS)),
                        Color(AccentPalette.hsvToArgb(hue, saturation, 1f))
                    )
                ),
                thumbColor = color,
                onValueChange = { brightness = it; syncHex() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            HexField(
                color = color,
                text = hexText,
                onTextChange = { raw ->
                    val cleaned = raw.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        .uppercase()
                        .take(6)
                    hexText = cleaned
                    if (cleaned.length == 6) {
                        AccentPalette.parseHex(cleaned)?.let { parsed ->
                            val hsv = AccentPalette.argbToHsv(parsed)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2].coerceAtLeast(AccentPalette.MIN_BRIGHTNESS)
                            if (hsv[2] < AccentPalette.MIN_BRIGHTNESS) syncHex()
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = isLowContrast,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.TriangleAlert,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.accent_custom_low_contrast),
                        style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ApplyButton(
                text = stringResource(R.string.accent_custom_apply),
                color = color,
                onColor = onColor,
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    onApply(argb)
                }
            )

            Spacer(modifier = Modifier.navigationBarsPadding().height(20.dp))
        }
    }
}

@Composable
private fun AccentPreview(color: Color, onColor: Color) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Aa",
                style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-1).sp),
                color = color
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(color)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.action_continue),
                    style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                    color = onColor,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.64f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(LucideIcons.Wallet, LucideIcons.TrendingUp, LucideIcons.Check).forEach { icon ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    brush: Brush,
    thumbColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = Typography.labelSmall.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GradientSlider(
            value = value,
            valueRange = valueRange,
            brush = brush,
            thumbColor = thumbColor,
            description = label,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun GradientSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    brush: Brush,
    thumbColor: Color,
    description: String,
    onValueChange: (Float) -> Unit
) {
    var widthPx by remember { mutableFloatStateOf(0f) }
    val thumbPx = with(androidx.compose.ui.platform.LocalDensity.current) { ThumbSize.toPx() }
    val span = valueRange.endInclusive - valueRange.start
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

    fun valueAt(x: Float): Float {
        val usable = (widthPx - thumbPx).coerceAtLeast(1f)
        val f = ((x - thumbPx / 2f) / usable).coerceIn(0f, 1f)
        return valueRange.start + f * span
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(valueRange) {
                detectTapGestures { offset -> onValueChange(valueAt(offset.x)) }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures(
                    onDragStart = { offset -> onValueChange(valueAt(offset.x)) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        onValueChange(valueAt(change.position.x))
                    }
                )
            }
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange)
                setProgress { target ->
                    onValueChange(target.coerceIn(valueRange.start, valueRange.endInclusive))
                    true
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(brush)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(7.dp))
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(((widthPx - thumbPx).coerceAtLeast(0f) * fraction).roundToInt(), 0) }
                .size(ThumbSize)
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(5.dp)
                .background(thumbColor, CircleShape)
        )
    }
}

@Composable
private fun HexField(
    color: Color,
    text: String,
    onTextChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "#",
            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(color),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions.Default,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ApplyButton(
    text: String,
    color: Color,
    onColor: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(),
        label = "accent_apply_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)))
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, letterSpacing = 0.2.sp),
            color = onColor,
            maxLines = 1
        )
    }
}
