package matrix.commons.datastore

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore

/**
 * @author tarkarn
 * @since 2021.08.24
 *
 * Preference DataStore 생성.
 */

private const val MATRIX_MODULES_DATASTORE_DATA = "matrix-modules-data"

/**
 * Matrix Modules - Push Module 전용 Datastore
 */
val Context.matrixPushDatastore by preferencesDataStore(
    name = MATRIX_MODULES_DATASTORE_DATA,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "matrix-push-datastore"))
    }
)