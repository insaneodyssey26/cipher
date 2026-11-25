package com.masum.cipher.core.domain

import com.masum.cipher.core.data.local.pref.AccentColor
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserSettings
import com.masum.cipher.core.domain.model.AccentPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccentPaletteTest {

    @Test
    fun primaryHuesConvertToTheExpectedColors() {
        assertEquals(0xFFFF0000L, AccentPalette.hsvToArgb(0f, 1f, 1f))
        assertEquals(0xFF00FF00L, AccentPalette.hsvToArgb(120f, 1f, 1f))
        assertEquals(0xFF0000FFL, AccentPalette.hsvToArgb(240f, 1f, 1f))
        assertEquals(0xFFFFFF00L, AccentPalette.hsvToArgb(60f, 1f, 1f))
        assertEquals(0xFF00FFFFL, AccentPalette.hsvToArgb(180f, 1f, 1f))
        assertEquals(0xFFFF00FFL, AccentPalette.hsvToArgb(300f, 1f, 1f))
    }

    @Test
    fun zeroSaturationIsGreyAndZeroBrightnessIsBlack() {
        assertEquals(0xFFFFFFFFL, AccentPalette.hsvToArgb(200f, 0f, 1f))
        assertEquals(0xFF808080L, AccentPalette.hsvToArgb(200f, 0f, 0.5f))
        assertEquals(0xFF000000L, AccentPalette.hsvToArgb(200f, 1f, 0f))
    }

    @Test
    fun hueWrapsAroundAndInputsAreClamped() {
        assertEquals(AccentPalette.hsvToArgb(0f, 1f, 1f), AccentPalette.hsvToArgb(360f, 1f, 1f))
        assertEquals(AccentPalette.hsvToArgb(120f, 1f, 1f), AccentPalette.hsvToArgb(480f, 1f, 1f))
        assertEquals(AccentPalette.hsvToArgb(300f, 1f, 1f), AccentPalette.hsvToArgb(-60f, 1f, 1f))
        assertEquals(AccentPalette.hsvToArgb(0f, 1f, 1f), AccentPalette.hsvToArgb(0f, 5f, 5f))
    }

    @Test
    fun argbToHsvRecoversTheComponents() {
        val red = AccentPalette.argbToHsv(0xFFFF0000L)
        assertEquals(0f, red[0], 0.01f)
        assertEquals(1f, red[1], 0.001f)
        assertEquals(1f, red[2], 0.001f)

        val blue = AccentPalette.argbToHsv(0xFF0000FFL)
        assertEquals(240f, blue[0], 0.01f)

        val grey = AccentPalette.argbToHsv(0xFF808080L)
        assertEquals(0f, grey[1], 0.001f)
        assertEquals(0.502f, grey[2], 0.002f)

        val black = AccentPalette.argbToHsv(0xFF000000L)
        assertEquals(0f, black[1], 0.001f)
        assertEquals(0f, black[2], 0.001f)
    }

    @Test
    fun everyPresetSurvivesAHsvRoundTrip() {
        AccentColor.presets.forEach { preset ->
            val hsv = AccentPalette.argbToHsv(preset.colorValue)
            val roundTripped = AccentPalette.hsvToArgb(hsv[0], hsv[1], hsv[2])
            val expected = preset.colorValue
            for (shift in listOf(16, 8, 0)) {
                val a = (expected shr shift) and 0xFF
                val b = (roundTripped shr shift) and 0xFF
                assertTrue("${preset.name} channel drifted", kotlin.math.abs(a - b) <= 1)
            }
        }
    }

    @Test
    fun hexFormattingIsSixUppercaseDigits() {
        assertEquals("6366F1", AccentPalette.toHex(0xFF6366F1L))
        assertEquals("00000A", AccentPalette.toHex(0xFF00000AL))
        assertEquals("FFFFFF", AccentPalette.toHex(0xFFFFFFFFL))
    }

    @Test
    fun hexParsingAcceptsCommonForms() {
        assertEquals(0xFF6366F1L, AccentPalette.parseHex("6366F1"))
        assertEquals(0xFF6366F1L, AccentPalette.parseHex("#6366f1"))
        assertEquals(0xFF6366F1L, AccentPalette.parseHex("  #6366F1 "))
        assertEquals(0xFFAABBCCL, AccentPalette.parseHex("ABC"))
        assertEquals(0xFFAABBCCL, AccentPalette.parseHex("#abc"))
    }

    @Test
    fun hexParsingRejectsInvalidInput() {
        assertNull(AccentPalette.parseHex(""))
        assertNull(AccentPalette.parseHex("12345"))
        assertNull(AccentPalette.parseHex("1234567"))
        assertNull(AccentPalette.parseHex("GGGGGG"))
        assertNull(AccentPalette.parseHex("#12 456"))
    }

    @Test
    fun hexRoundTripKeepsTheColor() {
        listOf(0xFF6366F1L, 0xFF000000L, 0xFFFFFFFFL, 0xFF84CC16L).forEach {
            assertEquals(it, AccentPalette.parseHex(AccentPalette.toHex(it)))
        }
    }

    @Test
    fun veryDarkColorsAreLiftedToTheMinimumBrightness() {
        val lifted = AccentPalette.clampBrightness(0xFF000000L)
        assertEquals(AccentPalette.MIN_BRIGHTNESS, AccentPalette.argbToHsv(lifted)[2], 0.01f)
        assertEquals(0xFF6366F1L, AccentPalette.clampBrightness(0xFF6366F1L))
    }

    @Test
    fun clampingAlwaysReturnsAnOpaqueColor() {
        assertEquals(0xFFL, (AccentPalette.clampBrightness(0x006366F1L) shr 24) and 0xFFL)
    }

    @Test
    fun customIsAProOnlyChoiceAndNeverAPresetSwatch() {
        assertTrue(AccentColor.CUSTOM.isProOnly)
        assertFalse(AccentColor.presets.contains(AccentColor.CUSTOM))
        assertEquals(AccentColor.entries.size - 1, AccentColor.presets.size)
    }

    @Test
    fun presetColorValuesAreDistinct() {
        val values = AccentColor.presets.map { it.colorValue }
        assertEquals(values.size, values.toSet().size)
    }

    private fun settings(accent: AccentColor, custom: Long) = UserSettings(
        theme = AppTheme.SYSTEM,
        isBiometricEnabled = false,
        isPrivacyModeEnabled = false,
        isHapticsEnabled = true,
        currency = "USD",
        currencyCode = "USD",
        autoLockTimeout = 0L,
        lastStopTime = 0L,
        monthlyBudget = 0.0,
        accentColor = accent,
        customAccentColor = custom
    )

    @Test
    fun aPresetSelectionResolvesToThePresetColor() {
        assertEquals(AccentColor.MINT.colorValue, settings(AccentColor.MINT, 0xFF123456L).accentColorValue)
    }

    @Test
    fun theCustomSelectionResolvesToTheStoredCustomColor() {
        assertEquals(0xFF123456L, settings(AccentColor.CUSTOM, 0xFF123456L).accentColorValue)
    }

    @Test
    fun aStoredCustomColorIsIgnoredWhileAPresetIsSelected() {
        val resolved = settings(AccentColor.CHERRY, 0xFF123456L).accentColorValue
        assertNotNull(resolved)
        assertEquals(AccentColor.CHERRY.colorValue, resolved)
    }
}
