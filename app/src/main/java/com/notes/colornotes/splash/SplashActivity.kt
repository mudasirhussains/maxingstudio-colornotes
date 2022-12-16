package com.notes.colornotes.splash

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.notes.colornotes.MainActivity
import com.notes.colornotes.databinding.ActivityMainBinding
import com.notes.colornotes.databinding.ActivitySplashBinding
import com.notes.colornotes.home.HomeViewModel

class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBar()
        setBinding()
        goToMain()

    }

    private fun setToolBar() {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val decorView: View = window.decorView
            val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.setSystemUiVisibility(uiOptions)
        }
    }

    private fun setBinding() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun goToMain() {
        Handler().postDelayed({
            finish()
            startActivity(Intent(this@SplashActivity, SplashSecondActivity::class.java))
        }, 3000)
    }
}