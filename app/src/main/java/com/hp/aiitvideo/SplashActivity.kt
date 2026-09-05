package com.hp.aiitvideo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.hp.aiitvideo.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimation()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            startActivity(
                Intent(
                    this@SplashActivity,
                    LoginActivity::class.java
                )
            )

            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            finish()
        }
    }

    private fun startSplashAnimation() {

        binding.logoContainer.alpha = 0f
        binding.logoContainer.scaleX = 0.75f
        binding.logoContainer.scaleY = 0.75f

        binding.tvLogo.alpha = 0f
        binding.tvTagline.alpha = 0f
        binding.progressContainer.alpha = 0f
        binding.tvPowered.alpha = 0f

        val logoAlpha = ObjectAnimator.ofFloat(
            binding.logoContainer,
            View.ALPHA,
            0f,
            1f
        )

        val logoScaleX = ObjectAnimator.ofFloat(
            binding.logoContainer,
            View.SCALE_X,
            0.75f,
            1f
        )

        val logoScaleY = ObjectAnimator.ofFloat(
            binding.logoContainer,
            View.SCALE_Y,
            0.75f,
            1f
        )

        AnimatorSet().apply {
            playTogether(
                logoAlpha,
                logoScaleX,
                logoScaleY
            )

            duration = 650
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        binding.tvLogo.postDelayed({

            binding.tvLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

        }, 250)

        binding.tvTagline.postDelayed({

            binding.tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

        }, 450)

        binding.progressContainer.postDelayed({

            binding.progressContainer.animate()
                .alpha(1f)
                .setDuration(400)
                .start()

        }, 650)

        binding.tvPowered.postDelayed({

            binding.tvPowered.animate()
                .alpha(1f)
                .setDuration(400)
                .start()

        }, 800)

        startFloatingAnimation(
            binding.glowCircle1,
            18f,
            2400
        )

        startFloatingAnimation(
            binding.glowCircle2,
            -15f,
            2800
        )
    }

    private fun startFloatingAnimation(
        view: View,
        translation: Float,
        duration: Long
    ) {

        view.animate()
            .translationY(translation)
            .translationX(translation / 2)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {

                view.animate()
                    .translationY(0f)
                    .translationX(0f)
                    .setDuration(duration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
            .start()
    }
}