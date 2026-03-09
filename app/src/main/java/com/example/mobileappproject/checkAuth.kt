package com.example.mobileappproject

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ใช้เก็บว่า Google user นี้เป็นครั้งแรกหรือเปล่า
    private val _isNewGoogleUser = MutableStateFlow(false)
    val isNewGoogleUser: StateFlow<Boolean> = _isNewGoogleUser.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object NewGoogleUser : AuthState() // Google login แต่ยังไม่มีข้อมูลใน Firestore
        object ResetPasswordSent : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // ─── Register ด้วย Email + บันทึกข้อมูลลง Firestore ───
    fun register(name: String, email: String, password: String, phone: String, studentId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()

                // บันทึกข้อมูลลง Firestore
                val userProfile = mapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "studentId" to studentId
                )
                db.collection("users").document(email).set(userProfile).await()

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    // ─── Login ด้วย Email ───
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    // ─── Google Sign-In ───
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )
                    auth.signInWithCredential(firebaseCredential).await()

                    // เช็คว่ามีข้อมูลใน Firestore แล้วหรือยัง
                    val email = auth.currentUser?.email ?: ""
                    val doc = db.collection("users").document(email).get().await()

                    if (!doc.exists() || doc.getString("name").isNullOrEmpty()) {
                        // ครั้งแรก ต้องกรอกข้อมูลเพิ่มเติม
                        _authState.value = AuthState.NewGoogleUser
                    } else {
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error("ไม่สามารถรับข้อมูล Google ได้")
                }
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Sign-In ถูกยกเลิก")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    // ─── บันทึกข้อมูลเพิ่มเติมสำหรับ Google User ครั้งแรก ───
    fun saveGoogleUserProfile(name: String, phone: String, studentId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val email = auth.currentUser?.email ?: ""
                val userProfile = mapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "studentId" to studentId
                )
                db.collection("users").document(email).set(userProfile).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    // ─── Reset Password ───
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                    "ERROR_INVALID_EMAIL" -> "รูปแบบ Email ไม่ถูกต้อง"
                    else -> "เกิดข้อผิดพลาด กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            }
        }
    }

    // ─── Logout ───
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        private const val WEB_CLIENT_ID = "785375792286-oet7df8safrh4ajmv8hs0t3knro5st3a.apps.googleusercontent.com"
    }
}