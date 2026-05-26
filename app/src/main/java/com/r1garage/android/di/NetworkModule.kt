package com.r1garage.android.di

import com.r1garage.android.data.rivian.RivianApi
import com.r1garage.android.data.rivian.RivianAuthApi
import com.r1garage.android.data.rivian.RivianAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // All authenticated GraphQL queries (vehicleState / getUserInfo /
    // getVehicleImages) go through the same gateway endpoint as login —
    // there is no separate `/consumer/graphql` endpoint, and hitting it
    // returns 404.  See https://github.com/bretterer/rivian-python-client.
    private const val RIVIAN_GATEWAY_URL = "https://rivian.com/api/gql/gateway/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: RivianAuthInterceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRivianApi(client: OkHttpClient, json: Json): RivianApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(RIVIAN_GATEWAY_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(RivianApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRivianAuthApi(client: OkHttpClient, json: Json): RivianAuthApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(RIVIAN_GATEWAY_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(RivianAuthApi::class.java)
    }
}
