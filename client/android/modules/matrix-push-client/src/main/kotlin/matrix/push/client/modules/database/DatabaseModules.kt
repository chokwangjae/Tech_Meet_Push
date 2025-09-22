package matrix.push.client.modules.database

import android.content.Context

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 */
internal object DatabaseModules {

    private lateinit var applicationContext: Context

    private lateinit var databaseInstance: MatrixPushDatabase
    internal lateinit var pushMessagesDao: PushMessagesDao

    internal fun inject(context: Context) {
        this.applicationContext = context.applicationContext
        this.databaseInstance = MatrixPushDatabase.getInstance(applicationContext)

        this.pushMessagesDao = databaseInstance.matrixPushReceiveDao()
    }
}