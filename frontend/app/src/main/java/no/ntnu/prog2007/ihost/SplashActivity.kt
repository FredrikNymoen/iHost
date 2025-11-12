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
        //val waterDrop = findViewById<TextView>(R.id.waterDrop)
        //val dotOnI = findViewById<View>(R.id.dotOnI)

        // Animate background color from light blue to dark blue
        animateBackgroundColor(rootLayout)

        // Animate letters flying in with hardcoded positions
        animateLetter(letterI, -300f, -200f, 1000)
        animateLetter(letterH, 300f, -250f, 1150)
        animateLetter(letterO, -350f, 100f, 1300)
        animateLetter(letterS, 350f, 150f, 1450)
        animateLetter(letterT, 0f, 300f, 1600)

        // Wait for layout to position the letters, then set water drop and dot positions
        letterI.post {
            val location = IntArray(2)
            letterI.getLocationOnScreen(location)

        }

        // Navigate to MainActivity after animation
        rootLayout.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    private fun animateBackgroundColor(view: View) {
        val colorAnimator = ValueAnimator.ofArgb(
            Color.parseColor("#60a5fa"), // light blue
            Color.parseColor("#1e3a8a")  // dark blue
        )
        colorAnimator.duration = 1000
        colorAnimator.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
    }

    private fun animateLetter(letter: TextView, fromX: Float, fromY: Float, delay: Long) {
        // Set fixed initial position
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
        animSet.duration = 1200
        animSet.startDelay = delay
        animSet.start()
    }
}