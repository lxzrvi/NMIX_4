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

fun NmixAnimationName.label():String=when(this){
    NmixAnimationName.DRIFT->"Drift"
    NmixAnimationName.ORBIT->"Orbit"
    NmixAnimationName.FLOW->"Flow"
    NmixAnimationName.FLOAT->"Float"
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

private val GreenPalette=NmixPalette(
    NmixThemeName.GREEN,
    Color(0xFF319B79),
    Color(0xFF216E56),
    Color(0xFF69D6B2),
    Color(0xFF19493A),
    Color(0xFF173E33)
)

private val BluePalette=NmixPalette(
    NmixThemeName.BLUE,
    Color(0xFF348BB8),
    Color(0xFF225E7D),
    Color(0xFF75C8EF),
    Color(0xFF143A50),
    Color(0xFF102C3E)
)

private val PurplePalette=NmixPalette(
    NmixThemeName.PURPLE,
    Color(0xFF8A62C8),
    Color(0xFF60428F),
    Color(0xFFC2A1EF),
    Color(0xFF33224D),
    Color(0xFF241B37)
)

private val OrangePalette=NmixPalette(
    NmixThemeName.ORANGE,
    Color(0xFFD57D35),
    Color(0xFF92531F),
    Color(0xFFEFAD73),
    Color(0xFF563116),
    Color(0xFF392313)
)

private val RosePalette=NmixPalette(
    NmixThemeName.ROSE,
    Color(0xFFC85878),
    Color(0xFF893950),
    Color(0xFFEF91AD),
    Color(0xFF542338),
    Color(0xFF351722)
)

private val CyanPalette=NmixPalette(
    NmixThemeName.CYAN,
    Color(0xFF26A6B5),
    Color(0xFF176B76),
    Color(0xFF69DCE7),
    Color(0xFF123F46),
    Color(0xFF0D3035)
)

fun NmixThemeName.palette():NmixPalette=when(this){
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
    color:Color
):NmixPalette{
    return NmixPalette(
        name=NmixThemeName.GREEN,

        accent=
            color.copy(
                alpha=1f
            ),

        accentDark=
            mixColor(
                color,
                Color.Black,
                .31f
            ),

        accentLight=
            mixColor(
                color,
                Color.White,
                .32f
            ),

        topDark=
            mixColor(
                color,
                Color(0xFF07100D),
                .61f
            ),

        topEnd=
            mixColor(
                color,
                Color(0xFF0B1511),
                .70f
            )
    )
}

val NmixInter=FontFamily(
    Font(
        R.font.inter_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.inter_bold,
        FontWeight.Bold
    )
)

val NmixNunito=FontFamily(
    Font(
        R.font.nunito_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.nunito_bold,
        FontWeight.Bold
    )
)

val NmixOutfit=FontFamily(
    Font(
        R.font.outfit_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.outfit_bold,
        FontWeight.Bold
    )
)

val NmixPoppins=FontFamily(
    Font(
        R.font.poppins_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.poppins_bold,
        FontWeight.Bold
    )
)

val NmixQuicksand=FontFamily(
    Font(
        R.font.quicksand_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.quicksand_bold,
        FontWeight.Bold
    )
)

val NmixLogoFont=FontFamily(
    Font(
        R.font.cinzel_decorative_bold,
        FontWeight.Bold
    )
)

fun NmixFontName.family():FontFamily=when(this){
    NmixFontName.INTER->NmixInter
    NmixFontName.NUNITO->NmixNunito
    NmixFontName.OUTFIT->NmixOutfit
    NmixFontName.POPPINS->NmixPoppins
    NmixFontName.QUICKSAND->NmixQuicksand
}

fun NmixFontName.label():String=when(this){
    NmixFontName.INTER->"Inter"
    NmixFontName.NUNITO->"Nunito"
    NmixFontName.OUTFIT->"Outfit"
    NmixFontName.POPPINS->"Poppins"
    NmixFontName.QUICKSAND->"Quicksand"
}

@Stable
class NmixAppearanceState internal constructor(
    initialTheme:NmixThemeName,
    initialDark:Boolean,
    initialFont:NmixFontName,
    initialAnimation:NmixAnimationName,
    initialAnimationSpeed:Float,
    initialAnimationQuantity:Int,
    initialCustomColor:Color?,
    private val context:Context
){
    private var themeState by
        mutableStateOf(
            initialTheme
        )

    private var darkModeState by
        mutableStateOf(
            initialDark
        )

    private var fontState by
        mutableStateOf(
            initialFont
        )

    private var animationState by
        mutableStateOf(
            initialAnimation
        )

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

    val usingCustomColor:Boolean
        get()=
            customColorState!=null

    val fontFamily:FontFamily
        get()=
            fontState.family()

    val palette:NmixPalette
        get()=
            customColorState
                ?.let{
                    customPalette(it)
                }
                ?:themeState.palette()

    fun setTheme(
        value:NmixThemeName
    ){
        themeState=value
        customColorState=null

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_THEME,
                value.name
            )
            .remove(
                KEY_CUSTOM_COLOR
            )
            .apply()
    }

    fun setCustomColor(
        value:Color
    ){
        val opaque=
            value.copy(
                alpha=1f
            )

        customColorState=
            opaque

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putLong(
                KEY_CUSTOM_COLOR,
                colorToLong(
                    opaque
                )
            )
            .apply()
    }

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

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
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

    fun setFont(
        value:NmixFontName
    ){
        if(fontState==value){
            return
        }

        fontState=value

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_FONT,
                value.name
            )
            .apply()
    }

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

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
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

        animationSpeedState=
            safe

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
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

        animationQuantityState=
            safe

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putInt(
                KEY_ANIMATION_QUANTITY,
                safe
            )
            .apply()
    }

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
    }
}

