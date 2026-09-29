package com.ujascode.everus.domain.usecase

import javax.inject.Inject

/**
 * Use case to verify the pairing relationship after out-of-band verification.
 */
class VerifyPairingUseCase @Inject constructor() {
    fun execute(): Nothing =
        throw UnsupportedOperationException("Out-of-band pairing verification is not implemented")
}
