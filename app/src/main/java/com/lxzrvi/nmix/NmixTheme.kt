package com.lxzrvi.nmix

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

enum class NmixThemeName{
    GREEN,BLUE,PURPLE,ORANGE,ROSE,CYAN
}

enum class NmixFontName{
    INTER,NUNITO,OUTFIT,POPPINS,QUICKSAND
}

enum class NmixAnimationName{
    DRIFT,ORBIT,FLOW,FLOAT,PULSE,CROSS
}

enum class NmixIconStyle{
    ADAPTIVE,
    ROUND
}

enum class NmixHapticStrength{
    SOFT,
    MEDIUM,
    HARD
}

fun NmixHapticStrength.label():String=
    when(this){
        NmixHapticStrength.SOFT->"Soft"
        NmixHapticStrength.MEDIUM->"Medium"
        NmixHapticStrength.HARD->"Hard"
    }

fun NmixAnimationName.label():String=
    when(this){
        NmixAnimationName.DRIFT->"Orb Drift"
        NmixAnimationName.ORBIT->"Orbit"
        NmixAnimationName.FLOW->"Flow"
        NmixAnimationName.FLOAT->"Box Float"
        NmixAnimationName.PULSE->"Pulse"
        NmixAnimationName.CROSS->"Cross"
    }

@Stable
data class NmixPalette(
    val name:NmixThemeName,
    val accent:Color,
    val accentDark:Color,
    val accentLight:Color,
    val topDark:Color,
    val topEnd:Color
)

private val GreenPalette=
    NmixPalette(
        NmixThemeName.GREEN,
        Color(0xFF319B79),
        Color(0xFF216E56),
        Color(0xFF69D6B2),
        Color(0xFF19493A),
        Color(0xFF173E33)
    )

private val BluePalette=
    NmixPalette(
        NmixThemeName.BLUE,
        Color(0xFF348BB8),
        Color(0xFF225E7D),
        Color(0xFF75C8EF),
        Color(0xFF143A50),
        Color(0xFF102C3E)
    )

private val PurplePalette=
    NmixPalette(
        NmixThemeName.PURPLE,
        Color(0xFF8A62C8),
        Color(0xFF60428F),
        Color(0xFFC2A1EF),
        Color(0xFF33224D),
        Color(0xFF241B37)
    )

private val OrangePalette=
    NmixPalette(
        NmixThemeName.ORANGE,
        Color(0xFFD57D35),
        Color(0xFF92531F),
        Color(0xFFEFAD73),
        Color(0xFF563116),
        Color(0xFF392313)
    )

private val RosePalette=
    NmixPalette(
        NmixThemeName.ROSE,
        Color(0xFFC85878),
        Color(0xFF893950),
        Color(0xFFEF91AD),
        Color(0xFF542338),
        Color(0xFF351722)
    )

private val CyanPalette=
    NmixPalette(
        NmixThemeName.CYAN,
        Color(0xFF26A6B5),
        Color(0xFF176B76),
        Color(0xFF69DCE7),
        Color(0xFF123F46),
        Color(0xFF0D3035)
    )

fun NmixThemeName.palette():NmixPalette=
    when(this){
        NmixThemeName.GREEN->GreenPalette
        NmixThemeName.BLUE->BluePalette
        NmixThemeName.PURPLE->PurplePalette
        NmixThemeName.ORANGE->OrangePalette
        NmixThemeName.ROSE->RosePalette
        NmixThemeName.CYAN->CyanPalette
    }

private fun mixColor(
    first:Color,
    second:Color,
    amount:Float
):Color{
    val t=
        amount.coerceIn(
            0f,
            1f
        )

    return Color(
        red=
            first.red+
                (
                    second.red-
                        first.red
                )*t,

        green=
            first.green+
                (
                    second.green-
                        first.green
                )*t,

        blue=
            first.blue+
                (
                    second.blue-
                        first.blue
                )*t,

        alpha=1f
    )
}

private fun customPalette(
    color:Color,
    transparency:Float
):NmixPalette{
    val base=
        color.copy(alpha=1f)

    val opacity=
        (
            1f-
                transparency.coerceIn(
                    0f,
                    .80f
                )
        ).coerceIn(
            .20f,
            1f
        )

    return NmixPalette(
        name=NmixThemeName.GREEN,

        accent=
            base.copy(
                alpha=opacity
            ),

        accentDark=
            mixColor(
                base,
                Color.Black,
                .31f
            ),

        accentLight=
            mixColor(
                base,
                Color.White,
                .32f
            ),

        topDark=
            mixColor(
                base,
                Color(0xFF07100D),
                .61f
            ),

        topEnd=
            mixColor(
                base,
                Color(0xFF0B1511),
                .70f
            )
    )
}

