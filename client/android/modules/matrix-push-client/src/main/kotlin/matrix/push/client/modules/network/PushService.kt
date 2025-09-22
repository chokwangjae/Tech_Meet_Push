package matrix.push.client.modules.network

import matrix.push.client.modules.network.data.HealthCheckRequest
import matrix.push.client.modules.network.data.LoginRequest
import matrix.push.client.modules.network.data.LoginResponse
import matrix.push.client.modules.network.data.LogoutRequest
import matrix.push.client.modules.network.data.MessageStatusRequest
import matrix.push.client.modules.network.data.MessageSyncRequest
import matrix.push.client.modules.network.data.RegisterRequest
import matrix.push.client.modules.network.data.RegisterResponse
import matrix.push.client.modules.network.data.UserCampaignGetRequest
import matrix.push.client.modules.network.data.UserCampaignGetResponse
import matrix.push.client.modules.network.data.UserConsentCampaignRequest
import matrix.push.client.modules.network.data.UserConsentRequest
import matrix.push.client.modules.network.data.UserConsentResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PushService {

    /**
     * 사용자 등록 및 푸시 토큰 업데이트
     * 응답값으로 rId (임시 accessToken) 을 받을 수 있다.
     */
    @POST("/mps/v1/public/user/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    /**
     * 사용자 로그인
     * 응답값으로 matrixPushId (accessToken) 을 받을 수 있다.
     */
    @POST("/mps/v1/public/user/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    /**
     * 사용자 로그아웃
     * 성공 시 응답 200 에 body 가 없는 Response 형태. (httpStatus code 만 성공이면 된다.)
     */
    @POST("/mps/v1/public/user/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<String>

    /**
     * SSE 연결
     * 연결 성공 시 응답값으로 matrixPushId (accessToken) 을 받을 수 있다.
     */
    @Streaming
    @GET("/mps/v1/public/sse/connect")
    fun sseConnect(
        @Header("Accept") accept: String = "text/event-stream",
        @Query(value = "rId") rId: String,
        @Query(value = "deviceId") deviceId: String,
        @Query(value = "appIdentifier") appIdentifier: String,
        @Query(value = "platform") platform: String = "AOS"
    ): Call<ResponseBody>

    /**
     * 클라이언트 메시지 수신 상태 업데이트
     */
    @POST("/mps/v1/public/message/status")
    suspend fun messageStatus(@Body body: MessageStatusRequest): Response<String>

    /**
     * SSE 연결 유지를 위한 health check
     */
    @POST("/mps/v1/public/sse/health-check")
    suspend fun healthCheck(@Body body: HealthCheckRequest): Response<String>

    /**
     * 미수신 메시지 동기화
     */
    @POST("/mps/v1/public/messages/sync")
    suspend fun messageSync(@Body body: MessageSyncRequest): Response<String>

    /**
     * 푸시 알림 수신 동의/미동의
     */
    @POST("/mps/v1/public/user/consent")
    suspend fun updateUserConsent(@Body body: UserConsentRequest): Response<UserConsentResponse>

    /**
     * 캠페인 별 푸시 알림 수신 동의/미동의
     */
    @POST("/mps/v1/public/user/consent/campaign")
    suspend fun updateCampaignConsent(@Body body: UserConsentCampaignRequest): Response<UserConsentResponse>

    /**
     * 사용자가 속해있는 캠페인 목록 조회.
     */
    @POST("/mps/v1/public/user/campaign/fetch")
    suspend fun userCampaignList(@Body body: UserCampaignGetRequest): Response<List<UserCampaignGetResponse>>

}