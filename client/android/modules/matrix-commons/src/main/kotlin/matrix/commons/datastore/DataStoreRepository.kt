package matrix.commons.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * @author tarkarn
 * @since 2021.08.24
 *
 * SharedPreference 를 대체하기 위한 Preference DataStore 구현체.
 * https://developer.android.com/topic/libraries/architecture/datastore?hl=ko#kotlin
 */
class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val TAG = DataStoreRepository::class.java.simpleName
    }

    private val gson = Gson()

    /**
     * DataStore 에 Data 를 저장한다.
     *
     * @param key   저장할 데이터의 키 값.
     * @param value 저장할 데이터의 값.
     */
    suspend fun <T> setData(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preference ->
            preference[key] = value
        }
    }

    /**
     * DataStore 에 저장되어 있는 Data 를 가져온다.
     *
     * @param key   가져올 데이터의 키 값.
     * @param defaultValue  값이 null 인 경우 가져올 기본 값.
     *
     * @return 키 값에 맞는 데이터 리턴.
     */
    suspend fun <T> getData(key: Preferences.Key<T>, defaultValue: T): T {

        return dataStore.data.catch { exception ->
            when (exception is IOException) {
                true -> emit(emptyPreferences())
                false -> throw exception
            }
        }.map { preference ->
            preference[key] ?: defaultValue
        }.first()
    }

    /**
     * DataStore 파일을 삭제한다.
     */
    suspend fun clearDataStore() {
        dataStore.edit { it.clear() }
    }

    /**
     * DataStore 의 데이터를 삭제한다.
     *
     * @param key   삭제할 데이터의 키 값.
     */
    suspend fun <T> removeData(key: Preferences.Key<T>) {
        dataStore.edit { it.remove(key) }
    }
}