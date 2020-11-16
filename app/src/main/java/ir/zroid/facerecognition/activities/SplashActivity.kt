package ir.zroid.facerecognition.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import ir.zroid.facerecognition.MainActivity
import ir.zroid.facerecognition.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        start()
    }

    private fun start() {
        Handler().postDelayed({
            finish()
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit)
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit)
        }, 1500)
    }

}