package com.yusuf.kotlinruntimeapp

import android.os.CountDownTimer

class CustomCountdownTimer (private val totalTime: Long, private val interval: Long){
     var remainingTime: Long = totalTime
     var isPaused: Boolean = false
     var timer: CountDownTimer? = null

    fun start() {
        timer = object : CountDownTimer(remainingTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remainingTime = millisUntilFinished
                    // Her aralıkta gerçekleştirilecek işlemler burada yer alır


                }
            }

            override fun onFinish() {
                // Zamanlayıcı tamamlandığında gerçekleştirilecek işlemler burada yer alır
            }
        }.start()
    }
    fun pause() {
        isPaused = true
        timer?.cancel()
    }
    fun resume() {
        isPaused = false
        start()
    }
    fun finish() {
        timer?.cancel()
    }





}