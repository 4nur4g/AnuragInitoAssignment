package com.example.anuraginitoassignment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.example.anuraginitoassignment.databinding.ActivityScreen2Binding
import com.github.lzyzsd.circleprogress.CircleProgress

class Screen2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // start timer
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.circularTimer.text = (seconds).toString() + 's'
                binding.circularTimer.progress = ((millisUntilFinished/5000))*100.toFloat()
            }

            override fun onFinish() {
                binding.circularTimer.progress = 0f
                binding.circularTimer.text = "0" + 's'
                // move to Screen 3
            }
        }.start()

        // capture image with custom camera using Camera1 library
        // set ISO to 100 and focus to 1
        // save image to gallery
    }
}