/*
 * ==================================================
 * FONTS
 * ==================================================
 */

val NmixInter=
    FontFamily(
        Font(
            R.font.inter_regular,
            FontWeight.Normal
        ),
        Font(
            R.font.inter_bold,
            FontWeight.Bold
        )
    )

val NmixNunito=
    FontFamily(
        Font(
            R.font.nunito_regular,
            FontWeight.Normal
        ),
        Font(
            R.font.nunito_bold,
            FontWeight.Bold
        )
    )

val NmixOutfit=
    FontFamily(
        Font(
            R.font.outfit_regular,
            FontWeight.Normal
        ),
        Font(
            R.font.outfit_bold,
            FontWeight.Bold
        )
    )

val NmixPoppins=
    FontFamily(
        Font(
            R.font.poppins_regular,
            FontWeight.Normal
        ),
        Font(
            R.font.poppins_bold,
            FontWeight.Bold
        )
    )

val NmixQuicksand=
    FontFamily(
        Font(
            R.font.quicksand_regular,
            FontWeight.Normal
        ),
        Font(
            R.font.quicksand_bold,
            FontWeight.Bold
        )
    )

val NmixLogoFont=
    FontFamily(
        Font(
            R.font.cinzel_decorative_bold,
            FontWeight.Bold
        )
    )

fun NmixFontName.family():FontFamily=
    when(this){
        NmixFontName.INTER->
            NmixInter

        NmixFontName.NUNITO->
            NmixNunito

        NmixFontName.OUTFIT->
            NmixOutfit

        NmixFontName.POPPINS->
            NmixPoppins

        NmixFontName.QUICKSAND->
            NmixQuicksand
    }

fun NmixFontName.label():String=
    when(this){
        NmixFontName.INTER->
            "Inter"

        NmixFontName.NUNITO->
            "Nunito"

        NmixFontName.OUTFIT->
            "Outfit"

        NmixFontName.POPPINS->
            "Poppins"

        NmixFontName.QUICKSAND->
            "Quicksand"
    }

/*
 * ==================================================
 * APPEARANCE STATE
 * ==================================================
 */

