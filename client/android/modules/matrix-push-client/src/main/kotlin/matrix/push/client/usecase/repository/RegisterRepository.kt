package matrix.push.client.usecase.repository

import matrix.push.client.modules.network.data.RegisterResponse
import retrofit2.Response

/**
 *   @author tarkarn
 *   @since 2025. 7. 15.
 */
interface RegisterRepository {

    suspend fun register(): Response<RegisterResponse>

    suspend fun updateTokenOnServerIfNeeded(newToken: String): Response<RegisterResponse>

}