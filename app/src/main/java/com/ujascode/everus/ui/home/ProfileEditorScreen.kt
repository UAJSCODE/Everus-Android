package com.ujascode.everus.ui.home

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusInputField

@Composable
fun ProfileEditorScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var isSaving by remember { mutableStateOf(false) }
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = "Edit Profile",
            showBackButton = true,
            onBackClicked = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
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

                    val initial = name.text.firstOrNull()?.uppercase() ?: "Y"
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
            }

            Text(
                text = "Your name",
                fontSize = 16.sp,
                color = contentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            EverusInputField(
                value = name,
                onValueChange = { name = it },
                label = "",
                placeholder = "Enter your name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            EverusButton(
                onClick = {
                    if (name.text.isNotBlank()) {
                        isSaving = true
                        isSaving = false
                        onBack()
                    }
                },
                text = if (isSaving) "Saving..." else "Save Changes",
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}
