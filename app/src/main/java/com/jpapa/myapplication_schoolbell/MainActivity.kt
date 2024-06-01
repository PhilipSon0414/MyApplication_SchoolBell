package com.jpapa.myapplication_schoolbell

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MainActivity : AppCompatActivity() {

    private lateinit var studyTimeInput: EditText
    private lateinit var breakTimeInput: EditText
    private lateinit var sessionCountInput: EditText
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var timerText: TextView

    private var studyTimeInMillis: Long = 0
    private var breakTimeInMillis: Long = 0
    private var sessionCount: Int = 0
    private var currentSession: Int = 0

    private lateinit var countDownTimer: CountDownTimer
    private var isStudyTime: Boolean = true
    private var isTimerRunning: Boolean = false
    private var timeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        studyTimeInput = findViewById(R.id.study_time_input)
        breakTimeInput = findViewById(R.id.break_time_input)
        sessionCountInput = findViewById(R.id.session_count_input)
        startButton = findViewById(R.id.start_button)
        pauseButton = findViewById(R.id.pause_button)
        timerText = findViewById(R.id.timer_text)

        pauseButton.isEnabled = false

        startButton.setOnClickListener {
            val studyTime = studyTimeInput.text.toString().toIntOrNull()
            val breakTime = breakTimeInput.text.toString().toIntOrNull()
            val sessions = sessionCountInput.text.toString().toIntOrNull()

            if (studyTime != null && breakTime != null && sessions != null) {
                studyTimeInMillis = studyTime * 60 * 1000L
                breakTimeInMillis = breakTime * 60 * 1000L
                sessionCount = sessions
                currentSession = 1
                isStudyTime = true
                startTimer(studyTimeInMillis)
                pauseButton.isEnabled = true
            } else {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        pauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer(timeLeftInMillis)
            }
        }
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                if (isStudyTime) {
                    if (currentSession < sessionCount) {
                        sendNotification("Study session finished, take a break!")
                        isStudyTime = false
                        startTimer(breakTimeInMillis)
                    } else {
                        sendNotification("All sessions completed, well done!")
                        pauseButton.isEnabled = false
                    }
                } else {
                    currentSession++
                    sendNotification("Break time over, start studying!")
                    isStudyTime = true
                    startTimer(studyTimeInMillis)
                }
            }
        }.start()
        isTimerRunning = true
        startButton.isEnabled = false
        pauseButton.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        startButton.isEnabled = true
        pauseButton.text = "Resume"
    }

    private fun updateTimerText() {
        val minutes = timeLeftInMillis / 1000 / 60
        val seconds = timeLeftInMillis / 1000 % 60
        timerText.text = String.format("Timer: %02d:%02d", minutes, seconds)
    }

    private fun sendNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "study_timer_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Study Timer", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Study Timer")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}
