package com.nbow.texteditor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SplashScreen : AppCompatActivity() {

    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "onCreate :starting ", )
        val intent=Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        Log.e(TAG, "onCreate: finishing", )
        finish()

    }
}