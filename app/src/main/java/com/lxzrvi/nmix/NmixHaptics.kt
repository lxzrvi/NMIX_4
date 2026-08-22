package com.lxzrvi.nmix

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberNmixHapticAction():
    (()->Unit)->Unit{

    val appearance=
        LocalNmixAppearance.current

    val context=
        LocalContext.current

    return {action->
        if(appearance.hapticsEnabled){
            performNmixHaptic(
                context=context,
                strength=
                    appearance.hapticStrength
            )
        }

        action()
    }
}

private fun performNmixHaptic(
    context:Context,
    strength:NmixHapticStrength
){
    runCatching{
        val vibrator=
            if(
                Build.VERSION.SDK_INT>=
                    Build.VERSION_CODES.S
            ){
                context
                    .getSystemService(
                        VibratorManager::class.java
                    )
                    ?.defaultVibrator
            }else{
                @Suppress("DEPRECATION")
                context.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as? Vibrator
            }

        if(
            vibrator==null ||
            !vibrator.hasVibrator()
        ){
            return
        }

        val duration=
            when(strength){
                NmixHapticStrength.SOFT->
                    18L

                NmixHapticStrength.MEDIUM->
                    30L

                NmixHapticStrength.HARD->
                    44L
            }

        val amplitude=
            when(strength){
                NmixHapticStrength.SOFT->
                    70

                NmixHapticStrength.MEDIUM->
                    145

                NmixHapticStrength.HARD->
                    225
            }

        if(
            Build.VERSION.SDK_INT>=
                Build.VERSION_CODES.O
        ){
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    amplitude
                )
            )
        }else{
            @Suppress("DEPRECATION")
            vibrator.vibrate(
                duration
            )
        }
    }
}
