package no.ntnu.prog2007.ihost.data.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import no.ntnu.prog2007.ihost.data.remote.api.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Interceptor that adds Firebase ID token to every request
    private class FirebaseAuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = getFirebaseToken()

            // Add Authorization header if token exists
            val requestBuilder = originalRequest.newBuilder()
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(requestBuilder.build())
        }

        /**
         * Fetches the current Firebase ID token.
         * @return The Firebase ID token, or null if user is not authenticated or an error occurs
         */
        private fun getFirebaseToken(): String? {
            return try {
                val firebaseUser = Firebase.auth.currentUser
                if (firebaseUser != null) {
                    // Use runBlocking with await() for proper async handling
                    // This is acceptable in OkHttp interceptors as they run on IO threads
                    runBlocking {
                        firebaseUser.getIdToken(false).await().token
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                // Log the error for debugging
                android.util.Log.e("RetrofitClient", "Failed to get Firebase token", e)
                null
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(FirebaseAuthInterceptor())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val eventApi: EventApi by lazy {
        retrofit.create(EventApi::class.java)
    }

    val eventUserApi: EventUserApi by lazy {
        retrofit.create(EventUserApi::class.java)
    }

    val imageApi: ImageApi by lazy {
        retrofit.create(ImageApi::class.java)
    }

    val friendshipApi: FriendshipApi by lazy {
        retrofit.create(FriendshipApi::class.java)
    }

    val stripeApi: StripeApi by lazy {
        retrofit.create(StripeApi::class.java)
    }
}
