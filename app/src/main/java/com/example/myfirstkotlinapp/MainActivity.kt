package com.example.myfirstkotlinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myfirstkotlinapp.ui.screen.MainScreen
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstKotlinAppTheme {
                MainScreen()
            }
        }
    }
}
