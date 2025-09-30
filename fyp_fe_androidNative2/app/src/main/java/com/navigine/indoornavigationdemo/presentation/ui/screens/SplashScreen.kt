package com.navigine.indoornavigationdemo.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // This effect runs once when the screen appears
    LaunchedEffect(Unit) {
        delay(2000) // Wait for 2000 milliseconds (2 seconds)
        onTimeout() // Call the function to navigate away
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Or any color you like
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}