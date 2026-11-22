package com.rinatblade.rinpin

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily

/** Одна цветовая тема: фон, поверхность карточек, два акцента и цвет текста. */
data class RinTheme(
    val id: Int,
    val name: String,
    val dark: Boolean,
    val bg: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val text: Color,
)

private fun t(id: Int, name: String, dark: Boolean, bg: Long, surface: Long, primary: Long, secondary: Long, text: Long) =
    RinTheme(id, name, dark, Color(bg), Color(surface), Color(primary), Color(secondary), Color(text))

object Themes {
    val dark: List<RinTheme> = listOf(
        t(0, "Полночь", true, 0xFF0B1020, 0xFF151B30, 0xFF7C9CFF, 0xFF5EEAD4, 0xFFE6EAF5),
        t(1, "Графит", true, 0xFF121212, 0xFF1E1E1E, 0xFFBB86FC, 0xFF03DAC6, 0xFFEDEDED),
        t(2, "Океан", true, 0xFF06141F, 0xFF0D2434, 0xFF38BDF8, 0xFF22D3EE, 0xFFE0F2FE),
        t(3, "Лес", true, 0xFF0A1A12, 0xFF12281B, 0xFF4ADE80, 0xFFA3E635, 0xFFE4F5EA),
        t(4, "Бордо", true, 0xFF1A0A10, 0xFF2A1119, 0xFFFB7185, 0xFFFBBF24, 0xFFFCE7EC),
        t(5, "Фиолет", true, 0xFF140B24, 0xFF211436, 0xFFA78BFA, 0xFFF472B6, 0xFFEDE9FE),
        t(6, "Янтарь", true, 0xFF181006, 0xFF271A0A, 0xFFFBBF24, 0xFFFB923C, 0xFFFEF3C7),
        t(7, "Изумруд", true, 0xFF04181A, 0xFF0A2B2D, 0xFF2DD4BF, 0xFF34D399, 0xFFD1FAF5),
        t(8, "Космос", true, 0xFF0A0A1F, 0xFF16163A, 0xFF818CF8, 0xFFC084FC, 0xFFE0E7FF),
        t(9, "Чёрный рубин", true, 0xFF000000, 0xFF111111, 0xFFFF5252, 0xFFFFAB40, 0xFFF2F2F2),
        t(10, "Сакура ночью", true, 0xFF1C1018, 0xFF2D1A27, 0xFFF9A8D4, 0xFFC4B5FD, 0xFFFDF2F8),
        t(11, "Мята ночью", true, 0xFF0B1714, 0xFF13261F, 0xFF6EE7B7, 0xFF67E8F9, 0xFFECFDF5),
        t(12, "Шоколад", true, 0xFF1A120D, 0xFF2A1D15, 0xFFD6A77A, 0xFFF59E0B, 0xFFF5E9DC),
        t(13, "Сталь", true, 0xFF10151A, 0xFF1A222B, 0xFF94A3B8, 0xFF60A5FA, 0xFFE2E8F0),
        t(14, "Неон", true, 0xFF0A0014, 0xFF1A0A2E, 0xFF00FFC6, 0xFFFF2BD6, 0xFFF0E6FF),
        t(15, "Закат", true, 0xFF1A0F14, 0xFF2B1822, 0xFFFF7A59, 0xFFFFC857, 0xFFFFEDE5),
        t(16, "Пустыня ночью", true, 0xFF171310, 0xFF251F1A, 0xFFE9B872, 0xFFD97757, 0xFFF3EADF),
        t(17, "Арктика", true, 0xFF081420, 0xFF10212F, 0xFF7DD3FC, 0xFFA5F3FC, 0xFFEAF6FF),
        t(18, "Лаванда ночью", true, 0xFF14111F, 0xFF211C33, 0xFFC4B5FD, 0xFF93C5FD, 0xFFEEEAFB),
        t(19, "Красный Китай", true, 0xFF1A0808, 0xFF2B1010, 0xFFFF4D4D, 0xFFFFD43B, 0xFFFFECEC),
    )

