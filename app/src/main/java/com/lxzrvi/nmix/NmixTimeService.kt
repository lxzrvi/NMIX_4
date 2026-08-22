package com.lxzrvi.nmix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NmixTimeService : Service(){

    companion object{
        const val ACTION_START_TIMER=
            "com.lxzrvi.nmix.START_TIMER"

        const val ACTION_START_STOPWATCH=
            "com.lxzrvi.nmix.START_STOPWATCH"

        const val ACTION_STOP=
            "com.lxzrvi.nmix.STOP_TIME_SERVICE"

        const val EXTRA_TIMER_SECONDS=
            "timer_seconds"

        private const val CHANNEL_ACTIVE=
            "nmix_time_active"

        private const val CHANNEL_COMPLETE=
            "nmix_time_complete"

        private const val NOTIFICATION_ACTIVE=4101
        private const val NOTIFICATION_COMPLETE=4102
    }

    private val handler=
        Handler(Looper.getMainLooper())

    private var mode=""

    private var timerFinishAt=0L
    private var stopwatchStartedAt=0L

    private val updateRunnable=
        object:Runnable{
            override fun run(){
                when(mode){
                    "timer"->updateTimer()
                    "stopwatch"->updateStopwatch()
                }
            }
        }

    override fun onCreate(){
        super.onCreate()
        createChannels()
    }

    override fun onStartCommand(
        intent:Intent?,
        flags:Int,
        startId:Int
    ):Int{
        when(intent?.action){
            ACTION_START_TIMER->{
                val seconds=
                    intent.getIntExtra(
                        EXTRA_TIMER_SECONDS,
                        0
                    ).coerceAtLeast(0)

                if(seconds<=0){
                    stopSelf()
                    return START_NOT_STICKY
                }

                mode="timer"

                timerFinishAt=
                    SystemClock.elapsedRealtime()+
                        seconds*1000L

                startForeground(
                    NOTIFICATION_ACTIVE,
                    activeNotification(
                        title="NMIX • TIMER",
                        value=formatTimer(seconds),
                        detail="Timer is running"
                    )
                )

                handler.removeCallbacks(
                    updateRunnable
                )

                handler.post(
                    updateRunnable
                )
            }

            ACTION_START_STOPWATCH->{
                mode="stopwatch"

                stopwatchStartedAt=
                    SystemClock.elapsedRealtime()

                startForeground(
                    NOTIFICATION_ACTIVE,
                    activeNotification(
                        title="NMIX • STOPWATCH",
                        value="00:00.00",
                        detail="Stopwatch is running"
                    )
                )

                handler.removeCallbacks(
                    updateRunnable
                )

                handler.post(
                    updateRunnable
                )
            }

            ACTION_STOP->{
                stopTiming()
            }
        }

        return START_NOT_STICKY
    }

    private fun updateTimer(){
        if(mode!="timer"){
            return
        }

        val remainingMs=
            timerFinishAt-
                SystemClock.elapsedRealtime()

        if(remainingMs<=0L){
            timerFinished()
            return
        }

        val seconds=
            (
                remainingMs+
                    999L
            )/1000L

        updateActiveNotification(
            title="NMIX • TIMER",
            value=
                formatTimer(
                    seconds.toInt()
                ),
            detail="Timer is running"
        )

        handler.postDelayed(
            updateRunnable,
            500L
        )
    }

    private fun updateStopwatch(){
        if(mode!="stopwatch"){
            return
        }

        val elapsed=
            SystemClock.elapsedRealtime()-
                stopwatchStartedAt

        updateActiveNotification(
            title="NMIX • STOPWATCH",
            value=
                formatStopwatch(
                    elapsed
                ),
            detail="Stopwatch is running"
        )

        handler.postDelayed(
            updateRunnable,
            500L
        )
    }

    private fun timerFinished(){
        mode=""

        handler.removeCallbacks(
            updateRunnable
        )

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        playCompletionTone()

        runCatching{
            NotificationManagerCompat
                .from(this)
                .notify(
                    NOTIFICATION_COMPLETE,
                    completeNotification()
                )
        }

        stopSelf()
    }

    private fun stopTiming(){
        mode=""

        handler.removeCallbacks(
            updateRunnable
        )

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        stopSelf()
    }

    private fun updateActiveNotification(
        title:String,
        value:String,
        detail:String
    ){
        runCatching{
            NotificationManagerCompat
                .from(this)
                .notify(
                    NOTIFICATION_ACTIVE,
                    activeNotification(
                        title=title,
                        value=value,
                        detail=detail
                    )
                )
        }
    }

    private fun activeNotification(
        title:String,
        value:String,
        detail:String
    )=
        NotificationCompat.Builder(
            this,
            CHANNEL_ACTIVE
        )
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_alarm
            )
            .setContentTitle(title)
            .setContentText(
                "$value  •  $detail"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "$value\n$detail\nEVERYTHING WITH NUMBERS"
                    )
            )
            .setColor(
                0xFF319B79.toInt()
            )
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(
                openAppPendingIntent()
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent()
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .build()

    private fun completeNotification()=
        NotificationCompat.Builder(
            this,
            CHANNEL_COMPLETE
        )
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_alarm
            )
            .setContentTitle(
                "NMIX • TIMER COMPLETE"
            )
            .setContentText(
                "Time's up • EVERYTHING WITH NUMBERS"
            )
            .setColor(
                0xFF319B79.toInt()
            )
            .setAutoCancel(true)
            .setContentIntent(
                openAppPendingIntent()
            )
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .build()

    private fun openAppPendingIntent():PendingIntent{
        val intent=
            packageManager
                .getLaunchIntentForPackage(
                    packageName
                )
                ?:Intent(
                    this,
                    MainActivity::class.java
                )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        return PendingIntent.getActivity(
            this,
            5101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopPendingIntent():PendingIntent{
        val intent=
            Intent(
                this,
                NmixTimeService::class.java
            ).apply{
                action=ACTION_STOP
            }

        return PendingIntent.getService(
            this,
            5102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannels(){
        if(
            Build.VERSION.SDK_INT<
                Build.VERSION_CODES.O
        ){
            return
        }

        val manager=
            getSystemService(
                NotificationManager::class.java
            )

        val active=
            NotificationChannel(
                CHANNEL_ACTIVE,
                "NMIX active time tools",
                NotificationManager.IMPORTANCE_LOW
            ).apply{
                description=
                    "Timer and stopwatch progress"

                setSound(
                    null,
                    null
                )

                enableVibration(false)
            }

        val complete=
            NotificationChannel(
                CHANNEL_COMPLETE,
                "NMIX timer alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply{
                description=
                    "Timer completion alerts"

                /*
                 * NMIX plays its own four-note
                 * completion sequence.
                 */
                setSound(
                    null,
                    null
                )

                enableVibration(false)
            }

        manager.createNotificationChannel(
            active
        )

        manager.createNotificationChannel(
            complete
        )
    }

    /*
     * Four gentle short sine-like system tones.
     *
     * No external audio file is required, and the
     * sequence stays short/non-aggressive.
     */
    private fun playCompletionTone(){
        val attributes=
            AudioAttributes.Builder()
                .setUsage(
                    AudioAttributes.USAGE_NOTIFICATION_EVENT
                )
                .setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                .build()

        val soundPool=
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                    attributes
                )
                .build()

        /*
         * SoundPool needs a resource to load,
         * so use ToneGenerator instead for a
         * deterministic native tone sequence.
         */
        soundPool.release()

        val tone=
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                42
            )

        val gaps=
            longArrayOf(
                0L,
                360L,
                720L,
                1080L
            )

        gaps.forEachIndexed{
            index,
            delay->

            handler.postDelayed(
                {
                    if(index<4){
                        tone.startTone(
                            android.media.ToneGenerator.TONE_PROP_BEEP2,
                            150
                        )
                    }
                },
                delay
            )
        }

        handler.postDelayed(
            {
                runCatching{
                    tone.release()
                }
            },
            1450L
        )
    }

    private fun formatTimer(
        total:Int
    ):String{
        val safe=
            total.coerceAtLeast(0)

        return "%02d:%02d".format(
            safe/60,
            safe%60
        )
    }

    private fun formatStopwatch(
        elapsed:Long
    ):String{
        val safe=
            elapsed.coerceAtLeast(0L)

        val seconds=
            safe/1000L

        return "%02d:%02d.%02d".format(
            seconds/60L,
            seconds%60L,
            (safe%1000L)/10L
        )
    }

    override fun onDestroy(){
        handler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }

    override fun onBind(
        intent:Intent?
    ):IBinder?=null
}
