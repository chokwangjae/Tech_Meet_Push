# Matrix Push SDK 사용 가이드

Matrix Push SDK는 귀하의 안드로이드 애플리케이션에 실시간 푸시 알림 기능을 손쉽게 통합할 수 있도록 돕습니다. 본 문서는 SDK의 설치, 초기화 및 주요 기능 사용법을 안내합니다.

## 목차

1.  [시작하기](#1-시작하기)
    *   [의존성 추가](#의존성-추가)
    *   [AndroidManifest.xml 설정](#androidmanifestxml-설정)
2.  [SDK 초기화](#2-sdk-초기화)
    *   [기본 초기화](#기본-초기화)
    *   [초기화 옵션](#초기화-옵션)
    *   [푸시 모드 설정](#푸시-모드-설정)
    *   [연결 및 로그인](#연결-및-로그인)
3.  [푸시 메시지 처리](#3-푸시-메시지-처리)
    *   [새 메시지 리스너 등록](#새-메시지-리스너-등록-setonnewmessagelistener)
    *   [메시지 목록 조회 및 관찰](#메시지-목록-조회-및-관찰)
    *   [메시지 상태 변경](#메시지-상태-변경)
    *   [미수신 메시지 동기화](#미수신-메시지-동기화)
4.  [푸시 수신 동의 관리](#4-푸시-수신-동의-관리)
    *   [캠페인 목록 조회](#캠페인-목록-조회-getcampaignlist)
    *   [전체 푸시 수신 동의 설정](#전체-푸시-수신-동의-설정-updateuserconsent)
    *   [캠페인 별 수신 동의 설정](#캠페인-별-수신-동의-설정-updatecampaignconsent)
5.  [고급 설정](#5-고급-설정)
    *   [SDK 종료](#sdk-종료-shutdown)
    *   [재부팅 후 메시지 수신](#재부팅-후-메시지-수신)

---

## 1. 시작하기

### settings.gradle.kts 설정

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

### gradle 디렉토리에 toml 파일 추가

`gradle/push-libs.versions.toml` 파일을 추가한다.

### app/build.gradle.kts 설정

SDK를 추가한다:

```kotlin
plugins {
    ...

    alias(push.plugins.ksp)
    alias(push.plugins.google.services)

    ...
}
```

### 의존성 추가

먼저, 프로젝트의 `build.gradle.kts` 파일에 Matrix Push SDK 의존성을 추가합니다.

```kotlin
dependencies {
    // ... 다른 의존성들

    // ==== Push Client Dependencies Start ====
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(push.shortcut.badger)
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
    // ==== Push Client Dependencies End ====


}
```

### AndroidManifest.xml 설정

푸시 기능을 정상적으로 사용하려면 `AndroidManifest.xml` 파일에 다음 권한과 컴포넌트를 추가해야 합니다.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.yourapp">

    <!-- 인터넷 사용 권한 (필수) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Android 13 이상에서 알림을 표시하기 위한 권한 (필수) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

    <application
    ... >

    <!-- Matrix Push Start -->

    <service android:name="matrix.push.client.service.MatrixPushService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT"/>
        </intent-filter>
    </service>

    <receiver android:name="matrix.push.client.service.MatrixPushReceiver"
        android:exported="false"/>

    <!-- Matrix Push End -->

</application>
    </manifest>
```

---

## 2. SDK 초기화

### 기본 초기화

SDK를 사용하기 전에 반드시 초기화해야 합니다. 일반적으로 `Application` 클래스의 `onCreate()` 메서드에서 초기화하는 것을 권장합니다.

```kotlin
// YourApplication.kt
import android.app.Application
import matrix.push.client.MatrixPushClient
import matrix.push.client.PushMode

class YourApplication : Application() {

    var pushClient: MatrixPushClient? = null

    override fun onCreate() {
        super.onCreate()

        pushClient = MatrixPushClient.builder(this, "YOUR_SERVER_URL")
            .build()
    }
}
```

### 초기화 옵션

`builder`를 통해 다양한 초기화 옵션을 설정할 수 있습니다.

```kotlin
pushClient = MatrixPushClient.builder(context, "YOUR_SERVER_URL")
    // FCM 토큰 자동 등록 및 서버 연결 여부 (기본값: true)
    // false로 설정 시, 원하는 시점에 `pushClient.start()`를 직접 호출해야 합니다.
    .autoTokenRegistration(true)
    
    // 푸시 모드 설정 (기본값: PushMode.PUBLIC)
    .pushMode(PushMode.PUBLIC)
    
    // SDK 내부 로그 출력 여부 (개발 시 유용, 기본값: false)
    .debugMode(true)
    
    // 초기화 과정에서 에러 발생 시 콜백
    .onInitializeError { exception ->
        Log.e("MyApp", "Push SDK initialization failed", exception)
    }
    .build()
```

### 푸시 모드 설정

SDK는 세 가지 푸시 모드를 지원합니다:

- **PushMode.PUBLIC**: FCM을 통한 일반 푸시만 사용 (기본값)
- **PushMode.PRIVATE**: SSE(Server-Sent Events)를 통한 실시간 푸시만 사용
- **PushMode.ALL**: 두 방식을 모두 사용

```kotlin
pushClient = MatrixPushClient.builder(context, "YOUR_SERVER_URL")
    .pushMode(PushMode.ALL) // 두 방식 모두 사용
    .build()
```

### 서비스 시작

`autoTokenRegistration(true)`로 설정하면 `build()` 시점에 자동으로 서비스가 시작됩니다.

만약 `autoTokenRegistration(false)`로 설정했다면, 개발자가 원하는 시점에 `start()` 메서드를 직접 호출하여 푸시 서비스(로그인, SSE 연결 등)를 시작해야 합니다.

```kotlin
// autoTokenRegistration(false)로 빌드한 경우...

// 예: 사용자가 로그인에 성공한 시점에 푸시 서비스 시작
fun onUserLoggedIn() {
    pushClient?.start { exception ->
        if (exception == null) {
            Log.d("MyApp", "Push service started successfully")
        } else {
            Log.e("MyApp", "Failed to start push service", exception)
        }
    }
}
```

#### 개별 로그인 및 연결 (고급)

특별한 시나리오에서 로그인과 SSE 연결을 분리하여 호출해야 할 경우, `login()`과 `connect()`를 직접 사용할 수 있습니다.

**Public Push 로그인**
```kotlin
// 사용자 정보와 함께 로그인
pushClient.login(
    userId = "user123",
    userName = "홍길동", 
    email = "user@example.com"
) { exception ->
    if (exception == null) {
        Log.d("MyApp", "Login successful")
    } else {
        Log.e("MyApp", "Login failed", exception)
    }
}
```

**Private Push 연결**
```kotlin
pushClient.connect { exception ->
    if (exception == null) {
        Log.d("MyApp", "SSE connection successful")
    } else {
        Log.e("MyApp", "SSE connection failed", exception)
    }
}
```

---

## 3. 푸시 메시지 처리

### 새 메시지 리스너 등록: `setOnNewMessageListener`

앱이 실행 중일 때 새로운 푸시 메시지가 도착하면 호출되는 리스너를 등록합니다.

```kotlin
// Activity나 Service에서
val pushClient = (application as YourApplication).pushClient

val messageListener = OnNewMessageListener { pushMessage ->
    // 새로운 메시지 도착 시 처리할 로직
    Log.d("MyApp", "New message received: ${pushMessage.title}")
}
pushClient.setOnNewMessageListener(messageListener)
```

### 메시지 목록 조회 및 관찰

#### 모든 메시지 한 번에 조회: `getAllMessages`

로컬 DB에 저장된 모든 푸시 메시지 목록을 가져옵니다.

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

#### 메시지 목록 실시간 관찰: `observeAllMessages`

메시지 목록에 변경이 생길 때마다 새로운 목록을 방출하는 `Flow`를 반환합니다.

```kotlin
// Coroutine Scope 내에서 수집
lifecycleScope.launch {
    pushClient.observeAllMessages().collect { messages ->
        // 메시지 목록이 변경될 때마다 UI 업데이트
        messageListAdapter.submitList(messages)
    }
}
```

### 메시지 상태 변경

#### 특정 메시지 읽음 처리: `markMessageAsConfirmed`

사용자가 특정 메시지를 확인했을 때, 서버에 '읽음' 상태를 전송합니다.

```kotlin
val pushDispatchId = "some-unique-dispatch-id"
pushClient.markMessageAsConfirmed(pushDispatchId)
```

#### 모든 메시지 읽음 처리: `markAllMessagesAsConfirmed`

모든 '읽지 않은' 메시지를 '읽음' 상태로 변경합니다.

```kotlin
pushClient.markAllMessagesAsConfirmed()
```

#### 메시지 삭제: `deleteMessage`

로컬 DB에 저장된 특정 메시지를 삭제합니다.

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

### 미수신 메시지 동기화

#### 동기화 완료 리스너 등록: `setOnSyncMessageCompleteListener`

`syncMessages()` 호출 후 미수신 메시지 동기화가 완료되었을 때 호출되는 리스너입니다.

```kotlin
pushClient.setOnSyncMessageCompleteListener { success, count ->
    if (success) {
        Log.d("MyApp", "$count new messages synced.")
    } else {
        Log.e("MyApp", "Message sync failed.")
    }
}
```

#### 동기화 요청: `syncMessages`

서버에 미수신 메시지가 있는지 확인하고 동기화를 요청합니다.

```kotlin
// 예: 앱이 포그라운드로 돌아왔을 때
pushClient.syncMessages()
```

---

## 4. 푸시 수신 동의 관리

### 캠페인 목록 조회: `getCampaignList`

사용자가 수신 동의 여부를 설정할 수 있는 전체 캠페인 목록을 조회합니다.

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

### 전체 푸시 수신 동의 설정: `updateUserConsent`

앱 전체의 푸시 수신 동의 여부를 설정합니다. 성공 시, 변경된 동의 상태와 캠페인 목록이 포함된 `UserConsentResponse` 객체를 반환합니다.

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

### 캠페인 별 수신 동의 설정: `updateCampaignConsent`

특정 캠페인에 대한 수신 동의 여부를 설정합니다. 성공 시, 변경된 전체 동의 상태와 캠페인 목록이 포함된 `UserConsentResponse` 객체를 반환합니다.

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

---

## 5. 고급 설정

### SDK 종료: `shutdown`

앱이 종료되거나 사용자가 로그아웃할 때 SDK의 모든 동작(SSE 연결, 백그라운드 작업 등)을 중지하고 리소스를 정리합니다.

```kotlin
// 예: 사용자가 로그아웃할 때
pushClient.shutdown()
```

> **주의)** `shutdown()` 호출 시:
> - SSE 연결이 끊어집니다
> - 모든 백그라운드 작업이 중지됩니다
> - 앱 종료 후 FCM 메시지 수신 시 Firebase가 앱 프로세스를 깨우면서 `Application.onCreate()`에서 SDK가 자동으로 다시 초기화됩니다
> - 따라서 `shutdown()` 호출 후에도 푸시 메시지는 정상적으로 수신 및 처리됩니다

### 재부팅 후 메시지 수신

Android 시스템 재부팅 후에도 푸시 메시지를 정상적으로 수신할 수 있습니다:

- **앱이 실행 중인 경우**: SDK가 이미 초기화되어 있어 정상적으로 처리됩니다
- **앱이 종료된 상태**: FCM 메시지 수신 시 Firebase가 앱 프로세스를 깨우면서 `Application.onCreate()`에서 SDK가 자동으로 초기화됩니다

재부팅 후 첫 번째 푸시 메시지가 도착하면 앱이 자동으로 시작되어 SDK 초기화가 완료되므로, 별도의 설정이나 수동 초기화 없이도 지속적으로 푸시 메시지를 받을 수 있습니다. 