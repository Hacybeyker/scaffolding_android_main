package com.hacybeyker.main.di.module

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.hacybeyker.main.BuildConfig
import com.hacybeyker.main.commons.exception.ApiException
import com.hacybeyker.main.commons.exception.NoInternetConnectionException
import com.hacybeyker.main.commons.network.NetworkStatus
import com.hacybeyker.main.utils.constans.ConstantsDI
import com.hacybeyker.main.utils.constans.ConstantsDI.Http.RESPONSE_OK
import com.hacybeyker.main.utils.constans.ConstantsDI.Named.API_KEY
import com.hacybeyker.main.utils.constans.ConstantsDI.Named.BASE_URL
import com.hacybeyker.main.utils.constans.ConstantsDI.Parameters.AUTH_TOKEN
import com.hacybeyker.main.utils.constans.ConstantsDI.Parameters.BEARER
import com.hacybeyker.main.utils.constans.ConstantsDI.Parameters.TIMEOUT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private external fun getApiKeyRelease(): String
    private external fun getApiKeyQA(): String

    @Singleton
    @Provides
    @Named(API_KEY)
    fun provideApiKey(): String {
        return if (BuildConfig.DEBUG) getApiKeyQA() else getApiKeyRelease()
    }

    @Singleton
    @Provides
    @Named(BASE_URL)
    fun provideBaseUrl(): String {
        return BuildConfig.BASE_URL
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Singleton
    @Provides
    fun provideChuckCollector(@ApplicationContext context: Context): ChuckerCollector {
        return ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )
    }

    @Singleton
    @Provides
    fun provideChuckInterceptor(
        chuckerCollector: ChuckerCollector,
        @ApplicationContext context: Context
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(ConstantsDI.Parameters.CONTENT_LENGTH)
            .redactHeaders(AUTH_TOKEN, BEARER)
            .alwaysReadResponseBody(true)
            .build()
    }

    @Singleton
    @Provides
    fun providerOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        apiInterceptor: ApiInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(apiInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun providerRetrofit(
        @Named(BASE_URL) baseUrl: String,
        client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    class ApiInterceptor @Inject constructor(
        @Named(API_KEY) private val apiKey: String,
        private val networkUtils: NetworkStatus
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!networkUtils.isOnline()) {
                throw NoInternetConnectionException()
            }
            var request = chain.request()
            val url = request.url.newBuilder().addQueryParameter(API_KEY, apiKey).build()
            request = Request.Builder().url(url).build()
            val response = chain.proceed(request)
            if (!response.isSuccessful && response.code != RESPONSE_OK) {
                throw ApiException()
            }
            return response
        }
    }
}
