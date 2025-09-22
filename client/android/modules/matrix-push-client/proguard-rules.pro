# MatrixPushClient Builder 메서드 보존
-keep class matrix.push.client.MatrixPushClient { *; }
-keep class matrix.push.client.MatrixPushClient$Builder { *; }
-keep class matrix.push.client.MatrixPushClient$Companion { *; }

# 콜백 인터페이스 및 메서드 보존
-keep interface matrix.push.client.usecase.OnErrorListener {
    public void onError(matrix.push.client.modules.error.MpsException);
}
-keep interface matrix.push.client.usecase.OnInitializedListener {
    public void onInitialized(matrix.push.client.MatrixPushClient);
}

-keep interface matrix.push.client.usecase.OnNewMessageListener {
    public void onNewMessage(matrix.push.client.modules.database.PushMessageEntity);
}

-keep interface matrix.push.client.usecase.OnSyncMessageCompleteListener {
    public void onComplete(int);
}

-keep class matrix.push.client.data.** { *; }
-keep class matrix.push.client.service.MatrixPushService
-keep class matrix.push.client.service.MatrixPushReceiver

# 예외 및 에러 코드 보존
-keep class matrix.push.client.modules.error.MpsException { *; }
-keep class matrix.push.client.modules.error.MpsError { *; }

# 데이터 클래스 보존
-keep class matrix.push.client.modules.network.data.** { *; }