package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.IdentityRepository
import javax.inject.Inject

class GetOwnDeviceIdUseCase @Inject constructor(
    private val identityRepository: IdentityRepository
) {
    suspend fun execute(): String = identityRepository.getDeviceId()
}
