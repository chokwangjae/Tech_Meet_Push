package matrix.push.client.modules.error

/**
 * @author tarkarn
 * @since 2025.07.31
 *
 * 공통 에러 처리 코드 정의.
 */
enum class MpsError(
    override val code: String,
    override val defaultMessage: String
) : IErrorCode {
    UNKNOWN("MPS_001", "An unknown error has occurred."),
    INITIALIZATION_FAILED("MPS_002", "SDK initialization failed."),
    INVALID_SERVER_URL("MPS_003", "The server URL is invalid."),
    PERMISSION_DENIED("MPS_004", "Notification permission has been denied."),
    SSE_CONNECTION_FAILED("MPS_005", "Failed to connect to the real-time server (SSE)."),
    LOGIN_FAILED("MPS_006", "Login failed."),
    NETWORK_ERROR("MPS_007", "A network connection error has occurred."),
    SYNC_MESSAGES_FAILED("MPS_008", "An error occurred while syncing messages."),
    MARK_AS_CONFIRMED_FAILED("MPS_009", "An error occurred while changing message status."),
    GET_MESSAGES_FAILED("MPS_010", "An error occurred while retrieving messages."),
    CAMPAIGN_INFO_FAILED("MPS_011", "An error occurred while processing campaign information."),
    DELETE_MESSAGE_FAILED("MPS_012", "An error occurred while deleting the message."),
    RETRY_MESSAGE_CALLBACK_FAILED("MPS_013", "An error occurred while sending the message status."),
    LOGOUT_FAILED("MPS_014", "Logout failed."),
    REGISTER_FAILED("MPS_015", "fmc token register failed."),
    INVALID_PUSH_MODE("MPS_016", "Push mode is not configured.");

}