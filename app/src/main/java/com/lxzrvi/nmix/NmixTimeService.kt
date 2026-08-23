package com.lxzrvi.nmix

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NmixTimeService:Service(){

    companion object{
        const val ACTION_START_TIMER="com.lxzrvi.nmix.START_TIMER"
        const val ACTION_START_STOPWATCH="com.lxzrvi.nmix.START_STOPWATCH"
        const val ACTION_STOP="com.lxzrvi.nmix.STOP_TIME_SERVICE"
        const val ACTION_TIMER_PLUS="com.lxzrvi.nmix.TIMER_PLUS"
        const val ACTION_TIMER_MINUS="com.lxzrvi.nmix.TIMER_MINUS"
        const val ACTION_TIMER_RESET="com.lxzrvi.nmix.TIMER_RESET"
        const val ACTION_STOPWATCH_RESET="com.lxzrvi.nmix.STOPWATCH_RESET"
        const val EXTRA_TIMER_SECONDS="timer_seconds"
        const val EXTRA_STOPWATCH_ELAPSED="stopwatch_elapsed"

        private const val CHANNEL_ACTIVE="nmix_time_active"
        private const val CHANNEL_COMPLETE="nmix_time_complete"
        private const val ACTIVE_ID=4101
        private const val COMPLETE_ID=4102
    }

    private val handler=Handler(Looper.getMainLooper())
    private var mode=""
    private var timerFinishAt=0L
    private var stopwatchBase=0L
    private var player:MediaPlayer?=null
    private var alarmPlayCount=0

    private val updater=object:Runnable{
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

    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        when(intent?.action){
            ACTION_START_TIMER->startTimer(intent.getIntExtra(EXTRA_TIMER_SECONDS,0))
            ACTION_START_STOPWATCH->startStopwatch(intent.getLongExtra(EXTRA_STOPWATCH_ELAPSED,0L))
            ACTION_TIMER_PLUS->if(mode=="timer"){
                timerFinishAt+=5000L
                updateTimer()
            }
            ACTION_TIMER_MINUS->if(mode=="timer"){
                timerFinishAt-=5000L
                if(timerFinishAt<=SystemClock.elapsedRealtime()) completeTimer()
                else updateTimer()
            }
            ACTION_TIMER_RESET->stopTiming()
            ACTION_STOPWATCH_RESET->if(mode=="stopwatch"){
                stopwatchBase=SystemClock.elapsedRealtime()
                updateStopwatch()
            }
            ACTION_STOP->stopTiming()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(seconds:Int){
        val safe=seconds.coerceAtLeast(0)
        if(safe<=0){
            stopTiming()
            return
        }

        stopAlarm()
        handler.removeCallbacks(updater)
        mode="timer"
        timerFinishAt=SystemClock.elapsedRealtime()+safe*1000L
        startForeground(ACTIVE_ID,timerNotification(safe))
        handler.post(updater)
    }

    private fun startStopwatch(existingElapsed:Long){
        stopAlarm()
        handler.removeCallbacks(updater)
        mode="stopwatch"
        val safe=existingElapsed.coerceAtLeast(0L)
        stopwatchBase=SystemClock.elapsedRealtime()-safe
        startForeground(ACTIVE_ID,stopwatchNotification(safe))
        handler.post(updater)
    }

    private fun updateTimer(){
        if(mode!="timer") return
        handler.removeCallbacks(updater)

        val remaining=timerFinishAt-SystemClock.elapsedRealtime()
        if(remaining<=0L){
            completeTimer()
            return
        }

        val seconds=((remaining+999L)/1000L).toInt()
        notifyActive(timerNotification(seconds))
        handler.postDelayed(updater,500L)
    }

    private fun updateStopwatch(){
        if(mode!="stopwatch") return
        handler.removeCallbacks(updater)

        val elapsed=(SystemClock.elapsedRealtime()-stopwatchBase).coerceAtLeast(0L)
        notifyActive(stopwatchNotification(elapsed))
        handler.postDelayed(updater,500L)
    }

    private fun completeTimer(){
        if(mode!="timer") return

        mode=""
        handler.removeCallbacks(updater)
        stopForeground(STOP_FOREGROUND_REMOVE)

        runCatching{
            NotificationManagerCompat.from(this)
                .notify(COMPLETE_ID,completionNotification())
        }

        playAlarmTwice()
    }

    /*
     * res/raw/nimix_alarm.mp3
     *
     * First play -> completion callback -> second play.
     * Service and completion notification are removed
     * only after the second complete playback.
     */
    private fun playAlarmTwice(){
        stopAlarm()
        alarmPlayCount=0

        fun finish(){
            stopAlarm()
            runCatching{
                NotificationManagerCompat.from(this).cancel(COMPLETE_ID)
            }
            stopSelf()
        }

        fun begin(){
            val mp=runCatching{
                MediaPlayer.create(this,R.raw.nimix_alarm)
            }.getOrNull()

            if(mp==null){
                finish()
                return
            }

            player=mp
            alarmPlayCount++

            mp.setOnCompletionListener{completed->
                runCatching{completed.reset()}
                runCatching{completed.release()}
                if(player===completed) player=null

                if(alarmPlayCount<2) begin()
                else finish()
            }

            mp.setOnErrorListener{failed,_,_->
                runCatching{failed.reset()}
                runCatching{failed.release()}
                if(player===failed) player=null
                finish()
                true
            }

            runCatching{mp.start()}
                .onFailure{finish()}
        }

        begin()
    }

    private fun stopAlarm(){
        player?.let{
            runCatching{if(it.isPlaying) it.stop()}
            runCatching{it.reset()}
            runCatching{it.release()}
        }
        player=null
        alarmPlayCount=0
    }

    private fun stopTiming(){
        mode=""
        handler.removeCallbacks(updater)
        stopAlarm()
        stopForeground(STOP_FOREGROUND_REMOVE)

        runCatching{
            NotificationManagerCompat.from(this).cancel(ACTIVE_ID)
            NotificationManagerCompat.from(this).cancel(COMPLETE_ID)
        }

        stopSelf()
    }

    private fun notifyActive(notification:Notification){
        runCatching{
            NotificationManagerCompat.from(this).notify(ACTIVE_ID,notification)
        }
    }

    private fun baseNotification(title:String,text:String)=
        NotificationCompat.Builder(this,CHANNEL_ACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(0xFF111111.toInt())
            .setColorized(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)

    private fun timerNotification(seconds:Int):Notification{
        val value=formatTimer(seconds)

        return baseNotification("NMIX • TIMER","$value  •  Running")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$value\nTimer running\nEVERYTHING WITH NUMBERS"
            ))
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .addAction(
                android.R.drawable.ic_media_rew,
                "−5",
                servicePendingIntent(5201,ACTION_TIMER_MINUS)
            )
            .addAction(
                android.R.drawable.ic_input_add,
                "+5",
                servicePendingIntent(5202,ACTION_TIMER_PLUS)
            )
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Reset",
                servicePendingIntent(5203,ACTION_TIMER_RESET)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                servicePendingIntent(5204,ACTION_STOP)
            )
            .build()
    }

    private fun stopwatchNotification(elapsed:Long):Notification{
        val value=formatStopwatch(elapsed)

        return baseNotification("NMIX • STOPWATCH","$value  •  Running")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$value\nStopwatch running\nEVERYTHING WITH NUMBERS"
            ))
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Reset",
                servicePendingIntent(5301,ACTION_STOPWATCH_RESET)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                servicePendingIntent(5302,ACTION_STOP)
            )
            .build()
    }

    private fun completionNotification()=
        NotificationCompat.Builder(this,CHANNEL_COMPLETE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("NMIX • TIMER COMPLETE")
            .setContentText("Time's up")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Time's up.\nEVERYTHING WITH NUMBERS"
            ))
            .setColor(0xFF111111.toInt())
            .setColorized(false)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

    private fun openAppPendingIntent():PendingIntent{
        val intent=packageManager.getLaunchIntentForPackage(packageName)
            ?:Intent(this,MainActivity::class.java)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        return PendingIntent.getActivity(
            this,5101,intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun servicePendingIntent(request:Int,action:String):PendingIntent{
        val intent=Intent(this,NmixTimeService::class.java).apply{
            this.action=action
        }

        return PendingIntent.getService(
            this,request,intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannels(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O) return

        val manager=getSystemService(NotificationManager::class.java)

        val active=NotificationChannel(
            CHANNEL_ACTIVE,
            "NMIX time tools",
            NotificationManager.IMPORTANCE_LOW
        ).apply{
            description="Live Timer and Stopwatch progress"
            setSound(null,null)
            enableVibration(false)
            setShowBadge(false)
        }

        val complete=NotificationChannel(
            CHANNEL_COMPLETE,
            "NMIX timer alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply{
            description="Timer completion"
            setSound(null,null)
            enableVibration(false)
        }

        manager.createNotificationChannel(active)
        manager.createNotificationChannel(complete)
    }

    private fun formatTimer(total:Int):String{
        val safe=total.coerceAtLeast(0)
        return "%02d:%02d".format(safe/60,safe%60)
    }

    private fun formatStopwatch(elapsed:Long):String{
        val safe=elapsed.coerceAtLeast(0L)
        val seconds=safe/1000L

        return "%02d:%02d.%02d".format(
            seconds/60L,
            seconds%60L,
            (safe%1000L)/10L
        )
    }

    override fun onDestroy(){
        handler.removeCallbacksAndMessages(null)
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent:Intent?):IBinder?=null
}
