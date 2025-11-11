package no.ntnu.prog2007.ihost

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import android.widget.TextView

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)

        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
        val letterI = findViewById<TextView>(R.id.letter_i)
        val letterH = findViewById<TextView>(R.id.letter_H)
        val letterO = findViewById<TextView>(R.id.letter_o)
        val letterS = findViewById<TextView>(R.id.letter_s)
        val letterT = findViewById<TextView>(R.id.letter_t)
        val waterDrop = findViewById<TextView>(R.id.waterDrop)
        val dotOnI = findViewById<View>(R.id.dotOnI)

        // Animate background color from light blue to dark blue (faster)
        animateBackgroundColor(rootLayout)

        // Animate letters flying in (faster delays)
        animateLetter(letterI, -300f, -200f, 200)
        animateLetter(letterH, 300f, -250f, 300)
        animateLetter(letterO, -350f, 100f, 400)
        animateLetter(letterS, 350f, 150f, 500)
        animateLetter(letterT, 0f, 300f, 600)

        // Animate water drop falling (faster)
        animateWaterDrop(waterDrop, dotOnI, letterI)

        // Navigate to MainActivity after 2.5 seconds
        rootLayout.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500)
    }

    private fun animateBackgroundColor(view: View) {
        val colorAnimator = ValueAnimator.ofArgb(
            Color.parseColor("#60a5fa"), // light blue
            Color.parseColor("#1e3a8a")  // dark blue
        )
        colorAnimator.duration = 600 // Faster: 600ms instead of 1000ms
        colorAnimator.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
    }

    private fun animateLetter(letter: TextView, fromX: Float, fromY: Float, delay: Long) {
        letter.translationX = fromX
        letter.translationY = fromY
        letter.rotation = (Math.random() * 360 - 180).toFloat()

        val animSet = AnimatorSet()

        val translateXAnim = ObjectAnimator.ofFloat(letter, "translationX", fromX, 0f)
        val translateYAnim = ObjectAnimator.ofFloat(letter, "translationY", fromY, 0f)
        val rotationAnim = ObjectAnimator.ofFloat(letter, "rotation", letter.rotation, 0f)
        val alphaAnim = ObjectAnimator.ofFloat(letter, "alpha", 0f, 1f)

        translateXAnim.interpolator = OvershootInterpolator(1.5f)
        translateYAnim.interpolator = OvershootInterpolator(1.5f)
        rotationAnim.interpolator = OvershootInterpolator(1.5f)

        animSet.playTogether(translateXAnim, translateYAnim, rotationAnim, alphaAnim)
        animSet.duration = 500 // Faster: 500ms instead of 800ms
        animSet.startDelay = delay
        animSet.start()
    }

    private fun animateWaterDrop(waterDrop: TextView, dot: View, letterI: TextView) {
        // Position water drop above the "i"
        letterI.post {
            val location = IntArray(2)
            letterI.getLocationOnScreen(location)

            waterDrop.x = location[0] - 3f
            waterDrop.y = location[1] - 200f
        }

        // Animate water drop falling (faster)
        val dropAnimSet = AnimatorSet()

        val dropY = ObjectAnimator.ofFloat(waterDrop, "translationY", -200f, 78f)
        val dropAlpha = ObjectAnimator.ofFloat(waterDrop, "alpha", 0f, 1f, 0f)
        val dropScale = ObjectAnimator.ofFloat(waterDrop, "scaleX", 1f, 1f, 0f)

        dropAnimSet.playTogether(dropY, dropAlpha, dropScale)
        dropAnimSet.duration = 400 // Faster: 400ms instead of 600ms
        dropAnimSet.startDelay = 1400 // Earlier: 1400ms instead of 2800ms
        dropAnimSet.interpolator = AccelerateInterpolator()
        dropAnimSet.start()

        // Animate dot appearing (faster)
        letterI.post {
            val location = IntArray(2)
            letterI.getLocationOnScreen(location)

            dot.x = location[0] + 5f
            dot.y = location[1] + 38f
        }

        val dotAnimSet = AnimatorSet()
        val dotScaleX = ObjectAnimator.ofFloat(dot, "scaleX", 0f, 1.5f, 1f)
        val dotScaleY = ObjectAnimator.ofFloat(dot, "scaleY", 0f, 1.5f, 1f)
        val dotAlpha = ObjectAnimator.ofFloat(dot, "alpha", 0f, 1f)

        dotAnimSet.playTogether(dotScaleX, dotScaleY, dotAlpha)
        dotAnimSet.duration = 250 // Faster: 250ms instead of 300ms
        dotAnimSet.startDelay = 1800 // Earlier: 1800ms instead of 3400ms
        dotAnimSet.start()
    }
}