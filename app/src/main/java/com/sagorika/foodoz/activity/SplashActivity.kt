package com.sagorika.foodoz.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.sagorika.foodoz.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        //to show the splash page for 2 sec
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // We use finish() here so the when the user presses back button, he/she is not redirected to this activity
        }, 1000)

    }

}
