package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundHapticEffect(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playPickup() {
        vibrate(15, 60)
    }

    fun playSnap() {
        vibrate(25, 140)
    }

    fun playLineBlast(combos: Int) {
        val duration = (40 + combos * 15).toLong().coerceAtMost(150)
        val amplitude = (160 + combos * 20).coerceAtMost(255)
        vibrate(duration, amplitude)
    }

    fun playComboImpact() {
        vibrate(80, 255)
    }

    fun playGameOver() {
        vibrate(120, 200)
    }

    private fun vibrate(millis: Long, amplitude: Int) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(millis, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(millis)
                    }
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore if permission restricted
        }
    }
}
