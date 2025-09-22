package matrix.push.client.usecase.repository

/**
 *   @author tarkarn
 *   @since 2025. 7. 31.
 */
interface LogoutRepository {

    /**
     * 서버에 등록한 사용자 정보를 초기화 한다.
     * 현재 기기와 연결된 userId, name, email 정보를 초기화 시킨다.
     */
    suspend fun logout()
}