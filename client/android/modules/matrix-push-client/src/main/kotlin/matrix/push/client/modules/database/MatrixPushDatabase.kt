package matrix.push.client.modules.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlin.concurrent.Volatile

/**
 * @author tarkarn
 * @since 2025.05.16
 */
@Database(
    entities = [
        PushMessageEntity::class
    ],
    version = 2,  // 스키마 변경으로 인한 버전 업그레이드
    exportSchema = false
)
@TypeConverters(TimeConverters::class)
internal abstract class MatrixPushDatabase : RoomDatabase() {

    abstract fun matrixPushReceiveDao(): PushMessagesDao

    companion object Companion {
        private const val DB_NAME = "matrix_push.db"

        @Volatile
        private var instance: MatrixPushDatabase? = null

        fun getInstance(context: Context): MatrixPushDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MatrixPushDatabase::class.java,
                        DB_NAME
                    )
                    .fallbackToDestructiveMigration(true)  // 스키마 변경으로 인한 기존 데이터 삭제 후 재생성
                    .build()
                    .also { instance = it }
                }
            }

            return instance!!
        }

        fun destroyInstance() {
            instance = null
        }
    }
}