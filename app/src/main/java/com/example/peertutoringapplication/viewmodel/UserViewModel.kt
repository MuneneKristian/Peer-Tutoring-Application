package com.example.peertutoringapplication.viewmodel

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertutoringapplication.data.model.User
import com.example.peertutoringapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _registrationMessage = MutableStateFlow<String?>(null)
    val registrationMessage: StateFlow<String?> = _registrationMessage

    private val _loginMessage = MutableStateFlow<String?>(null)
    val loginMessage: StateFlow<String?> = _loginMessage

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            try {
                if (firstName.isBlank() || lastName.isBlank() ||
                    email.isBlank() || password.isBlank() || role.isBlank()
                ) {
                    _registrationMessage.value = "Please fill in all fields."
                    return@launch
                }

                val normalizedEmail = email.trim().lowercase()

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                    _registrationMessage.value = "Enter a valid email address."
                    return@launch
                }

                if (password.length < 8) {
                    _registrationMessage.value =
                        "Password must be at least 8 characters."
                    return@launch
                }

                if (repository.getUserByEmail(normalizedEmail) != null) {
                    _registrationMessage.value =
                        "This email is already registered."
                    return@launch
                }

                val user = User(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = normalizedEmail,
                    password = hashPassword(password),
                    role = role
                )

                repository.registerUser(user)
                _registrationMessage.value = "Registration successful!"

            } catch (e: Exception) {
                _registrationMessage.value =
                    "Registration failed. Please try again."
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                if (email.isBlank() || password.isBlank()) {
                    _loginMessage.value = "Please enter your email and password."
                    return@launch
                }

                val normalizedEmail = email.trim().lowercase()
                val user = repository.getUserByEmail(normalizedEmail)

                if (user == null || !verifyPassword(password, user.password)) {
                    _loggedInUser.value = null
                    _loginMessage.value = "Invalid email or password."
                    return@launch
                }

                _loggedInUser.value = user
                _loginMessage.value = "Login successful!"

            } catch (e: Exception) {
                _loggedInUser.value = null
                _loginMessage.value = "Login failed. Please try again."
            }
        }
    }

    private fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(password.toCharArray(), salt, 120000, 256)

        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded

        spec.clearPassword()

        return Base64.encodeToString(salt, Base64.NO_WRAP) +
                ":" +
                Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun verifyPassword(password: String, storedPassword: String): Boolean {
        return try {
            val parts = storedPassword.split(":")
            if (parts.size != 2) return false

            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val storedHash = Base64.decode(parts[1], Base64.NO_WRAP)

            val spec = PBEKeySpec(password.toCharArray(), salt, 120000, 256)

            val enteredHash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded

            spec.clearPassword()

            MessageDigest.isEqual(storedHash, enteredHash)

        } catch (e: Exception) {
            false
        }
    }
}
