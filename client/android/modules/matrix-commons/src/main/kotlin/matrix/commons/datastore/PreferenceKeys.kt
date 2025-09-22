package matrix.commons.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * @author tarkarn
 * @since 2021.08.24
 *
 * Preference DataStore 에서 사용할 Key 정의.
 */
object PreferenceKeys {

    /**
     * Push 모듈 전용 Preference Key
     */
    object Push {

        /**
         * Push ServerUrl
         * 최근 접속한 서버주소를 저장하기 위함.
         */
        @JvmStatic
        val SERVER_URL = stringPreferencesKey("push-server-url")

        /**
         * 토큰 자동 등록 여부
         */
        @JvmStatic
        val AUTO_TOKEN_REGISTRATION = booleanPreferencesKey("push-auto-token-registration")

        /**
         * Push Mode
         * default: PUBLIC
         */
        @JvmStatic
        val PUSH_MODE = stringPreferencesKey("push-mode")

        /**
         * 최근 초기화 시 성공적으로 초기화가 되었었는지 기록.
         */
        @JvmStatic
        val IS_INITIALIZED_ONCE = booleanPreferencesKey("push-is-initialized-once")

        /**
         * API 접근 인가를 위한 Id
         */
        @JvmStatic
        val MATRIX_PUSH_ID = stringPreferencesKey("push-matrix-push-id")

        /**
         * 재인증 시 필요 할 수 있으므로 저장.
         */
        object Auth {

            @JvmStatic
            val USER_ID = stringPreferencesKey("push-auth-user-id")

            @JvmStatic
            val USER_NAME = stringPreferencesKey("push-auth-user-name")

            @JvmStatic
            val EMAIL = stringPreferencesKey("push-auth-email")
        }

    }


}