package matrix.push.client.modules.network

import matrix.push.client.MatrixPushClientOptions
import matrix.push.client.MatrixPushClientOptions.sseConnectTimeoutMs
import matrix.push.client.MatrixPushClientOptions.sseReadTimeoutMs
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 *   @author tarkarn
 *   @since 2025. 7. 14.
 */
internal object NetworkModules {
    private const val TAG = "NetworkModules"

    val okHttpClient: OkHttpClient by lazy {
        CustomHttpClient().getClient()
    }
    val pushService: PushService by lazy {
        Retrofit.Builder().apply {
            baseUrl(MatrixPushClientOptions.serverUrl)
            client(okHttpClient)
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
        }.build().create(PushService::class.java)
    }
}

class CustomHttpClient {
    fun getClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        return builder.apply {
            addInterceptor { chain ->
                return@addInterceptor run {
                    val request = chain.request()
                        .newBuilder()
                        .addHeader("Cache-control", "no-cache")
                        .addHeader("Connection", "keep-alive")
                        .build()

                    chain.proceed(request)
                }
            }

            readTimeout(sseReadTimeoutMs, TimeUnit.SECONDS)
            connectTimeout(sseConnectTimeoutMs, TimeUnit.SECONDS)

        }.build()
    }
}