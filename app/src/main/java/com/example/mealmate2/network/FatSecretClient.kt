package com.example.mealmate2.network

import android.util.Base64
import com.example.mealmate2.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object FatSecretClient {
    private const val API_BASE_URL = "https://platform.fatsecret.com/rest/"
    private const val AUTH_BASE_URL = "https://oauth.fatsecret.com/"
    private const val USER_AGENT = "MealMate2/1.0 (Android; com.example.mealmate2)"
    private const val TOKEN_EXPIRY_BUFFER_SECONDS = 60L

    fun create(): FatSecretFoodService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(
                FatSecretFoodResults::class.java,
                FatSecretFoodResultsDeserializer()
            )
            .registerTypeAdapter(
                FatSecretFoods::class.java,
                FatSecretFoodsDeserializer()
            )
            .registerTypeAdapter(
                FatSecretServings::class.java,
                FatSecretServingsDeserializer()
            )
            .create()
        val gsonConverterFactory = GsonConverterFactory.create(gson)
        val authApi = Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(FatSecretAuthApi::class.java)

        val foodApi = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(FatSecretFoodApi::class.java)

        return FatSecretFoodService(authApi, foodApi)
    }

    internal fun basicAuthHeader(): String {
        val credentials = "${BuildConfig.FATSECRET_CLIENT_ID}:${BuildConfig.FATSECRET_CLIENT_SECRET}"
        val encoded = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    internal fun hasCredentials(): Boolean =
        BuildConfig.FATSECRET_CLIENT_ID.isNotBlank() && BuildConfig.FATSECRET_CLIENT_SECRET.isNotBlank()

    internal fun expiresAtSeconds(expiresInSeconds: Long): Long =
        System.currentTimeMillis() / 1000L + expiresInSeconds - TOKEN_EXPIRY_BUFFER_SECONDS
}

class FatSecretFoodService(
    private val authApi: FatSecretAuthApi,
    private val foodApi: FatSecretFoodApi
) {
    private var accessToken: String? = null
    private var expiresAtSeconds: Long = 0L

    suspend fun searchFood(query: String): FoodSearchResponse {
        val token = getAccessToken()
        val response = foodApi.searchFood(
            authorization = "Bearer $token",
            query = query.trim().replace(Regex("\\s+"), " ")
        )
        response.error?.let { throw FatSecretApiException(it.message, it.code) }
        return response
    }

    suspend fun getFood(foodId: Long): FoodProduct {
        val token = getAccessToken()
        val response = foodApi.getFood(
            authorization = "Bearer $token",
            foodId = foodId
        )
        response.error?.let { throw FatSecretApiException(it.message, it.code) }
        return response.food ?: throw FatSecretApiException("FatSecret returned no food details.", null)
    }

    private suspend fun getAccessToken(): String {
        if (!FatSecretClient.hasCredentials()) {
            throw MissingFatSecretCredentialsException()
        }

        val nowSeconds = System.currentTimeMillis() / 1000L
        val cachedToken = accessToken
        if (cachedToken != null && nowSeconds < expiresAtSeconds) {
            return cachedToken
        }

        val response = authApi.getAccessToken(
            authorization = FatSecretClient.basicAuthHeader()
        )
        accessToken = response.accessToken
        expiresAtSeconds = FatSecretClient.expiresAtSeconds(response.expiresInSeconds)
        return response.accessToken
    }
}

interface FatSecretAuthApi {
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "basic"
    ): FatSecretTokenResponse
}

interface FatSecretFoodApi {
    @FormUrlEncoded
    @POST("server.api")
    suspend fun searchFood(
        @Header("Authorization") authorization: String,
        @Field("method") method: String = "foods.search",
        @Field("search_expression") query: String,
        @Field("max_results") maxResults: Int = 20,
        @Field("format") format: String = "json"
    ): FoodSearchResponse

    @FormUrlEncoded
    @POST("server.api")
    suspend fun getFood(
        @Header("Authorization") authorization: String,
        @Field("method") method: String = "food.get",
        @Field("food_id") foodId: Long,
        @Field("flag_default_serving") flagDefaultServing: Boolean = true,
        @Field("format") format: String = "json"
    ): FoodGetResponse
}

class MissingFatSecretCredentialsException : IllegalStateException(
    "Missing FatSecret API credentials"
)

class FatSecretApiException(
    message: String,
    val code: Int?
) : IllegalStateException(message)