@Stable
class NmixAppearanceState internal constructor(
    initialTheme:NmixThemeName,
    initialDark:Boolean,
    initialFont:NmixFontName,
    initialAnimation:NmixAnimationName,
    initialAnimationSpeed:Float,
    initialAnimationQuantity:Int,
    initialCustomColor:Color?,
    initialCustomTransparency:Float,

    initialColorEnabled:Boolean,
    initialAnimationEnabled:Boolean,
    initialFontEnabled:Boolean,
    initialHapticsEnabled:Boolean,
    initialHapticStrength:NmixHapticStrength,

    initialAppIconEnabled:Boolean,
    initialIconFollowTheme:Boolean,
    initialIconTheme:NmixThemeName,
    initialIconStyle:NmixIconStyle,

    private val context:Context
){
    private var themeState by
        mutableStateOf(initialTheme)

    private var darkModeState by
        mutableStateOf(initialDark)

    private var fontState by
        mutableStateOf(initialFont)

    private var animationState by
        mutableStateOf(initialAnimation)

    private var animationSpeedState by
        mutableFloatStateOf(
            initialAnimationSpeed
        )

    private var animationQuantityState by
        mutableIntStateOf(
            initialAnimationQuantity
        )

    private var customColorState by
        mutableStateOf(
            initialCustomColor
        )

    private var customTransparencyState by
        mutableFloatStateOf(
            initialCustomTransparency
        )

    private var colorEnabledState by
        mutableStateOf(
            initialColorEnabled
        )

    private var animationEnabledState by
        mutableStateOf(
            initialAnimationEnabled
        )

    private var fontEnabledState by
        mutableStateOf(
            initialFontEnabled
        )

    private var hapticsEnabledState by
        mutableStateOf(
            initialHapticsEnabled
        )

    private var hapticStrengthState by
        mutableStateOf(
            initialHapticStrength
        )

    private var appIconEnabledState by
        mutableStateOf(
            initialAppIconEnabled
        )

    private var iconFollowThemeState by
        mutableStateOf(
            initialIconFollowTheme
        )

    private var iconThemeState by
        mutableStateOf(
            initialIconTheme
        )

    private var iconStyleState by
        mutableStateOf(
            initialIconStyle
        )

    val theme:NmixThemeName
        get()=themeState

    val darkMode:Boolean
        get()=darkModeState

    val font:NmixFontName
        get()=fontState

    val animation:NmixAnimationName
        get()=animationState

    val animationSpeed:Float
        get()=animationSpeedState

    val animationQuantity:Int
        get()=animationQuantityState

    val customColor:Color?
        get()=customColorState

    val customTransparency:Float
        get()=customTransparencyState

    val usingCustomColor:Boolean
        get()=
            customColorState!=null

    val colorEnabled:Boolean
        get()=colorEnabledState

    val animationEnabled:Boolean
        get()=animationEnabledState

    val fontEnabled:Boolean
        get()=fontEnabledState

    val hapticsEnabled:Boolean
        get()=hapticsEnabledState

    val hapticStrength:NmixHapticStrength
        get()=hapticStrengthState

    val appIconEnabled:Boolean
        get()=appIconEnabledState

    val iconFollowTheme:Boolean
        get()=iconFollowThemeState

    val iconTheme:NmixThemeName
        get()=iconThemeState

    val iconStyle:NmixIconStyle
        get()=iconStyleState

    val fontFamily:FontFamily
        get()=
            fontState.family()

    val palette:NmixPalette
        get()=
            customColorState
                ?.let{
                    customPalette(
                        color=it,
                        transparency=
                            customTransparencyState
                    )
                }
                ?:themeState.palette()

    /*
     * ==================================================
     * COLOR
     * ==================================================
     */

    fun setTheme(
        value:NmixThemeName
    ){
        themeState=value

        customColorState=null
        customTransparencyState=0f

        /*
         * Important:
         * theme selection does not silently mutate
         * launcher icon choice here. Settings asks
         * the user before applying it to the icon.
         */

        prefs()
            .edit()
            .putString(
                KEY_THEME,
                value.name
            )
            .remove(
                KEY_CUSTOM_COLOR
            )
            .remove(
                KEY_CUSTOM_TRANSPARENCY
            )
            .apply()
    }

    fun setCustomColor(
        value:Color
    ){
        setCustomAppearance(
            color=value,
            transparency=
                customTransparencyState
        )
    }

    fun setCustomTransparency(
        value:Float
    ){
        val safe=
            value.coerceIn(
                0f,
                .80f
            )

        customTransparencyState=safe

        prefs()
            .edit()
            .putFloat(
                KEY_CUSTOM_TRANSPARENCY,
                safe
            )
            .apply()
    }

    fun setCustomAppearance(
        color:Color,
        transparency:Float
    ){
        val opaque=
            color.copy(alpha=1f)

        val safeTransparency=
            transparency.coerceIn(
                0f,
                .80f
            )

        customColorState=opaque

        customTransparencyState=
            safeTransparency

        prefs()
            .edit()
            .putLong(
                KEY_CUSTOM_COLOR,
                colorToLong(opaque)
            )
            .putFloat(
                KEY_CUSTOM_TRANSPARENCY,
                safeTransparency
            )
            .apply()
    }

    fun setColorEnabled(
        value:Boolean
    ){
        colorEnabledState=value

        themeState=
            NmixThemeName.GREEN

        customColorState=null
        customTransparencyState=0f

        prefs()
            .edit()
            .putBoolean(
                KEY_COLOR_ENABLED,
                value
            )
            .putString(
                KEY_THEME,
                NmixThemeName.GREEN.name
            )
            .remove(
                KEY_CUSTOM_COLOR
            )
            .remove(
                KEY_CUSTOM_TRANSPARENCY
            )
            .apply()
    }

    /*
     * ==================================================
     * DARK / LIGHT
     * ==================================================
     */

    fun setDarkMode(
        value:Boolean
    ){
        if(
            darkModeState==
            value
        ){
            return
        }

        darkModeState=value

        prefs()
            .edit()
            .putBoolean(
                KEY_DARK,
                value
            )
            .apply()
    }

    fun toggleDarkMode(){
        setDarkMode(
            !darkModeState
        )
    }

    /*
     * ==================================================
     * FONT
     * ==================================================
     */

    fun setFont(
        value:NmixFontName
    ){
        if(fontState==value){
            return
        }

        fontState=value

        prefs()
            .edit()
            .putString(
                KEY_FONT,
                value.name
            )
            .apply()
    }

    fun setFontEnabled(
        value:Boolean
    ){
        fontEnabledState=value

        fontState=
            NmixFontName.INTER

        prefs()
            .edit()
            .putBoolean(
                KEY_FONT_ENABLED,
                value
            )
            .putString(
                KEY_FONT,
                NmixFontName.INTER.name
            )
            .apply()
    }

    /*
     * ==================================================
     * ANIMATION
     * ==================================================
     */

    fun setAnimation(
        value:NmixAnimationName
    ){
        if(
            animationState==
            value
        ){
            return
        }

        animationState=value

        prefs()
            .edit()
            .putString(
                KEY_ANIMATION,
                value.name
            )
            .apply()
    }

    fun setAnimationSpeed(
        value:Float
    ){
        val safe=
            value.coerceIn(
                .45f,
                2.20f
            )

        animationSpeedState=safe

        prefs()
            .edit()
            .putFloat(
                KEY_ANIMATION_SPEED,
                safe
            )
            .apply()
    }

    fun setAnimationQuantity(
        value:Int
    ){
        val safe=
            value.coerceIn(
                1,
                5
            )

        animationQuantityState=safe

        prefs()
            .edit()
            .putInt(
                KEY_ANIMATION_QUANTITY,
                safe
            )
            .apply()
    }

    fun setAnimationEnabled(
        value:Boolean
    ){
        animationEnabledState=value

        /*
         * Visible settings now expose two families:
         * Orb Drift and Box Float.
         */
        animationState=
            NmixAnimationName.DRIFT

        animationSpeedState=1f
        animationQuantityState=2

        prefs()
            .edit()
            .putBoolean(
                KEY_ANIMATION_ENABLED,
                value
            )
            .putString(
                KEY_ANIMATION,
                NmixAnimationName.DRIFT.name
            )
            .putFloat(
                KEY_ANIMATION_SPEED,
                1f
            )
            .putInt(
                KEY_ANIMATION_QUANTITY,
                2
            )
            .apply()
    }

    /*
     * ==================================================
     * HAPTICS
     * ==================================================
     */

    fun setHapticsEnabled(
        value:Boolean
    ){
        hapticsEnabledState=value

        prefs()
            .edit()
            .putBoolean(
                KEY_HAPTICS_ENABLED,
                value
            )
            .apply()
    }

    fun toggleHaptics(){
        setHapticsEnabled(
            !hapticsEnabledState
        )
    }

    fun setHapticStrength(
        value:NmixHapticStrength
    ){
        if(
            hapticStrengthState==
            value
        ){
            return
        }

        hapticStrengthState=value

        prefs()
            .edit()
            .putString(
                KEY_HAPTIC_STRENGTH,
                value.name
            )
            .apply()
    }

    /*
     * ==================================================
     * APP ICON
     * ==================================================
     */

    fun setAppIconEnabled(
        value:Boolean
    ){
        appIconEnabledState=value

        prefs()
            .edit()
            .putBoolean(
                KEY_APP_ICON_ENABLED,
                value
            )
            .apply()
    }

    fun setIconFollowTheme(
        value:Boolean
    ){
        iconFollowThemeState=value

        prefs()
            .edit()
            .putBoolean(
                KEY_ICON_FOLLOW_THEME,
                value
            )
            .apply()
    }

    fun setIconTheme(
        value:NmixThemeName
    ){
        iconThemeState=value

        prefs()
            .edit()
            .putString(
                KEY_ICON_THEME,
                value.name
            )
            .apply()
    }

    fun setIconStyle(
        value:NmixIconStyle
    ){
        iconStyleState=value

        prefs()
            .edit()
            .putString(
                KEY_ICON_STYLE,
                value.name
            )
            .apply()
    }

    private fun prefs()=
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

    companion object{
        const val PREFS=
            "nmix_appearance"

        const val KEY_THEME=
            "theme_v2"

        const val KEY_DARK=
            "dark_v2"

        const val KEY_FONT=
            "font_v1"

        const val KEY_ANIMATION=
            "animation_v1"

        const val KEY_ANIMATION_SPEED=
            "animation_speed_v1"

        const val KEY_ANIMATION_QUANTITY=
            "animation_quantity_v1"

        const val KEY_CUSTOM_COLOR=
            "custom_color_v1"

        const val KEY_CUSTOM_TRANSPARENCY=
            "custom_transparency_v1"

        const val KEY_COLOR_ENABLED=
            "color_enabled_v1"

        const val KEY_ANIMATION_ENABLED=
            "animation_enabled_v1"

        const val KEY_FONT_ENABLED=
            "font_enabled_v1"

        const val KEY_HAPTICS_ENABLED=
            "haptics_enabled_v1"

        const val KEY_HAPTIC_STRENGTH=
            "haptic_strength_v1"

        const val KEY_APP_ICON_ENABLED=
            "app_icon_enabled_v1"

        const val KEY_ICON_FOLLOW_THEME=
            "icon_follow_theme_v1"

        const val KEY_ICON_THEME=
            "icon_theme_v1"

        const val KEY_ICON_STYLE=
            "icon_style_v1"
    }
}

