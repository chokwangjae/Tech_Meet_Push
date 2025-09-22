package matrix.push.client.modules.error

/**
 *   @author tarkarn
 *   @since 2025. 7. 30.
 *
 *  모든 에러 코드 Enum이 구현해야 하는 인터페이스.
 *  에러를 식별하기 위한 고유 코드와 기본 영문 메시지를 정의.
 */
interface IErrorCode {
    val code: String
    val defaultMessage: String
}