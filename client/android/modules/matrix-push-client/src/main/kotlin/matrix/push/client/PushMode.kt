package matrix.push.client

/**
 *   @author tarkarn
 *   @since 2025. 7. 23.
 *
 *   푸시 모드 구분 용도.
 */
enum class PushMode {
    PRIVATE,
    PUBLIC,     // default 는 PUBLIC 이다.
    ALL         // public + private
}