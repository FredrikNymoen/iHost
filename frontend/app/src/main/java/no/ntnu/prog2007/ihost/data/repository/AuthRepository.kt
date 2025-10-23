package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import no.ntnu.prog2007.ihost.data.model.CreateUserRequest
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient

class AuthRepository(
    private val firebaseAuth: FirebaseAuth
) {

    private val apiService = RetrofitClient.apiService

    /**
     * Register user in Firebase Auth and create profile on backend
     * Handles the complete registration flow
     */
    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> = try {
        Log.d("AuthRepository", "Starting registration for email: $email, name: $name")

        // Step 1: Create user in Firebase Auth
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
            .await()
        val firebaseUser = authResult.user
            ?: return Result.failure(Exception("User creation failed"))

        Log.d("AuthRepository", "Firebase user created with UID: ${firebaseUser.uid}")

        // Step 2: Update display name in Firebase
        firebaseUser.updateProfile(userProfileChangeRequest {
            displayName = name
        }).await()

        Log.d("AuthRepository", "Firebase profile updated with name: $name")

        // Step 3: Register on backend (token is added automatically by interceptor)
        val createUserRequest = CreateUserRequest(
            uid = firebaseUser.uid,
            displayName = name,
            email = email
        )

        Log.d("AuthRepository", "Sending to backend: uid=${firebaseUser.uid}, email=$email, displayName=$name")

        val response = apiService.registerUser(createUserRequest)

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
     * Get current user
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
