package com.lxzrvi.nmix

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
                            Vibrator::class.java
                        )
                    }

                if(
                    vibrator!=null &&
                    vibrator.hasVibrator()
                ){
                    if(
                        Build.VERSION.SDK_INT>=
                        Build.VERSION_CODES.O
                    ){
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                32L,
                                145
                            )
                        )
                    }else{
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(32L)
                    }
                }
            }
        }

        action()
    }
}