    val light: List<RinTheme> = listOf(
        t(20, "Снег", false, 0xFFF8FAFC, 0xFFFFFFFF, 0xFF2563EB, 0xFF0EA5E9, 0xFF0F172A),
        t(21, "Облако", false, 0xFFF1F5F9, 0xFFFFFFFF, 0xFF4F46E5, 0xFF8B5CF6, 0xFF1E293B),
        t(22, "Мята", false, 0xFFF0FDF9, 0xFFFFFFFF, 0xFF0D9488, 0xFF10B981, 0xFF134E4A),
        t(23, "Персик", false, 0xFFFFF7ED, 0xFFFFFFFF, 0xFFEA580C, 0xFFF59E0B, 0xFF431407),
        t(24, "Роза", false, 0xFFFFF1F2, 0xFFFFFFFF, 0xFFE11D48, 0xFFF472B6, 0xFF4C0519),
        t(25, "Лаванда", false, 0xFFFAF5FF, 0xFFFFFFFF, 0xFF7C3AED, 0xFFC026D3, 0xFF2E1065),
        t(26, "Лимон", false, 0xFFFEFCE8, 0xFFFFFFFF, 0xFFCA8A04, 0xFF84CC16, 0xFF422006),
        t(27, "Небо", false, 0xFFF0F9FF, 0xFFFFFFFF, 0xFF0284C7, 0xFF06B6D4, 0xFF082F49),
        t(28, "Трава", false, 0xFFF7FEE7, 0xFFFFFFFF, 0xFF16A34A, 0xFF65A30D, 0xFF052E16),
        t(29, "Песок", false, 0xFFFAF7F2, 0xFFFFFFFF, 0xFFB45309, 0xFFD97706, 0xFF3F2A14),
        t(30, "Сакура", false, 0xFFFDF2F8, 0xFFFFFFFF, 0xFFDB2777, 0xFFA855F7, 0xFF500724),
        t(31, "Бирюза", false, 0xFFECFEFF, 0xFFFFFFFF, 0xFF0891B2, 0xFF14B8A6, 0xFF083344),
        t(32, "Кофе с молоком", false, 0xFFF8F1EA, 0xFFFFFFFF, 0xFF8B5E3C, 0xFFC08457, 0xFF3B2A1E),
        t(33, "Серебро", false, 0xFFF4F4F5, 0xFFFFFFFF, 0xFF52525B, 0xFF3B82F6, 0xFF18181B),
        t(34, "Коралл", false, 0xFFFFF5F3, 0xFFFFFFFF, 0xFFF4511E, 0xFFFF8A65, 0xFF3E1A12),
        t(35, "Фисташка", false, 0xFFF3F9EC, 0xFFFFFFFF, 0xFF65A30D, 0xFF22C55E, 0xFF1A2E05),
        t(36, "Индиго", false, 0xFFEEF2FF, 0xFFFFFFFF, 0xFF4338CA, 0xFF6366F1, 0xFF1E1B4B),
        t(37, "Ромашка", false, 0xFFFFFDF5, 0xFFFFFFFF, 0xFFEAB308, 0xFFF97316, 0xFF3B2F05),
        t(38, "Нежный сад", false, 0xFFF6FBF4, 0xFFFFFFFF, 0xFF059669, 0xFFEC4899, 0xFF14352A),
        t(39, "Светлый Китай", false, 0xFFFFF8F5, 0xFFFFFFFF, 0xFFDE2910, 0xFFFFC107, 0xFF3A0A02),
    )

    val all: List<RinTheme> = dark + light

    fun byId(id: Int): RinTheme = all.firstOrNull { it.id == id } ?: all[0]
}

private fun onColor(c: Color): Color = if (c.luminance() > 0.45f) Color(0xFF141414) else Color.White

fun RinTheme.scheme(): ColorScheme {
    val onP = onColor(primary)
    val onS = onColor(secondary)
    val variant = lerp(surface, text, if (dark) 0.07f else 0.04f)
    val container = primary.copy(alpha = if (dark) 0.22f else 0.14f).compositeOver(surface)
    val secContainer = primary.copy(alpha = if (dark) 0.26f else 0.16f).compositeOver(bg)
    return if (dark) {
        darkColorScheme(
            primary = primary, onPrimary = onP,
            primaryContainer = container, onPrimaryContainer = text,
            secondary = secondary, onSecondary = onS,
            secondaryContainer = secContainer, onSecondaryContainer = text,
            background = bg, onBackground = text,
            surface = surface, onSurface = text,
            surfaceVariant = variant, onSurfaceVariant = text.copy(alpha = 0.72f),
            outline = text.copy(alpha = 0.35f), outlineVariant = text.copy(alpha = 0.16f),
            surfaceContainerLowest = bg, surfaceContainerLow = surface, surfaceContainer = surface,
            surfaceContainerHigh = variant, surfaceContainerHighest = variant,
            error = Color(0xFFFF6B6B),
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = onP,
            primaryContainer = container, onPrimaryContainer = text,
            secondary = secondary, onSecondary = onS,
            secondaryContainer = secContainer, onSecondaryContainer = text,
            background = bg, onBackground = text,
            surface = surface, onSurface = text,
            surfaceVariant = variant, onSurfaceVariant = text.copy(alpha = 0.72f),
            outline = text.copy(alpha = 0.35f), outlineVariant = text.copy(alpha = 0.16f),
            surfaceContainerLowest = bg, surfaceContainerLow = surface, surfaceContainer = surface,
            surfaceContainerHigh = variant, surfaceContainerHighest = variant,
            error = Color(0xFFD32F2F),
        )
    }
}

fun typographyFor(f: FontFamily): Typography {
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = f),
        displayMedium = d.displayMedium.copy(fontFamily = f),
        displaySmall = d.displaySmall.copy(fontFamily = f),
        headlineLarge = d.headlineLarge.copy(fontFamily = f),
        headlineMedium = d.headlineMedium.copy(fontFamily = f),
        headlineSmall = d.headlineSmall.copy(fontFamily = f),
        titleLarge = d.titleLarge.copy(fontFamily = f),
        titleMedium = d.titleMedium.copy(fontFamily = f),
        titleSmall = d.titleSmall.copy(fontFamily = f),
        bodyLarge = d.bodyLarge.copy(fontFamily = f),
        bodyMedium = d.bodyMedium.copy(fontFamily = f),
        bodySmall = d.bodySmall.copy(fontFamily = f),
        labelLarge = d.labelLarge.copy(fontFamily = f),
        labelMedium = d.labelMedium.copy(fontFamily = f),
        labelSmall = d.labelSmall.copy(fontFamily = f),
    )
}

@Composable
fun RinPinTheme(theme: RinTheme, font: FontFamily, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = theme.scheme(), typography = typographyFor(font), content = content)
}
