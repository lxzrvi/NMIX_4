package com.lxzrvi.nmix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
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

        const val EXTRA_STOPWATCH_ELAPSED=
            "stopwatch_elapsed"

        private const val CHANNEL_ACTIVE=
            "nmix_time_active"

        private const val CHANNEL_COMPLETE=
            "nmix_time_complete"

        private const val ACTIVE_ID=4101
        private const val COMPLETE_ID=4102
    }

    private val handler=
        Handler(
            Looper.getMainLooper()
        )

    private var mode=""

    private var timerFinishAt=0L

    /*
     * Base allows Stopwatch notification to resume
     * from the same value shown inside NMIX.
     */
    private var stopwatchBase=0L

    private val updateRunnable=
        object:Runnable{
            override fun run(){
                when(mode){
                    "timer"->
                        updateTimer()

                    "stopwatch"->
                        updateStopwatch()
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
                startTimer(
                    intent.getIntExtra(
                        EXTRA_TIMER_SECONDS,
                        0
                    )
                )
            }

            ACTION_START_STOPWATCH->{
                startStopwatch(
                    intent.getLongExtra(
                        EXTRA_STOPWATCH_ELAPSED,
                        0L
                    )
                )
            }

            ACTION_STOP->{
                stopTiming()
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer(
        seconds:Int
    ){
        val safe=
            seconds.coerceAtLeast(0)

        if(safe<=0){
            stopTiming()
            return
        }

        handler.removeCallbacks(
            updateRunnable
        )

        mode="timer"

        timerFinishAt=
            SystemClock.elapsedRealtime()+
                safe*1000L

        startForeground(
            ACTIVE_ID,
            activeNotification(
                title="NMIX • TIMER",
                value=formatTimer(safe),
                detail="Timer is running"
            )
        )

        handler.post(
            updateRunnable
        )
    }

    private fun startStopwatch(
        existingElapsed:Long
    ){
        handler.removeCallbacks(
            updateRunnable
        )

        mode="stopwatch"

        val safeElapsed=
            existingElapsed.coerceAtLeast(0L)

        stopwatchBase=
            SystemClock.elapsedRealtime()-
                safeElapsed

        startForeground(
            ACTIVE_ID,
            activeNotification(
                title="NMIX • STOPWATCH",
                value=
                    formatStopwatch(
                        safeElapsed
                    ),
                detail="Stopwatch is running"
            )
        )

        handler.post(
            updateRunnable
        )
    }

    private fun updateTimer(){
        if(mode!="timer"){
            return
        }

        val remaining=
            timerFinishAt-
                SystemClock.elapsedRealtime()

        if(remaining<=0L){
            completeTimer()
            return
        }

        val seconds=
            (
                remaining+
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
            (
                SystemClock.elapsedRealtime()-
                    stopwatchBase
            ).coerceAtLeast(0L)

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

    private fun completeTimer(){
        mode=""

        handler.removeCallbacks(
            updateRunnable
        )

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        playCompletionSequence()

        runCatching{
            NotificationManagerCompat
                .from(this)
                .notify(
                    COMPLETE_ID,
                    completionNotification()
                )
        }

        /*
         * Keep service alive just long enough for
         * the four gentle beeps to finish.
         */
        handler.postDelayed(
            {
                stopSelf()
            },
            1750L
        )
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
                    ACTIVE_ID,
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
                android.R.drawable
                    .ic_lock_idle_alarm
            )
            .setContentTitle(title)
            .setContentText(
                "$value  •  $detail"
            )
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(
                        "$value\n$detail\nEVERYTHING WITH NUMBERS"
                    )
            )
            .setColor(
                0xFF319B79.toInt()
            )
            .setColorized(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(
                NotificationCompat.CATEGORY_STOPWATCH
            )
            .setVisibility(
                NotificationCompat
                    .VISIBILITY_PUBLIC
            )
            .setContentIntent(
                openAppPendingIntent()
            )
            .addAction(
                android.R.drawable
                    .ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent()
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .build()

    private fun completionNotification()=
        NotificationCompat.Builder(
            this,
            CHANNEL_COMPLETE
        )
            .setSmallIcon(
                android.R.drawable
                    .ic_lock_idle_alarm
            )
            .setContentTitle(
                "NMIX • TIMER COMPLETE"
            )
            .setContentText(
                "Time's up • EVERYTHING WITH NUMBERS"
            )
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(
                        "Time's up.\nEVERYTHING WITH NUMBERS"
                    )
            )
            .setColor(
                0xFF319B79.toInt()
            )
            .setAutoCancel(true)
            .setCategory(
                NotificationCompat.CATEGORY_ALARM
            )
            .setVisibility(
                NotificationCompat
                    .VISIBILITY_PUBLIC
            )
            .setContentIntent(
                openAppPendingIntent()
            )
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .build()

    private fun openAppPendingIntent():
        PendingIntent{

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

    private fun stopPendingIntent():
        PendingIntent{

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
                "NMIX time tools",
                NotificationManager
                    .IMPORTANCE_LOW
            ).apply{
                description=
                    "Live Timer and Stopwatch progress"

                setSound(
                    null,
                    null
                )

                enableVibration(false)
                setShowBadge(false)
            }

        val complete=
            NotificationChannel(
                CHANNEL_COMPLETE,
                "NMIX timer alerts",
                NotificationManager
                    .IMPORTANCE_HIGH
            ).apply{
                description=
                    "Timer completion"

                /*
                 * Completion audio comes from our
                 * own gentle four-beep sequence.
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
     * Four short, spaced notification tones.
     * Volume intentionally moderate.
     */
    private fun playCompletionSequence(){
        val tone=
            ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                38
            )

        val starts=
            longArrayOf(
                0L,
                390L,
                780L,
                1170L
            )

        starts.forEach{
            delay->

            handler.postDelayed(
                {
                    runCatching{
                        tone.startTone(
                            ToneGenerator
                                .TONE_PROP_BEEP2,
                            145
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
            1550L
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
