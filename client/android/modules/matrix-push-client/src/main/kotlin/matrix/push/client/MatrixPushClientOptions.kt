package matrix.push.client

/**
 * @author tarkarn
 * @since 2025. 5. 16.
 */
object MatrixPushClientOptions {

    /**
     * push 에서 사용 할 notification channel id.
     * 외부에서 수정 할 일이 생길 수 있어 var 로 생성.
     */
    var channelId = "matrix_push_service"

    /**
     * push 에서 사용 할 notification channel name.
     * push 에서 사용 할 notification icon.
     * 외부에서 수정 할 일이 생길 수 있어 var 로 생성.
     * drawable/matrix_push_icon.png 파일을 교체하면 아이콘 변경이 가능하다.
     */
    var channelName = "matrix push alert"

    /**
     * push 에서 사용 할 notification channel description.
     * 외부에서 수정 할 일이 생길 수 있어 var 로 생성.
     */
    var channelDescription = "matrix push alert from matrix push server."

    /**
     * push 에서 사용 할 notification icon.
     * 외부에서 수정 할 일이 생길 수 있어 var 로 생성.
     * drawable/matrix_push_icon.png 파일을 교체하면 아이콘 변경이 가능하다.
     */
    var defaultSmallIcon = "matrix_push_icon"


    /**
     * Push Controller 연결할 서버 주소.
     */
    var serverUrl = ""

    /**
     * SSE 연결이 끊겼을 때, 재시도 요청 interval
     * milliseconds
     */
    var sseReconnectionIntervalMs: Long = 30000

    /**
     * SSE 최초 연결 시도 시 타임아웃
     * milliseconds
     */
    var sseConnectionTimeoutMs: Long = 10000

    /**
     * SSE 응답 읽기(read) 대기 시간
     * milliseconds
     */
    var sseReadTimeoutMs: Long = 60000

    /**
     * SSE 연결(connect) 시도 타임아웃
     * milliseconds
     */
    var sseConnectTimeoutMs: Long = 60000

    /**
     * 미수신 메시지 동기화 시 한번에 가져올 데이터 수.
     */
    var syncMessagesLimit = 20

}