package com.masum.cipher.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.pref.AccentColor
import com.masum.cipher.core.domain.model.AccentPalette
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.Lock
import compose.icons.lucideicons.Pencil
import compose.icons.lucideicons.Pipette

private const val SwatchesPerRow = 6

@Composable
internal fun AccentColorPicker(
    selected: AccentColor,
    customColor: Long,
    isPro: Boolean,
    onPresetSelected: (AccentColor) -> Unit,
    onCustomClick: () -> Unit,
    onLockedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val freeColors = remember { AccentColor.presets.filterNot { it.isProOnly } }
    val proColors = remember { AccentColor.presets.filter { it.isProOnly } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        GroupLabel(text = stringResource(R.string.accent_group_free))
        Spacer(modifier = Modifier.height(10.dp))
        SwatchGrid(
            colors = freeColors,
            selected = selected,
            isPro = isPro,
            onPresetSelected = onPresetSelected,
            onLockedClick = onLockedClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        GroupLabel(text = stringResource(R.string.accent_group_pro), showCrown = true)
        Spacer(modifier = Modifier.height(10.dp))
        SwatchGrid(
            colors = proColors,
            selected = selected,
            isPro = isPro,
            onPresetSelected = onPresetSelected,
            onLockedClick = onLockedClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        CustomColorRow(
            isSelected = selected == AccentColor.CUSTOM,
            customColor = customColor,
            isLocked = !isPro,
            onClick = if (isPro) onCustomClick else onLockedClick
        )
    }
}

@Composable
private fun GroupLabel(text: String, showCrown: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = Typography.labelSmall.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showCrown) {
            Icon(
                imageVector = LucideIcons.Crown,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
private fun SwatchGrid(
    colors: List<AccentColor>,
    selected: AccentColor,
    isPro: Boolean,
    onPresetSelected: (AccentColor) -> Unit,
    onLockedClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.chunked(SwatchesPerRow).forEach { rowColors ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowColors.forEach { color ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AccentSwatch(
                            color = color,
                            isSelected = selected == color,
                            isLocked = color.isProOnly && !isPro,
                            onClick = {
                                if (color.isProOnly && !isPro) onLockedClick() else onPresetSelected(color)
                            }
                        )
                    }
                }
                repeat(SwatchesPerRow - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    color: AccentColor,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val swatchColor = Color(color.colorValue)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "accent_swatch_scale"
    )
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) swatchColor else Color.Transparent,
        label = "accent_swatch_ring"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .selectable(
                selected = isSelected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .border(2.dp, ringColor, CircleShape)
            .padding(4.dp)
            .background(swatchColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSelected -> Icon(
                imageVector = LucideIcons.Check,
                contentDescription = color.colorName,
                tint = if (swatchColor.luminance() > 0.6f) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
            isLocked -> Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Lock,
                    contentDescription = color.colorName,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomColorRow(
    isSelected: Boolean,
    customColor: Long,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant,
        label = "accent_custom_border"
    )
    val swatch = Color(customColor)
    val rainbow = remember {
        Brush.sweepGradient(
            listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { Color(AccentPalette.hsvToArgb(it, 0.85f, 1f)) }
        )
    }
    val title = stringResource(R.string.accent_custom_title)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isSelected) primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .selectable(
                selected = isSelected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) Brush.linearGradient(listOf(swatch, swatch)) else rainbow),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = LucideIcons.Check,
                    contentDescription = null,
                    tint = if (swatch.luminance() > 0.6f) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) LucideIcons.Lock else LucideIcons.Pipette,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFFFBBF24) else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.bodyLarge.copy(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = if (isSelected) "#" + AccentPalette.toHex(customColor) else stringResource(R.string.accent_custom_hint),
                style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Icon(
            imageVector = if (isSelected) LucideIcons.Pencil else LucideIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(if (isSelected) 16.dp else 18.dp)
        )
    }
}
