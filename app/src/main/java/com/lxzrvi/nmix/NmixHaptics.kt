package com.lxzrvi.nmix

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberNmixHapticAction():
    (()->Unit)->Unit{

    val appearance=
        LocalNmixAppearance.current

    val haptic=
        LocalHapticFeedback.current

    return {action->
        if(appearance.hapticsEnabled){
            haptic.performHapticFeedback(
                HapticFeedbackType.TextHandleMove
            )
        }

        action()
    }
}
