package com.lxzrvi.nmix

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class NmixThemeName {
    GREEN,
    BLUE,
    PURPLE,
    ORANGE,
    ROSE
}

@Stable
data class NmixPalette(
    val name: NmixThemeName,
    val accent: Color,
    val accentDark: Color,
    val accentLight: Color,
    val topDark: Color,
    val topEnd: Color
)

private val GreenPalette = NmixPalette(
    name = NmixThemeName.GREEN,
    accent = Color(0xFF319B79),
    accentDark = Color(0xFF216E56),
    accentLight = Color(0xFF69D6B2),
    topDark = Color(0xFF19493A),
    topEnd = Color(0xFF173E33)
)

private val BluePalette = NmixPalette(
    name = NmixThemeName.BLUE,
    accent = Color(0xFF348BB8),
    accentDark = Color(0xFF225E7D),
    accentLight = Color(0xFF75C8EF),
    topDark = Color(0xFF143A50),
    topEnd = Color(0xFF102C3E)
)

private val PurplePalette = NmixPalette(
    name = NmixThemeName.PURPLE,
    accent = Color(0xFF8A62C8),
    accentDark = Color(0xFF60428F),
    accentLight = Color(0xFFC2A1EF),
    topDark = Color(0xFF33224D),
    topEnd = Color(0xFF241B37)
)

private val OrangePalette = NmixPalette(
    name = NmixThemeName.ORANGE,
    accent = Color(0xFFD57D35),
    accentDark = Color(0xFF92531F),
    accentLight = Color(0xFFEFAD73),
    topDark = Color(0xFF563116),
    topEnd = Color(0xFF392313)
)

private val RosePalette = NmixPalette(
    name = NmixThemeName.ROSE,
    accent = Color(0xFFC85878),
    accentDark = Color(0xFF893950),
    accentLight = Color(0xFFEF91AD),
    topDark = Color(0xFF542338),
    topEnd = Color(0xFF351722)
)

fun NmixThemeName.palette(): NmixPalette {
    return when (this) {
        NmixThemeName.GREEN -> GreenPalette
        NmixThemeName.BLUE -> BluePalette
        NmixThemeName.PURPLE -> PurplePalette
        NmixThemeName.ORANGE -> OrangePalette
        NmixThemeName.ROSE -> RosePalette
    }
}

@Stable
class NmixAppearanceState internal constructor(
    initialTheme: NmixThemeName,
    initialDark: Boolean,
    private val context: Context
) {
    var theme by mutableStateOf(initialTheme)
        private set

    var darkMode by mutableStateOf(initialDark)
        private set

    val palette: NmixPalette
        get() = theme.palette()

    fun setTheme(value: NmixThemeName) {
        if (theme == value) return

        theme = value

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(KEY_THEME, value.name)
            .apply()
    }

    fun setDarkMode(value: Boolean) {
        if (darkMode == value) return

        darkMode = value

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean(KEY_DARK, value)
            .apply()
    }

    fun toggleDarkMode() {
        setDarkMode(!darkMode)
    }

    companion object {
        const val PREFS = "nmix_appearance"
        const val KEY_THEME = "theme_v2"
        const val KEY_DARK = "dark_v2"
    }
}

@Composable
fun rememberNmixAppearance(
    context: Context
): NmixAppearanceState {
    return remember(context) {
        val prefs = context.getSharedPreferences(
            NmixAppearanceState.PREFS,
            Context.MODE_PRIVATE
        )

        val savedTheme = runCatching {
            NmixThemeName.valueOf(
                prefs.getString(
                    NmixAppearanceState.KEY_THEME,
                    NmixThemeName.GREEN.name
                ) ?: NmixThemeName.GREEN.name
            )
        }.getOrDefault(NmixThemeName.GREEN)

        val savedDark = prefs.getBoolean(
            NmixAppearanceState.KEY_DARK,
            false
        )

        NmixAppearanceState(
            initialTheme = savedTheme,
            initialDark = savedDark,
            context = context.applicationContext
        )
    }
}

/*
 * Shared visual values.
 *
 * Landing, main screen, result display, labels and buttons
 * should all use these instead of hard-coded colors.
 */
@Stable
data class NmixUiColors(
    val page: Color,
    val glass: Color,
    val glassStrong: Color,
    val accentGlass: Color,
    val accentGlassStrong: Color,
    val text: Color,
    val muted: Color,
    val displayStart: Color,
    val displayEnd: Color
)

fun NmixAppearanceState.uiColors(): NmixUiColors {
    val p = palette

    return if (darkMode) {
        NmixUiColors(
            page = Color(0xFF0D1110),

            glass = Color.White.copy(
                alpha = 0.075f
            ),

            glassStrong = Color.White.copy(
                alpha = 0.11f
            ),

            accentGlass = p.accent.copy(
                alpha = 0.13f
            ),

            accentGlassStrong = p.accent.copy(
                alpha = 0.22f
            ),

            text = Color(0xFFEDF4F1),
            muted = Color(0xFFA4AFAA),

            displayStart = Color(0xFF202725),
            displayEnd = Color(0xFF121816)
        )
    } else {
        NmixUiColors(
            page = Color(0xFFE0E2E1),

            glass = Color.White.copy(
                alpha = 0.60f
            ),

            glassStrong = Color.White.copy(
                alpha = 0.78f
            ),

            accentGlass = p.accent.copy(
                alpha = 0.10f
            ),

            accentGlassStrong = p.accent.copy(
                alpha = 0.17f
            ),

            text = Color(0xFF202321),
            muted = Color(0xFF66706C),

            displayStart = Color(0xFFF0F3F1),
            displayEnd = Color(0xFFD7DFDC)
        )
    }
}

val LocalNmixAppearance =
    staticCompositionLocalOf<NmixAppearanceState> {
        error(
            "NMIX appearance has not been provided."
        )
    }

@Composable
fun ProvideNmixAppearance(
    appearance: NmixAppearanceState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNmixAppearance provides appearance,
        content = content
    )
}