/*
 * ==================================================
 * COLOR STORAGE
 * ==================================================
 */

private fun colorToLong(
    color:Color
):Long{
    val alpha=
        (
            color.alpha*255f
        )
            .toInt()
            .coerceIn(0,255)

    val red=
        (
            color.red*255f
        )
            .toInt()
            .coerceIn(0,255)

    val green=
        (
            color.green*255f
        )
            .toInt()
            .coerceIn(0,255)

    val blue=
        (
            color.blue*255f
        )
            .toInt()
            .coerceIn(0,255)

    return (
        (alpha.toLong() shl 24) or
            (red.toLong() shl 16) or
            (green.toLong() shl 8) or
            blue.toLong()
        )
}

private fun colorFromLong(
    value:Long
):Color{
    val alpha=
        (
            (
                value shr 24
            ) and
                0xFF
        ).toFloat()/255f

    val red=
        (
            (
                value shr 16
            ) and
                0xFF
        ).toFloat()/255f

    val green=
        (
            (
                value shr 8
            ) and
                0xFF
        ).toFloat()/255f

    val blue=
        (
            value and
                0xFF
        ).toFloat()/255f

    return Color(
        red=red,
        green=green,
        blue=blue,
        alpha=alpha
    )
}

/*
 * ==================================================
 * LOAD STATE
 * ==================================================
 */

