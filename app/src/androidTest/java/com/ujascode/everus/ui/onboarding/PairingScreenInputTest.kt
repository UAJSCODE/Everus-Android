package com.ujascode.everus.ui.onboarding

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.pressKey
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.theme.EverusTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class PairingScreenInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun preservesTextOrderCursorDeletionSelectionAndSubmittedValue() {
        val submitted = AtomicReference<String?>(null)
        composeRule.setContent {
            EverusTheme {
                PairingScreen(
                    ownDeviceId = "550e8400-e29b-41d4-a716-446655440001",
                    pairingState = PairingViewModel.UiState.Idle,
                    onStartPairing = submitted::set,
                    onRespondToPairing = { _, _ -> },
                    onCancelPairing = {},
                    onRetryConnection = {}
                )
            }
        }

        composeRule.onNodeWithText("Your device ID").assertIsDisplayed()
        composeRule.onNodeWithText("Partner device ID").assertIsDisplayed()
        val field = composeRule.onNode(hasSetTextAction())
        field.performClick()
        field.performTextInput("123456")
        field.assertTextEquals("123456")

        field.performTextInputSelection(TextRange(3, 3))
        field.performTextInput("X")
        field.assertTextEquals("123X456")

        field.performKeyInput { pressKey(Key.Backspace) }
        field.assertTextEquals("123456")

        val validDeviceId = "550e8400-e29b-41d4-a716-446655440000"
        field.performTextInputSelection(TextRange(0, 6))
        field.performTextInput(validDeviceId)
        field.assertTextEquals(validDeviceId)
        composeRule.onNodeWithText("Send pairing request").performClick()
        composeRule.runOnIdle {
            assertEquals(validDeviceId, submitted.get())
        }
    }

    @Test
    fun invalidTextIsNotRewrittenOrClearedBeforeSubmission() {
        val submitted = AtomicReference<String?>(null)
        composeRule.setContent {
            EverusTheme {
                PairingScreen(
                    ownDeviceId = "550e8400-e29b-41d4-a716-446655440001",
                    pairingState = PairingViewModel.UiState.Idle,
                    onStartPairing = submitted::set,
                    onRespondToPairing = { _, _ -> },
                    onCancelPairing = {},
                    onRetryConnection = {}
                )
            }
        }

        val invalidDeviceId = "not-a-device-id"
        val field = composeRule.onNode(hasSetTextAction())
        field.performClick()
        field.performTextInput(invalidDeviceId)
        field.assertTextEquals(invalidDeviceId)
        composeRule.onNodeWithText("Send pairing request").performClick()
        composeRule.runOnIdle {
            assertEquals(invalidDeviceId, submitted.get())
        }
    }
}
