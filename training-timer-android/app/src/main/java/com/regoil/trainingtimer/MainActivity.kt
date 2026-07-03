package com.regoil.trainingtimer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.regoil.trainingtimer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* proceed regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        requestNotifPermissionIfNeeded()
        setupSliders()

        b.checkExerciseBreak.setOnCheckedChangeListener { _, checked ->
            b.sliderExBreak.isEnabled = checked
            b.labelExBreak.isEnabled = checked
            b.valueExBreak.isEnabled = checked
        }

        b.btnStart.setOnClickListener { startWorkout() }
        b.btnHelp.setOnClickListener { showHelp() }
        b.btnPauseResume.setOnClickListener { togglePause() }
        b.btnStop.setOnClickListener { sendAction(TimerService.ACTION_STOP) }

        observeState()
    }

    // ---- Setup UI ------------------------------------------------------------

    private fun setupSliders() {
        b.sliderTotal.addOnChangeListener { _, v, _ -> b.valueTotal.text = "${v.toInt()}" }
        b.sliderWork.addOnChangeListener { _, v, _ -> b.valueWork.text = "${v.toInt()}" }
        b.sliderRest.addOnChangeListener { _, v, _ -> b.valueRest.text = "${v.toInt()}" }
        b.sliderExBreak.addOnChangeListener { _, v, _ -> b.valueExBreak.text = "${v.toInt()}" }

        b.valueTotal.text = "${b.sliderTotal.value.toInt()}"
        b.valueWork.text = "${b.sliderWork.value.toInt()}"
        b.valueRest.text = "${b.sliderRest.value.toInt()}"
        b.valueExBreak.text = "${b.sliderExBreak.value.toInt()}"
    }

    private fun startWorkout() {
        val i = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EX_TOTAL, b.sliderTotal.value.toLong() * 60_000L)
            putExtra(TimerService.EX_WORK, b.sliderWork.value.toLong() * 1_000L)
            putExtra(TimerService.EX_REST, b.sliderRest.value.toLong() * 1_000L)
            putExtra(TimerService.EX_EX_BREAK, b.sliderExBreak.value.toLong() * 60_000L)
            putExtra(TimerService.EX_USE_EX_BREAK, b.checkExerciseBreak.isChecked)
        }
        ContextCompat.startForegroundService(this, i)
    }

    private fun togglePause() {
        val action = if (TimerState.ui.value.paused) TimerService.ACTION_RESUME
        else TimerService.ACTION_PAUSE
        sendAction(action)
    }

    private fun sendAction(action: String) {
        startService(Intent(this, TimerService::class.java).setAction(action))
    }

    // ---- Observe running state ----------------------------------------------

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TimerState.ui.collect { render(it) }
            }
        }
    }

    private fun render(ui: TimerUi) {
        val running = ui.active
        b.setupPanel.visibility = if (running) android.view.View.GONE else android.view.View.VISIBLE
        b.runPanel.visibility = if (running) android.view.View.VISIBLE else android.view.View.GONE
        if (!running) return

        b.phaseLabel.text = when (ui.phase) {
            Phase.WORK -> "РАБОТА"
            Phase.REST -> "ОТДЫХ"
            Phase.EXERCISE_BREAK -> "ПЕРЕРЫВ"
            Phase.DONE -> "ГОТОВО"
        }
        b.phaseTime.text = format(ui.phaseRemainingMs)
        b.totalTime.text = "Осталось всего: ${format(ui.totalRemainingMs)}"
        b.setLabel.text = "Подход ${ui.setNumber}"
        b.btnPauseResume.text = if (ui.paused) "Продолжить" else "Пауза"
    }

    private fun format(ms: Long): String {
        val totalSec = ms / 1000
        return "%02d:%02d".format(totalSec / 60, totalSec % 60)
    }

    // ---- Help & permission ---------------------------------------------------

    private fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle("Справка")
            .setMessage(
                "При помощи ползунков выберите время, которое Вы должны потратить на " +
                    "тренировку, длительность одного подхода и время, которое Вам будет " +
                    "достаточным для отдыха между подходами, а так же, при необходимости, " +
                    "время между упражнениями.\n\n" +
                    "Таймер продолжает работать и подаёт свисток, даже если свернуть " +
                    "приложение, выключить экран или во время звонка. По завершении " +
                    "тренировки прозвучит финальный свисток."
            )
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