@Composable
fun rememberNmixAppearance(
    context:Context
):NmixAppearanceState{
    return remember(context){
        val appContext=
            context.applicationContext

        val prefs=
            appContext.getSharedPreferences(
                NmixAppearanceState.PREFS,
                Context.MODE_PRIVATE
            )

        val savedTheme=
            runCatching{
                NmixThemeName.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_THEME,
                        NmixThemeName.GREEN.name
                    )
                        ?:NmixThemeName.GREEN.name
                )
            }.getOrDefault(
                NmixThemeName.GREEN
            )

        val savedFont=
            runCatching{
                NmixFontName.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_FONT,
                        NmixFontName.INTER.name
                    )
                        ?:NmixFontName.INTER.name
                )
            }.getOrDefault(
                NmixFontName.INTER
            )

        val savedAnimation=
            runCatching{
                NmixAnimationName.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_ANIMATION,
                        NmixAnimationName.DRIFT.name
                    )
                        ?:NmixAnimationName.DRIFT.name
                )
            }.getOrDefault(
                NmixAnimationName.DRIFT
            )

        val savedHapticStrength=
            runCatching{
                NmixHapticStrength.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_HAPTIC_STRENGTH,
                        NmixHapticStrength.MEDIUM.name
                    )
                        ?:NmixHapticStrength.MEDIUM.name
                )
            }.getOrDefault(
                NmixHapticStrength.MEDIUM
            )

        val savedIconTheme=
            runCatching{
                NmixThemeName.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_ICON_THEME,
                        savedTheme.name
                    )
                        ?:savedTheme.name
                )
            }.getOrDefault(
                savedTheme
            )

        val savedIconStyle=
            runCatching{
                NmixIconStyle.valueOf(
                    prefs.getString(
                        NmixAppearanceState.KEY_ICON_STYLE,
                        NmixIconStyle.ADAPTIVE.name
                    )
                        ?:NmixIconStyle.ADAPTIVE.name
                )
            }.getOrDefault(
                NmixIconStyle.ADAPTIVE
            )

        val savedCustom=
            if(
                prefs.contains(
                    NmixAppearanceState.KEY_CUSTOM_COLOR
                )
            ){
                colorFromLong(
                    prefs.getLong(
                        NmixAppearanceState.KEY_CUSTOM_COLOR,
                        0L
                    )
                ).copy(alpha=1f)
            }else{
                null
            }

        NmixAppearanceState(
            initialTheme=savedTheme,

            initialDark=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_DARK,
                    false
                ),

            initialFont=savedFont,

            initialAnimation=
                savedAnimation,

            initialAnimationSpeed=
                prefs.getFloat(
                    NmixAppearanceState.KEY_ANIMATION_SPEED,
                    1f
                ).coerceIn(
                    .45f,
                    2.20f
                ),

            initialAnimationQuantity=
                prefs.getInt(
                    NmixAppearanceState.KEY_ANIMATION_QUANTITY,
                    2
                ).coerceIn(
                    1,
                    5
                ),

            initialCustomColor=
                savedCustom,

            initialCustomTransparency=
                prefs.getFloat(
                    NmixAppearanceState.KEY_CUSTOM_TRANSPARENCY,
                    0f
                ).coerceIn(
                    0f,
                    .80f
                ),

            initialColorEnabled=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_COLOR_ENABLED,
                    true
                ),

            initialAnimationEnabled=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_ANIMATION_ENABLED,
                    true
                ),

            initialFontEnabled=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_FONT_ENABLED,
                    true
                ),

            initialHapticsEnabled=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_HAPTICS_ENABLED,
                    false
                ),

            initialHapticStrength=
                savedHapticStrength,

            initialAppIconEnabled=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_APP_ICON_ENABLED,
                    false
                ),

            initialIconFollowTheme=
                prefs.getBoolean(
                    NmixAppearanceState.KEY_ICON_FOLLOW_THEME,
                    true
                ),

            initialIconTheme=
                savedIconTheme,

            initialIconStyle=
                savedIconStyle,

            context=appContext
        )
    }
}

