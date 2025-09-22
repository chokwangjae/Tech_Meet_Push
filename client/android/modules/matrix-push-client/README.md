# MatrixPush Android SDK 가이드

Android 앱에서 MatrixPush SDK를 사용하기 위한 설정 가이드

## 환경 요구사항

- **minSdk**: 24
- **compileSdk**: 36
- **Kotlin**: 2.1.0 이상
- **JDK**: 11 이상

## 필요 파일 복사

다음 파일들을 프로젝트에 복사해야 함:

```
gradle/push-libs.versions.toml                      // push client 의존성 관리 파일
app/google-services.json                           // Firebase 인증 토큰
app/libs/matrix-commons-1.0.0.jar                 // Matrix Commons SDK
app/libs/matrix-push-client-1.0.0.jar            // MatrixPush Client SDK  
```

## 설정

### 1. settings.gradle.kts 설정

Push Client용 Version Catalog를 추가한다:

```kotlin
dependencyResolutionManagement {
    ...

    versionCatalogs {
        create("push") {
            from(files("gradle/push-libs.versions.toml"))
        }
    }

    ...
}
```

### 2. gradle 디렉토리에 toml 파일 추가

`gradle/push-libs.versions.toml` 파일을 추가한다.

### 3. app/build.gradle.kts 설정

SDK를 추가한다:

```kotlin
plugins {
    ...

    alias(push.plugins.ksp)
    alias(push.plugins.google.services)

    ...
}

dependencies {
    // MatrixPushClient SDK JAR 파일 추가


    ...


    // ==== Push Client Dependencies Start ====
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(push.bundles.retrofit)
    implementation(push.coil)
    implementation(push.datastore.preferences)
    implementation(push.bundles.database)
    ksp(push.androidx.room.compiler)

    // Firebase FCM 의존성 (필수) - 개별로 넣을 경우.
//    implementation("com.google.firebase:firebase-messaging:24.1.2")

    // Firebase FCM 의존성 (필수) - Bom 사용 시
    implementation(platform(push.firebase.bom))
    implementation(push.firebase.messaging.ktx)

    // 임시 사용 SocketIO Library
    implementation ("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // ==== Push Client Dependencies End ====
}
```

### 4. AndroidManifest.xml 설정

필요한 권한과 서비스를 추가한다:

```xml
<!-- 네트워크 권한 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- 알림 권한 (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- 개발 프로젝트에 맞는 application 으로 설정하세요. -->
<application
android:name=".PushSampleApplication"
android:usesCleartextTraffic="true">

...

<!-- 서비스 추가 -->
<service android:name="matrix.push.client.service.MatrixPushService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT"/>
    </intent-filter>
</service>

...

</application>
```

## 사용법

### 1. Application 클래스에서 초기화

`PushSampleApplication.kt`:

```kotlin
class PushSampleApplication : Application() {

    private lateinit var pushClient: MatrixPushClient

    override fun onCreate() {
        super.onCreate()

        // 기본 초기화
        pushClient = MatrixPushClient.builder(this, "https://your-domain.com:port")
            .autoTokenRegistration(false)
            .debugMode(true)
            // 초기화 과정에서 에러 발생 시 콜백
            .onError { object : OnErrorListener {
                override fun onError(mpsException: MpsException) {
                    Log.e("MyApp", "${mpsException.details ?: ""} (${mpsException.error.code})")
                }
            }
            }
            // 초기화 완료 콜백
            .onInitialized { object : OnInitializedListener {
                override fun onInitialized(client: MatrixPushClient) {
                    Log.d("MyApp", "init 완료")
                }
            }
            }
            .build()
    }

    override fun onTerminate() {
        super.onTerminate()
        pushClient.shutdown()
    }
}
```

### 2. 개별 로그인 및 연결 (고급)

#### Public Push 로그인

```kotlin
// 사용자 정보와 함께 로그인
pushClient.login(
    userId = "user123",
    userName = "홍길동",
    email = "user@example.com"
) {
    Log.d("MyApp", "Login successful")
}
```

#### Private Push 연결

```kotlin
pushClient.connect {
    Log.d("MyApp", "SSE connection successful")
}
```


### 3. 푸시 메시지 처리

`setOnNewMessageListener` 리스너를 등록하여 실시간으로 푸시 메시지를 받을 수 있다:

```kotlin
// Activity나 Service에서
val pushClient = (application as YourApplication).pushClient
val messageListener = OnNewMessageListener { pushMessage ->
    // 새로운 메시지 도착 시 처리할 로직
    Log.d("MyApp", "New message received: ${pushMessage.title}")
}
pushClient.setOnNewMessageListener(messageListener)
```

### 4. 메시지 목록 조회 및 관찰

