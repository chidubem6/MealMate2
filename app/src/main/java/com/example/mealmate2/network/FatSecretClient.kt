package com.example.mealmate2.network

import android.util.Base64
import com.example.mealmate2.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object FatSecretClient {
    private const val API_BASE_URL = "https://platform.fatsecret.com/rest/"
    private const val API_REQUEST_URL = "https://platform.fatsecret.com/rest/server.api"
    private const val USER_AGENT = "MealMate2/1.0 (Android; com.example.mealmate2)"

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
        val foodApi = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(FatSecretFoodApi::class.java)

        return FatSecretFoodService(foodApi)
    }

    internal fun hasCredentials(): Boolean =
        BuildConfig.FATSECRET_CONSUMER_KEY.isNotBlank() && BuildConfig.FATSECRET_CONSUMER_SECRET.isNotBlank()

    internal fun oauth1Parameters(
        requestParameters: Map<String, String>,
        httpMethod: String = "GET",
        timestampSeconds: Long = System.currentTimeMillis() / 1000L,
        nonce: String = UUID.randomUUID().toString()
    ): Map<String, String> {
        if (!hasCredentials()) {
            throw MissingFatSecretCredentialsException()
        }

        val unsignedParameters = requestParameters + mapOf(
            "oauth_consumer_key" to BuildConfig.FATSECRET_CONSUMER_KEY,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestampSeconds.toString(),
            "oauth_nonce" to nonce,
            "oauth_version" to "1.0"
        )
        val signatureBaseString = listOf(
            httpMethod.uppercase(),
            API_REQUEST_URL,
            unsignedParameters.normalizedParameterString()
        ).joinToString("&") { it.rfc3986Encode() }
        val signingKey = "${BuildConfig.FATSECRET_CONSUMER_SECRET.rfc3986Encode()}&"
        val signature = hmacSha1(signatureBaseString, signingKey)
        return unsignedParameters + ("oauth_signature" to signature)
    }

    private fun Map<String, String>.normalizedParameterString(): String =
        entries
            .map { it.key.rfc3986Encode() to it.value.rfc3986Encode() }
            .sortedWith(compareBy<Pair<String, String>> { it.first }.thenBy { it.second })
            .joinToString("&") { "${it.first}=${it.second}" }

    private fun String.rfc3986Encode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")

    private fun hmacSha1(value: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA1"))
        return Base64.encodeToString(mac.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
}

class FatSecretFoodService(
    private val foodApi: FatSecretFoodApi
) {
    suspend fun searchFood(query: String): FoodSearchResponse {
        val parameters = FatSecretClient.oauth1Parameters(
            mapOf(
                "method" to "foods.search",
                "search_expression" to query.trim().replace(Regex("\\s+"), " "),
                "max_results" to "20",
                "format" to "json"
            )
        )
        val response = foodApi.call(parameters)
        response.error?.let { throw FatSecretApiException(it.message, it.code) }
        return response
    }

    suspend fun getFood(foodId: Long): FoodProduct {
        val parameters = FatSecretClient.oauth1Parameters(
            mapOf(
                "method" to "food.get",
                "food_id" to foodId.toString(),
                "flag_default_serving" to "true",
                "format" to "json"
            )
        )
        val response = foodApi.call(parameters)
        response.error?.let { throw FatSecretApiException(it.message, it.code) }
        return response.food ?: throw FatSecretApiException("FatSecret returned no food details.", null)
    }
}

interface FatSecretFoodApi {
    @GET("server.api")
    suspend fun call(
        @QueryMap parameters: Map<String, String>
    ): FoodSearchResponse
}

class MissingFatSecretCredentialsException : IllegalStateException(
    "Missing FatSecret API credentials"
)

class FatSecretApiException(
    message: String,
    val code: Int?
) : IllegalStateException(message)
