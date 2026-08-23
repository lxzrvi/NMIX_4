package com.lxzrvi.nmix

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object NmixIconManager{

    private fun aliasName(theme:NmixThemeName):String{
        val color=theme.name
            .lowercase()
            .replaceFirstChar{it.uppercase()}

        return "com.lxzrvi.nmix.Icon${color}Adaptive"
    }

    private val allAliases=
        NmixThemeName.values().map(::aliasName)

    fun apply(
        context:Context,
        enabled:Boolean,
        theme:NmixThemeName,
        style:NmixIconStyle=NmixIconStyle.ADAPTIVE
    ):Boolean=runCatching{
        val app=context.applicationContext
        val manager=app.packageManager
        val target=if(enabled)aliasName(theme) else null

        if(target!=null){
            manager.setComponentEnabledSetting(
                ComponentName(app,target),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        allAliases.forEach{alias->
            if(alias!=target){
                manager.setComponentEnabledSetting(
                    ComponentName(app,alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }

        manager.setComponentEnabledSetting(
            ComponentName(app,"${app.packageName}.MainActivity"),
            if(enabled)
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        true
    }.getOrDefault(false)

    fun applyFromState(
        context:Context,
        appearance:NmixAppearanceState
    ):Boolean=apply(
        context=context,
        enabled=appearance.appIconEnabled,
        theme=appearance.iconTheme
    )
}
