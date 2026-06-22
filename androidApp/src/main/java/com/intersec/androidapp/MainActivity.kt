package com.intersec.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.intersec.androidapp.ui.InterSecApp
import com.intersec.androidapp.ui.theme.InterSecTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InterSecTheme {
                InterSecApp()
            }
        }
    }
}