private fun colorToLong(
    color:Color
):Long{
    val a=
        (
            color.alpha*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    val r=
        (
            color.red*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    val g=
        (
            color.green*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    val b=
        (
            color.blue*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    return (
        (a.toLong() shl 24) or
        (r.toLong() shl 16) or
        (g.toLong() shl 8) or
        b.toLong()
    )
}

private fun colorFromLong(
    value:Long
):Color{
    val a=
        (
            (
                value shr 24
            ) and
            0xFF
        ).toFloat()/255f

    val r=
        (
            (
                value shr 16
            ) and
            0xFF
        ).toFloat()/255f

    val g=
        (
            (
                value shr 8
            ) and
            0xFF
        ).toFloat()/255f

    val b=
        (
            value and
            0xFF
        ).toFloat()/255f

    return Color(
        red=r,
        green=g,
        blue=b,
        alpha=a
    )
}

@Composable
fun rememberNmixAppearance(
    context:Context
):NmixAppearanceState{
    return remember(context){
        val appContext=
            context.applicationContext

        val prefs=
            appContext
                .getSharedPreferences(
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

        val savedSpeed=
            prefs.getFloat(
                NmixAppearanceState.KEY_ANIMATION_SPEED,
                1f
            ).coerceIn(
                .45f,
                2.20f
            )

        val savedQuantity=
            prefs.getInt(
                NmixAppearanceState.KEY_ANIMATION_QUANTITY,
                2
            ).coerceIn(
                1,
                5
            )

        val savedDark=
            prefs.getBoolean(
                NmixAppearanceState.KEY_DARK,
                false
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
                )
            }else{
                null
            }

        NmixAppearanceState(
            initialTheme=savedTheme,
            initialDark=savedDark,
            initialFont=savedFont,
            initialAnimation=savedAnimation,
            initialAnimationSpeed=savedSpeed,
            initialAnimationQuantity=savedQuantity,
            initialCustomColor=savedCustom,
            context=appContext
        )
    }
}

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

fun NmixAppearanceState.uiColors():NmixUiColors{
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
                Color(0xFFE0E2E1),

            glass=
                Color.White.copy(
                    alpha=.60f
                ),

            glassStrong=
                Color.White.copy(
                    alpha=.78f
                ),

            accentGlass=
                p.accent.copy(
                    alpha=.10f
                ),

            accentGlassStrong=
                p.accent.copy(
                    alpha=.17f
                ),

            text=
                Color(0xFF202321),

            muted=
                Color(0xFF66706C),

            displayStart=
                Color(0xFFF0F3F1),

            displayEnd=
                Color(0xFFD7DFDC)
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
