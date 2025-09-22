package matrix.push.client.modules.error

/**
 * @author tarkarn
 * @since 2025.07.31
 *
 * Matrix Push Exception 공통 처리 객체.
 */
class MpsException(
    val error: MpsError,
    val details: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    buildFullMessage(error.defaultMessage, details),
    cause
) {
    companion object {
        /**
         * 기본 메시지와 상세 설명을 조합하여 최종 메시지를 생성하는 헬퍼 함수.
         */
        private fun buildFullMessage(defaultMessage: String, details: String?): String {
            return if (details.isNullOrBlank()) {
                defaultMessage
            } else {
                "$defaultMessage [Details: $details]"
            }
        }
    }
}