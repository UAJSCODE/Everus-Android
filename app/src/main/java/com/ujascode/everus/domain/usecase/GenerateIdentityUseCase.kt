package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.IdentityRepository
import javax.inject.Inject

class GenerateIdentityUseCase @Inject constructor(
    private val identityRepository: IdentityRepository
) {
    suspend fun execute() = identityRepository.ensureIdentityExists()
}
