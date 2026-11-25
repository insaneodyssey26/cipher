package com.masum.cipher.core.domain.model

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object AccentPalette {
    const val MIN_BRIGHTNESS = 0.25f
    private const val OPAQUE = 0xFF000000L

    fun hsvToArgb(hue: Float, saturation: Float, value: Float): Long {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val chroma = v * s
        val sector = h / 60f
        val x = chroma * (1f - abs(sector % 2f - 1f))
        val m = v - chroma
        val (r, g, b) = when (floor(sector).toInt()) {
            0 -> Triple(chroma, x, 0f)
            1 -> Triple(x, chroma, 0f)
            2 -> Triple(0f, chroma, x)
            3 -> Triple(0f, x, chroma)
            4 -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }
        return pack(channel(r + m), channel(g + m), channel(b + m))
    }

    fun argbToHsv(argb: Long): FloatArray {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        val hue = when {
            delta == 0f -> 0f
            maxC == r -> 60f * (((g - b) / delta) % 6f)
            maxC == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        val saturation = if (maxC == 0f) 0f else delta / maxC
        return floatArrayOf(((hue % 360f) + 360f) % 360f, saturation, maxC)
    }

    fun toHex(argb: Long): String = "%06X".format(argb and 0xFFFFFF)

    fun parseHex(input: String): Long? {
        val digits = input.trim().removePrefix("#")
        val expanded = when (digits.length) {
            3 -> digits.flatMap { listOf(it, it) }.joinToString("")
            6 -> digits
            else -> return null
        }
        if (!expanded.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        return OPAQUE or expanded.toLong(16)
    }

    fun clampBrightness(argb: Long): Long {
        val hsv = argbToHsv(argb)
        return if (hsv[2] < MIN_BRIGHTNESS) hsvToArgb(hsv[0], hsv[1], MIN_BRIGHTNESS) else OPAQUE or (argb and 0xFFFFFF)
    }

    private fun channel(value: Float): Int = (value * 255f).roundToInt().coerceIn(0, 255)

    private fun pack(r: Int, g: Int, b: Int): Long =
        OPAQUE or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}
