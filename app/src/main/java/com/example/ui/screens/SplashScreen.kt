package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val logoOffset = remember { Animatable(1000f) }
    val sloganOffset = remember { Animatable(-200f) }

    LaunchedEffect(key1 = true) {
        logoOffset.animateTo(0f, animationSpec = tween(1000))
        sloganOffset.animateTo(0f, animationSpec = tween(500))
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for logo
            Text(
                "BookZZZ",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1F3A5F),
                modifier = Modifier.offset(x = logoOffset.value.dp)
            )
            Text(
                "votre séjour en RDC",
                fontSize = 18.sp,
                color = Color(0xFF5DADE2),
                modifier = Modifier.offset(y = sloganOffset.value.dp)
            )
        }
    }
}
