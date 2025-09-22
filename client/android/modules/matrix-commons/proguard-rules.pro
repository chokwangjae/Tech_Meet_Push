# matrix.commons 패키지 중 외부에서 참조하는 주요 클래스 유지
-keep class matrix.commons.datastore.PreferenceDataStoreKt { *; }
-keep class matrix.commons.datastore.PreferenceKeys$Push { *; }
-keep class matrix.commons.datastore.PreferenceKeys$Push$Auth { *; }
-keep class matrix.commons.datastore.DataStoreRepository { *; }

-keep class matrix.commons.log.MatrixLog { *; }

-keep class matrix.commons.event.SingleCoroutineDispatcher { *; }

# GsonExtensionsKt 클래스 안의 특정 멤버만 유지
-keep class matrix.commons.utils.GsonExtensionsKt { *; }

# Gson 라이브러리 자체의 클래스들이 난독화되는 것을 방지
-keep class com.google.gson.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Utils에서 필요한 멤버만 보존
-keep class matrix.commons.utils.Utils { *; }