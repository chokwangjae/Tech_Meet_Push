package matrix.push.sample.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import matrix.push.sample.PushSampleApplication
import matrix.push.sample.presentation.screen.MainScreen
import matrix.push.sample.presentation.ui.theme.PushClientSampleTheme

class MainActivity : ComponentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    val app: PushSampleApplication by lazy { application as PushSampleApplication }

    // ViewModel 인스턴스 생성
    private val viewModel: MainViewModel by viewModels()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        registerPermissionListener()
        requestNotificationPermission()

        setContent {
            val darkTheme = isSystemInDarkTheme()
            SystemAppearance(darkTheme = darkTheme)

            PushClientSampleTheme(darkTheme = darkTheme) {
                MainScreen(viewModel = viewModel)
            }
        }

        lifecycleScope.launch {
            app.mpsErrorEvents.collectLatest { mpsException ->
                val errorMessage = "${mpsException.details ?: ""} (${mpsException.error.code})"
//                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        navigateFromIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        val app = application as PushSampleApplication
        app.mpsClient?.logout()
    }

    override fun onResume() {
        super.onResume()
        // 앱이 포그라운드로 돌아올 때 메시지 동기화
        viewModel.syncMessages()
        // 재진입 인텐트 처리 (딥링크/푸시 extras)
        navigateFromIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navigateFromIntent(intent)
    }

    private fun registerPermissionListener() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Notification permission granted")
            } else {
                Log.w(TAG, "Notification permission denied")
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                else -> {
                    Log.d(TAG, "Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * Push 데이터 로깅 (현재 PushMessageData 구조에 맞춤)
     */
    private fun logPushData(extras: Bundle) {
        Log.i(TAG, "Received Push Data:")
        Log.i(TAG, "  pushDispatchId: ${extras.getString("mps_dispatch_id")}")
        Log.i(TAG, "  messageType: ${extras.getString("mps_message_type")}")
        Log.i(TAG, "  title: ${extras.getString("mps_title")}")
        Log.i(TAG, "  body: ${extras.getString("mps_body")}")
        Log.i(TAG, "  imageUrl: ${extras.getString("mps_image_url")}")
        Log.i(TAG, "  campaignId: ${extras.getString("mps_campaign_id")}")
        Log.i(TAG, "  payload: ${extras.getString("mps_payload")}")
        Log.i(TAG, "  receivedAt: ${extras.getString("mps_received_at")}")
        Log.i(TAG, "  status: ${extras.getString("mps_status")}")
    }

    /**
     * 딥링크/푸시 클릭 인텐트 처리.
     * scheme: shop://app
     * 예: shop://app/productDetail?productId=123, shop://app/notifications
     */
    private fun handleDeepLinkIfAny(target: android.content.Intent) {
        val data = target.data ?: return
        val path = data.lastPathSegment
        val productId = data.getQueryParameter("productId")
        Log.d(TAG, "DeepLink received: $data")

        viewModel.handleDeeplink(path, productId)
    }

    /**
     * 다양한 포맷의 payload 에서 productId 추출.
     * - extras["productId"] 직접 제공
     * - extras["payload"] 가 JSON String
     * - extras.getBundle("payload") 내부에 productId 존재
     */
    private fun extractProductIdFromExtras(extras: Bundle): String? {
        // 1) direct key (mps_ 접두사 고려)
        extras.getString("productId")?.let { if (it.isNotBlank()) return it }
        extras.getString("mps_product_id")?.let { if (it.isNotBlank()) return it }

        // 2) payload as Bundle (mps_ 접두사 고려)
        val payloadBundle = extras.getBundle("payload")
        payloadBundle?.getString("productId")?.let { if (it.isNotBlank()) return it }
        
        val mpsPayloadBundle = extras.getBundle("mps_payload")
        mpsPayloadBundle?.getString("productId")?.let { if (it.isNotBlank()) return it }

        // 3) payload as JSON string (mps_ 접두사 고려)
        listOf("payload", "mps_payload").forEach { key ->
            val payloadString = extras.getString(key)
            if (!payloadString.isNullOrBlank()) {
                try {
                    val json = org.json.JSONObject(payloadString)
                    val pid = json.optString("productId")
                    if (pid.isNotBlank()) return pid
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse payload json: $payloadString", e)
                }
            }
        }
        return null
    }

    /**
     * Intent 로부터 네비게이션 트리거 처리 (딥링크/푸시 extras 모두 지원)
     */
    private fun navigateFromIntent(target: android.content.Intent?) {
        if (target == null) {
            Log.d(TAG, "navigateFromIntent: target is null")
            return
        }
        
        Log.d(TAG, "navigateFromIntent: action=${target.action}, data=${target.data}")
        
        // 딥링크 우선 처리
        handleDeepLinkIfAny(target)

        // extras 처리
        target.extras?.let { extras ->
            Log.d(TAG, "navigateFromIntent: extras found, keys=${extras.keySet()}")
            logPushData(extras)
            extractProductIdFromExtras(extras)?.let { pid ->
                Log.d(TAG, "navigateFromIntent: extracted productId=$pid, navigating to detail")
                viewModel.handleDeeplink("productDetail", pid)
            } ?: run {
                val payload = extras.getString("mps_payload")
                Log.d(TAG, "navigateFromIntent: no direct productId, trying payload=$payload")
                viewModel.handlePushPayloadNavigate(payload)
            }
        } ?: run {
            Log.d(TAG, "navigateFromIntent: no extras found")
        }
    }
}

/**
 * Composable 내부에서 시스템 UI 스타일(상태바 아이콘 색 등)을 제어하는 함수
 */
@Composable
private fun SystemAppearance(darkTheme: Boolean) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

}
