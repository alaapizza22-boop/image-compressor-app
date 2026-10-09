package com.imagecompressor.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around Firebase Authentication covering the three flows required by
 * the app: registration, login, and password reset. Firebase itself must be
 * configured by adding `google-services.json` to the app/ module — see README_BUILD.md.
 *
 * Accounts are fully OPTIONAL: screens using this manager must always offer a
 * "continue as guest" path (see AuthViewModel / AuthScreen).
 */
class FirebaseAuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user ?: error("Registration failed")
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        authResult.user ?: error("Login failed")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() = auth.signOut()
}
