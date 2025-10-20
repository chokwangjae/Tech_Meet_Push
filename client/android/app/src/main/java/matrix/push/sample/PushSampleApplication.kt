package matrix.push.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import matrix.push.client.MatrixPushClient
import matrix.push.client.modules.error.MpsException
import matrix.push.client.usecase.OnErrorListener
import matrix.push.client.usecase.OnInitializedListener

class PushSampleApplication : Application() {

    companion object {
        private val TAG = PushSampleApplication::class.simpleName ?: "PushSampleApplication"
    }

    private val _mpsErrorEvents = MutableSharedFlow<MpsException>(replay = 1)
    val mpsErrorEvents = _mpsErrorEvents.asSharedFlow()

    var mpsClient: MatrixPushClient? = null
    lateinit var mpsErrorListener: OnErrorListener

    override fun onCreate() {
        super.onCreate()

        // Matrix Push 에러 리스너.
        mpsErrorListener = object : OnErrorListener {
            override fun onError(mpsException: MpsException) {
                val errorMessage = "${mpsException.details ?: ""} (${mpsException.error.code})"
                Log.e(TAG, "onError received (will be replayed if needed): $errorMessage")

                CoroutineScope(Dispatchers.Main).launch {
                    _mpsErrorEvents.emit(mpsException)
                }
            }
        }

        val onInitializedListener = object : OnInitializedListener {
            override fun onInitialized(client: MatrixPushClient) {
                Log.d(TAG, "onInitialized ::")

                client.login("tarkarn", "tarkarn_name", "tarkarn@inswave.com")

                // PushMode.ALL 또는 PushMode.PRIVATE 인 경우에만 사용.
                // 서버에도 동일한 모드가 있으며 동일하게 서버-클라이언트가 모드가 맞아야 한다.
//                client.connect {
//                    Log.d(TAG, "onInitialized :: connected sse +++ ")
//                }
            }
        }

        // Matrix Push Client 초기화
        // Application 에서 초기화를 해야 앱이 종료 된 상태에서도 푸시를 받을 수 있다.
        mpsClient = MatrixPushClient.builder(this, "http://192.168.152.84:20512/")
            .debugMode(true)
            .onError(mpsErrorListener)
            .onInitialized(onInitializedListener)
            .build()

    }
}