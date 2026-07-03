package com.regoil.trainingtimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Runs the workout timer as a foreground service so it keeps ticking — and keeps
 * whistling — when the app is in the background or while a phone call is going on.
 *
 * Timing is based on [SystemClock.elapsedRealtime] deltas (not tick counting), so
 * the countdown never drifts even if the UI is paused by the system.
 */
class TimerService : Service() {

    companion object {
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"

        const val EX_TOTAL = "total"
        const val EX_WORK = "work"
        const val EX_REST = "rest"
        const val EX_EX_BREAK = "ex_break"
        const val EX_USE_EX_BREAK = "use_ex_break"

        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIF_ID = 1
        private const val FIVE_MIN = 5 * 60_000L
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null

    private lateinit var config: WorkoutConfig

    // Absolute deadlines on the elapsedRealtime clock.
    private var phaseEnd = 0L
    private var totalEnd = 0L
    private var phase = Phase.WORK
    private var completedSets = 0
    private var setNumber = 1

    private var paused = false
    // Remaining time captured at the moment of pausing.
    private var phaseRemainOnPause = 0L
    private var totalRemainOnPause = 0L

    private var warnedFiveMin = false

    private var wakeLock: PowerManager.WakeLock? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val players = java.util.Collections.synchronizedList(ArrayList<MediaPlayer>())

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> stopEverything()
        }
        return START_STICKY
    }

    private fun handleStart(intent: Intent) {
        config = WorkoutConfig(
            totalMillis = intent.getLongExtra(EX_TOTAL, 40 * 60_000L),
            workMillis = intent.getLongExtra(EX_WORK, 45_000L),
            restMillis = intent.getLongExtra(EX_REST, 75_000L),
            exerciseBreakMillis = intent.getLongExtra(EX_EX_BREAK, 2 * 60_000L),
            useExerciseBreak = intent.getBooleanExtra(EX_USE_EX_BREAK, true)
        )

        // Enter foreground immediately (must happen within a few seconds of start).
        startForeground(NOTIF_ID, buildNotification("Тренировка началась", "Подход 1"))
        acquireWakeLock()

        val now = SystemClock.elapsedRealtime()
        totalEnd = now + config.totalMillis
        phase = Phase.WORK
        completedSets = 0
        setNumber = 1
        phaseEnd = minOf(now + config.workMillis, totalEnd)
        paused = false
        warnedFiveMin = false

        playWhistle() // signal: start working

        loopJob?.cancel()
        loopJob = scope.launch { runLoop() }
    }

    private suspend fun runLoop() {
        while (scope.isActive) {
            if (!paused) {
                val now = SystemClock.elapsedRealtime()

                // Warning signal 5 minutes before the whole workout ends.
                val totalRemain = totalEnd - now
                if (!warnedFiveMin && config.totalMillis > FIVE_MIN && totalRemain in 1..FIVE_MIN) {
                    warnedFiveMin = true
                    playSignal()
                }

                if (now >= phaseEnd) {
                    advancePhase(now)
                    if (phase == Phase.DONE) break
                }

                publish()
                updateNotificationThrottled()
            }
            delay(100)
        }
    }

    /** Moves to the next phase, whistling on the transition. Ends the workout when the budget runs out. */
    private fun advancePhase(now: Long) {
        // Reached (or passed) the total training budget -> finish.
        if (now >= totalEnd) {
            finishWorkout()
            return
        }

        val next: Phase
        when (phase) {
            Phase.WORK -> {
                completedSets++
                next = if (config.useExerciseBreak &&
                    completedSets % config.setsPerExercise == 0
                ) Phase.EXERCISE_BREAK else Phase.REST
            }
            Phase.REST, Phase.EXERCISE_BREAK -> {
                next = Phase.WORK
                setNumber++
            }
            Phase.DONE -> {
                finishWorkout(); return
            }
        }

        val dur = when (next) {
            Phase.WORK -> config.workMillis
            Phase.REST -> config.restMillis
            Phase.EXERCISE_BREAK -> config.exerciseBreakMillis
            Phase.DONE -> 0L
        }
        phase = next
        phaseEnd = minOf(now + dur, totalEnd)
        playWhistle() // signal the change of phase
    }

    private fun finishWorkout() {
        phase = Phase.DONE
        publish()
        vibrateDone()
        val n = buildNotification("Тренировка завершена!", "Отличная работа 💪", ongoing = false)
        (getSystemService(NotificationManager::class.java)).notify(NOTIF_ID, n)
        releaseWakeLock()
        loopJob?.cancel()
        // End of the whole workout: play the train sound fully, then stop the service.
        playCue(R.raw.train_horn) {
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
        // Safety net in case playback completion never fires (max 30 s).
        scope.launch {
            delay(30_000)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun handlePause() {
        if (!TimerState.ui.value.active || paused) return
        val now = SystemClock.elapsedRealtime()
        phaseRemainOnPause = (phaseEnd - now).coerceAtLeast(0)
        totalRemainOnPause = (totalEnd - now).coerceAtLeast(0)
        paused = true
        publish()
        updateNotification()
    }

    private fun handleResume() {
        if (!paused) return
        val now = SystemClock.elapsedRealtime()
        phaseEnd = now + phaseRemainOnPause
        totalEnd = now + totalRemainOnPause
        paused = false
        publish()
        updateNotification()
    }

    private fun stopEverything() {
        TimerState.reset()
        releaseWakeLock()
        loopJob?.cancel()
        releaseAllPlayers()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun publish() {
        val now = SystemClock.elapsedRealtime()
        val phaseRemain = if (paused) phaseRemainOnPause else (phaseEnd - now).coerceAtLeast(0)
        val totalRemain = if (paused) totalRemainOnPause else (totalEnd - now).coerceAtLeast(0)
        TimerState.update(
            TimerUi(
                active = phase != Phase.DONE,
                paused = paused,
                phase = phase,
                phaseRemainingMs = phaseRemain,
                totalRemainingMs = totalRemain,
                setNumber = setNumber
            )
        )
    }

    // ---- Sound ---------------------------------------------------------------

    /**
     * Plays a bundled sound to completion. Uses MediaPlayer (not SoundPool) so long
     * clips — like the ~16 s train horn — are not truncated. [onDone] runs on the main
     * thread when playback finishes. Created on the main looper so callbacks fire.
     */
    private fun playCue(resId: Int, onDone: (() -> Unit)? = null) {
        mainHandler.post {
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // plays even in silent/vibrate & after a call
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val mp = MediaPlayer.create(this, resId, attrs, AudioManager.AUDIO_SESSION_ID_GENERATE)
                if (mp == null) {
                    onDone?.invoke(); return@post
                }
                players.add(mp)
                mp.setOnCompletionListener {
                    players.remove(it); it.release(); onDone?.invoke()
                }
                mp.setOnErrorListener { p, _, _ ->
                    players.remove(p); p.release(); onDone?.invoke(); true
                }
                mp.start()
            } catch (e: Exception) {
                onDone?.invoke()
            }
        }
    }

    private fun playWhistle() = playCue(R.raw.whistle)   // end of a set (подход)
    private fun playSignal() = playCue(R.raw.signal_5min) // 5 minutes before the end

    private fun releaseAllPlayers() {
        synchronized(players) {
            players.forEach { runCatching { it.release() } }
            players.clear()
        }
    }

    private fun vibrateDone() {
        val vib = getSystemService(Vibrator::class.java) ?: return
        val pattern = longArrayOf(0, 400, 200, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION") vib.vibrate(pattern, -1)
        }
    }

    // ---- Wake lock -----------------------------------------------------------

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrainingTimer::wakelock").apply {
            setReferenceCounted(false)
            acquire(6 * 60 * 60 * 1000L) // safety cap: 6h
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
        wakeLock = null
    }

    // ---- Notification --------------------------------------------------------

    private var lastNotifSecond = -1L

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Тренировка", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Отсчёт времени тренировки"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun phaseTitle(): String = when (phase) {
        Phase.WORK -> "Работа — подход $setNumber"
        Phase.REST -> "Отдых между подходами"
        Phase.EXERCISE_BREAK -> "Перерыв между упражнениями"
        Phase.DONE -> "Тренировка завершена"
    }

    private fun buildNotification(title: String, text: String, ongoing: Boolean = true): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(ongoing)

        if (ongoing) {
            val (actionTitle, actionName) =
                if (paused) "Продолжить" to ACTION_RESUME else "Пауза" to ACTION_PAUSE
            builder.addAction(0, actionTitle, servicePendingIntent(actionName, 1))
            builder.addAction(0, "Стоп", servicePendingIntent(ACTION_STOP, 2))
        }
        return builder.build()
    }

    private fun servicePendingIntent(action: String, code: Int): PendingIntent {
        val i = Intent(this, TimerService::class.java).setAction(action)
        return PendingIntent.getService(
            this, code, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotificationThrottled() {
        val sec = TimerState.ui.value.totalRemainingMs / 1000
        if (sec != lastNotifSecond) {
            lastNotifSecond = sec
            updateNotification()
        }
    }

    private fun updateNotification() {
        val remain = TimerState.ui.value.totalRemainingMs
        val text = "Осталось: ${formatTime(remain)}" + if (paused) "  (пауза)" else ""
        val n = buildNotification(phaseTitle(), text)
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, n)
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        releaseWakeLock()
        scope.cancel()
        releaseAllPlayers()
        super.onDestroy()
    }
}
