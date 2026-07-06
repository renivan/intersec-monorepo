package com.intersec.androidapp.presentation.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.R
import com.intersec.androidapp.presentation.viewmodel.InitializationViewModel

@Composable
fun InitializationScreen(
    onComplete: () -> Unit,
    viewModel: InitializationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        viewModel.startHealthCheck(activity)
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)), // StealthGray
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Neural Core Icon com animação de rotação
            val infiniteTransition = rememberInfiniteTransition(label = "core_rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Image(
                painter = painterResource(id = R.drawable.ic_neural_core),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "INTERSEC",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
            
            Text(
                "ADVANCED NETWORK INTELLIGENCE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Barra de Progresso Profissional
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                state.status.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 1.sp
            )

            if (state.error != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.startHealthCheck(activity) },
                    modifier = Modifier.padding(top = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("TENTAR NOVAMENTE")
                }
            }
        }

        // Rodapé
        Text(
            "ENGINE VERSION 1.0.4 - STABLE",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            fontSize = 8.sp,
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}
