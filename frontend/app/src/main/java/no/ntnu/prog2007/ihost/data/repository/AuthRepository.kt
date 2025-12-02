package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import no.ntnu.prog2007.ihost.data.model.dto.CreateUserRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.UserApi

/**
 * Repository for Firebase Authentication operations
 * Handles user registration, sign in/out, and Firebase user management
 *
 * For user profile operations (get, update, etc.), use UserRepository instead
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val userApi: UserApi = RetrofitClient.userApi
) {

    /**
     * Register user in Firebase Auth and create profile on backend
     * Handles the complete registration flow
     */
    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String? = null
    ): Result<FirebaseUser> = try {
        Log.d("AuthRepository", "Starting registration for email: $email, name: $username")

        // Step 1: Create user in Firebase Auth
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
            .await()
        val firebaseUser = authResult.user
            ?: return Result.failure(Exception("User creation failed"))

        Log.d("AuthRepository", "Firebase user created with UID: ${firebaseUser.uid}")

        // Step 2: Update display name in Firebase
        firebaseUser.updateProfile(userProfileChangeRequest {
            displayName = username
        }).await()

        Log.d("AuthRepository", "Firebase profile updated with name: $username")

        // Step 3: Register on backend (token is added automatically by interceptor)
        val createUserRequest = CreateUserRequest(
            uid = firebaseUser.uid,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email
        )

        Log.d(
            "AuthRepository",
            "Sending to backend: uid=${firebaseUser.uid}, email=$email, displayName=$username"
        )

        val response = userApi.registerUser(createUserRequest)

        Log.d("AuthRepository", "Backend registration successful: ${response.message}")

        Result.success(firebaseUser)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Registration error: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Sign in user with email and password
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = try {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = authResult.user
            ?: return Result.failure(Exception("Sign in failed"))
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Get current Firebase user
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Get ID token for authenticated requests
     */
    suspend fun getIdToken(): String? = try {
        firebaseAuth.currentUser?.getIdToken(true)?.await()?.token
    } catch (e: Exception) {
        null
    }
}
