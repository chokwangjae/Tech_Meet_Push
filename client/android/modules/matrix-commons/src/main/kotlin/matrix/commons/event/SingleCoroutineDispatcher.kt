@file:OptIn(DelicateCoroutinesApi::class)

package matrix.commons.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlin.coroutines.CoroutineContext

/**
 * @author tarkarn
 * @since 2024. 12. 16.
 *
 * 별도의 CoroutineDispatcher 를 만들때 사용 하기 위한 클래스.
 */
class SingleCoroutineDispatcher(name: String) : CoroutineDispatcher() {
    private val dispatcher = newFixedThreadPoolContext(
        nThreads = 1,
        name = name
    )

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatcher.dispatch(context, block)
    }
}
