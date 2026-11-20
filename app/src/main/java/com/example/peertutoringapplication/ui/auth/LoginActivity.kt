package com.example.peertutoringapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.repository.UserRepository
import com.example.peertutoringapplication.databinding.ActivityLoginBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.UserViewModel
import com.example.peertutoringapplication.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(repository)
        )[UserViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            userViewModel.loginUser(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        observeLoginMessage()
        observeLoggedInUser()
    }

    private fun observeLoginMessage() {
        lifecycleScope.launch {
            userViewModel.loginMessage.collect { message ->
                if (message != null) {
                    Toast.makeText(
                        this@LoginActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeLoggedInUser() {
        lifecycleScope.launch {
            userViewModel.loggedInUser.collect { user ->
                if (user != null) {
                    sessionManager.saveUserSession(
                        userId = user.userId,
                        role = user.role
                    )
                }
            }
        }
    }
}