/*
 * ==================================================
 * UI COLORS
 * ==================================================
 */

@Stable
data class NmixUiColors(
    val page:Color,
    val glass:Color,
    val glassStrong:Color,
    val accentGlass:Color,
    val accentGlassStrong:Color,
    val text:Color,
    val muted:Color,
    val displayStart:Color,
    val displayEnd:Color
)

fun NmixAppearanceState.uiColors():
    NmixUiColors{

    val p=palette

    return if(darkMode){
        NmixUiColors(
            page=
                Color(0xFF0D1110),

            glass=
                Color.White.copy(
                    alpha=.075f
                ),

            glassStrong=
                Color.White.copy(
                    alpha=.11f
                ),

            accentGlass=
                p.accent.copy(
                    alpha=.13f
                ),

            accentGlassStrong=
                p.accent.copy(
                    alpha=.22f
                ),

            text=
                Color(0xFFEDF4F1),

            muted=
                Color(0xFFA4AFAA),

            displayStart=
                Color(0xFF202725),

            displayEnd=
                Color(0xFF121816)
        )
    }else{
        NmixUiColors(
            page=
                Color(0xFFF5F7F6),

            glass=
                Color.White.copy(
                    alpha=.72f
                ),

            glassStrong=
                Color.White.copy(
                    alpha=.88f
                ),

            accentGlass=
                p.accent.copy(
                    alpha=.09f
                ),

            accentGlassStrong=
                p.accent.copy(
                    alpha=.15f
                ),

            text=
                Color(0xFF202321),

            muted=
                Color(0xFF66706C),

            displayStart=
                Color(0xFFF6F8F7),

            displayEnd=
                Color(0xFFE6ECE9)
        )
    }
}

val LocalNmixAppearance=
    staticCompositionLocalOf<
        NmixAppearanceState
    >{
        error(
            "NMIX appearance has not been provided."
        )
    }

@Composable
fun ProvideNmixAppearance(
    appearance:NmixAppearanceState,
    content:@Composable ()->Unit
){
    CompositionLocalProvider(
        LocalNmixAppearance provides
            appearance,
        content=content
    )
}
