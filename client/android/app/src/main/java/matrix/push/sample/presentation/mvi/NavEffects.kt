package matrix.push.sample.presentation.mvi

/**
 * 네비게이션 이펙트 정의.
 */
sealed interface NavEffect : UiEffect {
    data class ToProductDetail(val productId: String) : NavEffect
    data object ToNotifications : NavEffect
    data object ToHome : NavEffect
    data object None : NavEffect
}




