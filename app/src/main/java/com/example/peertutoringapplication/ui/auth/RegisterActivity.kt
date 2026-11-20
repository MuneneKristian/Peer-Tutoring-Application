package com.example.peertutoringapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.repository.UserRepository
import com.example.peertutoringapplication.databinding.ActivityRegisterBinding
import com.example.peertutoringapplication.viewmodel.UserViewModel
import com.example.peertutoringapplication.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(repository)
        )[UserViewModel::class.java]

        setupRegistrationButton()
        observeRegistrationMessage()

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRegistrationButton() {

        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            val selectedRoleId = binding.rgRole.checkedRadioButtonId

            if (selectedRoleId == -1) {
                Toast.makeText(
                    this,
                    "Please select your role.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val role = when (selectedRoleId) {
                binding.rbTutor.id -> "Tutor"
                binding.rbTutee.id -> "Tutee"
                binding.rbBoth.id -> "Both"
                else -> ""
            }

            userViewModel.registerUser(
                firstName,
                lastName,
                email,
                password,
                role
            )
        }
    }

    private fun observeRegistrationMessage() {

        lifecycleScope.launch {
            userViewModel.registrationMessage.collect { message ->
                if (message != null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
