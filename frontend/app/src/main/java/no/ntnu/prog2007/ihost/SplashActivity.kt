package no.ntnu.prog2007.ihost

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import kotlin.random.Random

class SplashActivity : Activity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var letterI: TextView
    private lateinit var letterH: TextView
    private lateinit var letterO: TextView
    private lateinit var letterS: TextView
    private lateinit var letterT: TextView
    private lateinit var dotOnI: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make fullscreen - hide status bar and navigation bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Set background color BEFORE setContentView
        window.decorView.setBackgroundColor(Color.parseColor("#EFE5DC"))

        // Set the content view
        setContentView(R.layout.activity_screen)

        initViews()
        startAnimation()

        // Navigate to MainActivity after animation
        rootLayout.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        letterI = findViewById(R.id.letter_i)
        letterH = findViewById(R.id.letter_H)
        letterO = findViewById(R.id.letter_o)
        letterS = findViewById(R.id.letter_s)
        letterT = findViewById(R.id.letter_t)
        dotOnI = findViewById(R.id.dotOnI)
    }

    private fun startAnimation() {
        // Animate background color
        animateBackgroundColor()

        // Animate letters
        animateLetter(letterI, -300f, -200f, 500)
        animateLetter(letterH, 300f, -250f, 600)
        animateLetter(letterO, -350f, 100f, 700)
        animateLetter(letterS, 350f, 150f, 800)
        animateLetter(letterT, 0f, 300f, 900)

        // Animate dot
        animateDot()
    }

    private fun animateBackgroundColor() {
        val colorAnimator = ValueAnimator.ofArgb(
            Color.parseColor("#EFE5DC"), // surface color (WarmCream)
            Color.parseColor("#EFE5DC")  // same surface color
        )
        colorAnimator.duration = 1000
        colorAnimator.addUpdateListener { animator ->
            rootLayout.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
    }

    private fun animateLetter(letter: TextView, fromX: Float, fromY: Float, delay: Long) {
        letter.translationX = fromX
        letter.translationY = fromY
        letter.rotation = Random.nextFloat() * 360 - 180
        letter.alpha = 0f

        val animSet = AnimatorSet()

        val translateXAnim = ObjectAnimator.ofFloat(letter, "translationX", fromX, 0f)
        val translateYAnim = ObjectAnimator.ofFloat(letter, "translationY", fromY, 0f)
        val rotationAnim = ObjectAnimator.ofFloat(letter, "rotation", letter.rotation, 0f)
        val alphaAnim = ObjectAnimator.ofFloat(letter, "alpha", 0f, 1f)

        val interpolator = OvershootInterpolator(1.56f)
        translateXAnim.interpolator = interpolator
        translateYAnim.interpolator = interpolator
        rotationAnim.interpolator = interpolator

        animSet.playTogether(translateXAnim, translateYAnim, rotationAnim, alphaAnim)
        animSet.duration = 600
        animSet.startDelay = delay
        animSet.start()
    }

    private fun animateDot() {
        dotOnI.scaleX = 0f
        dotOnI.scaleY = 0f
        dotOnI.alpha = 0f

        val scaleXAnim = ObjectAnimator.ofFloat(dotOnI, "scaleX", 0f, 1.5f, 1f)
        val scaleYAnim = ObjectAnimator.ofFloat(dotOnI, "scaleY", 0f, 1.5f, 1f)
        val alphaAnim = ObjectAnimator.ofFloat(dotOnI, "alpha", 0f, 1f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleXAnim, scaleYAnim, alphaAnim)
        animatorSet.duration = 200
        animatorSet.startDelay = 1700
        animatorSet.start()
    }
}




