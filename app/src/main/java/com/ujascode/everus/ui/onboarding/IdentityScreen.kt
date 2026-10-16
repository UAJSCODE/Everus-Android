package com.ujascode.everus.ui.onboarding

import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusInputField

@Composable
fun IdentityScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "CREATE YOUR IDENTITY",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Canvas(
            modifier = Modifier.size(80.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = 40.dp.toPx()

            drawCircle(
                color = contentColor.copy(alpha = 0.1f),
                center = Offset(centerX, centerY),
                radius = radius
            )

            val initial = name.text.firstOrNull()?.uppercase() ?: "?"
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = 24.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(initial, centerX, centerY + 8, paint)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "This identity stays private to EVERUS.",
            fontSize = 14.sp,
            color = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        EverusInputField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "Enter your name",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        EverusButton(
            onClick = onNext,
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = name.text.isNotBlank()
        )
    }
}