`getAllMessages` 모든 메시지 한 번에 조회 :

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val allMessages = pushClient.getAllMessages()
        // 메시지 목록 UI에 업데이트
    } catch (e: Exception) {
        // 에러 처리
    }
}
```

`observeAllMessages` 메시지 목록 실시간 관찰 :

```kotlin
// Coroutine Scope 내에서 수집
lifecycleScope.launch {
    pushClient.observeAllMessages().collect { messages ->
        // 메시지 목록이 변경될 때마다 UI 업데이트
        messageListAdapter.submitList(messages)
    }
}
```

## MatrixPushClient API 사용법

MatrixPushClient는 MatrixPush SDK의 공개 API 진입점이다. 모든 주요 기능에 대한 간단하고 일관된 인터페이스를 제공한다.

### 1. 핵심 푸시 기능

#### 푸시 수신 동의 관리

`getCampaignList` 캠페인 목록 조회 :

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val campaigns = pushClient.getCampaignList()
        // 캠페인 목록을 사용하여 설정 화면 UI 구성
    } catch (e: Exception) {
        Log.e("MyApp", "Failed to get campaign list", e)
    }
}
```

`updateUserConsent` 전체 푸시 수신 동의 설정 :

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val isConsented = true // 사용자가 동의함
        val response = pushClient.updateUserConsent(isConsented)
        // 반환된 최신 정보로 UI 업데이트
        Log.d("MyApp", "User consent updated: ${response.consented}")
        updateCampaignsUI(response.campaignDetails)
    } catch (e: Exception) {
        Log.e("MyApp", "Failed to update user consent", e)
    }
}
```

`updateCampaignConsent` 캠페인 별 수신 동의 설정 :

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val campaignId = 123L
        val isConsented = false // 특정 캠페인 미동의
        val response = pushClient.updateCampaignConsent(campaignId, isConsented)
        // 반환된 최신 정보로 UI 업데이트
        Log.d("MyApp", "User consent updated: ${response.consented}")
        updateCampaignsUI(response.campaignDetails)
    } catch (e: Exception) {
        Log.e("MyApp", "Failed to update campaign consent", e)
    }
}
```


### 2. 메시지 상태 변경

#### 특정 메시지 읽음 처리

```kotlin
val pushDispatchId = "some-unique-dispatch-id"
pushClient.markMessageAsConfirmed(pushDispatchId)
```

#### 모든 메시지 읽음 처리

```kotlin
pushClient.markAllMessagesAsConfirmed()
```

#### 메시지 삭제

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val pushDispatchId = "some-unique-dispatch-id"
        pushClient.deleteMessage(pushDispatchId)
        // 성공적으로 삭제됨
    } catch (e: Exception) {
        // 에러 처리
    }
}
```

### 3. 메시지 조회 및 관리

#### 전체 메시지 조회

```kotlin
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val allMessages = pushClient.getAllMessages()
        // 메시지 목록 UI에 업데이트
    } catch (e: Exception) {
        // 에러 처리
    }
}
```

#### 특정 메시지 조회

```kotlin
val messageId = "your_message_id"
// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val messages = pushClient.getMessageById(messageId)
        // 메시지 목록 UI에 업데이트
    } catch (e: Exception) {
        // 에러 처리
    }
}
```

#### 기간별 메시지 조회

```kotlin
val startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24시간 전
val endTime = System.currentTimeMillis()

// Coroutine Scope 내에서 호출
lifecycleScope.launch {
    try {
        val messages = pushClient.getMessagesByPeriod(startTime, endTime)
        // 메시지 목록 UI에 업데이트
    } catch (e: Exception) {
        // 에러 처리
    }
}
```

#### 타입별 메시지 조회

```kotlin
try {
    val messages = pushClient.getMessagesByType("TEXT")
    // 메시지 목록 UI에 업데이트
} catch (e: Exception) {
    // 에러 처리
}
```

### 4. 서버-클라이언트 미수신 메시지 동기화

`setOnSyncMessageCompleteListener` 동기화 완료 리스너 등록:

```kotlin
pushClient.setOnSyncMessageCompleteListener { count ->
    Log.d("MyApp", "$count new messages synced.")
}
```

`syncMessages` 동기화 요청:

```kotlin
// 예: 앱이 포그라운드로 돌아왔을 때
pushClient.syncMessages()
```


## 주요 기능

- **실시간 푸시 메시지 수신**: WebSocket을 통한 실시간 메시지 구독
- **Firebase FCM 지원**: 백그라운드 메시지 수신
- **사일런트 푸시**: 알림 없이 백그라운드 작업 수행
- **커스텀 페이로드**: JSON 형태의 추가 데이터 전송
- **에러 핸들링**: 메시지 처리 중 발생하는 오류 처리
- **메시지 조회**: 받은 메시지의 다양한 조회 방법 제공
- **동기화**: 서버와 클라이언트 간 메시지 동기화
- **수신 동의 관리**: 사용자 수신 동의 여부 관리

## 참고사항

- 서버 URL과 포트는 실제 환경에 맞게 수정해야 함
- 디버그 모드는 개발 시에만 사용하고 배포 시에는 비활성화할 것
- 권한 요청은 앱에서 적절한 시점에 처리해야 함
- 모든 API 호출은 비동기로 처리되며 콜백을 통해 결과를 받음
- SDK 초기화 후에 API를 사용해야 함 