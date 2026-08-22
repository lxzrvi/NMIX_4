package com.lxzrvi.nmix

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object NmixIconManager{

    /*
     * Alias names that will be declared in
     * AndroidManifest.xml in the next step.
     */
    private fun aliasName(
        theme:NmixThemeName,
        style:NmixIconStyle
    ):String{
        val color=
            theme.name
                .lowercase()
                .replaceFirstChar{
                    it.uppercase()
                }

        val suffix=
            when(style){
                NmixIconStyle.ADAPTIVE->
                    "Adaptive"

                NmixIconStyle.ROUND->
                    "Round"
            }

        return "com.lxzrvi.nmix.Icon${color}${suffix}"
    }

    private val allAliases=
        NmixThemeName.values()
            .flatMap{theme->
                NmixIconStyle.values()
                    .map{style->
                        aliasName(
                            theme,
                            style
                        )
                    }
            }

    fun apply(
        context:Context,
        enabled:Boolean,
        theme:NmixThemeName,
        style:NmixIconStyle
    ):Boolean{
        return runCatching{
            val appContext=
                context.applicationContext

            val manager=
                appContext.packageManager

            val target=
                if(enabled)
                    aliasName(
                        theme,
                        style
                    )
                else
                    null

            /*
             * First enable target alias so there is
             * always a launcher entry during swap.
             */
            if(target!=null){
                manager.setComponentEnabledSetting(
                    ComponentName(
                        appContext,
                        target
                    ),
                    PackageManager
                        .COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            allAliases.forEach{
                alias->

                if(alias!=target){
                    manager.setComponentEnabledSetting(
                        ComponentName(
                            appContext,
                            alias
                        ),
                        PackageManager
                            .COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            /*
             * Original MainActivity launcher entry
             * remains the fallback when custom icon
             * switching is disabled.
             */
            manager.setComponentEnabledSetting(
                ComponentName(
                    appContext,
                    "${
                        appContext.packageName
                    }.MainActivity"
                ),
                if(enabled)
                    PackageManager
                        .COMPONENT_ENABLED_STATE_DISABLED
                else
                    PackageManager
                        .COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            true
        }.getOrDefault(false)
    }

    fun applyFromState(
        context:Context,
        appearance:NmixAppearanceState
    ):Boolean{
        val iconTheme=
            if(
                appearance.iconFollowTheme &&
                !appearance.usingCustomColor
            ){
                appearance.theme
            }else{
                appearance.iconTheme
            }

        return apply(
            context=context,
            enabled=
                appearance.appIconEnabled,
            theme=iconTheme,
            style=
                appearance.iconStyle
        )
    }
}
